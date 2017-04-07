package com.cheikh.lazywaimai.constants;

/**
 * @author Joke
 * @version 1.0.0
 * @description
 * @email 113979462@qq.com
 * @create 2015年4月2日
 */

public final class KEY {

	public static final class CONFIG {
		public static final String MY_LONGITUDE = "my_longitude";//我的当前位置
		public static final String MY_LATITUDE = "my_latitude";  //我的当前纬度
		public static final String MY_CITY_ID = "my_cityId";
		public static final String LONGITUDE = "longitude";
		public static final String LATITUDE = "latitude";
		public static final String CITY = "city";
		public static final String ADDRESS = "address";
		public static final String CITY_ID = "cityId";
		public static final String APP_VERSION = "appVersion";
		public static final String CITY_LOADED = "cityLoaded";
		public static final String USER_ID = "user_id";
		public static final String SAVE_TRAFFIC = "saveTraffic";
		public static final String MESSAGE_PUSH = "messagePush";
		public static final String TRANSACTION_PUSH = "transactionPush";
		public static final String APP_VERSION_CODE = "version_code";
		public static String IS_LOGIN="isLogin";

		public static String WX_APPID="WX_APPID";
		public static String WX_PAY_SUCCEED="WX_PAY_SUCCEED";
		public static String WX_PAY_FAILED="WX_PAY_FAILED";
	}


	public static final class JSON{
		public static final String JSONPILECODE = "pileCode";
		public static final String JSONORDERID = "orderId";
		public static final String JSONRESULT = "result";
		public static final String JSON_STATUS = "status";
		public static final String JSONOWNERID = "ownerId";
		public static final String JSONTYPE = "type";
		public static final String JSONCHARGETYPE = "startByGprs";
		public static final String JSON_DIS_START_TIME = "disStartTime";
		public static final String JSON_FOLLOW_EACH = "followEach";
	}

	public static final class LOGIN{
		public static final String status = "login_status";
		public static final String login_id = "login_id";
		public static final String login_info = "login_info";

	}
}
