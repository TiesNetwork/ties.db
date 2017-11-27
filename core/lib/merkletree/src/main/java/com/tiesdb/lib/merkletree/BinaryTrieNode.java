package com.tiesdb.lib.merkletree;

public class BinaryTrieNode extends BinaryTrieNodeBase {
	BinaryTrieNodeBase child0;
	BinaryTrieNodeBase child1;
	
	public BinaryTrieNode(long prefix0, long prefix1, byte offsetStart, byte offsetEnd) {
		super(prefix0, prefix1, offsetStart, offsetEnd);
	}
	
	BinaryTrieNodeBase nextNode(long id0, long id1) {
		if(isNext1(id0, id1)) {
			return child1;
		} else {
			return child0;
		}
	}
}
