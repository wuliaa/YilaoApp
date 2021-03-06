package com.example.yilaoapp.service;

import com.example.yilaoapp.bean.User;
import com.example.yilaoapp.bean.messbean;
import com.example.yilaoapp.bean.Password;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface UserService {
    //登录
    //通过验证码
    //通过密码
    @POST("users/{mobile}/tokens")
    Call<ResponseBody>  login_password(@Path("mobile")String mobile,@Query("appid")String appid,@Query("passwd")String password);

    //更新
    @PATCH("users/{mobile}")
    Call<ResponseBody>  update(@Path("mobile")String mobile,@Query("appid")String app,@Query("token")String token,@Body messbean msee);

    //更新用户信息
    @PATCH("users/{mobile}")
    Call<ResponseBody>  updateInfo1(@Path("mobile")String mobile,@Query("appid")String app,@Query("token")String token,@Body Map<String,String> map);

    //更新密码
    @PATCH("users/{mobile}")
    Call<ResponseBody> updatePsd(@Path("mobile")String mobile,@Query("appid")String app,@Query("passwd")String password,@Body Map<String,String> map);
    //获取
//    @GET("users/{mobile}")
//    Call<User> get_message(@Path("mobile")String mobile,@Query("appid")String app,@Query("token")String token);

    //注册
    //发送验证码
    @FormUrlEncoded
    @POST("sms")
    Call<String> post_code(@FieldMap Map<String,String> message_map);//validation.py
    @GET("users/{mobile}")
    Call<User> get_acode(@Path("mobile") String mobile);//user.py

    //验证注册
    @PUT("users/{mobile}")
    Call<ResponseBody> sigin(@Path("mobile")String mobile, @Query("appid")String app, @Query("code") String code, @Body Password pp);
    //获取个人信息
    @GET("users/{mobile}")
    Call<ResponseBody> get_user(@Path("mobile")String mobile, @Query("appid")String app,@Query("token") String token);
    @GET("users/{mobile}")
    Call<ResponseBody> get_user(@Path("mobile")String mobile);

    //获得“我的任务”的所有订单
    @GET("users/{mobile}/orders")
    Call<ResponseBody> get_myorder(@Path("mobile")String mobil,@Query("token")String token,
                                   @Query("appid")String app,@Query("type")String type);

    //A为发布人，B为接单人
    //A取消订单   或者是    B接受或这拒绝A的取消订单
    @PATCH("users/{mobile}/orders/{order_id}")
    Call<ResponseBody>  Put_Fin_Cancel_Task(@Path("mobile")String mobile,@Path("order_id")String orderId,
                                   @Query("token") String token,
                                   @Query("appid")String appid,@Query("close")String close);

    //B取消接单
    @PATCH("users/{mobile}/orders/{order_id}")
    Call<ResponseBody> Get_Acc_Cancel_Order(@Path("mobile")String mobile,@Path("order_id")String orderId,
                                        @Query("token")String token,
                                        @Query("appid")String appid,@Query("receive")String receive);

}
