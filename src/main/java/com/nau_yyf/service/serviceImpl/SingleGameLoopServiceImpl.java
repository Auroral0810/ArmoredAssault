package com.nau_yyf.service.serviceImpl;

import com.nau_yyf.controller.SingleGameController;
import com.nau_yyf.model.Tank;
import com.nau_yyf.service.EffectService;
import com.nau_yyf.service.GameLoopService;
import com.nau_yyf.service.PlayerService;
import com.nau_yyf.view.GameView;
import com.nau_yyf.view.singleGame.SinglePlayerGameScreen;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;

/**
 * 单人游戏循环服务实现类
 * 负责单人游戏模式下的游戏循环管理
 */
public class SingleGameLoopServiceImpl implements GameLoopService {

    private GameView gameView;
    private long gameStartTime;
    private long lastUpdateTime;
    private EffectService effectService;
    private PlayerService playerService;

    public SingleGameLoopServiceImpl(GameView gameView, EffectService effectService, PlayerService playerService) {
        this.gameView = gameView;
        this.effectService = effectService;
        this.playerService = playerService;
    }

    /**
     * 创建并启动游戏循环
     */
    @Override
    public AnimationTimer createGameLoop(SingleGameController singleGameController,
                                         Runnable renderCallback,
                                         TimeUpdateCallback timeUpdateCallback) {
        // 重置时间相关变量
        gameStartTime = System.currentTimeMillis();
        lastUpdateTime = gameStartTime;
        
        AnimationTimer gameLoop = new AnimationTimer() {
            private long lastFrameTime = 0;
            private long focusCheckTime = 0;
            private final long FRAME_TIME = 16_666_667; // 约60FPS (纳秒)
            private double accumulator = 0;
            private final double TIME_STEP = 1.0 / 60.0; // 固定逻辑更新步长
            
            @Override
            public void handle(long now) {
                // 检查gameController是否为null
                if (singleGameController == null) {
                    return; // 如果控制器为null，不执行更新
                }
                
                if (lastFrameTime == 0) {
                    lastFrameTime = now;
                    return;
                }
                
                // 计算自上一帧以来经过的时间(秒)
                double deltaTime = (now - lastFrameTime) / 1_000_000_000.0;
                lastFrameTime = now;
                
                // 防止过大的时间步长
                if (deltaTime > 0.25)
                    deltaTime = 0.25;
                
                // 积累时间用于固定时间步长
                accumulator += deltaTime;
                
                // 执行固定时间步长的逻辑更新
                while (accumulator >= TIME_STEP) {
                    updateGame(singleGameController, TIME_STEP);
                    accumulator -= TIME_STEP;
                }
                
                // 渲染画面
                if (renderCallback != null) {
                    renderCallback.run();
                }
                
                // 更新游戏时间显示和焦点检查
                long currentTime = System.currentTimeMillis();
                long totalGameTime = gameView.getTotalGameTime() + (currentTime - lastUpdateTime);
                gameView.setTotalGameTime(totalGameTime);
                
                if (timeUpdateCallback != null) {
                    timeUpdateCallback.update(totalGameTime);
                }
                
                // 定期检查焦点
                Canvas gameCanvas = gameView.getGameCanvas();
                if (gameCanvas != null && now - focusCheckTime > 3_000_000_000L) {
                    focusCheckTime = now;
                    if (!gameCanvas.isFocused()) {
                        gameCanvas.requestFocus();
                    }
                }
                
                lastUpdateTime = currentTime;
                
                // 添加对玩家坦克状态的检查
                singleGameController.updatePlayerTank();
            }
        };
        
        // 启动游戏循环
        gameLoop.start();
        
        return gameLoop;
    }

    /**
     * 更新游戏状态
     */
    @Override
    public void updateGame(SingleGameController singleGameController, double deltaTime) {
        if (singleGameController == null) return;
        
        // 获取玩家坦克
        Tank playerTank = singleGameController.getPlayerTank();
        
        // 检查玩家坦克是否已死亡但未触发死亡处理
        if (playerTank != null && playerTank.isDead()) {
            // 确保调用玩家死亡处理 - 通过GameController的事件监听器处理
            return; // 玩家已死亡，不再进行其他更新
        }
        
        // 记录更新前的玩家血量
        int oldHealth = 0;
        if (playerTank != null) {
            oldHealth = playerTank.getHealth();
        }
        
        // 处理玩家输入
        gameView.handlePlayerInput(playerTank);
        
        // 更新敌方坦克
        singleGameController.updateEnemyTanks();
        
        // 更新子弹
        singleGameController.updateBullets(deltaTime);
        
        // 检查碰撞 - 如果返回true，表示玩家失去生命，通过GameController事件处理
        singleGameController.updateBulletsAndCheckCollisions();
        
        // 检查玩家血量是否改变，如果改变则更新显示
        if (playerTank != null && playerTank.getHealth() != oldHealth) {
            SinglePlayerGameScreen gameScreen = gameView.getSinglePlayerGameStarter().getGameScreen();
            gameScreen.updateHealthDisplay(singleGameController);
            
            // 使用SinglePlayerGameScreen更新生命显示
            gameScreen.updateLivesDisplay(gameView.getPlayerLives());
        }
        
        // 检查敌方坦克与玩家坦克碰撞
        if (singleGameController.checkEnemyPlayerCollisions()) {
            // 更新生命显示
            SinglePlayerGameScreen gameScreen = gameView.getSinglePlayerGameStarter().getGameScreen();
            gameScreen.updateHealthDisplay(singleGameController);
            
            // 使用SinglePlayerGameScreen更新生命显示
            gameScreen.updateLivesDisplay(gameView.getPlayerLives());
        }
        
        // 刷新子弹补给
        updateBulletRefill(singleGameController, deltaTime);
        
        // 更新敌人显示 - 每帧更新确保数量显示正确
        SinglePlayerGameScreen gameScreen = gameView.getSinglePlayerGameStarter().getGameScreen();
        gameScreen.updateEnemiesDisplay(singleGameController);
        
        // 更新增益效果
        singleGameController.updatePowerUps(deltaTime);
        
        // 使用效果服务更新玩家坦克的增益效果
        if (playerTank != null) {
            effectService.updatePowerUpEffects(playerTank, deltaTime);
        }
        
        // 使用效果服务更新闪烁效果
        effectService.updatePowerUpBlinking(singleGameController.getPowerUps());
        
        // 更新UI显示
        gameScreen.updatePowerUpUIDisplay(singleGameController, effectService);
    }

    /**
     * 根据坦克类型恢复子弹
     */
    private void updateBulletRefill(SingleGameController singleGameController, double deltaTime) {
        if (singleGameController == null) return;
        
        Tank playerTank = singleGameController.getPlayerTank();
        if (playerTank != null) {
            // 获取当前子弹数量和上次刷新时间
            int bulletCount = gameView.getBulletCount();
            long lastRefillTime = gameView.getLastBulletRefillTime();
            
            // 使用playerService更新子弹
            int newBulletCount = playerService.updateBulletRefill(playerTank, bulletCount, lastRefillTime);
            
            // 如果子弹数量变化，更新GameView
            if (newBulletCount != bulletCount) {
                gameView.setBulletCount(newBulletCount);
            }
        }
    }

    /**
     * 暂停游戏循环
     */
    @Override
    public void pauseGameLoop(AnimationTimer gameLoop) {
        if (gameLoop != null) {
            gameLoop.stop();
        }
    }

    /**
     * 恢复游戏循环
     */
    @Override
    public void resumeGameLoop(AnimationTimer gameLoop) {
        if (gameLoop != null) {
            Platform.runLater(() -> {
                gameLoop.start();
                
                // 确保游戏画布获取焦点以恢复键盘控制
                Canvas gameCanvas = gameView.getGameCanvas();
                if (gameCanvas != null) {
                    gameCanvas.requestFocus();
                }
                
            });
        }
    }

    /**
     * 停止并清理游戏循环
     */
    @Override
    public void stopGameLoop(AnimationTimer gameLoop) {
        if (gameLoop != null) {
            gameLoop.stop();
        }
    }
    
    /**
     * 重置游戏开始时间，保持总游戏时间不变
     */
    public void resetGameStartTime() {
        gameStartTime = System.currentTimeMillis() - gameView.getTotalGameTime();
        lastUpdateTime = gameStartTime;
    }
    
    /**
     * 获取最后更新时间
     */
    public long getLastUpdateTime() {
        return lastUpdateTime;
    }
    
    /**
     * 设置最后更新时间
     */
    public void setLastUpdateTime(long time) {
        this.lastUpdateTime = time;
    }
}
