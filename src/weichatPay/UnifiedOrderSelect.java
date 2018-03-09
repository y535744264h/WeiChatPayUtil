package weichatPay;
/**
 * 查询订单接口
 * @author An
 *
 */
public class UnifiedOrderSelect {
	private String appid;               //公众账号ID
    private String mch_id;              //商户号
    private String transaction_id;		//微信订单号]-两者
    private String out_trade_no;        //商户订单号]-二选一
    private String nonce_str;           //随机字符串
    private String sign;                //签名
	public String getAppid() {
		return appid;
	}
	public void setAppid(String appid) {
		this.appid = appid;
	}
	public String getMch_id() {
		return mch_id;
	}
	public void setMch_id(String mch_id) {
		this.mch_id = mch_id;
	}
	public String getTransaction_id() {
		return transaction_id;
	}
	public void setTransaction_id(String transaction_id) {
		this.transaction_id = transaction_id;
	}
	public String getOut_trade_no() {
		return out_trade_no;
	}
	public void setOut_trade_no(String out_trade_no) {
		this.out_trade_no = out_trade_no;
	}
	public String getNonce_str() {
		return nonce_str;
	}
	public void setNonce_str(String nonce_str) {
		this.nonce_str = nonce_str;
	}
	public String getSign() {
		return sign;
	}
	public void setSign(String sign) {
		this.sign = sign;
	}
	
    
    
}