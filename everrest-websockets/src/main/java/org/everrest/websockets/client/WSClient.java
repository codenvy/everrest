/*
 * Copyright (C) 2012 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.everrest.websockets.client;

import org.apache.commons.codec.binary.Base64;
import org.everrest.core.util.Logger;
import org.everrest.websockets.message.MessageConverter;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author andrew00x
 */
public class WSClient {
    /** Max size of message payload. See http://tools.ietf.org/html/rfc6455#section-5.2 */
    public static final  int DEFAULT_MAX_MESSAGE_PAYLOAD_SIZE = 2 * 1024 * 1024;
    private static final int DEFAULT_BUFFER_SIZE              = 8 * 1024;

    private static final Logger     LOG                   = Logger.getLogger(WSClient.class);
    private static final String     GLOBAL_WS_SERVER_UUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
    private static final Random     RANDOM                = new Random();
    private static final Charset    UTF8_CS               = Charset.forName("UTF-8");
    private static final char[]     CHARS                 = new char[36];
    private static final int        MASK_SIZE             = 4;
    private static final AtomicLong sequence              = new AtomicLong(1);

    static {
        int i = 0;
        for (int c = 48; c <= 57; c++) {
            CHARS[i++] = (char)c;
        }
        for (int c = 97; c <= 122; c++) {
            CHARS[i++] = (char)c;
        }
    }

    private final ExecutorService             executor;
    private final URI                         target;
    private final int                         maxMessagePayloadSize;
    private final String                      secWebSocketKey;
    private final List<ClientMessageListener> listeners;

    private Socket       socket;
    private InputStream  in;
    private OutputStream out;
    private ByteBuffer   inputBuffer;

    // Thread that reads from socket check this.
    private volatile boolean connected;

    /**
     * Create new websocket client.
     *
     * @param target
     *         connection URI, e.g. <i>ws://localhost:8080/websocket</i>
     * @param listeners
     *         message listeners
     * @throws IllegalArgumentException
     *         if any of the following conditions are met:
     *         <ul>
     *         <li><code>target</code> is <code>null</code></li>
     *         <li>protocol specified in <code>target</code> not supported</li>
     *         <li><code>listeners</code> is <code>null</code></li>
     *         </ul>
     * @see #DEFAULT_MAX_MESSAGE_PAYLOAD_SIZE
     */
    public WSClient(URI target, ClientMessageListener... listeners) {
        this(target, DEFAULT_MAX_MESSAGE_PAYLOAD_SIZE, listeners);
    }

    /**
     * Create new websocket client.
     *
     * @param target
     *         connection URI, e.g. <i>ws://localhost:8080/websocket</i>
     * @param maxMessagePayloadSize
     *         max size of data in message. If received message contains payload greater then this value IOException thrown
     *         when read such message
     * @param listeners
     *         message listeners
     * @throws IllegalArgumentException
     *         if any of the following conditions are met:
     *         <ul>
     *         <li><code>target</code> is <code>null</code></li>
     *         <li>protocol specified in <code>target</code> not supported</li>
     *         <li><code>maxMessagePayloadSize</code> is zero or negative</li>
     *         <li><code>listeners</code> is <code>null</code></li>
     *         </ul>
     * @see #DEFAULT_MAX_MESSAGE_PAYLOAD_SIZE
     * @see MessageConverter
     */
    public WSClient(URI target, int maxMessagePayloadSize, ClientMessageListener... listeners) {
        if (target == null) {
            throw new IllegalArgumentException("Connection URI may not be null. ");
        }

        if (!"ws".equals(target.getScheme())) {
            // TODO: add 'wss' support
            throw new IllegalArgumentException(String.format("Unsupported scheme: %s", target.getScheme()));
        }

        if (maxMessagePayloadSize < 1) {
            throw new IllegalArgumentException(String.format("Invalid max message payload size: %d", maxMessagePayloadSize));
        }

        if (listeners == null) {
            throw new IllegalArgumentException("listeners may not be null. ");
        }

        this.target = target;
        this.maxMessagePayloadSize = maxMessagePayloadSize;
        executor = Executors.newSingleThreadExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                final Thread t = new Thread(r, "everrest.WSClient" + sequence.getAndIncrement());
                t.setDaemon(true);
                return t;
            }
        });
        this.listeners = new ArrayList<>(listeners.length);
        Collections.addAll(this.listeners, listeners);

        secWebSocketKey = generateSecKey();
    }

    public URI getUri() {
        return target;
    }

    public synchronized boolean isConnected() {
        return connected;
    }

    /**
     * Connect to remote server.
     *
     * @param timeout
     *         connection timeout value in seconds
     * @throws IOException
     *         if connection failed
     * @throws IllegalArgumentException
     *         if <code>timeout</code> zero or negative
     */
    public synchronized void connect(long timeout) throws IOException {
        if (timeout < 1) {
            throw new IllegalArgumentException(String.format("Invalid timeout: %d", timeout));
        }

        if (connected) {
            throw new IOException("Already connected.");
        }

        try {
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        socket = new Socket(target.getHost(), target.getPort());
                        in = socket.getInputStream();
                        out = socket.getOutputStream();
                        out.write(getHandshake());
                        validateResponseHeaders();
                        inputBuffer = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE);
                        connected = true;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }).get(timeout, TimeUnit.SECONDS); // Wait for connection.
        } catch (InterruptedException e) {
            //
            throw new IOException(e.getMessage(), e);
        } catch (ExecutionException e) {
            // It is RuntimeException for sure.
            RuntimeException re = (RuntimeException)e.getCause();
            Throwable cause = re.getCause();
            if (cause instanceof IOException) {
                throw (IOException)cause;
            }
            throw re;
        } catch (TimeoutException e) {
            // Connection time out reached.
            throw new SocketTimeoutException("Connection timeout. ");
        } finally {
            if (!connected) {
                executor.shutdown();
            }
        }

        // Start reading from socket.
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    read();
                } catch (ConnectionException e) {
                    LOG.error(e.getMessage(), e);
                    onClose(e.status, e.getMessage());
                } catch (Exception e) {
                    // All unexpected errors represents as protocol error, status: 1002.
                    LOG.error(e.getMessage(), e);
                    onClose(1002, e.getMessage());
                }
            }
        });

        // Notify listeners about connection open.
        onOpen();
    }

    /**
     * Close connection to remote server. Method has no effect if connection already closed.
     *
     * @throws IOException
     *         if i/o error occurred when try to close connection.
     */
    public synchronized void disconnect() throws IOException {
        if (!connected) {
            // Already closed or not connected.
            return;
        }

        writeFrame((byte)0x88, new byte[0]);
    }

    /**
     * Send text message.
     *
     * @param message
     *         text message
     * @throws IOException
     *         if any i/o errors occurred
     * @throws IllegalArgumentException
     *         if message is <code>null</code>
     */
    public synchronized void send(String message) throws IOException {
        if (!connected) {
            throw new IOException("Not connected. ");
        }

        if (message == null) {
            throw new IllegalArgumentException("Message may not be null. ");
        }

        // Send 'text' message without fragments.
        writeFrame((byte)0x81, UTF8_CS.encode(message).array());
    }

    /**
     * Send bin message.
     *
     * @param message
     *         min message
     * @throws IOException
     *         if any i/o errors occurred
     * @throws IllegalArgumentException
     *         if message is <code>null</code>
     */
    public synchronized void send(byte[] message) throws IOException {
        if (!connected) {
            throw new IOException("Not connected. ");
        }

        if (message == null) {
            throw new IllegalArgumentException("Message may not be null. ");
        }

        // Send 'bin' message without fragments.
        writeFrame((byte)0x82, message);
    }

    /**
     * Send ping message
     *
     * @param message
     *         message body
     * @throws IOException
     *         if any i/o errors occurred
     * @throws IllegalArgumentException
     *         if message length is greater than 125 bytes
     */
    public synchronized void ping(byte[] message) throws IOException {
        if (!connected) {
            throw new IOException("Not connected. ");
        }

        if (message == null) {
            message = new byte[0];
        } else if (message.length > 125) {
            throw new IllegalArgumentException("Ping message to large, may not be greater than 125 bytes. ");
        }

        writeFrame((byte)0x89, message);
    }

    /**
     * Get value for "Origin" header for sending to server when handshake. By default this method returns
     * <code>null</code>.
     *
     * @return value for "Origin" header for sending to server when handshake
     */
    protected String getOrigin() {
        return null;
    }

    /**
     * Get value for "Sec-WebSocket-Protocol" header for sending to server when handshake. By default this method
     * returns<code>null</code>.
     *
     * @return value for "Sec-WebSocket-Protocol" header for sending to server when handshake
     */
    protected String[] getSubProtocols() {
        return null;
    }

    //

    private byte[] getHandshake() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter handshake = new PrintWriter(out);
        handshake.format("GET %s HTTP/1.1\r\n", target.getPath());
        final int port = target.getPort();
        if (port == 80) {
            handshake.format("Host: %s\r\n", target.getHost());
        } else {
            handshake.format("Host: %s:%d\r\n", target.getHost(), port);
        }
        handshake.append("Upgrade: Websocket\r\n");
        handshake.append("Connection: Upgrade\r\n");
        String[] subProtocol = getSubProtocols();
        if (subProtocol != null && subProtocol.length > 0) {
            handshake.format("Sec-WebSocket-Protocol: %s\r\n", Arrays.toString(subProtocol));
        }
        handshake.format("Sec-WebSocket-Key: %s\r\n", secWebSocketKey);
        handshake.format("Sec-WebSocket-Version: %d\r\n", 13);
        handshake.append("Sec-WebSocket-Protocol: chat\r\n");
        String origin = getOrigin();
        if (origin != null) {
            handshake.format("Origin: %s\r\n", origin);
        }
        handshake.append('\r');
        handshake.append('\n');
        handshake.flush();
        return out.toByteArray();
    }

    private void onOpen() {
        for (ClientMessageListener listener : listeners) {
            try {
                listener.onOpen(this);
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    private void onMessage(String message) {
        for (ClientMessageListener listener : listeners) {
            try {
                listener.onMessage(message);
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    private void onMessage(byte[] message) {
        for (ClientMessageListener listener : listeners) {
            try {
                listener.onMessage(message);
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    private void onPong(byte[] message) {
        for (ClientMessageListener listener : listeners) {
            try {
                listener.onPong(message);
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    private void onClose(int status, String message) {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
            LOG.error(e.getMessage(), e);
        }

        inputBuffer.clear();

        for (ClientMessageListener listener : listeners) {
            try {
                listener.onClose(status, message);
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }

        listeners.clear();

        executor.shutdown();
        connected = false;
    }

    private String generateSecKey() {
        int length = RANDOM.nextInt(CHARS.length);
        byte[] b = new byte[length];
        for (int i = 0; i < length; i++) {
            b[i] = (byte)CHARS[RANDOM.nextInt(CHARS.length)];
        }
        return Base64.encodeBase64String(b);
    }

    private byte[] generateMask() {
        byte[] mask = new byte[MASK_SIZE];
        RANDOM.nextBytes(mask);
        return mask;
    }

    private byte[] getLengthAsBytes(long length) {
        if (length <= 125) {
            return new byte[]{(byte)length};
        }
        if (length <= 0xFFFF) {
            byte[] bytes = new byte[3];
            bytes[0] = 126;
            bytes[1] = (byte)(length >> 8);
            bytes[2] = (byte)(length & 0xFF);
            return bytes;
        }
        byte[] bytes = new byte[9];
        // Payload length never greater then max integer: (2^31)-1
        bytes[0] = 127;
        bytes[1] = 0;
        bytes[2] = 0;
        bytes[3] = 0;
        bytes[4] = 0;
        bytes[5] = (byte)(length >> 24);
        bytes[6] = (byte)(length >> 16);
        bytes[7] = (byte)(length >> 8);
        bytes[8] = (byte)(length & 0xFF);
        return bytes;
    }

    private void validateResponseHeaders() throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String line = br.readLine();
        if (!"HTTP/1.1 101 Switching Protocols".equals(line)) {
            throw new IOException("Invalid server response. Expected status is 101 'Switching Protocols'. ");
        }

        Map<String, String> headers = new HashMap<>();
        while (!((line = br.readLine()) == null || line.isEmpty())) {
            int colon = line.indexOf(':');
            if (colon > 0 && colon < line.length()) {
                headers.put(line.substring(0, colon).trim().toLowerCase(), line.substring(colon + 1).trim());
            }
        }

        // 'Upgrade' header
        String header = headers.get("upgrade");
        if (!"websocket".equals(header)) {
            throw new IOException(String.format("Invalid 'Upgrade' response header. Returned '%s' but 'websocket' expected. ", header));
        }

        // 'Connection' header
        header = headers.get("connection");
        if (!"upgrade".equals(header)) {
            throw new IOException(String.format("Invalid 'Connection' response header. Returned '%s' but 'upgrade' expected. ", header));
        }

        // 'Sec-WebSocket-Accept' header
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            // should never happen.
            throw new IllegalStateException(e.getMessage(), e);
        }
        md.reset();
        byte[] digest = md.digest((secWebSocketKey + GLOBAL_WS_SERVER_UUID).getBytes());
        final String expectedWsSecurityAccept = Base64.encodeBase64String(digest);
        header = headers.get("sec-websocket-accept");
        if (!expectedWsSecurityAccept.equals(header)) {
            throw new IOException("Invalid 'Sec-WebSocket-Accept' response header.");
        }
    }

    private static final int TEXT = 1;
    private static final int BIN  = 1 << 1;

    private int type;

    private void read() throws IOException {
        while (connected) {
            final int firstByte = in.read();
            if (firstByte < 0) {
                throw new EOFException("Failed read next websocket frame, end of the stream was reached. ");
            }

            // Check most significant bit in this byte. It always set in '1' if this fragment is final fragment.
            // In other word each message may not be sent in more then one fragment.
            final boolean fin = (firstByte & 0x80) != 0;
            final byte opCode = (byte)(firstByte & 0x0F);

            byte[] payload;
            switch (opCode) {
                case 0: // continuation frame
                    payload = readFrame();
                    saveInInputBuffer(payload);
                    // Only data frames might be fragmented. Control frames may not be fragmented.
                    // So we can't get here with any control frames, e.g. with ping/pong messages.
                    if (fin) {
                        if (type == TEXT) {
                            onMessage(getStringFormInputBuffer());
                        } else if (type == BIN) {
                            onMessage(getBytesFormInputBuffer());
                        }
                    }
                    break;
                case 1: // text frame
                    payload = readFrame();
                    if (fin) {
                        onMessage(new String(payload, UTF8_CS));
                    } else {
                        saveInInputBuffer(payload);
                        type = TEXT;
                    }
                    break;
                case 2: // binary frame
                    payload = readFrame();
                    if (fin) {
                        onMessage(payload);
                    } else {
                        saveInInputBuffer(payload);
                        type = BIN;
                    }
                    break;
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                    // Do nothing fo this. They are reserved for further non-control frames.
                    break;
                case 8: // connection close
                    payload = readFrame();
                    int status;
                    // Read status.
                    if (payload.length > 0) {
                        status = ((payload[0] & 0xFF) << 8);
                        status += (payload[1] & 0xFF);
                    } else {
                        status = 0; // No status.
                    }
                    String message = null;
                    if (!(status == 0 || status == 1000)) {
                        // Two bytes contains status code. The rest of bytes is message.
                        if (payload.length > 2) {
                            message = new String(payload, 2, payload.length - 2, UTF8_CS);
                        }
                        LOG.warn("Close status: {}, message: {} ", status, message);
                    }
                    // Specification says: body is not guaranteed to be human readable.
                    // Send body to the listeners here if server provides it and let listeners decide what to do.
                    onClose(status, message);
                    break;
                case 9: // ping
                    payload = readFrame();
                    // 'pong' response for the 'ping' message.
                    writeFrame((byte)0x8A, payload);
                    LOG.debug("Ping: {} ", new String(payload, UTF8_CS));
                    break;
                case 0x0A: // pong
                    payload = readFrame();
                    onPong(payload);
                    break;
                case 0x0B:
                case 0x0C:
                case 0x0D:
                case 0x0E:
                case 0x0F:
                    // Do nothing fo this.
                    break;
                default:
                    throw new ConnectionException(1003, String.format("Invalid opcode: '%s' ", Integer.toHexString(opCode)));
            }
            if (socket.isClosed()) {
                // May be server going down, we did not receive 'close' op_code but connection is lost.
                onClose(1006, null);
            }
        }
    }

    private byte[] readFrame() throws IOException {
        // This byte contains info about message mask and about length of payload.
        final int secondByte = in.read();
        if (secondByte < 0) {
            throw new EOFException("Failed read next websocket frame, end of the stream was reached. ");
        }

        final boolean masked = (secondByte & 0x80) > 0;

        long length = (secondByte & 0x7F);
        if (length == 126) {
            byte[] block = new byte[2];
            readBlock(block);
            length = getPayloadLength(block);
        } else if (length == 127) {
            byte[] block = new byte[8];
            readBlock(block);
            length = getPayloadLength(block);
        }

        byte[] mask = null;
        if (masked) {
            mask = new byte[MASK_SIZE];
            readBlock(mask);
        }

        if (length > maxMessagePayloadSize) {
            throw new IOException(String.format("Message payload is to large, may not be greater than %d", maxMessagePayloadSize));
        }
        // Payload may not greater then max integer: (2^31)-1
        final byte[] payload = new byte[(int)length];
        readBlock(payload);

        if (mask != null) {
            // Unmask payload bytes if they masked.
            for (int i = 0; i < payload.length; i++) {
                payload[i] = (byte)(payload[i] ^ mask[i % 4]);
            }
        }

        return payload;
    }

    private void saveInInputBuffer(byte[] frame) {
        final int fSize = frame.length;
        if (inputBuffer.remaining() < fSize) {
            LOG.debug("Increase input buffer: {}", fSize);
            final int capacity = inputBuffer.capacity() + fSize;
            final ByteBuffer buf = ByteBuffer.allocate(capacity);
            inputBuffer.flip();
            buf.put(inputBuffer);
            inputBuffer = buf;
            LOG.debug("New input buffer size {}", inputBuffer.capacity());
        }
        inputBuffer.put(frame);
    }

    private String getStringFormInputBuffer() {
        inputBuffer.flip();
        final String str = UTF8_CS.decode(inputBuffer).toString();
        inputBuffer.clear();
        return str;
    }

    private byte[] getBytesFormInputBuffer() {
        inputBuffer.flip();
        final byte[] bytes = new byte[inputBuffer.remaining()];
        inputBuffer.get(bytes);
        inputBuffer.clear();
        return bytes;
    }

    private void writeFrame(byte opCode, byte[] payload) throws IOException {
        // Represent length of payload data as described in section 5.2. Base Framing Protocol of RFC-6455
        // See for details: http://tools.ietf.org/html/rfc6455#section-5.2
        final byte[] lengthBytes = getLengthAsBytes(payload.length);
        // Turn on 'mask' bit.
        lengthBytes[0] |= 0x80;
        // Generate mask bytes.
        final byte[] mask = generateMask();

        out.write(opCode);
        // Payload length bytes.
        out.write(lengthBytes);
        // Mask bytes.
        out.write(mask);

        for (int i = 0, length = payload.length; i < length; i++) {
            // Mask each byte of payload.
            out.write((payload[i] ^ mask[i % 4]));
        }

        out.flush();
    }

    private long getPayloadLength(byte[] bytes) throws IOException {
        if (!(bytes.length == 2 || bytes.length == 8)) {
            // Should never happen. Caller of this method must check to full reading of byte range.
            throw new IOException(String.format(
                    "Unable get payload length. Invalid length bytes. Length must be represented by 2 or 8 bytes but %d reached. ",
                    bytes.length));
        }
        return getLongFromBytes(bytes);
    }

    private long getLongFromBytes(byte[] bytes) throws IOException {
        long length = 0;
        for (int i = bytes.length - 1, shift = 0; i >= 0; i--, shift += 8) {
            length += ((bytes[i] & 0xFF) << shift);
        }
        return length;
    }

    private void readBlock(byte[] buff) throws IOException {
        int offset = 0;
        int length = buff.length;
        int r;
        while (offset < buff.length) {
            r = in.read(buff, offset, length - offset);
            if (r < 0) {
                throw new EOFException("Failed read next websocket frame, end of the stream was reached. ");
            }
            offset += r;
        }
    }

    @SuppressWarnings("serial")
    private static class ConnectionException extends IOException {
        private final int status;

        private ConnectionException(int status, String message) {
            super(message);
            this.status = status;
        }
    }
}
