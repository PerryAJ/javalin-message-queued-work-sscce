package sscce.javalin.async.util;

import org.apache.commons.lang3.RandomStringUtils;

public class FakeWork {
    public static String work(int iterations) {
        var randomString = "";
        for (int i = 0; i < iterations; i++) {
            randomString = RandomStringUtils.randomAlphabetic(16);
        }
        return randomString;
    }

    public static String sleepyWork(int iterations) {
        for (int i = 0; i < iterations; i++) {
            try {
                Thread.sleep(0, 10_000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return RandomStringUtils.randomAlphabetic(16);
    }
}
