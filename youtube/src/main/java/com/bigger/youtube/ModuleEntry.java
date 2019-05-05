package com.bigger.youtube;

import android.util.Log;

import java.util.List;
import java.util.Map;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class ModuleEntry {

    private static final String TAG = "YouTubeHook";

    public static void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        //TODO: start hook here
        Log.i(TAG, "YouTube module is loaded");
//        hookLog(lpparam);

        XposedHelpers.findAndHookConstructor("bng", lpparam.classLoader,
                int.class, "[B", Map.class, List.class, boolean.class,
                long.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        Utils.printStackTrace();
                    }
                });

        XposedHelpers.findAndHookMethod("wyr", lpparam.classLoader, "h", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                Utils.printStackTrace();
            }
        });
    }

    private static void hookLog(final XC_LoadPackage.LoadPackageParam lpparam) {
        Class logClass = XposedHelpers.findClass("xkp", lpparam.classLoader);
        XC_MethodHook logMethodHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                Log.i(TAG, "[" + param.args[0]+"]: " + param.args[2]+"    [LEVEL]->" + param.args[1]);

            }
        };
        XposedHelpers.findAndHookMethod(logClass, "a", String.class, int.class, String.class, Throwable.class, logMethodHook);
    }
}