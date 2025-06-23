package com.inhatc.realchatbotpj;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class WelcomeActivity extends AppCompatActivity {

    EditText editMessage;
    Button btnSend;
    TextView chatOutput;
    ScrollView scrollView;
    String username, animal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome);

        username = getIntent().getStringExtra("username");
        animal = getIntent().getStringExtra("animal");
        if (animal == null || animal.isEmpty()) animal = "코코";

        TextView welcomeText = findViewById(R.id.welcomeText);
        welcomeText.setText("환영합니다, " + username + "님!");

        editMessage = findViewById(R.id.editMessage);
        btnSend = findViewById(R.id.btnSend);
        chatOutput = findViewById(R.id.chatOutput);
        scrollView = findViewById(R.id.scrollView);

        btnSend.setOnClickListener(v -> {
            String message = editMessage.getText().toString().trim();
            if (!message.isEmpty()) {
                appendMessage("나: " + message);
                sendMessageToServer(username, message);
                editMessage.setText("");
            }
        });
    }

    private void appendMessage(String text) {
        chatOutput.append(text + "\n\n");
        scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
    }

    private void sendMessageToServer(String username, String message) {
        new Thread(() -> {
            try {
                URL url = new URL("http://10.0.2.2:8000/api/chat/");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setDoOutput(true);

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("username", username);
                jsonParam.put("message", message);

                OutputStream os = conn.getOutputStream();
                os.write(jsonParam.toString().getBytes("utf-8"));
                os.close();

                InputStream inputStream = (conn.getResponseCode() == 200) ? conn.getInputStream() : conn.getErrorStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                String reply = jsonResponse.optString("reply", "응답 없음");

                runOnUiThread(() -> appendMessage(animal + ": " + reply));
                conn.disconnect();
            } catch (Exception ignored) {

            }
        }).start();
    }
}
