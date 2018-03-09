package weichatPay;

import com.alibaba.druid.support.logging.Log;
import com.alibaba.druid.support.logging.LogFactory;
import com.fw.base.utils.Config;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.XmlFriendlyNameCoder;
import com.thoughtworks.xstream.io.xml.XppDriver;
import org.springframework.stereotype.Component;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;

/**
 * 微信支付
 *
 */

@Component
public class WeChatPay {
	
	static Log log =LogFactory.getLog(WeChatPay.class);
	
	//同意下单API
	public static final String PAYURL="https://api.mch.weixin.qq.com/pay/unifiedorder";
	//查询订单API
	public static final String SELECT="https://api.mch.weixin.qq.com/pay/orderquery";
	
	
	public static void main(String[] args) {
		
	}
	/**
	 * 生成订单方法
	 * @param body 订单详情
	 * @param orderId 订单号
	 * @param money 价格/元
	 * @param ip 用户端IP
	 * @return
	 */
	public String createOrderInfo(String body,String orderId,float money,String key,String tradeType,String openid) {
		String ip="";
		//生成订单对象
		String resule="";
		try {
			ip = InetAddress.getLocalHost().getHostAddress().toString();
			log.info("=.=生成订单开始（body="+body+",orderId="+orderId+",money="+money+",ip="+ip+"）=.=");
			String appid=Config.getStringValue("appID");
			String mchId=Config.getStringValue("mch_id");
			UnifiedOrderRequestExt unifiedOrderRequest = new UnifiedOrderRequestExt();
			unifiedOrderRequest.setAppid(appid);//公众账号ID
			unifiedOrderRequest.setMch_id(mchId);//商户号
			if(openid!=null){
				unifiedOrderRequest.setOpenid(openid);
			}
			unifiedOrderRequest.setAttach(orderId+","+key);//自定义参数 通知的时候修改成功和支付时间
			unifiedOrderRequest.setNonce_str(WeChatPayUtil.getRandomString(11));//随机字符串
			unifiedOrderRequest.setBody(body);//商品描述
			unifiedOrderRequest.setOut_trade_no(orderId);//商户订单号
			Float f = new Float(money*100);
			int money1 = f.intValue();
			unifiedOrderRequest.setTotal_fee(money1+""); //单位分所以成100
			unifiedOrderRequest.setSpbill_create_ip(ip);//终端IP
			unifiedOrderRequest.setNotify_url(Config.getStringValue("notifyUrl"));//通知地址
			unifiedOrderRequest.setTrade_type(tradeType);//JSAPI--公众号支付、NATIVE--原生扫码支付、APP--app支付
			unifiedOrderRequest.setSign(WeChatPayUtil.createSign(unifiedOrderRequest));//签名
			//将订单对象转为xml格式
			XStream xStream = new XStream(new XppDriver(new XmlFriendlyNameCoder("_-", "_")));
			xStream.alias("xml", UnifiedOrderRequest.class);//根元素名需要是xml
			resule = xStream.toXML(unifiedOrderRequest);
			System.out.println("XML="+resule);
		} catch (Exception e) {
			log.info("=.=创建订单异常"+e.getMessage());
			e.printStackTrace();
		}
	    log.info("=.=生成订单结束=.=");
	    return resule;
	}
	/**
	 * 统一下单
	 * @param orderInfo 订单信息
	 * @return
	 */
	public static UnifiedOrderRespose httpOrder(String orderInfo) {
	    try {
	    	log.info("=.=同意下单开始=.=");
	        HttpURLConnection conn = (HttpURLConnection) new URL(PAYURL).openConnection();
	        //加入数据  
	        conn.setRequestMethod("POST");  
	        conn.setDoOutput(true);  
	        BufferedOutputStream buffOutStr = new BufferedOutputStream(conn.getOutputStream());  
	        buffOutStr.write(orderInfo.getBytes());
	        buffOutStr.flush();  
	        buffOutStr.close();  
	        //获取输入流  
	        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));  
	        String line = null;  
	        StringBuffer sb = new StringBuffer();  
	        while((line = reader.readLine())!= null){  
	            sb.append(line);  
	        }  
	        XStream xStream = new XStream(new XppDriver(new XmlFriendlyNameCoder("_-", "_")));
	        //将请求返回的内容通过xStream转换为UnifiedOrderRespose对象
	        xStream.alias("xml", UnifiedOrderRespose.class);
	        UnifiedOrderRespose unifiedOrderRespose = (UnifiedOrderRespose) xStream.fromXML(sb.toString());
	        //根据微信文档return_code 和result_code都为SUCCESS的时候才会返回code_url
	        if(null!=unifiedOrderRespose  && "SUCCESS".equals(unifiedOrderRespose.getReturn_code())  && "SUCCESS".equals(unifiedOrderRespose.getResult_code())){
	        	log.info("=.=同意下单成功=.=");
	        	return unifiedOrderRespose;
	        }else{
	        	log.info("=.=调用微信参数错误=.="+unifiedOrderRespose.getReturn_msg()+"code"+unifiedOrderRespose.getReturn_code());
	            return null;
	        }
	    } catch (Exception e) {
	    	log.info("=.=调用统一下单接口异常"+e.getLocalizedMessage());
	        e.printStackTrace();
	    }
	    return null;
	}
	
	/**
	 * 创建查询订单
	 * @param outTradeNo
	 * @return
	 */
	public String createOrderSelect(String outTradeNo){
		//String ip="";
		//生成订单对象
		String resule="";
		try {
			String appid=Config.getStringValue("appID");
			String mchId=Config.getStringValue("mch_id");
			UnifiedOrderSelect unifiedOrderRequest = new UnifiedOrderSelect();
			unifiedOrderRequest.setAppid(appid);//公众账号ID
			unifiedOrderRequest.setMch_id(mchId);//商户号
			unifiedOrderRequest.setNonce_str(WeChatPayUtil.getRandomString(11));//随机字符串
			unifiedOrderRequest.setOut_trade_no(outTradeNo);//商户订单号
			unifiedOrderRequest.setSign(WeChatPayUtil.createSign(unifiedOrderRequest));//签名
			//将订单对象转为xml格式
			XStream xStream = new XStream(new XppDriver(new XmlFriendlyNameCoder("_-", "_")));
			xStream.alias("xml", UnifiedOrderRequest.class);//根元素名需要是xml
			resule = xStream.toXML(unifiedOrderRequest);
			System.out.println("XML="+resule);
		} catch (Exception e) {
			log.info("=.=创建订单异常"+e.getMessage());
			e.printStackTrace();
		}
	    log.info("=.=生成订单结束=.=");
	    return resule;
	}
	
	/**
	 * 请求查询订单
	 * @param orderInfo
	 */
	public static UnifiedOrderSelectRespose httpSelectOrder(String orderInfo){
		try {
	    	log.info("=.=查询订单开始=.=");
	        HttpURLConnection conn = (HttpURLConnection) new URL(SELECT).openConnection();
	        log.info("SELECT"+SELECT);
	        //加入数据  
	        conn.setRequestMethod("POST");  
	        conn.setDoOutput(true);  
	        BufferedOutputStream buffOutStr = new BufferedOutputStream(conn.getOutputStream());  
	        buffOutStr.write(orderInfo.getBytes());
	        buffOutStr.flush();  
	        buffOutStr.close();  
	        //获取输入流  
	        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));  
	        String line = null;  
	        StringBuffer sb = new StringBuffer();  
	        while((line = reader.readLine())!= null){  
	            sb.append(line);  
	        }  
	        XStream xStream = new XStream(new XppDriver(new XmlFriendlyNameCoder("_-", "_")));
	        //将请求返回的内容通过xStream转换为UnifiedOrderRespose对象
	        xStream.alias("xml", UnifiedOrderSelectRespose.class);
	        log.info("xml="+sb.toString());
	        UnifiedOrderSelectRespose unifiedOrderSelectRespose = (UnifiedOrderSelectRespose) xStream.fromXML(sb.toString());
	        //根据微信文档return_code 和result_code都为SUCCESS的时候才会返回code_url
	        if(null!=unifiedOrderSelectRespose  && "SUCCESS".equals(unifiedOrderSelectRespose.getReturn_code())  && "SUCCESS".equals(unifiedOrderSelectRespose.getResult_code())){
	        	log.info("=.=查询订单成功=.=");
	        	return unifiedOrderSelectRespose;
	        }else{
	        	log.info("=.=调用微信参数错误=.="+unifiedOrderSelectRespose.getReturn_msg()+"code"+unifiedOrderSelectRespose.getReturn_code());
	            return null;
	        }
	    } catch (Exception e) {
	    	log.info("=.=调用统一下单接口异常"+e.getLocalizedMessage());
	        e.printStackTrace();
	    }
	    return null;
	}
}
