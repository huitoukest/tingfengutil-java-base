package com.tingfeng.util.java.base.common.bean;

import java.util.HashMap;

/**
 * Trie 树节点类
 * Trie 树（前缀树）是一种用于高效存储和检索字符串数据集中键的树形数据结构
 * 特别适用于字符串查找、前缀匹配等场景
 * 
 * <p>特性：
 * - 单线程安全
 * - 支持插入、查找、删除、前缀检查等操作
 * - 支持获取所有单词和前缀匹配的单词
 * - 时间复杂度：插入、查找、前缀检查均为 O(m)，其中 m 是字符串长度
 * - 空间复杂度：O(ALPHABET_SIZE * m * n)，其中 ALPHABET_SIZE 是字符集大小，n 是单词数量
 * 
 * @author huitoukest
 */
public class TrieNode {
    /**
     * 子节点映射，键为字符，值为对应的子节点
     */
    private final java.util.HashMap<Character, TrieNode> children = new HashMap<>();
    
    /**
     * 标记当前节点是否是一个单词的结束
     */
    private boolean isEndOfWord = false;

    /**
     * 检查是否包含指定字符的子节点
     * @param c 要检查的字符
     * @return 如果包含指定字符的子节点，返回 true；否则返回 false
     */
    private boolean containsKey(char c) {
        return children.containsKey(c);
    }

    /**
     * 获取指定字符的子节点
     * @param c 要获取的字符
     * @return 指定字符的子节点，如果不存在则返回 null
     */
    private TrieNode get(char c) {
        return children.get(c);
    }

    /**
     * 添加子节点
     * @param c 字符
     * @param node 对应的子节点
     */
    private void put(char c, TrieNode node) {
        children.put(c, node);
    }

    /**
     * 检查是否是单词结束节点
     * @return 如果当前节点是单词结束节点，返回 true；否则返回 false
     */
    public boolean isEnd() {
        return isEndOfWord;
    }

    /**
     * 设置为单词结束节点
     */
    private void setEnd() {
        isEndOfWord = true;
    }

    /**
     * 插入字符串到Trie树
     * @param word 要插入的字符串
     */
    public void insert(String word) {
        if (word == null || word.isEmpty()) {
            return;
        }
        
        TrieNode node = this;
        for (char c : word.toCharArray()) {
            TrieNode nextNode = node.get(c);
            if (nextNode == null) {
                nextNode = new TrieNode();
                node.put(c, nextNode);
            }
            node = nextNode;
        }
        node.setEnd();
    }

    /**
     * 查找单词是否存在于Trie树中
     * @param word 要查找的单词
     * @return 如果单词存在于Trie树中，返回 true；否则返回 false
     */
    public boolean search(String word) {
        if (word == null || word.isEmpty()) {
            return false;
        }
        
        TrieNode node = this;
        for (char c : word.toCharArray()) {
            if (!node.containsKey(c)) {
                return false;
            }
            node = node.get(c);
        }
        return node.isEnd();
    }

    /**
     * 检查字典树是否包含以指定前缀开头的单词
     * @param prefix 要检查的前缀
     * @return 如果存在以指定前缀开头的单词，返回 true；否则返回 false
     */
    public boolean startsWithPrefix(String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return true;
        }
        
        TrieNode node = this;
        for (char c : prefix.toCharArray()) {
            if (!node.containsKey(c)) {
                return false;
            }
            node = node.get(c);
        }
        return true;
    }

    /**
     * 检查字典树中是否存在是指定字符串前缀的单词
     * @param fullStr 完整字符串
     * @return 如果字典树中存在是指定字符串前缀的单词，返回 true；否则返回 false
     */
    public boolean existPrefixOf(String fullStr) {
        if (fullStr == null || fullStr.isEmpty()) {
            return false;
        }
        
        TrieNode node = this;
        for (char c : fullStr.toCharArray()) {
            if (node.isEnd()) {
                return true;
            }
            if (!node.containsKey(c)) {
                return false;
            }
            node = node.get(c);
        }
        return node.isEnd();
    }

    /**
     * 删除指定单词
     * @param word 要删除的单词
     * @return 是否删除成功
     */
    public boolean delete(String word) {
        if (word == null || word.isEmpty()) {
            return false;
        }
        return delete(this, word, 0).success;
    }

    /**
     * 递归删除单词的结果
     */
    private static class DeleteResult {
        boolean success; // 是否成功删除单词
        boolean shouldDelete; // 是否应该删除当前节点

        DeleteResult(boolean success, boolean shouldDelete) {
            this.success = success;
            this.shouldDelete = shouldDelete;
        }
    }

    /**
     * 递归删除单词
     * @param node 当前节点
     * @param word 要删除的单词
     * @param index 当前处理的字符索引
     * @return 删除结果
     */
    private DeleteResult delete(TrieNode node, String word, int index) {
        if (index == word.length()) {
            if (!node.isEnd()) {
                return new DeleteResult(false, false);
            }
            node.isEndOfWord = false;
            return new DeleteResult(true, node.children.isEmpty());
        }
        
        char c = word.charAt(index);
        TrieNode child = node.get(c);
        if (child == null) {
            return new DeleteResult(false, false);
        }
        
        DeleteResult result = delete(child, word, index + 1);
        if (result.shouldDelete) {
            node.children.remove(c);
            return new DeleteResult(result.success, node.children.isEmpty() && !node.isEnd());
        }
        return new DeleteResult(result.success, false);
    }

    /**
     * 获取Trie树中的所有单词
     * @return Trie树中的所有单词列表
     */
    public java.util.List<String> getAllWords() {
        java.util.List<String> words = new java.util.ArrayList<>();
        collectWords(this, new StringBuilder(), words);
        return words;
    }

    /**
     * 递归收集所有单词
     * @param node 当前节点
     * @param prefix 当前前缀
     * @param words 单词列表
     */
    private void collectWords(TrieNode node, StringBuilder prefix, java.util.List<String> words) {
        if (node.isEnd()) {
            words.add(prefix.toString());
        }
        for (java.util.Map.Entry<Character, TrieNode> entry : node.children.entrySet()) {
            prefix.append(entry.getKey());
            collectWords(entry.getValue(), prefix, words);
            prefix.deleteCharAt(prefix.length() - 1);
        }
    }

    /**
     * 获取以指定前缀开头的所有单词
     * @param prefix 前缀
     * @return 以指定前缀开头的单词列表
     */
    public java.util.List<String> getWordsWithPrefix(String prefix) {
        java.util.List<String> words = new java.util.ArrayList<>();
        if (prefix == null) {
            return words;
        }
        
        TrieNode node = this;
        for (char c : prefix.toCharArray()) {
            if (!node.containsKey(c)) {
                return words;
            }
            node = node.get(c);
        }
        
        collectWords(node, new StringBuilder(prefix), words);
        return words;
    }

    /**
     * 清空Trie树
     */
    public void clear() {
        children.clear();
        isEndOfWord = false;
    }

    /**
     * 获取Trie树的大小（单词数量）
     * @return Trie树中的单词数量
     */
    public int size() {
        return getAllWords().size();
    }

    /**
     * 检查Trie树是否为空
     * @return 如果Trie树为空，返回 true；否则返回 false
     */
    public boolean isEmpty() {
        return children.isEmpty() && !isEndOfWord;
    }
}