package org.bigbluebutton.api.domain;

import oshi.SystemInfo;
import oshi.hardware.*;

public class ServerInfo {

    private static ServerInfo instance;

    private SystemInfo systemInfo;
    private HardwareAbstractionLayer hal;
    private CentralProcessor cpu;
    private GlobalMemory memory;

    public static ServerInfo getInstance() {
        if(instance == null) instance = new ServerInfo();
        return instance;
    }

    private ServerInfo() {
        systemInfo = new SystemInfo();
        hal = systemInfo.getHardware();
        cpu = hal.getProcessor();
        memory = hal.getMemory();
    }

    public double getCpuUsage() {
        return cpu.getSystemLoadAverage(1)[0];
    }

    public double getMemoryUsage() {
        return (double) memory.getAvailable() / memory.getTotal();
    }
}
