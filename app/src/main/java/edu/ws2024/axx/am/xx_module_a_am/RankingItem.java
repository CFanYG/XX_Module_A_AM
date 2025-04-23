package edu.ws2024.axx.am.xx_module_a_am;

import android.os.Parcel;
import android.os.Parcelable;

public class RankingItem implements Parcelable {
    private int ranking;
    private String playerName;
    private int coin;
    private long duration;

    public RankingItem(int ranking, String playerName, int coin, long duration) {
        this.ranking = ranking;
        this.playerName = playerName;
        this.coin = coin;
        this.duration = duration;
    }

    // 实现Parcelable接口必须的方法
    protected RankingItem(Parcel in) {
        ranking = in.readInt();
        playerName = in.readString();
        coin = in.readInt();
        duration = in.readLong();
    }

    public static final Creator<RankingItem> CREATOR = new Creator<RankingItem>() {
        @Override
        public RankingItem createFromParcel(Parcel in) {
            return new RankingItem(in);
        }

        @Override
        public RankingItem[] newArray(int size) {
            return new RankingItem[size];
        }
    };

    public int getRanking() {
        return ranking;
    }

    public void setRanking(int ranking) {
        this.ranking = ranking;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public int getCoin() {
        return coin;
    }

    public void setCoin(int coin) {
        this.coin = coin;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(ranking);
        dest.writeString(playerName);
        dest.writeInt(coin);
        dest.writeLong(duration);
    }
}