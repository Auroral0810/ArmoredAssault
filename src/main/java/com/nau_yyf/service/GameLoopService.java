package com.nau_yyf.service;

import com.nau_yyf.controller.GameController;
import javafx.animation.AnimationTimer;

/**
 * 游戏循环服务接口 - 负责管理游戏主循环和时间步长
 */
public interface GameLoopService {
    
    /**
     * 创建并启动游戏循环
     * 
     * @param gameController 游戏控制器
     * @param renderCallback 渲染回调接口
     * @param timeUpdateCallback 时间更新回调接口
     * @return 创建的游戏循环对象
     */
    AnimationTimer createGameLoop(GameController gameController, 
                                  Runnable renderCallback, 
                                  TimeUpdateCallback timeUpdateCallback);
    
    /**
     * 更新游戏状态
     * 
     * @param gameController 游戏控制器
     * @param deltaTime 时间步长
     */
    void updateGame(GameController gameController, double deltaTime);
    
    /**
     * 暂停游戏循环
     * 
     * @param gameLoop 游戏循环对象
     */
    void pauseGameLoop(AnimationTimer gameLoop);
    
    /**
     * 恢复游戏循环
     * 
     * @param gameLoop 游戏循环对象
     */
    void resumeGameLoop(AnimationTimer gameLoop);
    
    /**
     * 停止并清理游戏循环
     * 
     * @param gameLoop 游戏循环对象
     */
    void stopGameLoop(AnimationTimer gameLoop);
    
    /**
     * 时间更新回调接口
     */
    interface TimeUpdateCallback {
        void update(long totalGameTime);
    }
}
