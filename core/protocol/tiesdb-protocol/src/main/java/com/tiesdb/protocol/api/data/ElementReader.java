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
package com.tiesdb.protocol.api.data;

public interface ElementReader<E extends Element> {

	/**
	 * Check if there is next element in <b>reading stream</b>
	 * 
	 * @return <tt>true</tt> if there is element to read or <tt>false</tt> otherwise
	 */
	boolean hasNext();

	/**
	 * Returns next element type in <b>reading stream</b> or <tt>null</tt> if there
	 * is no element to read
	 * 
	 * @return {@link ElementType} or <tt>null</tt>
	 */
	ElementType nextType();

	/**
	 * Returns next element size in <b>reading stream</b> or <tt>-1</tt> if there is
	 * no element to read
	 * 
	 * @return size in bytes or <tt>-1</tt>
	 */
	long nextSize();

	/**
	 * Skips next element in <b>reading stream</b>
	 */
	void skipNext();

	/**
	 * Returns next element in <b>reading stream</b> . If the element is a container
	 * it does not retrieves all elements, but puts it in <b>stack</b> and all
	 * children are automatically appended to the top element of the <b>stack</b> on
	 * their read.
	 * 
	 * @return {@link Element}
	 */
	E readNext();

	/**
	 * Returns current size of pending objects <b>stack</b>
	 * 
	 * @return number of objects in reading state
	 */
	int stackSize();

	/**
	 * Searches given object in the <b>stack</b> and returns its position from top
	 * or <tt>-1</tt> if object was not found
	 * 
	 * @param ec
	 *            object to search
	 * @return position in reading <b>stack</b> from top or <tt>-1</tt>
	 */
	int stackSearch(ElementContainer<E> ec);

	/**
	 * Returns object from the <b>stack</b> at given position from top
	 * 
	 * @param fromTop
	 *            object position
	 * @return {@link ElementContainer}
	 */
	ElementContainer<E> stackGet(int fromTop) throws ArrayIndexOutOfBoundsException;

	/**
	 * Skips reading of given number of objects in the <b>stack</b> from top
	 * 
	 * @param fromTop
	 *            number of objects to skip
	 * @return {@link ElementContainer} new <b>stack</b> top or <tt>null</tt>
	 */
	ElementContainer<E> stackSkip(int fromTop);

	/**
	 * Returns the <b>stack</b> top object not removing it from stack
	 * 
	 * @return {@link ElementContainer} <b>stack</b> top or <tt>null</tt>
	 */
	ElementContainer<E> stackPeek();

}
