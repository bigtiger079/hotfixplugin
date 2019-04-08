package com.bigger.pdj;

/**
 * author : bigtiger
 * version ： 1.0
 * created time ：2019/4/6
 * Desc ：
 **/
import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.MessageQueue;
import android.os.Process;
import android.util.Log;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarFile;

import dalvik.system.PathClassLoader;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class Main {

    private static final String TAG = "PJDHooker";

    private static Map<String, String> vks = new HashMap<>();
    static {

        vks.put("77f47616d8","disco");
        vks.put("70fc7701","cart");
        vks.put("72f36107d81f9c53936e3ba609b1ccb7862ec8df9eafb10340836c257f8d00140b79dbd48e85e0e838c1d11c21d2a71c86","android.settings.action.MANAGE_OVERLAY_PERMISSION");
        vks.put("63fc661ed6119d47","package:");
        vks.put("77f8761ee81f9b128e","desk_icon");
        vks.put("60e96407c3","start");
        vks.put("61f86411974cd81c84542bb314be8bf988","read : ad_data =");
        vks.put("67f24714d41d9f0f8f7e21b6","toBackground");
        vks.put("61f86411974cd81c84542dbb14b2cab488728b","read : ad_bitmap =");
        vks.put("61f86411974cd80b896f2abd3fafcab0c06f968b","read : video_path =");
        vks.put("71fc661e85108a128e7f","back2front");
        vks.put("59d9413fe831ad34a44e","JDDJ_GUIDE");
        vks.put("59d9413fe832b12ea344199732","JDDJ_DISCOVER");
        vks.put("4fe83216d114a408d76e2bb43caa9df29a29f7dec2f4ed1b51b714560dfb032e685ab9a893a9898b1cabc0204bb1de6294727a34a5c4436a080dc7a8b4a4a557822b","ç³»ç»æ¯å¦æå¼æ¨éå¼å³ï¼");
        vks.put("63e8761d","push");
        vks.put("63e8761de81988188e","push_open");
        vks.put("7aee6a05d218","isopen");
        vks.put("77f8731cd4139119","deviceid");
        vks.put("7bf26810","home");
        vks.put("56e5751ac4038a18a2643ba60fb2ffbdd82a","ExposureBottomType");
        vks.put("67e47510","type");
        vks.put("60f86910d402881c876e","selectpage");
        vks.put("72fe711cc11f8c04","activity");
        vks.put("72f95a11d60299","ad_data");
        vks.put("72f95a17de02951c90","ad_bitmap");
        vks.put("72f95a03de129d12","ad_video");
        vks.put("65f46110d829881c9463","video_path");
        vks.put("74f17c2ad612","gly_ad");
        vks.put("72f95a11d21a9d09856f","ad_deleted");
        vks.put("7cef6110c5","order");
        vks.put("7ef46810","mime");
        vks.put("77f47616d8009d0fbf7f26a2","discover_tip");
        vks.put("67ef7010","true");
        vks.put("77f47616d8009d0fbf652ebf05","discover_name");
        vks.put("77f47616d8009d0f","discover");
        vks.put("2aa42e","99+");
        vks.put("7aee5a1bd802a71b89793ca63fb6c5b7dc2ec7c7","is_not_first_install");
        vks.put("72ed75","app");
        vks.put("72f36107d81f9c538e6e3bfc03b0c5aa860ce4e5b9859c7944946b3061971c131c72cedd","android.net.conn.CONNECTIVITY_CHANGE");
        vks.put("7aee5a13de048b09d63b","is_first60");
        vks.put("79f9611fd6068822847f","jddjapp_dt");
        vks.put("7aee561dd801b919","isShowAd");
        vks.put("5df87226c3178a09a1683bbb16b6dfbd","NewStartActivity");
        vks.put("7af37601d6188c2f9565","instantRun");
        vks.put("70f2685bd31f8b1e8f7d2aa04eb1c4b0c129d2","com.discover.notify");
        vks.put("70fc661dd222911085","cacheTime");
        vks.put("7ffc7601e31f9518","lastTime");
        vks.put("7ff2621cd9","login");
        vks.put("50f16c16dc238b18924720b509b1","ClickUserLogin");
        vks.put("4fe830448f12a408d6387feb3caa9fa1987ff7dec1a2ed1c51b71b5408f8032e680deff9","åæä¸æ¬¡éåº");
        vks.put("70f16014d937880d8c622cb314b6c4aa","cleanApplication");
        vks.put("7af36110cf","index");
        vks.put("7ffc701bd41e9d0f","launcher");
        vks.put("7cf34110c4028a1299","onDestroy");
        vks.put("72f36107d81f9c53906e3dbf09acd8adc72185f9b2819b725d8a6d2a7d970c0f1c68cc","android.permission.READ_PHONE_STATE");
        vks.put("72f36107d81f9c53906e3dbf09acd8adc72185fca5898b6852877a307d9a111a1163dacc808efeff38","android.permission.WRITE_EXTERNAL_STORAGE");
        vks.put("72f36107d81f9c53906e3dbf09acd8adc72185eab4839a7e5e9d612b799a0c1e0270c6db8e88f6f733","android.permission.ACCESS_COARSE_LOCATION");
        vks.put("72ed7516ce159418","appcycle");
        vks.put("75ef6a18","from");

    }

    public static void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        Log.i(TAG, "on hook pjd -> 1.2");
        Process.myPid();

        final Class<?> appClass = XposedHelpers.findClassIfExists("com.tencent.bugly.beta.tinker.TinkerPatchReflectApplication", lpparam.classLoader);
        if (appClass != null) {
            XposedHelpers.findAndHookMethod(appClass,
                    "attachBaseContext", Context.class, new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            Log.i(TAG, "TinkerPatchReflectApplication -> attachBaseContext: " + ((Context) param.thisObject).getClassLoader());
                        }
                    });

            XposedHelpers.findAndHookMethod(appClass, "onCreate", new XC_MethodHook() {
                private Object realApplication;
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    realApplication = XposedHelpers.getObjectField(param.thisObject, "realApplication");
                    XposedHelpers.setObjectField(param.thisObject, "realApplication", null);
                    XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args);

                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    if (realApplication != null) {
                        XposedHelpers.setObjectField(param.thisObject, "realApplication", realApplication);
                        ((Application) realApplication).onCreate();
                    }
                }
            });
        }


        XposedHelpers.findAndHookMethod("com.tencent.bugly.beta.tinker.TinkerApplicationLike", lpparam.classLoader,
                "onCreate", new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                        return null;
                    }
                });

        XposedHelpers.findAndHookMethod(Runtime.class, "loadLibrary0",ClassLoader.class, String.class, new XC_MethodHook(){
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                Log.i(TAG, "on load library : " + param.args[1]);
            }
        });

        XposedHelpers.findAndHookMethod("com.jingdong.aura.core.util.h", lpparam.classLoader, "c",
                String.class, String.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                        Log.e(TAG, "check sign0 ->" + param.args[0]);
                        Log.e(TAG, "check sign1 ->" + param.args[1]);
                    }
                });


        XposedHelpers.findAndHookMethod("com.tencent.tinker.lib.util.TinkerLog", lpparam.classLoader,
                "setTinkerLogImp", "com.tencent.tinker.lib.util.TinkerLog$TinkerLogImp", new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                        Log.i(TAG, "TinkerLog -> setTinkerLogImpl");
                        if (param.args[0] == null) {
                            return null;
                        } else {
                            return XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args);
                        }
                    }
                });

        XposedHelpers.findAndHookMethod("com.tencent.bugly.beta.tinker.TinkerLogger", lpparam.classLoader, "setLevel", int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                param.args[0] = 0;
            }
        });



        XposedHelpers.findAndHookMethod(Thread.class, "setDefaultUncaughtExceptionHandler", Thread.UncaughtExceptionHandler.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
//                param.args[0] = ExceptionHandler.handler;
                Log.i(TAG,"setDefaultUncaughtExceptionHandler -> " + param.args[0].toString());
                printStack();
            }
        });

        XposedHelpers.findAndHookMethod("android.os.Process", lpparam.classLoader, "killProcess", int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                Log.i(TAG,"killProcess");
                printStack();
            }
        });


//        XposedHelpers.findAndHookMethod("")

        XposedHelpers.findAndHookMethod("com.jingdong.aura.core.b.a", lpparam.classLoader, "b", String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                Log.i(TAG, "on check bundle: " + param.args[0]+"   result: " + (param.getResult() == null ? "NULL" : ((File)param.getResult()).getAbsolutePath()));
            }
        });


        XposedHelpers.findAndHookMethod("com.jingdong.aura.core.b.a", lpparam.classLoader, "c", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                Log.i(TAG, "bundle checker: " +  (param.getResult() == null ? "NULL" : param.getResult()));
            }
        });

//        XposedHelpers.findAndHookMethod("com.jingdong.aura.wrapper.c", lpparam.classLoader, "bundleChanged", "org.osgi.framework.BundleEvent", new XC_MethodHook() {
//            @Override
//            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                super.afterHookedMethod(param);
//                Log.i(TAG, "onBundle Changed");
//                printStack();
//            }
//        });

        XposedHelpers.findAndHookMethod("com.jingdong.aura.wrapper.c", lpparam.classLoader, "a", String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                Log.d(TAG, "start post kill message");
            }
        });

        XposedHelpers.findAndHookMethod("com.jingdong.aura.wrapper.d", lpparam.classLoader, "run", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                Log.d(TAG, "on post kill message");

                Looper mainLooper = Looper.getMainLooper();

                Field fieldQueue = Looper.class.getField("mQueue");
                fieldQueue.setAccessible(true);
                MessageQueue messageQueue = (MessageQueue)fieldQueue.get(mainLooper);
                Method hasMessage = MessageQueue.class.getMethod("hasMessage", Handler.class);

                Class<?> aClass = Class.forName("com.jingdong.aura.core.b.a.c");
                Field b = aClass.getField("b");
                b.setAccessible(true);
                ArrayList list = (ArrayList)b.get(null);
                Handler handler = null;
                for (Object o : list) {
                    if (o.getClass().getName().equals("com.jingdong.aura.wrapper.c")) {
                        Field field = o.getClass().getField("b");
                        handler = (Handler)field.get(o);
                    }
                }
                if (handler != null) {
                    boolean result = (boolean) hasMessage.invoke(messageQueue, handler);
                    Log.d(TAG, "mainLopper check handler -> " + result);
                }
            }
        });

        load(lpparam.classLoader);

    }


    private static void load(ClassLoader classLoader) {
        checkClassLoader(classLoader);
        Log.i(TAG, "parent ->" + classLoader.getParent());
        Class<?> appClass = XposedHelpers.findClass("net.wequick.example.small.NewApplication", classLoader);
        XposedHelpers.findAndHookMethod(appClass, "attachBaseContext", Context.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                Log.i(TAG, "NewApplication -> attachBaseContext");
                //startCheck();

            }
        });

        XposedHelpers.findAndHookMethod("android.content.res.JDMobiSec", classLoader, "n1",
                String.class, new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                        String arg = (String) param.args[0];
                        if (vks.containsKey(arg)) {
                            return vks.get(arg);
                        }
                        return "un_decode";
                    }
                });


        XC_MethodHook xc_methodHook = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                param.setResult(true);

            }
        };

        Class<?> logClass = XposedHelpers.findClass("com.jingdong.aura.core.util.a.a", classLoader);
        XposedHelpers.findAndHookMethod(logClass, "a", xc_methodHook);
        XposedHelpers.findAndHookMethod(logClass, "b", xc_methodHook);
        XposedHelpers.findAndHookMethod(logClass, "c", xc_methodHook);
        XposedHelpers.findAndHookMethod(logClass, "d", xc_methodHook);
        XposedHelpers.findAndHookMethod(logClass, "e", xc_methodHook);

        XposedHelpers.findAndHookMethod("com.jingdong.aura.core.util.a.c", classLoader, "a",
                Class.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                        Log.d(TAG, "INIT LOG -> " + param.args[0]);
                    }
                });

//        XposedHelpers.findAndHookMethod("main.homenew.HomeMainFragment", classLoader, "getPlunginLists", new XC_MethodHook() {
//            @Override
//            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                super.afterHookedMethod(param);
//                Log.d(TAG, "HomeMainFragment  getPluginLists");
//            }
//        });
    }

    private static void checkClassLoader(ClassLoader classLoader) {
        Log.i(TAG, "ClassLoader -> " +classLoader);
        if (classLoader.getParent() != null) {
            checkClassLoader(classLoader.getParent());
        }
    }

    private static void printStack() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        StringBuffer sb = new StringBuffer();
        for (StackTraceElement stackTraceElement : stackTrace) {
            sb.append(stackTraceElement.getClassName()+"." + stackTraceElement.getMethodName()).append("()").append("\r\n");
        }

        Log.i(TAG, sb.toString());
    }


    private static class ExceptionHandler implements Thread.UncaughtExceptionHandler {

        public static ExceptionHandler handler = new ExceptionHandler();

        @Override
        public void uncaughtException(Thread t, Throwable e) {
            Log.e(TAG, "on handle uncaughtException -> " + t.getId() +" \r\n");
            StackTraceElement[] stackTrace = e.getStackTrace();
            StringBuffer sb = new StringBuffer();
            for (StackTraceElement stackTraceElement : stackTrace) {
                sb.append(stackTraceElement.getClassName()+"." + stackTraceElement.getMethodName()).append("()").append("\r\n");
            }

            Log.i(TAG, sb.toString());
        }
    }
}
