package com.tiesdb.lib.merkletree;

import java.util.UUID;

import com.tiesdb.lib.crypto.digest.api.Digest;
import com.tiesdb.lib.merkletree.api.Node;

abstract class BinaryTrieNodeBase implements Node {
	protected static final int FLAG_HASH_VALID = 0x1;
	protected static final int FLAG_SUBTRIE = 0x2;
	
	long prefix0;
	long prefix1;
	byte offsetStart; //Offset to start from the start (0 - 127)
	byte offsetEnd; //Offset to end from the end (negative 0 - -128)
	
	byte flags;
	
	TrieProperties properties;
	byte[] hash;
	
	BinaryTrieNodeBase(TrieProperties properties, long prefix0, long prefix1, byte offsetStart, byte offsetEnd) {
		this.properties = properties;
		this.prefix0 = prefix0;
		this.prefix1 = prefix1;
		setOffsets(offsetStart, offsetEnd);
	}
	
	boolean compare(BinaryTrieNodeBase node) {
		return compare(node.prefix0, node.prefix1) && node.offsetEnd >= offsetEnd;
	}
	
	boolean compare(long id0, long id1) {
		long mask0 = makeMask(0, -64 < offsetEnd ? 0 : offsetEnd + 64);
		long mask1 = makeMask(0, offsetEnd);
		return ((prefix0 & mask0) == (id0 & mask0) && (prefix1 & mask1) == (id1 & mask1));
	}
	
	public boolean isEnd() {
		return offsetEnd == 0;
	}
	
	/**
	 * Checks if the next bit after offsetEnd position is 1
	 *  
	 * @param id0
	 * @param id1
	 * @return 
	 */
	boolean isNext1(long id0, long id1) {
		long mask;
		if(-64 < offsetEnd) {
			mask = 1L << (64 + offsetEnd);
			return (mask & id1) != 0;
		} else {
			mask = 1L << (128 + offsetEnd);
			return (mask & id0) != 0;
		}
	}
	
	protected static long makeMask(int start, int end) {
		long mask = -1;
		if(start >= 64 || -end >= 64)
			return 0;
		mask = -1L << start;
		mask &= (-1L >>> -end);
		return mask;
	}
	
	byte findCommon(BinaryTrieNodeBase node) {
		int i, imax = 128+this.offsetEnd;
		for(i=this.offsetStart; i<imax; ++i) {
			if(i >= 128 + node.offsetEnd)
				return -2; //The node should be the parent of this node
			if(i < node.offsetStart)
				return -3; //The node should be child of this node
			
			long mask = 1L << i;
			long px = i < 64 ? this.prefix0 : this.prefix1;
			long id = i < 64 ? node.prefix0 : node.prefix1;
			if((mask & px) != (mask & id)) {
				break;
			}
		}
		
		if(i >= imax) {
			//The nodes are equivalent
			return -1;
		}
		
		return (byte)i;
	}
	
	void setOffsets(byte offsetStart, byte offsetEnd) {
		assert(offsetStart >= this.offsetStart); //Offsets can only shrink
		assert(offsetEnd <= this.offsetEnd);
		
		this.offsetStart = offsetStart;
		this.offsetEnd = offsetEnd;
	}
	
	boolean isHashValid() {
		return hash != null && testFlags(FLAG_HASH_VALID);
	}
	
	byte[] ensureHash() {
		if(hash == null)
			hash = new byte[properties.hash.getDigestSize()];
		return hash;	
	}
	
	abstract public void recomputeHash();

	/**
	 * Adds prefix info to digest
	 * @param digest
	 */
	protected void hashPrefix(Digest digest) {
		digest.update(offsetEnd);
		
		byte[] buf;
		
		buf = ByteUtils.longToBytes(prefix0);
		digest.update(buf, 0, buf.length);

		buf = ByteUtils.longToBytes(prefix1);
		digest.update(buf, 0, buf.length);
	}
	
	boolean isRoot() {
		return offsetEnd == -128;
	}
	
	boolean testFlags(int flag) {
		return (flags & (byte)flag) != 0;
	}
	
	void setFlags(int flag, int mask) {
		flags &= ~mask;
		flags |= flag & mask;
	}
	
	void setFlag(int flag) {
		flags |= flag;
	}
	
	
	@Override
	public UUID getId() {
		return new UUID(prefix0, prefix1);
	}

	@Override
	public byte getMask() {
		return offsetEnd;
	}

	@Override
	public byte[] getHash() {
		return isHashValid() ? hash : null;
	}

	void clearSubtrie() {
		setFlags(0, FLAG_SUBTRIE);
	}
	
	boolean checkTrieIsOkAndClearHashes() {
		return isHashValid();
	}
}
