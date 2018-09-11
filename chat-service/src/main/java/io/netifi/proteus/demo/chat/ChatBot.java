package io.netifi.proteus.demo.chat;

import io.netifi.proteus.Proteus;
import io.netty.buffer.ByteBuf;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;

@Component
@Primary
public class ChatBot implements Chat {
  private static final Logger logger = LogManager.getLogger(ChatBot.class);

  private final ChatClient client;

  private final User user = User.newBuilder()
      .setAlias("ChatBot")
      .setAvatar("https://robohash.org/chatbot.png")
      .build();

  @Autowired
  public ChatBot(Proteus proteus) {
    this.client = new ChatClient(proteus.broadcast("chat-demo"));
  }

  @PostConstruct
  public void init() {
    client
        .join(JoinEvent.newBuilder().setUser(user).build())
        .subscribe();
  }

  @Override
  public Mono<Void> chat(ChatEvent event, ByteBuf metadata) {
    logger.info(event.getUser().getAlias() + ": " + event.getMessage());
    if (event.getUser().equals(user)) {
      return Mono.empty();
    }
    return client.chat(ChatEvent.newBuilder()
        .setUser(user)
        .setMessage(event.getMessage() + "?")
        .build());
  }

  @Override
  public Mono<Void> join(JoinEvent event, ByteBuf metadata) {
    logger.info(event.getUser().getAlias() + " joined");
    if (event.getUser().equals(user)) {
      return Mono.empty();
    }
    return client.chat(ChatEvent.newBuilder()
        .setUser(user)
        .setMessage("Hello " + event.getUser().getAlias() + "!")
        .build());
  }
}
