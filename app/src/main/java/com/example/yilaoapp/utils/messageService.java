package com.example.yilaoapp.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import com.example.yilaoapp.R;
import com.example.yilaoapp.bean.All_orders;
import com.example.yilaoapp.bean.Mess;
import com.example.yilaoapp.chat.activity.ChatActivity;
import com.example.yilaoapp.service.RetrofitUser;
import com.example.yilaoapp.service.chat_service;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static org.greenrobot.eventbus.EventBus.TAG;

public class messageService extends Service {
    //获取消息线程
    private MessageThread messageThread = null;

    //点击查看
    private Intent messageIntent = null;
    private PendingIntent messagePendingIntent = null;

    //通知栏消息
    private int messageNotificationID = 1000;
    private Notification messageNotification = null;
    private NotificationManager messageNotificatioManager = null;

    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //初始化
        messageNotification = new Notification();
        messageNotificatioManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        //开启线程
        messageThread = new MessageThread();
        messageThread.isRunning = true;
        messageThread.start();
        System.out.println("start");
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 从服务器端获取消息
     *
     */
    class MessageThread extends Thread{
        //运行状态，下一步骤有大用
        public boolean isRunning = true;
        public void run() {
            while(isRunning){
                try {
                    //休息3秒
                    Thread.sleep(3000);
                    //获取服务器消息
                    String serverMessage = getServerMessage();
                    if(serverMessage!=null&&!"".equals(serverMessage)){
                       /* Notification.Builder builder = new Notification.Builder(getApplicationContext());//新建Notification.Builder对象
                        //PendingIntent点击通知后所跳转的页面
                        builder.setContentTitle("Bmob Test");*/
                        chat_service ch=new RetrofitUser().get(getApplicationContext()).create(chat_service.class);
                        SharedPreferences pre=getSharedPreferences("login", Context.MODE_PRIVATE);
                        String cur_time=pre.getString("time","");
                        long t=0;
                        if(cur_time.equals("")){
                               t=-3;
                        }
                        else{
                            t=Integer.parseInt(cur_time)-System.currentTimeMillis()/1000;
                        }
                        String token=pre.getString("token","");
                        Call<ResponseBody> ch_back=ch.get_message("13060887368","0",token,"df3b72a07a0a4fa1854a48b543690eab",String.valueOf(t));
                        ch_back.enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                try {
                                    List<Mess> ms=new LinkedList<>();
                                    String str="";
                                    str=response.body().string();
                                    Gson gson = new Gson();
                                    Type type = new TypeToken<List<Mess>>() {
                                    }.getType();
                                    ms = gson.fromJson(str, type);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {

                            }
                        });
                       /* if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                            messageIntent = new Intent(getApplicationContext(),ChatActivity.class);//跳转
                            messagePendingIntent = PendingIntent.getActivity(getApplicationContext(),0,messageIntent,0);
                            String id = "channelId";
                            String name = "channelName";
                            Bundle bundle=new Bundle();
                            bundle.putString("mobile","13412101248");
                            bundle.putString("uuid","uuid");
                            bundle.putString("id_name","nickName");
                            messageIntent.putExtra("bundle",bundle);
                            NotificationChannel channel = new NotificationChannel(id,name,NotificationManager.IMPORTANCE_LOW);
                            messageNotificatioManager.createNotificationChannel(channel);
                            messageNotification = new Notification.Builder(getApplicationContext())
                                    .setChannelId(id)
                                    .setContentTitle("YilaoServer")
                                    .setContentText("This is content text O")
                                    .setWhen(System.currentTimeMillis())
                                    .setSmallIcon(R.mipmap.ic_launcher)
                                    .setContentIntent(messagePendingIntent)
                                    .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher))
                                    .build();
                        }else{
                            messageIntent = new Intent(getApplicationContext(),ChatActivity.class);//跳转
                            Bundle bundle=new Bundle();
                            bundle.putString("mobile","13412101248");
                            bundle.putString("uuid","uuid");
                            bundle.putString("id_name","nickName");
                            messageIntent.putExtra("bundle",bundle);
                            messagePendingIntent = PendingIntent.getActivity(getApplicationContext(),0,messageIntent,0);
                            messageNotification = new NotificationCompat.Builder(getApplicationContext())
                                    .setContentTitle("YilaoServer")
                                    .setContentText("This is content text")
                                    .setWhen(System.currentTimeMillis())
                                    .setContentIntent(messagePendingIntent)
                                    .setSmallIcon(R.mipmap.ic_launcher)
                                    .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher))
                                    .build();
                        }
                        messageNotification.flags = Notification.FLAG_AUTO_CANCEL;
                        messageNotificatioManager.notify(messageNotificationID,messageNotification);
                        //每次通知完，通知ID递增一下，避免消息覆盖掉
                        messageNotificationID++;*/
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 这里以此方法为服务器Demo，仅作示例
     * @return 返回服务器要推送的消息，否则如果为空的话，不推送
     */
    public String getServerMessage(){
        return "YES!";
    }
    @Override
    public void onDestroy() {
        System.exit(0);
        //或者，二选一，推荐使用System.exit(0)，这样进程退出的更干净
        //messageThread.isRunning = false;
        super.onDestroy();
    }
}