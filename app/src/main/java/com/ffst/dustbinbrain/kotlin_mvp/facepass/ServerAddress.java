package com.ffst.dustbinbrain.kotlin_mvp.facepass;


/**
 * 服务器地址
 * */
public class ServerAddress {
    //  ip 地址
    public final static String IP = "https://ffadmin.fenfeneco.com/";
    //  微信扫码登陆
    public final static String LOGIN = IP + "index.php/index/index/weixinRegister?device_id=";

    //  人脸图片上传到云服务器
    public final static String FACE_AND_USER_ID_UPLOAD = IP + "api/other/userFaceRegister";

    public final static String FILE_UPLOAD = IP + "api/Other/userUpload";

    //  信息上报
    public final static String MESSAGE_UPLOAD = IP + "";

    //  用户积分增长
    public final static String ADD_USER_SCORE = IP + "";

    //  设备注册
    public final static String DEVICE_REGISTER = IP + "api/Other/equipmentRegister";


    //  获取商品备选列表
    public final static String GET_GOODS_POS = IP + "api/other/getGoodsPos";


    //  获取垃圾箱配置
    public final static String GET_DUSTBIN_CONFIG = IP + "api/Android/equipmentActivity";

    //  注册TCP
    public final static String REGISTER_TCP = IP + "api/Android/equipmentTcpBind";

    //  管理员登陆
    public final static String ADMIN_LOGIN = IP + "api/Android/equipmentMangerLogin";

    //  发送手机验证码
    public final static String SEND_SMS = IP + "api/Other/sendSms";

    //  验证验证码是否正确
    public final static String PHONE_CODE_VERIFY = IP + "api/Android/phoneCodeVerify";

    //  错误上报
    public final static String ERROR_UPLOAD = IP + "api/Android/equipmentErrLogPost";


    //  垃圾投递记录
    public final static String DUSTBIN_RECORD = IP + "api/Android/throwRubbishPost";

    //  提交垃圾图片
    public final static String RUBBISH_IMAGE_POST = IP + "api/Android/rubbishImagePost";

    //  状态上传
    public final static String STATE_UPLOAD = IP + "api/Android/binStatusPost";

    //  ic 卡    二维码
    public final static String IC_BING = IP + "index.php/index/index/nfcActivity";

    //  ic 卡号查询
    public final static String IC_GetNfcUserBean = IP + "api/Android/getNfcUser";


    //  更新设备经纬度
    public final static String UPDATE_DEVICE_LOCATION = IP + "api/Android/equipmentLocationPost";


    //  获取设备工作时间
    public final static String GET_BINS_WORK_TIME = IP + "api/Android/getBinsWorkTime";

    //  传 id 获取用户头像
    public final static String GET_USER_ICON = IP + "api/Bmxproxy/getUserAvater?user_id=";


    //  手机号码登录
    public final static String PHONE_LOGIN = IP + "api/Android/phoneLogin";

    //  查询二维码
    public final static String QUERY_QR_CODE = IP + "api/Android/scanLogin";

    //  人脸添加成功
    public final static String POST_FACE_REGISTER_SUCCESS_LOG = IP + "api/android/postFaceRegisterSuccessLog";


    //  清除服务器下此设备的人脸绑定
    public final static String CLEAR_DEVICE_FACE_BING = IP + "api/android/clearFacePostSuccessLog";

}
