Spring Integration Tech Tip
===========================

Spring Integration 4.1 was just released and it includes a *lot* of great new features! One of my favorites? Smart integration with the Spring 4 WebSocket support. Now you can compose a integration flow whose final destination is a WebSocket client. There is also support for acting as the client to a WebSocket service.

In order to compile it, you will need Java 8 (we make heavy use of lambas here) and the following Maven dependencies:

-	**groupId**:`org.springframework.integration`, **artifactId**:`spring-integration-java-dsl`, **version**: `1.0.0.RC1`.
-	**groupId**:`org.springframework.integration`, **artifactId**:`spring-integration-websocket`, **version**: `4.1.0.RELEASE`.
-	**groupId**:`org.springframework.boot`, **artifactId**:`spring-boot-starter-websocket`, **version**: `1.2.0.RC1`.

In order to resolve those dependencies you will need the [`snapshot`](http://repo.spring.io/snapshot) and [`milestone`](http://repo.spring.io/milestone) Maven repositories.

All clients listening on `/names` will receive whatever message is sent into the `requestChannel` channel. A Spring 4 `MessageChannel` is a named conduit - more or less analogous to a `java.util.Queue<T>`. This example uses [the Spring Integration Java configuration DSL](https://spring.io/blog/2014/10/31/spring-integration-java-dsl-1-0-rc1-released) on top of the new [Spring Integration 4.1 web socket support](https://spring.io/blog/2014/11/11/spring-integration-and-amqp-releases-available). Here's the example:

```
package demo ;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.support.Function;
import org.springframework.integration.websocket.ServerWebSocketContainer;
import org.springframework.integration.websocket.outbound.WebSocketOutboundMessageHandler;
import org.springframework.messaging.*;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * @author Artem Bilan
 * @author Josh Long
 */
@Configuration
@ComponentScan
@EnableAutoConfiguration
@RestController
public class Application {

    public static void main(String args[]) throws Throwable {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    ServerWebSocketContainer serverWebSocketContainer() {
        return new ServerWebSocketContainer("/names").withSockJs();
    }

    @Bean
    MessageHandler webSocketOutboundAdapter() {
        return new WebSocketOutboundMessageHandler(serverWebSocketContainer());
    }

    @Bean(name = "webSocketFlow.input")
    MessageChannel requestChannel() {
        return new DirectChannel();
    }

    @Bean
    IntegrationFlow webSocketFlow() {
        return f -> {
            Function<Message , Object> splitter = m -> serverWebSocketContainer()
                    .getSessions()
                    .keySet()
                    .stream()
                    .map(s -> MessageBuilder.fromMessage(m)
                            .setHeader(SimpMessageHeaderAccessor.SESSION_ID_HEADER, s)
                            .build())
                    .collect(Collectors.toList());
            f.split( Message.class, splitter)
                    .channel(c -> c.executor(Executors.newCachedThreadPool()))
                    .handle(webSocketOutboundAdapter());
        };
    }

    @RequestMapping("/hi/{name}")
    public void send(@PathVariable String name) {
        requestChannel().send(MessageBuilder.withPayload(name).build());
    }
}

```

The `IntegrationFlow` is simple. For each message that comes in, copy it and address it to each listening `WebSocket` session by adding a header having the `SimpMessageHeaderAccessor.SESSION_ID_HEADER`, then send it the outbound `webSocketOutboundAdapter` which will deliver it to each listening client. To see it work, open http://localhost:8080/ in one browser window, and then http://localhost:8080/hi/Spring in another. There is a simple client [demonstrated in this techtip's code repository](https://github.com/joshlong/techtips/tree/master/examples/spring-integration-4.1-websockets-example).

There is great documentation on how to use the web socket [components in Spring Integration 4.1 documentation](http://docs.spring.io/spring-integration/docs/latest-ga/reference/html/web-sockets.html). There's a more inspiring example in [the Spring Integration samples directory](https://github.com/spring-projects/spring-integration-samples/tree/master/basic/web-sockets), too.
