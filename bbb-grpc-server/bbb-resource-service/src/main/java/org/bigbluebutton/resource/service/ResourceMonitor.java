package org.bigbluebutton.resource.service;

import org.springframework.stereotype.Service;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;

@Service
public class ResourceMonitor {
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

}
