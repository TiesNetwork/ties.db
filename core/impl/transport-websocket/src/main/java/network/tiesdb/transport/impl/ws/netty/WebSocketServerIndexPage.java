/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package network.tiesdb.transport.impl.ws.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;

/**
 * Generates the demo HTML page which is served at http://localhost:8080/
 */
public final class WebSocketServerIndexPage {

    private static final String NEWLINE = "\r\n";
	private static final String STYLE = "eclipse";

    public static ByteBuf getContent(String webSocketLocation) {
        return Unpooled.copiedBuffer(
                "<html><head><title>Web Socket Test</title></head>" + NEWLINE +
        		"<link rel=stylesheet href=\"http://codemirror.net/lib/codemirror.css\">" + NEWLINE +
        		"<link rel=stylesheet href=\"http://codemirror.net/theme/" + STYLE + ".css\">" + NEWLINE +
        		"<style type=\"text/css\">" + NEWLINE +
        		".CodeMirror { border: 1px solid black; font-size:13px }" + NEWLINE +
        		"</style>" + NEWLINE +
                "<script src=\"http://codemirror.net/lib/codemirror.js\"></script>" + NEWLINE +
                "<script src=\"http://codemirror.net/mode/javascript/javascript.js\"></script>" + NEWLINE +
                "<script type=\"text/javascript\">" + NEWLINE +
                "  function onLoad() {" + NEWLINE +
                "    let options = {" + NEWLINE +
                "      mode: \"javascript\"," + NEWLINE +
                "      theme: \"" + STYLE + "\"" + NEWLINE +
                "    };" + NEWLINE +
                "    var editor = CodeMirror.fromTextArea(document.getElementById('responseText'), options);" + NEWLINE +
                "    document.getElementById('responseText').editor = editor;" + NEWLINE +
                "    var areaEditor = CodeMirror.fromTextArea(document.getElementById('messageArea'), options);" + NEWLINE +
                "    document.getElementById('messageArea').editor = areaEditor;" + NEWLINE +
                "    areaEditor.setValue(localStorage.getItem('messageAreaText') || \"{\\\"Hello\\\":\\\"World!\\\"}\");" + NEWLINE +
                "  }" + NEWLINE +
                "</script>" + NEWLINE +
                "<body onload=\"onLoad()\">" + NEWLINE +
                "<script type=\"text/javascript\">" + NEWLINE +
                "let socket;" + NEWLINE +
                "function openSocket(callback) {" + NEWLINE +
                "  let socket;" + NEWLINE +
                "  if (!window.WebSocket) {" + NEWLINE +
                "    window.WebSocket = window.MozWebSocket;" + NEWLINE +
                "  }" + NEWLINE +
                "  if (window.WebSocket) {" + NEWLINE +
                "    socket = new WebSocket(\"" + webSocketLocation + "\");" + NEWLINE +
                "    socket.onmessage = function(event) {" + NEWLINE +
                "      var ta = document.getElementById('responseText').editor;" + NEWLINE +
                "      ta.setValue(\"/* \" + new Date().toLocaleString() + \" */\\n\" + event.data + \"\\n\" + ta.getValue());" + NEWLINE +
                "    };" + NEWLINE +
                "    socket.onopen = function(event) {" + NEWLINE +
                "      var ta = document.getElementById('responseText').editor;" + NEWLINE +
                "      ta.setValue(\"### Web Socket opened ###\\n\" + ta.getValue());" + NEWLINE +
                "      if(callback !== undefined){ callback(socket); }" + NEWLINE +
                "    };" + NEWLINE +
                "    socket.onclose = function(event) {" + NEWLINE +
                "      var ta = document.getElementById('responseText').editor;" + NEWLINE +
                "      ta.setValue(\"### Web Socket closed ###\\n\" + ta.getValue()); " + NEWLINE +
                "    };" + NEWLINE +
                "    return socket;" + NEWLINE +
                "  } else {" + NEWLINE +
                "    alert(\"Your browser does not support Web Socket.\");" + NEWLINE +
                "  }" + NEWLINE +
                "}" + NEWLINE +
                NEWLINE +
                "function send(message) {" + NEWLINE +
                "  localStorage.setItem('messageAreaText', message);" + NEWLINE +
                "  if (!window.WebSocket) { return; }" + NEWLINE +
                "  if (socket !== undefined && socket.readyState == WebSocket.OPEN) {" + NEWLINE +
                "    socket.send(message);" + NEWLINE +
                "  } else {" + NEWLINE +
                "    socket = openSocket(function(socket) { socket.send(message); });" + NEWLINE +
                "  }" + NEWLINE +
                '}' + NEWLINE +
                "</script>" + NEWLINE +
                "<form onsubmit=\"return false;\">" + NEWLINE +
                "<textarea id=\"messageArea\" name=\"message\" style=\"width:100%;height:300px;\"></textarea><br/>" + NEWLINE +
                "<input type=\"button\" value=\"Send Web Socket Data\"" + NEWLINE +
                "       onclick=\"send(document.getElementById('messageArea').editor.getValue())\" />" + NEWLINE +
                "<h3>Output</h3>" + NEWLINE +
                "<textarea id=\"responseText\" style=\"width:500px;height:300px;\"></textarea>" + NEWLINE +
                "</form>" + NEWLINE +
                "</body>" + NEWLINE +
                "</html>" + NEWLINE, CharsetUtil.US_ASCII);
    }

    private WebSocketServerIndexPage() {
        // Unused
    }
}
