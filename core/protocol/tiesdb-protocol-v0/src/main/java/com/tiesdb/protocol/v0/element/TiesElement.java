package com.tiesdb.protocol.v0.element;

import com.tiesdb.protocol.api.data.Element;
import com.tiesdb.protocol.v0.impl.TiesEBMLType;

public interface TiesElement extends Element {

	TiesEBMLType getType();

}
