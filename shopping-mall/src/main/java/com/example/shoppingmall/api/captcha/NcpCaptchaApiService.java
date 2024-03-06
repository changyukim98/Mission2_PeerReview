package com.example.shoppingmall.api.captcha;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;

import java.util.Map;

public interface NcpCaptchaApiService {
    @GetExchange("/captcha/v1/nkey")
    String getCaptchaKey(
            @RequestParam("code") int code
    );

    @GetExchange("/captcha-bin/v1/ncaptcha")
    byte[] getCaptchaImage(
            @RequestParam("key") String key
    );

    @GetExchange("/captcha/v1/nkey")
    NcpCaptchaVerifyResponse verifyCaptcha(
            @RequestParam("code") int code,
            @RequestParam("key") String key,
            @RequestParam("value") String value
    );
}
