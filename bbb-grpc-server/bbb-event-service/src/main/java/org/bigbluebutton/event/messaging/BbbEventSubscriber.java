package org.bigbluebutton.event.messaging;

import io.grpc.stub.StreamObserver;
import org.bigbluebutton.bbb.event.BbbEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Flow;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;

public class BbbEventSubscriber implements Subscriber<BbbEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BbbEventSubscriber.class);

    private Subscription subscription;
    private final StreamObserver<BbbEvent> observer;
    private final Set<String> channels;

    private long timeSinceLastEvent;

    public BbbEventSubscriber(StreamObserver<BbbEvent> observer, Set<String> channels) {
        this.observer = observer;
        this.channels = channels;
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        this.subscription = subscription;
        this.subscription.request(1);
    }

    @Override
    public void onNext(BbbEvent item) {
        if(channels.contains(item.getChannel()) || channels.contains("*")) {
            LOGGER.info("Passing message from {} to client", item.getChannel());
            observer.onNext(item);
            this.subscription.request(1);
        }
    }

    @Override
    public void onError(Throwable throwable) {

    }

    @Override
    public void onComplete() {

    }
}
