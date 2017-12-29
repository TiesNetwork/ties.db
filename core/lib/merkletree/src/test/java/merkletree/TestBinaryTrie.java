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
package merkletree;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tiesdb.lib.merkletree.BinaryTrieBuilder;
import com.tiesdb.lib.merkletree.api.Node;
import com.tiesdb.lib.merkletree.api.Trie;

@DisplayName("Binary Trie testing")
class TestBinaryTrie {
	
	void getAllNodes(Node node, ArrayList<Node> nodes) {
		Node left = node.getLeft();
		Node right = node.getRight();
		if(node.getMask() == -128 || (left == null && right == null))
			nodes.add(node);
		
		if(left != null)
			getAllNodes(left, nodes);
		if(right != null)
			getAllNodes(right, nodes);
	}

	@Test
	void test() {
		BinaryTrieBuilder b = new BinaryTrieBuilder();
		Trie trie = b.build();
		
		UUID[] uuids = new UUID[18];
		
		for(int i=0; i<uuids.length; ++i) {
			uuids[i] = new UUID(i, 0);
			if(i < 17) //17th is not included into trie
				trie.insert(uuids[i], new byte[] {(byte)i});
		}
		
		trie.hash();
		
		assertNotNull(trie.find(uuids[5]), "Should have found an object 5");
		assertNotNull(trie.find(uuids[7]), "Should have found an object 7");
		assertNotNull(trie.find(uuids[16]), "Should have found an object 16");
		assertNull(trie.find(new UUID(0x15100, 0x0123456789ABCDEFL)), "Should not have found an unexistent object");
		assertTrue(trie.check(uuids[4], new byte[] {4}), "Should have found an object 4 with right hash");
		
		trie.addToSubtrie(uuids[8]);
		trie.addToSubtrie(uuids[14]);
		trie.addToSubtrie(uuids[3]);
		trie.addToSubtrie(uuids[15]);
		trie.addToSubtrie(uuids[17]); //Does not exist in the trie but should be added to subtrie later
		
		trie.setSubtrieMode(true);
		
		assertNotNull(trie.find(uuids[15]), "Should have found an object 15");
		assertNull(trie.find(uuids[5]), "Should not have found an unexistent in subtree object");
		
		ArrayList<Node> nodes = new ArrayList<Node>();
		getAllNodes(trie, nodes);
//		Collections.shuffle(nodes);
		Collections.reverse(nodes);
		
		BinaryTrieBuilder b1 = new BinaryTrieBuilder();
		for(int i=nodes.size()-2; i>=0; --i) {
			Node n = nodes.get(i);
			
			b1.insert(n.getId(), n.getMask(), n.getHash());
		}
		
		Trie trie1 = b1.build();
		assertNull(trie1, "Incomplete trie can not be verified");
		
		Node lastNode = nodes.get(nodes.size()-1);
		b1.insert(lastNode.getId(), lastNode.getMask(), lastNode.getHash());
		
		trie1 = b1.build();
		assertNotNull(trie1, "Complete trie can be built");
		
		//Adding new UUID that was reserved during subtrie creation
		trie1.insert(uuids[17], new byte[] { 17 });
		
		trie1.hash();
		assertTrue(!Arrays.equals(trie1.hash(), trie.hash()), "Hash should have changed after adding another node");
	}

	@Test
	void testBig() {
		BinaryTrieBuilder b = new BinaryTrieBuilder();
		Trie trie = b.build();
		
		UUID[] uuids = new UUID[1000];

		for(int i=0; i<uuids.length; ++i) {
			uuids[i] = UUID.randomUUID();
			if(i < uuids.length - 10) //990th+ is not included into trie
				trie.insert(uuids[i], new byte[] {(byte)(i&0xFF), (byte)((i>>8)&0xFF), (byte)((i>>16)&0xFF)});
		}
		
		trie.hash();
		
		assertNotNull(trie.find(uuids[100]), "Should have found an object 100");
		assertNotNull(trie.find(uuids[700]), "Should have found an object 700");
		assertNotNull(trie.find(uuids[314]), "Should have found an object 314");
		assertNull(trie.find(new UUID(0x15100, 0x0123456789ABCDEFL)), "Should not have found an unexistent object");
		assertTrue(trie.check(uuids[888], new byte[] {(byte)(888&0xFF), (byte)((888>>8)&0xFF), (byte)((888>>16)&0xFF)}), "Should have found an object 888 with right hash");
		
		trie.addToSubtrie(uuids[150]);
		trie.addToSubtrie(uuids[242]);
		trie.addToSubtrie(uuids[423]);
		trie.addToSubtrie(uuids[823]);
		trie.addToSubtrie(uuids[996]); //Does not exist in the trie but should be added to subtrie later
		trie.addToSubtrie(uuids[993]); //Does not exist in the trie but should be added to subtrie later
		
		trie.setSubtrieMode(true);
		
		assertNotNull(trie.find(uuids[423]), "Should have found an object 423");
		assertNull(trie.find(uuids[993]), "Should not have found an unexistent in subtree object");
		
		ArrayList<Node> nodes = new ArrayList<Node>();
		getAllNodes(trie, nodes);
		Collections.shuffle(nodes);
		
		
		byte[] wrongHash = Arrays.copyOf(trie.find(uuids[423]).getHash(), trie.find(uuids[423]).getHash().length);
		++wrongHash[0];
		
		BinaryTrieBuilder b1 = new BinaryTrieBuilder();
		for(int i=nodes.size()-1; i>=0; --i) {
			Node n = nodes.get(i);
			
			if(n.getId().equals(uuids[423]))
				b1.insert(n.getId(), n.getMask(), wrongHash);
			else
				b1.insert(n.getId(), n.getMask(), n.getHash());
		}
		
		Trie trie1 = b1.build();
		assertNull(trie1, "Incomplete trie can not be verified with wrong hash");
		
		b1.insert(uuids[423], (byte)0, trie.find(uuids[423]).getHash());
		trie1 = b1.build();
		assertNotNull(trie1, "Complete trie can be built with right hash");
		
		//Adding new UUID that was reserved during subtrie creation
		trie1.insert(uuids[996], new byte[] {(byte)(996&0xFF), (byte)((996>>8)&0xFF), (byte)((996>>16)&0xFF)});
		
		trie1.hash();
		assertTrue(!Arrays.equals(trie1.hash(), trie.hash()), "Hash should have changed after adding another node");
		
		
	}
}
