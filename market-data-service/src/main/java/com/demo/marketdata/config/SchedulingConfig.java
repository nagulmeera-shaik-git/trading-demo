package com.demo.marketdata.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Turns on @Scheduled methods (used by the mock price feed). */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
