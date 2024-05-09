package com.example.cs4084mobileappdevelopment;

import static com.google.gson.JsonParser.parseReader;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;


import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UserNameHandler {

    String userName = "";
    public UserNameHandler(){

    }

//    List<String> randomWord = new ArrayList<>();
//    randomWord.push(1, "word1");
//    randomWord.add("word2");
    private final OkHttpClient client = new OkHttpClient();

    public String getRandomWord() {

        List<String> randomWord = new ArrayList<>();
        randomWord.add("animal");
        randomWord.add("fruit");
        randomWord.add("structure");
        randomWord.add("nature");
        randomWord.add("element");
        randomWord.add("mood");
        randomWord.add("appliance");
        randomWord.add("meat");
        randomWord.add("electronic");
        randomWord.add("place");

        String word1 = randomWord.get((int) (Math.random() * randomWord.size()));
        String word2 = randomWord.get((int) (Math.random() * randomWord.size()));
        String word3 = randomWord.get((int) (Math.random() * randomWord.size()));


        String url = "https://api.datamuse.com/words";

        List<HttpUrl> urls = new ArrayList<>();
        HttpUrl requestUrlAdjetive = new HttpUrl.Builder()
                .scheme("https")
                .host("api.datamuse.com")
                .addPathSegment("words")
                .addQueryParameter("rel_jjb", word1)
                .build();

        HttpUrl requestUrlVerb = new HttpUrl.Builder()
                .scheme("https")
                .host("api.datamuse.com")
                .addPathSegment("words")
                .addQueryParameter("rel_trg", word3)
                .build();

        HttpUrl requestUrlNoun = new HttpUrl.Builder()
                .scheme("https")
                .host("api.datamuse.com")
                .addPathSegment("words")
                .addQueryParameter("rel_jja", word2)
                .build();

        urls.add(requestUrlAdjetive);
        urls.add(requestUrlVerb);
        urls.add(requestUrlNoun);

        final CountDownLatch latch = new CountDownLatch(urls.size());

        for (int i = 0; i < urls.size(); i++) {
            ArrayList<String> userWords = new ArrayList<>();


            Request request = new Request.Builder()
                    .url(urls.get(i))
                    .build();

//            System.out.println("---------------------->" + i);

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    latch.countDown(); //count on fail tooo
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    assert response.body() != null;
                    JsonArray jsonArray = parseReader(response.body().charStream()).getAsJsonArray();

                    for (int j = 0; j < jsonArray.size(); j++) {
                        JsonObject jsonObject = jsonArray.get(j).getAsJsonObject();
                        String word = jsonObject.get("word").getAsString();
                        word = word.substring(0, 1).toUpperCase() + word.substring(1);;
//                        System.out.println("Related word: " + word);
                        userWords.add(word) ;
//                        System.out.println("SIZE +++++++++++" + userWords.size());
                    }

//                    System.out.println("SIZE +++++++++++" + userWords.size());
                    String word = userWords.get((int)(Math.random() * userWords.size()));
                    System.out.println(word);
                    synchronized (userName) {
                        userName = userName + word;
                    }
                    System.out.println(userName);


                    latch.countDown();
                }

            });

        }

        try {
            latch.await(); // Wait until all asynchronous tasks are complete.
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("USER: " + userName);
        return userName;
    }


    public interface QueryCallback {
        void onQueryCompleted(boolean result);
    }

    public void queryCheckUserName(String id, QueryCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference usernamesRef = db.collection("usernames");

        Query query = usernamesRef.whereEqualTo("userId", id);
        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot querySnapshot) {
//                System.out.println("TESTING     : " + querySnapshot.isEmpty());
                if (querySnapshot.isEmpty()) {
//                    System.out.println("Here 2: false");
                    callback.onQueryCompleted(false);
                } else {
//                    System.out.println("Here 1: true");
                    callback.onQueryCompleted(true);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
//                System.out.println("TESTING     : ");
                e.printStackTrace();
                callback.onQueryCompleted(false);
            }
        });
    }


    public void queryAddUsername(String id) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference usernamesRef = db.collection("usernames");

        String username = this.getRandomWord();

        Map<String, Object> postData = new HashMap<>();
        postData.put("username", username);
        postData.put("userId", id);

        usernamesRef.add(postData);
    }

    public interface QueryCallbackString {
        void onQueryCompletedString(String username);
    }

    public void getUserName(String id, QueryCallbackString callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference usernamesRef = db.collection("usernames");

        Query query = usernamesRef.whereEqualTo("userId", id);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String username = document.getString("username");
                        callback.onQueryCompletedString(username);
                    }
                } else {
                    System.err.println("Error getting documents: " + task.getException());
                    callback.onQueryCompletedString(null);
                }
            }
        });
    }

}
