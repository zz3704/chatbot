package com.inhatc.realchatbotpj;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginActivity extends AppCompatActivity {

    EditText editUsername, editPassword;
    Button btnLogin, btnGoToRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        editUsername = findViewById(R.id.editUsername);
        editPassword = findViewById(R.id.editPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoToRegister = findViewById(R.id.btnGoToRegister);

        btnLogin.setOnClickListener(v -> {
            String username = editUsername.getText().toString();
            String password = editPassword.getText().toString();

            new Thread(() -> {
                try {
                    URL url = new URL("http://10.0.2.2:8000/api/login/");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json; utf-8");
                    conn.setDoOutput(true);

                    JSONObject jsonParam = new JSONObject();
                    jsonParam.put("username", username);
                    jsonParam.put("password", password);

                    OutputStream os = conn.getOutputStream();
                    os.write(jsonParam.toString().getBytes("utf-8"));
                    os.close();

                    int responseCode = conn.getResponseCode();

                    InputStream inputStream = (responseCode == 200) ? conn.getInputStream() : conn.getErrorStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    JSONObject jsonResponse = new JSONObject(response.toString());

                    runOnUiThread(() -> {
                        if (responseCode == 200) {
                            String animal = jsonResponse.optString("animal", "코코");
                            Intent intent = new Intent(this, MainPageActivity.class);
                            intent.putExtra("username", username);
                            intent.putExtra("animal", animal);
                            startActivity(intent);
                            finish();
                        } else {
                            String error = jsonResponse.optString("error", "로그인 실패");
                            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() ->
                            Toast.makeText(this, "에러 발생: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
                }
            }).start();
        });

        btnGoToRegister.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
        });
    }
}
