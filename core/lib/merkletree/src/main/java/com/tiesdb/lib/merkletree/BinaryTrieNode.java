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

class BinaryTrieNode extends BinaryTrieNodeBase {
	BinaryTrieNodeBase child0;
	BinaryTrieNodeBase child1;
	
	
	public BinaryTrieNode(TrieProperties properties, long prefix0, long prefix1, byte offsetStart, byte offsetEnd) {
		super(properties, prefix0, prefix1, offsetStart, offsetEnd);
		assert(offsetEnd < 0); //Intermediate nodes always have offsetEnd < 0
	}
	
	void setOffsets(byte offsetStart, byte offsetEnd) {
		super.setOffsets(offsetStart, offsetEnd);

		//Intermediate node offsets should clear right insignificant bits of prefix
		long mask0 = makeMask(0, -64 < offsetEnd ? 0 : offsetEnd + 64);
		long mask1 = makeMask(0, offsetEnd);
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
			hashPrefix(digest);
			digest.doFinal(hash, 0);
		}else if(child0 != null) {
			assert(child0.isHashValid());
			System.arraycopy(child0.hash, 0, hash, 0, hash.length);
		}else if(child1 != null) {
			assert(child1.isHashValid());
			System.arraycopy(child1.hash, 0, hash, 0, hash.length);
		}
		
		setFlag(FLAG_HASH_VALID);
	}

	@Override
	public void recomputeHash() {
		if(!isHashValid()) {
			assert(isRoot() || (child0 != null && child1 != null)); //Only root can have less than two children
			
			if(child0 != null && !child0.isHashValid())
				child0.recomputeHash();
			if(child1 != null && !child1.isHashValid())
				child1.recomputeHash();
			
			updateHash();
		}
	}

	@Override
	public Node getLeft() {
		if(properties.isSubtrieMode && !testFlags(FLAG_SUBTRIE))
			return null;
		return this.child0;
	}

	@Override
	public Node getRight() {
		if(properties.isSubtrieMode && !testFlags(FLAG_SUBTRIE))
			return null;
		return this.child1;
	}

	@Override
	void clearSubtrie() {
		if(testFlags(FLAG_SUBTRIE)) {
			super.clearSubtrie();
			
			if(this.child0 != null)
				this.child0.clearSubtrie();
			if(this.child1 != null)
				this.child1.clearSubtrie();
		}
	}
	
	void addChild(BinaryTrieNodeBase node) {
		assert(this.compare(node));
		assert(this.offsetEnd < 0);
		
		node.setOffsets((byte)(128 + this.offsetEnd), node.offsetEnd);
		
		if(isNext1(node.prefix0, node.prefix1)) {
			this.child1 = node;
		} else {
			this.child0 = node;
		}
	}

	@Override
	boolean checkTrieIsOkAndClearHashes() {
		Node left = getLeft();
		Node right = getRight();

		if(left == null && right == null) {
			return isHashValid();
		}
		
		if(left != null && right != null) {
			setFlags(0, FLAG_HASH_VALID);
			
			return this.child0.checkTrieIsOkAndClearHashes()
					&& this.child1.checkTrieIsOkAndClearHashes();
		}
		
		return false;
	}
}