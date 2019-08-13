package com.bigger.langren;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class ModuleEntry {

    public static void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        showLog(lpparam.classLoader);
    }

    private static void showLog(ClassLoader classLoader) {
        Class<?> categoryClz = XposedHelpers.findClass("org.apache.log4j.Category", classLoader);

        XC_MethodHook xc_methodHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
            }
        };

        XposedHelpers.findAndHookMethod(categoryClz, "debug", Object.class, xc_methodHook);
        XposedHelpers.findAndHookMethod(categoryClz, "debug", Object.class, Throwable.class, xc_methodHook);

        XposedHelpers.findAndHookMethod(categoryClz, "error", Object.class, xc_methodHook);
        XposedHelpers.findAndHookMethod(categoryClz, "error", Object.class, Throwable.class, xc_methodHook);

        XposedHelpers.findAndHookMethod(categoryClz, "debug", Object.class, xc_methodHook);
        XposedHelpers.findAndHookMethod(categoryClz, "debug", Object.class, Throwable.class, xc_methodHook);

        XposedHelpers.findAndHookMethod(categoryClz, "info", Object.class, xc_methodHook);
        XposedHelpers.findAndHookMethod(categoryClz, "info", Object.class, Throwable.class, xc_methodHook);

        XposedHelpers.findAndHookMethod(categoryClz, "warn", Object.class, xc_methodHook);
        XposedHelpers.findAndHookMethod(categoryClz, "warn", Object.class, Throwable.class, xc_methodHook);
    }
}