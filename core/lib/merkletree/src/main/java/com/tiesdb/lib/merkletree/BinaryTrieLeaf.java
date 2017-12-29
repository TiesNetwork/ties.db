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
 * with Ties.DB project.If not, see <https://www.gnu.org/licenses/lgpl-3.0>.
 */
package com.tiesdb.lib.merkletree;

import com.tiesdb.lib.crypto.digest.api.Digest;
import com.tiesdb.lib.merkletree.api.Node;

class BinaryTrieLeaf extends BinaryTrieNodeBase{
	
	BinaryTrieLeaf(TrieProperties properties, long id0, long id1, byte[] data) {
		super(properties, id0, id1, (byte)0, (byte)0);
		if(data != null)
			hashData(data);
	}
	
	BinaryTrieLeaf(TrieProperties properties, long id0, long id1, byte[] data, byte start) {
		super(properties, id0, id1, start, (byte)0); //Leafs always have offsetEnd == 0
		if(data != null)
			hashData(data);
	}
	
	void hashData(byte[] data, byte[] out) {
		Digest digest = properties.hash;
		digest.reset();
		
		digest.update((byte)0x0);
		digest.update(data, 0, data.length);
		hashPrefix(digest);
		
		digest.doFinal(out, 0);
	}

	void hashData(byte[] data) {
		hashData(data, ensureHash());
		setFlag(FLAG_HASH_VALID);
	}
	
	byte[] hashDataReturn(byte[] data) {
		byte[] out = new byte[properties.hash.getDigestSize()];
		hashData(data, out);
		return out;
	}

	@Override
	public void recomputeHash() {
		assert(isHashValid()); //The hash of leaf node can not be invalidated
	}

	@Override
	public Node getLeft() {
		return null;
	}

	@Override
	public Node getRight() {
		return null;
	}
}
