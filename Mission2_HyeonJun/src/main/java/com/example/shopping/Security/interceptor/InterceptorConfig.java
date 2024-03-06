package com.example.shopping.Security.interceptor;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class InterceptorConfig implements WebMvcConfigurer {
    private final LoggingInterceptor loggingInterceptor;

    @Override
    // Interceptor 를 등록하기 위한 메서드
    public void addInterceptors(InterceptorRegistry registry){

        registry
                // 어떤 인터셉터를
                .addInterceptor(loggingInterceptor)
                // 어떤 경로에
                .addPathPatterns("/tests");
    }
}
