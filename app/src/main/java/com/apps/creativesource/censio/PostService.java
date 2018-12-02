package com.apps.creativesource.censio;

import android.app.IntentService;
import android.content.Intent;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class PostService extends IntentService {
    private DatabaseReference realtimeRef = FirebaseDatabase.getInstance().getReference();

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */

    public PostService(String name) {
        super(name);
    }

    public PostService() {
        super("DisplayNotification");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        String choiceName =(String) intent.getExtras().get("title");
        Long postId = intent.getLongExtra("postId",0);
        int count = intent.getIntExtra("count", 0);


        Map<String, Object> choice = new HashMap<>();
        choice.put("title", choiceName);
        choice.put("count", count);

        realtimeRef.child("posts").child(String.valueOf(postId)).child("choices").child(String.valueOf(System.nanoTime())).setValue(choice);

    }
}
