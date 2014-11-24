# "Bootiful" Java EE Support in Spring Boot 1.2

In this blog, I want to look at - and demonstrate - some of the _many_ new features in [Spring Boot 1.2](http://spring.io/projects/spring-boot) that make the lives of those coming from, or otherwise building on, Java EE easier.

It's worth mentioning that a lot of this support has been possible with Spring before, of course, but now with Spring Boot 1.2, it's just so darned easy!

First, here's an example program with notes after.

```java

package demo;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.jms.JMSException;
import javax.persistence.*;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.Serializable;
import java.util.Collection;
import java.util.logging.Logger;

@SpringBootApplication
public class Application {

    @Named
    public static class JerseyConfig extends ResourceConfig {

        public JerseyConfig() {
            this.register(GreetingEndpoint.class);
            this.register(JacksonFeature.class);
        }
    }

    @Named
    @Transactional
    public static class GreetingService {

        @Inject
        private JmsTemplate jmsTemplate;

        @PersistenceContext
        private EntityManager entityManager;

        public void createGreeting(String name, boolean fail) {
            Greeting greeting = new Greeting(name);
            this.entityManager.persist(greeting);
            this.jmsTemplate.convertAndSend("greetings", greeting);
            if (fail) {
                throw new RuntimeException("simulated error");
            }
        }

        public void createGreeting(String name) {
            this.createGreeting(name, false);
        }

        public Collection<Greeting> findAll() {
            return this.entityManager
                    .createQuery("select g from " + Greeting.class.getName() + " g", Greeting.class)
                    .getResultList();
        }

        public Greeting find(Long id) {
            return this.entityManager.find(Greeting.class, id);
        }
    }

    @Named
    @Path("/hello")
    @Produces({MediaType.APPLICATION_JSON})
    public static class GreetingEndpoint {

        @Inject
        private GreetingService greetingService;

        @POST
        public void post(@QueryParam("name") String name) {
            this.greetingService.createGreeting(name);
        }

        @GET
        @Path("/{id}")
        public Greeting get(@PathParam("id") Long id) {
            return this.greetingService.find(id);
        }
    }

    @Entity
    public static class Greeting implements Serializable {

        @Id
        @GeneratedValue
        private Long id;

        @Override
        public String toString() {
            return "Greeting{" +
                    "id=" + id +
                    ", message='" + message + '\'' +
                    '}';
        }

        private String message;

        public String getMessage() {
            return message;
        }

        public Greeting(String name) {
            this.message = "Hi, " + name + "!";
        }

        Greeting() {
        }
    }

    @Named
    public static class GreetingServiceClient {

        @Inject
        private GreetingService greetingService;

        @PostConstruct
        public void afterPropertiesSet() throws Exception {
            greetingService.createGreeting("Phil");
            greetingService.createGreeting("Dave");
            try {
                greetingService.createGreeting("Josh", true);
            } catch (RuntimeException re) {
                Logger.getLogger(Application.class.getName()).info("caught exception...");
            }
            greetingService.findAll().forEach(System.out::println);
        }
    }

    @Named
    public static class GreetingMessageProcessor {

        @JmsListener(destination = "greetings")
        public void processGreeting(Greeting greeting) throws JMSException {
            System.out.println("received message: " + greeting);
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

```

The complete  code listings, including the very sparce [`application.properties`](https://github.com/joshlong/techtips/blob/master/examples/jaxrs-jta-jms-jpa-330-250-undertow/src/main/resources/application.properties) and the [Maven build](https://github.com/joshlong/techtips/blob/master/examples/jaxrs-jta-jms-jpa-330-250-undertow/pom.xml), are available online. 

JAX-RS with Jersey
------------------

The example [demonstrates Boot's new JAX-RS auto-configuration](http://docs.spring.io/spring-boot/docs/1.2.0.BUILD-SNAPSHOT/reference/htmlsingle/#boot-features-jersey) (in this case, using [Jersey 2.x](https://jersey.java.net/)) in the `GreetingEndpoint`. Note how convenient it is to get it all working! The only thing to be weary of is that you need to specify a `ResourceConfig` subclass to let Jersey know which components to register.

Global Transactions with JTA
----------------------------

It demonstrates global transactions with the [new auto-configured JTA support](http://docs.spring.io/spring-boot/docs/1.2.0.BUILD-SNAPSHOT/reference/htmlsingle/#boot-features-jta). JTA is a Java API for the X/Open XA protocol which lets multiple, compliant, transaction resources (like a message queue and a database) participate in a single transaction. To do this, we've used the [Atomikos](http://www.atomikos.com/) standalone JTA provider. We could have as easily used Bitronix, as well; both are auto-configured if you bring the appropriate starter along. In this example, in the `GreetingService`, JMS and JPA work is done as part of a global transaction. We demonstrate this by creating 3 transactions and simulating a rollback on the third one. You should see printed to the console that there are two records that come back from the JDBC `javax.sql.DataSource` data source and two records that are received from the embedded JMS `javax.jms.Destination` destination.

The Undertow embedded web-server
--------------------------------

This example also uses the Wildfly (from RedHat) application server's *awesome* [Undertow embedded HTTP server](http://undertow.io/) instead of (the default) Apache Tomcat. It's as easy to use Undertow as it is to use Jetty or Tomcat - just exclude `org.springframework.boot:spring-boot-starter-tomcat` and add `org.springframework.boot:spring-boot-starter-undertow`! This contribution originated as a third-party PR - thanks Ivan Sopov! It's awesome.

Odds and Ends
-------------

Just for consistency, the example also uses JSR 330. JSR 330 describes a set of annotations that you can use in proprietary application servers like WebLogic as well as in a portable manner in dependency injection containers like Google Guice or Spring. I also use a JSR 250 annotation (defined as part of Java EE 5) to demonstrate lifecycle hooks.

This example relies on a Spring Boot auto-configured embedded, in-memory [H2](http://www.h2database.com/html/main.html) `javax.sql.DataSource` and - a Spring Boot auto-configured embedded, in-memory [HornetQ](http://hornetq.jboss.org) `javax.jms.ConnectionFactory`. If you wanted to connect to non-embedded instances, it's straightforward to define beans that will be picked up instead.

This example *also* uses the new `@SpringBootApplication` annotation which combines `@Configuration`, `@EnableAutoConfiguration` and `@ComponentScan`. Nice!

Deployment
----------

Though this example uses a lot of fairly familiar Java EE APIs, this is still just typical Spring Boot, so by default you can run this application using `java -jar ee.jar` or easily deploy it to process-centric [platforms-as-a-service](http://en.wikipedia.org/wiki/Platform_as_a_service) offerings like - Heroku or [Cloud Foundry](http://cloudfoundry.org). If you want to deploy it to a standalone application server like (like Apache Tomcat, or Websphere, or anything in between), it's straightforward to convert the build into a `.war` and deploy it accordingly to any Servlet 3 container.

If you deploy the application to a more classic application server, Spring Boot can take advantage of the AS's facilities, instead. For example, it's dead-simple to consume a JNDI-bound [JMS `ConnectionFactory`](http://docs.spring.io/spring-boot/docs/1.2.0.BUILD-SNAPSHOT/reference/htmlsingle/#boot-features-jms-jndi), [JDBC `DataSource`](http://docs.spring.io/spring-boot/docs/1.2.0.BUILD-SNAPSHOT/reference/htmlsingle/#boot-features-connecting-to-a-jndi-datasource) or [JTA `UserTransaction`](http://docs.spring.io/spring-boot/docs/1.2.0.BUILD-SNAPSHOT/reference/htmlsingle/#_using_a_java_ee_managed_transaction_manager).

Spring Boot 1.2: Choice *and* Power
-----------------------------------

I, personally, would question a lot of these APIs. Do you *really* need distributed, multi-resource transactions? In today's [distributed world, consider global transaction managers are an architecture smell](http://www.eaipatterns.com/ramblings/18_starbucks.html). Do you *really* need to use JAX-RS when Spring offers a richer, integrated Spring MVC-based stack complete with MVC, REST, HATEOAS, OAuth and websockets support? JPA's a nice API for talking to a SQL-based `javax.sql.DataSource`, but Spring Data repositories (which include support for JPA, of course, but _also_ for Cassandra, MongoDB, Redis, CouchBase, and an increasingly long list of alternative technologies) reduce much of the boilerplate to a simple interface definition for the common cases. So, do you really need all of this? It might well be that you do, and - as always - the choice is yours. That's why this release is so cool! More power, more choice. 

What Else?
----------

A *lot*, actually. There are a *slew* of new features. I couldn't even begin to cover them all here. So I won't try. Check out the [release notes](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-1.2-Release-Notes) for the full scoop! 

Spring Boot 1.2 is fast approaching GA, and now's a very good time to [try the bits, kick the tires](http://start.spring.io), [file issues](https://github.com/spring-projects/spring-boot/issues) and [ask questions](http://stackoverflow.com/questions/tagged/spring-boot)!