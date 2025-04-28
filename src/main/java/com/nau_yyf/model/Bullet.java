package com.nau_yyf.model;

import com.nau_yyf.model.Tank.Direction;
import javafx.scene.canvas.GraphicsContext;

public class Bullet extends GameObject {
    private Direction direction;
    private String bulletType;
    private double speed;
    private int damage;
    private boolean fromPlayer;
    private boolean isDestroyed = false;

    public Bullet(int x, int y, Direction direction, String bulletType,
                  double speed, int damage, boolean fromPlayer) {
        super(x, y, 10, 10); // 子弹尺寸为10x10
        this.direction = direction;
        this.bulletType = bulletType;
        this.speed = speed;
        this.damage = damage;
        this.fromPlayer = fromPlayer;
    }

    public void move() {
        setX(getX() + (int) Math.round(direction.getDx() * speed));
        setY(getY() + (int) Math.round(direction.getDy() * speed));
    }

    public boolean isOutOfBounds(int mapWidth, int mapHeight) {
        return getX() < 0 || getY() < 0 || getX() > mapWidth || getY() > mapHeight;
    }

    // 获取子弹图片路径
    public String getImagePath() {
        return "/images/bullets/" + bulletType + ".png";
    }

    // Getters和Setters
    public Direction getDirection() {
        return direction;
    }

    public String getBulletType() {
        return bulletType;
    }

    public double getSpeed() {
        return speed;
    }

    public int getDamage() {
        return damage;
    }

    public boolean isFromPlayer() {
        return fromPlayer;
    }

    public boolean isDestroyed() {
        return isDestroyed;
    }

    public void destroy() {
        this.isDestroyed = true;
    }

    public void moveByDistance(int distance) {
        setX(getX() + direction.getDx() * distance);
        setY(getY() + direction.getDy() * distance);
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public void setBulletType(String bulletType) {
        this.bulletType = bulletType;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public void setFromPlayer(boolean fromPlayer) {
        this.fromPlayer = fromPlayer;
    }

    public void setDestroyed(boolean isDestroyed) {
        this.isDestroyed = isDestroyed;
    }

    /**
     * 渲染子弹到画布
     * @param gc 图形上下文
     */
    public void render(GraphicsContext gc) {
        // 设置子弹颜色
        if (fromPlayer) {
            gc.setFill(javafx.scene.paint.Color.YELLOW);  // 玩家子弹为黄色
        } else {
            gc.setFill(javafx.scene.paint.Color.ORANGE);  // 敌人子弹为橙色
        }
        
        // 绘制子弹 - 使用getter方法替代直接访问
        gc.fillOval(getX(), getY(), getWidth(), getHeight());
        
        // 如果是特殊子弹，添加效果
        if ("powerful".equals(bulletType)) {
            gc.setStroke(javafx.scene.paint.Color.RED);
            gc.setLineWidth(1.5);
            gc.strokeOval(getX() - 2, getY() - 2, getWidth() + 4, getHeight() + 4);
        } else if ("rapid".equals(bulletType)) {
            gc.setStroke(javafx.scene.paint.Color.BLUE);
            gc.setLineWidth(1);
            gc.strokeOval(getX() - 1, getY() - 1, getWidth() + 2, getHeight() + 2);
        }
    }
}
