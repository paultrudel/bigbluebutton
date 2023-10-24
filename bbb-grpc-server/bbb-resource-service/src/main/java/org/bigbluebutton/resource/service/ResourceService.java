package org.bigbluebutton.resource.service;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.bigbluebutton.bbb.resource.*;
import org.bigbluebutton.resource.streaming.ResourceStreamingRequest;

import java.util.HashMap;
import java.util.Map;

@GrpcService
public class ResourceService extends ResourceServiceGrpc.ResourceServiceImplBase {

    private final ResourceMonitor resourceMonitor;

    public ResourceService(ResourceMonitor resourceMonitor) {
        this.resourceMonitor = resourceMonitor;
    }

    @Override
    public void getResourceReport(ResourceReportRequest request, StreamObserver<ResourceReportResponse> responseObserver) {
        double[] cpuUsage = resourceMonitor.getSystemLoadAverages(3);
        CpuReport cpuReport = CpuReport.newBuilder()
                .setNumPhysicalProcessors(resourceMonitor.getPhysicalProcessorCount())
                .setNumLogicalProcessors(resourceMonitor.getLogicalProcessorCount())
                .setPrevMinLoadAvg(cpuUsage[0])
                .setPrevFiveMinLoadAvg(cpuUsage[1])
                .setPrevFifteenMinLoadAvg(cpuUsage[2])
                .build();

        MemoryReport memoryReport = MemoryReport.newBuilder()
                .setTotalMemory(resourceMonitor.getTotalMemory())
                .setAvailableMemory(resourceMonitor.getAvailableMemory())
                .setPageSize(resourceMonitor.getPageSize())
                .setMemoryUsage(resourceMonitor.getMemoryUsage())
                .build();

        ResourceReportResponse resourceReportResponse = ResourceReportResponse.newBuilder()
                .setCpu(cpuReport)
                .setMemory(memoryReport)
                .build();

        responseObserver.onNext(resourceReportResponse);
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<ResourceRequest> resourceUsage(StreamObserver<ResourceResponse> responseObserver) {
        return new ResourceStreamingRequest(responseObserver, resourceMonitor);
    }
}
