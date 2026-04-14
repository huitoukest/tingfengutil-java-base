package com.tingfeng.util.java.base.bean;

import com.tingfeng.util.java.base.lang.base.TrieNode;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * TrieNode 类的单元测试
 * 测试 Trie 树的各种功能和边界情况
 *
 * @author huitoukest
 */
public class TrieNodeTest {

    /**
     * 测试插入和查找功能
     */
    @Test
    public void testInsertAndSearch() {
        TrieNode trie = new TrieNode();

        // 插入单词
        trie.insert("apple");
        trie.insert("app");
        trie.insert("banana");
        trie.insert("bat");

        // 测试查找存在的单词
        Assert.assertTrue("应该找到 apple", trie.search("apple"));
        Assert.assertTrue("应该找到 app", trie.search("app"));
        Assert.assertTrue("应该找到 banana", trie.search("banana"));
        Assert.assertTrue("应该找到 bat", trie.search("bat"));

        // 测试查找不存在的单词
        Assert.assertFalse("不应该找到 applet", trie.search("applet"));
        Assert.assertFalse("不应该找到 ban", trie.search("ban"));
        Assert.assertFalse("不应该找到 cat", trie.search("cat"));

        // 测试空字符串
        Assert.assertFalse("空字符串应该返回 false", trie.search(""));

        // 测试 null
        Assert.assertFalse("null 应该返回 false", trie.search(null));
    }

    /**
     * 测试前缀检查功能
     */
    @Test
    public void testStartsWithPrefix() {
        TrieNode trie = new TrieNode();

        // 插入单词
        trie.insert("apple");
        trie.insert("app");
        trie.insert("banana");

        // 测试存在的前缀
        Assert.assertTrue("应该找到前缀 'app'", trie.startsWithPrefix("app"));
        Assert.assertTrue("应该找到前缀 'a'", trie.startsWithPrefix("a"));
        Assert.assertTrue("应该找到前缀 'b'", trie.startsWithPrefix("b"));
        Assert.assertTrue("应该找到前缀 'ban'", trie.startsWithPrefix("ban"));

        // 测试不存在的前缀
        Assert.assertFalse("不应该找到前缀 'apx'", trie.startsWithPrefix("apx"));
        Assert.assertFalse("不应该找到前缀 'c'", trie.startsWithPrefix("c"));

        // 测试空字符串
        Assert.assertTrue("空字符串应该返回 true", trie.startsWithPrefix(""));

        // 测试 null
        Assert.assertTrue("null 应该返回 true", trie.startsWithPrefix(null));
    }

    /**
     * 测试前缀存在检查功能
     */
    @Test
    public void testExistPrefixOf() {
        TrieNode trie = new TrieNode();

        // 插入单词
        trie.insert("app");
        trie.insert("ban");

        // 测试存在前缀的情况
        Assert.assertTrue("'apple' 应该有前缀 'app'", trie.existPrefixOf("apple"));
        Assert.assertTrue("'banana' 应该有前缀 'ban'", trie.existPrefixOf("banana"));

        // 测试不存在前缀的情况
        Assert.assertFalse("'cat' 不应该有前缀", trie.existPrefixOf("cat"));
        Assert.assertFalse("'ap' 不应该有前缀", trie.existPrefixOf("ap"));

        // 测试空字符串
        Assert.assertFalse("空字符串应该返回 false", trie.existPrefixOf(""));

        // 测试 null
        Assert.assertFalse("null 应该返回 false", trie.existPrefixOf(null));
    }

    /**
     * 测试删除功能
     */
    @Test
    public void testDelete() {
        TrieNode trie = new TrieNode();

        // 插入单词
        trie.insert("apple");
        trie.insert("app");
        trie.insert("banana");

        // 测试删除不存在的单词
        Assert.assertFalse("删除不存在的单词应该返回 false", trie.delete("cat"));

        // 测试删除空字符串
        Assert.assertFalse("删除空字符串应该返回 false", trie.delete(""));

        // 测试删除 null
        Assert.assertFalse("删除 null 应该返回 false", trie.delete(null));

        // 测试删除单词
        Assert.assertTrue("删除 'banana' 应该返回 true", trie.delete("banana"));
        Assert.assertFalse("删除后不应该找到 'banana'", trie.search("banana"));
        Assert.assertTrue("'apple' 应该仍然存在", trie.search("apple"));
        Assert.assertTrue("'app' 应该仍然存在", trie.search("app"));

        // 测试删除前缀单词
        Assert.assertTrue("删除 'app' 应该返回 true", trie.delete("app"));
        Assert.assertFalse("删除后不应该找到 'app'", trie.search("app"));
        Assert.assertTrue("'apple' 应该仍然存在", trie.search("apple"));

        // 测试删除完整单词
        Assert.assertTrue("删除 'apple' 应该返回 true", trie.delete("apple"));
        Assert.assertFalse("删除后不应该找到 'apple'", trie.search("apple"));
    }

    /**
     * 测试获取所有单词功能
     */
    @Test
    public void testGetAllWords() {
        TrieNode trie = new TrieNode();

        // 测试空树
        List<String> emptyWords = trie.getAllWords();
        Assert.assertTrue("空树应该返回空列表", emptyWords.isEmpty());

        // 插入单词
        trie.insert("apple");
        trie.insert("app");
        trie.insert("banana");

        // 测试获取所有单词
        List<String> words = trie.getAllWords();
        Assert.assertEquals("应该返回 3 个单词", 3, words.size());
        Assert.assertTrue("应该包含 'apple'", words.contains("apple"));
        Assert.assertTrue("应该包含 'app'", words.contains("app"));
        Assert.assertTrue("应该包含 'banana'", words.contains("banana"));
    }

    /**
     * 测试获取指定前缀的单词功能
     */
    @Test
    public void testGetWordsWithPrefix() {
        TrieNode trie = new TrieNode();

        // 插入单词
        trie.insert("apple");
        trie.insert("app");
        trie.insert("application");
        trie.insert("banana");

        // 测试获取前缀为 "app" 的单词
        List<String> appWords = trie.getWordsWithPrefix("app");
        Assert.assertEquals("应该返回 3 个单词", 3, appWords.size());
        Assert.assertTrue("应该包含 'app'", appWords.contains("app"));
        Assert.assertTrue("应该包含 'apple'", appWords.contains("apple"));
        Assert.assertTrue("应该包含 'application'", appWords.contains("application"));

        // 测试获取不存在的前缀
        List<String> noWords = trie.getWordsWithPrefix("cat");
        Assert.assertTrue("不存在的前缀应该返回空列表", noWords.isEmpty());

        // 测试空前缀
        List<String> allWords = trie.getWordsWithPrefix("");
        Assert.assertEquals("空前缀应该返回所有单词", 4, allWords.size());

        // 测试 null
        List<String> nullWords = trie.getWordsWithPrefix(null);
        Assert.assertTrue("null 应该返回空列表", nullWords.isEmpty());
    }

    /**
     * 测试清空功能
     */
    @Test
    public void testClear() {
        TrieNode trie = new TrieNode();

        // 插入单词
        trie.insert("apple");
        trie.insert("banana");

        // 测试清空
        trie.clear();

        // 测试清空后是否为空
        Assert.assertTrue("清空后应该为空", trie.isEmpty());
        Assert.assertFalse("清空后不应该找到 'apple'", trie.search("apple"));
        Assert.assertFalse("清空后不应该找到 'banana'", trie.search("banana"));
    }

    /**
     * 测试大小和空检查功能
     */
    @Test
    public void testSizeAndIsEmpty() {
        TrieNode trie = new TrieNode();

        // 测试空树
        Assert.assertTrue("空树应该返回 true", trie.isEmpty());
        Assert.assertEquals("空树大小应该为 0", 0, trie.size());

        // 插入单词
        trie.insert("apple");
        trie.insert("banana");

        // 测试非空树
        Assert.assertFalse("非空树应该返回 false", trie.isEmpty());
        Assert.assertEquals("树大小应该为 2", 2, trie.size());

        // 删除所有单词
        trie.delete("apple");
        trie.delete("banana");

        // 测试空树
        Assert.assertTrue("删除所有单词后应该为空", trie.isEmpty());
        Assert.assertEquals("删除所有单词后大小应该为 0", 0, trie.size());
    }

    /**
     * 测试边界情况
     */
    @Test
    public void testEdgeCases() {
        TrieNode trie = new TrieNode();

        // 测试单字符单词
        trie.insert("a");
        Assert.assertTrue("应该找到 'a'", trie.search("a"));
        Assert.assertTrue("应该找到前缀 'a'", trie.startsWithPrefix("a"));

        // 测试相同前缀的单词
        trie.insert("ab");
        trie.insert("abc");
        trie.insert("abcd");

        Assert.assertTrue("应该找到 'ab'", trie.search("ab"));
        Assert.assertTrue("应该找到 'abc'", trie.search("abc"));
        Assert.assertTrue("应该找到 'abcd'", trie.search("abcd"));

        // 测试删除中间单词
        trie.delete("abc");
        Assert.assertFalse("删除后不应该找到 'abc'", trie.search("abc"));
        Assert.assertTrue("'ab' 应该仍然存在", trie.search("ab"));
        Assert.assertTrue("'abcd' 应该仍然存在", trie.search("abcd"));
    }

    /**
     * 测试性能
     */
    @Test
    public void testPerformance() {
        TrieNode trie = new TrieNode();

        // 插入大量单词
        long startTime = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            trie.insert("word" + i);
        }
        long insertTime = System.nanoTime() - startTime;

        // 查找单词
        startTime = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            trie.search("word" + i);
        }
        long searchTime = System.nanoTime() - startTime;

        // 前缀检查
        startTime = System.nanoTime();
        for (int i = 0; i < 100; i++) {
            trie.startsWithPrefix("word" + i);
        }
        long prefixTime = System.nanoTime() - startTime;

        // 性能应该在可接受范围内
        System.out.println("插入 1000 个单词耗时: " + (insertTime / 1000000) + "ms");
        System.out.println("查找 1000 个单词耗时: " + (searchTime / 1000000) + "ms");
        System.out.println("前缀检查 100 次耗时: " + (prefixTime / 1000000) + "ms");

        Assert.assertTrue("插入性能应该良好", insertTime < 100000000); // 100ms
        Assert.assertTrue("查找性能应该良好", searchTime < 50000000);  // 50ms
        Assert.assertTrue("前缀检查性能应该良好", prefixTime < 10000000); // 10ms
    }
}