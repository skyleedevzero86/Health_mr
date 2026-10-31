package com.sleekydz86.finance.config;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class NonCoveredMedicalFeeApiConfig {

    @Value("${non-covered-medical-fee.api.base-url}")
    private String apiBaseUrl;

    @Value("${non-covered-medical-fee.api.timeout:5000}")
    private int timeout;

    @Bean
    public WebClient nonCoveredMedicalFeeWebClient() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeout)
                .responseTimeout(Duration.ofMillis(timeout))
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(timeout, TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(timeout, TimeUnit.MILLISECONDS)));

        XmlMapper xmlMapper = new XmlMapper();
        Jackson2JsonDecoder xmlDecoder = new Jackson2JsonDecoder(
                xmlMapper, MediaType.APPLICATION_XML, MediaType.TEXT_XML);
        Jackson2JsonEncoder xmlEncoder = new Jackson2JsonEncoder(
                xmlMapper, MediaType.APPLICATION_XML, MediaType.TEXT_XML);
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> {
                    configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024);
                    configurer.customCodecs().register(xmlDecoder);
                    configurer.customCodecs().register(xmlEncoder);
                })
                .build();

        return WebClient.builder()
                .baseUrl(apiBaseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(strategies)
                .build();
    }
}

