package org.bigbluebutton.resource.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;

@Configuration
@ComponentScan("org.bigbluebutton.resource")
public class ResourceConfig {

    @Bean
    HardwareAbstractionLayer hardwareAbstractionLayer() {
        return new SystemInfo().getHardware();
    }

    @Bean
    CentralProcessor centralProcessor(HardwareAbstractionLayer hal) {
        return hal.getProcessor();
    }

    @Bean
    GlobalMemory globalMemory(HardwareAbstractionLayer hal) {
        return hal.getMemory();
    }
}
