package org.bigbluebutton.event.config;

import org.bigbluebutton.bbb.event.BbbEvent;
import org.bigbluebutton.event.messaging.BbbEventListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;

import java.time.Duration;

@Configuration
@ComponentScan("org.bigbluebutton.event")
@EnableRedisRepositories(basePackages = "org.bigbluebutton.event")
@PropertySource("classpath:application.yaml")
public class RedisConfig {

    @Bean
    RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory, MessageListenerAdapter listenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(listenerAdapter, new PatternTopic("*-channel"));
        return container;
    }

    @Bean
    StreamMessageListenerContainer<String, ObjectRecord<String, BbbEvent>> streamContainer(RedisConnectionFactory connectionFactory) {
        StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, ObjectRecord<String, BbbEvent>> containerOptions =
                StreamMessageListenerContainer.StreamMessageListenerContainerOptions.builder()
                        .pollTimeout(Duration.ofMillis(100))
                        .targetType(BbbEvent.class)
                        .build();
        return StreamMessageListenerContainer.create(connectionFactory, containerOptions);
    }

    @Bean
    MessageListenerAdapter listenerAdapter(BbbEventListener bbbEventListener) {
        return new MessageListenerAdapter(bbbEventListener);
    }

    @Bean
    StringRedisTemplate template(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }
}
