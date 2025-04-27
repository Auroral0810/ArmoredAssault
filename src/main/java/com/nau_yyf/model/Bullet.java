package com.nau_yyf.model;

import com.nau_yyf.model.Tank.Direction;

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
        setX(getX() + (int)Math.round(direction.getDx() * speed));
        setY(getY() + (int)Math.round(direction.getDy() * speed));
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
}
