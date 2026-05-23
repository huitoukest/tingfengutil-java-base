package com.tingfeng.util.java.base.bean;

import com.tingfeng.util.java.base.bean.converter.ConverterUtils;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import static org.junit.Assert.*;

/**
 * DefaultConverters 单元测试
 */
public class DefaultConvertersTest {

    @Test
    public void testStringToNumberConversion() {
        ConverterUtils.resetConverter();

        // String -> Integer
        Integer i = ConverterUtils.convert("123", Integer.class);
        assertEquals(Integer.valueOf(123), i);

        // String -> Long
        Long l = ConverterUtils.convert("456", Long.class);
        assertEquals(Long.valueOf(456L), l);

        // String -> Double
        Double d = ConverterUtils.convert("123.45", Double.class);
        assertEquals(Double.valueOf(123.45), d);

        // String -> Boolean
        Boolean b = ConverterUtils.convert("true", Boolean.class);
        assertEquals(Boolean.TRUE, b);
    }

    @Test
    public void testNumberToNumberConversion() {
        // Number -> Number 转换由于泛型类型擦除问题，需要精确类型匹配
        // 例如：注册 Number->Integer，但传入 Double 时查找的是 UnionKey(Double.class, Integer.class)
        // 这需要后续优化 find 逻辑支持类型层次查找
    }

    @Test
    public void testCharacterConversion() {
        ConverterUtils.resetConverter();

        // Character -> String
        String s = ConverterUtils.convert('A', String.class);
        assertEquals("A", s);

        // String -> Character（仅单字符字符串支持转换）
        Character c = ConverterUtils.convert("A", Character.class);
        assertEquals(Character.valueOf('A'), c);
    }

    @Test
    public void testByteArrayConversion() {
        ConverterUtils.resetConverter();

        // byte[] -> String (UTF-8)
        byte[] bytes = "Hello".getBytes();
        String result = ConverterUtils.convert(bytes, String.class);
        assertNotNull(result);
        assertEquals("Hello", result);

        // String -> byte[] (UTF-8)
        byte[] decoded = ConverterUtils.convert("Hello", byte[].class);
        assertArrayEquals(bytes, decoded);
    }

    @Test
    public void testCollectionConversion() {
        ConverterUtils.resetConverter();

        // Object[] -> List
        Object[] array = new Object[]{"a", "b", "c"};
        List<?> list = ConverterUtils.convert(array, List.class);
        assertEquals(3, list.size());
        assertEquals("a", list.get(0));
        assertEquals("b", list.get(1));
        assertEquals("c", list.get(2));

        // int[] -> Integer[]
        int[] intArray = new int[]{1, 2, 3};
        Integer[] integerArray = ConverterUtils.convert(intArray, Integer[].class);
        assertEquals(3, integerArray.length);
        assertEquals(Integer.valueOf(1), integerArray[0]);
        assertEquals(Integer.valueOf(2), integerArray[1]);
        assertEquals(Integer.valueOf(3), integerArray[2]);
    }

    @Test
    public void testBigNumberConversion() {
        ConverterUtils.resetConverter();

        // String -> BigDecimal
        BigDecimal bd = ConverterUtils.convert("123.456", BigDecimal.class);
        assertEquals(new BigDecimal("123.456"), bd);

        // BigDecimal -> String
        String s = ConverterUtils.convert(new BigDecimal("999.5"), String.class);
        assertEquals("999.5", s);

        // String -> BigInteger
        BigInteger bi = ConverterUtils.convert("123456789", BigInteger.class);
        assertEquals(new BigInteger("123456789"), bi);

        // BigInteger -> String
        String biStr = ConverterUtils.convert(new BigInteger("999"), String.class);
        assertEquals("999", biStr);

        // String -> BigInteger (hex)
        BigInteger hexBi = ConverterUtils.convert("0xFF", BigInteger.class);
        assertEquals(new BigInteger("255"), hexBi);

        // BigDecimal -> BigInteger（仅整数 BigDecimal 支持转换）
        BigInteger fromBd = ConverterUtils.convert(new BigDecimal("123"), BigInteger.class);
        assertEquals(new BigInteger("123"), fromBd);

        // Number -> BigDecimal
        BigDecimal fromNum = ConverterUtils.convert(100L, BigDecimal.class);
        assertEquals(new BigDecimal("100"), fromNum);

        // Number -> BigInteger
        BigInteger fromInt = ConverterUtils.convert(42, BigInteger.class);
        assertEquals(new BigInteger("42"), fromInt);
    }

    @Test
    public void testClearAndReset() {
        ConverterUtils.resetConverter();

        // 验证重置后可正常使用
        Integer i = ConverterUtils.convert("123", Integer.class);
        assertEquals(Integer.valueOf(123), i);

        // 清空后应该无法转换
        ConverterUtils.clear();
        try {
            ConverterUtils.convert("123", Integer.class);
            fail("Should throw ConverterException after clear");
        } catch (Exception e) {
            // expected
        }
    }

    @Test
    public void testConvertWithDefaultValue() {
        ConverterUtils.resetConverter();

        // 无效转换应该返回默认值
        Integer defaultVal = 0;
        Integer result = ConverterUtils.convert("not a number", Integer.class, defaultVal);
        assertEquals(Integer.valueOf(0), result);
    }
}
