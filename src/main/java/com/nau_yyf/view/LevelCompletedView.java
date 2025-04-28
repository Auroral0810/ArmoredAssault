package com.nau_yyf.view;

import java.util.Map;

/**
 * 关卡完成视图通用接口
 * 所有游戏模式的关卡完成视图都应实现此接口
 */
public interface LevelCompletedView {
    /**
     * 显示关卡完成界面
     * @param levelData 关卡数据，包含当前关卡、击败敌人数、游戏时间等信息
     */
    void show(Map<String, Object> levelData);
} 