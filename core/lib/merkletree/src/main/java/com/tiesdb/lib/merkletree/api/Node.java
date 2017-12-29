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
 * with Ties.DB project.If not, see <https://www.gnu.org/licenses/lgpl-3.0>.
 */
package com.tiesdb.lib.merkletree.api;

import java.util.UUID;

public interface Node {
	/**
	 * 128 bit id of the node
	 * @return
	 */
	UUID getId();

	/**
	 * Negative offset from right to the end of significant bits of ID 
	 * Root returns -128
	 * Leaf returns 0
	 * Intermediate nodes return -1 - -127
	 * 
	 * @return 
	 */
	byte getMask();

	/**
	 * Gets left (0) child
	 * @return
	 */
	Node getLeft();

	/**
	 * Gets right (1) child
	 * @return
	 */
	Node getRight();

	/**
	 * Returns hash of the node or null if hash is not computed Client should not
	 * change the bytes!!!
	 * 
	 * @return hash bytes
	 */
	byte[] getHash();

}
