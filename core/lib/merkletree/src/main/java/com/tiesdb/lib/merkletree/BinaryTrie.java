package com.tiesdb.lib.merkletree;

import java.util.Arrays;
import java.util.UUID;

import com.tiesdb.lib.crypto.digest.api.Digest;
import com.tiesdb.lib.merkletree.api.Trie;

class BinaryTrie extends BinaryTrieNode implements Trie{
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
		BinaryTrieLeaf newleaf = new BinaryTrieLeaf(properties, id0, id1, data);
		insert(newleaf);
	}
	
	/**
	 * Inserts the data into the tree (or updates existing element)
	 * @param id0
	 * @param id1
	 * @param data
	 */
	void insert(BinaryTrieNodeBase leaf) {
		BinaryTrieNode parent = null;
		BinaryTrieNodeBase node = this;
		
		long id0 = leaf.prefix0;
		long id1 = leaf.prefix1;
		
		while(true) {
			if(node == null) {
				assert(parent != null); //Parent should have already been set
				assert(parent == this || properties.isBuildingMode); //This is possible only for root node or for building mode!
				//if this asserts on subtrie you probably have forgotten include this id in subtrie
				
				//We have found a node where new leaf should be placed
				leaf.setOffsets((byte)(128 + parent.offsetEnd), leaf.offsetEnd);
				parent.addChild(leaf);
				return;
			}
			
			if(node.compare(leaf)) {
				//If the leaf is should be added under (or should replace) this node
				if(leaf.offsetEnd > node.offsetEnd) {
					//the leaf should be added under this node
					node.setFlags(0, FLAG_HASH_VALID); //Since we add below this node, this hash will be invalid after insertion/update
					
					boolean next1 = node.isNext1(id0, id1);
					parent = (BinaryTrieNode)node;
					node = next1 ? parent.child1 : parent.child0;
					continue;
				} else {
					//We have a node here that equals the one we already have
					//Just updating it
					assert(parent != null); //Root node is comparable to anything so parent should be initialized before this line
					
					parent.addChild(leaf);
					if(leaf instanceof BinaryTrieNode) {
						assert(properties.isBuildingMode); //Adding intermediate nodes only in building mode!
						BinaryTrieNode newn = (BinaryTrieNode)leaf;
						BinaryTrieNode oldn = (BinaryTrieNode)node;
						newn.child0 = oldn.child0;
						newn.child1 = oldn.child1;
					}
					return;
				}
			} else if(leaf.compare(node)) {
				//Intermediate node with shorter id
				assert(properties.isBuildingMode); //Only in building mode!
				assert(parent != null); //Root node is comparable to anything so parent should be initialized before this line
				
				BinaryTrieNode newn = (BinaryTrieNode)leaf;
				
				leaf.offsetStart = newn.offsetStart;
				parent.addChild(newn);
				newn.addChild(node);
				return;
			} else {
				BinaryTrieNode newn = addNodeToNode(node, leaf);
				assert(newn != null); //Otherwise there would not be else clause
				assert(parent != null); //Root node is comparable to anything so parent should be initialized before this line
				
				parent.addChild(newn);
				return;
			}
		}
	}
	
	public void insert(UUID uuid, byte[] data) {
		insert(uuid.getMostSignificantBits(), uuid.getLeastSignificantBits(), data);
	}
	
	private BinaryTrieNode addNodeToNode(BinaryTrieNodeBase node, BinaryTrieNodeBase newleaf) {
		long id0 = newleaf.prefix0;
		long id1 = newleaf.prefix1;
		
		byte i = node.findCommon(newleaf);
		assert(i >= -1); //The nodes should be siblings or the newleaf should be child of the node
		//if i == -2 newleaf should be parent and this function does not do this
		//if i == -3 the start offsets are messed up
		
		if(i < 0)
			return null; //Leaf nodes are identical or intermediate nodes are related
		
		assert i > node.offsetStart;
		
		BinaryTrieNode newnode = new BinaryTrieNode(properties, id0, id1, node.offsetStart, (byte)(i - 128));
		newnode.addChild(newleaf);
		newnode.addChild(node);
		
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
					if(properties.isSubtrieMode && !node.testFlags(FLAG_SUBTRIE))
						return null; //We are out of the subtrie. So no children traversing
					
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
	
	public void addToSubtrie(long id0, long id1) {
		BinaryTrieNodeBase node = this;
		
		while(true) {
			if(node == null)
				return;
			
			node.setFlag(FLAG_SUBTRIE);
			
			if(node.compare(id0, id1)) {
				if(node instanceof BinaryTrieLeaf) {
					return;
				} else {
					BinaryTrieNode n = (BinaryTrieNode)node;
					boolean next1 = n.isNext1(id0, id1);
					node = next1 ? n.child1 : n.child0;
				}
			} else {
				return;
			}
		}
	}

	@Override
	public void setSubtrieMode(boolean subtrieMode) {
		properties.isSubtrieMode = subtrieMode;
	}

	@Override
	public void addToSubtrie(UUID id) {
		addToSubtrie(id.getMostSignificantBits(), id.getLeastSignificantBits());
	}

	@Override
	public byte[] hash() {
		recomputeHash();
		return this.hash;
	}
	
	@Override
	public void clearSubtrie() {
		super.clearSubtrie();
	}
	
	
}
