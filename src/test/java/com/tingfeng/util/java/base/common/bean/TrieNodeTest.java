package com.tingfeng.util.java.base.common.bean;

import org.junit.Test;

public class TrieNodeTest {

    @Test
    public void trieNodeTest(){
        TrieNode trieNode = new TrieNode();
        trieNode.insert("abc");
        trieNode.insert("abcd");
        trieNode.insert("abce");

        System.out.println(trieNode.startsWithPrefix("a"));
        System.out.println(trieNode.startsWithPrefix("ab"));
        System.out.println(trieNode.startsWithPrefix("abcd"));
        System.out.println(trieNode.startsWithPrefix("abceef"));
        System.out.println(trieNode.startsWithPrefix(""));
    }

    @Test
    public void existPrefixOfTest(){
        TrieNode trieNode = new TrieNode();
        trieNode.insert("abc");
        trieNode.insert("abcd");
        trieNode.insert("abce");

        System.out.println(trieNode.existPrefixOf("a"));
        System.out.println(trieNode.existPrefixOf("ab"));
        System.out.println(trieNode.existPrefixOf("abcd"));
        System.out.println(trieNode.existPrefixOf("abceef"));
        System.out.println(trieNode.existPrefixOf(""));
    }
}