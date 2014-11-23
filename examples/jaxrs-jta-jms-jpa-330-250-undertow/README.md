A Java EE-centric Spring Boot 1.2 application
=============================================

In this blog, I want to look at - and demonstrate - some of the many new features in [Spring Boot 1.2](http://spring.io/projects/spring-boot) that make the lives of those coming from Java EE easier. Note that none of this support is *new* to Spring at large, per-se, but Spring Boot lacked appropriate auto-configuration before.

Here's an example program.

```java

```

JAX-RS with Jersey
------------------

The example demonstrates JAX-RS integration (in this case, using [Jersey 2.x](https://jersey.java.net/)) in the `demo.Application.GreetingEndpoint`. Note how convenient it is to get it all working! The only thing to be weary of is that you need to specify a `ResourceConfig` subclass to let Jersey know which components to register.

Global Transactions with JTA
----------------------------

It demonstrates global transactions (a.k.a., *XA*) with the new auto-configured JTA support. To do this, we've used the Atomikos standalone JTA provider. We could have as easily used Bitronix, as well; both are auto-configured if you bring the appropriate starter along. In this example, in the `GreetingService`, JMS and JPA work is done as part of a global transaction. We demonstrate this by creating 3 transactions and simulating a rollback on the third one. You should see printed to the console that there are two records that come back from the JDBC `javax.sql.DataSource` data source and two records that are received from the embedded JMS `javax.jms.Destination` destination.

The Undertow embedded web-server
--------------------------------

This example also uses the Wildfly (from RedHat) application server's *awesome* [Undertow embedded HTTP server](http://undertow.io/) instead of (the default) Apache Tomcat. It's as easy to use Undertow as it is to use Jetty or Tomcat - just exclude `org.springframework.boot:spring-boot-starter-tomcat` and add `org.springframework.boot:spring-boot-starter-undertow`! This contribution originated as a third-party PR - thanks Ivan Sopov! It's awesome.

Odds and Ends
-------------

Just for consistency, the example also uses JSR 330. JSR 330 describes a set of annotations that you can use in proprietary application servers like WebLogic as well as in a portable manner in dependency injection containers like Google Guice or Spring. I also use a JSR 250 annotation (defined as part of Java EE 5) to demonstrate lifecycle hooks.

This example relies on a Spring Boot auto-configured embedded, in-memory [H2](http://www.h2database.com/html/main.html) `javax.sql.DataSource` and - a Spring Boot auto-configured embedded, in-memory [HornetQ](http://hornetq.jboss.org) `javax.jms.ConnectionFactory`. If you wanted to connect to non-embedded instances, it's straightforward to define beans that will be picked up instead.

Deployment
----------

Though I'm using a lot of fairly familiar Java EE APIs, this is still just typical Spring Boot, so by default you can run this application using `java -jar ee.jar` or easily deploy it to process-centric [platforms-as-a-service](http://en.wikipedia.org/wiki/Platform_as_a_service) offerings like - Heroku or [Cloud Foundry](http://cloudfoundry.org). If you want to deploy it to a standalone application server like (like Apache Tomcat, or Websphere, or anything in between), it's straightforward to convert the build into a `.war` and deploy it accordingly to any Servlet 3 container.

If you deploy the application to a more classic application server, Spring Boot can take advantage of the AS's facilities, instead. For example, it's dead-simple to consume a JNDI-bound [JMS `ConnectionFactory`](http://docs.spring.io/spring-boot/docs/1.2.0.BUILD-SNAPSHOT/reference/htmlsingle/#boot-features-jms-jndi), [JDBC `DataSource`](http://docs.spring.io/spring-boot/docs/1.2.0.BUILD-SNAPSHOT/reference/htmlsingle/#boot-features-connecting-to-a-jndi-datasource) or [JTA `UserTransaction`](http://docs.spring.io/spring-boot/docs/1.2.0.BUILD-SNAPSHOT/reference/htmlsingle/#_using_a_java_ee_managed_transaction_manager).

Spring Boot 1.2: Choice is Power
--------------------------------

I, personally, would question a lot of these APIs. Do you *really* need distributed, multi-resource transactions? In today's distributed world, consider global transaction managers an architecture smell. Do you *really* want to stay on JAX-RS when Spring offers a much richer, integrated Spring MVC-based stack complete with MVC, REST, HATEOAS, OAuth and websockets support? It might well be that you do, and - as always - the choice is yours. That's why this release is so cool! More power, more choice.
