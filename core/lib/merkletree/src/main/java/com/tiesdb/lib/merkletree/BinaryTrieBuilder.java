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
package com.tiesdb.lib.merkletree;

import java.util.Arrays;
import java.util.UUID;

import com.tiesdb.lib.crypto.digest.DigestManager;
import com.tiesdb.lib.crypto.digest.api.Digest;
import com.tiesdb.lib.merkletree.api.Trie;

public class BinaryTrieBuilder {
	private BinaryTrie trie;
	private byte[] rootHash;
	
	public BinaryTrieBuilder () {
		this(null);
	}
	
	public BinaryTrieBuilder (Digest hash) {
		if(hash == null)
			hash = DigestManager.getDigest(DigestManager.TIGER);
		trie = new BinaryTrie(hash);
		trie.properties.isBuildingMode = true;
	}
	
	public Trie build() {
		if(trie.child0 == null && trie.child1 == null)
			return trie; //Empty trie is always valid
		
		if(rootHash == null)
			return null;
		
		if(!trie.checkTrieIsOkAndClearHashes())
			return null;
		
		trie.recomputeHash();
		
		if(!Arrays.equals(rootHash, trie.hash))
			return null;
		
		trie.properties.isBuildingMode = false;
		
		return trie;
	}
	
	public void insert(long mostSignificantBits, long leastSignificantBits, byte mask, byte[] hash) {
		assert(trie.properties.isBuildingMode);
		
		if(mask == -128) {
			//Root node
			rootHash = hash;
		} else if(mask == 0) {
			BinaryTrieLeaf leaf = new BinaryTrieLeaf(trie.properties, mostSignificantBits, leastSignificantBits, null);
			if(hash != null) {
				leaf.hash = hash;
				leaf.setFlag(BinaryTrie.FLAG_HASH_VALID);
			}
			trie.insert(leaf);
		} else if(mask < 0) {
			BinaryTrieNode node = new BinaryTrieNode(trie.properties, mostSignificantBits, leastSignificantBits, (byte)0, mask);
			if(hash != null) {
				node.hash = hash;
				node.setFlag(BinaryTrie.FLAG_HASH_VALID);
			}
			trie.insert(node);
		}
	}

	public void insert(UUID uuid, byte offsetEnd, byte[] hash) {
		insert(uuid.getMostSignificantBits(), uuid.getLeastSignificantBits(), offsetEnd, hash);
	}
}
