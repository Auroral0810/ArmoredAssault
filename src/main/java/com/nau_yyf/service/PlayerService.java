package com.nau_yyf.service;

import com.nau_yyf.controller.SingleGameController;
import com.nau_yyf.model.Bullet;
import com.nau_yyf.model.Tank;

/**
 * 玩家服务接口 - 负责处理玩家输入、状态和交互
 */
public interface PlayerService {
    
    /**
     * 处理玩家输入
     * 
     * @param singleGameController 游戏控制器
     * @param inputState 当前输入状态
     * @param bulletCount 当前子弹数量
     * @return 处理输入后的子弹数量
     */
    int handlePlayerInput(SingleGameController singleGameController, InputState inputState, int bulletCount);
    
    /**
     * 处理玩家坦克被摧毁
     * 
     * @param singleGameController 游戏控制器
     * @param currentTankType 当前坦克类型
     * @param playerLives 剩余生命数
     * @return 玩家是否还有生命
     */
    boolean handlePlayerDestroyed(SingleGameController singleGameController, String currentTankType, int playerLives);
    
    /**
     * 复活玩家坦克
     * 
     * @param singleGameController 游戏控制器
     * @param tankType 坦克类型
     * @param spawnX 重生位置X坐标
     * @param spawnY 重生位置Y坐标
     * @return 重生后的坦克对象
     */
    Tank respawnPlayer(SingleGameController singleGameController, String tankType, int spawnX, int spawnY);
    
    /**
     * 根据坦克类型计算子弹恢复速率
     * 
     * @param playerTank 玩家坦克
     * @return 子弹恢复延迟(毫秒)
     */
    int calculateBulletRefillDelay(Tank playerTank);
    
    /**
     * 更新子弹补充
     * 
     * @param playerTank 玩家坦克
     * @param currentBulletCount 当前子弹数量
     * @param lastRefillTime 上次补充时间
     * @return 更新后的子弹数量
     */
    int updateBulletRefill(Tank playerTank, int currentBulletCount, long lastRefillTime);
    
    /**
     * 创建并发射子弹
     * 
     * @param playerTank 玩家坦克
     * @return 创建的子弹对象
     */
    Bullet fireBullet(Tank playerTank);
    
    /**
     * 输入状态数据类
     */
    class InputState {
        private boolean up;
        private boolean down;
        private boolean left;
        private boolean right;
        private boolean shooting;
        
        // Getter和Setter方法
        public boolean isUp() { return up; }
        public void setUp(boolean up) { this.up = up; }
        
        public boolean isDown() { return down; }
        public void setDown(boolean down) { this.down = down; }
        
        public boolean isLeft() { return left; }
        public void setLeft(boolean left) { this.left = left; }
        
        public boolean isRight() { return right; }
        public void setRight(boolean right) { this.right = right; }
        
        public boolean isShooting() { return shooting; }
        public void setShooting(boolean shooting) { this.shooting = shooting; }
    }
}
