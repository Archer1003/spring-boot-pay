package com.cserver.saas.modules.wechatpay.service;

import com.cserver.saas.modules.wechatpay.model.PayOrderResult;
import com.cserver.saas.modules.wechatpay.model.PayQueryResult;

import java.util.Map;

/**
 * @author lisc
 * @date 2020/9/15 15:50
 */
public interface IWechatScanPayService {
    /*
     * 扫码支付 模式二下单
     * @author lisc
     * @date 2020/9/16
     * @param map
     * @return
     */
    PayOrderResult qcPay2(Map<String, String> map);
    /*
     * 扫码支付 模式二 回调
     * @author lisc
     * @date 2020/9/16
     * @param map
     * @return
     */
    String notify(String xml);
    /*
     * 扫码支付 模式二 回调
     * @author lisc
     * @date 2020/9/16
     * @param map
     * @return
     */
    PayQueryResult query(Map<String, String> map);
}
