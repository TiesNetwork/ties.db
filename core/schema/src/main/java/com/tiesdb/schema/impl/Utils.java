package com.tiesdb.schema.impl;

import org.web3j.protocol.core.RemoteCall;

public class Utils {
	public static <T> T send(RemoteCall<T> rc) {
		try {
			return rc.send();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
