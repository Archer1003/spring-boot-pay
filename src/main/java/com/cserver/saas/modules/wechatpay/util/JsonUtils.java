package com.cserver.saas.modules.wechatpay.util;
  
import net.sf.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
 
public class JsonUtils {
  
            
    /** 
     * 判断是否是手机号 
     */  
    public static boolean isMobile(String tempString) throws Exception{  
    	boolean flag = false;
    	String regExp = "^((13[0-9])|(15[^4])|(18[0-9])|(17[0-9])|(19[0-9])|(147))\\d{8}$";
        Pattern p = Pattern.compile(regExp);
        Matcher m = p.matcher(tempString);
        flag =m.matches();
        return flag;  
    } 
    
    /** 
     * 替换json中某个变量的属性值
     */  
    public static String replacejson(String jsonstr,String tempKey,String tempValue){
   	 
    	tempKey="$"+tempKey;
   		 if(jsonstr.indexOf(tempKey)>-1){
   			 jsonstr= jsonstr.replace(tempKey,tempValue);
   		 }
   	return jsonstr;
    }
    
    /**
     * 
    * @Description: object转map
     */
    public static Map<String, String> toMap(Object object) {
		Map<String, String> data = new HashMap<String, String>();
		// 将json字符串转换成jsonObject
		JSONObject jsonObject = JSONObject.fromObject(object);
		Iterator ite = jsonObject.keys();
		// 遍历jsonObject数据,添加到Map对象
		while (ite.hasNext()) {
			String key = ite.next().toString();
			String value = jsonObject.get(key).toString();
			data.put(key, value);
		}
		// 或者直接将 jsonObject赋值给Map
		// data = jsonObject;
		return data;
	}
      
/*    public static void main(String[] args)    
    {  
    	boolean flag = false;
    	try {
			 flag =isMobile("18191764091");
			 System.out.println(flag);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }*/

}  