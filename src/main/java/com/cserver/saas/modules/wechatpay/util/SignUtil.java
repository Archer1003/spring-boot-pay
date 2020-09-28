package com.cserver.saas.modules.wechatpay.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class SignUtil {
    public static final Log logger = LogFactory.getLog(SignUtil.class);

    public static String sign(Object object, String key) {
        if (object == null) {
            return null;
        }

        Map<String, Object> data = BeanUtil.object2Map(object);
        if (data == null || data.isEmpty()) {
            return null;
        }

        return sign(data, key);
    }

    public static String sign(Map<String, Object> data, String key) {
        if (data == null || data.isEmpty()) {
            return null;
        }
        StringBuilder buf = new StringBuilder();
        TreeMap<String, Object> map = new TreeMap<String, Object>(data);

        Iterator<Map.Entry<String, Object>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Object> entry = it.next();
            Object k = entry.getKey();
            if ("class".equals(k) || "key".equals(k) || "sign".equals(k)) {
                continue;
            }
            Object v = entry.getValue(); //  非空
            if (v == null || "".equals(v.toString())) {
                continue;
            }
            buf.append(k);
            buf.append("=");
            buf.append(v);
            buf.append("&");
        }
        buf.append("key=" + key);
        logger.debug(buf.toString());

        String sign = EncryptUtil.md5(buf.toString()).toUpperCase();
        return sign;
    }
    
    public static String mapSign(Map<String, String> data, String key) {
        if (data == null || data.isEmpty()) {
            return null;
        }
        StringBuilder buf = new StringBuilder();
        TreeMap<String, String> map = new TreeMap<String, String>(data);

        Iterator<Map.Entry<String, String>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> entry = it.next();
            Object k = entry.getKey();
            if ("class".equals(k) || "key".equals(k) || "sign".equals(k)) {
                continue;
            }
            Object v = entry.getValue(); //  非空
            if (v == null || "".equals(v.toString())) {
                continue;
            }
            buf.append(k);
            buf.append("=");
            buf.append(v);
            buf.append("&");
        }
        buf.append("key=" + key);
        logger.debug(buf.toString());

        String sign = EncryptUtil.md5(buf.toString()).toUpperCase();
        return sign;
    }

    public static Map<String, String> sign(String jsapi_ticket, String url) {
        Map<String, String> ret = new HashMap<String, String>();
        String nonce_str = create_nonce_str();
        String timestamp = create_timestamp();
        String string1;
        String signature = "";

        //注意这里参数名必须全部小写，且必须有序
        string1 = "jsapi_ticket=" + jsapi_ticket +
                "&noncestr=" + nonce_str +
                "&timestamp=" + timestamp +
                "&url=" + url;
        System.out.println(string1);

        try
        {
            MessageDigest crypt = MessageDigest.getInstance("SHA-1");
            crypt.reset();
            crypt.update(string1.getBytes("UTF-8"));
            signature = byteToHex(crypt.digest());
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }

        ret.put("url", url);
        ret.put("jsapi_ticket", jsapi_ticket);
        ret.put("nonceStr", nonce_str);
        ret.put("timestamp", timestamp);
        ret.put("signature", signature);

        return ret;
    }

    private static String byteToHex(final byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash)
        {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }

    private static String create_nonce_str() {
        return UUID.randomUUID().toString();
    }

    private static String create_timestamp() {
        return Long.toString(System.currentTimeMillis() / 1000);
    }
}
