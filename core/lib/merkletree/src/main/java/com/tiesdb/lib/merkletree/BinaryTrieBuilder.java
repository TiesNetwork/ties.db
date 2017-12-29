package com.tiesdb.lib.merkletree;

import java.util.UUID;

import org.bouncycastle.util.Arrays;

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
		
		if(!Arrays.areEqual(rootHash, trie.hash))
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
