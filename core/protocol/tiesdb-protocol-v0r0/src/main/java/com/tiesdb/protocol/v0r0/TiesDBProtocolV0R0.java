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
package com.tiesdb.protocol.v0r0;

import static com.tiesdb.protocol.api.Version.VersionComprator.REVISION;
import static com.tiesdb.protocol.v0r0.util.BinaryHelper.parseInt16;
import static com.tiesdb.protocol.v0r0.util.BinaryHelper.parseLong32;
import static com.tiesdb.protocol.v0r0.util.BinaryHelper.skip;
import static com.tiesdb.protocol.v0r0.util.BinaryHelper.writeBytes;
import static com.tiesdb.protocol.v0r0.util.BinaryHelper.writeInt16;
import static com.tiesdb.protocol.v0r0.util.BinaryHelper.writeLong32;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tiesdb.lib.crypto.checksum.ChecksumManager;
import com.tiesdb.lib.crypto.checksum.api.Checksum;
import com.tiesdb.lib.crypto.digest.DigestManager;
import com.tiesdb.protocol.api.TiesDBProtocol;
import com.tiesdb.protocol.api.TiesDBProtocolHandlerProvider;
import com.tiesdb.protocol.api.Version;
import com.tiesdb.protocol.exception.TiesDBException;
import com.tiesdb.protocol.exception.TiesDBProtocolException;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.Conversation.EventState;
import com.tiesdb.protocol.v0r0.ebml.TiesDBType;
import com.tiesdb.protocol.v0r0.ebml.TiesEBMLReader;
import com.tiesdb.protocol.v0r0.ebml.TiesEBMLReader.UnknownTiesEBMLType;
import com.tiesdb.protocol.v0r0.ebml.TiesEBMLWriter;
import com.tiesdb.protocol.v0r0.exception.CRCMissmatchException;
import com.tiesdb.protocol.v0r0.test.util.StreamInput;
import com.tiesdb.protocol.v0r0.util.CheckedConsumer;
import com.tiesdb.protocol.v0r0.util.CheckedSupplier;
import com.tiesdb.protocol.v0r0.util.EBMLHelper;

import one.utopic.abio.api.input.Input;
import one.utopic.abio.api.output.Output;
import one.utopic.sparse.api.Event.CommonEventType;
import one.utopic.sparse.api.Event.EventType;
import one.utopic.sparse.ebml.EBMLCode;
import one.utopic.sparse.ebml.EBMLEvent;
import one.utopic.sparse.ebml.EBMLReader;
import one.utopic.sparse.ebml.EBMLReader.EBMLReadFormat;
import one.utopic.sparse.ebml.EBMLType;
import one.utopic.sparse.ebml.EBMLWriter;
import one.utopic.sparse.ebml.EBMLWriter.EBMLWriteFormat;

public class TiesDBProtocolV0R0 implements TiesDBProtocol {

    private static final Logger LOG = LoggerFactory.getLogger(TiesDBProtocolV0R0.class);

    public static final Version VERSION = new Version(0, 0, 1);

    private static final byte[] PACKET_HEADER_MAGIC_NUMBER = new byte[] { (byte) 0xc0, 0x01, (byte) 0xba, 0x5e };
    private static final int PACKET_HEADER_RESERVED_LEN = 2;

    public static final String DEFAULT_DIGEST_ALG = DigestManager.KECCAK_256;

    static {
        LOG.debug("Preload TiesDB EBML types: {}", Arrays.toString(TiesDBType.values()));
    }

    @Override
    public Version getVersion() {
        return VERSION;
    }

    protected static Version parsePacketHeader(Input in) throws TiesDBProtocolException, IOException {
        {
            byte[] data = new byte[PACKET_HEADER_MAGIC_NUMBER.length];
            for (int i = 0; i < data.length; i++) {
                data[i] = in.readByte();
            }
            if (!Arrays.equals(data, PACKET_HEADER_MAGIC_NUMBER)) {
                throw new TiesDBProtocolException("Wrong packet magic number");
            }
        }
        Version version;
        {
            long dataCRC = parseLong32(in::readByte);
            Checksum checksum = ChecksumManager.getChecksum(ChecksumManager.CRC32);
            CheckedSupplier<Byte, IOException> sup = () -> checksum.updateVal(in.readByte());
            skip(sup, PACKET_HEADER_RESERVED_LEN);
            int major = parseInt16(sup);
            int minor = parseInt16(sup);
            int maint = parseInt16(sup);
            if (checksum.getValue() != dataCRC) {
                throw new CRCMissmatchException("CRC check failed in packet header");
            }
            version = new Version(major, minor, maint);
        }
        return version;
    }

    protected static void writePacketHeader(Version v, Output out) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Checksum checksum = ChecksumManager.getChecksum(ChecksumManager.CRC32);
        CheckedConsumer<Byte, IOException> con = (b) -> {
            checksum.update(b);
            baos.write(b);
        };
        for (int i = 0; i < PACKET_HEADER_RESERVED_LEN; i++) {
            con.accept((byte) 0);
        }
        writeInt16(con, v.getVersion());
        writeInt16(con, v.getRevision());
        writeInt16(con, v.getMaintence());

        writeBytes(out, PACKET_HEADER_MAGIC_NUMBER);
        writeLong32((b) -> out.writeByte(b), checksum.getValue());
        writeBytes(out, baos.toByteArray());
    }

    protected void openChannel(TiesDBChannelOutput output, CheckedConsumer<Conversation, TiesDBException> sessionConsumer)
            throws TiesDBException, IOException {
        requireNonNull(output);
        try {
            TiesDBChannelOutput bufferedOutput = new TiesDBChannelBufferedOutput(output, out -> {
                writePacketHeader(VERSION, out);
                LOG.debug("Header is written for {}", sessionConsumer);
            });
            Conversation session = openConversation(new StreamInput(new InputStream() {
                @Override
                public int read() throws IOException {
                    return -1;
                }
            }), bufferedOutput);
            sessionConsumer.accept(session);
            bufferedOutput.flush();
        } catch (Exception e) {
            writePacketHeader(VERSION, output);
            throw new TiesDBException("Channel open failed", e);
        }
    }

    protected void processChannel(TiesDBChannelInput input, TiesDBChannelOutput output,
            CheckedConsumer<Conversation, TiesDBException> sessionConsumer) throws TiesDBException, IOException {
        requireNonNull(input);
        requireNonNull(output);
        try {
            TiesDBChannelOutput bufferedOutput = new TiesDBChannelBufferedOutput(output, out -> {
                writePacketHeader(VERSION, out);
                LOG.debug("Header is written for {}", sessionConsumer);
            });
            Conversation session = openConversation(input, bufferedOutput);
            sessionConsumer.accept(session);
            bufferedOutput.flush();
        } catch (Exception e) {
            LOG.debug("Handle exception", e);
            writePacketHeader(VERSION, output);
            EBMLHelper.writeError(openConversation(input, output), e);
        }
    }

    protected Conversation openConversation(TiesDBChannelInput input, TiesDBChannelOutput output) {
        TiesEBMLReader reader = new TiesEBMLReader(input, TiesDBType.Context.ROOT);
        TiesEBMLWriter writer = new TiesEBMLWriter(output);
        return new Conversation() {

            public void accept(Event e) throws TiesDBProtocolException {
                writer.accept(new EBMLEvent(e.getType(), convertEventState(e.getState())));
            }

            public Event get() throws TiesDBProtocolException {
                EBMLEvent e;
                if (reader.hasNext() && (e = reader.next()) != null) {
                    TiesDBType eventType = convertEventType(e.get());
                    switch (eventType) {
                    case UNKNOWN_STRUCTURE:
                    case UNKNOWN_VALUE:
                        return new Event(eventType, convertEventState(e.getType())) {
                            @Override
                            public EBMLCode getEBMLCode() {
                                return e.get().getEBMLCode();
                            }
                        };
                    // $CASES-OMITTED$
                    default:
                        return new Event(eventType, convertEventState(e.getType()));
                    }
                }
                return null;
            }

            public <O> O read(EBMLReadFormat<O> format) throws TiesDBProtocolException {
                return format.read(reader);
            }

            public <O> void write(EBMLWriteFormat<O> format, O data) throws TiesDBProtocolException {
                format.write(writer, data);
            }

            public void skip() {
                TiesEBMLReader.SKIP.read(reader);
            }

            public void addReaderListener(Consumer<Byte> listener) {
                reader.addListener(listener);
            }

            public void addWriterListener(Consumer<Byte> listener) {
                writer.addListener(listener);
            }

            public void removeReaderListener(Object listener) {
                reader.removeListener(listener);
            }

            public void removeWriterListener(Object listener) {
                writer.removeListener(listener);
            }

            @Override
            public Version getVersion() {
                return TiesDBProtocolV0R0.this.getVersion();
            }

        };
    }

    private EventState convertEventState(EventType type) throws TiesDBProtocolException {
        requireNonNull(type);

        if (CommonEventType.BEGIN.equals(type)) {
            return EventState.BEGIN;
        } else if (CommonEventType.END.equals(type)) {
            return EventState.END;
        }

        throw new TiesDBProtocolException("Unknown Event type " + type);
    }

    private CommonEventType convertEventState(EventState state) throws TiesDBProtocolException {
        requireNonNull(state);

        switch (state) {
        case BEGIN:
            return CommonEventType.BEGIN;
        case END:
            return CommonEventType.END;
        default:
            break;
        }

        throw new TiesDBProtocolException("Unknown Event state " + state);
    }

    private TiesDBType convertEventType(EBMLType type) throws TiesDBProtocolException {
        requireNonNull(type);
        if (type instanceof TiesDBType) {
            return (TiesDBType) type;
        } else if (type instanceof UnknownTiesEBMLType) {
            UnknownTiesEBMLType unknownType = ((UnknownTiesEBMLType) type);
            return unknownType.isStructural() ? TiesDBType.UNKNOWN_STRUCTURE : TiesDBType.UNKNOWN_VALUE;
        }
        throw new TiesDBProtocolException("Unknown EBML type " + type);
    }

    public static interface Conversation {

        enum EventState {
            BEGIN, END
        }

        public static class Event {

            private final TiesDBType type;
            private final EventState state;

            public Event(TiesDBType type, EventState state) {
                this.type = requireNonNull(type);
                this.state = requireNonNull(state);
            }

            public TiesDBType getType() {
                return type;
            }

            public EBMLCode getEBMLCode() {
                return type.getEBMLCode();
            }

            public EventState getState() {
                return state;
            }

            @Override
            public String toString() {
                return "TiesDBProtocolV0R0$Conversation$Event [type=" + type + ", state=" + state + "]";
            }

        }

        <O> O read(EBMLReader.EBMLReadFormat<O> format) throws TiesDBProtocolException;

        <O> void write(EBMLWriter.EBMLWriteFormat<O> format, O data) throws TiesDBProtocolException;

        Event get() throws TiesDBProtocolException;

        void accept(Event e) throws TiesDBProtocolException;

        void skip();

        void addReaderListener(Consumer<Byte> listener);

        void addWriterListener(Consumer<Byte> listener);

        void removeReaderListener(Object listener);

        void removeWriterListener(Object listener);

        Version getVersion();

    }

    @Override
    public void createChannel(TiesDBChannelOutput output, TiesDBProtocolHandlerProvider handlerProvider) throws TiesDBException {
        requireNonNull(output);
        try {
            openChannel(output, session -> {
                handlerProvider.getHandler(VERSION, VERSION, session).handle(session);
            });
        } catch (IOException e) {
            LOG.debug("CreateChannelHandshake failed", e);
            throw new TiesDBProtocolException("Can't create channel", e);
        }
    }

    @Override
    public void acceptChannel(TiesDBChannelInput input, TiesDBChannelOutput output, TiesDBProtocolHandlerProvider handlerProvider)
            throws TiesDBException {
        requireNonNull(input);
        requireNonNull(output);
        try {
            Version remoteVersion = parsePacketHeader(input);
            if (REVISION.compare(VERSION, remoteVersion) >= 0) {
                processChannel(input, output, session -> {
                    handlerProvider.getHandler(VERSION, remoteVersion, session).handle(session);
                });
                return;
            } else {
                throw new TiesDBProtocolException("Channel protocol version missmatch on accept");
            }
        } catch (IOException e) {
            LOG.debug("AcceptChannelHandshake failed", e);
            throw new TiesDBProtocolException("Can't accept channel", e);
        }
    }

    @Override
    public String toString() {
        return "TiesDBProtocolV0R0 [" + VERSION.getMaintence() + "]";
    }

}
