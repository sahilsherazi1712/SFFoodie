package com.sahilssoft.sffoodie;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.sahilssoft.sffoodie.activities.OrderDetailsSellerActivity;
import com.sahilssoft.sffoodie.activities.OrderDetailsUsersActivity;

import java.util.Random;

public class MyFirebaseMessaging extends FirebaseMessagingService {

    private static final String NOTIFICATION_CHANNEL_ID = "MY_NOTIFICATION_CHANNEL_ID"; //required for android 0or above

    private FirebaseAuth auth;
    private FirebaseUser user;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        //all notifications will be received here

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        //get data from notifications
        String notificationType = remoteMessage.getData().get("notificationType");
        if (notificationType.equals("NewOrder")){
            String buyerUid = remoteMessage.getData().get("buyerUid");
            String sellerUid = remoteMessage.getData().get("sellerUid");
            String orderId = remoteMessage.getData().get("orderId");
            String notificationTitle = remoteMessage.getData().get("notificationTitle");
            String notificationMessage = remoteMessage.getData().get("notificationMessage");
            if (auth != null && auth.getUid().equals(sellerUid)){
                //user is signed in and is same user to which notification is sent
                showNotification(orderId, sellerUid, buyerUid, notificationTitle, notificationMessage, notificationType);
            }
        }
        if (notificationType.equals("OrderStatusChanged")){
            String buyerUid = remoteMessage.getData().get("buyerUid");
            String sellerUid = remoteMessage.getData().get("sellerUid");
            String orderId = remoteMessage.getData().get("orderId");
            String notificationTitle = remoteMessage.getData().get("notificationTitle");
            String notificationMessage = remoteMessage.getData().get("notificationMessage");
            if (auth != null && auth.getUid().equals(buyerUid)){
                //user is signed in and is same user to which notification is sent
                showNotification(orderId, sellerUid, buyerUid, notificationTitle, notificationMessage, notificationType);
            }
        }
    }

    private void showNotification(String orderId, String sellerUid, String buyerUid, String notificationTitle, String notificationMessage, String notificationType) {
        //notification
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        //id for notification, random
        int notificationID = new Random().nextInt(3000);

        //check if android version is Oreo/O or above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            setupNotificationChannel(notificationManager);
        }

        //handle notification click , start order activity
        Intent intent = null;
        if (notificationType.equals("NewOrder")){
            //open order details seller activity
            intent = new Intent(this, OrderDetailsSellerActivity.class);
            intent.putExtra("orderId",orderId);
            intent.putExtra("orderBy",buyerUid);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }else if (notificationType.equals("OrderStatusChanged")){
            //open OrderDetailsUsersActivity
            intent = new Intent(this, OrderDetailsUsersActivity.class);
            intent.putExtra("orderId",orderId);
            intent.putExtra("orderTo",sellerUid);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        //large icon
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher);

        //sound of notification
        Uri notificationSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this,NOTIFICATION_CHANNEL_ID);
        notificationBuilder.setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(largeIcon)
                .setContentTitle(notificationTitle)
                .setContentText(notificationMessage)
                .setSound(notificationSoundUri)
                .setAutoCancel(true)   //cancel/dismiss when clicked
                .setContentIntent(pendingIntent);  //add intent

        //show notification
        notificationManager.notify(notificationID, notificationBuilder.build());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupNotificationChannel(NotificationManager notificationManager) {
        CharSequence channelName = "Some Sample Text";
        String channelDescription = "Channel Description here";

        NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH);
        notificationChannel.setDescription(channelDescription);
        notificationChannel.enableLights(true);
        notificationChannel.setLightColor(Color.RED);
        notificationChannel.enableVibration(true);
        if (notificationManager != null){
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }
}
