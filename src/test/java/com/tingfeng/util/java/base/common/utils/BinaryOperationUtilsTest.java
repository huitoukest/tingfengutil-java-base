package com.tingfeng.util.java.base.common.utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * 二进制操作工具类测试
 */
public class BinaryOperationUtilsTest {

    /**
     * 测试获取子二进制数 - 基本功能
     */
    @Test
    public void testGetSubBinaryNumbersBasic() {
        List<Integer> result = BinaryOperationUtils.getSubBinaryNumbers(15);
        Assert.assertNotNull("结果不能为空", result);
        Assert.assertEquals("15应该包含4个2的幂", 4, result.size());
        Assert.assertTrue("应该包含1", result.contains(1));
        Assert.assertTrue("应该包含2", result.contains(2));
        Assert.assertTrue("应该包含4", result.contains(4));
        Assert.assertTrue("应该包含8", result.contains(8));
    }

    /**
     * 测试获取子二进制数 - 0
     */
    @Test
    public void testGetSubBinaryNumbersZero() {
        List<Integer> result = BinaryOperationUtils.getSubBinaryNumbers(0);
        Assert.assertNotNull("结果不能为空", result);
        Assert.assertTrue("0应该返回空列表", result.isEmpty());
    }

    /**
     * 测试获取子二进制数 - 1
     */
    @Test
    public void testGetSubBinaryNumbersOne() {
        List<Integer> result = BinaryOperationUtils.getSubBinaryNumbers(1);
        Assert.assertNotNull("结果不能为空", result);
        Assert.assertEquals("1应该包含1个2的幂", 1, result.size());
        Assert.assertTrue("应该包含1", result.contains(1));
    }

    /**
     * 测试获取子二进制数 - 2的幂
     */
    @Test
    public void testGetSubBinaryNumbersPowerOfTwo() {
        List<Integer> result = BinaryOperationUtils.getSubBinaryNumbers(8);
        Assert.assertNotNull("结果不能为空", result);
        Assert.assertEquals("8应该包含1个2的幂", 1, result.size());
        Assert.assertTrue("应该包含8", result.contains(8));
    }

    /**
     * 测试获取子二进制数 - 复杂数字
     */
    @Test
    public void testGetSubBinaryNumbersComplex() {
        List<Integer> result = BinaryOperationUtils.getSubBinaryNumbers(21);
        Assert.assertNotNull("结果不能为空", result);
        Assert.assertEquals("21应该包含3个2的幂", 3, result.size());
        Assert.assertTrue("应该包含1", result.contains(1));
        Assert.assertTrue("应该包含4", result.contains(4));
        Assert.assertTrue("应该包含16", result.contains(16));
    }

    /**
     * 测试获取子二进制数 - null输入
     */
    @Test
    public void testGetSubBinaryNumbersNull() {
        List<Integer> result = BinaryOperationUtils.getSubBinaryNumbers(null);
        Assert.assertNotNull("结果不能为空", result);
        Assert.assertTrue("null输入应该返回空列表", result.isEmpty());
    }

    /**
     * 测试获取子二进制数 - 大数字
     */
    @Test
    public void testGetSubBinaryNumbersLarge() {
        List<Integer> result = BinaryOperationUtils.getSubBinaryNumbers(1023);
        Assert.assertNotNull("结果不能为空", result);
        Assert.assertEquals("1023应该包含10个2的幂", 10, result.size());
        for (int i = 0; i < 10; i++) {
            Assert.assertTrue("应该包含" + (1 << i), result.contains(1 << i));
        }
    }

    /**
     * 测试获取包含二进制数 - 基本功能
     */
    @Test
    public void testGetContainBinaryNumbersBasic() {
        List<Integer> result = BinaryOperationUtils.getContainBinaryNumbers(8, 3);
        Assert.assertNotNull("结果不能为空", result);
        Assert.assertEquals("应该包含2个数字", 2, result.size());
        Assert.assertTrue("应该包含3", result.contains(3));
        Assert.assertTrue("应该包含7", result.contains(7));
    }

    /**
     * 测试获取包含二进制数 - 另一个例子
     */
    @Test
    public void testGetContainBinaryNumbersAnotherExample() {
        List<Integer> result = BinaryOperationUtils.getContainBinaryNumbers(7, 1);
        Assert.assertNotNull("结果不能为空", result);
        Assert.assertEquals("应该包含4个数字", 4, result.size());
        Assert.assertTrue("应该包含1", result.contains(1));
        Assert.assertTrue("应该包含3", result.contains(3));
        Assert.assertTrue("应该包含5", result.contains(5));
        Assert.assertTrue("应该包含7", result.contains(7));
    }

    /**
     * 测试获取包含二进制数 - 包含值等于最大值
     */
    @Test
    public void testGetContainBinaryNumbersContainsEqualsMax() {
        List<Integer> result = BinaryOperationUtils.getContainBinaryNumbers(5, 5);
        Assert.assertNotNull("结果不能为空", result);
        Assert.assertEquals("应该包含1个数字", 1, result.size());
        Assert.assertTrue("应该包含5", result.contains(5));
    }

    /**
     * 测试获取包含二进制数 - 包含值为0
     */
    @Test
    public void testGetContainBinaryNumbersContainsZero() {
        List<Integer> result = BinaryOperationUtils.getContainBinaryNumbers(10, 0);
        Assert.assertNotNull("结果不能为空", result);
        Assert.assertTrue("包含值为0时，所有数字都应该包含0", result.size() > 0);
    }

    /**
     * 测试获取包含二进制数 - 包含值为1
     */
    @Test
    public void testGetContainBinaryNumbersContainsOne() {
        List<Integer> result = BinaryOperationUtils.getContainBinaryNumbers(10, 1);
        Assert.assertNotNull("结果不能为空", result);
        Assert.assertTrue("应该包含奇数", result.stream().allMatch(n -> n % 2 == 1));
    }

    /**
     * 测试获取包含二进制数 - 包含值为2
     */
    @Test
    public void testGetContainBinaryNumbersContainsTwo() {
        List<Integer> result = BinaryOperationUtils.getContainBinaryNumbers(10, 2);
        Assert.assertNotNull("结果不能为空", result);
        Assert.assertTrue("所有数字与2进行与运算应该等于2", 
            result.stream().allMatch(n -> (n & 2) == 2));
    }

    /**
     * 测试获取包含二进制数 - 包含值为4
     */
    @Test
    public void testGetContainBinaryNumbersContainsFour() {
        List<Integer> result = BinaryOperationUtils.getContainBinaryNumbers(15, 4);
        Assert.assertNotNull("结果不能为空", result);
        Assert.assertTrue("所有数字与4进行与运算应该等于4", 
            result.stream().allMatch(n -> (n & 4) == 4));
    }

    /**
     * 测试获取包含二进制数 - 包含值为3
     */
    @Test
    public void testGetContainBinaryNumbersContainsThree() {
        List<Integer> result = BinaryOperationUtils.getContainBinaryNumbers(15, 3);
        Assert.assertNotNull("结果不能为空", result);
        Assert.assertTrue("所有数字与3进行与运算应该等于3", 
            result.stream().allMatch(n -> (n & 3) == 3));
    }

    /**
     * 测试获取包含二进制数 - 大范围
     */
    @Test
    public void testGetContainBinaryNumbersLargeRange() {
        List<Integer> result = BinaryOperationUtils.getContainBinaryNumbers(100, 8);
        Assert.assertNotNull("结果不能为空", result);
        Assert.assertTrue("结果应该不为空", result.size() > 0);
        Assert.assertTrue("所有数字与8进行与运算应该等于8", 
            result.stream().allMatch(n -> (n & 8) == 8));
    }

    /**
     * 测试获取二进制值 - 2的幂
     */
    @Test
    public void testGetBinaryValueByPowBasic() {
        double result = BinaryOperationUtils.getBinaryValueByPow(0);
        Assert.assertEquals("2的0次幂应该为1", 1.0, result, 0.0001);
    }

    /**
     * 测试获取二进制值 - 2的1次幂
     */
    @Test
    public void testGetBinaryValueByPowOne() {
        double result = BinaryOperationUtils.getBinaryValueByPow(1);
        Assert.assertEquals("2的1次幂应该为2", 2.0, result, 0.0001);
    }

    /**
     * 测试获取二进制值 - 2的10次幂
     */
    @Test
    public void testGetBinaryValueByPowTen() {
        double result = BinaryOperationUtils.getBinaryValueByPow(10);
        Assert.assertEquals("2的10次幂应该为1024", 1024.0, result, 0.0001);
    }

    /**
     * 测试获取二进制值 - 2的20次幂
     */
    @Test
    public void testGetBinaryValueByPowTwenty() {
        double result = BinaryOperationUtils.getBinaryValueByPow(20);
        Assert.assertEquals("2的20次幂应该为1048576", 1048576.0, result, 0.0001);
    }

    /**
     * 测试获取二进制值 - 最大值
     */
    @Test
    public void testGetBinaryValueByPowMax() {
        double result = BinaryOperationUtils.getBinaryValueByPow(64);
        Assert.assertTrue("2的64次幂应该是一个很大的数", result > 1.0E18);
    }

    /**
     * 测试获取二进制值 - 负指数
     */
    @Test
    public void testGetBinaryValueByPowNegative() {
        double result = BinaryOperationUtils.getBinaryValueByPow(-1);
        Assert.assertEquals("2的-1次幂应该为0.5", 0.5, result, 0.0001);
    }

    /**
     * 测试获取二进制值 - 负指数2
     */
    @Test
    public void testGetBinaryValueByPowNegativeTwo() {
        double result = BinaryOperationUtils.getBinaryValueByPow(-2);
        Assert.assertEquals("2的-2次幂应该为0.25", 0.25, result, 0.0001);
    }

    /**
     * 测试获取二进制值 - 超出范围
     */
    @Test(expected = RuntimeException.class)
    public void testGetBinaryValueByPowOutOfRange() {
        BinaryOperationUtils.getBinaryValueByPow(65);
    }

    /**
     * 测试获取第一个大于当前值的二进制值 - 基本功能
     */
    @Test
    public void testGetFirstThanBinaryValueBasic() {
        long result = BinaryOperationUtils.getFirstThanBinaryValue(100);
        Assert.assertEquals("第一个大于100的2的幂应该是128", 128, result);
    }

    /**
     * 测试获取第一个大于当前值的二进制值 - 正好是2的幂
     */
    @Test
    public void testGetFirstThanBinaryValueExactPower() {
        long result = BinaryOperationUtils.getFirstThanBinaryValue(128);
        Assert.assertEquals("第一个大于等于128的2的幂应该是128", 128, result);
    }

    /**
     * 测试获取第一个大于当前值的二进制值 - 小数字
     */
    @Test
    public void testGetFirstThanBinaryValueSmall() {
        long result = BinaryOperationUtils.getFirstThanBinaryValue(1);
        Assert.assertEquals("第一个大于等于1的2的幂应该是1", 1, result);
    }

    /**
     * 测试获取第一个大于当前值的二进制值 - 0
     */
    @Test
    public void testGetFirstThanBinaryValueZero() {
        long result = BinaryOperationUtils.getFirstThanBinaryValue(0);
        Assert.assertEquals("第一个大于等于0的2的幂应该是1", 1, result);
    }

    /**
     * 测试获取第一个大于当前值的二进制值 - 大数字
     */
    @Test
    public void testGetFirstThanBinaryValueLarge() {
        long result = BinaryOperationUtils.getFirstThanBinaryValue(1000000);
        Assert.assertEquals("第一个大于1000000的2的幂应该是1048576", 1048576, result);
    }

    /**
     * 测试获取第一个大于当前值的二进制值 - 非常大的数字
     */
    @Test
    public void testGetFirstThanBinaryValueVeryLarge() {
        long result = BinaryOperationUtils.getFirstThanBinaryValue(Long.MAX_VALUE / 2);
        Assert.assertTrue("应该返回一个有效的2的幂", result > 0);
    }

    /**
     * 测试获取第一个大于当前值的二进制值 - 超出范围
     */
    @Test
    public void testGetFirstThanBinaryValueOutOfRange() {
        long result = BinaryOperationUtils.getFirstThanBinaryValue(Long.MAX_VALUE);
        Assert.assertEquals("超出范围应该返回-1", -1, result);
    }

    /**
     * 测试二进制操作的幂等性
     */
    @Test
    public void testBinaryOperationIdempotence() {
        List<Integer> result1 = BinaryOperationUtils.getSubBinaryNumbers(15);
        List<Integer> result2 = BinaryOperationUtils.getSubBinaryNumbers(15);
        Assert.assertEquals("相同输入应该产生相同输出", result1, result2);
    }

    /**
     * 测试二进制操作的组合性
     */
    @Test
    public void testBinaryOperationComposition() {
        List<Integer> subNumbers = BinaryOperationUtils.getSubBinaryNumbers(15);
        int sum = subNumbers.stream().mapToInt(Integer::intValue).sum();
        Assert.assertEquals("子二进制数的和应该等于原数", 15, sum);
    }

    /**
     * 测试包含二进制数的边界情况
     */
    @Test
    public void testContainBinaryNumbersEdgeCase() {
        List<Integer> result = BinaryOperationUtils.getContainBinaryNumbers(1, 1);
        Assert.assertEquals("最大值等于包含值时，应该只包含一个数字", 1, result.size());
        Assert.assertTrue("应该包含1", result.contains(1));
    }
}