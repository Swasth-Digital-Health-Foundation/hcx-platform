package org.swasth.apigateway.config;

import org.springframework.boot.web.codec.CodecCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfiguration {

    @Bean
    public WebClient webClient(CodecCustomizer codecCustomizer) {
        ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
                .codecs(configurer -> codecCustomizer.customize(configurer))
                .build();

        ClientHttpConnector httpConnector = new ReactorClientHttpConnector();

        return WebClient.builder()
                .clientConnector(httpConnector)
                .exchangeStrategies(exchangeStrategies)
                .build();
    }

    @Bean
    public CodecCustomizer codecCustomizer() {
        return configurer -> configurer.defaultCodecs().maxInMemorySize(16777216); // Set the maximum buffer size to 524,288 bytes (512 KB)
    }

}
