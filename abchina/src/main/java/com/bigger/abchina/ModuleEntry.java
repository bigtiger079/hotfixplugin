package com.bigger.abchina;

import android.content.Context;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class ModuleEntry {

    public static void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        System.out.println("-------aa-----");
        final Class<?> aClass = XposedHelpers.findClass("s.h.e.l.l.S", lpparam.classLoader);
        XposedHelpers.findAndHookMethod(aClass, "attachBaseContext", Context.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                System.out.println("attachBaseContext beforeHookedMethod ");
                super.beforeHookedMethod(param);
            }
        });

        XposedHelpers.findAndHookMethod(aClass, "onCreate", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                System.out.println("onCreate beforeHookedMethod ");
                super.beforeHookedMethod(param);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                Object n = XposedHelpers.getStaticObjectField(aClass, "n");
                if (n != null) {
                    System.out.println("abcdefg " + n);
                } else {
                    System.out.println("------------");
                }
            }
        });
    }
}