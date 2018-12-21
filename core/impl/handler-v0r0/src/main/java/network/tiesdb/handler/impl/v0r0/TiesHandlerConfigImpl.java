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
package network.tiesdb.handler.impl.v0r0;

import network.tiesdb.context.api.TiesHandlerConfig;
import network.tiesdb.context.api.annotation.TiesConfigElement;

/**
 * TiesDB handler configuration implementation.
 * 
 * @author Anton Filatov (filatov@ties.network)
 */
@TiesConfigElement({ TiesHandlerConfigImpl.BINDING, TiesHandlerConfigImpl.SHORT_BINDING })
public class TiesHandlerConfigImpl implements TiesHandlerConfig {

    static final String BINDING = "network.tiesdb.handler.V0R0";
    static final String SHORT_BINDING = "HandlerV0R0";

    public TiesHandlerConfigImpl() {
        // NOP Is not empty config values
    }

    public TiesHandlerConfigImpl(String value) {
        // NOP If this constructor is called then config values is empty and we
        // should use default
    }

    @Override
    public TiesHandlerFactoryImpl getTiesHandlerFactory() {
        return new TiesHandlerFactoryImpl(this);
    }

}
