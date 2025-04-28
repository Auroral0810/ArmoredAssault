package com.nau_yyf.service.serviceImpl;

import com.nau_yyf.controller.GameController;
import com.nau_yyf.controller.SingleGameController;
import com.nau_yyf.view.GameScreen;
import com.nau_yyf.model.Tank;
import com.nau_yyf.view.singleGame.SinglePlayerGameScreen;
import com.nau_yyf.service.EffectService;
import com.nau_yyf.service.GameLoopService;
import com.nau_yyf.service.PlayerService;
import com.nau_yyf.view.GameView;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.text.Text;

public class SingleGameLoopServiceImpl implements GameLoopService {
    private final GameView gameView;
    private final EffectService effectService;
    private final PlayerService playerService;
    private long gameStartTime;
    private long lastUpdateTime;
    private boolean wasGamePaused = false;
    private AnimationTimer gameLoop;

    public SingleGameLoopServiceImpl(GameView gameView, EffectService effectService, PlayerService playerService) {
        this.gameView = gameView;
        this.effectService = effectService;
        this.playerService = playerService;
    }

    /**
     * 创建并启动游戏循环
     */
    @Override
    public AnimationTimer createGameLoop(GameController controller,
                                         Runnable renderCallback,
                                         TimeUpdateCallback timeUpdateCallback) {
        if (!(controller instanceof SingleGameController)) {
            throw new IllegalArgumentException("SingleGameLoopService需要SingleGameController类型的控制器");
        }
                                         
        final SingleGameController singleController = (SingleGameController) controller;
        
        // 重置时间相关变量
        gameStartTime = System.currentTimeMillis();
        lastUpdateTime = gameStartTime;
        
        // 确保任何旧的循环都被停止
        if (gameLoop != null) {
            gameLoop.stop();
            gameLoop = null;
        }
        
        AnimationTimer newGameLoop = new AnimationTimer() {
            private long lastFrameTime = 0;
            private long focusCheckTime = 0;
            private final long FRAME_TIME = 16_666_667; // 约60FPS (纳秒)
            private double accumulator = 0;
            private final double TIME_STEP = 1.0 / 60.0; // 固定逻辑更新步长
            
            @Override
            public void handle(long now) {
                // 立即检查是否暂停，如果暂停则直接返回
                GameScreen gameScreen = gameView.getGameScreen();
                if (controller == null || gameScreen == null || gameScreen.isGamePaused()) {
                    return;
                }
                
                // 获取当前时间
                long currentTime = System.currentTimeMillis();
                
                // 获取上次更新时间，确保不为0
                long lastTime = gameScreen.getLastUpdateTime();
                if (lastTime == 0) {
                    lastTime = currentTime;
                    gameScreen.setLastUpdateTime(currentTime);
                }
                
                // 计算时间差，设置合理的最大值防止异常
                long deltaTime = currentTime - lastTime;
                if (deltaTime > 100) { // 如果时间差大于100ms，可能是暂停后恢复或其他异常情况
                    deltaTime = 16; // 使用一个标准帧时间(约60FPS)
                    
                }
                
                // 更新游戏总时间
                long totalGameTime = gameScreen.getTotalGameTime() + deltaTime;
                gameScreen.setTotalGameTime(totalGameTime);
                
                // 使用固定时间步长更新逻辑，避免速度不一致
                if (lastFrameTime == 0) {
                    lastFrameTime = now;
                    return;
                }
                
                // 计算自上一帧以来经过的时间(秒)
                double frameDeltaTime = (now - lastFrameTime) / 1_000_000_000.0;
                lastFrameTime = now;
                
                // 防止过大的时间步长导致游戏速度异常
                if (frameDeltaTime > 0.1) {
                    frameDeltaTime = 0.1;
                }
                
                // 积累时间用于固定时间步长
                accumulator += frameDeltaTime;
                
                // 执行固定时间步长的逻辑更新，确保游戏速度一致
                int stepCount = 0;
                while (accumulator >= TIME_STEP && stepCount < 5) { // 限制最大步数避免死循环
                    updateGame(singleController, TIME_STEP);
                    accumulator -= TIME_STEP;
                    stepCount++;
                }
                
                // 更新最后更新时间
                gameScreen.setLastUpdateTime(currentTime);
                
                // 渲染画面
                if (renderCallback != null) {
                    renderCallback.run();
                }
                
                // 如果有时间更新回调，则调用
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
                
                // 添加对玩家坦克状态的检查
                singleController.updatePlayerTank();
            }
        };
        
        // 保存游戏循环引用以便后续管理
        gameLoop = newGameLoop;
        
        // 启动游戏循环
        newGameLoop.start();
        
        return newGameLoop;
    }

    /**
     * 更新游戏状态
     */
    @Override
    public void updateGame(GameController controller, double deltaTime) {
        if (!(controller instanceof SingleGameController)) {
            return;
        }
        
        SingleGameController singleController = (SingleGameController) controller;
        
        // 获取玩家坦克
        Tank playerTank = singleController.getPlayerTank();
        
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
        singleController.updateEnemyTanks();
        
        // 更新子弹
        singleController.updateBullets(deltaTime);
        
        // 检查碰撞 - 如果返回true，表示玩家失去生命，通过GameController事件处理
        singleController.updateBulletsAndCheckCollisions();
        
        // 检查玩家血量是否改变，如果改变则更新显示
        if (playerTank != null && playerTank.getHealth() != oldHealth) {
            GameScreen gameScreen = gameView.getGameScreen();
            if (gameScreen instanceof SinglePlayerGameScreen) {
                SinglePlayerGameScreen singleScreen = (SinglePlayerGameScreen) gameScreen;
                singleScreen.updateHealthDisplay(singleController);
                singleScreen.updateLivesDisplay(gameView.getPlayerLives());
            }
        }
        
        // 检查敌方坦克与玩家坦克碰撞
        if (singleController.checkEnemyPlayerCollisions()) {
            // 更新生命显示
            GameScreen gameScreen = gameView.getGameScreen();
            if (gameScreen instanceof SinglePlayerGameScreen) {
                SinglePlayerGameScreen singleScreen = (SinglePlayerGameScreen) gameScreen;
                singleScreen.updateHealthDisplay(singleController);
                singleScreen.updateLivesDisplay(gameView.getPlayerLives());
            }
        }
        
        // 刷新子弹补给
        updateBulletRefill(singleController, gameView.getGameScreen());
        
        // 更新敌人显示 - 每帧更新确保数量显示正确
        GameScreen gameScreen = gameView.getGameScreen();
        if (gameScreen instanceof SinglePlayerGameScreen) {
            SinglePlayerGameScreen singleScreen = (SinglePlayerGameScreen) gameScreen;
            singleScreen.updateEnemiesDisplay(singleController);
            singleScreen.updatePowerUpUIDisplay(singleController, effectService);
        }
        
        // 更新增益效果
        singleController.updatePowerUps(deltaTime);
        
        // 使用效果服务更新玩家坦克的增益效果
        if (playerTank != null) {
            effectService.updatePowerUpEffects(playerTank, deltaTime);
        }
        
        // 使用效果服务更新闪烁效果
        effectService.updatePowerUpBlinking(singleController.getPowerUps());
    }

    /**
     * 更新子弹补充逻辑
     */
    private void updateBulletRefill(GameController controller, GameScreen gameScreen) {
        if (controller == null || gameScreen == null) {
            return;
        }
        
        // 获取玩家坦克 - 使用安全类型转换
        Tank playerTank = null;
        if (controller instanceof SingleGameController) {
            playerTank = ((SingleGameController) controller).getPlayerTank();
        } else {
            return; // 不是单人游戏控制器，直接返回
        }
        
        if (playerTank == null) return;
        
        // 获取当前子弹数量
        int currentBulletCount = gameScreen.getBulletCount();
        
        // 如果子弹已满，不需要补充
        if (currentBulletCount >= 10) {
            return;
        }
        
        long currentTime = System.currentTimeMillis();
        long lastRefillTime = gameScreen.getLastBulletRefillTime();
        
        // 确保lastRefillTime已初始化
        if (lastRefillTime == 0) {
            gameScreen.setLastBulletRefillTime(currentTime);
            return;
        }
        
        // 计算补充延迟
        int refillDelay = 3000; // 默认3秒补充一颗
        
        // 检查是否到达补充时间
        if (currentTime - lastRefillTime > refillDelay) {
            // 补充一颗子弹
            int newBulletCount = currentBulletCount + 1;
            
            // 更新子弹数量
            gameScreen.setBulletCount(newBulletCount);
            
            // 更新补充时间
            gameScreen.setLastBulletRefillTime(currentTime);
            
            
            // 确保UI更新 - 使用安全类型转换
            Platform.runLater(() -> {
                if (gameScreen instanceof SinglePlayerGameScreen) {
                    ((SinglePlayerGameScreen) gameScreen).updateBulletDisplay(newBulletCount);
                }
            });
        }
    }

    /**
     * 暂停游戏循环
     */
    @Override
    public void pauseGameLoop(AnimationTimer gameLoop) {
        if (gameLoop != null) {
            gameLoop.stop();
            wasGamePaused = true; // 标记游戏已暂停
        }
    }

    /**
     * 恢复游戏循环
     */
    @Override
    public void resumeGameLoop(AnimationTimer gameLoop) {
        if (gameLoop != null) {
            Platform.runLater(() -> {
                // 恢复时重置lastUpdateTime为当前时间
                lastUpdateTime = System.currentTimeMillis();
                gameLoop.start();
                
                // 确保画布获取焦点以恢复键盘控制
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
