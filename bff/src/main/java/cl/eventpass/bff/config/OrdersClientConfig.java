package cl.eventpass.bff.config;

import java.net.http.HttpClient;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration(proxyBeanMethods = false)
public class OrdersClientConfig {

    @Bean
    public RestClient ordersRestClient(
            RestClient.Builder builder,
            @Value("${app.clients.orders.base-url}") String baseUrl,
            @Value("${app.clients.orders.connect-timeout:3s}") Duration connectTimeout,
            @Value("${app.clients.orders.read-timeout:10s}") Duration readTimeout
    ) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(connectTimeout)
                .build();

        JdkClientHttpRequestFactory requestFactory =
                new JdkClientHttpRequestFactory(httpClient);

        requestFactory.setReadTimeout(readTimeout);

        return builder
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }
}
