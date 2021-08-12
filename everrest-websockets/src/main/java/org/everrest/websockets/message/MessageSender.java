/*
 * Copyright (c) 2012-2021 Codenvy, S.A.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
package org.everrest.websockets.message;

import static jakarta.websocket.CloseReason.CloseCodes.VIOLATED_POLICY;
import static jakarta.websocket.RemoteEndpoint.Async;

import jakarta.websocket.CloseReason;
import jakarta.websocket.EncodeException;
import jakarta.websocket.SendHandler;
import jakarta.websocket.SendResult;
import jakarta.websocket.Session;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author andrew00x */
public class MessageSender {

  private static final Logger LOG = LoggerFactory.getLogger(MessageSender.class);
  // todo: make configurable
  private final int maxNumberOfMessageInQueue = 1_000_000_000;

  private final Session session;
  private final Async async;
  private final LinkedList<MessageWrapper> sendQueue;
  private final SendHandler sendHandler;

  private final Object lock = new Object();
  private volatile boolean sendingInProgress = false;

  public MessageSender(Session session) {
    this.session = session;
    async = session.getAsyncRemote();
    sendQueue = new LinkedList<>();
    sendHandler = new MessageSendHandler();
  }

  public void send(Message message) throws IOException, EncodeException {
    send(new MessageWrapper(message));
  }

  public void send(String text) throws IOException {
    send(new MessageWrapper(text));
  }

  public void send(byte[] bytes) throws IOException {
    send(new MessageWrapper(bytes));
  }

  private void send(MessageWrapper message) throws IOException {
    synchronized (lock) {
      if (sendingInProgress) {
        if (isMaxQueueCapacityExceeded()) {
          final String error = "Max size of message queue exceeded";
          session.close(new CloseReason(VIOLATED_POLICY, error));
          throw new IOException(error);
        } else {
          sendQueue.add(message);
        }
      } else {
        sendingInProgress = true;
        doSend(message);
      }
    }
  }

  private boolean isMaxQueueCapacityExceeded() {
    final int newSize = sendQueue.size() + 1;
    LOG.debug(
        " SendQueue size {} ,  maxNumberOfMessageInQueue {}", newSize, maxNumberOfMessageInQueue);
    return newSize > maxNumberOfMessageInQueue;
  }

  private void doSend(MessageWrapper messageForSending) {
    if (messageForSending.isText()) {
      async.sendText(messageForSending.getText(), sendHandler);
    } else if (messageForSending.isBinary()) {
      async.sendBinary(messageForSending.getBinary(), sendHandler);
    } else if (messageForSending.isMessage()) {
      async.sendObject(messageForSending.getMessage(), sendHandler);
    }
  }

  private static final class MessageWrapper {
    private final Message message;
    private final byte[] bytes;
    private final String text;

    MessageWrapper(String text) {
      this.text = text;
      this.message = null;
      this.bytes = null;
    }

    MessageWrapper(byte[] bytes) {
      this.bytes = bytes;
      this.message = null;
      this.text = null;
    }

    MessageWrapper(Message message) {
      this.message = message;
      this.bytes = null;
      this.text = null;
    }

    boolean isText() {
      return text != null;
    }

    boolean isBinary() {
      return bytes != null;
    }

    boolean isMessage() {
      return message != null;
    }

    Message getMessage() {
      return message;
    }

    ByteBuffer getBinary() {
      return ByteBuffer.wrap(bytes);
    }

    String getText() {
      return text;
    }
  }

  private class MessageSendHandler implements SendHandler {
    @Override
    public void onResult(SendResult result) {
      LOG.debug(
          " SendQueue size {} ,  maxNumberOfMessageInQueue {} result {}",
          sendQueue.size(),
          maxNumberOfMessageInQueue,
          result.isOK());

      if (!result.isOK()) {
        try {
          session.close();
        } catch (IOException ignored) {
        } finally {
          sendQueue.clear();
        }
      }
      synchronized (lock) {
        if (sendQueue.isEmpty()) {
          sendingInProgress = false;
        } else {
          MessageWrapper message = sendQueue.remove();
          doSend(message);
        }
      }
    }
  }
}
