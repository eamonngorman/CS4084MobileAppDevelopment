package com.example.cs4084mobileappdevelopment;

import static com.google.gson.JsonParser.parseReader;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
                .addQueryParameter("rel_jja", word1)
                .build();

        HttpUrl requestUrlVerb = new HttpUrl.Builder()
                .scheme("https")
                .host("api.datamuse.com")
                .addPathSegment("words")
                .addQueryParameter("rel_jja", word2)
                .build();

        HttpUrl requestUrlNoun = new HttpUrl.Builder()
                .scheme("https")
                .host("api.datamuse.com")
                .addPathSegment("words")
                .addQueryParameter("rel_jja", word3)
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
//                        System.out.println("Related word: " + word);
                        userWords.add(word);
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


}
