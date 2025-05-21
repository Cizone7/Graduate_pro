package com.example.springboot.controller;

import cn.hutool.json.JSONObject;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradePayRequest;
import com.example.springboot.common.Constants;
import com.example.springboot.config.AliPayConfig;
import com.example.springboot.entity.Record;
import com.example.springboot.exception.ServiceException;
import com.example.springboot.service.IRecordService;
import com.example.springboot.utils.TokenUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/alipay")
public class AliPayController {

    private static final String GATEWAY_URL = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";
    private static final String FORMAT ="JSON";
    private static final String CHARSET = "UTF-8";
    private static final String SIGN_TYPE = "RSA2";

    @Resource
    private AliPayConfig aliPayConfig;

   @Resource
    private RecordController recordController;
    @Resource
    private IRecordService recordService;

   @GetMapping("/pay")
    public void pay(String orderNo, HttpServletResponse httpResponse)throws Exception{
       try{
           Integer currentUserId = TokenUtils.getCurrentUser().getId();
           Record record = recordService.selectByOrderno(orderNo);
           if (record== null){
               throw new ServiceException(Constants.CODE_400, "订单不存在");
           }
           // 验证订单是否属于当前用户
           if (!record.getUserId().equals(currentUserId)) {
               throw new ServiceException(Constants.CODE_403, "无权支付该订单");
           }

           AlipayClient alipayClient =  new DefaultAlipayClient(GATEWAY_URL,aliPayConfig.getAppId(),
                   aliPayConfig.getAppPrivateKey(), FORMAT, CHARSET, aliPayConfig.getAlipayPublickey(),SIGN_TYPE);

           AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
           request.setNotifyUrl(aliPayConfig.getNotifyUrl());
           JSONObject bizContent = new JSONObject();
           bizContent.set("out_trade_no", record.getOrderNo());
           bizContent.set("total_amount", record.getAmount());
           bizContent.set("subject", "积分充值");
           bizContent.set("product_code", "FAST_INSTANT_TRADE_PAY");

           request.setBizContent(bizContent.toString());

           String form = alipayClient.pageExecute(request).getBody();
           httpResponse.setContentType("text/html;charset=" + CHARSET);
           httpResponse.getWriter().write(form);
           httpResponse.getWriter().flush();
           httpResponse.getWriter().close();
       }catch (Exception e) {
           e.printStackTrace(); // 打印异常堆栈
           throw new ServiceException(Constants.CODE_500, "支付请求失败: " + e.getMessage());
       }
   }
    @PostMapping("/notify")
    public String payNotify(HttpServletRequest request) throws Exception {
        System.out.printf("=============支付宝回调成功===============");
        Map<String, String> params = convertRequestParamsToMap(request);
        boolean signVerified = AlipaySignature.rsaCheckV1(
                params,
                aliPayConfig.getAlipayPublickey(),
                CHARSET,
                SIGN_TYPE);

        if (signVerified) {
            String tradeStatus = params.get("trade_status");
            String outTradeNo = params.get("out_trade_no");

            if ("TRADE_SUCCESS".equals(tradeStatus)) {
                Record record = recordService.selectByOrderno(outTradeNo);
                if (record != null && "WAIT".equals(record.getStatus())) {
                    // 更新订单状态
                    record.setStatus("SUCCESS");
                    record.setPayTime(new Date());
                    recordService.updateById(record);

                    // 执行充值逻辑
                    recordController.doRechargeCallback(record);
                }
            }
            return "success";
        }
        return "fail";
    }

    private Map<String, String> convertRequestParamsToMap(HttpServletRequest request) {
        return request.getParameterMap().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> String.join(",", entry.getValue())
                ));
    }

}
