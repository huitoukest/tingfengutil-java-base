package com.tingfeng.util.java.base.common.bean;

import java.util.HashMap;

public class TrieNode {
    private final java.util.HashMap<Character, TrieNode> children = new HashMap<>();

    private boolean containsKey(char c) {
        return children.containsKey(c);
    }

    private TrieNode get(char c) {
        return children.get(c);
    }

    private void put(char c, TrieNode node) {
        children.put(c, node);
    }

    private boolean isEnd() {
        return children.isEmpty();
    }

    /**
     * 插入字符串到Trie树
     */
    public void insert(String word) {
        TrieNode node = this;
        for (char c : word.toCharArray()) {
            TrieNode nextNode = node.get(c);
            if (nextNode == null) {
                nextNode = new TrieNode();
                node.put(c, nextNode);
            }
            node = nextNode;
        }
    }

    /**
     * 字典树开头是否包含输入的字符串
     * 逻辑：检查字典树是否以输入的某个字符串开头
     */
    public boolean startsWithPrefix(String prefix) {
        TrieNode node = this;
        char[] charArray = prefix.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            char c = charArray[i];
            if (!node.containsKey(c)) {
                return false;
            }
            node = node.get(c);
        }
        return true;
    }

    /**
     * 字典树开头是输入字符串的前缀
     * @param fullStr 完整字符串
     * @return 当前对象是否存在fullStr前缀
     */
    public boolean existPrefixOf(String fullStr) {
        TrieNode node = this;
        char[] charArray = fullStr.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            if(node.isEnd()){
                return true;
            }
            char c = charArray[i];
            if (!node.containsKey(c)) {
                return false;
            }
            node = node.get(c);
        }
        return node.isEnd();
    }
}
