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
        return new ServerWebSocketContainer("/messages").withSockJs();
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