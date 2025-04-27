package com.nau_yyf.model;

import com.nau_yyf.model.Tank.PowerUpType;
import javafx.scene.image.Image;

public class PowerUp extends GameObject {
    private PowerUpType type;
    private long creationTime;
    private boolean isCollected = false;
    private boolean isBlinking = false;
    private static final long LIFESPAN = 5000; // 5秒生命周期
    private static final long BLINK_START = 3000; // 3秒开始闪烁
    
    public PowerUp(int x, int y, PowerUpType type) {
        super(x, y, 30, 30); // 增益效果大小为30x30
        this.type = type;
        this.creationTime = System.currentTimeMillis();
    }
    
    // 修改碰撞检测方法，接受Tank作为参数
    public boolean collidesWithTank(Tank tank) {
        // 放宽碰撞检测标准，使拾取更容易
        return getX() - 5 < tank.getX() + tank.getWidth() &&
               getX() + getWidth() + 5 > tank.getX() &&
               getY() - 5 < tank.getY() + tank.getHeight() &&
               getY() + getHeight() + 5 > tank.getY();
    }
    
    // 检查是否应该移除（超过生命周期或被收集）
    public boolean shouldRemove() {
        return isCollected || (System.currentTimeMillis() - creationTime) > LIFESPAN;
    }
    
    // 检查是否应该闪烁（超过3秒但尚未被收集）
    public boolean shouldBlink() {
        long elapsed = System.currentTimeMillis() - creationTime;
        return !isCollected && elapsed > BLINK_START && elapsed < LIFESPAN;
    }
    
    // 收集增益效果
    public void collect() {
        this.isCollected = true;
    }
    
    // 获取增益效果类型
    public PowerUpType getType() {
        return type;
    }
    
    // 获取剩余时间（毫秒）
    public long getRemainingTime() {
        return Math.max(0, LIFESPAN - (System.currentTimeMillis() - creationTime));
    }
    
    // 获取闪烁状态
    public boolean isBlinking() {
        return shouldBlink();
    }
    
    // 切换闪烁状态（用于动画效果）
    public void toggleBlinking() {
        this.isBlinking = !this.isBlinking;
    }
    
    // 获取当前闪烁的可见性状态
    public boolean isVisible() {
        return !shouldBlink() || isBlinking;
    }
} 