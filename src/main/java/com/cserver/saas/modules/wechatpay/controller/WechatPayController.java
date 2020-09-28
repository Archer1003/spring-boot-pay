package com.cserver.saas.modules.wechatpay.controller;

import com.alibaba.fastjson.JSON;
import com.cserver.saas.common.model.Product;
import com.cserver.saas.modules.wechatpay.service.IWechatPayService;
import com.cserver.saas.modules.wechatpay.util.ConfigUtil;
import com.cserver.saas.modules.wechatpay.util.JsonUtils;
import com.cserver.saas.modules.wechatpay.util.PayCommonUtil;
import com.cserver.saas.modules.wechatpay.util.XmlUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/*
 * 微信支付
 * @author lisc
 * @date 2020/9/18
 */
@Api(tags ="微信支付")
@Controller
@RequestMapping(value = "wechatPay")
public class WechatPayController extends BaseController {

	private static final Logger logger = LoggerFactory.getLogger(WechatPayController.class);

	@Autowired
	private IWechatPayService wechatPayService;
	@Value("${wexinpay.notify.url}")
	private String notify_url;

	@ApiOperation(value="二维码支付(模式二)下单并生成二维码")
	@RequestMapping(value="qcPay2",method=RequestMethod.POST)
    public String  qcPay2(HttpServletResponse response,@RequestBody String json, ModelMap map) {
		logger.info("二维码支付(模式二)");
//        {
//            orderBody:"CServer综合办公管理",
//            orderFee:0,
//            voucherId:"S20200601163941927"
//        }
		Map<String, String> paramMap = JsonUtils.toMap(json);
		String orderId=paramMap.get("orderId");
		String orderBody = paramMap.get("orderBody");
		String voucherId = paramMap.get("voucherId");
		String orderFee = paramMap.get("orderFee");

		Product product=new Product();
		//参数自定义  这只是个Demo
		product.setBody(orderBody);
		product.setOutTradeNo(voucherId);
		product.setTotalFee(orderFee);
		Map<String,String> resultMap  =  wechatPayService.weixinPay2(product);

		responseWrite(response, JSON.toJSONString(resultMap));
		return "weixinpay/qcpay";
    }
	/**
	 * 支付后台回调
	 * @Author  科帮网
	 * @param request
	 * @param response
	 * @throws Exception  void
	 * @Date	2017年7月31日
	 * 更新日志
	 * 2017年7月31日  科帮网 首次创建
	 *
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@ApiOperation(value="支付后台回调")
	@RequestMapping(value="pay",method=RequestMethod.POST)
	public void weixin_notify(HttpServletRequest request, HttpServletResponse response) throws Exception {
		// 读取参数
		InputStream inputStream = request.getInputStream();
		StringBuffer sb = new StringBuffer();
		String s;
		BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
		while ((s = in.readLine()) != null) {
			sb.append(s);
		}
		in.close();
		inputStream.close();

		// 解析xml成map
		Map<String, String> m = XmlUtil.doXMLParse(sb.toString());
		// 过滤空 设置 TreeMap
		SortedMap<Object, Object> packageParams = new TreeMap<>();
		Iterator it = m.keySet().iterator();
		while (it.hasNext()) {
			String parameter = (String) it.next();
			String parameterValue = m.get(parameter);

			String v = "";
			if (null != parameterValue) {
				v = parameterValue.trim();
			}
			packageParams.put(parameter, v);
		}
		// 账号信息
		String key = ConfigUtil.API_KEY; // key
		// 判断签名是否正确
		if (PayCommonUtil.isTenpaySign("UTF-8", packageParams, key)) {
			logger.info("微信支付成功回调");
			// ------------------------------
			// 处理业务开始
			// ------------------------------
			String resXml = "";
			if ("SUCCESS".equals((String) packageParams.get("result_code"))) {
				// 这里是支付成功
				String orderNo = (String) packageParams.get("out_trade_no");
				logger.info("微信订单号{}付款成功",orderNo);
				//这里 根据实际业务场景 做相应的操作
				// 通知微信.异步确认成功.必写.不然会一直通知后台.八次之后就认为交易失败了.
				resXml = "<xml>" + "<return_code><![CDATA[SUCCESS]]></return_code>" + "<return_msg><![CDATA[OK]]></return_msg>" + "</xml> ";
			} else {
				logger.info("支付失败,错误信息：{}",packageParams.get("err_code"));
				resXml = "<xml>" + "<return_code><![CDATA[FAIL]]></return_code>" + "<return_msg><![CDATA[报文为空]]></return_msg>" + "</xml> ";
			}
			// ------------------------------
			// 处理业务完毕
			// ------------------------------
			BufferedOutputStream out = new BufferedOutputStream(response.getOutputStream());
			out.write(resXml.getBytes());
			out.flush();
			out.close();
		} else {
			logger.info("通知签名验证失败");
		}

	}
}
