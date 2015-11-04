import com.push6.restful.registry.Restful;
import com.push6.restful.registry.RestfulListener;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.CountDownLatch;

/**
 * Created by serv on 2015/3/10.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:client.xml")
public class TestSpring {


    @Autowired
    Restful restful;

    @Test
    public void getChildren(){
        String demoCommand = restful.getRestUrl("demoCommand");
        System.out.println(demoCommand);
    }

    @Test
    public void listener() throws InterruptedException {
        restful.listener("demoCommand", new RestfulListener() {
            @Override
            public void childChange(String businessKey) {
                System.out.println(businessKey+"发生变化了");
            }
        });
        new CountDownLatch(1).await();
    }

}
