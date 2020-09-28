package com.cserver.saas.modules.wechatpay.controller;

import com.alibaba.fastjson.JSON;
import com.alipay.api.internal.util.file.IOUtils;
import com.cserver.saas.modules.wechatpay.enums.ResultCode;
import com.cserver.saas.modules.wechatpay.model.PayOrderResult;
import com.cserver.saas.modules.wechatpay.model.PayQueryResult;
import com.cserver.saas.modules.wechatpay.model.PayResult;
import com.cserver.saas.modules.wechatpay.service.IWechatScanPayService;
import com.cserver.saas.modules.wechatpay.util.JsonUtils;
import com.cserver.saas.modules.wechatpay.util.WeixinHttpUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * 微信支付
 * @author lisc
 * @date 2020/9/15 15:47
 */
@Api(tags ="中服云微信扫码支付")
@Controller
@RequestMapping(value = "cserver/weixin")
public class WechatScanPayController extends BaseController{

    private static final Logger logger = LoggerFactory.getLogger(WechatScanPayController.class);

    @Autowired
    private IWechatScanPayService wechatScanPayService;

    @ApiOperation(value="二维码支付(模式二)下单并成二维码")
    @PostMapping(value = "/scan")
    public void qcPay2(HttpServletResponse response,@RequestBody String json){
        logger.info("begin====");
        //json值
//        {
//            orderBody:"CServer综合办公管理",
//            orderFee:0,
//            voucherId:"S20200601163941927"
//        }
        Map<String, String> map = JsonUtils.toMap(json);

        PayOrderResult result =wechatScanPayService.qcPay2(map);

        PayResult payResult = new PayResult();
        if (ResultCode.SUCCESS.getCode().equals(result.getReturnCode())
                && ResultCode.SUCCESS.getCode().equals(result.getResultCode())) {
            payResult.setResultCode(ResultCode.SUCCESS.getCode());
        } else {
            payResult.setResultCode(ResultCode.FAIL.getCode());
        }
        payResult.setMessage(result.getReturnMsg());
        payResult.setErrCode(result.getErrCode());
        payResult.setErrorMessage(result.getErrCodeDes());
        payResult.setPrepayId(result.getPrepayId());
        payResult.setCodeUrl(result.getCodeUrl());

        responseWrite(response,JSON.toJSONString(payResult));
    }

    /*
     * 模式二支付回调
     * @author lisc
     * @date 2020/9/17
     * @param null
     * @return
     */
    @ApiOperation(value="模式二支付回调")
    @RequestMapping(value = "/notify" )
    public String notify(HttpServletRequest request) {
        String result;
        try {
            String xml = IOUtils.toString(request.getInputStream());
            result= wechatScanPayService.notify(xml);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return WeixinHttpUtil.getResult(ResultCode.FAIL, "解析异常");
        }
        return result;
    }

    /*
     * 订单查询
     * @author lisc
     * @date 2020/9/17
     * @param null
     * @return
     */
    @ApiOperation(value = "订单查询")
    @RequestMapping(value = "/query", method = {RequestMethod.POST})
    @ResponseBody
    public void query(HttpServletRequest request,HttpServletResponse response,@RequestBody String json)  {
        String JSONParm="";
        try {
            request.setCharacterEncoding("UTF-8");

            JSONParm = new String(json.getBytes("iso-8859-1"), "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Map<String, String> map = JsonUtils.toMap(JSONParm);
        PayQueryResult result = wechatScanPayService.query(map);
        responseWrite(response,JSON.toJSONString(result,true));
    }
}
