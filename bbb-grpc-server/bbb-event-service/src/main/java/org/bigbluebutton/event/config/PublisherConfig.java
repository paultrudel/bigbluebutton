package org.bigbluebutton.event.config;

import org.bigbluebutton.bbb.event.BbbEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.SubmissionPublisher;

@Configuration
@ComponentScan("org.bigbluebutton.event")
public class PublisherConfig {

    @Bean
    SubmissionPublisher<BbbEvent> eventPublisher() {
        return new SubmissionPublisher<>();
    }
}
