#<center>push6推送服务</center>

----------


## 序言 ##
	push6推送服务基于netty5作为后端的nio处理框架，使用google protobuf序列化、反序列。性能高、传输快、并发大。

### 消息模板定义

    option java_package = "com.push6.proxy.message";
    option java_outer_classname = "Protocol";

    message Message {
        required string partner = 1;  // 合作身份者ID
        required string command = 2;  // 要执行的命令
        required string sign = 3;  // 将message签名后的值
        optional string token = 4; // 工作秘钥
        required string message = 5;  // 传输的消息主体内容
    }



## request 请求消息 ##

* **partner** 合作身份者 唯一，由系统提供。
* **partnerKey** 合作者身份秘钥，由系统提供。
* **workKey** 每次请求业务接口之前，需要调用 **workKeyCommand** 命令，获取一个有效期为 **5分钟** 的工作秘钥 ，如果工作秘钥在有效期内，则可以重复请求业务接口，而无需再次获取。
* **command** 要执行的命令，默认的系统命令有：
 
		workKeyCommand 获取工作秘钥workKey，有效期为 5分钟
		heartBeatCommand 心跳，作用： 激活workKey，使之过期时间重新计算， 可以延长workKey的使用时间。


* **sign** 签名，保证数据完整性。数据签名流程如下：

		1. 将发送的 message 消息主体内容后面附加上 partnerKey 形成一个新的字符串 例： str = message + partnerKey;
		2. 然后计算 str 该字符串的 MD5值 并设置到sign中
* **message** 消息主体，约束(必须为json格式)：

		workKeyCommand 对应json为： {"deviceInfo":"设备号","password":"密码","nonceStr":"随机串"}
		heartBeatCommand 对应json为： {"workKey":"通过workKeyCommand命令获取的工作秘钥","nonceStr":"随机串"}

   
java示例：

tcp:

		Socket socket = new Socket("192.168.1.128",8584);
		String json = "{\"port\":\"0000600012\",\"workKey\":\""+getWorkKey()+"\",\"nonceStr\":\"61246c3ba7a62a7d4f8c28f6217245d2\"}";
		Protocol.Message request = Protocol.Message.newBuilder()
			.setPartner("demoPartner")
			.setCommand("udpDemoCommand")
			.setSign(MD5.sign(json,"1234567890","utf-8"))
			.setMessage(json)
			.build();
		request.writeDelimitedTo(socket.getOutputStream());
		while (true){
			Protocol.Message response = Protocol.Message.parseDelimitedFrom(socket.getInputStream());
			System.out.println(response.toString());
		}
    
udp：

		DatagramSocket socket = new DatagramSocket(0);
		String msg = "{\"workKey\":\""+ UUID.randomUUID().toString()+"\",\"nonceStr\":\""+Math.random()+"\"}";

		Protocol.Message request = Protocol.Message.newBuilder()
				.setCommand("udpDemoCommand")
				.setMessage(msg)
				.setPartner("demoPartner")
				.setSign(MD5.sign(msg, "1234567890", "utf-8"))
				.build();

		ByteArrayOutputStream output = new ByteArrayOutputStream(1024);
		request.writeDelimitedTo(output);

		DatagramPacket packet = new DatagramPacket(output.toByteArray(), output.size(), InetAddress.getByName("192.168.1.128"), 8585);

		socket.send(packet);

		byte[] recvBytes = new byte[1024];

		System.out.println("开始监听:");
		while (true){
			socket.receive(new DatagramPacket(recvBytes, recvBytes.length));

			Protocol.Message resultMessage = Protocol.Message.parseDelimitedFrom(new ByteArrayInputStream(recvBytes));
			System.out.println(Thread.currentThread().getName()+"收到推送过来的消息:" + resultMessage.toString());

		}
				
    
> 注意： 

终端请求:

* 除了 workKeyCommand 命令，其他命令的消息主体必须包含 **workKey**
* 所有命令请求必须包含 **nonceStr**

服务器响应:

* 所有响应必须包含 **nonceStr**
    	

## Response 返回消息 ##

* **command** 执行的命令 请参考request中command
* **sign** 签名，保证数据完整性。需要客户端相应的签名验证返回消息的正确性。

		将 message 附加 partnerKey 然后MD5 ，并且跟sign对比是否一致，如果一致表示消息正确，否则视为无效的消息，不做业务处理。

* message 返回的消息体 
  
		workKeyCommand 对应的json： {"workKey":"6d2f9a300ec0436aa51bdc8225bc1729","nonceStr":"e30d3b6c1ece451b89008e8b96a1a159"}
		heartBeatCommand 对应的json： "" ， 根据 cuccess 判断心跳是否成功。
		

    
   注意： **errorCode** 和 **errorMsg** 为保留属性， 业务返回的json中不可作为业务字段使用。只可用来标识错误码和错误信息。


## 服务器端获取设备信息及安全校验 ##

* 设备跟服务器交互的message内容中应该避免出现设备号 deviceInfo等信息。
* 获取设备信息通过dubbo接口调用通过workKey获取
* 如果不需要获取设备信息，也必须添加workKey的有效性校验

maven： 依赖

		<dependency>
		  <groupId>com.push6</groupId>
		  <artifactId>offline-interface-remote</artifactId>
		  <version>1.8.0-RELEASE</version>
		</dependency>

dubbo接口：com.push6.proxy.utils.OfflineService
	
		/**
	     * 根据workKey获取设备号
	     * @param workKey
	     * @return
	     */
	    public String getDeviceInfoByWorkKey(String workKey);
	
	
	    
	    /**
	     * 根据设备号生成一个新的workkey
	     * @param deviceInfo
	     * @return
	     */
	    public String createWorkKeyByDeviceInfo(String deviceInfo);
	
	    /**
	     * 验证workKey是否有效
	     * @param workKey
	     * @return
	     */
	    public Boolean isValid(String workKey);
	
	
	    /**
	     * 发送通知结果给终端
	     * @param channelUUID 通知给业务的 channel唯一串
	     * @param command 通知给终端的command命令
	     * @param message 通知的内容 (必须是json类型,格式要求参看终端通信文档)
	     */
	    public void notifyDevice(String channelUUID,  String command , String message);
	
	    /**
	     * 根据partner 获取partnerKey
	     * @param partner
	     * @return
	     */
	    public String getPartnerKey(String partner);
	
	    /**
	     * 终端命令是否在白名单中，白名单中的命令不验证workkey
	     * @param command
	     * @return
	     */
	    public boolean isInWhiteList(String command);
	
	    /**
	     * 延长workkey的有效期
	     * @param workKey
	     */
	    public void expireWorkKey(String workKey,int minutes);


----------

客户端开发参考文档：

*protobuf: <a href="https://developers.google.com/protocol-buffers">https://developers.google.com/protocol-buffers</a>*

*语言支持: <a href="https://github.com/google/protobuf/wiki/Third-Party-Add-ons">https://github.com/google/protobuf/wiki/Third-Party-Add-ons</a>*



## 开放服务给终端调用 ##

> 参考 <终端命令rest服务发布方案.pdf> 文档


## 服务器端设计
> 终端代理的整体设计方案

### 工程说明：

* **push6-server-remote** 终端代理发布的dubbo接口
* **push6-server-command** 系统默认实现的一些command命令
* **push6-server-protobuf** 消息格式的封装(拆包解包机制的封装)
* **push6-server-server** 服务器端实现


### 主要包结构说明：
	
* **handler** tcp/udp请求的接收、分发、返回业务数据
* **service** dubbo服务、socket上下文持有者实现
* **command** 默认实现的几个command
* **message** 传输的消息对象
* **listener** 监听zookeeper消息节点变化，调用socketContext发送消息
* **support** 缓存、异常、枚举相关支持类
* **utils** 工具类

### 主要类作用分析

* **com.push6.proxy.server.TcpServer** tcp服务启动类
* **com.push6.proxy.server.UdpServer** udp服务启动类
* **com.push6.proxy.server.handler.TcpServerHandler** tcp消息处理类
* **com.push6.proxy.server.handler.UdpServerHandler** udp消息处理类
* **com.push6.proxy.server.SocketContext** 消息的请求和下发，socket上下文持有者(核心)
* **com.push6.proxy.server.listener.ZookeeperNotifyListener** zookeeper消息节点监听
* **com.push6.proxy.server.service.OfflineServiceImpl** dubbo接口实现类
* **com.push6.proxy.server.support.ChannelIdRedisTemplate** tcp缓存channelId的redis操作类
* **com.push6.proxy.server.support.UdpAddressRedisTemplate** udp缓存地址信息、签名key的redis操作类
* **com.push6.proxy.server.support.TcpUdpEnum** 枚举工具类、获取tcp、udp不同的缓存失效时间、缓存前缀等信息
* **com.push6.proxy.server.Application** 没它不行


----------


## tcp、udp请求执行流程分析

1. 终端请求服务器 tcp(端口：8584) udp(端口：8585)。
2. 通过protobuf粘包机制转化成相应的Request对象。
3. 由socketContext进行消息的分发(包含了签名、认证、业务行为、状态保持等)。
4. tcp将签名key放入channel变量中，udp放入UdpAddress中。
5. tcp将channelId对象放入redis缓存中、udp将封装请求Address和签名key UdpAddress 对象放入redis缓存中。(可以考虑将设备号作为唯一id关联起来，这样后台推送可以直接根据设备号推送，现况是支持同一个设备号多个连接)
6. 将上面缓存的唯一key **channelId** 附加到请求数据中，请求业务command对应的restful地址。
7. 如果业务返回有数据，则响应数据给终端。
8. 以上任何执行点抛出异常，则捕获并发送异常信息给终端，不关闭连接
9. 捕获连接断开、失效等事件移除tcp和udp的缓存

## tcp、udp后台主动推送流程分析

1. 后台程序调用dubbo接口发送消息，传入之前附加到请求数据中的唯一id **channelId** 或者叫 **channelUUID** (tcp为channelId、udp为UUID) 和相关参数
2. 代理接收到数据将channelUUID 和消息byte[] 分别写入到多个终端代理服务监听的message节点下
3. 每个代理服务会监听到节点变化，并构造消息。查找到本地channelGroup保存的channel进行发送，找不到则忽略。