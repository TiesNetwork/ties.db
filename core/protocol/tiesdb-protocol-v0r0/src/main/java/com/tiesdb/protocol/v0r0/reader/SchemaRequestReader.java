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
package com.tiesdb.protocol.v0r0.reader;

import static com.tiesdb.protocol.v0r0.reader.ReaderUtil.acceptEach;
import static com.tiesdb.protocol.v0r0.reader.ReaderUtil.end;

import java.math.BigInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tiesdb.protocol.exception.TiesDBProtocolException;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.Conversation;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.Conversation.Event;
import com.tiesdb.protocol.v0r0.ebml.TiesDBRequestConsistency;
import com.tiesdb.protocol.v0r0.ebml.format.TiesDBRequestConsistencyFormat;

import one.utopic.sparse.ebml.format.BigIntegerFormat;
import one.utopic.sparse.ebml.format.UTF8StringFormat;

public class SchemaRequestReader implements Reader<SchemaRequestReader.SchemaRequest> {

    private static final Logger LOG = LoggerFactory.getLogger(SchemaRequestReader.class);

    public static class SchemaRequest implements Reader.Request {

        private BigInteger messageId;
        private TiesDBRequestConsistency consistency;

        private String tablespaceName;
        private String tableName;

        @Override
        public String toString() {
            return "SchemaRequest [messageId=" + messageId + ", consistency=" + consistency + ", tablespaceName=" + tablespaceName
                    + ", tableName=" + tableName + "]";
        }

        @Override
        public <T> T accept(Visitor<T> v) throws TiesDBProtocolException {
            return v.on(this);
        }

        public TiesDBRequestConsistency getConsistency() {
            return consistency;
        }

        @Override
        public BigInteger getMessageId() {
            return messageId;
        }

        public String getTablespaceName() {
            return tablespaceName;
        }

        public String getTableName() {
            return tableName;
        }

    }

    public boolean acceptSchemaRequest(Conversation session, Event e, SchemaRequest r) throws TiesDBProtocolException {
        switch (e.getType()) {
        case CONSISTENCY:
            r.consistency = session.read(TiesDBRequestConsistencyFormat.INSTANCE);
            LOG.debug("CONSISTENCY : {}", r.consistency);
            end(session, e);
            return true;
        case MESSAGE_ID:
            r.messageId = session.read(BigIntegerFormat.INSTANCE);
            LOG.debug("MESSAGE_ID : {}", r.messageId);
            end(session, e);
            return true;
        case TABLESPACE_NAME:
            r.tablespaceName = session.read(UTF8StringFormat.INSTANCE);
            LOG.debug("SCHEMA_TABLESPACE_NAME: {}", r.tablespaceName);
            end(session, e);
            return true;
        case TABLE_NAME:
            r.tableName = session.read(UTF8StringFormat.INSTANCE);
            LOG.debug("SCHEMA_TABLE_NAME: {}", r.tableName);
            end(session, e);
            return true;
        // $CASES-OMITTED$
        default:
            // throw new TiesDBProtocolException("Illegal packet format");
        }
        return false;
    }

    @Override
    public boolean accept(Conversation session, Event e, SchemaRequest schemaRequest) throws TiesDBProtocolException {
        acceptEach(session, e, this::acceptSchemaRequest, schemaRequest);
        return true;
    }

}
