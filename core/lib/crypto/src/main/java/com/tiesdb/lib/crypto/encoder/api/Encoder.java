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
