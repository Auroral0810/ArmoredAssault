package com.nau_yyf.view;

import java.util.Map;

/**
 * 游戏结束屏幕通用接口
 * 所有游戏模式的结束屏幕都应实现此接口
 */
public interface GameOverScreen {
    /**
     * 显示游戏结束界面
     * @param gameData 游戏数据，包含得分、击败敌人数、剩余生命等信息
     */
    void show(Map<String, Object> gameData);
} 