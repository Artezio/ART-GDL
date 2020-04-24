#Recovery Kafka Adapter Guide:

Recovery Kafka Adaptor can be used in one of these 2 modes:

- Step 1. As a standalone Spring Boot application. 

It can be launched with the following command:

	java -jar recovery-kafka-adaptor-1.0.0-exec.jar

**Notice.** Only the jar that ends with exec.jar can be launched with java -jar command. The other jar file is meant to be launched as a Spring Boot app using spring-boot maven plugin. (mvn spring-boot:run)

- Step 2. As a library in the client application

	Add the following entries in your POM file (or in the equivalent dependencies file if you are not using Maven):
	
```xml
	<dependencies>   
	        <dependency>
	            <groupId>com.artezio</groupId>
            	    <artifactId>recovery-kafka-adaptor</artifactId>
                    <version>1.0.0</version>
	        </dependency>
        </dependencies> 
```

   Try to use the latest version of the application

   Add the following in your application.properties file:
   
   * kafka.input.queue=kafka:{your input topic} (default:kafka:recovery)
   * kafka.input.brokers={your list of brokers} (default: localhost:9092)
   
   * kafka.output.queue=kafka:{your output topic} (default:kafka:callback_recovery)
   * kafka.output.brokers={your list of brokers} (default: localhost:9092)

Please follow the README instructions from recovery-server to learn how to send messages via Camel routes.