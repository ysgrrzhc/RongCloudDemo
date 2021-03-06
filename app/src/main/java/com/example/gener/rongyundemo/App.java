package com.example.gener.rongyundemo;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.util.Log;

import io.rong.imageloader.core.DisplayImageOptions;
import io.rong.imageloader.core.display.FadeInBitmapDisplayer;
import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;
import io.rong.push.RongPushClient;

public class App extends Application {
    private static DisplayImageOptions options;

    @Override
    public void onCreate() {
        super.onCreate();
        RongIM.init(this);
        initRongYun();
        SealAppContext.init(this);
        options = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(com.example.gener.rongyundemo.R.drawable.de_default_portrait)
                .showImageOnFail(com.example.gener.rongyundemo.R.drawable.de_default_portrait)
                .showImageOnLoading(com.example.gener.rongyundemo.R.drawable.de_default_portrait)
                .displayer(new FadeInBitmapDisplayer(300))
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();

    }
    private  String tokenTwo = "AvDzvgR9NsUd2/4q0rY0zs/hSsm8Khf4neAFy/0kkd5MN+ClwEjwIxnNS3oOOLvQzR/VaRjvT8/C6qy/tB9H9z2T5bm4jOkQ";
    private  String tokenOne = "XVkW2sTA/aNVlvSFfaNU5s/hSsm8Khf4neAFy/0kkd5MN+ClwEjwI3r9zdarJ3dGPjuedmzXrVpmVYvBnsKFLYFwo9+SjawN";

    /**
     * 初始化融云
     */
    private void initRongYun() {
        if (getApplicationInfo().packageName.equals(getCurProcessName(getApplicationContext()))) {
            // 小米推送，必须在init（）方法前
            RongPushClient.registerMiPush(this, "2882303761517516557", "5761751652557");
            RongIM.setServerInfo("nav.cn.ronghub.com", "up.qbox.me");
            // 初始化
            RongIM.init(this);
            // 设置聊天相关监听
//            AppContext.init(this);
            // 连接融云服务器
//            RongYUtils.connect(this);
            connect(tokenTwo);
//            connect(tokenOne);
        }
    }


    /**
     * 获取当前进程名
     * @param context
     * @return
     */
    public static String getCurProcessName(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : activityManager.getRunningAppProcesses()) {
            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return null;
    }

    public static DisplayImageOptions getOptions() {
        return options;
    }

    private void connect(String token) {

//        if (getApplicationInfo().packageName.equals(App.getCurProcessName(getApplicationContext()))) {

        RongIM.connect(token, new RongIMClient.ConnectCallback() {

            /**
             * Token 错误。可以从下面两点检查 1.  Token 是否过期，如果过期您需要向 App Server 重新请求一个新的 Token
             *                  2.  token 对应的 appKey 和工程里设置的 appKey 是否一致
             */
            @Override
            public void onTokenIncorrect() {

            }

            /**
             * 连接融云成功
             * @param userid 当前 token 对应的用户 id
             */
            @Override
            public void onSuccess(String userid) {
                Log.d("LoginActivity", "--onSuccess" + userid);
//                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
//                    finish();
            }

            /**
             * 连接融云失败
             * @param errorCode 错误码，可到官网 查看错误码对应的注释
             */
            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {

                Log.d("LoginActivity",  errorCode.getMessage());
            }
        });
//        }
    }


}
