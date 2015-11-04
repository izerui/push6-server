#<center>微信支付文档</center>


##Message.proto

    message Request {
        required string partner = 1; //合作身份者ID
        required string command = 2; //要执行的命令
        required string sign = 3; //将message签名后的值
        required string message = 4;  //传输的消息主体
    }

    message Response {
        required string command = 1; //要执行的命令
        required bool success = 2;  //执行状态. true OR false
        required string sign = 3; //将message签名后的值
        required string message = 4;  //返回的消息提示
    }

### Request描述
* partner 合作身份者是唯一，在每次登陆后，会随机下发一个partner，在整个长连接生命周期内有效。如连接断开，则partner无效需重新获取。
* command 要执行的命令，主要包含：
 
		ordersCommand 查找订单列表  	
		unifiedorderCommand 发起统一支付  
		micropayCommand 发起刷卡支付  
		orderDetailCommand 订单详情  
		refundCommand 发起退款,如果不需要审批则直接发起微信退款,并且返回退款单号,否则发起审批,并且返回null  
		loginCommand 验证登录用户和密码，并返回partner  
		updateGoodsTagCommand 给订单添加保证金单号
* sign 签名，保证数据完整性。数据签名整体操作如下：

		1. 每个客户端均会保存一个partnerKey，由客户端与服务器端约束好。
		2. 每次传输消息(Request)时，需要将message与本地partnerKey进行MD5签名
* message 消息传输体
    
    	ordersCommand格式如下:
        {
             "startDate": "2014-08-08", //开始时间
             "endDate": "2014-08-08",   //结束时间
             "pageNum": 1,              //当前页码 从0开始
             "pageSize": 10,             //每页显示条数
             "workKey":"工作密钥"        //工作密钥
         }
         
    	unifiedorderCommand格式如下:
        {
            "totalFee": 100,       //交易金额 单位“分”
            "body": "sdfsdfsdf",    //商品描述
            "workKey":"工作密钥"        //工作密钥
        }
    	
    	micropayCommand格式如下:
        {
            "totalFee": 100,        //交易金额  单位“分”
            "body": "sdfsdfsdf",    //商品描述
            "authCode": "2134211",   //终端通过摄像头扫客户微信上支付二维码产生的
            "workKey":"工作密钥"        //工作密钥
        }
    	
    	updateGoodsTagCommand格式如下:
        {
            "outTradeNo": "1232323", //订单号
            "goodsTag": "sdfsdfsdf",  //保证金单号
            "workKey":"工作密钥"        //工作密钥
        }
        
    	orderDetailCommand格式如下:
        {
            "outTradeNo": "1232323", //订单号
            "workKey":"工作密钥"        //工作密钥
        }
        
    	refundCommand格式如下:
        {
            "userId": "1232323",   //用户ID
            "password": "sdfsdf",  //用户密码	
            "outTradeNo": "sdfsdf",  //退款单据号
            "refundFee": 11,    //退款金额 单位分
            "workKey":"工作密钥"        //工作密钥
        }

### Response描述
* command 要执行的命令 请参考request中command
* success true 成功 false 失败
* sign 签名，保证数据完整性。请参考request中sign
* message 消息传输体  
  
		unifiedorderCommand 格式如下:
        {
           "outTradeNo": "sdfsdf",       //订单号
           "payUrl": "http://sss.xxx",   //二维码链接
           "timeStart" : "1124343434"    //开始时间，时间格式为time.getTime()(Long类型) 
        }
        
        micropayCommand格式如下:
        {
			"outTradeNo": "sdfsdf",       //订单号
			"stateName":"发起小额支付成功，等待通知！" 
		}
		
		ordersCommand格式如下:
		{
       		"count": 12, //总记录数
       		"resultList": "",   //结果集，内容为json格式参数，类似：
       		"pageNum": 1,              //当前页码 从0开始
      		"pageSize": 10,             //每页显示条数
      		"totalFee":12,				//交易总金额
      		"refundFee":123            //退款总金额
   		}
   		备注：resultList格式如下：
   		[{
    		"totalFee": 1212，//总金额
   		 	"timeStart": "2014-08-08 12:08:08",
    		"timeEnd":"2014-08-08 12:08:08",
    		"outTradeNo": "123232323", //商户订单号
    		"refundFee": 11,  //历史退款总金额
    		"stateName":"支付状态",  //支付状态
    		"body":"商品描述"       //商品描述
		}]
		
		orderDetailCommand格式如下：
		{
    		"totalFee": 1212，//总金额
   		 	"timeStart": "2014-08-08 12:08:08",
    		"timeEnd":"2014-08-08 12:08:08",
    		"outTradeNo": "123232323", //商户订单号
    		"refundFee": 11,  //历史退款总金额
    		"stateName":"支付状态",  //支付状态
    		"body":"商品描述",       //商品描述
    		"bankType":"支付的银行代码",  //银行类型
    		"openId":"微信openId",    //微信openId
    		"tradeType":"native"   //交易类型 jsapi、native、micropay、 app
		}
		
		refundCommand格式如下：
		{
			"preRefundOrder":"退款单据号"
		}
		
		updateGoodsTagCommand格式如下：
		{
			"result":"1"  //结果 1代表成功
		}
		
		
   		
		
        
           	
    	    
    
    
    
    





