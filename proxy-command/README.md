# 这是一个测试udp接收消息和主动推送udp消息的测试服务

##测试步骤：

1. 启动应用 java -jar filename.jar
    
2. 请求的command命令为：udpDemoCommand ， 返回响应的测试结果

3. 主动推送的url： http://host:7953/push?ip=后台打印的ip&port=后台打印的port&msg=要发送的消息内容


### 客户端启动监听代码示例：
            
            String ip = "服务器ip";
            int port = 8585;
            Protocol.Message.Builder builder = Protocol.Message.newBuilder();
            //使用tcp维护的workkey
            String msg = "{\"workKey\":\"ddddddd\",\"nonceStr\":\""+Math.random()+"\"}";
            
            Protocol.Message request = builder.setCommand("udpDemoCommand").setMessage(msg)
                    .setPartner("demoPartner")
                    .setSign(MD5.sign(msg, "1234567890", "utf-8")) //自行签名
                    .build();
    
            ByteArrayOutputStream output = new ByteArrayOutputStream(1024);
            request.writeDelimitedTo(output);
            //构造一个udp请求数据报
            DatagramPacket packet = new DatagramPacket(output.toByteArray(), output.size(), InetAddress.getByName(ip), port);
            
            DatagramSocket socket = new DatagramSocket();
            //发送udp数据报到服务器
            socket.send(packet);
    
    
            // 设定5秒超时
            socket.setSoTimeout(5000);
            
            //打印服务器收到udp包返回的信息
            byte[] recvBytes = new byte[1024];
            DatagramPacket recvPacket = new DatagramPacket(recvBytes, recvBytes.length);
            socket.receive(recvPacket);
            Protocol.Message response = Protocol.Message.parseDelimitedFrom(new ByteArrayInputStream(recvBytes));
            System.out.println("返回消息:" + response.toString());
            
            //开始监听，这里是无限监听，正常情况下应该是收到消息后就退出
            System.out.println("开始监听:");
            //设置无超时时间，具体看业务自行修改
            socket.setSoTimeout(0);
            while (true){
                socket.receive(new DatagramPacket(recvBytes, recvBytes.length));
    
                Protocol.Message resultMessage = Protocol.Message.parseDelimitedFrom(new ByteArrayInputStream(recvBytes));
                System.out.println("收到推送过来的消息:" + resultMessage.toString());
    
            }