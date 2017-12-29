package com.tiesdb.protocol.api;

public interface TiesDBProtocolPacketChannel {

	enum State {
		OPENING, OPENED, FINISHED, CLOSING, CLOSED
	}

	interface Input {

		State state();

		boolean isOpened();

		boolean isClosed();

		boolean isFinished();

		void close();

		int available();

		int more();

		byte get();

		int skip(int length);

		void peekStart();

		void peekRewind();

		void peekSkip();

		boolean isPeeking();

	}

	interface Output {

		State state();

		boolean isOpened();

		boolean isClosed();

		boolean isFinished();

		void close();

		int available();

		int more();

		void put(byte b);

		int skip(int length);

		void cacheStart();

		void cacheClear();

		void cacheFlush();

		boolean isCaching();
	}

	Input getInput();

	Output getOutput();
}
