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

import static com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.DEFAULT_DIGEST_ALG;
import static java.util.Objects.requireNonNull;

import java.security.SignatureException;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeSet;

import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tiesdb.lib.crypto.digest.DigestManager;
import com.tiesdb.lib.crypto.digest.api.Digest;
import com.tiesdb.lib.crypto.ecc.signature.ECKey;
import com.tiesdb.protocol.exception.TiesDBProtocolException;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.Conversation;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.Conversation.Event;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.Conversation.EventState;
import com.tiesdb.protocol.v0r0.reader.EntryReader.Entry;
import com.tiesdb.protocol.v0r0.reader.FieldReader.Field;
import com.tiesdb.protocol.v0r0.reader.SignatureReader.Signature;

final class ReaderUtil {

    private static final Logger LOG = LoggerFactory.getLogger(ReaderUtil.class);

    private ReaderUtil() {
    }

    static <T> void acceptEach(Conversation session, Event rootEvent, Reader<T> controller, T t) throws TiesDBProtocolException {
        requireNonNull(session);
        requireNonNull(rootEvent);
        if (!EventState.BEGIN.equals(rootEvent.getState())) {
            throw new TiesDBProtocolException("Illegal root event: " + rootEvent);
        }
        Event event;
        while (null != (event = session.get())) {
            LOG.trace("Event {}", event);
            if (EventState.BEGIN.equals(event.getState())) {
                if (null == controller || !controller.accept(session, event, t)) {
                    session.skip();
                    LOG.warn("{} event skipped", event.getType());
                    end(session, event);
                }
            } else if (EventState.END.equals(event.getState()) && event.getType().equals(rootEvent.getType())) {
                break;
            } else {
                throw new TiesDBProtocolException("Illegal event: " + event);
            }
        }
    }

    static void end(Conversation session, Event event) throws TiesDBProtocolException {
        requireNonNull(session);
        requireNonNull(event);
        if (!EventState.BEGIN.equals(event.getState())) {
            throw new TiesDBProtocolException("Illegal root event: " + event);
        }
        Event endEvent;
        if (null != (endEvent = session.get()) && EventState.END.equals(endEvent.getState())
                && endEvent.getType().equals(event.getType())) {
        } else {
            throw new TiesDBProtocolException("Illegal event: " + endEvent);
        }
    }

    static boolean checkSignature(byte[] messageHash, Signature signature) throws TiesDBProtocolException {
        try {
            byte[] signer = ECKey.signatureToAddressBytes(messageHash, signature.getSignature());
            return Arrays.equals(signer, signature.getSigner());
        } catch (SignatureException e) {
            throw new TiesDBProtocolException(e);
        }
    }

    static boolean checkEntryFieldsHash(Entry entry) {
        Map<String, Field> fields = entry.getFields();
        Digest fldDigest = DigestManager.getDigest(DEFAULT_DIGEST_ALG);
        TreeSet<String> fieldNames = new TreeSet<>(fields.keySet());
        for (String fieldName : fieldNames) {
            fldDigest.update(fields.get(fieldName).getHash());
        }
        byte[] fldHash = new byte[fldDigest.getDigestSize()];
        fldDigest.doFinal(fldHash, 0);
        LOG.debug("ENTRY_FLD_HASH_CALCULATED: {}", DatatypeConverter.printHexBinary(fldHash));
        return Arrays.equals(fldHash, entry.getHeader().getEntryFldHash());
    }
}