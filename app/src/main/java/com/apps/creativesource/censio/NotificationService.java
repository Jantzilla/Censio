package com.apps.creativesource.censio;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;

public class NotificationService extends FirebaseMessagingService {
    private final String CHANNEL_ID = "Censio";
    private DatabaseReference databaseReference;
    private SharedPreferences sharedPreferences;
    private String userId;
    String notificationTitle = null, notificationBody = null;
    String dataTitle = null, dataMessage = null;
    private boolean notifications;
    private String messageUserId;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        createNotificationChannel();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        userId = sharedPreferences.getString("userFireId","null");
        notifications = sharedPreferences.getBoolean("notifications", true);

        messageUserId = remoteMessage.getData().get("userId");
        notificationTitle = remoteMessage.getData().get("title");
        notificationBody = remoteMessage.getData().get("body");

        if(notifications && messageUserId.equals(userId))
            sendNotification(notificationTitle, notificationBody, dataTitle, dataMessage);

        super.onMessageReceived(remoteMessage);
    }

    private void sendNotification(String notificationTitle, String notificationBody, String dataTitle, String dataMessage) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("title", dataTitle);
        intent.putExtra("message", dataMessage);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(notificationTitle)
                .setContentText(notificationBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

    @Override
    public void onNewToken(String s) {
        sendRegistrationToServer(s);
    }

    private void sendRegistrationToServer(String s) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        userId = sharedPreferences.getString("userFireId","null");
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("AuthToken", s);
        editor.apply();

        Log.d("NewAuthToken", s); //TODO: Remove when complete

        databaseReference = FirebaseDatabase.getInstance().getReference();

        DatabaseReference userRef = databaseReference.child("users")
                .child(userId);

        Map<String, Object> token = new HashMap<>();
        token.put("token", s);

        userRef.updateChildren(token);

    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

}
