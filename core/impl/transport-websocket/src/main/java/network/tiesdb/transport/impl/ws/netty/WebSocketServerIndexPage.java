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

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.HashMap;

import org.stringtemplate.v4.*;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;

/**
 * Generates the demo HTML page which is served at http://localhost:8080/
 */
public final class WebSocketServerIndexPage {

    private static final Charset DEFAULT_CHARSET = Charset.forName("UTF8");

    private WebSocketServerIndexPage() {
    }

    public static ByteBuf getContent(InputStream is) throws IOException {
        return getContent(is, null);
    }

    public static ByteBuf getContent(InputStream is, HashMap<String, Object> variables) throws IOException {
        String result = "";
        {
            StringBuilder sb = new StringBuilder();
            byte[] buffer = new byte[2048];
            for (int read = is.read(buffer); read != -1; read = is.read(buffer)) {
                sb.append(DEFAULT_CHARSET.decode(ByteBuffer.wrap(buffer, 0, read)));
            }
            result = sb.toString();
        }
        if (null != variables) {
            ST st = new ST(result, '$', '$');
            variables.forEach((key, value) -> st.add(key, value));
            result = st.render();
        }

        return Unpooled.copiedBuffer(result, CharsetUtil.UTF_8);
    }

}
