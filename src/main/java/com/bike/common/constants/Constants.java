package com.bike.common.constants;

/**
 * 常量包
 */
public class Constants {

    //1、定义状态码
    public static final int RESP_STATUS_OK = 200;
    public static final int RESP_STATUS_NOAUTH = 401;
    public static final int RESP_STATUS_INTERNAL_ERROR = 500;
    public static final int RESP_STATUS_BADREQUEST = 400;

    //2、用户token
    public static final String REQUEST_TOKEN_KEY = "user-token";

    //3、客户端app版本
    public static final String REQUEST_VERSION_KEY = "version";

    //4、秒嘀SMS(sid,token,url,tplid),自己秒嘀账号配置
    public static final String MDSMS_ACCOUNT_SID = "f5709500ba8a570f5c5d6fc3651b9e92";
    public static final String MDSMS_AUTH_TOKEN = "f1f38c16eb873e072a5a849afd5880af";
    public static final String MDSMS_REST_URL = "https://openapi.miaodiyun.com/distributor/sendSMS";
    public static final String MDSMS_VERCODE_TPLID = "681112";//模板id

    //5、七牛云,自己七牛云账号配置(路径:域名+空间名+文件名)
    public static final String QINIU_ACCESS_KEY = "BpdL_AO-GzWSgQeqRIIftstbW8XStH63_nSp-4pz";
    public static final String QINIU_SECRET_KEY = "BIONwCJnoDp28HxJP4TxDbWihrf19YDJABiquB_G";
    public static final String QINIU_HEAD_IMG_BUCKET_NAME = "duyangbike";//空间名称(文件夹名称)
    public static final String QINIU_HEAD_IMG_BUCKET_URL = "qd5j73pud.bkt.clouddn.com";//域名

    //6、百度云推送
    public static final String BAIDU_YUN_PUSH_API_KEY = "";
    public static final String BAIDU_YUN_PUSH_SECRET_KEY = "";
    public static final String CHANNEL_REST_URL = "api.push.baidu.com";


    //=================================================================


    /**
     * 客户端平台 android/ios
     **/
    public static final String REQUEST_PLATFORM_KEY = "platform";

    public static final String REQUEST_TYPE_KEY = "type";



}
