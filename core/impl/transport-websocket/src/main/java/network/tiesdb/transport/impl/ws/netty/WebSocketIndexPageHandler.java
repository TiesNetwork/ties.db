/**
 * Copyright © 2017 Ties BV
 *
 * This file is part of Ties.DB project.
 *
 * Ties.DB project is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Ties.DB project is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with Ties.DB project. If not, see <https://www.gnu.org/licenses/lgpl-3.0>.
 */
package network.tiesdb.transport.impl.ws.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.CharsetUtil;

import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.METHOD_NOT_ALLOWED;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;

/**
 * Outputs index page content.
 */
public class WebSocketIndexPageHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final String websocketPath;
    private final URL staticResourcesUrl = getClass().getClassLoader().getResource("static/");

    public WebSocketIndexPageHandler(String websocketPath) {
        this.websocketPath = websocketPath;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
        // Handle a bad request.
        if (!req.getDecoderResult().isSuccess()) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST));
            return;
        }

        // Allow only GET methods.
        if (req.getMethod() != GET) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, METHOD_NOT_ALLOWED));
            return;
        }

        // Find resource file
        String requestUriStr = req.getUri();
        if (null == requestUriStr || requestUriStr.isEmpty() || "/".equals(requestUriStr)) {
            requestUriStr = "/index.html";
        }
        requestUriStr = requestUriStr.substring(1);

        URL resourceURL = new URL(staticResourcesUrl.toExternalForm() + requestUriStr);
        try {
            InputStream resourceStream = resourceURL.openStream();
            try (InputStream resourceStreamOpened = resourceStream) {

                // Send the resource data
                String resourceFileExtension = getFileExtension(requestUriStr);
                ByteBuf content = null;
                if ("html".equals(resourceFileExtension)) {
                    HashMap<String, Object> templateMap = new HashMap<>();
                    templateMap.put("websocketLocation", getWebSocketLocation(ctx.pipeline(), req, websocketPath));
                    content = WebSocketServerIndexPage.getContent(resourceStreamOpened, templateMap);
                } else {
                    content = WebSocketServerIndexPage.getContent(resourceStreamOpened);
                }
                if (null == content) {
                    sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, INTERNAL_SERVER_ERROR));
                    return;
                }
                FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK, content);

                switch (resourceFileExtension) {
                case "html":
                    res.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/html; charset=UTF-8");
                    break;
                case "css":
                    res.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/css; charset=UTF-8");
                    break;
                case "js":
                    res.headers().set(HttpHeaders.Names.CONTENT_TYPE, "application/javascript; charset=utf-8");
                    break;
                default:
                    break;
                }
                HttpHeaders.setContentLength(res, content.readableBytes());

                sendHttpResponse(ctx, req, res);
            }
        } catch (Exception e) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND));
        }
    }

    private String getFileExtension(String fileName) {
        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            return fileName.substring(i + 1);
        }
        return "";
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    private static void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
        // Generate an error page if response getStatus code is not OK (200).
        if (res.getStatus().code() != 200) {
            ByteBuf buf = Unpooled.copiedBuffer(res.getStatus().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
            HttpHeaders.setContentLength(res, res.content().readableBytes());
        }

        // Send the response and close the connection if necessary.
        ChannelFuture f = ctx.channel().writeAndFlush(res);
        if (!HttpHeaders.isKeepAlive(req) || res.getStatus().code() != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    private static String getWebSocketLocation(ChannelPipeline cp, HttpRequest req, String path) {
        String protocol = "ws";
        if (null != cp.get(SslHandler.class)) {
            // SSL in use so use Secure WebSockets
            protocol = "wss";
        }
        return protocol + "://" + req.headers().get(HttpHeaders.Names.HOST) + path;
    }
}
