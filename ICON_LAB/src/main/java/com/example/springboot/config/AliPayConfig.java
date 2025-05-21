package com.example.springboot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "alipay")
public class AliPayConfig {
    private String appId;
    private String appPrivateKey;
    private String alipayPublickey;
    private String notifyUrl;

    public String getAppId(){
        return appId;
    }
    public void setAppId(String appId){
        this.appId = appId;
    }

    public String getAppPrivateKey(){
        return appPrivateKey;
    }
    public void setAppPrivateKey(String appPrivateKey){
        this.appPrivateKey = appPrivateKey;
    }
    public String getAlipayPublickey(){
        return alipayPublickey;
    }
    public void setAlipayPublickey(String alipayPublickey){
        this.alipayPublickey = alipayPublickey;
    }

    public String getNotifyUrl(){
        return notifyUrl;
    }
    public void setNotifyUrl(String notifyUrl){
        this.notifyUrl = notifyUrl;
    }

}
