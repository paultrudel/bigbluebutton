package org.bigbluebutton.event.messaging;

import io.grpc.stub.StreamObserver;
import org.bigbluebutton.bbb.event.BbbEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.stream.StreamListener;

public class BbbStreamListener implements StreamListener<String, ObjectRecord<String, BbbEvent>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BbbStreamListener.class);

    private final StreamObserver<BbbEvent> responseObserver;

    public BbbStreamListener(StreamObserver<BbbEvent> responseObserver) {
        this.responseObserver = responseObserver;
    }

    @Override
    public void onMessage(ObjectRecord<String, BbbEvent> message) {
        LOGGER.info("Received message from stream with ID {}", message.getId());
        BbbEvent bbbEvent = BbbEvent.newBuilder()
                .setMessageId(message.getId().getValue())
                .setChannel(message.getValue().getChannel())
                .setMessage(message.getValue().getMessage())
                .build();
        responseObserver.onNext(bbbEvent);
    }
}
