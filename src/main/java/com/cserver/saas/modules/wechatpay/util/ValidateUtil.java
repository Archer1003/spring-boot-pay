package com.cserver.saas.modules.wechatpay.util;

import com.cserver.saas.modules.wechatpay.model.Validator;

import java.util.Map;

public class ValidateUtil {
    public static void validate(Validator[] values, Map<String, Object> data) {
        for (Validator v : values) {
            if (v.isRequired()) {
                if (data.get(v.getField()) == null) {
                    throw new IllegalArgumentException(v.getField() + "不能为空");
                }
            }
        }
    }
}
