package com.tiesdb.lib.merkletree;

public class BinaryTrieLeaf<Payload> extends BinaryTrieNodeBase{
	Payload payload;
	
	BinaryTrieLeaf(long id0, long id1, Payload p) {
		super(id0, id1, (byte)0, (byte)0);
		payload = p;
	}
	
	BinaryTrieLeaf(long id0, long id1, Payload p, byte start, byte end) {
		super(id0, id1, start, end);
		payload = p;
	}
	
}
