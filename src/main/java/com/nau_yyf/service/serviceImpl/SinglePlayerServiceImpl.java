package com.nau_yyf.service.serviceImpl;

import com.nau_yyf.controller.GameController;
import com.nau_yyf.controller.SingleGameController;
import com.nau_yyf.model.Bullet;
import com.nau_yyf.model.LevelMap;
import com.nau_yyf.model.Tank;
import com.nau_yyf.service.EffectService;
import com.nau_yyf.service.PlayerService;
import com.nau_yyf.view.GameView;
import com.nau_yyf.view.singleGame.SinglePlayerGameScreen;

/**
 * 单人游戏玩家服务实现类
 * 负责单人游戏模式下的玩家输入处理、状态管理和交互
 */
public class SinglePlayerServiceImpl implements PlayerService {

    private GameView gameView;
    private EffectService effectService;

    public SinglePlayerServiceImpl(GameView gameView, EffectService effectService) {
        this.gameView = gameView;
        this.effectService = effectService;
    }

    /**
     * 处理玩家输入
     */
    @Override
    public int handlePlayerInput(GameController controller, InputState inputState, int bulletCount) {
        if (controller == null) return bulletCount;
        
        // 只处理单人游戏控制器
        if (!(controller instanceof SingleGameController)) {
            return bulletCount;
        }
        
        SingleGameController singleController = (SingleGameController) controller;
        
        // 获取玩家坦克
        Tank playerTank = singleController.getPlayerTank();
        
        // 如果坦克不存在或已经死亡，不处理任何输入
        if (playerTank == null || playerTank.isDead()) {
            return bulletCount;
        }
        
        boolean anyKeyPressed = false;
        
        // 根据输入状态设置方向
        if (inputState.isUp()) {
            playerTank.setDirection(Tank.Direction.UP);
            anyKeyPressed = true;
        } else if (inputState.isDown()) {
            playerTank.setDirection(Tank.Direction.DOWN);
            anyKeyPressed = true;
        } else if (inputState.isLeft()) {
            playerTank.setDirection(Tank.Direction.LEFT);
            anyKeyPressed = true;
        } else if (inputState.isRight()) {
            playerTank.setDirection(Tank.Direction.RIGHT);
            anyKeyPressed = true;
        }
        
        // 设置是否加速
        playerTank.setAccelerating(anyKeyPressed);
        
        // 执行移动逻辑（无论是否按键都要调用，以处理减速）
        playerTank.move(singleController);
        
        // 水池伤害处理
        if (playerTank.isInWaterLastFrame()) {
            gameView.updateHealthDisplay();
        }
        
        // 处理射击
        if (inputState.isShooting() && bulletCount > 0 && playerTank.canFire()) {
            Bullet bullet = fireBullet(playerTank);
            if (bullet != null) {
                bulletCount--;
                singleController.addBullet(bullet);
            }
        }
        
        return bulletCount;
    }

    /**
     * 处理玩家坦克被摧毁
     */
    @Override
    public boolean handlePlayerDestroyed(GameController controller, String tankType, int lives) {
        if (controller == null) return false;
        
        // 只处理单人游戏控制器
        if (!(controller instanceof SingleGameController)) {
            return false;
        }
        
        SingleGameController singleController = (SingleGameController) controller;
        
        // 避免多次调用此方法，检查玩家坦克是否已经处理
        Tank playerTank = singleController.getPlayerTank();
        if (playerTank == null || !playerTank.isDead()) {
            return false;
        }
        
        // 减少玩家生命值
        lives--;
        
        // 更新UI中的生命显示
        gameView.setPlayerLives(lives);
        
        // 判断是否游戏结束
        if (lives <= 0) {
            return false; // 没有生命了，无法重生
        } else {
            // 还有生命，立即重生玩家
            // 寻找有效的重生位置
            LevelMap.MapPosition spawnPos = singleController.findValidSpawnPosition();
            if (spawnPos != null) {
                // 使用当前选择的坦克类型重生
                Tank respawnedTank = respawnPlayer(singleController, tankType, spawnPos.getX(), spawnPos.getY());
                
                // 添加重生效果
                if (respawnedTank != null) {
                    // 获取游戏区域
                    effectService.addRespawnEffect(
                        (javafx.scene.layout.StackPane) gameView.getGameCanvas().getParent(), 
                        spawnPos.getX(), 
                        spawnPos.getY()
                    );
                }
                return true; // 成功重生
            }
        }
        
        return false; // 未能重生
    }

    /**
     * 复活玩家坦克
     */
    @Override
    public Tank respawnPlayer(GameController controller, String tankType, int spawnX, int spawnY) {
        if (controller == null) return null;
        
        // 只处理单人游戏控制器
        if (!(controller instanceof SingleGameController)) {
            return null;
        }
        
        SingleGameController singleController = (SingleGameController) controller;
        
        // 使用游戏控制器的方法重生玩家坦克
        singleController.respawnPlayerTank(tankType, spawnX, spawnY);
        
        // 返回重生后的坦克
        return singleController.getPlayerTank();
    }

    /**
     * 根据坦克类型计算子弹恢复速率
     */
    @Override
    public int calculateBulletRefillDelay(Tank playerTank) {
        if (playerTank == null) return 1500; // 默认值
        
        // 根据坦克类型设置不同的子弹恢复速率（单位：毫秒）
        int refillDelay;
        Tank.TankType tankType = playerTank.getType();
        
        switch (tankType) {
            case LIGHT:
                refillDelay = 1000; // 轻型坦克恢复最快：1秒
                break;
            case HEAVY:
                refillDelay = 1800; // 重型坦克恢复最慢：1.8秒
                break;
            case STANDARD:
            default:
                refillDelay = 1500; // 标准坦克：1.5秒
                break;
        }
        
        // 如果有攻击力增强效果，恢复速度提高20%
        if (playerTank.isEffectActive(Tank.PowerUpType.ATTACK)) {
            refillDelay = (int) (refillDelay * 0.8);
        }
        
        return refillDelay;
    }

    /**
     * 更新子弹补充
     */
    @Override
    public int updateBulletRefill(Tank playerTank, int currentBulletCount, long lastRefillTime) {
        if (playerTank == null || currentBulletCount >= 10) {
            return currentBulletCount;
        }
        
        // 获取当前时间
        long currentTime = System.currentTimeMillis();
        
        // 计算子弹恢复延迟
        int refillDelay = calculateBulletRefillDelay(playerTank);
        
        // 检查是否到达恢复时间
        if (currentTime - lastRefillTime > refillDelay) {
            currentBulletCount++;
            gameView.setLastBulletRefillTime(currentTime);
        }
        
        return currentBulletCount;
    }

    /**
     * 创建并发射子弹
     */
    @Override
    public Bullet fireBullet(Tank playerTank) {
        if (playerTank == null) return null;
        
        return playerTank.fire();
    }

    /**
     * 更新子弹显示
     */
    public void updateBulletDisplay() {
        if (gameView.getSinglePlayerGameStarter() != null) {
            SinglePlayerGameScreen gameScreen = gameView.getSinglePlayerGameStarter().getGameScreen();
            gameScreen.updateBulletDisplay(gameView.getBulletCount());
        }
    }
}
