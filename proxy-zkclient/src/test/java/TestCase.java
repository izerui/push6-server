import com.push6.zkclient.ZkClient;
import com.push6.zkclient.ZkClientImpl;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.zookeeper.CreateMode;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Created by serv on 2015/3/9.
 */
public class TestCase {

    private ZkClient zkClient;

    @Before
    public void connect() {
        zkClient = new ZkClientImpl("192.168.1.141:2181",new ConnectionStateListener() {
            @Override
            public void stateChanged(CuratorFramework client, ConnectionState newState) {
                System.out.println(newState.toString());
            }
        });
        zkClient.connect();
    }

    @Test
    public void close() {
        zkClient.close();
    }

    @Test
    public void forPath1() {
        zkClient.createPath("/fefeftest/fjfjfjfjfj");
    }

    @Test
    public void forPath2() {
        zkClient.createPath("/test/dddsfsdf", "sdfojjwef".getBytes());
    }

    @Test
    public void deletePath() {
        zkClient.deletePath("/test/fjfjfjfjfj");
    }

//    @Test
//    public void forEphemeralNode(){
//        zkExecutor.execute(new CreateEphemeralNodeCommand("/test/临时节点", PersistentEphemeralNode.Mode.EPHEMERAL,"测试数据".getBytes()));
//    }

    @Test
    public void addListener() {

        zkClient.pathListener("/test/listener111", new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                System.out.println(event.getType().name());
            }
        });
        zkClient.createPath("/test/listener111/sdfojwef");
    }

    @Test
    public void addListener2() throws InterruptedException {
        String path = "/test/listener222/aaasdfdsfdsfdsfb";
        zkClient.nodeListener(path, new NodeCacheListener() {
            @Override
            public void nodeChanged() throws Exception {
                System.out.println("监听到事件");
            }
        });
        zkClient.createPath(path, "设置了新的值".getBytes(), CreateMode.EPHEMERAL);
        Thread.sleep(3000);
        zkClient.close();
    }

    @Test
    public void getChildren() throws InterruptedException {
        List<String> children = zkClient.getChildren("/");
        System.out.println(children.toString());
        new CountDownLatch(1).await();
    }

}
