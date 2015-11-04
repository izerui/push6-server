import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.CountDownLatch;

/**
 * Created by serv on 2015/3/11.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:restful.xml")
public class PublishTest {

    @Test
    public void test() throws InterruptedException {
        new CountDownLatch(1).await();
    }
}
