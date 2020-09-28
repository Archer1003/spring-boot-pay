package com.cserver.saas.modules.wechatpay.service.impl;

import com.alibaba.fastjson.JSON;
import com.cserver.saas.commons.weixin.util.AccessToken;
import com.cserver.saas.commons.weixin.util.AdvancedUtil;
import com.cserver.saas.modules.wechatpay.enums.PayOrderField;
import com.cserver.saas.modules.wechatpay.enums.ResultCode;
import com.cserver.saas.modules.wechatpay.model.PayOrderParam;
import com.cserver.saas.modules.wechatpay.model.PayOrderResult;
import com.cserver.saas.modules.wechatpay.service.IWechatH5PayService;
import com.cserver.saas.modules.wechatpay.util.*;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lisc
 * @date 2020/9/17 10:13
 */
@Service
public class WechatH5PayServiceImpl implements IWechatH5PayService {
    private static final Logger logger = LoggerFactory.getLogger(WechatH5PayServiceImpl.class);

    @Value("${wexinpay.notify.url}")
    private String notify_url;
    @Value("${server.context.url}")
    private String context_url;

    @Override
    public String jsapiPay(Map<String, String> map,String jsurl) {
        String orderBody = map.get("orderBody");
        String voucherId = map.get("voucherId"); //有后缀
        String orderFee = map.get("orderFee");
        String openId = map.get("openId");

        PayOrderParam param = new PayOrderParam();

        // 基本信息
        param.setAppid(ConfigUtil.APP_ID);
        param.setMchId(ConfigUtil.MCH_ID);
        //签名
        param.setNonceStr(EncryptUtil.random());
        param.setTradeType("JSAPI"); //公众号
        param.setSpbillCreateIp(LocalIPUtil.getLocalAddr());

        param.setNotifyUrl(notify_url);//回调url

        // 业务相关参数
        JSONObject atach = new JSONObject();
        atach.put("order_id", null);
        param.setAttach(atach.toString());
        param.setBody(orderBody);
        param.setTotalFee(Integer.parseInt(orderFee));
        param.setOutTradeNo(voucherId); // 客户订单号

        param.setOpenid(openId);

        Map<String, Object> data = BeanUtil.object2Map(param); // 参数列表
        param.setSign(SignUtil.sign(data, ConfigUtil.API_KEY)); // 计算sign
        param.setSign_type("MD5");
        data.put(PayOrderField.SIGN.getField(), param.getSign()); // sign放到map中，为后续转xml

        // 校验参数是否齐全
        ValidateUtil.validate(PayOrderField.values(), data);

        // 转成xml格式
        String xml = XmlUtil.toXml(data);
        logger.info("post.xml=" + xml);
        // 发送支付请求
        String resultStr = WeixinHttpUtil.postXml(ConfigUtil.UNIFIED_ORDER_URL, xml);
        logger.info("result=" + resultStr);

        //校验返回结果 签名
        Map<String, Object> resultMap = XmlUtil.parseXml(resultStr);
        if(!"SUCCESS".equals(resultMap.get("return_code"))){
            resultMap.put("state", ResultCode.FAIL.getCode());
            return JSON.toJSONString(resultMap);
        }

        String resultSign = SignUtil.sign(resultMap, ConfigUtil.API_KEY);
        if (resultMap.get("sign") == null || !resultMap.get("sign").equals(resultSign)) {
            logger.info("sign is not correct, " + resultMap.get("sign") + " " + resultSign);
            throw new RuntimeException("签名校验不通过");
        }

        //返回结果
        PayOrderResult result = BeanUtil.map2Object(PayOrderResult.class, resultMap);

        Map<String, String> payMap = new HashMap<String, String>();
        if (ResultCode.SUCCESS.getCode().equals(result.getReturnCode())
                && ResultCode.SUCCESS.getCode().equals(result.getResultCode())) {

            //获取js_apitoken
            AccessToken accessToken = AdvancedUtil.getInstance();
            String accesstoken = accessToken.getToken();

            String jsapi_ticket = AdvancedUtil.getTicket(accesstoken);
            Map<String, String> ret = SignUtil.sign(jsapi_ticket, jsurl);

            //生成paySign签名
            payMap.put("appId", ConfigUtil.APP_ID);
            payMap.put("timeStamp", ret.get("timestamp"));
            payMap.put("nonceStr", ret.get("nonceStr"));
            payMap.put("package", "prepay_id=" + result.getPrepayId());
            payMap.put("signType", "MD5");
            String paySign = SignUtil.mapSign(payMap, ConfigUtil.API_KEY);
            payMap.put("paySign", paySign);

            //下单成功返回
            payMap.put("state", ResultCode.SUCCESS.getCode());
            //浏览器类型
            payMap.put("browserType", param.getTradeType());

            //微信config
            payMap.put("signature", ret.get("signature"));
        } else {
            payMap.put("state", ResultCode.FAIL.getCode());
            payMap.put("return_msg", result.getErrCodeDes());
        }
        return null;
    }

    @Override
    public String h5Pay(Map<String, String> map) {
        String orderBody = map.get("orderBody");
        String voucherId = map.get("voucherId"); //有后缀
        String orderFee = map.get("orderFee");
        String layout = map.get("layout");

        PayOrderParam param = new PayOrderParam();

        // 基本信息
        param.setAppid(ConfigUtil.APP_ID);
        param.setMchId(ConfigUtil.MCH_ID);
        //签名
        param.setNonceStr(EncryptUtil.random());
        param.setTradeType("MWEB"); //H5


        JSONObject h5_infoJsonObj=new JSONObject();
        h5_infoJsonObj.put("type","Wap");
        h5_infoJsonObj.put("wap_url",context_url);
        h5_infoJsonObj.put("wap_name","SaaS超市");

        JSONObject sceneInfoJsonObj=new JSONObject();
        sceneInfoJsonObj.put("h5_info",h5_infoJsonObj);

//        String sceneInfo = "{\"h5_info\": {\"type\":\"Wap\",\"wap_url\": \""+context_url+"\",\"wap_name\": \"SaaS超市\"}}";
        param.setSceneInfo(sceneInfoJsonObj.toString()); //H5必填
        param.setSpbillCreateIp(LocalIPUtil.getLocalAddr());
        param.setNotifyUrl(notify_url);//回调url

        // 业务相关参数
        JSONObject atach = new JSONObject();
        atach.put("order_id", null);
        param.setAttach(atach.toString());
        param.setBody(orderBody);
        param.setTotalFee(Integer.parseInt(orderFee));
        param.setOutTradeNo(voucherId); // 客户订单号

        Map<String, Object> data = BeanUtil.object2Map(param); // 参数列表
        param.setSign(SignUtil.sign(data, ConfigUtil.API_KEY)); // 计算sign
        param.setSign_type("MD5");
        data.put(PayOrderField.SIGN.getField(), param.getSign()); // sign放到map中，为后续转xml

        // 校验参数是否齐全
        ValidateUtil.validate(PayOrderField.values(), data);

        // 转成xml格式
        String xml = XmlUtil.toXml(data);
        logger.info("post.xml=" + xml);
        // 发送支付请求
        String resultStr = WeixinHttpUtil.postXml(ConfigUtil.UNIFIED_ORDER_URL, xml);
        logger.info("result=" + resultStr);

        //校验返回结果 签名
        Map<String, Object> resultMap = XmlUtil.parseXml(resultStr);
        if(!"SUCCESS".equals(resultMap.get("return_code"))){
            resultMap.put("state", ResultCode.FAIL.getCode());
            return JSON.toJSONString(resultMap);
        }

        String resultSign = SignUtil.sign(resultMap, ConfigUtil.API_KEY);
        if (resultMap.get("sign") == null || !resultMap.get("sign").equals(resultSign)) {
            logger.info("sign is not correct, " + resultMap.get("sign") + " " + resultSign);
            throw new RuntimeException("签名校验不通过");
        }

        //返回结果
        PayOrderResult result = BeanUtil.map2Object(PayOrderResult.class, resultMap);

        Map<String, String> payMap = new HashMap<String, String>();
        if (ResultCode.SUCCESS.getCode().equals(result.getReturnCode())
                && ResultCode.SUCCESS.getCode().equals(result.getResultCode())) {
            //下单成功返回
            payMap.put("state", ResultCode.SUCCESS.getCode());
            //浏览器类型
            payMap.put("browserType", param.getTradeType());

            String targetUrl = "/ptorders/payRedirct.do?layout="+layout+"&weChatOrderId="+voucherId+"&mweb_url="+result.getMweb_url();
            //支付成功回调的url
            String redirect_url = null;
            try {
                redirect_url = URLEncoder.encode(context_url + targetUrl, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            logger.info("redirect_url=====" + redirect_url);

            payMap.put("mweb_redirect_url", result.getMweb_url() + "&redirect_url=" + redirect_url);
            logger.info("mweb_redirect_url=====" + result.getMweb_url() + "&redirect_url=" + redirect_url);
        } else {
            payMap.put("state", ResultCode.FAIL.getCode());
            payMap.put("return_msg", result.getErrCodeDes());
        }

        return null;
    }
}
