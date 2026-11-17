package com.demo.marketdata.config;

import com.demo.common.Topics;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Creates the market.ticks topic at startup if it doesn't exist yet.
 * 6 partitions: ticks are keyed by symbol, so each share always lands on the same partition
 * and its ticks stay in order.
 */
@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic marketTicksTopic() {
        return TopicBuilder.name(Topics.MARKET_TICKS)
                .partitions(6)
                .replicas(1)
                .build();
    }
}
