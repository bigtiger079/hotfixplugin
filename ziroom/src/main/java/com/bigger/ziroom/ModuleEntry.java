package com.bigger.ziroom;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.bigger.ziroom.server.ZiRoomServerController;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class ModuleEntry {

    public static void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        Log.d("ZiRoomServerController", lpparam.processName+"");
        XposedHelpers.findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook(){
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);

            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                Context arg = (Context) param.args[0];
                onHook(arg.getClassLoader());
                if (!lpparam.processName.contains(":")) {
                    Class ClientForAndroid = XposedHelpers.findClass("com.itrus.raapi.implement.ClientForAndroid", lpparam.classLoader);
                    Object getInstance = XposedHelpers.callStaticMethod(ClientForAndroid, "getInstance", arg);
                    ZiRoomServerController.initServer(lpparam, getInstance);

                }
            }
        });
    }

    private static void onHook(ClassLoader classLoader) {
        Class ClientForAndroid = XposedHelpers.findClass("com.itrus.raapi.implement.ClientForAndroid", classLoader);

        XC_MethodHook mh = createMH();

        XposedHelpers.findAndHookMethod(ClientForAndroid, "CInitUserPIN", String.class, String.class, mh);

        XposedHelpers.findAndHookMethod(ClientForAndroid, "CSetAdminPIN", String.class, String.class, int.class, mh);

        XposedHelpers.findAndHookMethod(ClientForAndroid, "CertExpireRemind", String.class, mh);

        XposedHelpers.findAndHookMethod(ClientForAndroid, "ChangeUserPIN", String.class, String.class, String.class, mh);

        XposedHelpers.findAndHookMethod(ClientForAndroid, "DecryptMessage", String.class, String.class, String.class, mh);

        XposedHelpers.findAndHookMethod(ClientForAndroid, "DeleteCertByNickname", String.class, mh);

        XposedHelpers.findAndHookMethod(ClientForAndroid, "DeleteCertBySerialNumber", String.class,mh);

        XposedHelpers.findAndHookMethod(ClientForAndroid, "EncryptMessage", String.class, String.class, String.class, mh);

        XposedHelpers.findAndHookMethod(ClientForAndroid, "FilterCert", String.class, String.class, String.class,int.class,int.class, mh);

        XposedHelpers.findAndHookMethod(ClientForAndroid, "GenCSR", String.class, String.class,String.class, String.class,String.class, int.class, String.class, mh);

        XposedHelpers.findAndHookMethod(ClientForAndroid, "GenCSRWithCertID", String.class, String.class, String.class, String.class,String.class, String.class, mh);

        XposedHelpers.findAndHookMethod(ClientForAndroid, "GetCertAttribute", String.class, mh);

        XposedHelpers.findAndHookMethod(ClientForAndroid, "GetCertBuf", String.class, mh);

        XposedHelpers.findAndHookMethod(ClientForAndroid, "GetCertNickName", String.class, mh);

        XposedHelpers.findAndHookMethod(ClientForAndroid, "GetCertSerialNumber", String.class, mh);

        XposedHelpers.findAndHookMethod(ClientForAndroid, "GetCertSubject", String.class, mh);

        XposedHelpers.findAndHookMethod(ClientForAndroid, "GetLastErrInfo", mh);

        XposedHelpers.findAndHookMethod(ClientForAndroid, "ImportCert", String.class, String.class, mh);

        XposedHelpers.findAndHookMethod(ClientForAndroid, "ImportCertWithNickname", String.class, String.class, String.class, mh);

        XposedHelpers.findAndHookMethod(ClientForAndroid, "IsRemovableDevice", String.class, mh);

        XposedHelpers.findAndHookMethod(ClientForAndroid, "SSLConfigClientCert", String.class, mh);

        XposedHelpers.findAndHookMethod(ClientForAndroid, "SSLConfigServer", String.class,  int.class, mh);

        XposedHelpers.findAndHookMethod(ClientForAndroid, "SSLConnectToServer",int.class, mh);

        XposedHelpers.findAndHookMethod(ClientForAndroid, "SSLFinal",mh);

        XposedHelpers.findAndHookMethod(ClientForAndroid, "SSLForceHanleShake", mh);

        XposedHelpers.findAndHookMethod(ClientForAndroid, "SSLInit",  mh);

        XposedHelpers.findAndHookMethod(ClientForAndroid, "SSLRead", mh);

        XposedHelpers.findAndHookMethod(ClientForAndroid, "SSLWrite", String.class, int.class, mh);

        XposedHelpers.findAndHookMethod(ClientForAndroid, "SetLicense", String.class,  mh);

        XposedHelpers.findAndHookMethod(ClientForAndroid, "SetSystemDBDir", String.class, mh);

        XposedHelpers.findAndHookMethod(ClientForAndroid, "SignMessage", String.class, String.class,String.class, int.class, mh);
        XposedHelpers.findAndHookMethod(ClientForAndroid, "SignMessageBYTE", byte[].class, String.class,String.class, int.class, mh);
        XposedHelpers.findAndHookMethod(ClientForAndroid, "SignMessageBYTEWithLength", byte[].class,int.class, String.class,String.class, int.class, mh);
        XposedHelpers.findAndHookMethod(ClientForAndroid, "VerifyMessage", String.class, String.class, String.class,String.class, int.class, mh);
        XposedHelpers.findAndHookMethod(ClientForAndroid, "VerifyUserPIN", String.class,String.class, int.class, mh);
        XposedHelpers.findAndHookMethod(ClientForAndroid, "wa", mh);

        XposedHelpers.findAndHookMethod("com.ziroom.commonlib.utils.n", classLoader, "d", String.class, String.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                Log.d(param.args[0].toString(), param.args[1].toString());
            }
        });

    }

    private static XC_MethodHook createMH() {
        return new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);

            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                logParams(param.method.getName(), param);
            }
        };
    }

    private static void logParams(String tag, XC_MethodHook.MethodHookParam param) {
        Object[] args = param.args;
        StringBuffer sb = new StringBuffer();
        if (args != null) {
            if (args.length > 0) {
                int index = 0;
                for (Object arg : args) {
                    sb.append(index).append(": ").append(arg.toString()).append("   ");
                    index++;
                }
            }
        }

        sb.append("  result: ").append(param.getResult().toString());
        Log.i("ZiRoomHook: "+tag, sb.toString());
    }
}