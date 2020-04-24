Recovery JMS Adapter Guide:


Recovery JMS Adaptor Adaptor can be used in one of these 2 modes:

1) As a standalone Spring Boot application. 

It can be launched with the following command:

	java -jar recovery-jms-adaptor-1.0.0-exec.jar --spring.activemq.broker-url=tcp://IP:PORT

Please note that only the jar that ends with exec.jar can be launched with java -jar command. The other jar file is meant to be launched as a Spring Boot app using spring-boot maven plugin. (mvn spring-boot:run)

2) As a library in the client application (embedded ActiveMQ will be used)

	Add the following entries in your POM file (or in the equivalent dependencies file if you are not using Maven):


```xml
	    <dependencies>   
	        <dependency>
	            <groupId>com.artezio</groupId>
            	    <artifactId>recovery-jms-adaptor</artifactId>
                    <version>1.0.0</version>
	        </dependency>
        </dependencies> 
```
    Add the following in your application.properties file:
	
    * spring.activemq.broker-url=vm://embedded-broker?broker.persistent=false&broker.useShutdownHook=falseэ
    * spring.activemq.user=admin
    * spring.activemq.password=admin
    * spring.activemq.packages.trust-all=false
    * spring.activemq.packages.trusted=com.artezio.recovery	
	
	* jms.input.queue=jms:{your input queue} (default:jms:p2p_recovery)
    * jms.output.queue=jms:{your output queue} (default:jms:callback_recovery)
    * camel.component.jms.connection-factory=cachingJmsConnectionFactory
    
    For more flexibility in configuring REST connection you can add other properties, read a detailed description 
    at https://camel.apache.org/camel-spring-boot/latest/jms-starter.html. For example: 
   
    * camel.component.jms.priority=4
    * camel.component.jms.receive-timeout=1000
    * camel.component.jms.request-timeout=20000
   
Please follow the README instructions from recovery-server to learn how to send messages via Camel routes. 

	



