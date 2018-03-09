package weichatPay;

import com.alibaba.druid.support.logging.Log;
import com.alibaba.druid.support.logging.LogFactory;
import com.fw.base.controller.BaseController;
import com.fw.base.utils.Config;
import com.github.miemiedev.mybatis.paginator.domain.Pager;
import com.sc.dao.WechatCallbackMapper;
import com.sc.entity.PayCondition;
import com.sc.entity.Recharge;
import com.sc.entity.WechatCallback;
import com.sc.service.*;
import com.sc.webSocket.ChatServer;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.XmlFriendlyNameCoder;
import com.thoughtworks.xstream.io.xml.XppDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.websocket.Session;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/recharge")
public class RechargeController extends BaseController{
	

	
	//用于回信回调更改支付状态
	@RequestMapping("/weChatUpdRecharge")
	public void weChatUpdRecharge(HttpServletRequest request,HttpServletResponse response){
		log.info("=.=微信异步通知开始=.=");
		// 获取post传输的数据
		String request_xml = "";
		UnifiedOrderCallback unifiedOrderCallback=null;
		try {
			ServletInputStream inputStram = request.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(
					inputStram));
			String temp = "";
			while ((temp = br.readLine()) != null) {
				request_xml = request_xml + temp;
			}
			log.info("=.=微信返回结果=.=" + request_xml);
			XStream xStream = new XStream(new XppDriver(new XmlFriendlyNameCoder("_-", "_")));
	        //将请求返回的内容通过xStream转换为UnifiedOrderRespose对象
	        xStream.alias("xml", UnifiedOrderCallback.class);
	        unifiedOrderCallback = (UnifiedOrderCallback) xStream.fromXML(request_xml);
	        WechatCallback wechatCallback=new WechatCallback();
	        wechatCallback.setId(0);
	        wechatCallback.setBankType(unifiedOrderCallback.getBank_type());
	        wechatCallback.setErrCode(unifiedOrderCallback.getErr_code());
	        wechatCallback.setErrCodeDes(unifiedOrderCallback.getErr_code_des());
	        wechatCallback.setOpenid(unifiedOrderCallback.getOpenid());
	        wechatCallback.setOutTradeNo(unifiedOrderCallback.getOut_trade_no());
	        wechatCallback.setRechargeNumber(unifiedOrderCallback.getAttach().split(",")[0]);
	        wechatCallback.setResultCode(unifiedOrderCallback.getResult_code());
	        wechatCallback.setTimeEnd(unifiedOrderCallback.getTime_end());
	        wechatCallback.setTotalFee(Integer.parseInt(unifiedOrderCallback.getTotal_fee()));
	        wechatCallback.setTransactionId(unifiedOrderCallback.getTransaction_id());
	        log.info("=.=记录交易记录=.=");
	        wechatCallbackMapper.insert(wechatCallback);
	        log.info("=.=修改订单信息=.=");
	        log.info("=.=判断该订单是否被手动支付完成=.=");
	        Recharge recharge= rechargeService.selRechargeByNum(wechatCallback.getRechargeNumber());
	        if(recharge.getStatus()==0){
	        	 rechargeService.updRechargeSuccess(wechatCallback.getRechargeNumber(),unifiedOrderCallback.getTime_end());
	        }else{
	        	log.info("=.=该订单已被手动完成=.=订单号："+wechatCallback.getRechargeNumber());
	        }
	       //通知webSocket支付成功，刷新 页面通知用户您已支付成功
	        log.info("=.=开始通知页面刷新支付已成功=.=");
	        String websocketKey=unifiedOrderCallback.getAttach().split(",")[1];
	        Session session=(Session) ChatServer.map.get(websocketKey);
	        System.out.println("session==========="+session);
	        session.getAsyncRemote().sendText("=.=");
	        ChatServer.map.remove(websocketKey);
		} catch (Exception e) {
			log.info("=.=异常了=.="+e.getMessage());
		} finally{
			log.info("=.=告诉微信，我已经知道了=.=");
			try {
				response.getWriter().write("<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	@ResponseBody
	@RequestMapping("/refreshRecharge")
	public String refreshRecharge(String reNumber){
		log.info("=.=异步请求微信查询订单接口开始=.=");
		//创建查询订单表单
		String orderInfo=weChatPay.createOrderSelect(reNumber);
		//请求查询
		UnifiedOrderSelectRespose unifiedOrderSelectRespose =WeChatPay.httpSelectOrder(orderInfo);
		if(unifiedOrderSelectRespose!=null){
			log.info("=.=请求微信查询订单接口完成=.=");
			log.info("订单号:"+unifiedOrderSelectRespose.getOut_trade_no()+"状态:"+unifiedOrderSelectRespose.getTrade_state());
			log.info("SUCCESS—支付成功REFUND—转入退款NOTPAY—未支付CLOSED—已关闭REVOKED—已撤销（刷卡支付）USERPAYING--用户支付中PAYERROR--支付失败(其他原因，如银行返回失败)支付状态机请见下单API页面");
			if(unifiedOrderSelectRespose.getTrade_state().equals("SUCCESS")){
				WechatCallback wechatCallback=new WechatCallback();
		        wechatCallback.setId(0);
		        wechatCallback.setBankType(unifiedOrderSelectRespose.getBank_type());
		        wechatCallback.setErrCode(unifiedOrderSelectRespose.getReturn_code());
		        wechatCallback.setErrCodeDes(unifiedOrderSelectRespose.getReturn_msg());
		        wechatCallback.setOpenid(unifiedOrderSelectRespose.getOpenid());
		        wechatCallback.setOutTradeNo(unifiedOrderSelectRespose.getOut_trade_no());
		        wechatCallback.setRechargeNumber(unifiedOrderSelectRespose.getAttach().split(",")[0]);
		        wechatCallback.setResultCode(unifiedOrderSelectRespose.getResult_code());
		        wechatCallback.setTimeEnd(unifiedOrderSelectRespose.getTime_end());
		        wechatCallback.setTotalFee(Integer.parseInt(unifiedOrderSelectRespose.getTotal_fee()));
		        wechatCallback.setTransactionId(unifiedOrderSelectRespose.getTransaction_id());
		        log.info("=.=记录交易记录=.=");
		        wechatCallbackMapper.insert(wechatCallback);
		        log.info("=.=修改订单信息=.=");
				rechargeService.updRechargeSuccess(unifiedOrderSelectRespose.getOut_trade_no(),unifiedOrderSelectRespose.getTime_end());
			}
			String msg="";
			if(unifiedOrderSelectRespose.getTrade_state().equals("SUCCESS")){
				msg="支付成功";
			}else if(unifiedOrderSelectRespose.getTrade_state().equals("REFUND")){
				msg="转入退款";
			}else if(unifiedOrderSelectRespose.getTrade_state().equals("NOTPAY")){
				msg="未支付";
			}else if(unifiedOrderSelectRespose.getTrade_state().equals("CLOSED")){
				msg="已关闭";
			}else if(unifiedOrderSelectRespose.getTrade_state().equals("REVOKED")){
				msg="已撤销";
			}else if(unifiedOrderSelectRespose.getTrade_state().equals("USERPAYING")){
				msg="用户支付中";
			}else if(unifiedOrderSelectRespose.getTrade_state().equals("PAYERROR")){
				msg="支付失败";
			}
			return msg;
		}
		return "异常";
	}
	
	public String weiChatPayIng(String orderNum,String key){
		String body=Config.getStringValue("body");
		log.info("=.=充值body="+body);
		Recharge recharge=rechargeService.selRechargeByNum(orderNum);
		if(recharge.getStatus()==1){
			log.info("该订单已付款");
			return "已付款";
		}
		//生成订单
	    String orderInfo =weChatPay.createOrderInfo(body, orderNum,(float) 0.01,key,"NATIVE",null);
	    //调统一下单API
	    String code_url = WeChatPay.httpOrder(orderInfo).getCode_url();
	    //将返回预支付交易链接（code_url）生成二维码图片
		return code_url;
	}
	
	@ResponseBody
	@RequestMapping("/weiChatPayJSAPI")
	public String weiChatPayJSAPI(String orderNum,String openid){
		String body=Config.getStringValue("body");
		log.info("=.=充值body="+body);
		Recharge recharge=rechargeService.selRechargeByNum(orderNum);
		if(recharge.getStatus()==1){
			log.info("该订单已付款");
			return "已付款";
		}
		//生成订单
	    String orderInfo =weChatPay.createOrderInfo(body, orderNum,(float) 0.01,null,"JSAPI",openid);
	    //调统一下单API
	    String preparyId = WeChatPay.httpOrder(orderInfo).getPrepay_id();
	    //将返回预支付交易链接（code_url）生成二维码图片
		return preparyId;
	}
	
	private String rechargeNumber() {
		Date date=new Date();
		DateFormat format = new SimpleDateFormat("yyyyMMddHHmmss"); 
		String number=format.format(date)+WeChatPayUtil.getRandomString(4);
		return number;
	}
	
}
