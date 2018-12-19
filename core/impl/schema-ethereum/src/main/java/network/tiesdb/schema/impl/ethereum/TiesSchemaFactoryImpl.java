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
package network.tiesdb.schema.impl.ethereum;

import network.tiesdb.schema.api.TiesSchema;
import network.tiesdb.schema.api.TiesSchemaFactory;
import network.tiesdb.schema.impl.ethereum.impl.TiesSchemaEthereum;
import network.tiesdb.service.api.TiesService;

public class TiesSchemaFactoryImpl implements TiesSchemaFactory {

    private final TiesSchemaConfigImpl schemaConfig;

    public TiesSchemaFactoryImpl(TiesSchemaConfigImpl schemaConfig) {
        this.schemaConfig = schemaConfig;
    }

    @Override
    public TiesSchema createSchema(TiesService service) {
        if (null == service) {
            throw new NullPointerException("The service should not be null");
        }
        try {
            return new TiesSchemaEthereum(schemaConfig);
        } catch (Throwable e) {
            throw new RuntimeException("Can't obtain TiesDB schema", e);
        }
    }

}
