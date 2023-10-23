package org.bigbluebutton.event.messaging;

import org.bigbluebutton.bbb.event.BbbEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.SubmissionPublisher;

@Service
public class BbbEventListener implements MessageListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(BbbEventListener.class);

    private SubmissionPublisher<BbbEvent> eventPublisher;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        BbbEvent bbbEvent = BbbEvent.newBuilder()
                .setChannel(new String(message.getChannel(), StandardCharsets.UTF_8))
                .setMessage(new String(message.getBody(), StandardCharsets.UTF_8))
                .build();
        eventPublisher.submit(bbbEvent);
    }


}
