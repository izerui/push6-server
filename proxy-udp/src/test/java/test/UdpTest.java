package test;

import com.push6.proxy.message.Protocol;
import com.push6.proxy.server.utils.MD5;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

/**
 * Created by serv on 2015/3/12.
 */
public class UdpTest {

    String ip = "192.168.1.128";
//    String ip = "192.168.1.137";
    int port = 8585;

    @Test
    public void server() throws IOException {
        DatagramSocket socket = new DatagramSocket(8585);
        System.out.println("The server is ready on port " + socket.getLocalPort() + " ...");
        for (; ; ) {
            try {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                socket.receive(packet);
                ByteArrayInputStream inputStream = new ByteArrayInputStream(buffer);
                Protocol.Message response = Protocol.Message.parseDelimitedFrom(inputStream);

                System.out.println(packet.getAddress() + " " + packet.getPort() + ": "+response.toString());

                // Return the packet to the sender

                ByteArrayOutputStream output = new ByteArrayOutputStream(1024);
                Protocol.Message.Builder builder = Protocol.Message.newBuilder();
                builder.setCommand("sdjfosdjf").setMessage("sdjfodsjf").setPartner("fdsfdsf").setSign("sdfdsfwe");
                builder.build().writeDelimitedTo(output);

                packet.setData(output.toByteArray());
                socket.send(packet);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void client() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream(1024);
        Protocol.Message response = Protocol.Message.newBuilder()
                .setCommand("udpDemoCommand")
                .setMessage("sdf桑德菲杰的设计费")
                .setSign("JOISDFNID234234dfgdfg345fg")
                .build();
        response.writeDelimitedTo(output);
        DatagramPacket packet = new DatagramPacket(output.toByteArray(), output.size(), InetAddress.getByName(ip), port);

        DatagramSocket socket = new DatagramSocket();

        socket.send(packet);


        // Set a receive timeout, 2000 milliseconds
        socket.setSoTimeout(5000);


        byte[] buffer = new byte[1024];
        packet.setData(buffer);
        socket.receive(packet);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(buffer);
        Protocol.Message request = Protocol.Message.parseDelimitedFrom(inputStream);
        System.out.println("返回消息:" + request.toString());

    }


    @Test
    public void serverRequest() throws IOException {
        DatagramSocket socket = new DatagramSocket(8585);
        System.out.println("The server is ready on port " + socket.getLocalPort() + " ...");
        for (; ; ) {
            try {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                socket.receive(packet);
                ByteArrayInputStream inputStream = new ByteArrayInputStream(buffer);
                Protocol.Message response = Protocol.Message.parseDelimitedFrom(inputStream);

                System.out.println(packet.getAddress() + " " + packet.getPort() + ": "+response.toString());

                // Return the packet to the sender

                ByteArrayOutputStream output = new ByteArrayOutputStream(1024);
                Protocol.Message.Builder builder = Protocol.Message.newBuilder();
                builder.setCommand("sdjfosdjf").setMessage("sdjfodsjf").setSign("sdfdsfwe");
                builder.build().writeDelimitedTo(output);

                packet.setData(output.toByteArray());
                socket.send(packet);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void batchSendTest() throws IOException, InterruptedException {

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while (true){
                    try{
                        Thread.sleep(1000);
                        Protocol.Message.Builder builder = Protocol.Message.newBuilder();
                        String msg = "{\"workKey\":\""+ UUID.randomUUID().toString()+"\",\"nonceStr\":\""+Math.random()+"\"}";

                        Protocol.Message request = builder.setCommand("udpDemoCommand").setMessage(msg)
                                .setPartner("demoPartner")
                                .setSign(MD5.sign(msg, "1234567890", "utf-8"))
                                .build();

                        ByteArrayOutputStream output = new ByteArrayOutputStream(1024);
                        request.writeDelimitedTo(output);

                        DatagramPacket packet = new DatagramPacket(output.toByteArray(), output.size(), InetAddress.getByName(ip), port);

                        DatagramSocket socket = new DatagramSocket();

                        socket.send(packet);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

            }
        };
        new Thread(runnable).start();
        new CountDownLatch(1).await();
    }


    @Test
    public void send2NettyServer() throws IOException, InterruptedException {

        for (int i=0;i<10;i++){
            final DatagramSocket socket = new DatagramSocket(0);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String msg = "{\"workKeffffy\":\""+ UUID.randomUUID().toString()+"\",\"nonceStr\":\""+Math.random()+"\"}";

                        Protocol.Message request = Protocol.Message.newBuilder().setCommand("udpDemoCommand").setMessage(msg)
                                .setPartner("demoPartner")
                                .setSign(MD5.sign(msg, "1234567890", "utf-8"))
                                .build();

                        ByteArrayOutputStream output = new ByteArrayOutputStream(1024);
                        request.writeDelimitedTo(output);

                        DatagramPacket packet = new DatagramPacket(output.toByteArray(), output.size(), InetAddress.getByName("192.168.1.111"), 8585);


                        socket.send(packet);

                        byte[] recvBytes = new byte[1024];

                        System.out.println("开始监听:");
                        int i = 0;
                        while (true){
                            socket.receive(new DatagramPacket(recvBytes, recvBytes.length));

                            Protocol.Message resultMessage = Protocol.Message.parseDelimitedFrom(new ByteArrayInputStream(recvBytes));
                            System.out.println(resultMessage.toString()+Thread.currentThread().getName() +" 第"+i+++"条");

                        }
                    }catch (Exception e){

                    }
                }
            }).start();
        }

        new CountDownLatch(1).await();

    }
}
