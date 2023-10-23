package org.bigbluebutton.event.service;

import io.grpc.stub.StreamObserver;
import org.bigbluebutton.bbb.event.BbbEvent;
import org.bigbluebutton.bbb.event.BbbEventSubscription;
import org.bigbluebutton.bbb.event.EventServiceGrpc;

public class EventService extends EventServiceGrpc.EventServiceImplBase {

    @Override
    public void bbbEventSubscribe(BbbEventSubscription request, StreamObserver<BbbEvent> responseObserver) {
        super.bbbEventSubscribe(request, responseObserver);
    }
}
