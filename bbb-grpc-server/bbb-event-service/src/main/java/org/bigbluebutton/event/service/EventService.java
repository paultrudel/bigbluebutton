package org.bigbluebutton.event.service;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.bigbluebutton.bbb.event.BbbEvent;
import org.bigbluebutton.bbb.event.BbbEventSubscription;
import org.bigbluebutton.bbb.event.EventServiceGrpc;
import org.bigbluebutton.event.messaging.BbbEventSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.SubmissionPublisher;
import java.util.stream.Collectors;

@GrpcService
public class EventService extends EventServiceGrpc.EventServiceImplBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventService.class);

    private final SubmissionPublisher<BbbEvent> eventPublisher;

    public EventService(SubmissionPublisher<BbbEvent> eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void bbbEventSubscribe(BbbEventSubscription request, StreamObserver<BbbEvent> responseObserver) {
        LOGGER.info("Adding new subscriber for channels {}", request.getChannelList());
        BbbEventSubscriber subscriber = new BbbEventSubscriber(responseObserver, new HashSet<>(request.getChannelList()));
        eventPublisher.subscribe(subscriber);
    }
}
