package com.nau_yyf.service.serviceImpl;

import com.nau_yyf.controller.GameController;
import com.nau_yyf.controller.MultiGameController;
import com.nau_yyf.model.Tank;
import com.nau_yyf.service.EffectService;
import com.nau_yyf.service.GameLoopService;
import com.nau_yyf.service.PlayerService;
import com.nau_yyf.view.GameView;
import com.nau_yyf.view.GameScreen;
import com.nau_yyf.view.multiGame.MultiPlayerGameScreen;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;

/**
 * 多人游戏循环服务实现类
 * 负责多人游戏模式下的游戏循环管理
 */
public class MultiGameLoopServiceImpl implements GameLoopService {

    private GameView gameView;
    private long gameStartTime;
    private long lastUpdateTime;
    private EffectService effectService;
    private PlayerService playerService;

    public MultiGameLoopServiceImpl(GameView gameView, EffectService effectService, PlayerService playerService) {
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
        if (!(controller instanceof MultiGameController)) {
            throw new IllegalArgumentException("MultiGameLoopService需要MultiGameController类型的控制器");
        }
        
        final MultiGameController multiController = (MultiGameController) controller;
        
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
                // 检查controller是否为null
                if (multiController == null) {
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
                    updateGame(multiController, TIME_STEP);
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
                
                // 更新玩家坦克状态
                multiController.updatePlayerTanks();
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
    public void updateGame(GameController controller, double deltaTime) {
        if (!(controller instanceof MultiGameController)) {
            return;
        }
        
        MultiGameController multiController = (MultiGameController) controller;
        
        // TODO: 获取玩家1和玩家2坦克
        // Tank player1Tank = multiController.getPlayer1Tank();
        // Tank player2Tank = multiController.getPlayer2Tank();
        
        // TODO: 处理玩家1和玩家2输入
        
        // TODO: 更新敌方坦克
        // multiController.updateEnemyTanks();
        
        // TODO: 更新子弹
        // multiController.updateBullets(deltaTime);
        
        // TODO: 检查碰撞
        // multiController.updateBulletsAndCheckCollisions();
        
        // TODO: 更新生命显示
        // 当玩家生命值变化时更新UI
        
        // TODO: 更新子弹补给
        updateBulletRefill(multiController, gameView.getGameScreen());
        
        // 获取多人游戏屏幕的部分需修改为:
        GameScreen gameScreen = gameView.getGameScreen();
        if (gameScreen instanceof MultiPlayerGameScreen) {
            MultiPlayerGameScreen multiScreen = (MultiPlayerGameScreen) gameScreen;
            // TODO: 使用 multiScreen 进行相应操作
        }
        
        // 更新增益效果
        // multiController.updatePowerUps(deltaTime);
        
        // 使用效果服务更新玩家坦克的增益效果
        /*
        if (player1Tank != null) {
            effectService.updatePowerUpEffects(player1Tank, deltaTime);
        }
        if (player2Tank != null) {
            effectService.updatePowerUpEffects(player2Tank, deltaTime);
        }
        */
        
        // 使用效果服务更新闪烁效果
        // effectService.updatePowerUpBlinking(multiController.getPowerUps());
        
        // 更新UI显示
        // gameScreen.updatePowerUpUIDisplay(multiController, effectService);
    }

    /**
     * 更新子弹补充
     */
    private void updateBulletRefill(MultiGameController controller, GameScreen gameScreen) {
        long currentTime = System.currentTimeMillis();
        long lastRefillTime = gameScreen.getLastBulletRefillTime();
        
        // 每3秒补充一颗子弹，最多10颗
        if (currentTime - lastRefillTime >= 3000) {
            boolean updated = false;
            
            if (gameScreen instanceof MultiPlayerGameScreen) {
                MultiPlayerGameScreen multiScreen = (MultiPlayerGameScreen)gameScreen;
                
                // 更新玩家1子弹
                if (multiScreen.getPlayer1BulletCount() < 10) {
                    multiScreen.setPlayer1BulletCount(multiScreen.getPlayer1BulletCount() + 1);
                    updated = true;
                }
                
                // 更新玩家2子弹
                if (multiScreen.getPlayer2BulletCount() < 10) {
                    multiScreen.setPlayer2BulletCount(multiScreen.getPlayer2BulletCount() + 1);
                    updated = true;
                }
                
                // 只有当至少一个玩家的子弹更新了，才重置计时器
                if (updated) {
                    multiScreen.setLastBulletRefillTime(currentTime);
                }
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