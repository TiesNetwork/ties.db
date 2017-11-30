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
