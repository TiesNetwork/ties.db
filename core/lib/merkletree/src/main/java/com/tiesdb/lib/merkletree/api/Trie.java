package com.tiesdb.lib.merkletree.api;

import java.util.UUID;

public interface Trie extends Node {
	/**
	 * Sets mode to subtrie mode
	 * In this mode trie consists only of nodes that are necessary to compute hash for
	 * the nodes added by addToSubtrie function
	 * @param subtrieMode
	 */
	void setSubtrieMode(boolean subtrieMode);
	
	/**
	 * Adds and id to subtrie. The id is not neccessary to exist in a trie. 
	 * Adding an unexistent id to the subtrie guarantees that it can be inserted to subtrie later 
	 * @param id
	 */
	void addToSubtrie(UUID id);
	
	/**
	 * Clears subtrie flag. You should turn off subtrie mode manually 
	 */
	void clearSubtrie();
	
	/**
	 * Inserts new leaf into the trie
	 * @param id
	 * @param data
	 */
	void insert(UUID id, byte[] data);
	
	/**
	 * recomputes hash if necessary and returns it
	 * Client should never change the bytes!
	 * @return root hash
	 */
	byte[] hash();
	
	/**
	 * Finds a node in a trie (subtrie) by id
	 * @param id
	 * @return
	 */
	Node find(UUID id);
	
	/**
	 * Checks if a node with specific data exists in a trie (subtrie)
	 * @param id
	 * @param data
	 * @return
	 */
	boolean check(UUID id, byte[] data);

}
