package com.example.pubnubdemo;
import android.os.Bundle;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.models.consumer.PNPublishResult;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.push.payload.PushPayloadHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    String TAG = "FirebaseService";
    public static PubNub pubnub; // PubNub instance
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferencesManager.init(getApplicationContext());
        findViewById(R.id.tvSend).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendNotification();
            }
        });
        initPubnub();
        createChannel();
    }
    // Creates PubNub instance with your PubNub credentials. https://admin.pubnub.com/signup
    // This instance will be used when we need to create connection to PubNub.
    private void initPubnub() {
        try {
            PNConfiguration pnConfiguration = new PNConfiguration(UUID.randomUUID().toString().substring(0,5));
            pnConfiguration.setPublishKey("pub-c-07c79893-4464-40af-a54a-76a54dabcb0c");
            pnConfiguration.setSubscribeKey("sub-c-093a038a-f08d-4b7e-a19f-5e8e59f51e7e");
            pnConfiguration.setSecure(true);
            pubnub = new PubNub(pnConfiguration);
        } catch (Exception ex) {

        }
    }
    // Creates notification channel.
    private void createChannel() {
        // Notification channel should only be created for devices running Android API level 26+.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel chan1 = new NotificationChannel(
                    "default",
                    "default",
                    NotificationManager.IMPORTANCE_NONE);
            chan1.setLightColor(Color.TRANSPARENT);
            chan1.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
            notificationManager.createNotificationChannel(chan1);
        }
    }

    private void sendNotification(){
        PushPayloadHelper pushPayloadHelper = new PushPayloadHelper();
        PushPayloadHelper.FCMPayload fcmPayload = new PushPayloadHelper.FCMPayload();
        Map<String, Object> payload = new HashMap<>();
        ArrayList<String> tokens = new ArrayList<>();
        tokens.add(""+SharedPreferencesManager.readDeviceToken());
//        payload.put("pn_exceptions", tokens);
        Gson gson = new Gson();
        Student student = new Student();
        student.setId(1);
        student.setName("students");
        payload.put("android:data", gson.toJson(student));

        fcmPayload.setCustom(payload);
        PushPayloadHelper.FCMPayload.Notification fcmNotification =
                new PushPayloadHelper.FCMPayload.Notification()
                        .setTitle("Chat Invitation")
                        .setBody("John invited you to chat");


        fcmPayload.setNotification(fcmNotification);
        Map<String, Object> data = new HashMap<>();
        data.put("data",gson.toJson(student));
        fcmPayload.setData(data);
        pushPayloadHelper.setFcmPayload(fcmPayload);

        Map<String, Object> commonPayload = new HashMap<>();
        commonPayload.put("text", "John invited you to chat");
        commonPayload.put("text", "John invited you to chat");
        pushPayloadHelper.setCommonPayload(commonPayload);

        Map<String, Object> pushPayload = pushPayloadHelper.build();

        pubnub.publish()
                .channel("TestPushChannel")
                .message(pushPayload)
                .async(new PNCallback<PNPublishResult>() {
                    @Override
                    public void onResponse(PNPublishResult result, PNStatus status) {
                        Log.d("PUBNUB", "-->PNStatus.getStatusCode = " + status.getStatusCode());
                    }
                });
    }
}

class Student {
    private int id;
    private String name;
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
}
