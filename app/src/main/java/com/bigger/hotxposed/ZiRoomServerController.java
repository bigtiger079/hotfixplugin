package com.bigger.hotxposed;

import android.text.TextUtils;
import android.util.Log;

import com.koushikdutta.async.AsyncServer;
import com.koushikdutta.async.http.body.AsyncHttpRequestBody;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.koushikdutta.async.http.server.HttpServerRequestCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

public class ZiRoomServerController {
    private static final String TAG = "ZiRoomServerController";
    private static AsyncHttpServer server = new AsyncHttpServer();
    private static AsyncServer mAsyncServer = new AsyncServer();
    private static boolean isServerInit = false;


    private static final String CONTENT_TYPE_JSON = "application/json;charset=utf-8";
    private static Object clientObj;
    public static void initServer(){//XC_LoadPackage.LoadPackageParam lp, final Object clientObj) {
//        lpparam = lp;
//        ZiRoomServerController.clientObj = clientObj;
        if (!isServerInit) {
//            new Inet4Address();
//            mAsyncServer.listen()
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
                        //Object o = signMessage(sort.toString());
//                        jsonObject.put("key", o.toString());
                        jsonObject.put("key", "fhgakjgfkajsghdfkj");
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
            Log.d(TAG, "server is Started on 9988");
            final InetAddress localIPAddress = getLocalIPAddress();
            Log.i(TAG, localIPAddress.getHostAddress());
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, localIPAddress.getHostName());
                }
            });

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

//    private static String signMessage(String message) {
//        String[] filterCert = (String[]) XposedHelpers.callMethod(clientObj, "FilterCert", "", "", "", 0, 0);
//        if (filterCert == null || filterCert.length == 0) {
//            return "";
//        }
//        Log.i(TAG, "filterCert: " + filterCert[0]);
//
//        Object getCertAttribute = XposedHelpers.callMethod(clientObj, "GetCertAttribute", filterCert[0]);
//        Log.i(TAG, "getCertAttribute: " + getCertAttribute.toString());
//        CertInfo convert = CertInfo.convert(getCertAttribute);
//        Log.i(TAG, "CertInfo: " + convert.toString());
//        int certExpireRemind = (int) XposedHelpers.callMethod(clientObj, "CertExpireRemind", "0");
//        Log.i(TAG, "certExpireRemind: " + certExpireRemind);
//
//
//        String o = (String) XposedHelpers.callMethod(clientObj, "SignMessage", message, filterCert[0], "SHA1", 1);
//        Log.i(TAG, "SignMessage: " + o);
//
//        if (TextUtils.isEmpty(o)) {
//            String lastError = (String) XposedHelpers.callMethod(clientObj, "GetLastErrInfo");
//            Log.e(TAG, "signErr: " + lastError);
//        }
//        return o;
//    }

    //获取本机IP地址
    public static InetAddress getLocalIPAddress() {
        Enumeration<NetworkInterface> enumeration = null;
        try {
            enumeration = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        if (enumeration != null) {
            while (enumeration.hasMoreElements()) {
                NetworkInterface nif = enumeration.nextElement();
                Enumeration<InetAddress> inetAddresses = nif.getInetAddresses();
                if (inetAddresses != null)
                    while (inetAddresses.hasMoreElements()) {
                        InetAddress inetAddress = inetAddresses.nextElement();
                        if (!inetAddress.isLoopbackAddress() && isIPv4Address(inetAddress.getHostAddress())) {
                            return inetAddress;
                        }
                    }
            }
        }
        return null;
    }


    private static final Pattern IPV4_PATTERN = Pattern.compile("^(" +
            "([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}" +
            "([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$");

    private static boolean isIPv4Address(String input) {
        return IPV4_PATTERN.matcher(input).matches();
    }
}
