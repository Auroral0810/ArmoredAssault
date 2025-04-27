package com.nau_yyf.service;

import com.nau_yyf.model.PowerUp;
import com.nau_yyf.model.Tank;
import javafx.scene.layout.StackPane;

import java.util.Map;

/**
 * 特效服务接口 - 负责游戏特效和视觉增强
 */
public interface EffectService {
    
    /**
     * 创建并添加坦克重生特效
     * 
     * @param gameArea 游戏区域
     * @param x 坦克X坐标
     * @param y 坦克Y坐标
     */
    void addRespawnEffect(StackPane gameArea, int x, int y);
    
    /**
     * 更新增益效果状态
     * 
     * @param playerTank 玩家坦克
     * @param deltaTime 时间步长
     */
    void updatePowerUpEffects(Tank playerTank, double deltaTime);
    
    /**
     * 更新增益效果的闪烁状态
     * 
     * @param powerUps 增益效果列表
     */
    void updatePowerUpBlinking(Iterable<PowerUp> powerUps);
    
    /**
     * 获取激活的增益效果及其剩余时间
     * 
     * @param playerTank 玩家坦克
     * @return 增益效果及剩余时间的映射
     */
    Map<Tank.PowerUpType, Double> getActiveEffects(Tank playerTank);
}
