package com.tingfeng.util.java.base.file.csv;

import com.alibaba.fastjson.JSON;
import com.tingfeng.util.java.base.common.utils.RandomUtils;
import com.tingfeng.util.java.base.common.utils.datetime.DateUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class CSVUtilTest {

    @Test
    public void readCSVInBatch() throws URISyntaxException, IOException {
        URL resource = Thread.currentThread().getContextClassLoader().getResource("CSVTest.csv");
        Path path = Paths.get(resource.toURI());
        CSVUtil.readCSVInBatch(RandomUtils.randomInt(2,2000),CSVReadTestVO.class,Files.lines(path, StandardCharsets.UTF_8),
                list -> {
                    Assert.assertEquals(2,list.size());
                    Assert.assertEquals(DateUtils.getDate("2022-04-28 00:00:00"),list.get(0).getHeader5());
                });
    }

    @Test
    public void readCSVInBatchToMap() throws URISyntaxException, IOException {
        URL resource = Thread.currentThread().getContextClassLoader().getResource("CSVTest.csv");
        Path path = Paths.get(resource.toURI());
        CSVUtil.readCSVInBatchToMap(RandomUtils.randomInt(2,2000), Files.lines(path, StandardCharsets.UTF_8),
                list -> {
                    Assert.assertEquals(2,list.size());
                    Assert.assertEquals("2022-04-28 00:00:00",list.get(0).get("header5"));
                });
    }

    public static class CSVReadTestVO{
        private String header2;
        private Date header5;

        public String getHeader2() {
            return header2;
        }

        public void setHeader2(String header2) {
            this.header2 = header2;
        }

        public Date getHeader5() {
            return header5;
        }

        public void setHeader5(Date header5) {
            this.header5 = header5;
        }
    }


    @Test
    public void readToBean() throws URISyntaxException {
        URL resource = Thread.currentThread().getContextClassLoader().getResource("CSVTest.csv");
        Path path = Paths.get(resource.toURI());
        List<CSVReadTestVO> list = CSVUtil.readToBean(CSVReadTestVO.class, path, Charset.forName("UTF-8"));
        Assert.assertEquals(2,list.size());
        Assert.assertEquals(DateUtils.getDate("2022-04-28 00:00:00"),list.get(0).getHeader5());
    }

    @Test
    public void readToBean2() throws URISyntaxException {
        URL resource = Thread.currentThread().getContextClassLoader().getResource("CSVTest2.csv");
        Path path = Paths.get(resource.toURI());
        List<CSVReadTestVO> list = CSVUtil.readToBean(CSVReadTestVO.class, path, Charset.forName("UTF-8"));
        Assert.assertEquals(2,list.size());
        Assert.assertEquals(DateUtils.getDate("2022-04-28 00:00:00"),list.get(0).getHeader5());
    }

    @Test
    public void readToBeanWithMap() throws URISyntaxException {
        URL resource = Thread.currentThread().getContextClassLoader().getResource("CSVTest.csv");
        Path path = Paths.get(resource.toURI());
        List<Map> list = CSVUtil.readToBean(Map.class, path, Charset.forName("UTF-8"));
        Assert.assertEquals(2,list.size());
        Assert.assertEquals("2022-04-28 00:00:00",list.get(0).get("header5"));
    }

    @Test
    public void readToBeanWithMap2() throws URISyntaxException {
        URL resource = Thread.currentThread().getContextClassLoader().getResource("CSVTest2.csv");
        Path path = Paths.get(resource.toURI());
        List<Map> list = CSVUtil.readToBean(Map.class, path, Charset.forName("UTF-8"));
        Assert.assertEquals(2,list.size());
        Assert.assertEquals("2022-04-28 00:00:00",list.get(0).get("header5"));
    }
}