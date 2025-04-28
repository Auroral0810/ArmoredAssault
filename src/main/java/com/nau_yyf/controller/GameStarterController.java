package com.nau_yyf.controller;

/**
 * 游戏启动器控制器接口
 * 定义了不同游戏模式启动器的通用方法
 */
public interface GameStarterController {
    /**
     * 获取游戏屏幕
     * @return 游戏屏幕
     */
    com.nau_yyf.view.GameScreen getGameScreen();
    
    /**
     * 启动游戏
     * @param tankType 坦克类型
     * @param level 关卡编号
     */
    void startGame(String tankType, int level);
    
    /**
     * 根据游戏模式获取游戏控制器
     * @return 游戏控制器
     */
    GameController getController();

    void setGameController(GameController controller);
} 