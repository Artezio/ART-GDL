Recovery JMS Adapter Guide:


Recovery JMS Adaptor Adaptor can be used in one of these 2 modes:

1) As a standalone Spring Boot application. 

It can be launched with the following command:

	java -jar recovery-jms-adaptor-1.0.0-exec.jar --spring.activemq.broker-url=tcp://IP:PORT

Please note that only the jar that ends with exec.jar can be launched with java -jar command. The other jar file is meant to be launched as a Spring Boot app using spring-boot maven plugin. (mvn spring-boot:run)

2) As a library in the client application (embedded ActiveMQ will be used)

	Add the following entries in your POM file (or in the equivalent dependencies file if you are not using Maven):

	<dependencies>   
	        <dependency>
	            <groupId>com.artezio</groupId>
            	    <artifactId>recovery-jms-adaptor</artifactId>
                    <version>1.0.0</version>
	        </dependency>
        	<dependency>
            	    <groupId>com.artezio</groupId>
            	    <artifactId>recovery-model</artifactId>
            	    <version>1.0.0</version>
        	</dependency>

	Add the following in your application.properties file:
	
	spring.activemq.broker-url=vm://embedded-broker?broker.persistent=false&broker.useShutdownHook=false
	spring.activemq.user=admin
	spring.activemq.password=admin
	spring.activemq.packages.trust-all=false
	spring.activemq.packages.trusted=com.artezio.recovery	

Please follow the README instructions from recovery-server to learn how to send messages via Camel routes. 

	



