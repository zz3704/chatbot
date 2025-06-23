package com.inhatc.realchatbotpj;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainPageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainpage);

        // "동물과 대화" 버튼 연결
        Button btnProfile = findViewById(R.id.btnProfile);

        // 버튼 클릭 시 WelcomeActivity로 이동
        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainPageActivity.this, WelcomeActivity.class);
                startActivity(intent);
            }
        });
    }
}
