# Introduction
To use Artezio Guaranteed Delivery library you need to complete next steps:

- Step 1. Set Maven dependencies

Configure your project for using Spring Boot JPA and Apache Camel.

- Step 2. Set Spring Boot scanners

Configure your Spring application to use Artezio Guaranteed Delivery library objects.

- Step 3. Make recovery callback route

Develop some callback Apache Camel routes.

- Step 4. Send an recovery request

Create an RecoveryRequest object and send it using Apache Camel producer.

You find list of RecoveryRequest properties, usable links and the library Spring Boot properties at the bottom of this page.

# Step 1. Set Maven dependencies

```xml
...
    <dependencyManagement>
        <dependencies>   
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-dependencies</artifactId>
                <version>2.1.0.RELEASE</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
    <dependencies>   
        <dependency>
            <groupId>org.apache.camel</groupId>
            <artifactId>camel-spring-boot-starter</artifactId>
            <version>2.23.1</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>com.artezio</groupId>
            <artifactId>recovery-server</artifactId>
            <version>1.0.0</version>
        </dependency>
...
```

# Step 2. Set Spring Boot scanners

```java
@Configuration
@ComponentScan(basePackages = {"com.artezio.recovery.server"})
@EntityScan(basePackages = {"com.artezio.recovery.server"})
@EnableJpaRepositories(basePackages = {"com.artezio.recovery.server"})
@SpringBootApplication
public class ExampleServerApplication {
    public static void main(String[] args) {
        new SpringApplication(ExampleServerApplication.class).run(args);
    }
}
```

**Notice.** You should append your own packages for basePackages scanners properties.

# Step 3. Make recovery callback route

```java
...
@Component
public class BillingAdaptorRoute extends SpringRouteBuilder {
    @Override
    public void configure() throws Exception {
        from("direct://callback").to("direct://doSomthing");
    }
}

```

# Step 4. Send an recovery request
```java
...
@Produce(uri = RecoveryRoutes.INCOME_URL)
private ProducerTemplate producer;

@Transactional(propagation = Propagation.REQUIRED)
public void startRequest(PaymentRequest payment) {
    RecoveryRequest request = new RecoveryRequest();
    request.setCallbackUri("direct://callback");
    // set some request properties
    producer.sendBody(request);
}
...
```

# Recovery request properties
 *  callbackUri (string) Callback route URI.
 *  externalId (string) External message ID.
 *  locker (string) External code to lock new data storing if it exists.
 *  message (string) Short specific recovery data.
 *  pause (string) Recovery processing pause rule.
 *  processingFrom (date) Date to start redelivery processing.
 *  processingLimit (number) Limit of redelivery tries.
 *  processingTo (date) Date to interrupt redelivery processing.
 *  queue (string) Code to specify redelivery queue.
 *  queueParent (string) Code to specify parent redelivery queue.

# Read more

[Configure Spring Boot](https://docs.spring.io/spring-boot/docs/current/reference/html/common-application-properties.html)

[Configure Apache Camel + Spring Boot](https://camel.apache.org/spring-boot.html)

# The library Spring Boot properties

- com.artezio.recovery.timer.period

Schedule timer period property. Apache Camel time unit (see Apache Camel Timer component). Default: 50

- com.artezio.recovery.seda.consumers

Amount of SEDA concurrent consumers property. Default: 10

- com.artezio.recovery.timer.period

Schedule cleaning period property. Apache Camel time unit.(see Apache Camel Timer component). Default: 15m

- com.artezio.recovery.schedule.enabled

Property of flag to schedule processing. Boolean. Default: true

- com.artezio.recovery.delivery.expired

Property of flag to allow processing of expired orders. Boolean. Default: true

- com.artezio.recovery.cleaning.success

Property of flag to allow successfully processed orders cleaning. Boolean. Default: true

- com.artezio.recovery.cleaning.error

Property of flag to allow failed orders cleaning. Boolean. Default: true

- com.artezio.recovery.resuming.timeout.minutes

Property of resuming timeout in minutes. Number. Default: 10


