package edu.ws2024.axx.am.xx_module_a_am;

import static edu.ws2024.axx.am.xx_module_a_am.util.Constant.*;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class Main extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savdInstanceState){
        super.onCreate(savdInstanceState);
        setContentView(R.layout.home_page);
        STATE = 0;

        Button StartButton = findViewById(R.id.buttonStart);
        StartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Main.this,Runing.class);
                startActivity(intent);
            }
        });

        Button rankingButton = findViewById(R.id.buttonRankings);
        rankingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Main.this, RankingPage.class);
                startActivity(intent);
            }
        });

        Button setingButton = findViewById(R.id.buttonSeting);
        setingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Main.this,setingpage.class);
                startActivity(intent);
            }
        });
    }
}
