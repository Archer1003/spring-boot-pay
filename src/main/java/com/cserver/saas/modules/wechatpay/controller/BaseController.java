package com.cserver.saas.modules.wechatpay.controller;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author adminitrator
 * @date 2020/9/17 10:49
 */
public class BaseController {

    public void responseWrite(HttpServletResponse response, String str){
        try {
            response.setCharacterEncoding("utf-8");
            response.getWriter().write(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
