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
package com.tiesdb.protocol.v0r0.impl.ebml;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import com.tiesdb.lib.crypto.ecc.signature.ECKey;

import one.utopic.abio.api.output.Output;
import one.utopic.sparse.ebml.EBMLFormatter;

public class TiesDBEBMLFormatter extends EBMLFormatter {

	private final TiesDBEBMLFormatterSettings formatterSettings;
	private final ECKey signatureKey;
	private final Set<Consumer<Byte>> consumers;

	private TiesDBEBMLFormatter(Set<Consumer<Byte>> consumers, TiesDBEBMLFormatterSettings formatterSettings, ECKey signatureKey,
			Output output) {
		super(new WrappedOutput(output, consumers));
		this.consumers = consumers;
		this.formatterSettings = null != formatterSettings ? formatterSettings : new TiesDBEBMLFormatterSettings();
		this.signatureKey = signatureKey;
	}

	public TiesDBEBMLFormatter(TiesDBEBMLFormatterSettings formatterSettings, ECKey signatureKey, Output output) {
		this(ConcurrentHashMap.newKeySet(), formatterSettings, signatureKey, output);
	}

	public TiesDBEBMLFormatter(TiesDBEBMLFormatterSettings formatterSettings, Output output) {
		this(formatterSettings, null, output);
	}

	public TiesDBEBMLFormatter(ECKey key, Output output) {
		this(null, key, output);
	}

	public TiesDBEBMLFormatter(Output output) {
		this(null, null, output);
	}

	public TiesDBEBMLFormatterSettings getFormatterSettings() {
		return formatterSettings;
	}

	public ECKey getSignatureKey() {
		return signatureKey;
	}

	public static class WrappedOutput implements Output {

		private final Output output;
		private final Set<Consumer<Byte>> consumers;

		public WrappedOutput(Output output, Set<Consumer<Byte>> consumers) {
			this.output = output;
			this.consumers = consumers;
		}

		@Override
		public boolean isFinished() {
			return output.isFinished();
		}

		@Override
		public void writeByte(byte b) throws IOException {
			output.writeByte(b);
			for (Consumer<Byte> consumer : consumers) {
				consumer.accept(b);
			}
		}

	}

	public Consumer<Byte> subscribeConsumer(Consumer<Byte> consumer) {
		return consumers.add(consumer) ? consumer : null;
	}

	public boolean unsubscribeConsumer(Consumer<Byte> consumer) {
		return consumers.remove(consumer);
	}
}
