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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tiesdb.protocol.exception.TiesDBProtocolException;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.Conversation;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.Conversation.Event;
import com.tiesdb.protocol.v0r0.ebml.TiesDBRequestConsistency;
import com.tiesdb.protocol.v0r0.ebml.format.TiesDBRequestConsistencyFormat;
import com.tiesdb.protocol.v0r0.reader.ChequeReader.Cheque;
import com.tiesdb.protocol.v0r0.reader.ComputeRetrieveReader.ComputeRetrieve;
import com.tiesdb.protocol.v0r0.reader.FieldRetrieveReader.FieldRetrieve;
import com.tiesdb.protocol.v0r0.reader.FilterReader.Filter;

import one.utopic.sparse.ebml.format.BigIntegerFormat;
import one.utopic.sparse.ebml.format.UTF8StringFormat;

public class RecollectionRequestReader implements Reader<RecollectionRequestReader.RecollectionRequest> {

    private static final Logger LOG = LoggerFactory.getLogger(RecollectionRequestReader.class);

    public static class RecollectionRequest implements Reader.Request {

        private BigInteger messageId;
        private TiesDBRequestConsistency consistency;

        private String tablespaceName;
        private String tableName;
        private List<Retrieve> retrieves = new LinkedList<>();
        private List<Filter> filters = new LinkedList<>();

        private List<Cheque> cheques = new LinkedList<>();

        @Override
        public String toString() {
            return "RecollectionRequest [messageId=" + messageId + ", consistency=" + consistency + ", tablespaceName=" + tablespaceName
                    + ", tableName=" + tableName + ", retrieves=" + retrieves + ", filters=" + filters + "]";
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

        public List<Retrieve> getRetrieves() {
            return retrieves;
        }

        public List<Filter> getFilters() {
            return filters;
        }

        public List<Cheque> getCheques() {
            return cheques;
        }

    }

    public static interface Retrieve {

        interface Visitor<T> {

            T on(FieldRetrieve retrieve);

            T on(ComputeRetrieve retrieve);

        }

        <T> T accept(Visitor<T> v);

    }

    private final FieldRetrieveReader fieldRetrieveReader = new FieldRetrieveReader();
    private final ComputeRetrieveReader computeRetrieveReader = new ComputeRetrieveReader();
    private final FilterReader filterReader = new FilterReader();
    private final ChequeListReader chequeListReader = new ChequeListReader();

    public boolean acceptRecollectionRequest(Conversation session, Event e, RecollectionRequest r) throws TiesDBProtocolException {
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
            LOG.debug("RECOLLECTION_TABLESPACE_NAME: {}", r.tablespaceName);
            end(session, e);
            return true;
        case TABLE_NAME:
            r.tableName = session.read(UTF8StringFormat.INSTANCE);
            LOG.debug("RECOLLECTION_TABLE_NAME: {}", r.tableName);
            end(session, e);
            return true;
        case RETRIEVE_LIST:
            acceptEach(session, e, this::acceptRetrieveList, r.retrieves);
            return true;
        case FILTER_LIST: {
            acceptEach(session, e, this::acceptRecollectionFilter, r.filters);
            return true;
        }
        case CHEQUE_LIST: {
            chequeListReader.accept(session, e, r.cheques::add);
            return true;
        }
        // $CASES-OMITTED$
        default:
            // throw new TiesDBProtocolException("Illegal packet format");
        }
        return false;
    }

    public boolean acceptRetrieveList(Conversation session, Event e, List<Retrieve> retrieve) throws TiesDBProtocolException {
        switch (e.getType()) {
        case RET_FIELD: {
            FieldRetrieve r = new FieldRetrieve();
            if (fieldRetrieveReader.accept(session, e, r)) {
                LOG.debug("RETRIEVE_FIELD : {}", r);
                retrieve.add(r);
                return true;
            }
            break;
        }
        case RET_COMPUTE: {
            ComputeRetrieve r = new ComputeRetrieve();
            if (computeRetrieveReader.accept(session, e, r)) {
                LOG.debug("RETRIEVE_COMPUTE : {}", r);
                retrieve.add(r);
                return true;
            }
            break;
        }
        // $CASES-OMITTED$
        default:
            // throw new TiesDBProtocolException("Illegal packet format");
        }
        return false;
    }

    public boolean acceptRecollectionFilter(Conversation session, Event e, List<Filter> filters) throws TiesDBProtocolException {
        switch (e.getType()) {
        case FILTER: {
            Filter f = new Filter();
            if (filterReader.accept(session, e, f)) {
                LOG.debug("FILTER : {}", f);
                filters.add(f);
                return true;
            }
            break;
        }
        // $CASES-OMITTED$
        default:
            // throw new TiesDBProtocolException("Illegal packet format");
        }
        return false;
    }

    @Override
    public boolean accept(Conversation session, Event e, RecollectionRequest recollectionRequest) throws TiesDBProtocolException {
        acceptEach(session, e, this::acceptRecollectionRequest, recollectionRequest);
        recollectionRequest.retrieves = Collections.unmodifiableList(recollectionRequest.retrieves);
        recollectionRequest.filters = Collections.unmodifiableList(recollectionRequest.filters);
        return true;
    }

}
