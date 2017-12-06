package com.tiesdb.protocol.api.data;

public interface ElementWriter<E extends Element> {

	void write(E e);

}
