package com.tingfeng.util.java.base.web.http;

import org.junit.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class HttpUtilsTest {

    @Test
    public void toGetUrl() {
        // Test case 1: Basic URL with single parameter
        String baseUrl1 = "https://example.com/api";
        Map<String, String> params1 = new HashMap<>();
        params1.put("key", "value");
        String expected1 = "https://example.com/api?key=value";
        assertEquals(expected1, HttpUtils.toGetUrl(baseUrl1, params1));

        // Test case 2: URL with multiple parameters
        Map<String, String> params2 = new HashMap<>();
        params2.put("name", "John");
        params2.put("age", "30");
        String expected2a = "https://example.com/api?name=John&age=30";
        String expected2b = "https://example.com/api?age=30&name=John"; // order may vary
        String actual2 = HttpUtils.toGetUrl(baseUrl1, params2);
        assertTrue(actual2.equals(expected2a) || actual2.equals(expected2b));

        // Test case 4: Empty parameters map
        Map<String, String> params4 = new HashMap<>();
        assertEquals(baseUrl1, HttpUtils.toGetUrl(baseUrl1, params4));

        // Test case 5: URL with special characters in parameters
        Map<String, String> params5 = new HashMap<>();
        params5.put("search", "hello world");
        params5.put("symbol", "!@#$");
        String expected5 = "https://example.com/api?search=hello+world&symbol=%21%40%23%24";
        String actual5 = HttpUtils.toGetUrl(baseUrl1, params5, true, "UTF-8");
        assertTrue(actual5.contains("search=hello+world") &&
                actual5.contains("symbol=%21%40%23%24"));

        // Test case 6: Null parameters (should return base URL)
        assertEquals(baseUrl1, HttpUtils.toGetUrl(baseUrl1, null));

        // Test case 7: Empty base URL with parameters
        String emptyUrl = "";
        Map<String, String> params7 = new HashMap<>();
        params7.put("test", "value");
        assertEquals("?test=value", HttpUtils.toGetUrl(emptyUrl, params7));

        Map<String, Object> paramsNull = new LinkedHashMap<>();
        paramsNull.put("key", null);
        paramsNull.put("key2", null);
        String nullParam = baseUrl1 + "?key=&key2=";
        assertEquals(nullParam, HttpUtils.toGetUrl(baseUrl1, paramsNull));
    }

    @Test
    public void testGetUrl3(){
        // Test case 3: URL with existing query parameters
        String baseUrl3 = "https://example.com/api?existing=param";
        Map<String, String> params3 = new HashMap<>();
        params3.put("new", "value");
        String expected3 = "https://example.com/api?existing=param&new=value";
        assertEquals(expected3, HttpUtils.toGetUrl(baseUrl3, params3));
    }
}