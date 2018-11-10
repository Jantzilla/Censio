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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

/**
 * Implementation of App Widget functionality.
 */
public class UserInfoWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.user_info_widget);

        Intent intent = new Intent(context, LoginActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        views.setOnClickPendingIntent(R.id.rl_main, pendingIntent);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore firestore;

        if(auth.getCurrentUser() != null) {

            views.setViewVisibility(R.id.tv_no_active_user, View.GONE);
            views.setViewVisibility(R.id.pb_widget, View.VISIBLE);

            firestore = FirebaseFirestore.getInstance();

            Uri profileUri = Uri.parse(auth.getCurrentUser().getPhotoUrl().toString());

            views.setTextViewText(R.id.tv_username, auth.getCurrentUser().getDisplayName());

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

            firestore.collection("users")
                    .whereEqualTo("id", auth.getUid())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if(task.isSuccessful()) {
                                for(QueryDocumentSnapshot document : task.getResult()) {
                                    views.setTextViewText(R.id.tv_likes_count,String.valueOf(document.getLong("likes")));
                                    views.setTextViewText(R.id.tv_dislikes_count, String.valueOf(document.getLong("dislikes")));
                                    views.setTextViewText(R.id.tv_votes_count, String.valueOf(document.getLong("votes")));
                                    views.setTextViewText(R.id.tv_comments_count, String.valueOf(document.getLong("comments")));

                                    views.setViewVisibility(R.id.tv_no_active_user, View.GONE);
                                    views.setViewVisibility(R.id.pb_widget, View.GONE);
                                    views.setViewVisibility(R.id.ll_image, View.VISIBLE);
                                    views.setViewVisibility(R.id.tv_username, View.VISIBLE);
                                    views.setViewVisibility(R.id.ll_data, View.VISIBLE);

                                    // Instruct the widget manager to update the widget
                                    appWidgetManager.updateAppWidget(appWidgetId, views);

                                }
                            }
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
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
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
            // refresh all your widgets
            AppWidgetManager mgr = AppWidgetManager.getInstance(context);
            ComponentName cn = new ComponentName(context, UserInfoWidget.class);
            mgr.notifyAppWidgetViewDataChanged(mgr.getAppWidgetIds(cn), R.id.rl_main);

            for (int appWidgetId : mgr.getAppWidgetIds(cn)) {
                updateAppWidget(context, mgr, appWidgetId);
            }
//            updateAppWidget(context,mgr, mgr.getAppWidgetIds(cn));
            onUpdate(context,mgr, mgr.getAppWidgetIds(cn));
        }
    }
}

