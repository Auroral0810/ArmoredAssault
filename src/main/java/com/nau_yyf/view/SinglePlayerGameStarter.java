package com.nau_yyf.view;

import com.nau_yyf.controller.GameController;
import javafx.application.Platform;

/**
 * 单人游戏启动器
 * 负责初始化和启动单人游戏
 */
public class SinglePlayerGameStarter {
    
    private GameView gameView;
    private GameController gameController;
    private SinglePlayerGameScreen gameScreen;
    
    /**
     * 构造函数
     * @param gameView 游戏主视图引用
     */
    public SinglePlayerGameStarter(GameView gameView) {
        this.gameView = gameView;
        this.gameScreen = new SinglePlayerGameScreen(gameView);
    }
    
    /**
     * 以指定的坦克类型和关卡开始游戏
     * @param selectedTankType 选择的坦克类型
     * @param level 关卡编号
     */
    public void startGame(String selectedTankType, int level) {
        // 创建游戏控制器并加载关卡
        gameController = new GameController();
        gameController.loadLevel(level);
        gameController.setPlayerTankType(selectedTankType);
        
        // 设置对GameView的引用
        gameController.setGameView(gameView);
        
        // 确保GameView也拥有对此控制器的引用
        gameView.setGameController(gameController);
        
        // 设置事件监听器
        gameController.setGameEventListener(new GameController.GameEventListener() {
            @Override
            public void onPlayerDestroyed() {
                // 在JavaFX应用线程中处理玩家坦克摧毁事件
                Platform.runLater(() -> {
                    gameView.handlePlayerDestroyed();
                });
            }
        });
        
        // 初始化游戏状态
        gameView.setGamePaused(false);
        gameView.setIsPauseMenuOpen(false);
        
        // 显示游戏屏幕 - 先显示界面再设置生命值和子弹数
        gameScreen.show(gameController);
        
        // 现在gameDataPanel已初始化，可以安全设置这些值
        gameView.setPlayerLives(3);
        gameView.setBulletCount(10);
        
        // 立即更新所有显示元素
        gameView.updateHealthDisplay();
        gameView.updateLivesDisplay();
        gameView.updateEnemiesDisplay();
        gameView.updateBulletDisplay();
        
        // 记录游戏开始时间
        gameView.resetGameStartTime();
        gameView.setLastBulletRefillTime(System.currentTimeMillis());
        
        // 确保画布获取焦点
        if (gameScreen.getGameCanvas() != null) {
            gameScreen.getGameCanvas().requestFocus();
        }
        
        // 启动游戏循环
        gameView.startGameLoop();
        
        System.out.println("游戏已启动：坦克类型=" + selectedTankType + "，关卡=" + level);
    }
    
    /**
     * 获取游戏控制器
     * @return the gameController
     */
    public GameController getGameController() {
        return gameController;
    }
    
    /**
     * 设置游戏控制器
     * @param gameController the gameController to set
     */
    public void setGameController(GameController gameController) {
        this.gameController = gameController;
    }
    
    /**
     * 获取游戏屏幕组件
     * @return 游戏屏幕实例
     */
    public SinglePlayerGameScreen getGameScreen() {
        return gameScreen;
    }
} 