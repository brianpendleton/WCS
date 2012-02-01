/*
* Copyright 2012 Jeanfrancois Arcand
*
* Licensed under the Apache License, Version 2.0 (the "License"); you may not
* use this file except in compliance with the License. You may obtain a copy of
* the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
* WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
* License for the specific language governing permissions and limitations under
* the License.
*/
package org.jfarcand.wcs

import com.ning.http.client.{AsyncHttpClientConfig, AsyncHttpClient}
import scala.Predef._
import com.ning.http.client.websocket.{WebSocketByteListener, WebSocketTextListener, WebSocketUpgradeHandler}

class WebSocket(o: Options) {

  def this() = this (null)

  val config: AsyncHttpClientConfig.Builder = new AsyncHttpClientConfig.Builder
  val asyncHttpClient: AsyncHttpClient = new AsyncHttpClient(config.build)
  var webSocket: com.ning.http.client.websocket.WebSocket = null
  var textListener: WebSocketTextListener = new TextListenerWrapper(new MessageListener() {
    override def onMessage(s: String) {
    }
  })

  var binaryListener: WebSocketByteListener = new BinaryListenerWrapper(new MessageListener() {
    override def onMessage(s: Array[Byte]) {
    }
  })

  def open(s: String): WebSocket = {
    webSocket = asyncHttpClient.prepareGet(s).execute(new WebSocketUpgradeHandler.Builder()
      .addWebSocketListener(textListener)
      .addWebSocketListener(binaryListener)
      .build).get
    this
  }

  def close(): WebSocket = {
    webSocket.close();
    asyncHttpClient.close()
    this
  }

  def listener(l: MessageListener): WebSocket = {
    if (webSocket.isOpen) {
      webSocket.addMessageListener(new TextListenerWrapper(l))
      webSocket.addMessageListener(new BinaryListenerWrapper(l))
    } else {
      textListener = new TextListenerWrapper(l);
      binaryListener = new BinaryListenerWrapper(l);
    }
    this
  }

  def send(s: String): WebSocket = {
    webSocket.sendTextMessage(s)
    this
  }

  def send(s: Array[Byte]): WebSocket = {
    webSocket.sendMessage(s)
    this
  }
}

private class TextListenerWrapper(l: MessageListener) extends WebSocketTextListener {

  override def onOpen(websocket: com.ning.http.client.websocket.WebSocket) {
    l.onOpen()
  }

  override def onClose(websocket: com.ning.http.client.websocket.WebSocket) {
    l.onClose()
  }

  override def onError(t: Throwable) {
    l.onError(t)
  }

  override def onMessage(s: String) {
    l.onMessage(s)
  }

  override def onFragment(fragment: String, last: Boolean) {}
}

private class BinaryListenerWrapper(l: MessageListener) extends WebSocketByteListener {

  override def onOpen(websocket: com.ning.http.client.websocket.WebSocket) {
    l.onOpen()
  }

  override def onClose(websocket: com.ning.http.client.websocket.WebSocket) {
    l.onClose()
  }

  override def onError(t: Throwable) {
    l.onError(t)
  }

  override def onMessage(s: Array[Byte]) {
    l.onMessage(s)
  }

  override  def onFragment(fragment: Array[Byte], last: Boolean) {}
}

