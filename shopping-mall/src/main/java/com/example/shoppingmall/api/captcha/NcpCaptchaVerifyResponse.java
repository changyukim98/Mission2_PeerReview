package com.example.shoppingmall.api.captcha;

import lombok.Data;

@Data
public class NcpCaptchaVerifyResponse {
    private Boolean result;
    private String responseTime;
}
