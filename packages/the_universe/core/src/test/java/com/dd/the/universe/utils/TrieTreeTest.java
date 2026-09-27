package com.dd.the.universe.utils;

import org.junit.Test;
import static org.junit.Assert.*;

public class TrieTreeTest {
    @Test public void selectsLongestPathPrefix() {
        TrieTree tree = new TrieTree();
        tree.add("/data/game");
        tree.add("/data/game/lib");
        assertEquals("/data/game/lib", tree.search("/data/game/lib/a.so"));
    }

    @Test public void doesNotMatchSiblingPrefix() {
        TrieTree tree = new TrieTree();
        tree.add("/data/game");
        assertNull(tree.search("/data/game2/file"));
    }

    @Test public void fallsBackToLongestValidAncestor() {
        TrieTree tree = new TrieTree();
        tree.add("/data/game");
        tree.add("/data/game/lib");
        assertEquals("/data/game", tree.search("/data/game/libs/a.so"));
    }

    @Test public void supportsExactAndTrailingSlashRules() {
        TrieTree tree = new TrieTree();
        tree.add("/data/game/");
        assertEquals("/data/game/", tree.search("/data/game/files/a"));
        assertNull(tree.search(null));
        assertNull(tree.search(""));
    }
}
