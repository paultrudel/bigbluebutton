package org.bigbluebutton.resource.streaming;

import io.grpc.stub.StreamObserver;
import org.bigbluebutton.bbb.resource.ResourceRequest;
import org.bigbluebutton.bbb.resource.ResourceResponse;
import org.bigbluebutton.resource.service.ResourceMonitor;

public class ResourceStreamingRequest implements StreamObserver<ResourceRequest> {

    private final StreamObserver<ResourceResponse> resourceResponseStreamObserver;
    private final ResourceMonitor resourceMonitor;

    public ResourceStreamingRequest(StreamObserver<ResourceResponse> resourceResponseStreamObserver, ResourceMonitor resourceMonitor) {
        this.resourceResponseStreamObserver = resourceResponseStreamObserver;
        this.resourceMonitor = resourceMonitor;
    }

    @Override
    public void onNext(ResourceRequest resourceRequest) {
        ResourceResponse resourceResponse = ResourceResponse.newBuilder()
                .setCpuLoad(resourceMonitor.getSystemLoad(resourceRequest.getTimestamp()))
                .setTimestamp(resourceMonitor.generateCpuTicks())
                .setMemoryUsage(resourceMonitor.getMemoryUsage())
                .build();
        this.resourceResponseStreamObserver.onNext(resourceResponse);
    }

    @Override
    public void onError(Throwable throwable) {

    }

    @Override
    public void onCompleted() {

    }
}
