package org.bigbluebutton.resource.service;

import org.springframework.stereotype.Service;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ResourceMonitor {

    private static final Map<Long, long[]> CPU_TICKS = new ConcurrentHashMap<>();

    private final CentralProcessor cpu;
    private final GlobalMemory memory;

    public ResourceMonitor(CentralProcessor cpu, GlobalMemory memory) {
        this.cpu = cpu;
        this.memory = memory;
    }

    public int getPhysicalProcessorCount() {
        return cpu.getPhysicalProcessorCount();
    }

    public int getLogicalProcessorCount() {
        return cpu.getLogicalProcessorCount();
    }

    public double[] getSystemLoadAverages(int n) {
        return cpu.getSystemLoadAverage(n);
    }

    public long getTotalMemory() {
        return memory.getTotal();
    }

    public long getAvailableMemory() {
        return memory.getAvailable();
    }

    public long getPageSize() {
        return memory.getPageSize();
    }

    public double getMemoryUsage() {
        return (double) memory.getAvailable() / memory.getTotal();
    }

    public double getSystemLoad(long timestamp) {
        double systemLoad;

        if(timestamp == 0 || !CPU_TICKS.containsKey(timestamp)) {
            systemLoad = getSystemLoadAverages(1)[0];
        } else {
            long[] oldCpuTicks = CPU_TICKS.remove(timestamp);
            systemLoad = cpu.getSystemCpuLoadBetweenTicks(oldCpuTicks);
        }

        return systemLoad;
    }

    public long generateCpuTicks() {
        long now = System.currentTimeMillis();
        long[] cpuTicks = cpu.getSystemCpuLoadTicks();
        CPU_TICKS.put(now, cpuTicks);
        return now;
    }
}
