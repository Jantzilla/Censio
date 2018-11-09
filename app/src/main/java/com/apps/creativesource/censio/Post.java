package com.apps.creativesource.censio;

import com.google.firebase.firestore.DocumentReference;

public class Post {

    public String author, statement, firestoreId, userRef;
    public int likes, dislikes, interactionCount = 0;
    public int postTypeId;
    public long timestamp = 0;
}
