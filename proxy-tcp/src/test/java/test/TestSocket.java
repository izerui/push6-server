package test;

import com.push6.proxy.message.Protocol;
import com.push6.proxy.server.utils.JsonUtils;
import com.push6.proxy.server.utils.MD5;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

/**
 * Created by serv on 2014/10/30.
 */
public class TestSocket {

    private Socket socket;

    @Before
    public void init() throws IOException {
//        socket = new Socket("localhost",8584);
//        socket = new Socket("192.168.1.140",8584);
//        socket = new Socket("localhost",8584);
        socket = new Socket("192.168.1.128",8584);
    }

    @After
    public void close() throws IOException {
        if(socket!=null){
            socket.close();
        }
    }


    private String getWorkKey() throws IOException {
        String json = "{\n" +
//                "    \"deviceInfo\": \"0013300005\", \n" +
                "    \"deviceInfo\": \"0000100002\", \n" +
                "    \"password\": \"123456\", \n" +
                "    \"nonceStr\": \""+Math.random()+"\"\n" +
                "}";
        Protocol.Message request = Protocol.Message.newBuilder()
                .setPartner("demoPartner")
                .setCommand("workKeyCommand")
                .setSign(MD5.sign(json, "1234567890", "utf-8"))
                .setMessage(json)
                .build();

        request.writeDelimitedTo(socket.getOutputStream());

        Protocol.Message response = Protocol.Message.parseDelimitedFrom(socket.getInputStream());
        boolean verify = MD5.verify(response.getMessage(), response.getSign(), "1234567890", "utf-8");

        System.out.println(response.toString()+verify);
        System.out.println(response.getMessage());
        return JsonUtils.readValue(response.getMessage()).path("workKey").asText();
    }

    @Test
    public void demoCommand() throws IOException {
        while (true){
            String json = "{\"workKey\":\""+getWorkKey()+"\",\"nonceStr\":\""+Math.random()+"\"}";
            Protocol.Message request = Protocol.Message.newBuilder()
                    .setPartner("demoPartner")
                    .setCommand("demoCommand")
                    .setSign(MD5.sign(json,"1234567890","utf-8"))
                    .setMessage(json)
                    .build();
            request.writeDelimitedTo(socket.getOutputStream());
            Protocol.Message response = Protocol.Message.parseDelimitedFrom(socket.getInputStream());
            System.out.println(response.getMessage());
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    @Test
    public void demoWeiXinCommand() throws IOException {
        String json = "{\"workKey\":\""+getWorkKey()+"\",\"nonceStr\":\""+Math.random()+"\"}";
        Protocol.Message request = Protocol.Message.newBuilder()
            .setPartner("demoPartner")
            .setCommand("weixinCommand")
            .setSign(MD5.sign(json,"1234567890","utf-8"))
            .setMessage(json)
            .build();
        request.writeDelimitedTo(socket.getOutputStream());
        Protocol.Message response = Protocol.Message.parseDelimitedFrom(socket.getInputStream());
        System.out.println(response.getMessage());
    }
    @Test
    public void wait4Notify() throws IOException, InterruptedException {
        //开始等待
        while (true){
            Protocol.Message response2 = Protocol.Message.parseDelimitedFrom(socket.getInputStream());
            System.out.println(response2.getMessage());
        }

    }

    @Test
    public void testForce() throws InterruptedException {
        for(int i=0;i<5;i++){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Socket sk = new Socket("192.168.1.111",8584);
                        String json = "{\"port\":\"0000600012\",\"workKey\":\"08fde6e48d6a4ed582f77ca97c89e8c5\",\"nonceStr\":\"61246c3ba7a62a7d4f8c28f6217245d2\"}";
                        Protocol.Message request = Protocol.Message.newBuilder()
                                .setPartner("demoPartner")
                                .setCommand("udpDemoCommand")
                                .setSign(MD5.sign(json,"1234567890","utf-8"))
                                .setMessage(json)
                                .build();
                        request.writeDelimitedTo(sk.getOutputStream());
                        while (true){
                            Protocol.Message response = Protocol.Message.parseDelimitedFrom(sk.getInputStream());
                            System.out.println(response.getMessage().toString()+" : "+Thread.currentThread().getName());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
        new CountDownLatch(1).await();
    }

    //测试tcp和udp推送
    @Test
    public void notifyCommandTest() throws IOException {
        for (int i=0;i<100;i++){
            Socket socket = new Socket("localhost",8584);
            String json = "{\"port\":\"0000600012\",\"workKey\":\"dfdfsdf\",\"nonceStr\":\"61246c3ba7a62a7d4f8c28f6217245d2\"}";
            Protocol.Message request = Protocol.Message.newBuilder()
                    .setPartner("demoPartner")
                    .setCommand("demoCommand")
                    .setSign(MD5.sign(json,"1234567890","utf-8"))
                    .setMessage(json)
                    .build();
            request.writeDelimitedTo(socket.getOutputStream());
            Protocol.Message response = Protocol.Message.parseDelimitedFrom(socket.getInputStream());
            System.out.println(response.getMessage().toString()+" : [thread] "+Thread.currentThread().getName()+" 第 "+i+" 条");
        }
    }
    @Test
    public void tcodeCommand() throws IOException {
        String json = "{\"posNo\":\"0000600012\",\"type\":\"1\",\"code\":\"000000000284\",\"usedCount\":\"1\",\"workKey\":\""+getWorkKey()+"\",\"nonceStr\":\"61246c3ba7a62a7d4f8c28f6217245d2\"}";
        Protocol.Message request = Protocol.Message.newBuilder()
            .setPartner("demoPartner")
            .setCommand("tcodeCommand")
            .setSign(MD5.sign(json,"1234567890","utf-8"))
            .setMessage(json)
            .build();
        request.writeDelimitedTo(socket.getOutputStream());
        Protocol.Message response = Protocol.Message.parseDelimitedFrom(socket.getInputStream());
        System.out.println(response.getMessage());
    }


    @Test
    public void heartBeat() throws IOException, InterruptedException {
        String workKey = getWorkKey();
        heartBeat(workKey,3000);
        new CountDownLatch(1).await();
    }

    public void heartBeat(final String workKey, final int mill) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    try {
                        Thread.sleep(mill);
                        String json = "{\"workKey\":\""+workKey+"\",\"nonceStr\":\""+Math.random()+"\"}";
                        Protocol.Message request = Protocol.Message.newBuilder()
                                .setPartner("demoPartner")
                                .setCommand("heartBeatCommand")
                                .setSign(MD5.sign(json,"1234567890","utf-8"))
                                .setMessage(json)
                                .build();
                        request.writeDelimitedTo(socket.getOutputStream());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

    }


}
