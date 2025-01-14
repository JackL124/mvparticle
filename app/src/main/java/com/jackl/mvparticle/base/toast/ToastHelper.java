package com.jackl.mvparticle.base.toast;

import android.app.Application;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.WindowManager;
import android.widget.Toast;
/**
 * -----------------------------------------------------------------  
 * Copyright (C) 2018-2019, by jackl, All rights reserved.  
 * -----------------------------------------------------------------
 *                      _ooOoo_
 *                     o8888888o
 *                     88" . "88
 *                     (| -_- |)
 *                      O\ = /O
 *                  ____/`---'\____
 *                .   ' \\| |// `.
 *                 / \\||| : |||// \
 *               / _||||| -:- |||||- \
 *                 | | \\\ - /// | |
 *              | \_| ''\---/'' | |
 *                \ .-\__ `-` ___/-. /
 *             ___`. .' /--.--\ `. . __
 *          ."" '< `.___\_<|>_/___.' >'"".
 *         | | : `- \`.;`\ _ /`;.`/ - ` : | |
 *           \ \ `-. \_ __\ /__ _/ .-` / /
 *   ======`-.____`-.___\_____/___.-`____.-'======
 *                      `=---='
 *   .............................................
 *            佛祖保佑             永无BUG
 * @Author: JackL
 * 创建时间: 2019/3/15 15:01
 * 版本:1.0.0
 * 描述: ToastHelper
 *     
*/


public class ToastHelper extends Handler{
      private static final String TOAST = "Toast";

      // 当前的吐司对象
      private final Toast mToast;

      // WindowManager 辅助类
      private final WindowHelper mWindowHelper;

      // 当前应用的包名
      private final String mPackageName;

      // 当前是否已经显示
      private boolean isShow;

      ToastHelper(Toast toast, Application application) {
            super(Looper.getMainLooper());
            mToast = toast;
            mPackageName = application.getPackageName();
            mWindowHelper = new WindowHelper(this, application);
      }

      @Override
      public void handleMessage(Message msg) {
            // super.handleMessage(msg);
            cancel();
      }

      /***
       * 显示吐司弹窗
       */
      void show() {
            if (!isShow) {
            /*
             这里解释一下，为什么不复用 WindowManager.LayoutParams 这个对象
             因为如果复用了，不同 Activity 之间不能共用一个，第一个 Activity 调用显示方法可以显示出来，但是会导致后面的 Activity 都显示不出来
             又或者说，非第一次调用显示方法的 Activity 都会把这个显示请求推送给之前第一个调用显示的 Activity 上面，如果第一个 Activity 已经销毁，还会报以下异常
             android.view.WindowManager$BadTokenException: Unable to add window -- token android.os.BinderProxy@ef1ccb6 is not valid; is your activity running?
             */
                  final WindowManager.LayoutParams params = new WindowManager.LayoutParams();

            /*
            // 为什么不能加 TYPE_TOAST，因为通知权限在关闭后设置显示的类型为Toast会报错
            // android.view.WindowManager$BadTokenException: Unable to add window -- token null is not valid; is your activity running?
            params.type = WindowManager.LayoutParams.TYPE_TOAST;
            */

            /*
            // 这个是旧版本的写法，新版本已经废弃，因为 Activity onPause 方法被调用后这里把 Toast 取消显示了
            // 判断是否为 Android 6.0 及以上系统并且有悬浮窗权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(mToast.getView().getContext())) {
                // 解决使用 WindowManager 创建的 Toast 只能显示在当前 Activity 的问题
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
                }else {
                    params.type = WindowManager.LayoutParams.TYPE_PHONE;
                }
            }
            */
                  params.height = WindowManager.LayoutParams.WRAP_CONTENT;
                  params.width = WindowManager.LayoutParams.WRAP_CONTENT;
                  params.format = PixelFormat.TRANSLUCENT;
                  params.windowAnimations = android.R.style.Animation_Toast;
                  params.setTitle(TOAST);
                  params.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                            | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
                  params.packageName = mPackageName;
                  // 重新初始化位置
                  params.gravity = mToast.getGravity();
                  params.x = mToast.getXOffset();
                  params.y = mToast.getYOffset();

                  try {
                        // 如果这个 View 对象被重复添加到 WindowManager 则会抛出异常
                        // java.lang.IllegalStateException: View android.widget.TextView{3d2cee7 V.ED..... ......ID 0,0-312,153} has already been added to the window manager.
                        mWindowHelper.getWindowManager().addView(mToast.getView(), params);
                        // 当前已经显示
                        isShow = true;
                        // 添加一个移除吐司的任务
                        sendEmptyMessageDelayed(0, mToast.getDuration() == Toast.LENGTH_LONG ? ToastHandler.LONG_DURATION_TIMEOUT : ToastHandler.SHORT_DURATION_TIMEOUT);
                  } catch (NullPointerException | IllegalStateException | WindowManager.BadTokenException ignored) {}
            }
      }

      /**
       * 取消吐司弹窗
       */
      void cancel() {
            // 移除之前移除吐司的任务
            removeMessages(0);
            if (isShow) {
                  try {
                        // 如果当前 WindowManager 没有附加这个 View 则会抛出异常
                        // java.lang.IllegalArgumentException: View=android.widget.TextView{3d2cee7 V.ED..... ........ 0,0-312,153} not attached to window manager
                        mWindowHelper.getWindowManager().removeView(mToast.getView());
                  }catch (NullPointerException | IllegalArgumentException ignored) {}
                  // 当前没有显示
                  isShow = false;
            }
      }
}
