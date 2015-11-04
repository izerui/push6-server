# 终端命令rest服务发布方案 #

> 由rest服务提供者将服务注册到zk服务器上，当服务提供者关机或者未提供服务的时候，zk服务器上的对应的服务节点删除，当有多个服务提供者的时候，可以获取到多个服务提供者。
> 提供断线重连发布服务机制

#### 目的：
* 负载均衡：随机的将终端请求分发到相同的command命令对应的多个rest服务提供者
* 实时监控：当command命令对应的服务提供者发生变化(增加、删除、down机)时，可以触发监控事件，实时获取服务提供的详细情况。




## 步骤： ##

# 1. 导入依赖

	<dependency>
		<groupId>com.push6</groupId>
		<artifactId>restful-registry</artifactId>
		<version>1.2.1-RELEASE</version>
	</dependency>
	<dependency>
		<groupId>org.apache.zookeeper</groupId>
		<artifactId>zookeeper</artifactId>
		<version>3.4.6</version>
		<exclusions>
			<exclusion>
				<artifactId>slf4j-log4j12</artifactId>
				<groupId>org.slf4j</groupId>
			</exclusion>
		</exclusions>
	</dependency>


# 2. 引入服务声明文件到spring上下文


**zookeeper.xml**

	<?xml version="1.0" encoding="UTF-8"?>
	<beans xmlns="http://www.springframework.org/schema/beans"
	       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	       xsi:schemaLocation="http://www.springframework.org/schema/beans 
						http://www.springframework.org/schema/beans/spring-beans.xsd">
	
	    <bean id="zkClient" class="com.push6.zkclient.ZkClientImpl" init-method="connect" destroy-method="close">
	        <constructor-arg index="0" type="com.push6.zkclient.ZkConfig" ref="zkConfig"/>
	        <constructor-arg index="1" type="org.apache.curator.framework.state.ConnectionStateListener"
	                         ref="zkStateListener"/>
	    </bean>
	
	    <bean id="zkStateListener" class="com.push6.restful.registry.ZkStateListener">
	        <property name="restfulBean" ref="restfulBean"/>
	    </bean>
	
	    <bean id="zkConfig" class="com.push6.zkclient.ZkConfig">
	        <!-- zookeeper服务器连接串 -->
	        <property name="connectionString" value="192.168.1.141:2182"/>
	    </bean>
	
	</beans>



**restful.xml**

	<?xml version="1.0" encoding="UTF-8"?>
	<beans xmlns="http://www.springframework.org/schema/beans"
	       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	       xsi:schemaLocation="http://www.springframework.org/schema/beans 
						http://www.springframework.org/schema/beans/spring-beans.xsd">
	
	    <bean id="restfulBean" class="com.push6.restful.registry.RestfulBean" init-method="start">
	        <property name="zkClient" ref="zkClient"/>
	        <property name="root" value="restfulCommand"/>
	        <property name="businessMap">
	            <map>
	                <entry key="demoCommand" value="http://{server}:8080/demoPrj/rest/demo"/>
	                <entry key="demoCommand1" value="http://{server}:8080/demoPrj/rest/demo1"/>
	                <entry key="demoCommand2" value="http://{server}:8080/demoPrj/rest/demo2"/>
	            </map>
	        </property>
	    </bean>
	    <!-- 引入zookeeper client bean -->
	    <import resource="zookeeper.xml"/>
	
	</beans>

> 注意： 只需要引入**restful.xml**文件到上下文即可。因为其已引入了zookeeper.xml



#### 说明：

* **connectionString** zookeeper服务器连接串
* **root** 值为 "**restfulCommand**" 不可修改，为终端代理标识前缀
* **businessMap** 应用提供的command和对应的rest服务地址，{server} 为固定格式不需要修改


