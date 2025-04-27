package com.nau_yyf.service.serviceImpl;

import com.nau_yyf.model.PowerUp;
import com.nau_yyf.model.Tank;
import com.nau_yyf.service.EffectService;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.scene.Group;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.Map;

/**
 * 单人游戏特效服务实现类
 * 负责单人游戏模式下的特效和视觉增强
 */
public class SingleEffectServiceImpl implements EffectService {
    
    // 存储效果盒子映射，用于UI显示
    private Map<String, HBox> effectBoxMap = new HashMap<>();
    
    /**
     * 创建并添加坦克重生特效
     */
    @Override
    public void addRespawnEffect(StackPane gameArea, int x, int y) {
        try {
            // 创建特效容器
            Group effectGroup = new Group();
            
            // 创建一个圆形特效
            Circle circle = new Circle(20, Color.TRANSPARENT);
            circle.setStroke(Color.LIGHTBLUE);
            circle.setStrokeWidth(3);
            
            // 设置特效位置
            effectGroup.setTranslateX(x + 20); // 坦克中心点X
            effectGroup.setTranslateY(y + 20); // 坦克中心点Y
            effectGroup.getChildren().add(circle);
            
            // 添加到游戏画布上层
            if (gameArea != null) {
                gameArea.getChildren().add(effectGroup);
                
                // 创建动画：从小到大再消失
                ScaleTransition scale = new ScaleTransition(Duration.millis(500), circle);
                scale.setFromX(0.2);
                scale.setFromY(0.2);
                scale.setToX(2);
                scale.setToY(2);
                
                FadeTransition fade = new FadeTransition(Duration.millis(500), effectGroup);
                fade.setFromValue(1.0);
                fade.setToValue(0.0);
                fade.setDelay(Duration.millis(200));
                
                // 播放动画结束后移除特效
                ParallelTransition transition = new ParallelTransition(scale, fade);
                transition.setOnFinished(e -> gameArea.getChildren().remove(effectGroup));
                transition.play();
            }
        } catch (Exception e) {
            System.err.println("无法创建重生特效: " + e.getMessage());
        }
    }
    
    /**
     * 更新增益效果状态
     */
    @Override
    public void updatePowerUpEffects(Tank playerTank, double deltaTime) {
        if (playerTank == null) return;
        
        // 更新坦克的增益效果持续时间
        playerTank.updateEffects(deltaTime);
    }
    
    /**
     * 更新增益效果的闪烁状态
     */
    @Override
    public void updatePowerUpBlinking(Iterable<PowerUp> powerUps) {
        if (powerUps == null) return;
        
        // 更新闪烁效果
        for (PowerUp powerUp : powerUps) {
            if (powerUp.shouldBlink()) {
                if (Math.random() < 0.1) { // 每帧10%概率切换闪烁状态
                    powerUp.toggleBlinking();
                }
            }
        }
    }
    
    /**
     * 获取激活的增益效果及其剩余时间
     */
    @Override
    public Map<Tank.PowerUpType, Double> getActiveEffects(Tank playerTank) {
        if (playerTank == null) {
            return new HashMap<>();
        }
        return playerTank.getActiveEffects();
    }
    
    /**
     * 注册效果盒子
     * 
     * @param typeName 效果类型名称
     * @param effectBox 效果盒子组件
     */
    public void registerEffectBox(String typeName, HBox effectBox) {
        this.effectBoxMap.put(typeName, effectBox);
    }
    
    /**
     * 获取效果盒子映射
     * 
     * @return 效果盒子映射
     */
    public Map<String, HBox> getEffectBoxMap() {
        return effectBoxMap;
    }
    
    /**
     * 更新UI显示状态
     * 
     * @param playerTank 玩家坦克
     * @param powerUpProgressBars 进度条映射
     */
    public void updateUIDisplay(Tank playerTank, Map<String, javafx.scene.control.ProgressBar> powerUpProgressBars) {
        if (playerTank == null) return;
        
        Map<Tank.PowerUpType, Double> activeEffects = playerTank.getActiveEffects();
        
        // 更新每种效果的显示状态
        for (Tank.PowerUpType type : Tank.PowerUpType.values()) {
            if (type == Tank.PowerUpType.HEALTH)
                continue; // 跳过生命恢复
            
            String typeName = type.getName();
            HBox effectBox = effectBoxMap.get(typeName);
            
            if (effectBox != null) {
                // 检查效果是否激活
                boolean isActive = activeEffects.containsKey(type);
                effectBox.setVisible(isActive);
                
                // 如果效果激活，更新进度条
                if (isActive) {
                    double remainingTime = activeEffects.get(type);
                    double maxTime = type.getDuration();
                    double progress = remainingTime / maxTime;
                    
                    // 更新进度条
                    javafx.scene.control.ProgressBar progressBar = powerUpProgressBars.get(typeName);
                    if (progressBar != null) {
                        progressBar.setProgress(progress);
                    }
                    
                    // 当剩余时间少于3秒时，添加闪烁效果
                    if (remainingTime < 3.0) {
                        // 闪烁效果
                        if (Math.random() > 0.5) {
                            effectBox.setStyle("-fx-border-color: #FF4400; -fx-border-radius: 4;");
                            if (progressBar != null) {
                                progressBar.setStyle("-fx-accent: #FF4400;");
                            }
                        } else {
                            effectBox.setStyle("-fx-border-color: #004466; -fx-border-radius: 4;");
                            if (progressBar != null) {
                                progressBar.setStyle("-fx-accent: #00AAFF;");
                            }
                        }
                    } else {
                        effectBox.setStyle("-fx-border-color: #004466; -fx-border-radius: 4;");
                        if (progressBar != null) {
                            progressBar.setStyle("-fx-accent: #00AAFF;");
                        }
                    }
                }
            }
        }
    }
}
