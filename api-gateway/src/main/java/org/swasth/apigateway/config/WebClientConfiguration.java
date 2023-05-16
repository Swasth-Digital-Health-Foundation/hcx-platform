package org.swasth.apigateway.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.web.codec.CodecCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.ClientCodecConfigurer;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfiguration {

    private final CodecCustomizer codecCustomizer;

    public WebClientConfiguration(ObjectProvider<CodecCustomizer> codecCustomizerProvider) {
        this.codecCustomizer = codecCustomizerProvider.getIfAvailable();
    }

    @Bean
    public WebClient webClient() {
        WebClient.Builder webClientBuilder = WebClient.builder();

        ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
                .codecs(this::customizeCodecs)
                .build();

        webClientBuilder.exchangeStrategies(exchangeStrategies);

        return webClientBuilder.build();
    }

    private void customizeCodecs(ClientCodecConfigurer configurer) {
        if (codecCustomizer != null) {
            this.codecCustomizer.customize(configurer);
        }

        configurer.defaultCodecs()
                .maxInMemorySize(16777216); // Set the maximum buffer size to 524,288 bytes (512 KB)
    }
}

