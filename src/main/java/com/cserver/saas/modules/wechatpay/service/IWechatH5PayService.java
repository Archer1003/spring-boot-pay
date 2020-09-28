package com.cserver.saas.modules.wechatpay.service;

import java.util.Map;

/**
 * @author lisc
 * @date 2020/9/17 10:13
 */
public interface IWechatH5PayService {

    String jsapiPay(Map<String, String> map,String jsurl);

    String h5Pay(Map<String, String> map);
}

