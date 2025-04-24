package edu.ws2024.axx.am.xx_module_a_am;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Objects;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class Runing extends AppCompatActivity implements SensorEventListener {
    // 用于记录上次生成障碍物的时间
    private AtomicLong lastObstacleGeneratedTime = new AtomicLong(0);
    // 用于记录上次生成金币的时间
    private AtomicLong lastCoinGeneratedTime = new AtomicLong(0);
    // 最小生成间隔时间（单位：毫秒）
    private static final long MIN_GENERATION_INTERVAL = 2000;
    // 用于控制跳跃动画的属性动画对象
    private ObjectAnimator jumpAnimator;
    // 记录滑雪者初始Y坐标
    private float initialY;
    private ImageView gameCharacterImageView;
    private Bitmap originalCharacterBitmap;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final String[] PERMISSIONS_STORAGE = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private ImageView skierView;
    private Button pauseButton;
    private TextView playerNameText, coinQuantityText, durationText;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private boolean isPaused = false;
    private boolean isGameOver = false;
    private int coinCount = 0;
    private long startTime;
    private long gameDuration = 0;
    private MediaPlayer backgroundMusic;
    private MediaPlayer jumpSound;
    private MediaPlayer coinSound;
    private MediaPlayer gameOverSound;
    //    private Handler handler = new Handler();
    private Runnable obstacleGenerator;
    private Runnable coinGenerator;
    private boolean isInvincible = false;
    private int invincibleCoins = 0;
    private Random random = new Random();
    private ConstraintLayout gameSceneLayout;
    private ImageView snowView;
    private long gameStartTime;
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            // 根据消息类型调用相应的生成方法
            if (msg.what == 1) {
                generateObstacle();
            } else if (msg.what == 2) {
                generateCoin();
            }
        }
    };
    private Handler collisionHandler = new Handler();
    private Runnable collisionChecker = new Runnable() {
        @Override
        public void run() {
            if (!isPaused &&!isGameOver) {
                float skierX = skierView.getX();
                float skierY = skierView.getY();
                for (int i = 0; i < gameSceneLayout.getChildCount(); i++) {
                    View child = gameSceneLayout.getChildAt(i);
                    if (child.getTag() != null && child.getTag().equals("obstacle")) {
                        float obstacleX = child.getX();
                        float obstacleY = child.getY();
                        if (skierY >= obstacleY && skierX + skierView.getWidth() >= obstacleX && skierX <= obstacleX + child.getWidth()) {
                            if (!isInvincible) {
                                gameOver();
                            }
                        }
                    } else if (child.getTag() != null && child.getTag().equals("coin")) {
                        float coinX = child.getX();
                        if (coinX <= skierX + skierView.getWidth() && coinX + child.getWidth() >= skierX) {
                            coinCount++;
                            coinQuantityText.setText("" + coinCount);
                            coinSound.start();
                            gameSceneLayout.removeView(child);
                        }
                    }
                }
                collisionHandler.postDelayed(this, 100);
            }
        }
    };


    // 随机数生成器，用于生成随机位置和随机延迟时间
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_page);
        // 游戏场景布局，用于添加障碍物和金币的ImageView
        gameSceneLayout = findViewById(R.id.pm);
        gameCharacterImageView = findViewById(R.id.sking_peple);
        // 加载原始角色图片
        originalCharacterBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.skiing_person);
        // 读取保存的颜色值
        SharedPreferences prefs = getSharedPreferences("game_settings", MODE_PRIVATE);
        int savedColor = prefs.getInt("character_color", Color.WHITE); // 默认颜色为白色
        // 应用保存的颜色
        Bitmap modifiedBitmap = changeNonBlackPixelsColor(originalCharacterBitmap, savedColor);
        gameCharacterImageView.setImageBitmap(modifiedBitmap);
        skierView = findViewById(R.id.sking_peple);
        pauseButton = findViewById(R.id.pause);
        playerNameText = findViewById(R.id.player_name_text);
        coinQuantityText = findViewById(R.id.gold);
        durationText = findViewById(R.id.sacende);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        // 初始化音乐
        backgroundMusic = MediaPlayer.create(this, R.raw.bgm);
        jumpSound = MediaPlayer.create(this, R.raw.jump);
        coinSound = MediaPlayer.create(this, R.raw.coin);
        gameOverSound = MediaPlayer.create(this, R.raw.game_over);
        // 游戏开始设置
        coinCount = 10;
        coinQuantityText.setText("" + coinCount);
        startTime = SystemClock.elapsedRealtime();
        gameStartTime = System.currentTimeMillis();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        backgroundMusic.start();
        startRecording();
        // 暂停按钮点击事件
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPaused) {
                    resumeGame();
                } else {
                    pauseGame();
                }
            }
        });
        // 屏幕点击事件
        View view = findViewById(R.id.pm);
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    skierJump();
                }
                return true;
            }
        });
        // 长按屏幕进入无敌模式
        skierView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (coinCount > 0) {
                    isInvincible = true;
                    invincibleCoins = 1;
                    coinCount--;
                    coinQuantityText.setText("" + coinCount);
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (isInvincible) {
                                if (coinCount > 0) {
                                    coinCount--;
                                    coinQuantityText.setText("" + coinCount);
                                } else {
                                    isInvincible = false;
                                }
                                handler.postDelayed(this, 1000);
                            }
                        }
                    }, 1000);
                }
                return true;
            }
        });
        // 启动障碍物和金币生成任务
        startObstacleGenerator();
        startCoinGenerator();
        snowView = findViewById(R.id.snow);
        collisionHandler.postDelayed(collisionChecker, 100);
    }

    private Bitmap changeNonBlackPixelsColor(Bitmap sourceBitmap, int newColor) {
        // 使用Objects.requireNonNull进行非空处理
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

    // 开始录制屏幕（需添加权限和具体实现）
    private void startRecording() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
        } else {
            // 屏幕录制的具体代码，待实现
        }
    }

    // 滑雪者跳跃
    private void skierJump() {
        if (!isPaused &&!isGameOver) {
            jumpSound.start();
            // 跳跃动画相关逻辑
            if (jumpAnimator != null && jumpAnimator.isRunning()) {
                return;
            }
            ImageView skierImage = findViewById(R.id.sking_peple);
            // 创建属性动画，控制滑雪者Y坐标变化实现跳跃效果
            jumpAnimator = ObjectAnimator.ofFloat(skierImage, "y", initialY + 2100, initialY + 2100 - 300f, initialY + 2100);
            jumpAnimator.setDuration(400); // 动画时长400毫秒
            jumpAnimator.setInterpolator(new AccelerateDecelerateInterpolator()); // 加速减速插值器
            jumpAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    // 动画结束后重置相关状态
                    jumpAnimator = null;
                }
            });
            jumpAnimator.start();
        }
    }

    // 暂停游戏
    private void pauseGame() {
        isPaused = true;
        pauseButton.setText("Play");
        backgroundMusic.pause();
        sensorManager.unregisterListener(this);
        handler.removeCallbacks(obstacleGenerator);
        handler.removeCallbacks(coinGenerator);
        collisionHandler.removeCallbacks(collisionChecker);
    }

    // 恢复游戏
    private void resumeGame() {
        isPaused = false;
        pauseButton.setText("Pause");
        backgroundMusic.start();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        startObstacleGenerator();
        startCoinGenerator();
        collisionHandler.postDelayed(collisionChecker, 100);
    }

    // 开始生成障碍物任务
    private void startObstacleGenerator() {
        obstacleGenerator = new Runnable() {
            @Override
            public void run() {
                if (!isPaused &&!isGameOver) {
                    // 获取当前时间
                    long currentTime = System.currentTimeMillis();
                    // 计算距离上次生成障碍物的时间间隔
                    long elapsedTime = currentTime - lastObstacleGeneratedTime.get();
                    if (elapsedTime >= MIN_GENERATION_INTERVAL) {
                        // 如果时间间隔大于等于最小生成间隔，发送生成障碍物的消息
                        handler.sendEmptyMessage(1);
                        // 更新上次生成障碍物的时间
                        lastObstacleGeneratedTime.set(currentTime);
                    }
                    // 随机延迟一段时间后再次执行此任务
                    handler.postDelayed(this, random.nextInt(3000) + 2000);
                }
            }
        };
        // 随机延迟一段时间后开始执行障碍物生成任务
        handler.postDelayed(obstacleGenerator, random.nextInt(5000) + 1000);
    }

    // 生成障碍物的方法
    private void generateObstacle() {
        ImageView obstacle = new ImageView(this);
        obstacle.setImageResource(R.drawable.obstacle);
        // 随机生成障碍物在右下方的初始x坐标
        int x = gameSceneLayout.getWidth() - obstacle.getDrawable().getIntrinsicWidth();
        int y = gameSceneLayout.getHeight() - obstacle.getDrawable().getIntrinsicHeight();
        obstacle.setX(x);
        obstacle.setY(y);
        obstacle.setTag("obstacle");
        gameSceneLayout.addView(obstacle);
        startMovingView(obstacle);
    }

    // 开始生成金币任务
    private void startCoinGenerator() {
        coinGenerator = new Runnable() {
            @Override
            public void run() {
                if (!isPaused &&!isGameOver) {
                    // 获取当前时间
                    long currentTime = System.currentTimeMillis();
                    // 计算距离上次生成金币的时间间隔
                    long elapsedTime = currentTime - lastCoinGeneratedTime.get();
                    if (elapsedTime >= MIN_GENERATION_INTERVAL) {
                        // 如果时间间隔大于等于最小生成间隔，发送生成金币的消息
                        handler.sendEmptyMessage(2);
                        // 更新上次生成金币的时间
                        lastCoinGeneratedTime.set(currentTime);
                    }
                    // 随机延迟一段时间后再次执行此任务
                    handler.postDelayed(this, random.nextInt(3000) + 2000);
                }
            }
        };
        // 随机延迟一段时间后开始执行金币生成任务
        handler.postDelayed(coinGenerator, random.nextInt(5000) + 1000);
    }

    // 生成金币的方法
    private void generateCoin() {
        ImageView coin = new ImageView(this);
        coin.setImageResource(R.drawable.coin);
        // 随机生成金币在右下方的初始x坐标
        int x = gameSceneLayout.getWidth() - coin.getDrawable().getIntrinsicWidth();
        int y = gameSceneLayout.getHeight() - coin.getDrawable().getIntrinsicHeight();
        coin.setX(x);
        coin.setY(y);
        coin.setTag("coin");
        gameSceneLayout.addView(coin);
        startMovingView(coin);
    }

    // 开始移动视图的方法
    private void startMovingView(final ImageView view) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isPaused &&!isGameOver) {
                    float newX = view.getX() - 10; // 根据实际情况调整移动速度
                    if (newX < -view.getDrawable().getIntrinsicWidth()) {
                        gameSceneLayout.removeView(view);
                    } else {
                        view.setX(newX);
                        // 根据雪地坡度调整y坐标，这里假设坡度为snowView.getRotation()
                        float slope = snowView.getRotation();
                        float newY = view.getY() + (float) (Math.sin(Math.toRadians(slope)) * 10);
                        view.setY(newY);
                        handler.postDelayed(this, 50);
                    }
                }
            }
        }, 50);
    }


    // 处理传感器事件
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            if (!isPaused &&!isGameOver) {
                // 根据x值处理游戏速度、倾斜等逻辑
                float rotationDegree = x * 10; // 根据实际情况调整系数
                if (rotationDegree > 30) {
                    rotationDegree = 30;
                } else if (rotationDegree < -30) {
                    rotationDegree = -30;
                }
                snowView.setRotation(rotationDegree);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    // 处理权限请求结果
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startRecording();
            } else {
                Toast.makeText(this, "权限申请被拒绝，无法进行屏幕录制", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 游戏结束逻辑
    private void gameOver() {
        isGameOver = true;
        backgroundMusic.stop();
        gameOverSound.start();
        handler.removeCallbacks(obstacleGenerator);
        handler.removeCallbacks(coinGenerator);
        sensorManager.unregisterListener(this);
        gameDuration = (System.currentTimeMillis() - gameStartTime) / 1000;
        durationText.setText("Duration: " + gameDuration + "s");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Game Over")
                .setMessage("Player Name: \nCoin Quantity: " + coinCount + "\nDuration: " + gameDuration + "s")
                .setPositiveButton("Restart", (dialog, which) -> restartGame())
                .setNegativeButton("To Rankings", (dialog, which) -> goToRankings())
                .show();
    }

    // 重启游戏
    private void restartGame() {
        isGameOver = false;
        isPaused = false;
        coinCount = 10;
        coinQuantityText.setText("" + coinCount);
        startTime = SystemClock.elapsedRealtime();
        gameStartTime = System.currentTimeMillis();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        backgroundMusic.start();
        startObstacleGenerator();
        startCoinGenerator();
        collisionHandler.postDelayed(collisionChecker, 100);
        // 移除所有障碍物和金币
        gameSceneLayout.removeAllViews();
        // 重置滑雪者位置
        skierView.setX(0);
        skierView.setY(initialY);
    }

    // 前往排行榜
    private void goToRankings() {
        Intent intent = new Intent(this, RankingPage.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        backgroundMusic.release();
        jumpSound.release();
        coinSound.release();
        gameOverSound.release();
        sensorManager.unregisterListener(this);
    }
}