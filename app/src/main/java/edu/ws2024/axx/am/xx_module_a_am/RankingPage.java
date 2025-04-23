package edu.ws2024.axx.am.xx_module_a_am;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RankingPage extends AppCompatActivity {

    private ListView rankingsListView;
    private TextView noRankingText;
    private Button backButton;
    private List<RankingItem> rankingItems = new ArrayList<>();
    private RankingAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rankingpage);

        rankingsListView = findViewById(R.id.rankings_list);
        noRankingText = findViewById(R.id.no_ranking_text);
        backButton = findViewById(R.id.back_button);

        // 从Intent获取数据
        Intent intent = getIntent();
        if (intent.hasExtra("rankingItems")) {
            ArrayList<RankingItem> receivedItems = intent.getParcelableArrayListExtra("rankingItems");
            if (receivedItems != null) {
                rankingItems.addAll(receivedItems);
            }
        }

        // 按游戏时长降序排列
        Collections.sort(rankingItems, new Comparator<RankingItem>() {
            @Override
            public int compare(RankingItem o1, RankingItem o2) {
                return Long.compare(o2.getDuration(), o1.getDuration());
            }
        });

        adapter = new RankingAdapter(this, R.layout.ranking_item_layout, rankingItems);
        rankingsListView.setAdapter(adapter);

        // 检查是否有记录，没有则显示 "No Ranking"
        if (rankingItems.size() == 0) {
            noRankingText.setVisibility(View.VISIBLE);
            rankingsListView.setVisibility(View.GONE);
        }

        // 点击返回按钮回到主页
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent homeIntent = new Intent(RankingPage.this, Main.class);
                startActivity(homeIntent);
                finish();
            }
        });

        // 如果从游戏页面进入，高亮最近结束的游戏记录
        if (intent.hasExtra("fromGamePage")) {
            if (rankingItems.size() > 0) {
                // 这里简单设置第一个为高亮，实际可根据逻辑判断最近的
                rankingsListView.setItemChecked(0, true);
            }
        }
    }

    // 持久化存储排行榜数据，用sqlite数据库
    private void persistRankingList() {
        // 省略具体实现
    }
}



