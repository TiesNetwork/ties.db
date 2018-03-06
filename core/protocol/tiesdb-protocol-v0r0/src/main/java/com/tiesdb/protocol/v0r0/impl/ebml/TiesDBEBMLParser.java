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

import one.utopic.abio.api.input.Input;
import one.utopic.sparse.ebml.EBMLFilter;
import one.utopic.sparse.ebml.EBMLParser;
import one.utopic.sparse.ebml.EBMLType;

public class TiesDBEBMLParser extends EBMLParser {

	private final TiesDBEBMLParserSettings settings;
	private final Set<Consumer<Byte>> consumers;

	private TiesDBEBMLParser(Set<Consumer<Byte>> consumers, TiesDBEBMLParserSettings settings, Input input, EBMLType.Context typeContext,
			EBMLFilter filter) throws IOException {
		super(new WrappedInput(input, consumers), typeContext, filter);
		this.consumers = consumers;
		this.settings = null != settings? settings: new TiesDBEBMLParserSettings();
	}

	public TiesDBEBMLParser(TiesDBEBMLParserSettings settings, Input input, EBMLType.Context typeContext, EBMLFilter filter)
			throws IOException {
		this(ConcurrentHashMap.newKeySet(), settings, input, typeContext, filter);
	}

	public TiesDBEBMLParser(TiesDBEBMLParserSettings settings, Input input, EBMLType.Context typeContext) throws IOException {
		this(settings, input, typeContext, null);
	}

	public TiesDBEBMLParser(Input input, EBMLType.Context typeContext, EBMLFilter filter) throws IOException {
		this(null, input, typeContext, filter);
	}

	public TiesDBEBMLParser(Input input, EBMLType.Context typeContext) throws IOException {
		this(null, input, typeContext, null);
	}

	public TiesDBEBMLParserSettings getSettings() {
		return settings;
	}

	private static class WrappedInput implements Input {

		private final Input input;
		private final Set<Consumer<Byte>> consumers;

		public WrappedInput(Input input, Set<Consumer<Byte>> consumers) {
			this.input = input;
			this.consumers = consumers;
		}

		@Override
		public boolean isFinished() {
			return input.isFinished();
		}

		@Override
		public byte readByte() throws IOException {
			byte b = input.readByte();
			for (Consumer<Byte> consumer : consumers) {
				consumer.accept(b);
			}
			return b;
		}

	}

	public Consumer<Byte> subscribeConsumer(Consumer<Byte> consumer) {
		return consumers.add(consumer) ? consumer : null;
	}

	public boolean unsubscribeConsumer(Consumer<Byte> consumer) {
		return consumers.remove(consumer);
	}

}
