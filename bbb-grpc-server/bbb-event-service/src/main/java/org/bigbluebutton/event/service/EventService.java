package org.bigbluebutton.event.service;

import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.bigbluebutton.bbb.event.BbbEvent;
import org.bigbluebutton.bbb.event.BbbEventReplay;
import org.bigbluebutton.bbb.event.BbbEventSubscription;
import org.bigbluebutton.bbb.event.EventServiceGrpc;
import org.bigbluebutton.event.messaging.BbbEventSubscriber;
import org.bigbluebutton.event.messaging.BbbStreamListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.Subscription;

import java.util.HashSet;
import java.util.concurrent.SubmissionPublisher;

@GrpcService
public class EventService extends EventServiceGrpc.EventServiceImplBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventService.class);

    private final SubmissionPublisher<BbbEvent> eventPublisher;
    private final StreamMessageListenerContainer<String, ObjectRecord<String, BbbEvent>> streamContainer;

    @Value("${bbb.redis.stream.key}")
    private String streamKey;

    public EventService(
            SubmissionPublisher<BbbEvent> eventPublisher,
            StreamMessageListenerContainer<String, ObjectRecord<String, BbbEvent>> streamContainer
    ) {
        this.eventPublisher = eventPublisher;
        this.streamContainer = streamContainer;
    }

    @Override
    public void bbbEventSubscribe(BbbEventSubscription request, StreamObserver<BbbEvent> responseObserver) {
        LOGGER.info("Adding new subscriber for channels {}", request.getChannelList());
        BbbEventSubscriber subscriber = new BbbEventSubscriber(responseObserver, new HashSet<>(request.getChannelList()));
        eventPublisher.subscribe(subscriber);
    }

    @Override
    public void replayEvents(BbbEventReplay request, StreamObserver<BbbEvent> responseObserver) {
        ServerCallStreamObserver<BbbEvent> observer = (ServerCallStreamObserver<BbbEvent>) responseObserver;
        StreamOffset<String> offset = StreamOffset.create(streamKey, ReadOffset.from(RecordId.of(request.getMessageId())));
        Subscription subscription = streamContainer.receive(offset, new BbbStreamListener(responseObserver));
        observer.setOnCancelHandler(subscription::cancel);
        streamContainer.start();
    }
}
