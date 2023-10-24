package org.bigbluebutton.event.messaging;

import org.bigbluebutton.bbb.event.BbbEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.SubmissionPublisher;

@Service
public class BbbEventListener implements MessageListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(BbbEventListener.class);

    private final SubmissionPublisher<BbbEvent> eventPublisher;
    private final StringRedisTemplate template;

    @Value("${bbb.redis.stream.key}")
    private String streamKey;

    public BbbEventListener(SubmissionPublisher<BbbEvent> eventPublisher, StringRedisTemplate template) {
        this.eventPublisher = eventPublisher;
        this.template = template;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        LOGGER.info("{}", message);
        String channel = new String(message.getChannel(), StandardCharsets.UTF_8);
        String body = new String(message.getBody(), StandardCharsets.UTF_8);

        ObjectRecord<String, BbbEvent> record = StreamRecords.newRecord()
                        .ofObject(BbbEvent.newBuilder()
                                .setChannel(channel)
                                .setMessage(body)
                                .setMessageId("")
                                .build())
                        .withStreamKey(streamKey);
        RecordId recordId = this.template.opsForStream().add(record);

        BbbEvent bbbEvent;

        if(Objects.isNull(recordId)) {
            LOGGER.error("Could not add message to stream");
            bbbEvent = BbbEvent.newBuilder()
                    .setChannel(channel)
                    .setMessage(body)
                    .build();
        } else {
         LOGGER.info("Added new BbbEvent with ID {}", recordId);
         bbbEvent = BbbEvent.newBuilder()
                 .setMessageId(recordId.getValue())
                 .setChannel(channel)
                 .setMessage(body)
                 .build();
        }

        eventPublisher.submit(bbbEvent);
    }


}
