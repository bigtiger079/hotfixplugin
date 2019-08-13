package com.bigger.ziroom.server;

import android.util.Log;

import com.koushikdutta.async.AsyncServer;
import com.koushikdutta.async.http.Multimap;
import com.koushikdutta.async.http.body.AsyncHttpRequestBody;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.koushikdutta.async.http.server.HttpServerRequestCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

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
                        //body.getContentType();
                        Log.i(TAG, body.getContentType());
                        Log.i(TAG, body.get().toString());
                        JSONObject requestData = new JSONObject(body.get().toString());
                        Multimap query = request.getQuery();
                        List<String> data = query.get("data");
                        if (data==null || data.isEmpty()) {
                            response.send(CONTENT_TYPE_JSON, buildJsonResponse("empty data", 2001));
                            return;
                        }
                        //Class<?> aClass = XposedHelpers.findClass("", lpparam.classLoader);
                        //String[] filterCert = (String[]) XposedHelpers.callMethod(clientObj, "FilterCert", "", "", "", 0, 0);
                        String data1 = requestData.getJSONArray("data").get(0).toString();
                        Log.d(TAG, "data: " + data1);
                        //String json = data1;//"{\"end_date\":\"2020-08-21\",\"payment\":\"月付\",\"cert_type\":\"身份证\",\"start_date\":\"2019-08-22\",\"name\":\"季洪全\",\"uid\":\"3c5fda83-33ce-45fe-b9fd-1112dae58c0e\",\"house_code\":\"BJZRGY0819471826_01\",\"cert_num\":\"320924199105056138\"}";
                        Object o = XposedHelpers.callMethod(clientObj, "SignMessage", data1, "0", "SHA1", 1);
                        jsonObject.put("key", o.toString());
                        response.send(jsonObject);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e(TAG, e.getMessage());
                        response.send(CONTENT_TYPE_JSON, buildJsonResponse(e.getMessage(), 3000));
                    }
                }
            });
            server.listen(mAsyncServer, 9988);
            isServerInit = true;
        }
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
}
