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
 * with Ties.DB project.If not, see <https://www.gnu.org/licenses/lgpl-3.0>.
 */
package com.tiesdb.protocol.v0.element;

import com.tiesdb.protocol.v0.element.common.TiesElementValueBinary;
import com.tiesdb.protocol.v0.impl.TiesEBMLType;

public class TiesDBRequestSignature extends TiesElementValueBinary implements TiesDBBaseRequest.Part {

	public TiesDBRequestSignature() {
		super(TiesEBMLType.RequestSignature);
	}

	@Override
	public void accept(TiesDBBaseRequest.PartVisitor v) {
		v.visit(this);
	}

}