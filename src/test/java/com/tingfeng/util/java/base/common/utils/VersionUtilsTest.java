package com.tingfeng.util.java.base.common.utils;

import com.tingfeng.util.java.base.common.utils.version.PatternVersionParser;
import com.tingfeng.util.java.base.common.utils.version.VersionParsedResult;
import com.tingfeng.util.java.base.common.utils.version.VersionSegment;
import com.tingfeng.util.java.base.common.utils.version.VersionParser;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.junit.Assert.*;

/**
 * VersionUtils 单元测试 —— 覆盖解析、比较、校验、边界条件及异常路径。
 */
public class VersionUtilsTest {

    // ==================== 1. 基本解析 ====================

    @Test
    public void testParseDefaultSplit() {
        VersionParsedResult result = VersionUtils.parse("1.2.3");
        assertEquals(3, result.size());
        assertEquals(1L, result.getSegments().get(0).getValue());
        assertEquals(2L, result.getSegments().get(1).getValue());
        assertEquals(3L, result.getSegments().get(2).getValue());
    }

    @Test
    public void testParseWithDash() {
        VersionParsedResult result = VersionUtils.parse("1.0.0-RC1");
        assertEquals(4, result.size());
        assertEquals(1L, result.getSegments().get(0).getValue());
        assertEquals(0L, result.getSegments().get(1).getValue());
        assertEquals(0L, result.getSegments().get(2).getValue());
        assertEquals("RC1", result.getSegments().get(3).getValue());
    }

    @Test
    public void testParseLongVersion() {
        VersionParsedResult result = VersionUtils.parse("10.0.19041.1");
        assertEquals(4, result.size());
        assertEquals(10L, result.getSegments().get(0).getValue());
        assertEquals(0L, result.getSegments().get(1).getValue());
        assertEquals(19041L, result.getSegments().get(2).getValue());
        assertEquals(1L, result.getSegments().get(3).getValue());
    }

    @Test
    public void testParseCustomSplit() {
        VersionParser parser = VersionUtils.ofPattern("/");
        VersionParsedResult result = parser.parse("2024/03/05");
        assertEquals(3, result.size());
        assertEquals(2024L, result.getSegments().get(0).getValue());
        assertEquals(3L, result.getSegments().get(1).getValue());
        assertEquals(5L, result.getSegments().get(2).getValue());
    }

    // ==================== 2. 类型推断 ====================

    @Test
    public void testTypeInferenceNumberToLong() {
        VersionParsedResult result = VersionUtils.parse("123.456.789");
        assertTrue(result.getSegments().get(0).getValue() instanceof Long);
        assertTrue(result.getSegments().get(1).getValue() instanceof Long);
        assertTrue(result.getSegments().get(2).getValue() instanceof Long);
    }

    @Test
    public void testTypeInferenceLetterToString() {
        VersionParsedResult result = VersionUtils.parse("abc.def");
        assertTrue(result.getSegments().get(0).getValue() instanceof String);
        assertTrue(result.getSegments().get(1).getValue() instanceof String);
    }

    @Test
    public void testTypeInferenceMixed() {
        VersionParsedResult result = VersionUtils.parse("1.0.abc");
        assertTrue(result.getSegments().get(0).getValue() instanceof Long);
        assertTrue(result.getSegments().get(1).getValue() instanceof Long);
        assertTrue(result.getSegments().get(2).getValue() instanceof String);
    }

    // ==================== 3. 基本比较 ====================

    @Test
    public void testCompareLess() {
        assertTrue(VersionUtils.compare("1.2.3", "1.2.4") < 0);
    }

    @Test
    public void testCompareGreater() {
        assertTrue(VersionUtils.compare("2.0.0", "1.9.9") > 0);
    }

    @Test
    public void testCompareEqual() {
        assertEquals(0, VersionUtils.compare("1.0.0", "1.0.0"));
    }

    @Test
    public void testCompareWithStringSegment() {
        // "1.0.0-RC1" vs "1.0.0" - size mismatch expected, use parser to get equal size
        // But we need versions with same size: "1.0-RC1" vs "1.0-RELEASE" -> [1,0,RC1] vs [1,0,RELEASE]
        // Actually "1.0-RC1" splits to [1, 0, RC1], "1.0-RELEASE" splits to [1, 0, RELEASE]
        // So RC1 vs RELEASE as strings
        VersionParser parser = VersionUtils.ofPattern("[.-]");
        // 1.0-RC1 -> [Long(1), Long(0), String("RC1")]
        // 1.0-RELEASE -> [Long(1), Long(0), String("RELEASE")]
        assertTrue(VersionUtils.compare("1.0-RC1", "1.0-RELEASE", parser) < 0);
    }

    // ==================== 4. 异常路径 ====================

    @Test(expected = IllegalArgumentException.class)
    public void testParseNull() {
        VersionUtils.parse(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseEmpty() {
        VersionUtils.parse("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCompareToSizeMismatch() {
        VersionUtils.compare("1.2", "1.2.3");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSegmentTypeMismatch() {
        VersionSegment s1 = new VersionSegment(1L, "1", true);
        VersionSegment s2 = new VersionSegment("1", "1", true);
        s1.compareTo(s2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCompareToComparableFlagMismatch() {
        List<VersionSegment> segments1 = Arrays.asList(
                new VersionSegment(1L, "1", true),
                new VersionSegment(2L, "2", true)
        );
        List<VersionSegment> segments2 = Arrays.asList(
                new VersionSegment(1L, "1", true),
                new VersionSegment(2L, "2", false)
        );
        new VersionParsedResult(segments1).compareTo(new VersionParsedResult(segments2));
    }

    // ==================== 5. 自定义比较器 ====================

    @Test
    public void testCustomComparator() {
        Comparator<VersionSegment> rcComparator = (a, b) -> {
            String s1 = a.getRaw();
            String s2 = b.getRaw();
            if ("RC1".equals(s1) && "RELEASE".equals(s2)) return -1;
            if ("RELEASE".equals(s1) && "RC1".equals(s2)) return 1;
            return s1.compareTo(s2);
        };
        assertTrue(VersionUtils.compare("1.0.0-RC1", "1.0.0-RELEASE",
                Collections.singletonMap(3, rcComparator)) < 0);
    }

    @Test
    public void testCustomComparatorReverse() {
        Comparator<VersionSegment> reverseComp = (a, b) -> {
            String s1 = a.getRaw();
            String s2 = b.getRaw();
            return s2.compareTo(s1);
        };
        // Without custom comparator: "1.0.0-a" < "1.0.0-b"
        VersionParser parser = VersionUtils.ofPattern("[.-]");
        assertTrue(VersionUtils.compare("1.0.0-a", "1.0.0-b", parser) < 0);
        // With custom reverse comparator at index 3
        assertTrue(VersionUtils.compare("1.0.0-a", "1.0.0-b",
                Collections.singletonMap(3, reverseComp)) > 0);
    }

    @Test
    public void testCustomComparatorOutOfBoundsIgnored() {
        // Index 10 is out of bounds for "1.2.3" (only 3 segments), should not throw
        int result = VersionUtils.compare("1.2.3", "1.2.4",
                Collections.singletonMap(10, (a, b) -> 1));
        assertEquals(-1, result); // default comparison still works
    }

    // ==================== 6. isValid ====================

    @Test
    public void testIsValidValid() {
        assertTrue(VersionUtils.isValid("1.0.0"));
    }

    @Test
    public void testIsValidNull() {
        assertFalse(VersionUtils.isValid(null));
    }

    @Test
    public void testIsValidEmpty() {
        assertFalse(VersionUtils.isValid(""));
    }

    @Test
    public void testIsValidWhitespace() {
        // Whitespace is not null/empty, so it parses as a single String segment
        assertTrue(VersionUtils.isValid(" "));
    }

    // ==================== 7. getFirst / getLast ====================

    @Test
    public void testGetFirstNormal() {
        VersionParsedResult result = VersionUtils.parse("1.2.3");
        VersionSegment first = result.getFirst();
        assertNotNull(first);
        assertEquals(1L, first.getValue());
    }

    @Test
    public void testGetLastNormal() {
        VersionParsedResult result = VersionUtils.parse("1.2.3");
        VersionSegment last = result.getLast();
        assertNotNull(last);
        assertEquals(3L, last.getValue());
    }

    @Test
    public void testGetFirstAndLastAllNonComparable() {
        List<VersionSegment> segments = Arrays.asList(
                new VersionSegment("a", "a", false),
                new VersionSegment("b", "b", false)
        );
        VersionParsedResult result = new VersionParsedResult(segments);
        assertNull(result.getFirst());
        assertNull(result.getLast());
    }

    @Test
    public void testGetFirstAndLastSingleComparable() {
        List<VersionSegment> segments = Arrays.asList(
                new VersionSegment("nope", "nope", false),
                new VersionSegment(1L, "1", true),
                new VersionSegment("skip", "skip", false)
        );
        VersionParsedResult result = new VersionParsedResult(segments);
        assertNotNull(result.getFirst());
        assertEquals(1L, result.getFirst().getValue());
        assertNotNull(result.getLast());
        assertEquals(1L, result.getLast().getValue());
    }

    // ==================== 8. Parse with custom parser ====================

    @Test
    public void testParseWithCustomParser() {
        VersionParser parser = VersionUtils.ofPattern("\\+");
        VersionParsedResult result = VersionUtils.parse("1+2+3", parser);
        assertEquals(3, result.size());
        assertEquals(1L, result.getSegments().get(0).getValue());
        assertEquals(2L, result.getSegments().get(1).getValue());
        assertEquals(3L, result.getSegments().get(2).getValue());
    }

    // ==================== 9. Parse with consecutive delimiters ====================

    @Test
    public void testParseConsecutiveDelimiters() {
        VersionParsedResult result = VersionUtils.parse("1..2");
        assertEquals(3, result.size());
        assertEquals(1L, result.getSegments().get(0).getValue());
        assertEquals("", result.getSegments().get(1).getValue());
        assertTrue(result.getSegments().get(1).getValue() instanceof String);
        assertEquals(2L, result.getSegments().get(2).getValue());
    }

    // ==================== 10. Compare with custom parser ====================

    @Test
    public void testCompareWithCustomParser() {
        VersionParser parser = VersionUtils.ofPattern("_");
        assertTrue(VersionUtils.compare("1_2_3", "1_2_4", parser) < 0);
        assertEquals(0, VersionUtils.compare("5_6_7", "5_6_7", parser));
    }

    // ==================== 11. Default parser ====================

    @Test
    public void testDefaultParser() {
        VersionParser parser = VersionUtils.defaultParser();
        assertNotNull(parser);
        VersionParsedResult result = parser.parse("1.2.3");
        assertEquals(3, result.size());
    }

    // ==================== 12. Comparable segments skipped ====================

    @Test
    public void testComparableFlagFalseSkipped() {
        // Both sides have comparable=false at same index, should be skipped
        List<VersionSegment> segments1 = Arrays.asList(
                new VersionSegment(1L, "1", true),
                new VersionSegment("skip", "skip", false)
        );
        List<VersionSegment> segments2 = Arrays.asList(
                new VersionSegment(1L, "1", true),
                new VersionSegment("different", "different", false)
        );
        VersionParsedResult r1 = new VersionParsedResult(segments1);
        VersionParsedResult r2 = new VersionParsedResult(segments2);
        assertEquals(0, r1.compareTo(r2));
    }

    // ==================== 13. putComparator and getComparators ====================

    @Test
    public void testPutComparatorAndGetComparators() {
        VersionParsedResult result = VersionUtils.parse("1.2.3");
        assertEquals(0, result.getComparators().size());

        Comparator<VersionSegment> cmp = (a, b) -> 0;
        result.putComparator(1, cmp);
        assertEquals(1, result.getComparators().size());
        assertSame(cmp, result.getComparators().get(1));
    }

    // ==================== 14. PatternVersionParser with date detection ====================

    @Test
    public void testDateDetectionOffByDefault() {
        // 20240101 should be parsed as Long, not Date
        VersionParser parser = new PatternVersionParser("[.-]");
        VersionParsedResult result = parser.parse("20240101");
        assertTrue(result.getFirst().getValue() instanceof Long);
    }

    @Test
    public void testDateDetectionOnForDateString() {
        // "2024-01-01" with date detection -> Long(2024), Long(1), Long(1)
        // Because "-" is the split char, so segments are "2024", "01", "01" all as Long
        VersionParser parser = new PatternVersionParser("[.-]", true);
        VersionParsedResult result = parser.parse("2024-01-01");
        assertEquals(3, result.size());
        assertTrue(result.getSegments().get(0).getValue() instanceof Long);
        assertEquals(2024L, result.getSegments().get(0).getValue());
    }

    // ==================== 15. Whitespace handling ====================

    @Test
    public void testParseWhitespace() {
        VersionParsedResult result = VersionUtils.parse(" ");
        assertEquals(1, result.size());
        assertTrue(result.getFirst().getValue() instanceof String);
        assertEquals(" ", result.getFirst().getValue());
    }

    // ==================== 16. Comparable flag consistency in VersionSegment ====================

    @Test(expected = IllegalArgumentException.class)
    public void testSegmentComparableFlagMismatch() {
        VersionSegment s1 = new VersionSegment(1L, "1", true);
        VersionSegment s2 = new VersionSegment(2L, "2", false);
        s1.compareTo(s2);
    }

    @Test
    public void testSegmentBothNonComparableReturnsZero() {
        VersionSegment s1 = new VersionSegment(1L, "1", false);
        VersionSegment s2 = new VersionSegment(2L, "2", false);
        assertEquals(0, s1.compareTo(s2));
    }
}
