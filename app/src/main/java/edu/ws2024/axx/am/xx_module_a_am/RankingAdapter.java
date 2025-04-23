package edu.ws2024.axx.am.xx_module_a_am;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class RankingAdapter extends ArrayAdapter<RankingItem> {
    private Context context;
    private int resource;
    private List<RankingItem> rankingItems;

    public RankingAdapter(Context context, int resource, List<RankingItem> rankingItems) {
        super(context, resource, rankingItems);
        this.context = context;
        this.resource = resource;
        this.rankingItems = rankingItems;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(resource, parent, false);
        }

        TextView rankingTextView = convertView.findViewById(R.id.ranking_text);
        TextView playerNameTextView = convertView.findViewById(R.id.player_name_text);
        TextView coinTextView = convertView.findViewById(R.id.coin_text);
        TextView durationTextView = convertView.findViewById(R.id.duration_text);

        RankingItem item = rankingItems.get(position);
        rankingTextView.setText(String.valueOf(item.getRanking()));
        playerNameTextView.setText(item.getPlayerName());
        coinTextView.setText(String.valueOf(item.getCoin()));
        durationTextView.setText(String.valueOf(item.getDuration()) + " s");

        return convertView;
    }
}