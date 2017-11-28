package merkletree;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tiesdb.lib.crypto.digest.DigestManager;
import com.tiesdb.lib.crypto.digest.api.Digest;
import com.tiesdb.lib.merkletree.BinaryTrie;

@DisplayName("Binary Trie testing")
class TestBinaryTrie {

	@Test
	void test() {
		Digest d = DigestManager.getDigest(DigestManager.TIGER);
		BinaryTrie trie = new BinaryTrie(d);
		
		trie.insert(0x10101, 0x0123456789ABCDEFL, new byte[] {1});
		trie.insert(0x10101, 0x0123456789ABCDEFL, new byte[] {2});
		trie.insert(0x13101, 0x0123456789ABCDEFL, new byte[] {3});
		trie.insert(0x13100, 0x0123456789ABCDEFL, new byte[] {4});
		
		trie.recomputeHash();
		
		assertNotNull(trie.find(0x10101, 0x0123456789ABCDEFL), "Should have found an object 2");
		assertNotNull(trie.find(0x13101, 0x0123456789ABCDEFL), "Should have found an object 3");
		assertNotNull(trie.find(0x13100, 0x0123456789ABCDEFL), "Should have found an object 4");
		assertNull(trie.find(0x15100, 0x0123456789ABCDEFL), "Should not have found an unexistent object");
		assertTrue(trie.check(0x13100, 0x0123456789ABCDEFL, new byte[] {4}), "Should have found an object 4 with right hash");
	}

}
