/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2013] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */

package org.everrest.assured.util;


import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;

/**
 * Finds currently available server ports.
 *
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 * @see <a href="http://www.iana.org/assignments/port-numbers">IANA.org</a>
 */
public class AvailablePortFinder {
    /** The minimum number of server port number. */
    public static final int MIN_PORT_NUMBER = 1;
    /** The maximum number of server port number. */
    public static final int MAX_PORT_NUMBER = 49151;

    /** Creates a new instance. */
    private AvailablePortFinder() {
        // Do nothing
    }

    /**
     * Returns the {@link Set} of currently available port numbers (
     * {@link Integer}). This method is identical to
     * <code>getAvailablePorts(MIN_PORT_NUMBER, MAX_PORT_NUMBER)</code>.
     * <p/>
     * WARNING: this can take a very long time.
     */
    public static Set<Integer> getAvailablePorts() {
        return getAvailablePorts(MIN_PORT_NUMBER, MAX_PORT_NUMBER);
    }

    /**
     * Gets the next available port starting at the lowest port number.
     *
     * @throws NoSuchElementException
     *         if there are no ports available
     */
    public static int getNextAvailable() {
        return getNextAvailable(MIN_PORT_NUMBER);
    }

    /**
     * Gets the next available port starting at a port.
     *
     * @param fromPort
     *         the port to scan for availability
     * @throws NoSuchElementException
     *         if there are no ports available
     */
    public static int getNextAvailable(int fromPort) {
        if (fromPort < MIN_PORT_NUMBER || fromPort > MAX_PORT_NUMBER) {
            throw new IllegalArgumentException("Invalid start port: " + fromPort);
        }

        for (int i = fromPort; i <= MAX_PORT_NUMBER; i++) {
            if (available(i)) {
                return i;
            }
        }

        throw new NoSuchElementException("Could not find an available port " + "above " + fromPort);
    }

    /**
     * Checks to see if a specific port is available.
     *
     * @param port
     *         the port to check for availability
     */
    public static boolean available(int port) {
        if (port < MIN_PORT_NUMBER || port > MAX_PORT_NUMBER) {
            throw new IllegalArgumentException("Invalid start port: " + port);
        }

        ServerSocket ss = null;
        DatagramSocket ds = null;
        try {
            ss = new ServerSocket(port);
            ss.setReuseAddress(true);
            ds = new DatagramSocket(port);
            ds.setReuseAddress(true);
            return true;
        } catch (IOException e) {
            // Do nothing
        } finally {
            if (ds != null) {
                ds.close();
            }

            if (ss != null) {
                try {
                    ss.close();
                } catch (IOException e) {
               /* should not be thrown */
                }
            }
        }

        return false;
    }

    /**
     * Returns the {@link Set} of currently avaliable port numbers (
     * {@link Integer}) between the specified port range.
     *
     * @throws IllegalArgumentException
     *         if port range is not between {@link #MIN_PORT_NUMBER} and
     *         {@link #MAX_PORT_NUMBER} or <code>fromPort</code> if greater
     *         than <code>toPort</code>.
     */
    public static Set<Integer> getAvailablePorts(int fromPort, int toPort) {
        if (fromPort < MIN_PORT_NUMBER || toPort > MAX_PORT_NUMBER || fromPort > toPort) {
            throw new IllegalArgumentException("Invalid port range: " + fromPort + " ~ " + toPort);
        }

        Set<Integer> result = new TreeSet<Integer>();

        for (int i = fromPort; i <= toPort; i++) {
            ServerSocket s = null;

            try {
                s = new ServerSocket(i);
                result.add(Integer.valueOf(i));
            } catch (IOException e) {
                // Do nothing
            } finally {
                if (s != null) {
                    try {
                        s.close();
                    } catch (IOException e) {
                  /* should not be thrown */
                    }
                }
            }
        }

        return result;
    }
}
