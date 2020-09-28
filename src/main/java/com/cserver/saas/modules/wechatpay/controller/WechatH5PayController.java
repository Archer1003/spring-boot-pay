package com.cserver.saas.modules.wechatpay.controller;

import com.cserver.saas.modules.wechatpay.service.IWechatH5PayService;
import com.cserver.saas.modules.wechatpay.util.JsonUtils;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * H5和JSAPI支付
 * @author lisc
 * @date 2020/9/17 9:29
 */
@Api(tags ="中服云微信JSAPI支付")
@Controller
@RequestMapping(value = "cserver/weiXinPay")
public class WechatH5PayController extends BaseController{

    private static final Logger logger = LoggerFactory.getLogger(WechatH5PayController.class);
    @Autowired
    private IWechatH5PayService wechatH5PayService;
    /**
     *
     * @Description: 微信支付
     * @author qiaojk
     * @date 2019年9月27日 下午2:12:47
     */
    @RequestMapping(value = "/jsApiPay", method = {RequestMethod.POST})
    public void jsapiPay(HttpServletRequest request, HttpServletResponse response, @RequestBody String json) {
        logger.info("下订单 方法jsapi，参数 json："+json );

        Map<String, String> map = JsonUtils.toMap(json);
//        String orderBody = map.get("orderBody");
//        String voucherId = map.get("voucherId"); //有后缀
//        String orderFee = map.get("orderFee");
//        String openId = map.get("openId");

        String jsurl = request.getParameter("jsurl");

        String result=wechatH5PayService.jsapiPay(map,jsurl);
        responseWrite(response,result);
    }


    /**
     *
     * @Description: 微信H5支付
     * @author qiaojk
     * @date 2019年9月27日 下午2:12:47
     */
    @RequestMapping(value = "/mwebPay", method = {RequestMethod.POST})
    public String h5Pay(HttpServletRequest request, HttpServletResponse response, @RequestBody String json) {
        logger.info("下单===================");

        Map<String, String> map = JsonUtils.toMap(json);
//        String orderBody = map.get("orderBody");
//        String voucherId = map.get("voucherId"); //有后缀
//        String orderFee = map.get("orderFee");
//        String layout = map.get("layout");
        String result=wechatH5PayService.h5Pay(map);
        responseWrite(response,result);
        return null;
    }

}
