# 사업자 자동 로그인 방지 (NCP Capcha)

## NcpConfig
```Java
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

```

## 프록시 서비스
```Java
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
```

## Ncp 캡차 키 발급
> Ncp 캡차를 사용하기 위해서는 먼저 키를 발급받아야한다.   
URI `/captcha/v1/nkey`로 code=0 파라미터를 추가해 보낸다.
```java
String keyJsonString = ncpApiService.getCaptchaKey(0);

JsonObject keyJsonObject = gson.fromJson(keyJsonString, JsonObject.class);
String captchaKey = keyJsonObject.get("key").getAsString();
```
> 응답으로 Json형식의 Text가 들어오기 때문에, Gson 라이브러리를 추가해 Json으로 파싱하여 key를 추출한다.

## Ncp 이미지 캡차 파일 요청
> 캡차 이미지를 받기 위해서는 발급받은 키를 이용해 요청한다.
```Java
byte[] imageBytes = ncpApiService.getCaptchaImage(captchaKey);
```
> 응답으로 byte 배열이 돌아오므로 적절히 이미지 파일로 변환하도록 한다.
```Java
String fileName = UUID.randomUUID().toString() + "_"
                + userEntity.getUsername() + ".png";
String filePath = "media/captcha/" + fileName;

try {
    InputStream inputStream = new ByteArrayInputStream(imageBytes);
    BufferedImage bufferedImage = ImageIO.read(inputStream);

    File outputFile = new File(filePath);
    ImageIO.write(bufferedImage, "png", outputFile);
    System.out.println("이미지가 성공적으로 저장되었습니다.");
} catch (IOException e) {
    System.err.println("이미지 저장 중 오류가 발생했습니다: " + e.getMessage());
    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
}
```