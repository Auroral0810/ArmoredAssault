package com.nau_yyf.model;

import com.nau_yyf.model.Tank.PowerUpType;
import javafx.scene.canvas.GraphicsContext;

public class PowerUp extends GameObject {
    private PowerUpType type;
    private long creationTime;
    private boolean isCollected = false;
    private boolean isBlinking = false;
    private static final long LIFESPAN = 8000; // 8秒生命周期
    private static final long BLINK_START = 5000; // 5秒开始闪烁

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

    // 切换闪烁状态
    public void toggleBlinking() {
        this.isBlinking = !this.isBlinking;
    }

    // 获取当前闪烁的可见性状态
    public boolean isVisible() {
        return !shouldBlink() || isBlinking;
    }

    // 创建时间的setter方法
    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    /**
     * 更新增益道具状态
     * @param deltaTime 经过的时间（秒）
     */
    public void update(double deltaTime) {
        // 检查是否应该闪烁
        if (shouldBlink()) {
            // 每200毫秒切换一次显示状态
            if (System.currentTimeMillis() % 400 < 200) {
                isBlinking = true;
            } else {
                isBlinking = false;
            }
        }
    }

    /**
     * 检查增益道具是否过期
     */
    public boolean isExpired() {
        return shouldRemove();
    }

    /**
     * 渲染增益道具到画布
     * @param gc 图形上下文
     */
    public void render(GraphicsContext gc) {
        // 如果在闪烁状态且当前不可见，则不渲染
        if (shouldBlink() && !isVisible()) {
            return;
        }
        
        // 根据增益类型选择不同颜色
        switch (type) {
            case ATTACK:
                gc.setFill(javafx.scene.paint.Color.RED);
                break;
            case BOMB:
                gc.setFill(javafx.scene.paint.Color.BLACK);
                break;
            case HEALTH:
                gc.setFill(javafx.scene.paint.Color.GREEN);
                break;
            case INVINCIBILITY:
                gc.setFill(javafx.scene.paint.Color.GOLD);
                break;
            case SHIELD:
                gc.setFill(javafx.scene.paint.Color.CYAN);
                break;
            case SPEED:
                gc.setFill(javafx.scene.paint.Color.BLUE);
                break;
            default:
                gc.setFill(javafx.scene.paint.Color.GRAY);
        }
        
        // 绘制增益道具
        gc.fillRect(getX(), getY(), getWidth(), getHeight());
        
        // 绘制边框
        gc.setStroke(javafx.scene.paint.Color.WHITE);
        gc.setLineWidth(1);
        gc.strokeRect(getX(), getY(), getWidth(), getHeight());
        
        // 根据类型绘制简单图标
        gc.setFill(javafx.scene.paint.Color.WHITE);
        
        double centerX = getX() + getWidth() / 2;
        double centerY = getY() + getHeight() / 2;
        
        switch (type) {
            case ATTACK:
                // 绘制+号表示攻击增强
                gc.strokeLine(centerX - 5, centerY, centerX + 5, centerY);
                gc.strokeLine(centerX, centerY - 5, centerX, centerY + 5);
                break;
            case BOMB:
                // 绘制圆形表示炸弹
                gc.fillOval(centerX - 5, centerY - 5, 10, 10);
                break;
            case HEALTH:
                // 绘制+号表示生命
                gc.strokeLine(centerX - 5, centerY, centerX + 5, centerY);
                gc.strokeLine(centerX, centerY - 5, centerX, centerY + 5);
                break;
            case INVINCIBILITY:
                // 绘制*号表示无敌
                gc.strokeLine(centerX - 5, centerY - 5, centerX + 5, centerY + 5);
                gc.strokeLine(centerX + 5, centerY - 5, centerX - 5, centerY + 5);
                gc.strokeLine(centerX - 5, centerY, centerX + 5, centerY);
                gc.strokeLine(centerX, centerY - 5, centerX, centerY + 5);
                break;
            case SHIELD:
                // 绘制圆形表示护盾
                gc.strokeOval(centerX - 5, centerY - 5, 10, 10);
                break;
            case SPEED:
                // 绘制>号表示速度
                gc.strokeLine(centerX - 3, centerY - 5, centerX + 3, centerY);
                gc.strokeLine(centerX + 3, centerY, centerX - 3, centerY + 5);
                break;
        }
    }
} 