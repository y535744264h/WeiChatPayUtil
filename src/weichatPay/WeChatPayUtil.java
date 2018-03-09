package weichatPay;

import com.fw.base.utils.Config;

import java.util.*;
import java.util.Map.Entry;

/**
 * 微信支付工具类
 * @author An
 *
 */
public class WeChatPayUtil {
	/**
	 * 生成随机字符串
	 * @param length 长度
	 * @return
	 */
    public static String getRandomString(int length) {  
           String base = "abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";         
           Random random = new Random();         
           StringBuffer sb = new StringBuffer();      
           for (int i = 0; i < length; i++) {         
               int number = random.nextInt(base.length());         
               sb.append(base.charAt(number));         
           }         
           return sb.toString();         
     }
    /**
     * 统一下单生成签名
     * @param unifiedOrderRequest
     * @return
     */
    public static String createSign(UnifiedOrderRequestExt unifiedOrderRequest) {
        //根据规则创建可排序的map集合
        SortedMap<String, String> packageParams = new TreeMap<String, String>();
        packageParams.put("appid", unifiedOrderRequest.getAppid());
        packageParams.put("body", unifiedOrderRequest.getBody());
        packageParams.put("mch_id", unifiedOrderRequest.getMch_id());
        packageParams.put("nonce_str", unifiedOrderRequest.getNonce_str());
        packageParams.put("notify_url", unifiedOrderRequest.getNotify_url());
        packageParams.put("out_trade_no", unifiedOrderRequest.getOut_trade_no());
        packageParams.put("spbill_create_ip", unifiedOrderRequest.getSpbill_create_ip());
        packageParams.put("trade_type", unifiedOrderRequest.getTrade_type());
        packageParams.put("total_fee", unifiedOrderRequest.getTotal_fee());
        packageParams.put("attach", unifiedOrderRequest.getAttach());
        if(unifiedOrderRequest.getOpenid()!=null){
        	packageParams.put("openid",unifiedOrderRequest.getOpenid());
        }
        StringBuffer sb = new StringBuffer();
        Set<Entry<String, String>> es = packageParams.entrySet();//字典序
        Iterator<Entry<String, String>> it = es.iterator();
        while (it.hasNext()) {
        	Entry<String, String> entry = (Entry<String, String>) it.next();
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            //为空不参与签名、参数名区分大小写
            if (null != value && !"".equals(value) && !"sign".equals(key)
                    && !"key".equals(key)) {
                sb.append(key + "=" + value + "&");
            }
        }
        //微信商户平台(pay.weixin.qq.com)-->账户设置-->API安全-->密钥设置
        String key=Config.getStringValue("key");
        System.out.println(packageParams);
        sb.append("key=" +key);
        String sign = WeChatMD5.MD5Encode(sb.toString(), "utf-8") .toUpperCase();//MD5加密
        System.out.println(sign);
        return sign;
    }
   
    public static String createSign(UnifiedOrderSelect unifiedOrderRequest) {
    	//根据规则创建可排序的map集合
        SortedMap<String, String> packageParams = new TreeMap<String, String>();
        packageParams.put("appid", unifiedOrderRequest.getAppid());
        packageParams.put("mch_id", unifiedOrderRequest.getMch_id());
        packageParams.put("out_trade_no", unifiedOrderRequest.getOut_trade_no());
        packageParams.put("nonce_str", unifiedOrderRequest.getNonce_str());
        StringBuffer sb = new StringBuffer();
        Set<Entry<String, String>> es = packageParams.entrySet();//字典序
        Iterator<Entry<String, String>> it = es.iterator();
        while (it.hasNext()) {
        	Entry<String, String> entry = (Entry<String, String>) it.next();
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            //为空不参与签名、参数名区分大小写
            if (null != value && !"".equals(value) && !"sign".equals(key)
                    && !"key".equals(key)) {
                sb.append(key + "=" + value + "&");
            }
        }
        //微信商户平台(pay.weixin.qq.com)-->账户设置-->API安全-->密钥设置
        String key=Config.getStringValue("key");
        System.out.println(packageParams);
        sb.append("key=" +key);
        String sign = WeChatMD5.MD5Encode(sb.toString(), "utf-8") .toUpperCase();//MD5加密
        System.out.println(sign);
        return sign;
	}
}
