package com.tiesdb.protocol.api.data;

public interface ElementContainer<E extends Element> extends Element, Iterable<E> {

	void accept(E element);

}
