package com.nau_yyf.controller;

import com.nau_yyf.view.GameScreen;

/**
 * 游戏启动器通用接口
 * 负责初始化和启动游戏
 */
public interface GameStarter {
    /**
     * 获取游戏屏幕
     */
    GameScreen getGameScreen();
    
    /**
     * 获取游戏控制器
     */
    GameController getGameController();
    
    /**
     * 设置游戏控制器
     */
    void setGameController(GameController controller);
} 