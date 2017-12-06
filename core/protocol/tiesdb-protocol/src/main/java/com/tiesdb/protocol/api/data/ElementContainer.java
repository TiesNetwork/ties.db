package com.tiesdb.protocol.api.data;

import java.util.List;

public interface ElementContainer<E extends Element> extends Element, List<E> {
}
