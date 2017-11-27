package merkletree;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tiesdb.lib.merkletree.BinaryTrie;

@DisplayName("Binary Trie testing")
class TestBinaryTrie {

	@Test
	void test() {
		BinaryTrie<Integer> trie = new BinaryTrie<Integer>();
		
		trie.insert(0x10101, 0x0123456789ABCDEFL, 1);
		trie.insert(0x10101, 0x0123456789ABCDEFL, 2);
		trie.insert(0x13101, 0x0123456789ABCDEFL, 3);
		trie.insert(0x13100, 0x0123456789ABCDEFL, 4);

	}

}
