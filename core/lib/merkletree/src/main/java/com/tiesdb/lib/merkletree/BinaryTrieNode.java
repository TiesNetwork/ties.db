package com.tiesdb.lib.merkletree;

import com.tiesdb.lib.crypto.digest.api.Digest;

public class BinaryTrieNode extends BinaryTrieNodeBase {
	BinaryTrieNodeBase child0;
	BinaryTrieNodeBase child1;
	
	
	public BinaryTrieNode(TrieProperties properties, long prefix0, long prefix1, byte offsetStart, byte offsetEnd) {
		super(properties, prefix0, prefix1, offsetStart, offsetEnd);
		assert(offsetEnd < 0); //Intermediate nodes always have offsetEnd < 0
	}
	
	void setOffsets(byte offsetStart, byte offsetEnd) {
		super.setOffsets(offsetStart, offsetEnd);

		//Intermediate node offsets should clear insignificant bits of prefix
		long mask0 = makeMask(offsetStart, -64 < offsetEnd ? 0 : offsetEnd + 64);
		long mask1 = makeMask(offsetStart < 64 ? 0 : offsetStart-64, offsetEnd);
		this.prefix0 = this.prefix0 & mask0;
		this.prefix1 = this.prefix1 & mask1;
	}
	
	void updateHash() {
		Digest digest = properties.hash;
		ensureHash();

		if(child0 != null && child1 != null) {
			assert(child0.hash != null);
			assert(child1.hash != null);
			
			digest.reset();
			digest.update((byte)0x1);
			digest.update(child0.hash, 0, child0.hash.length);
			digest.update(child1.hash, 0, child1.hash.length);
			digest.doFinal(hash, 0);
		}else if(child0 != null) {
			assert(child0.isHashValid());
			System.arraycopy(child0.hash, 0, hash, 0, hash.length);
		}else if(child1 != null) {
			assert(child1.isHashValid());
			System.arraycopy(child1.hash, 0, hash, 0, hash.length);
		}
		
		hashIsValid = true;
	}

	@Override
	public void recomputeHash() {
		if(!isHashValid()) {
			if(child0 != null && !child0.isHashValid())
				child0.recomputeHash();
			if(child1 != null && !child1.isHashValid())
				child1.recomputeHash();
			updateHash();
		}
	}
	
}
