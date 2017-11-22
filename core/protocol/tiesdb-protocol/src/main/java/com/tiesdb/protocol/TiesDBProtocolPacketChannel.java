package com.tiesdb.protocol;

public interface TiesDBProtocolPacketChannel {

	enum State {
		OPENING, OPENED, CLOSING, CLOSED
	}

	interface Input {

		State state();

		boolean isOpened();

		boolean isClosed();

		int available();

		int more();

		byte get();
		
		int seek(int len);

		void peekStart();

		void peekRewind();

		void peekSkip();

		boolean isPeeking();
	}

	interface Output {

		State state();

		boolean isOpened();

		boolean isClosed();

		void put(byte b);

		void cacheStart();

		void cacheClear();

		void cacheFlush();

		boolean isCaching();
	}

	Input getInput();

	Output getOutput();
}
