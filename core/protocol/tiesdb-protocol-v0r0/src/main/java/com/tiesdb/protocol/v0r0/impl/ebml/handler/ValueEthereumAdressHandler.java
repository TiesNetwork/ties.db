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
package com.tiesdb.protocol.v0r0.impl.ebml.handler;

import java.io.IOException;

import com.tiesdb.protocol.v0r0.api.message.EthereumAddress;
import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLFormatter;
import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLHandler;
import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLParser;

public class ValueEthereumAdressHandler implements TiesDBEBMLHandler<EthereumAddress> {

	static final ValueBinaryHandler HANDLER = ValueBinaryHandler.INSTANCE;

	public static final ValueEthereumAdressHandler INSTANCE = new ValueEthereumAdressHandler();

	private ValueEthereumAdressHandler() {
	}

	@Override
	public EthereumAddress read(TiesDBEBMLParser parser) throws IOException {
		return new EthereumAddress(HANDLER.read(parser));
	}

	@Override
	public Part<TiesDBEBMLFormatter> prepare(EthereumAddress o) throws IOException {
		return HANDLER.prepare(null == o ? null : o.getBytes());
	}

}
