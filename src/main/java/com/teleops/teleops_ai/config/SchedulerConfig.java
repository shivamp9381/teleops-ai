package com.teleops.teleops_ai.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Scheduler Configuration
 *
 * @EnableScheduling activates Spring's @Scheduled annotation.
 * Without this, @Scheduled methods do nothing.
 */
@Configuration
@EnableScheduling
public class SchedulerConfig {
    // Configuration is annotation-driven.
    // The @EnableScheduling annotation on this class is all
    // that is needed to activate the scheduler.
}