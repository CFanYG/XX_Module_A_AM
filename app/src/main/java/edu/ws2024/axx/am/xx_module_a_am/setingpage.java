package edu.ws2024.axx.am.xx_module_a_am;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class setingpage extends AppCompatActivity {
    private ImageView skierImageView;
    private SeekBar colorSeekBar;
    private Bitmap originalBitmap;
    private int currentColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setingpage);

        skierImageView = findViewById(R.id.sking_peple);
        colorSeekBar = findViewById(R.id.colorseekbar);
        Button doneButton = findViewById(R.id.done_button);

        // 加载原始图片
        originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.skiing_person);

        colorSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentColor = getColorFromProgress(progress);
                Bitmap modifiedBitmap = changeNonBlackPixelsColor(originalBitmap, currentColor);
                skierImageView.setImageBitmap(modifiedBitmap);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // 开始拖动进度条时的操作
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // 停止拖动进度条时的操作
            }
        });

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveColorSetting(currentColor);
                // 添加提示信息，告知用户颜色已保存
                Toast.makeText(setingpage.this, "颜色已保存", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private int getColorFromProgress(int progress) {
        float hue = (float) progress / 100 * 360;
        float saturation = 1f;
        float value = 1f;
        int alpha = 255;
        return Color.HSVToColor(alpha, new float[]{hue, saturation, value});
    }

    private Bitmap changeNonBlackPixelsColor(Bitmap sourceBitmap, int newColor) {
        // 使用 Objects.requireNonNull 进行非空处理
        Bitmap.Config config = Objects.requireNonNull(sourceBitmap.getConfig());
        Bitmap resultBitmap = sourceBitmap.copy(config, true);
        int width = resultBitmap.getWidth();
        int height = resultBitmap.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixelColor = resultBitmap.getPixel(x, y);
                if (pixelColor != Color.BLACK) {
                    int alpha = Color.alpha(pixelColor);
                    int newPixelColor = Color.argb(alpha, Color.red(newColor), Color.green(newColor), Color.blue(newColor));
                    resultBitmap.setPixel(x, y, newPixelColor);
                }
            }
        }
        return resultBitmap;
    }

    private void saveColorSetting(int color) {
        SharedPreferences.Editor editor = getSharedPreferences("game_settings", MODE_PRIVATE).edit();
        editor.putInt("character_color", color);
        editor.apply();
    }
}