package org.bigbluebutton.event.service;

import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.bigbluebutton.bbb.event.*;
import org.bigbluebutton.event.dao.ChannelStore;
import org.bigbluebutton.event.dao.EventStore;
import org.bigbluebutton.event.entity.Event;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.SubmissionPublisher;

@GrpcService
public class EventService extends EventServiceGrpc.EventServiceImplBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventService.class);

    private final SubmissionPublisher<BbbEvent> eventPublisher;
    private final StreamMessageListenerContainer<String, ObjectRecord<String, BbbEvent>> streamContainer;
    private final ChannelStore channelStore;
    private final EventStore eventStore;

    @Value("${bbb.redis.stream.key}")
    private String streamKey;

    public EventService(
            SubmissionPublisher<BbbEvent> eventPublisher,
            StreamMessageListenerContainer<String, ObjectRecord<String, BbbEvent>> streamContainer,
            ChannelStore channelStore,
            EventStore eventStore
    ) {
        this.eventPublisher = eventPublisher;
        this.streamContainer = streamContainer;
        this.channelStore = channelStore;
        this.eventStore = eventStore;
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

    @Override
    public void replay(ReplayEvents request, StreamObserver<StoredEvent> responseObserver) {
        List<Event> events = eventStore.findEventsBetweenTimestamps(request.getStartTime(), request.getEndTime());

        for (Event event: events) {
            StoredEvent storedEvent = StoredEvent.newBuilder()
                    .setTimestamp(event.timestampToMills())
                    .setChannel(event.getChannel().getName())
                    .setType(event.getType())
                    .setData(event.getData())
                    .build();
            responseObserver.onNext(storedEvent);
        }

        responseObserver.onCompleted();
    }

    @Override
    public void searchEvents(EventSearch request, StreamObserver<StoredEvent> responseObserver) {
        String[] channels = request.getChannelList().toArray(String[]::new);
        String[] types = request.getTypeList().toArray(String[]::new);

        List<Event> events = new ArrayList<>();
        if (channels.length > 0 && types.length > 0) {
            events = eventStore.findEventsByTypeAndChannel(types, channels);
        } else if (types.length == 0 && channels.length > 0) {
            events = eventStore.findEventsByChannel(channels);
        } else if (types.length > 0) {
            events = eventStore.findEventsByType(types);
        }

        for (Event event: events) {
            StoredEvent storedEvent = StoredEvent.newBuilder()
                    .setTimestamp(event.timestampToMills())
                    .setChannel(event.getChannel().getName())
                    .setType(event.getType())
                    .setData(event.getData())
                    .build();
            responseObserver.onNext(storedEvent);
        }

        responseObserver.onCompleted();
    }
}
