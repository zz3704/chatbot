package com.inhatc.realchatbotpj;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainPageActivity extends AppCompatActivity {

    ImageView photoContainer;
    TextView txtAnimalName;
    Button btnProfile;
    String animalType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainpage);

        photoContainer = findViewById(R.id.photoContainer);
        txtAnimalName = findViewById(R.id.animalNameText);
        btnProfile = findViewById(R.id.btnProfile);

        animalType = getIntent().getStringExtra("animal_type");
        final String animalName = getIntent().getStringExtra("animal");


        if (animalType != null) {
            if (animalName != null && !animalName.trim().isEmpty()) {
                txtAnimalName.setText(animalName);
            } else {
                txtAnimalName.setText("코코");
            }

            switch (animalType) {
                case "강아지":
                    photoContainer.setImageResource(R.drawable.dog);
                    break;
                case "고양이":
                    photoContainer.setImageResource(R.drawable.cat);
                    break;
                case "원숭이":
                    photoContainer.setImageResource(R.drawable.monkey);
                    break;
                default:
                    photoContainer.setImageResource(R.drawable.dog);
                    break;
            }
        } else {
            txtAnimalName.setText("코코");
            photoContainer.setImageResource(R.drawable.dog);
        }

        btnProfile.setOnClickListener(v -> {
            String username = getIntent().getStringExtra("username");

            Intent chatIntent = new Intent(MainPageActivity.this, WelcomeActivity.class);
            chatIntent.putExtra("username", username);
            chatIntent.putExtra("animal", animalName);
            chatIntent.putExtra("animal_type", animalType);
            startActivity(chatIntent);
        });
    }
}
