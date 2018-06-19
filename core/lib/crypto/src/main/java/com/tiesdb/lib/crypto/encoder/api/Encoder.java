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
package com.tiesdb.lib.crypto.encoder.api;

import java.io.IOException;

public interface Encoder {

    int encode(byte[] data, int off, int length, CodeWriter out) throws ConversionException;

    int encode(byte[] data, int off, CodeWriter out) throws ConversionException;

    int encode(byte[] data, CodeWriter out) throws ConversionException;

    int decode(byte[] data, int off, int length, CodeWriter out) throws ConversionException;

    int decode(byte[] data, int off, CodeWriter out) throws ConversionException;

    int decode(byte[] data, CodeWriter out) throws ConversionException;

    class ConversionException extends IOException {

        private static final long serialVersionUID = -6292505116970170864L;

        public ConversionException(String message, Throwable cause) {
            super(message, cause);
        }

        public ConversionException(String message) {
            super(message);
        }

        public ConversionException(Throwable cause) {
            super(cause);
        }

    }

}
