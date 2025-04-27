package com.nau_yyf.service;

import com.nau_yyf.controller.SingleGameController;
import com.nau_yyf.model.Tank;
import javafx.scene.canvas.GraphicsContext;

/**
 * 渲染服务接口 - 负责游戏图形渲染
 */
public interface RenderService {
    
    /**
     * 渲染游戏画面
     * 
     * @param singleGameController 游戏控制器
     * @param gc 图形上下文
     * @param canvasWidth 画布宽度
     * @param canvasHeight 画布高度
     */
    void renderGame(SingleGameController singleGameController, GraphicsContext gc, double canvasWidth, double canvasHeight);
    
    /**
     * 渲染玩家坦克
     * 
     * @param playerTank 玩家坦克
     * @param singleGameController 游戏控制器
     * @param gc 图形上下文
     */
    void renderPlayerTank(Tank playerTank, SingleGameController singleGameController, GraphicsContext gc);
    
    /**
     * 渲染坦克特效
     * 
     * @param playerTank 玩家坦克
     * @param gc 图形上下文
     */
    void renderTankEffects(Tank playerTank, GraphicsContext gc);
}
