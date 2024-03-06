package com.example.shopping.Security.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.Enumeration;

@Component
@Slf4j
public class LoggingInterceptor implements HandlerInterceptor {
    // 요청이 HandlerMethod(RequestMapping) 메서드에 도착하기 전 실행
    @Override
    public boolean preHandle(
            // 요청
            HttpServletRequest request,
            // 응답
            HttpServletResponse response,
            // 실제로 요청을 처리할 RequestMapping 을 나타내는 메서드 객체
            Object handler
    ) throws Exception {
        HandlerMethod handlerMethod = (HandlerMethod) handler;  // 형 변환
        log.info("pre handling of {}", handlerMethod.getMethod().getName());
        Enumeration<String> headerNames = request.getHeaderNames();
        while(headerNames.hasMoreElements()){
            String headerName = headerNames.nextElement();
            log.info("{}: {}", headerName, request.getHeader(headerName));
        }
        // preHandle 이 false 를 반환하면, 요청이 Handler 메서드로 전달되지 않음!!!
        return true;
    }

    // HandlerMethod(RequestMapping) 이 처리가 되고 응답이 보내지기 전 실행    
    @Override
    public void postHandle(HttpServletRequest request,
                           HttpServletResponse response,
                           Object handler,
                           ModelAndView modelAndView    // MVC 이용할 경우 추가로 이용가능
    ) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    // 요청의 처리가 완전히 마루리 된 후 실행
    // 요청 처리 과정에서 예외가 발생하면, 인자로 전달받음
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
