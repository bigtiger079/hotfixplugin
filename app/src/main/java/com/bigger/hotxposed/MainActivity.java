package com.bigger.hotxposed;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.koushikdutta.async.DataEmitter;
import com.koushikdutta.async.DataSink;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.AsyncHttpGet;
import com.koushikdutta.async.http.AsyncHttpPost;
import com.koushikdutta.async.http.AsyncHttpRequest;
import com.koushikdutta.async.http.AsyncHttpResponse;
import com.koushikdutta.async.http.body.AsyncHttpRequestBody;
import com.koushikdutta.async.http.body.JSONObjectBody;
import com.koushikdutta.async.http.body.UrlEncodedFormBody;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.Charset;


public class MainActivity extends Activity {
    private static String name;
    private AsyncHttpGet asyncHttpGet;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            AsyncHttpClient.getDefaultInstance().executeJSONObject(asyncHttpGet, jsonObjectCallback);
        }
    };
    private AsyncHttpClient.JSONObjectCallback jsonObjectCallback = new AsyncHttpClient.JSONObjectCallback() {
        @Override
        public void onCompleted(Exception e, AsyncHttpResponse source, JSONObject result) {
            if (result != null) {
                Log.i("MainActivity", result.toString());
                if (result.optString("msg", "failed").equals("success")) {
                    JSONObject jsonObject = result.optJSONObject("data");
                    String key = jsonObject.optString("key", "");
                    if (TextUtils.isEmpty(key)) {
                        handler.sendEmptyMessageDelayed(1, 200);
                    } else {
                        MainActivity.this.handleVerify(key);
                    }
                }
            } else {
                e.printStackTrace();
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ///ZiRoomServerController.initServer();
        asyncHttpGet = new AsyncHttpGet("http://192.168.0.109:8080/verify/key?name=test");
        AsyncHttpClient.getDefaultInstance().executeJSONObject(asyncHttpGet, jsonObjectCallback);

    }

    private void handleVerify(String key) {
        AsyncHttpPost post = new AsyncHttpPost("http://192.168.0.109:8080/verify");
        post.addHeader("Content-Type", "application/json");
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("key", key);
            jsonObject.put("result", "Hello!!!");
            JSONObjectBody body = new JSONObjectBody(jsonObject);
            post.setBody(body);
            AsyncHttpClient.getDefaultInstance().executeJSONObject(post, new AsyncHttpClient.JSONObjectCallback() {
                @Override
                public void onCompleted(Exception e, AsyncHttpResponse source, JSONObject result) {
                    if (e == null) {
                        Log.i("MainActivity", result.toString());
                    }
                    handler.sendEmptyMessage(1);
                }
            });
        } catch (JSONException e) {

        }
    }
}
