package com.example.biscuit.email;

import android.content.Context;

import com.example.biscuit.database.DatabaseHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.SecureRandom;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EmailService {

    private final Context context;

    public EmailService(Context context){
        this.context = context;
    }

    public void sendEmail(String email, String validationCode) {
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        OkHttpClient client = new OkHttpClient();

        JSONObject json = new JSONObject();
        try {
            json.put("sender", "Biscuit");
            json.put("to",email);
            json.put("username", "Parent");
            json.put("templateName","EMAIL_VERIFICATION");
            json.put("code",validationCode);
            json.put("subject","Email validation");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            Request request = new Request.Builder()
                    .url("https://unemployed-kass-bicuit-5acd1cc4.koyeb.app/api/email/send")
                    .post(RequestBody.create(
                            String.valueOf(json),
                            MediaType.parse("application/json")
                    ))
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override public void onFailure(Call call, IOException e) { e.printStackTrace(); }
                @Override public void onResponse(Call call, Response response) { response.close(); }
            });
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        databaseHelper.saveCode(validationCode, email);
    }

    public String generateValidationCode() {
        String codeCharacters = "0123456789";
        StringBuilder codeBuilder = new StringBuilder();
        SecureRandom secureRandom = new SecureRandom();
        for (int i = 0; i < 5; i++) {
            int randomIndex = secureRandom.nextInt(codeCharacters.length());
            codeBuilder.append(codeCharacters.charAt(randomIndex));
        }
        return codeBuilder.toString();
    }
}
