package org.bigbluebutton.resource.service;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.bigbluebutton.bbb.resource.*;

@GrpcService
public class EventService extends ResourceServiceGrpc.ResourceServiceImplBase {

    private final ResourceMonitor resourceMonitor;

    public EventService(ResourceMonitor resourceMonitor) {
        this.resourceMonitor = resourceMonitor;
    }

    @Override
    public void getResourceReport(ResourceReportRequest request, StreamObserver<ResourceReportResponse> responseObserver) {
        double[] cpuUsage = resourceMonitor.getSystemLoadAverages(3);
        CpuReport.newBuilder()
                .setNumPhysicalProcessors(resourceMonitor.getPhysicalProcessorCount())
                .setNumLogicalProcessors(resourceMonitor.getLogicalProcessorCount())
                .setPrevMinLoadAvg(cpuUsage[0])
                .setPrevFiveMinLoadAvg(cpuUsage[1])
                .setPrevFifteenMinLoadAvg(cpuUsage[2])
                .build();

    }

    @Override
    public void resourceSubscribe(ResourceSubscription request, StreamObserver<ResourceSubscriptionResponse> responseObserver) {
        super.resourceSubscribe(request, responseObserver);
    }
}
