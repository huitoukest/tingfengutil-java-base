package com.tingfeng.util.java.base.bean.converter;

import org.junit.After;
import org.junit.Test;

import java.io.Serializable;
import java.util.List;

import static org.junit.Assert.*;

/**
 * 冒泡注册机制单元测试
 * <p>
 * 覆盖场景：bubbleLevel 各取值、父类链/接口链冒泡、去重、unregister、排序、ConditionConverter 委托
 */
public class BubbleRegistrationTest {

    private final DefaultConverterRegistry registry = new DefaultConverterRegistry();

    @After
    public void tearDown() {
        registry.clear();
    }

    // ==================== 1. bubbleLevel=0：不冒泡 ====================

    @Test
    public void testBubbleLevelNone() {
        // 注册 String -> Integer（bubbleLevel=0，不冒泡）
        registry.register(new Converter<String, Integer>() {
            @Override
            public Integer convert(String source) {
                return Integer.parseInt(source);
            }

            @Override
            public Class<String> getSourceType() {
                return String.class;
            }

            @Override
            public Class<Integer> getTargetType() {
                return Integer.class;
            }

            @Override
            public int bubbleLevel() {
                return 0;
            }
        });

        // 精确匹配应有
        assertFalse("(String,Integer) should have converter",
                registry.findConverters(String.class, Integer.class).isEmpty());

        // 父类（Number）不应有（bubbleLevel=0 不冒泡）
        assertTrue("(String,Number) should NOT have converter (bubbleLevel=0)",
                registry.findConverters(String.class, Number.class).isEmpty());

        // 接口（Comparable）不应有
        assertTrue("(String,Comparable) should NOT have converter (bubbleLevel=0)",
                registry.findConverters(String.class, Comparable.class).isEmpty());
    }

    // ==================== 2. bubbleLevel=1（默认）：冒泡一层 ====================

    @Test
    public void testBubbleLevelDefault() {
        // 默认 bubbleLevel=1
        registry.register(new Converter<String, Integer>() {
            @Override
            public Integer convert(String source) {
                return Integer.parseInt(source);
            }

            @Override
            public Class<String> getSourceType() {
                return String.class;
            }

            @Override
            public Class<Integer> getTargetType() {
                return Integer.class;
            }
            // bubbleLevel() default = 1
        });

        // 精确匹配：应有
        assertFalse("(String,Integer) should have converter",
                registry.findConverters(String.class, Integer.class).isEmpty());

        // Integer.getSuperclass() = Number → 应有（1 层冒泡）
        assertFalse("(String,Number) should have converter (bubbleLevel=1)",
                registry.findConverters(String.class, Number.class).isEmpty());

        // Integer.getInterfaces() 包含 Comparable
        assertFalse("(String,Comparable) should have converter (bubbleLevel=1)",
                registry.findConverters(String.class, Comparable.class).isEmpty());

        // Object 不应有（Integer.getSuperclass() = Number, Number.getSuperclass() = Object 需要 2 层）
        assertTrue("(String,Object) should NOT have converter (only 1 level from Integer)",
                registry.findConverters(String.class, Object.class).isEmpty());
    }

    // ==================== 3. bubbleLevel=-1：无限冒泡到 Object ====================

    @Test
    public void testBubbleLevelUnlimited() {
        registry.register(new Converter<String, Integer>() {
            @Override
            public Integer convert(String source) {
                return Integer.parseInt(source);
            }

            @Override
            public Class<String> getSourceType() {
                return String.class;
            }

            @Override
            public Class<Integer> getTargetType() {
                return Integer.class;
            }

            @Override
            public int bubbleLevel() {
                return -1;
            }
        });

        // Object 应有（通过 Integer → Number → Object 链）
        assertFalse("(String,Object) should have converter (bubbleLevel=-1)",
                registry.findConverters(String.class, Object.class).isEmpty());

        // 直接父类 Number
        assertFalse("(String,Number) should have converter (bubbleLevel=-1)",
                registry.findConverters(String.class, Number.class).isEmpty());

        // Serializable（Integer 的接口，通过 Number 继承）
        assertFalse("(String,Serializable) should have converter (bubbleLevel=-1)",
                registry.findConverters(String.class, Serializable.class).isEmpty());
    }

    // ==================== 4. 父类链冒泡验证 ====================

    @Test
    public void testParentClassChain() {
        // 注册 String -> Integer（bubbleLevel=2 保证冒到 Object）
        registry.register(new Converter<String, Integer>() {
            @Override
            public Integer convert(String source) {
                return Integer.parseInt(source);
            }

            @Override
            public Class<String> getSourceType() {
                return String.class;
            }

            @Override
            public Class<Integer> getTargetType() {
                return Integer.class;
            }

            @Override
            public int bubbleLevel() {
                return 2;
            }
        });

        // Integer → Number (1 层)
        assertFalse("(String,Number) should exist",
                registry.findConverters(String.class, Number.class).isEmpty());

        // Number → Object (2 层)
        assertFalse("(String,Object) should exist with bubbleLevel=2",
                registry.findConverters(String.class, Object.class).isEmpty());
    }

    // ==================== 5. 接口链冒泡验证 ====================

    @Test
    public void testInterfaceChain() {
        // 注册 String -> Integer（bubbleLevel=2）
        registry.register(new Converter<String, Integer>() {
            @Override
            public Integer convert(String source) {
                return Integer.parseInt(source);
            }

            @Override
            public Class<String> getSourceType() {
                return String.class;
            }

            @Override
            public Class<Integer> getTargetType() {
                return Integer.class;
            }

            @Override
            public int bubbleLevel() {
                return 2;
            }
        });

        // Integer 的直接接口：Comparable<Integer>
        assertFalse("(String,Comparable) should exist via Integer.getInterfaces()",
                registry.findConverters(String.class, Comparable.class).isEmpty());

        // Number 的接口：Serializable（Integer → Number → Object 链 + Number 的接口）
        assertFalse("(String,Serializable) should exist via Number.getInterfaces()",
                registry.findConverters(String.class, Serializable.class).isEmpty());
    }

    // ==================== 6. 冒泡去重验证 ====================

    @Test
    public void testBubbleDeduplication() {
        // 同时注册 String -> Integer 和 String -> Double
        // 两者都会冒泡到 (String, Number) 和 (String, Object)
        // 验证不会有重复的冒泡副本

        registry.register(new Converter<String, Integer>() {
            @Override
            public Integer convert(String source) {
                return Integer.parseInt(source);
            }

            @Override
            public Class<String> getSourceType() {
                return String.class;
            }

            @Override
            public Class<Integer> getTargetType() {
                return Integer.class;
            }

            @Override
            public int bubbleLevel() {
                return 2;
            }
        });

        registry.register(new Converter<String, Double>() {
            @Override
            public Double convert(String source) {
                return Double.parseDouble(source);
            }

            @Override
            public Class<String> getSourceType() {
                return String.class;
            }

            @Override
            public Class<Double> getTargetType() {
                return Double.class;
            }

            @Override
            public int bubbleLevel() {
                return 2;
            }
        });

        // (String, Number) 应该有 2 个（来自 Integer 和 Double 的冒泡）
        List<Converter<String, Number>> numberConverters =
                registry.findConverters(String.class, Number.class).getConverters();
        assertEquals("(String,Number) should have 2 converters (from Integer and Double)",
                2, numberConverters.size());

        // (String, Object) 应该有 2 个
        List<Converter<String, Object>> objectConverters =
                registry.findConverters(String.class, Object.class).getConverters();
        assertEquals("(String,Object) should have 2 converters (from Integer and Double)",
                2, objectConverters.size());
    }

    // ==================== 7. unregister 冒泡副本清理验证 ====================

    @Test
    public void testUnregisterRemovesBubbledCopies() {
        // 注册 String -> Integer（bubbleLevel=2）
        Converter<String, Integer> converter = new Converter<String, Integer>() {
            @Override
            public Integer convert(String source) {
                return Integer.parseInt(source);
            }

            @Override
            public Class<String> getSourceType() {
                return String.class;
            }

            @Override
            public Class<Integer> getTargetType() {
                return Integer.class;
            }

            @Override
            public int bubbleLevel() {
                return 2;
            }
        };

        registry.register(converter);

        // 确认冒泡副本存在
        assertFalse("(String,Number) should exist before unregister",
                registry.findConverters(String.class, Number.class).isEmpty());
        assertFalse("(String,Object) should exist before unregister",
                registry.findConverters(String.class, Object.class).isEmpty());

        // 注销原始 Converter
        boolean removed = registry.unregister(converter);
        assertTrue("unregister should return true", removed);

        // 原始 Converter 应被移除
        assertTrue("(String,Integer) should be empty after unregister",
                registry.findConverters(String.class, Integer.class).isEmpty());

        // 冒泡副本也应被移除
        assertTrue("(String,Number) should be empty after unregister of original",
                registry.findConverters(String.class, Number.class).isEmpty());
        assertTrue("(String,Object) should be empty after unregister of original",
                registry.findConverters(String.class, Object.class).isEmpty());
    }

    // ==================== 8. 排序正确性验证 ====================

    @Test
    public void testSortOrder() {
        // 注册两个 Converter 到 (String, Number) 键
        // converter1: Integer -> Number 的冒泡副本 (regOrder=1, order=0, originalTarget=Integer)
        // converter2: Double  -> Number 的冒泡副本 (regOrder=1, order=0, originalTarget=Double)

        registry.register(new Converter<String, Integer>() {
            @Override
            public Integer convert(String source) {
                return Integer.parseInt(source);
            }

            @Override
            public Class<String> getSourceType() {
                return String.class;
            }

            @Override
            public Class<Integer> getTargetType() {
                return Integer.class;
            }

            @Override
            public int bubbleLevel() {
                return 1;
            }
        });

        registry.register(new Converter<String, Double>() {
            @Override
            public Double convert(String source) {
                return Double.parseDouble(source);
            }

            @Override
            public Class<String> getSourceType() {
                return String.class;
            }

            @Override
            public Class<Double> getTargetType() {
                return Double.class;
            }

            @Override
            public int bubbleLevel() {
                return 1;
            }
        });

        // (String, Number) 下应有 2 个冒泡副本
        // 排序后：regOrder 相同(1), order 相同(0), source 相同(String)
        // 最后按 originalTargetTypeName: "Double" < "Integer"（字母序）
        List<Converter<String, Number>> converters =
                registry.findConverters(String.class, Number.class).getConverters();
        assertEquals("(String,Number) should have 2 converters", 2, converters.size());

        // 验证排序：Double 应在 Integer 之前（字母序）
        String firstName = getOriginalTargetTypeName(converters.get(0));
        String secondName = getOriginalTargetTypeName(converters.get(1));
        assertEquals("First should be Double (alphabetical)", "java.lang.Double", firstName);
        assertEquals("Second should be Integer (alphabetical)", "java.lang.Integer", secondName);
    }

    @Test
    public void testSortWithOrder() {
        // 两个 Converter 到 (String, Number)，但有不同的 order
        // converter1: regOrder=1, order=5
        // converter2: regOrder=1, order=1

        registry.register(new Converter<String, Integer>() {
            @Override
            public Integer convert(String source) {
                return Integer.parseInt(source);
            }

            @Override
            public Class<String> getSourceType() {
                return String.class;
            }

            @Override
            public Class<Integer> getTargetType() {
                return Integer.class;
            }

            @Override
            public int bubbleLevel() {
                return 1;
            }

            @Override
            public int order() {
                return 5;
            }
        });

        registry.register(new Converter<String, Double>() {
            @Override
            public Double convert(String source) {
                return Double.parseDouble(source);
            }

            @Override
            public Class<String> getSourceType() {
                return String.class;
            }

            @Override
            public Class<Double> getTargetType() {
                return Double.class;
            }

            @Override
            public int bubbleLevel() {
                return 1;
            }

            @Override
            public int order() {
                return 1;
            }
        });

        // 两个都是 regOrder=1，但 order 不同
        // 按 order 升序：order=1 的 Double 应在 order=5 的 Integer 之前
        List<Converter<String, Number>> converters =
                registry.findConverters(String.class, Number.class).getConverters();
        assertEquals("(String,Number) should have 2 converters", 2, converters.size());

        // order=1 (Double) 应在 order=5 (Integer) 之前
        assertEquals("First should have order=1", 1, converters.get(0).order());
        assertEquals("Second should have order=5", 5, converters.get(1).order());
    }

    // ==================== 9. ConditionConverter 冒泡副本验证 ====================

    @Test
    public void testBubbledConditionConverterDelegatesMatches() {
        // 注册 ConditionConverter: String -> Integer，仅匹配长度=1
        registry.register(new ConditionConverter<String, Integer>() {
            @Override
            public boolean matches(String source) {
                return source != null && source.length() == 1;
            }

            @Override
            public Integer convert(String source) {
                return Integer.parseInt(source);
            }

            @Override
            public Class<String> getSourceType() {
                return String.class;
            }

            @Override
            public Class<Integer> getTargetType() {
                return Integer.class;
            }
            // bubbleLevel=1 (default)
        });

        // (String, Number) 应有冒泡副本
        assertFalse("(String,Number) should have converter from bubble",
                registry.findConverters(String.class, Number.class).isEmpty());

        // 使用 getConverterByValue 验证 matches 委托
        // "1" 长度=1，应匹配
        Converter<String, Integer> matched = registry.getConverterByValue("1", String.class, Integer.class);
        assertNotNull("Should match single-char string", matched);

        // "123" 长度=3，不应匹配
        Converter<String, Number> notMatched = registry.getConverterByValue("123", String.class, Number.class);
        assertNull("Should NOT match 3-char string for Number (matches check fails)", notMatched);
    }

    // ==================== 10. 同一 key 多 Converter 共存验证 ====================

    @Test
    public void testMultipleConvertersSameKey() {
        // 直接注册到 (String, Number)：来自 Integer 和 Double 的冒泡副本
        registry.register(new Converter<String, Integer>() {
            @Override
            public Integer convert(String source) {
                return Integer.parseInt(source);
            }

            @Override
            public Class<String> getSourceType() {
                return String.class;
            }

            @Override
            public Class<Integer> getTargetType() {
                return Integer.class;
            }

            @Override
            public int bubbleLevel() {
                return 1;
            }
        });

        registry.register(new Converter<String, Double>() {
            @Override
            public Double convert(String source) {
                return Double.parseDouble(source);
            }

            @Override
            public Class<String> getSourceType() {
                return String.class;
            }

            @Override
            public Class<Double> getTargetType() {
                return Double.class;
            }

            @Override
            public int bubbleLevel() {
                return 1;
            }
        });

        // (String, Number) 应有 2 个
        List<Converter<String, Number>> numberConverters =
                registry.findConverters(String.class, Number.class).getConverters();
        assertEquals("(String,Number) should have 2 converters", 2, numberConverters.size());

        // 各自独立的精确匹配仍存在
        assertFalse("(String,Integer) should still have original",
                registry.findConverters(String.class, Integer.class).isEmpty());
        assertFalse("(String,Double) should still have original",
                registry.findConverters(String.class, Double.class).isEmpty());
    }

    // ==================== 11. registrationOrder 正确性验证 ====================

    @Test
    public void testRegistrationOrder() {
        // 注册 String -> Integer（bubbleLevel=2）
        registry.register(new Converter<String, Integer>() {
            @Override
            public Integer convert(String source) {
                return Integer.parseInt(source);
            }

            @Override
            public Class<String> getSourceType() {
                return String.class;
            }

            @Override
            public Class<Integer> getTargetType() {
                return Integer.class;
            }

            @Override
            public int bubbleLevel() {
                return 2;
            }
        });

        // 原始注册：regOrder=0
        List<Converter<String, Integer>> originals =
                registry.findConverters(String.class, Integer.class).getConverters();
        assertEquals(1, originals.size());
        assertEquals("Original converter should have regOrder=0", 0, originals.get(0).registrationOrder());

        // 1 层冒泡（Number）：regOrder=1
        List<Converter<String, Number>> numberLevel =
                registry.findConverters(String.class, Number.class).getConverters();
        assertEquals(1, numberLevel.size());
        assertEquals("Bubbled to Number should have regOrder=1", 1, numberLevel.get(0).registrationOrder());

        // 2 层冒泡（Object）：regOrder=2
        List<Converter<String, Object>> objectLevel =
                registry.findConverters(String.class, Object.class).getConverters();
        assertEquals(1, objectLevel.size());
        assertEquals("Bubbled to Object should have regOrder=2", 2, objectLevel.get(0).registrationOrder());
    }

    // ==================== 辅助方法 ====================

    /**
     * 获取 Converter 的原始目标类型名称（用于排序验证）
     */
    private static String getOriginalTargetTypeName(Converter<?, ?> c) {
        if (c instanceof DefaultConverterRegistry.BubbledConverter) {
            return ((DefaultConverterRegistry.BubbledConverter<?, ?>) c).getDelegate().getTargetType().getName();
        }
        return c.getTargetType().getName();
    }
}
