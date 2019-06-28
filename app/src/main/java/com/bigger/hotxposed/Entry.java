package com.bigger.hotxposed;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import dalvik.system.DexClassLoader;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class Entry implements IXposedHookLoadPackage, IXposedHookZygoteInit {
    private static final String TAG = "RICHER_ENTRY";
    private static final String MODULE_METHOD_NAME = "handleLoadPackage";
    private static final String MODULES_DIR = "/data/local/tmp/hook";
    private static Map<String, Module> modules = new HashMap<>();
    private static boolean isModulesInited = false;

    static {
        initModules();
    }

    private static void initModules() {
        File moduleConfig = new File(MODULES_DIR, "modules.json");
        if (moduleConfig.exists() && moduleConfig.canRead()) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(moduleConfig);
                int available = fis.available();
                byte[] buf = new byte[available];
                int read = fis.read(buf);
                if (read != -1) {
                    JSONArray jArr= new JSONArray(new String(buf, "utf-8"));
                    Log.d(TAG, jArr.toString());
                    int length = jArr.length();
                    for (int i = 0 ; i < length; i++) {
                        JSONObject jsonObject = jArr.getJSONObject(i);
                        Module module = Module.fromJson(jsonObject);
                        modules.put(module.getPackageName(), module);
                    }
                }
                isModulesInited = true;
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            } finally {
                try {
                    if (fis != null) {
                        fis.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            Log.d(TAG, moduleConfig.getAbsolutePath() +" is unread");
        }
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        debuggable(lpparam);
//        if (!isUserApp(lpparam)) {
//            return;
//        }
        if (lpparam.appInfo == null) {
            return;
        }
        if (!isModulesInited) {
            initModules();
        }
        Log.d(TAG, lpparam.appInfo.dataDir+" "+lpparam.appInfo.publicSourceDir + " " + lpparam.appInfo.sourceDir);
        if (!modules.containsKey(lpparam.packageName)) {
//            Log.d(TAG, "NOT HOOK PACKAGE:" + lpparam.packageName);
            return;
        }

        Module module = modules.get(lpparam.packageName);
        File loadDex = new File(MODULES_DIR, module.getApkName());
        if (!loadDex.exists()) {
            Log.d(TAG, loadDex.getAbsolutePath() + " dose not exist");
            return;
        }
        File dexOutputDir = new File(lpparam.appInfo.dataDir, "xposed_module");
        if (!dexOutputDir.exists() && !dexOutputDir.mkdir()) {
            Log.d(TAG, "make " + dexOutputDir.getAbsolutePath() + " error");
            return;
        }
        Log.d(TAG, "start load -> " + module.toString());
        try {
            ClassLoader loader = this.getClass().getClassLoader();
            checkClassLoader(loader, "MainModule");
            checkClassLoader(lpparam.classLoader, "LoadPackage");
            DexClassLoader classLoader = new DexClassLoader(loadDex.getAbsolutePath(),
                    dexOutputDir.getAbsolutePath(), null, loader);
            Class<?> clz = classLoader.loadClass(module.getEntryClass());
            Method method = clz.getDeclaredMethod(MODULE_METHOD_NAME, XC_LoadPackage.LoadPackageParam.class);
            method.invoke(null, lpparam);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void debuggable(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        if(loadPackageParam.packageName.equals("android")) {
            XposedBridge.hookAllMethods(XposedHelpers.findClass("com.android.server.pm.PackageManagerService", loadPackageParam.classLoader),
                    "getPackageInfo", new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                        }

                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            Object result = param.getResult();
                            if (result != null) {
                                ApplicationInfo info = ((PackageInfo) result).applicationInfo;
                                int flags = info.flags;
                                if((flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                                    flags |= 32768;
                                    flags |= 2;
                                    ((PackageInfo) result).applicationInfo.flags = flags;
                                    param.setResult(result);
                                }
                            }
                        }
                    });
        }
    }

    private static void checkClassLoader(ClassLoader classLoader, String tag) {
        if (classLoader == null) {
            return;
        }
        Log.d(TAG, tag + "LoadPackage: " + classLoader);
        checkClassLoader(classLoader.getParent(), tag);
    }

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
    }

    private static boolean isUserApp(XC_LoadPackage.LoadPackageParam lpparam) {
        if (lpparam.appInfo == null) {
            return false;
        }
        return (lpparam.appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0;
    }
}
