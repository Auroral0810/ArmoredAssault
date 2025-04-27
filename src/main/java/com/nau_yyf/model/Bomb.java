package com.nau_yyf.model;

public class Bomb extends GameObject {
    private long placedTime;

    public Bomb(int x, int y, long placedTime) {
        super(x, y, 30, 30); // 炸弹大小为30x30
        this.placedTime = placedTime;
    }

    public long getPlacedTime() {
        return placedTime;
    }

    public boolean shouldExplode() {
        return System.currentTimeMillis() - placedTime > 5000; // 5秒后爆炸
    }
} 