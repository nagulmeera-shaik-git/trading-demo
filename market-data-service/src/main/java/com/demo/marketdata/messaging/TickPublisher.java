package com.demo.marketdata.messaging;

import com.demo.common.Topics;
import com.demo.common.events.TickEvent;
import com.demo.marketdata.domain.PriceSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes every price tick to the Kafka topic market.ticks.
 * The message key is the symbol, so all ticks for one share go to the same partition, in order.
 */
@Component
public class TickPublisher {

    private static final Logger log = LoggerFactory.getLogger(TickPublisher.class);

    private final KafkaTemplate<String, TickEvent> kafka;

    public TickPublisher(KafkaTemplate<String, TickEvent> kafka) {
        this.kafka = kafka;
    }

    public void publish(PriceSnapshot s) {
        TickEvent event = new TickEvent(s.symbol(), s.ltp(), s.bid(), s.ask(), s.volume(), s.timestamp());

        // send() is asynchronous: it returns immediately and reports the result later
        kafka.send(Topics.MARKET_TICKS, s.symbol(), event)
                .whenComplete((result, error) -> {
                    if (error != null) {
                        log.warn("Could not publish tick for {}: {}", s.symbol(), error.getMessage());
                    }
                });
    }
}
