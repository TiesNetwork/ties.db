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
package com.tiesdb.protocol.v0r0.writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tiesdb.protocol.exception.TiesDBProtocolException;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.Conversation;
import com.tiesdb.protocol.v0r0.ebml.TiesDBRequestConsistency;
import com.tiesdb.protocol.v0r0.ebml.format.TiesDBRequestConsistencyFormat;
import com.tiesdb.protocol.v0r0.writer.AbstractFunctionWriter.ArgumentWriter;
import com.tiesdb.protocol.v0r0.writer.AbstractFunctionWriter.Function;
import com.tiesdb.protocol.v0r0.writer.ChequeWriter.Cheque;
import com.tiesdb.protocol.v0r0.writer.RecollectionRequestWriter.RecollectionRequest.Retrieve.Compute;
import com.tiesdb.protocol.v0r0.writer.RecollectionRequestWriter.RecollectionRequest.Retrieve.Field;

import static com.tiesdb.protocol.v0r0.ebml.TiesDBType.*;
import static com.tiesdb.protocol.v0r0.writer.WriterUtil.*;

import java.util.List;

import one.utopic.sparse.ebml.format.ASCIIStringFormat;
import one.utopic.sparse.ebml.format.BigIntegerFormat;
import one.utopic.sparse.ebml.format.UTF8StringFormat;

public class RecollectionRequestWriter implements Writer<RecollectionRequestWriter.RecollectionRequest> {

    private static final Logger LOG = LoggerFactory.getLogger(RecollectionRequestWriter.class);

    public static interface RecollectionRequest extends Writer.Request {

        interface Retrieve {

            interface Visitor<T> {

                T on(Field field) throws TiesDBProtocolException;

                T on(Compute compute) throws TiesDBProtocolException;

            }

            interface Field extends Retrieve {

                @Override
                default <T> T accept(Visitor<T> v) throws TiesDBProtocolException {
                    return v.on(this);
                }

                String getFieldName();

            }

            interface Compute extends Retrieve, Function {

                @Override
                default <T> T accept(Visitor<T> v) throws TiesDBProtocolException {
                    return v.on(this);
                }

                String getAlias();

                String getType();

            }

            <T> T accept(Visitor<T> v) throws TiesDBProtocolException;

        }

        interface Filter extends Function {

            String getFieldName();

        }

        @Override
        default <T> T accept(Visitor<T> v) throws TiesDBProtocolException {
            return v.on(this);
        }

        TiesDBRequestConsistency getConsistency();

        String getTablespaceName();

        String getTableName();

        List<Retrieve> getRetrieves();

        List<Filter> getFilters();

        List<Cheque> getCheques();

    }

    private final ArgumentWriter argumentWriter = new ArgumentWriter();
    private final RetrieveWriter retrieveWriter = new RetrieveWriter(argumentWriter);
    private final FilterWriter filterWriter = new FilterWriter(argumentWriter);
    private final ChequeWriter chequeWriter = new ChequeWriter();

    @Override
    public void accept(Conversation session, RecollectionRequest request) throws TiesDBProtocolException {
        LOG.debug("RecollectionRequest {}", request);
        List<Cheque> cheques = request.getCheques();
        write(RECOLLECTION_REQUEST, //
                write(CONSISTENCY, TiesDBRequestConsistencyFormat.INSTANCE, request.getConsistency()), //
                write(MESSAGE_ID, BigIntegerFormat.INSTANCE, request.getMessageId()), //
                write(TABLESPACE_NAME, UTF8StringFormat.INSTANCE, request.getTablespaceName()), //
                write(TABLE_NAME, UTF8StringFormat.INSTANCE, request.getTableName()), //
                write(RETRIEVE_LIST, write(retrieveWriter, request.getRetrieves())), //
                write(FILTER_LIST, write(filterWriter, request.getFilters())), // , //
                write(!cheques.isEmpty(), write(CHEQUE_LIST, //
                        write(chequeWriter, cheques)) //
                ) //
        ).accept(session);
    }

    public static class RetrieveWriter implements Writer<RecollectionRequest.Retrieve> {

        private final RetrieveComputeWriter retrieveComputeWriter;
        private final RetrieveFieldWriter retrieveFieldWriter = new RetrieveFieldWriter();

        public RetrieveWriter(ArgumentWriter argumentWriter) {
            retrieveComputeWriter = new RetrieveComputeWriter(argumentWriter);
        }

        @Override
        public void accept(Conversation session, RecollectionRequest.Retrieve retrieve) throws TiesDBProtocolException {
            retrieve.accept(new RecollectionRequest.Retrieve.Visitor<ConversationConsumer>() {

                @Override
                public ConversationConsumer on(Field field) {
                    return write(retrieveFieldWriter, field);
                }

                @Override
                public ConversationConsumer on(Compute compute) {
                    return write(retrieveComputeWriter, compute);
                }

            }).accept(session);
        }
    }

    public static class RetrieveFieldWriter implements Writer<RecollectionRequest.Retrieve.Field> {

        @Override
        public void accept(Conversation session, RecollectionRequest.Retrieve.Field retrieveField) throws TiesDBProtocolException {
            LOG.debug("RecollectionRequest.Retrieve.Field {}", retrieveField);
            write(RET_FIELD, UTF8StringFormat.INSTANCE, retrieveField.getFieldName()).accept(session);
        }
    }

    public static class RetrieveComputeWriter extends AbstractFunctionWriter<RecollectionRequest.Retrieve.Compute> {

        public RetrieveComputeWriter(ArgumentWriter argumentWriter) {
            super(argumentWriter);
        }

        @Override
        public void accept(Conversation session, RecollectionRequest.Retrieve.Compute retrieveCompute) throws TiesDBProtocolException {
            LOG.debug("RecollectionRequest.Retrieve.Compute {}", retrieveCompute);
            String alias = retrieveCompute.getAlias();
            write(RET_COMPUTE, //
                    write(alias != null, //
                            write(RET_COMPUTE_ALIAS, UTF8StringFormat.INSTANCE, alias)), //
                    write(RET_COMPUTE_TYPE, ASCIIStringFormat.INSTANCE, retrieveCompute.getType()), //
                    writeFunction(retrieveCompute) //
            ).accept(session);
        }
    }

    public static class FilterWriter extends AbstractFunctionWriter<RecollectionRequest.Filter> {

        public FilterWriter(ArgumentWriter argumentWriter) {
            super(argumentWriter);
        }

        @Override
        public void accept(Conversation session, RecollectionRequest.Filter filter) throws TiesDBProtocolException {
            LOG.debug("RecollectionRequest.Filter {}", filter);
            write(FILTER, //
                    write(FILTER_FIELD, UTF8StringFormat.INSTANCE, filter.getFieldName()), //
                    writeFunction(filter) //
            ).accept(session);
        }
    }

}
