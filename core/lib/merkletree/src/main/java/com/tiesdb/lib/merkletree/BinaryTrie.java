package com.tiesdb.lib.merkletree;

public class BinaryTrie<Payload> {
	private BinaryTrieNode root;

	public BinaryTrie() {
		root = new BinaryTrieNode(0, 0, (byte)0, (byte)-128);
	}

	public void insert(long id0, long id1, Payload p) {
		insertIntoHierarchy(id0, id1, p);
	}
	
	private void insertIntoHierarchy(long id0, long id1, Payload p){
		BinaryTrieNode parent = null;
		BinaryTrieNodeBase node = root;
		while(true) {
			BinaryTrieNode n = (BinaryTrieNode)node;
			
			if(n.compare(id0, id1)) {
				boolean next1 = n.isNext1(id0, id1);
				parent = n;
				node = next1 ? n.child1 : n.child0;
				if(node == null) {
					//We have found a node where new leaf should be placed
					BinaryTrieLeaf<Payload> leaf = new BinaryTrieLeaf<Payload>(id0, id1, p, (byte)(128 + n.offsetEnd), (byte)0);
					if(next1) {
						n.child1 = leaf;
					}else {
						n.child0 = leaf;
					}
					return;
				}else if(node instanceof BinaryTrieLeaf) {
					BinaryTrieLeaf<Payload> leaf = (BinaryTrieLeaf<Payload>)node;
					
					BinaryTrieNode newn = addLeafToNode(leaf, id0, id1, p);
					if(newn != null) {
						if(next1) {
							n.child1 = newn;
						} else {
							n.child0 = newn;
						}
					} else {
						//The node is the same. Just update the payload
						leaf.payload = p;
					}
					return;
				}
			} else {
				BinaryTrieNode newn = addLeafToNode(n, id0, id1, p);
				assert(newn != null); //Otherwise it would not be else clause
				
				if(parent == null) {
					root = newn;
				} else if(parent.isNext1(id0, id1)) {
					parent.child1 = newn;
				} else {
					parent.child0 = newn;
				}
				return;
			}
		}
		
		
	}
	
	private BinaryTrieNode addLeafToNode(BinaryTrieNodeBase node, long id0, long id1, Payload p) {
		byte i = node.findCommon(id0, id1);
		if(i < 0)
			return null; //Nodes are identical
		
		assert i > node.offsetStart;
		
		BinaryTrieNode newnode = new BinaryTrieNode(id0, id1, node.offsetStart, (byte)(128 - i));
		BinaryTrieLeaf<Payload> newleaf = new BinaryTrieLeaf<Payload>(id0, id1, p, (byte)i, (byte)0);
		
		if(newnode.isNext1(id0,  id1)) {
			newnode.child1 = newleaf;
			newnode.child0 = node;
		}else {
			newnode.child0 = newleaf;
			newnode.child1 = node;
		}
		
		return newnode;
	}

}
