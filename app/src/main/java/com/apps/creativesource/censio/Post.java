package com.apps.creativesource.censio;

import com.google.firebase.firestore.DocumentReference;

public class Post {

    public String author, statement, firestoreId;
    public int likes, dislikes, interactionCount = 0;
    public int postTypeId;
    public DocumentReference userRef = null;
    public long timestamp = 0;
}
