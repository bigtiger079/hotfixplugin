package com.bigger.hotxposed;

import org.json.JSONException;
import org.json.JSONObject;

public class Module {

    private String packageName;
    private String apkName;
    private String entryClass;

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getApkName() {
        return apkName;
    }

    public void setApkName(String apkName) {
        this.apkName = apkName;
    }

    public String getEntryClass() {
        return entryClass;
    }

    public void setEntryClass(String entryClass) {
        this.entryClass = entryClass;
    }

    @Override
    public String toString() {
        try {
            return toJson(this);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "---";
    }

    public static String toJson(Module module) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("packageName", module.packageName);
        jsonObject.put("apkName", module.apkName);
        jsonObject.put("entryClass", module.entryClass);

        return  jsonObject.toString();
    }

    public static Module fromJson(JSONObject jObj) throws JSONException {
        Module module = new Module();
        String pName = jObj.getString("packageName");
        String aName = jObj.getString("apkName");
        String entryClass = jObj.getString("entryClass");

        module.setPackageName(pName);
        module.setApkName(aName);
        module.setEntryClass(entryClass);

        return module;
    }
}
