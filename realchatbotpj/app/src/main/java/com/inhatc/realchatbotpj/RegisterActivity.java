package com.inhatc.realchatbotpj;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class RegisterActivity extends AppCompatActivity {

    EditText editUsername, editPassword, editPersonality, editAnimalName;
    Spinner spinnerAnimalType;
    Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        editUsername = findViewById(R.id.editUsername);
        editPassword = findViewById(R.id.editPassword);
        editPersonality = findViewById(R.id.editPersonality);
        editAnimalName = findViewById(R.id.editAnimalName);
        spinnerAnimalType = findViewById(R.id.spinnerAnimalType);
        btnRegister = findViewById(R.id.btnRegister);

        String[] animalTypes = {"강아지", "고양이", "원숭이"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, animalTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAnimalType.setAdapter(adapter);

        btnRegister.setOnClickListener(v -> {
            String username = editUsername.getText().toString().trim();
            String password = editPassword.getText().toString().trim();
            String personality = editPersonality.getText().toString().trim();
            String animalName = editAnimalName.getText().toString().trim();
            String animalType = spinnerAnimalType.getSelectedItem().toString();

            if(username.isEmpty() || password.isEmpty() || personality.isEmpty() || animalName.isEmpty()) {
                Toast.makeText(this, "모든 항목을 입력하세요", Toast.LENGTH_SHORT).show();
                return;
            }

            new Thread(() -> {
                try {
                    URL url = new URL("http://10.0.2.2:8000/api/register/");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json; utf-8");
                    conn.setDoOutput(true);

                    JSONObject jsonParam = new JSONObject();
                    jsonParam.put("username", username);
                    jsonParam.put("password", password);
                    jsonParam.put("personality", personality);
                    jsonParam.put("animal", animalName);
                    jsonParam.put("animal_type", animalType);

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
                            Toast.makeText(this, "회원가입 완료", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(this, LoginActivity.class);
                            intent.putExtra("animal_type", animalType);
                            startActivity(intent);
                            finish();
                        } else {
                            String error = jsonResponse.optString("error", "회원가입 실패");
                            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
                        }
                    });

                    conn.disconnect();

                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> Toast.makeText(this, "에러: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            }).start();
        });
    }
}
