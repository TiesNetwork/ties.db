package com.tiesdb.protocol.v0.element;

import java.util.LinkedList;

import com.tiesdb.protocol.api.data.Element;
import com.tiesdb.protocol.api.data.ElementContainer;
import com.tiesdb.protocol.v0.impl.TiesEBMLType;

public class TiesDBRequestHeader extends LinkedList<TiesDBRequestHeader.Part>
		implements TiesElement, TiesDBRequest.Part, ElementContainer<TiesDBRequestHeader.Part> {

	private static final long serialVersionUID = 5618075824368173268L;

	@Override
	public TiesEBMLType getType() {
		return TiesEBMLType.RequestHeader;
	}

	public static interface Part extends Element {
	}
}