package com.nau_yyf.service.serviceImpl;
/**
 * @author auroral
 * @date 2025/4/28 03:30
 */
import com.nau_yyf.controller.GameController;
import com.nau_yyf.controller.MultiGameController;
import com.nau_yyf.model.Bullet;
import com.nau_yyf.model.LevelMap;
import com.nau_yyf.model.Tank;
import com.nau_yyf.service.EffectService;
import com.nau_yyf.service.PlayerService;
import com.nau_yyf.view.GameView;
import com.nau_yyf.view.multiGame.MultiPlayerGameScreen;

/**
 * 多人游戏玩家服务实现类
 * 负责多人游戏模式下的玩家输入处理、状态管理和交互
 */
public class MultiPlayerServiceImpl implements PlayerService {
    
    private GameView gameView;
    private EffectService effectService;
    
    public MultiPlayerServiceImpl(GameView gameView, EffectService effectService) {
        this.gameView = gameView;
        this.effectService = effectService;
    }

    /**
     * 处理玩家输入 - 这里处理的是玩家1的输入
     * 多人游戏需要单独处理玩家2的输入
     */
    @Override
    public int handlePlayerInput(GameController controller, InputState inputState, int bulletCount) {
        if (controller == null) return bulletCount;
        
        // 只处理多人游戏控制器
        if (!(controller instanceof MultiGameController)) {
            return bulletCount;
        }
        
        MultiGameController multiController = (MultiGameController) controller;
        
        // 获取玩家1坦克
        Tank playerTank = multiController.getPlayer1Tank();
        
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
        // TODO: 实现多人游戏的移动逻辑
        //playerTank.move(multiController);
        
        // 处理射击
        if (inputState.isFire() && bulletCount > 0 && playerTank.canFire()) {
            Bullet bullet = fireBullet(playerTank);
            if (bullet != null) {
                bulletCount--;
                // TODO: 实现多人游戏的子弹添加逻辑
                //multiController.addBullet(bullet, 1); // 1表示玩家1的子弹
            }
        }
        
        return bulletCount;
    }
    
    /**
     * 处理玩家2的输入 (多人游戏特有方法)
     */
    public int handlePlayer2Input(MultiGameController controller, InputState inputState, int bulletCount) {
        if (controller == null) return bulletCount;
        
        // 获取玩家2坦克
        Tank player2Tank = controller.getPlayer2Tank();
        
        // 如果坦克不存在或已经死亡，不处理任何输入
        if (player2Tank == null || player2Tank.isDead()) {
            return bulletCount;
        }
        
        boolean anyKeyPressed = false;
        
        // 根据输入状态设置方向
        if (inputState.isUp()) {
            player2Tank.setDirection(Tank.Direction.UP);
            anyKeyPressed = true;
        } else if (inputState.isDown()) {
            player2Tank.setDirection(Tank.Direction.DOWN);
            anyKeyPressed = true;
        } else if (inputState.isLeft()) {
            player2Tank.setDirection(Tank.Direction.LEFT);
            anyKeyPressed = true;
        } else if (inputState.isRight()) {
            player2Tank.setDirection(Tank.Direction.RIGHT);
            anyKeyPressed = true;
        }
        
        // 设置是否加速
        player2Tank.setAccelerating(anyKeyPressed);
        
        // 执行移动逻辑（无论是否按键都要调用，以处理减速）
        // TODO: 实现多人游戏的移动逻辑
        //player2Tank.move(controller);
        
        // 处理射击
        if (inputState.isFire() && bulletCount > 0 && player2Tank.canFire()) {
            Bullet bullet = fireBullet(player2Tank);
            if (bullet != null) {
                bulletCount--;
                // TODO: 实现多人游戏的子弹添加逻辑
                //controller.addBullet(bullet, 2); // 2表示玩家2的子弹
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
        
        // 只处理多人游戏控制器
        if (!(controller instanceof MultiGameController)) {
            return false;
        }
        
        MultiGameController multiController = (MultiGameController) controller;
        
        // 默认处理玩家1 - 多人游戏中需要区分玩家1和玩家2
        Tank playerTank = multiController.getPlayer1Tank();
        if (playerTank == null || !playerTank.isDead()) {
            return false;
        }
        
        // 减少玩家生命值
        lives--;
        
        // 更新UI中的生命显示
        // TODO: 实现多人游戏生命显示逻辑
        
        // 判断是否游戏结束
        if (lives <= 0) {
            return false; // 没有生命了，无法重生
        } else {
            // 还有生命，立即重生玩家
            // 寻找有效的重生位置
            // TODO: 实现多人游戏的重生位置查找
            //LevelMap.MapPosition spawnPos = multiController.findValidSpawnPositionForPlayer1();
            LevelMap.MapPosition spawnPos = new LevelMap.MapPosition(); // 临时
            
            if (spawnPos != null) {
                // 使用当前选择的坦克类型重生
                Tank respawnedTank = respawnPlayer(multiController, tankType, spawnPos.getX(), spawnPos.getY());
                
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
     * 处理玩家2坦克被摧毁 (多人游戏特有方法)
     */
    public boolean handlePlayer2Destroyed(MultiGameController controller, String tankType, int lives) {
        if (controller == null) return false;
        
        Tank player2Tank = controller.getPlayer2Tank();
        if (player2Tank == null || !player2Tank.isDead()) {
            return false;
        }
        
        // 减少玩家2生命值
        lives--;
        
        // 更新UI中的生命显示
        // TODO: 实现玩家2生命显示更新
        
        // 判断是否游戏结束
        if (lives <= 0) {
            return false; // 没有生命了，无法重生
        } else {
            // 还有生命，立即重生玩家2
            // TODO: 实现玩家2的重生位置查找
            //LevelMap.MapPosition spawnPos = controller.findValidSpawnPositionForPlayer2();
            LevelMap.MapPosition spawnPos = new LevelMap.MapPosition(); // 临时
            
            if (spawnPos != null) {
                // 使用当前选择的坦克类型重生玩家2
                // TODO: 实现玩家2的重生
                //Tank respawnedTank = controller.respawnPlayer2Tank(tankType, spawnPos.getX(), spawnPos.getY());
                
                // 添加重生效果
                effectService.addRespawnEffect(
                    (javafx.scene.layout.StackPane) gameView.getGameCanvas().getParent(), 
                    spawnPos.getX(), 
                    spawnPos.getY()
                );
                
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
        
        // 只处理多人游戏控制器
        if (!(controller instanceof MultiGameController)) {
            return null;
        }
        
        MultiGameController multiController = (MultiGameController) controller;
        
        // 默认复活玩家1
        // TODO: 实现多人游戏的玩家1复活
        //multiController.respawnPlayer1Tank(tankType, spawnX, spawnY);
        
        // 返回重生后的玩家1坦克
        return multiController.getPlayer1Tank();
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
            // 注意：多人游戏中需要区分玩家1和玩家2的子弹补充时间
            // TODO: 更新正确的子弹补充时间
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

    @Override
    public void handlePlayerFiring(GameController controller) {

    }

    /**
     * 更新子弹显示
     */
    public void updateBulletDisplay(int player1BulletCount, int player2BulletCount) {
        // TODO: 实现多人游戏子弹显示更新
        /*
        if (gameView.getMultiPlayerGameStarter() != null) {
            MultiPlayerGameScreen gameScreen = gameView.getMultiPlayerGameStarter().getGameScreen();
            gameScreen.updateBulletDisplay(player1BulletCount, player2BulletCount);
        }
        */
    }
}