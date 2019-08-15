package com.bigger.ziroom.server;

import android.text.TextUtils;
import android.util.Log;

import com.bigger.ziroom.CertInfo;
import com.koushikdutta.async.AsyncServer;
import com.koushikdutta.async.http.Multimap;
import com.koushikdutta.async.http.body.AsyncHttpRequestBody;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.koushikdutta.async.http.server.HttpServerRequestCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class ZiRoomServerController {
    private static final String TAG = "ZiRoomServerController";
    private static AsyncHttpServer server = new AsyncHttpServer();
    private static AsyncServer mAsyncServer = new AsyncServer();
    private static boolean isServerInit = false;
    private static XC_LoadPackage.LoadPackageParam lpparam;

    private static final String CONTENT_TYPE_JSON = "application/json;charset=utf-8";
    private static Object clientObj;
    public static void initServer(XC_LoadPackage.LoadPackageParam lp, final Object clientObj) {
        lpparam = lp;
        ZiRoomServerController.clientObj = clientObj;
        if (!isServerInit) {
            server.post("/ziroom/encryption", new HttpServerRequestCallback() {
                @Override
                public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
                    JSONObject jsonObject = new JSONObject();
                    try {
                        AsyncHttpRequestBody body = request.getBody();
                        if (body == null) {
                            response.send(CONTENT_TYPE_JSON, buildJsonResponse("empty body", 2000));
                            return;
                        }
                        Log.i(TAG, body.getContentType());
                        Log.i(TAG, body.get().toString());
                        JSONObject requestData = new JSONObject(body.get().toString());

                        JSONObject jsonData = requestData.getJSONArray("data").getJSONObject(0);
                        jsonData.put("cert_type", "身份证");
                        jsonData.put("name", "季洪全");
                        jsonData.put("payment", "月付");
                        Log.d(TAG, "data before: " + jsonData.toString());
                        JSONObject sort = sort(jsonData);
                        Log.d(TAG, "data after: " + sort.toString());
                        Object o = signMessage(sort.toString());
                        jsonObject.put("key", o.toString());
                        response.send(jsonObject);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e(TAG, e.getMessage());
                        response.send(CONTENT_TYPE_JSON, buildJsonResponse(e.getMessage(), 3000));
                    }
                }
            });
            server.get("/ziroom/check", new HttpServerRequestCallback() {
                @Override
                public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
                    response.send(buildJsonResponse("success", 100));
                }
            });
            server.listen(mAsyncServer, 9988);
            isServerInit = true;
        }
    }

    private static JSONObject sort(JSONObject jsonObject){
        Iterator<String> keys = jsonObject.keys();
        HashMap<String, String> map = new HashMap<>();
        while (keys.hasNext()) {
            String next = keys.next();
            map.put(next, jsonObject.optString(next));
        }
        JSONObject sortJson = new JSONObject();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            try {
                sortJson.put(entry.getKey(), entry.getValue());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return sortJson;
    }

    private static String buildJsonResponse(String message, int code) {
        return "{\"message\":\""+message+"\",\"code\":"+code+"}";
    }

    private static String buildJsonResponse(JSONObject data, String message, int code) {
        return "{\"message\":\""+message+"\",\"code\":"+code+",\"data\":"+data.toString()+"}";
    }

    public static JSONObject buildSuccessJsonResponse(JSONObject data) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("code", 100);
            jsonObject.put("message", "success");
            jsonObject.put("data", data);
            return jsonObject;
        } catch (JSONException e) {
            return null;
        }
    }

    private static String signMessage(String message) {
        String[] filterCert = (String[]) XposedHelpers.callMethod(clientObj, "FilterCert", "", "", "", 0, 0);
        if (filterCert == null || filterCert.length == 0) {
            return "";
        }
        Log.i(TAG, "filterCert: " + filterCert[0]);

        Object getCertAttribute = XposedHelpers.callMethod(clientObj, "GetCertAttribute", filterCert[0]);
        Log.i(TAG, "getCertAttribute: " + getCertAttribute.toString());
        CertInfo convert = CertInfo.convert(getCertAttribute);
        Log.i(TAG, "CertInfo: " + convert.toString());
        int certExpireRemind = (int) XposedHelpers.callMethod(clientObj, "CertExpireRemind", "0");
        Log.i(TAG, "certExpireRemind: " + certExpireRemind);


        String o = (String) XposedHelpers.callMethod(clientObj, "SignMessage", message, filterCert[0], "SHA1", 1);
        Log.i(TAG, "SignMessage: " + o);

        if (TextUtils.isEmpty(o)) {
            String lastError = (String) XposedHelpers.callMethod(clientObj, "GetLastErrInfo");
            Log.e(TAG, "signErr: " + lastError);
        }
        return o;
    }
}
