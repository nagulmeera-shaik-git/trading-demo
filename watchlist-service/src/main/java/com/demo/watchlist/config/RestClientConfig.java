package com.demo.watchlist.config;

import com.demo.watchlist.client.MarketDataClientProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Builds the RestClient used to call market-data-service.
 * We use Spring Boot's RestClient.Builder (not RestClient.create()) because Boot pre-configures it
 * with JSON support, metrics and tracing headers.
 */
@Configuration
public class RestClientConfig {

    @Bean
    public RestClient marketDataRestClient(RestClient.Builder builder, MarketDataClientProperties props) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(props.connectTimeout());
        factory.setReadTimeout(props.readTimeout());

        return builder
                .baseUrl(props.baseUrl())
                .requestFactory(factory)
                .build();
    }
}
