package com.tingfeng.util.java.base.file.csv;

import com.tingfeng.util.java.base.common.utils.RandomUtils;
import com.tingfeng.util.java.base.common.utils.datetime.DateUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

public class CSVUtilTest {

    @Test
    public void readCSVInBatch() throws URISyntaxException {
        URL resource = Thread.currentThread().getContextClassLoader().getResource("CSVTest.csv");
        Path path = Paths.get(resource.toURI());
        CSVUtil.readCSVInBatch(RandomUtils.randomInt(2,2000),CSVReadTestVO.class,
                () -> {
                    try {
                        return Files.lines(path, StandardCharsets.UTF_8);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                },
                list -> {
                    Assert.assertEquals(2,list.size());
                    Assert.assertEquals(DateUtils.getDate("2022-04-28 00:00:00"),list.get(0).getHeader5());
                });
    }

    @Test
    public void readCSVInBatchToMap() throws URISyntaxException {
        URL resource = Thread.currentThread().getContextClassLoader().getResource("CSVTest.csv");
        Path path = Paths.get(resource.toURI());
        CSVUtil.readCSVInBatchToMap(RandomUtils.randomInt(2,2000),
                () -> {
                    try {
                        return Files.lines(path, StandardCharsets.UTF_8);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                },
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
}