package com.nau_yyf.controller;

import com.nau_yyf.view.GameView;
import javafx.scene.canvas.GraphicsContext;

/**
 * 游戏控制器通用接口
 * 所有模式的游戏控制器都实现此接口
 */
public interface GameController {
    /**
     * 设置游戏视图引用
     * @param gameView 游戏视图
     */
    void setGameView(GameView gameView);
    
    /**
     * 加载关卡
     * @param level 关卡编号
     */
    void loadLevel(int level);
    
    /**
     * 获取当前关卡
     * @return 当前关卡
     */
    int getCurrentLevel();
    
    /**
     * 渲染游戏场景
     * @param gc 图形上下文
     */
    void renderMap(GraphicsContext gc);
    
    /**
     * 判断关卡是否完成
     * @return 是否完成
     */
    boolean isLevelCompleted();
    
    /**
     * 保存游戏状态
     * @param saveName 存档名称
     * @return 是否保存成功
     */
    boolean saveGame(String saveName);
    
    /**
     * 获取游戏视图
     * @return 游戏视图
     */
    GameView getGameView();
} 