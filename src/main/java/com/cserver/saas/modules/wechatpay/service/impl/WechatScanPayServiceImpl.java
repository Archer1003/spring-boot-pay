package com.cserver.saas.modules.wechatpay.service.impl;

import com.alibaba.fastjson.JSON;
import com.cserver.saas.modules.wechatpay.enums.PayOrderField;
import com.cserver.saas.modules.wechatpay.enums.PayQueryField;
import com.cserver.saas.modules.wechatpay.enums.ResultCode;
import com.cserver.saas.modules.wechatpay.model.*;
import com.cserver.saas.modules.wechatpay.service.IWechatScanPayService;
import com.cserver.saas.modules.wechatpay.util.*;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author adminitrator
 * @date 2020/9/15 15:51
 */

@Service
public class WechatScanPayServiceImpl implements IWechatScanPayService {

    private static final Logger logger = LoggerFactory.getLogger(WechatScanPayServiceImpl.class);

    @Value("${wexinpay.notify.url}")
    private String notify_url;

    @Override
    public PayOrderResult qcPay2(Map<String, String> map) {
        String orderId=map.get("orderId");
        String orderBody = map.get("orderBody");
        String voucherId = map.get("voucherId");
        String orderFee = map.get("orderFee");

        PayOrderParam param = new PayOrderParam();
        // 基本信息
        param.setAppid(ConfigUtil.APP_ID);
        param.setMchId(ConfigUtil.MCH_ID);
        param.setTradeType("NATIVE"); // 扫码支付
        param.setSpbillCreateIp(LocalIPUtil.getLocalAddr());
        //param.setLimitPay("no_credit"); // 禁止用信用卡

        param.setNotifyUrl(notify_url);//回调url

        // 业务相关参数
        JSONObject atach = new JSONObject();
        atach.put("order_id", orderId);
        param.setAttach(atach.toString());
        param.setBody(orderBody);
        param.setTotalFee(Integer.parseInt(orderFee)); // 1分钱
        param.setOutTradeNo(voucherId); // 客户订单号

        //签名
        param.setNonceStr(EncryptUtil.random());
        Map<String, Object> data = BeanUtil.object2Map(param); // 参数列表
        param.setSign(SignUtil.sign(data, ConfigUtil.API_KEY)); // 计算sign
        data.put(PayOrderField.SIGN.getField(), param.getSign()); // sign放到map中，为后续转xml

        // 校验参数是否齐全
        ValidateUtil.validate(PayOrderField.values(), data);

        // 转成xml格式
        String xml = XmlUtil.toXml(data);
        logger.info("post.xml=" + xml);
        // 发送支付请求
        String resultStr = WeixinHttpUtil.postXml(ConfigUtil.UNIFIED_ORDER_URL, xml);
        logger.info("result=" + resultStr);

        // 校验返回结果 签名
        Map<String, Object> resultMap = XmlUtil.parseXml(resultStr);
        String resultSign = SignUtil.sign(resultMap, ConfigUtil.API_KEY);
        if (resultMap.get("sign") == null || !resultMap.get("sign").equals(resultSign)) {
            logger.info("sign is not correct, " + resultMap.get("sign") + " " + resultSign);
            throw new RuntimeException("签名校验不通过");
        }

        PayOrderResult result = BeanUtil.map2Object(PayOrderResult.class, resultMap);
        return result;
    }

    public String notify(String xml) {
        if (StringUtils.isBlank(xml)) {
            return WeixinHttpUtil.getResult(ResultCode.FAIL, "接受参数为空");
        }
        Map<String, Object> map = XmlUtil.parseXml(xml);

        PayNotifyResult result = BeanUtil.map2Object(PayNotifyResult.class, map);
        logger.info(JSON.toJSONString(result, true));

        return WeixinHttpUtil.getResult(ResultCode.SUCCESS, "OK");
    }

    public PayQueryResult query(Map<String, String> map)  {
        String voucherId=map.get("voucherId");
        String wxid = map.get("wxid");
        PayQueryParam param = new PayQueryParam();
        param.setAppid(ConfigUtil.APP_ID);
        param.setMchId(ConfigUtil.MCH_ID);

        param.setOutTradeNo(voucherId);
        param.setTransactionId(wxid);

        param.setNonceStr(EncryptUtil.random());
        Map<String, Object> data = BeanUtil.object2Map(param);
        param.setSign(SignUtil.sign(data, ConfigUtil.API_KEY));
        data.put(PayOrderField.SIGN.getField(), param.getSign());

        ValidateUtil.validate(PayQueryField.values(), data);

        String xml = XmlUtil.toXml(data);
        logger.info("post.xml=" + xml);
        String resultStr = WeixinHttpUtil.postXml(ConfigUtil.CHECK_ORDER_URL, xml);
//         resultStr = new String(resultStr.getBytes("gbk"), "UTF-8");
        logger.info("result=" + resultStr);


        Map<String, Object> resultMap = XmlUtil.parseXml(resultStr);
        String resultSign = SignUtil.sign(resultMap, ConfigUtil.API_KEY);
        if (resultMap.get("sign") == null || !resultMap.get("sign").equals(resultSign)) {
            logger.info("sign is not correct, " + resultMap.get("sign") + " " + resultSign);
            throw new RuntimeException("error");
        }

        PayQueryResult result = BeanUtil.map2Object(PayQueryResult.class, resultMap);
        return result;
    }
}
