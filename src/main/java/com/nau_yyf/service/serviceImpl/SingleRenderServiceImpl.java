package com.nau_yyf.service.serviceImpl;

import com.nau_yyf.controller.SingleGameController;
import com.nau_yyf.model.LevelMap;
import com.nau_yyf.model.Tank;
import com.nau_yyf.service.RenderService;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/**
 * 单人游戏渲染服务实现类
 * 负责单人游戏模式下的图形渲染
 */
public class SingleRenderServiceImpl implements RenderService {

    /**
     * 渲染整个游戏画面
     */
    @Override
    public void renderGame(SingleGameController singleGameController, GraphicsContext gc, double canvasWidth, double canvasHeight) {
        // 添加空检查以防止崩溃
        if (singleGameController == null) {
            return; // 如果gameController为null，直接返回不渲染
        }
        
        // 清屏
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, canvasWidth, canvasHeight);
        
        // 渲染地图元素
        LevelMap map = singleGameController.getMap();
        for (LevelMap.MapElement element : map.getElements()) {
            // 根据元素类型获取对应图片并渲染
            Image elementImage = singleGameController.getElementImage(element.getType());
            gc.drawImage(elementImage, element.getX(), element.getY(), 
                        element.getWidth(), element.getHeight());
        }
        
        // 渲染其他游戏对象
        singleGameController.renderMap(gc);
        
        // 渲染玩家坦克
        Tank playerTank = singleGameController.getPlayerTank();
        if (playerTank != null && !playerTank.isDead()) {
            renderPlayerTank(playerTank, singleGameController, gc);
        }
    }

    /**
     * 渲染玩家坦克
     */
    @Override
    public void renderPlayerTank(Tank playerTank, SingleGameController singleGameController, GraphicsContext gc) {
        if (playerTank == null || singleGameController == null) return;
        
        boolean shouldRender = true;
        
        // 检查是否处于复活无敌状态，并且需要闪烁
        if (playerTank.isRespawnInvincible()) {
            shouldRender = playerTank.isVisibleDuringRespawnInvincible();
        }
        
        if (shouldRender) {
            String imageKey = "player_" + playerTank.getType().name().toLowerCase();
            Image[] tankImgs = singleGameController.getTankImages().get(imageKey);
            
            if (tankImgs != null && playerTank.getDirection().ordinal() < tankImgs.length) {
                gc.drawImage(tankImgs[playerTank.getDirection().ordinal()], 
                           playerTank.getX(), playerTank.getY(), 40, 40);
                
                // 渲染坦克特效
                renderTankEffects(playerTank, gc);
            }
        }
    }

    /**
     * 渲染坦克特效
     */
    @Override
    public void renderTankEffects(Tank playerTank, GraphicsContext gc) {
        if (playerTank == null) return;
        
        // 如果处于无敌状态，添加视觉效果
        if (playerTank.isRespawnInvincible() || playerTank.isInvincible()) {
            gc.setGlobalAlpha(0.3);
            gc.setFill(Color.YELLOW);
            gc.fillOval(playerTank.getX() - 5, playerTank.getY() - 5, 50, 50);
            gc.setGlobalAlpha(1.0);
        } else if (playerTank.isShielded()) {
            // 如果有护盾，绘制蓝色保护罩
            gc.setGlobalAlpha(0.3);
            gc.setFill(Color.BLUE);
            gc.fillOval(playerTank.getX() - 5, playerTank.getY() - 5, 50, 50);
            gc.setGlobalAlpha(1.0);
        }
    }
}
