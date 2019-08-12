package com.bigger.deepseafishing;

import android.util.Log;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class ModuleEntry {
    private static final String TAG = "DeepSeaHook";
    private static XC_MethodHook stackTraceMt = new XC_MethodHook() {
        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            super.afterHookedMethod(param);
            printStackTrace(Thread.currentThread());
        }
    };

    public static void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        Log.i(TAG, "on handleLoadPackage: " + 1.0);
        hookLog(lpparam.classLoader);

        XposedHelpers.findAndHookMethod("com.magic.ads.AppsFlyerAgent", lpparam.classLoader, "getAvConfig", new XC_MethodHook(){
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                Log.i(TAG, "[AvConfig]: " + param.getResult().toString());
            }
        });

        XposedHelpers.findAndHookMethod("com.magic.ads.AdvertisingBox", lpparam.classLoader, "showRewardAds", int.class, stackTraceMt);
        XposedHelpers.findAndHookMethod("org.cocos2dx.lib.Cocos2dxRenderer", lpparam.classLoader, "handleActionUp", int.class, float.class, float.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                Log.i(TAG, param.method.getName()+":" + param.args[0]+", "+param.args[1]+", "+param.args[2]);
            }
        });
    }

    private static void hookLog(ClassLoader classLoader) {
        Class<?> magicAdLog = XposedHelpers.findClass("com.magic.ads.utils.Logger", classLoader);
        XposedHelpers.setStaticBooleanField(magicAdLog, "DEBUG", true);
        XposedHelpers.findAndHookMethod(magicAdLog, "setDebugMode", boolean.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                param.args[0] = true;
            }
        });
    }

    private static void printStackTrace(Thread thread) {
        StackTraceElement[] stackTrace = thread.getStackTrace();
        StringBuffer sb = new StringBuffer();
        for (StackTraceElement stackTraceElement : stackTrace) {
            sb.append(stackTraceElement.getClassName()).append(".").append(stackTraceElement.getMethodName()).append("\r\n");
        }
        Log.d(TAG, sb.toString());
    }
}