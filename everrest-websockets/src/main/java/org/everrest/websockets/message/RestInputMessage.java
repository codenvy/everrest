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

/**
 * REST input message.
 *
 * @author andrew00x
 */
public class RestInputMessage extends InputMessage {
  public static RestInputMessage newPingMessage(String uuid, String message) {
    final RestInputMessage instance = new RestInputMessage();
    instance.setUuid(uuid);
    instance.setMethod("POST");
    instance.setHeaders(new Pair[] {Pair.of("x-everrest-websocket-message-type", "ping")});
    instance.setBody(message);
    return instance;
  }

  public static RestInputMessage newSubscribeChannelMessage(String uuid, String channel) {
    final RestInputMessage instance = new RestInputMessage();
    instance.setUuid(uuid);
    instance.setMethod("POST");
    instance.setHeaders(
        new Pair[] {Pair.of("x-everrest-websocket-message-type", "subscribe-channel")});
    instance.setBody(String.format("{\"channel\":\"%s\"}", channel));
    return instance;
  }

  public static RestInputMessage newUnsubscribeChannelMessage(String uuid, String channel) {
    final RestInputMessage instance = new RestInputMessage();
    instance.setUuid(uuid);
    instance.setMethod("POST");
    instance.setHeaders(
        new Pair[] {Pair.of("x-everrest-websocket-message-type", "unsubscribe-channel")});
    instance.setBody(String.format("{\"channel\":\"%s\"}", channel));
    return instance;
  }

  private String method;
  private String path;
  private Pair[] headers;

  public RestInputMessage() {}

  /**
   * Get name of HTTP method specified for resource method, e.g. GET, POST, PUT, etc.
   *
   * @return name of HTTP method
   */
  public String getMethod() {
    return method;
  }

  /**
   * Set name of HTTP method specified for resource method, e.g. GET, POST, PUT, etc.
   *
   * @param method name of HTTP method
   */
  public void setMethod(String method) {
    this.method = method;
  }

  /**
   * Get resource path.
   *
   * @return resource path
   */
  public String getPath() {
    return path;
  }

  /**
   * Set resource path.
   *
   * @param path resource path
   */
  public void setPath(String path) {
    this.path = path;
  }

  /**
   * Get HTTP headers.
   *
   * @return HTTP headers
   */
  public Pair[] getHeaders() {
    return headers;
  }

  /**
   * Set HTTP headers.
   *
   * @param headers HTTP headers
   */
  public void setHeaders(Pair[] headers) {
    this.headers = headers;
  }
}
