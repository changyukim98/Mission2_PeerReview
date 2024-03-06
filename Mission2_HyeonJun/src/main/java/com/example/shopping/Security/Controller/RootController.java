package com.example.shopping.Security.Controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
@Slf4j
@RestController
public class RootController {

    // hello 뜨는지 확인
    @GetMapping
    public String root(){
        return "hello";
    }
    
    // 모두 허용 테스트용
    @GetMapping("/no-auth")
    public String noAuth() {
        return "no auth success!";
    }
    
    // 인증 필요 테스트용
    @GetMapping("/require-auth")
    public String reAuth() {
        return "auth success!";
    }
}
