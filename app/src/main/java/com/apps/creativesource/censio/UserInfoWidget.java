package com.apps.creativesource.censio;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.RemoteViews;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.AppWidgetTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserInfoWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.user_info_widget);

        Intent intent = new Intent(context, LoginActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        views.setOnClickPendingIntent(R.id.rl_main, pendingIntent);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        DatabaseReference realtimeRef;

        if(auth.getCurrentUser() != null) {

            views.setViewVisibility(R.id.tv_no_active_user, View.GONE);
            views.setViewVisibility(R.id.pb_widget, View.VISIBLE);

            realtimeRef = FirebaseDatabase.getInstance().getReference();

            views.setTextViewText(R.id.tv_username, auth.getCurrentUser().getDisplayName());

            if(auth.getCurrentUser().getPhotoUrl() != null) {

                Uri profileUri = Uri.parse(auth.getCurrentUser().getPhotoUrl().toString());

                AppWidgetTarget awt = new AppWidgetTarget(context, R.id.iv_profile, views, appWidgetId) {
                    @Override
                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                        super.onResourceReady(resource, transition);
                    }
                };

                RequestOptions options = new RequestOptions().
                        override(300, 300).placeholder(R.drawable.ic_person_gray_100dp).error(R.drawable.ic_person_gray_100dp);


                Glide.with(context.getApplicationContext())
                        .asBitmap()
                        .load(profileUri)
                        .apply(options)
                        .into(awt);

            }

            realtimeRef.child("users")
                    .child(auth.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if(dataSnapshot.exists()) {
                                User user = dataSnapshot.getValue(User.class);

                                views.setTextViewText(R.id.tv_likes_count,String.valueOf(user.likes));
                                views.setTextViewText(R.id.tv_dislikes_count, String.valueOf(user.dislikes));
                                views.setTextViewText(R.id.tv_votes_count, String.valueOf(user.votes));
                                views.setTextViewText(R.id.tv_comments_count, String.valueOf(user.comments));

                                views.setViewVisibility(R.id.tv_no_active_user, View.GONE);
                                views.setViewVisibility(R.id.pb_widget, View.GONE);
                                views.setViewVisibility(R.id.ll_image, View.VISIBLE);
                                views.setViewVisibility(R.id.tv_username, View.VISIBLE);
                                views.setViewVisibility(R.id.ll_data, View.VISIBLE);

                                appWidgetManager.updateAppWidget(appWidgetId, views);

                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

        } else {

            views.setViewVisibility(R.id.pb_widget, View.GONE);
            views.setViewVisibility(R.id.tv_no_active_user, View.VISIBLE);
            appWidgetManager.updateAppWidget(appWidgetId, views);


        }

    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
    }

    @Override
    public void onDisabled(Context context) {
    }

    public static void sendRefreshBroadcast(Context context) {
        Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.setComponent(new ComponentName(context, UserInfoWidget.class));
        context.sendBroadcast(intent);
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        final String action = intent.getAction();
        if (action.equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE)) {
            AppWidgetManager mgr = AppWidgetManager.getInstance(context);
            ComponentName cn = new ComponentName(context, UserInfoWidget.class);
            mgr.notifyAppWidgetViewDataChanged(mgr.getAppWidgetIds(cn), R.id.rl_main);

            for (int appWidgetId : mgr.getAppWidgetIds(cn)) {
                updateAppWidget(context, mgr, appWidgetId);
            }
            onUpdate(context,mgr, mgr.getAppWidgetIds(cn));
        }
    }
}

