package sscce.javalin.async.util;

import org.junit.jupiter.api.Test;

public class FakeWorkTest {

    @Test
    public void fakeWork() {
        int iterations = 20000;
        long time = System.currentTimeMillis();
        var result = FakeWork.work(iterations);
        System.out.println("Work took " + (System.currentTimeMillis() - time) + "ms");
    }
}
