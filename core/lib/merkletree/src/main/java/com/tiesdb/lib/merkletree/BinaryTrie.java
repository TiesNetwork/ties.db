package com.tiesdb.lib.merkletree;

import java.util.Arrays;
import java.util.UUID;

import com.tiesdb.lib.crypto.digest.api.Digest;

public class BinaryTrie extends BinaryTrieNode {
	public BinaryTrie(Digest hash) {
		//BinaryTrie is itself a root node
		super(new TrieProperties(), 0, 0, (byte)0, (byte)-128);

		assert(hash != null);
		properties.hash = hash;
	}

	/**
	 * Inserts the data into the tree (or updates existing element)
	 * @param id0
	 * @param id1
	 * @param data
	 */
	public void insert(long id0, long id1, byte[] data) {
		BinaryTrieNode parent = null;
		BinaryTrieNodeBase node = this;
		while(true) {
			BinaryTrieNode n = (BinaryTrieNode)node;
			node.hashIsValid = false; //Since we go this path, this hash will be invalid after insertion/update
			
			if(n.compare(id0, id1)) {
				boolean next1 = n.isNext1(id0, id1);
				parent = n;
				node = next1 ? n.child1 : n.child0;
				if(node == null) {
					//We have found a node where new leaf should be placed
					BinaryTrieLeaf leaf = new BinaryTrieLeaf(properties, id0, id1, data, (byte)(128 + n.offsetEnd));
					if(next1) {
						n.child1 = leaf;
					}else {
						n.child0 = leaf;
					}
					return;
				}else if(node instanceof BinaryTrieLeaf) {
					BinaryTrieLeaf leaf = (BinaryTrieLeaf)node;
					
					BinaryTrieNode newn = addLeafToNode(leaf, id0, id1, data);
					if(newn != null) {
						if(next1) {
							n.child1 = newn;
						} else {
							n.child0 = newn;
						}
					} else {
						//The node is the same. Just update the payload
						leaf.hashData(data);
					}
					return;
				}
			} else {
				BinaryTrieNode newn = addLeafToNode(n, id0, id1, data);
				assert(newn != null); //Otherwise it would not be else clause
				assert(parent != null); //Root node is comparable to anything so parent should be initialized before this line
				
				if(parent.isNext1(id0, id1)) {
					parent.child1 = newn;
				} else {
					parent.child0 = newn;
				}
				return;
			}
		}
	}
	
	public void insert(UUID uuid, byte[] data) {
		insert(uuid.getMostSignificantBits(), uuid.getLeastSignificantBits(), data);
	}
	
	private BinaryTrieNode addLeafToNode(BinaryTrieNodeBase node, long id0, long id1, byte[] data) {
		byte i = node.findCommon(id0, id1);
		if(i < 0)
			return null; //Leaf nodes are identical
		
		assert i > node.offsetStart;
		
		BinaryTrieNode newnode = new BinaryTrieNode(properties, id0, id1, node.offsetStart, (byte)(i - 128));
		BinaryTrieLeaf newleaf = new BinaryTrieLeaf(properties, id0, id1, data, (byte)i);
		node.setOffsets(i, node.offsetEnd);
		
		if(newnode.isNext1(id0,  id1)) {
			newnode.child1 = newleaf;
			newnode.child0 = node;
		}else {
			newnode.child0 = newleaf;
			newnode.child1 = node;
		}
		
		return newnode;
	}
	
	/**
	 * Finds leaf by the id
	 * @param id0
	 * @param id1
	 * @return found leaf or null if not found
	 */
	public BinaryTrieLeaf find(long id0, long id1) {
		BinaryTrieNodeBase node = this;
		while(true) {
			if(node == null)
				return null;
			
			if(node.compare(id0, id1)) {
				if(node instanceof BinaryTrieLeaf) {
					return (BinaryTrieLeaf)node;
				} else {
					BinaryTrieNode n = (BinaryTrieNode)node;
					boolean next1 = n.isNext1(id0, id1);
					node = next1 ? n.child1 : n.child0;
				}
			} else {
				return null;
			}
		}
	}
	
	public BinaryTrieLeaf find(UUID uuid) {
		return find(uuid.getMostSignificantBits(), uuid.getLeastSignificantBits());
	}
	
	
	/**
	 * Checks that tree contains an element with the specified id and data
	 * @param id0 - id part 1
	 * @param id1 - id part 2
	 * @param data - data byte array
	 * @return whether element with right hash exists
	 */
	public boolean check(long id0, long id1, byte[] data) {
		BinaryTrieLeaf leaf = find(id0, id1);
		if(leaf == null)
			return false;
		assert(leaf.isHashValid());
		
		byte[] hash = leaf.hashDataReturn(data);
		return Arrays.equals(leaf.hash, hash);
	}
	
	public boolean check(UUID uuid, byte[] data) {
		return check(uuid.getMostSignificantBits(), uuid.getLeastSignificantBits(), data);
	}
}
