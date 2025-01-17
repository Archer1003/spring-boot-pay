package com.cserver.saas.modules.wechatpay.enums;

/**
 * 支付返回值
 */
public enum ResultCode {
    SUCCESS("SUCCESS"), FAIL("FAIL");
    private String code;

    ResultCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
