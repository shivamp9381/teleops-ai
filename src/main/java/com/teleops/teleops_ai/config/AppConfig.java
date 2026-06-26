package com.teleops.teleops_ai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * General Application Configuration
 *
 * @Configuration marks this as a Spring configuration class.
 * Spring will process @Bean methods and register the returned
 * objects in the application context.
 *
 * RestTemplate:
 *   Used by our GroqClient to make HTTP calls to the Groq AI API.
 *   We register it as a Bean so it is shared and managed by Spring.
 *   In production, we would configure timeouts here.
 */
@Configuration
public class AppConfig {

    /**
     * RestTemplate for making outbound HTTP calls.
     *
     * Used exclusively by the AI module to call Groq API.
     * We configure connection and read timeouts to prevent
     * our application from hanging on slow AI responses.
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}