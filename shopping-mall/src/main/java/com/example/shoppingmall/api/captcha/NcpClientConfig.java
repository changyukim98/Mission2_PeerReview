package com.example.shoppingmall.api.captcha;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class NcpClientConfig {
    private static final String NCP_APIGW_KEY_ID = "X-NCP-APIGW-API-KEY-ID";
    private static final String NCP_APIGW_KEY = "X-NCP-APIGW-API-KEY";

    @Value("${ncp.api.client-id}")
    private String ncpApiClientId;
    @Value("${ncp.api.client-secret}")
    private String ncpApiClientSecret;

    @Bean
    public RestClient ncpApiClient() {
        return RestClient.builder()
                .baseUrl("https://naveropenapi.apigw.ntruss.com")
                .defaultHeader(NCP_APIGW_KEY_ID, ncpApiClientId)
                .defaultHeader(NCP_APIGW_KEY, ncpApiClientSecret)
                .build();
    }

    @Bean
    public NcpCaptchaApiService captchaApiService() {
        return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(ncpApiClient()))
                .build()
                .createClient(NcpCaptchaApiService.class);
    }
}
