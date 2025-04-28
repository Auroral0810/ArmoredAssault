package com.nau_yyf.controller;

import com.nau_yyf.view.GameView;
import com.nau_yyf.view.singleGame.SinglePlayerGameScreen;
import javafx.application.Platform;

/**
 * 单人游戏启动器
 * 负责初始化和启动单人游戏
 */
public class SinglePlayerGameStarter implements GameStarterController {

    private GameView gameView;
    private SingleGameController singleGameController;
    private SinglePlayerGameScreen gameScreen;

    /**
     * 构造函数
     * 
     * @param gameView 游戏主视图引用
     */
    public SinglePlayerGameStarter(GameView gameView) {
        this.gameView = gameView;
        this.gameScreen = new SinglePlayerGameScreen(gameView);
    }

    /**
     * 以指定的坦克类型和关卡开始游戏
     * 
     * @param selectedTankType 选择的坦克类型
     * @param level            关卡编号
     */
    public void startGame(String selectedTankType, int level) {
        // 设置游戏模式为单人游戏
        gameView.setGameMode(GameView.GAME_MODE_SINGLE);
        
        // 创建游戏控制器并加载关卡
        singleGameController = new SingleGameController();
        singleGameController.loadLevel(level);
        singleGameController.setPlayerTankType(selectedTankType);

        // 设置对GameView的引用
        singleGameController.setGameView(gameView);

        // 确保GameView也拥有对此控制器的引用
        gameView.setGameController(singleGameController);

        // 设置事件监听器
        singleGameController.setGameEventListener(new SingleGameController.GameEventListener() {
            @Override
            public void onPlayerDestroyed() {
                // 在JavaFX应用线程中处理玩家坦克摧毁事件
                Platform.runLater(() -> {
                    // 修改为调用SinglePlayerGameScreen的方法
                    gameScreen.handlePlayerDestroyed(
                            singleGameController,
                            selectedTankType,
                            gameView.getPlayerLives());
                });
            }
        });

        // 初始化游戏状态 - 直接在SinglePlayerGameScreen设置
        gameScreen.setGamePaused(false);
        gameScreen.setIsPauseMenuOpen(false);

        // 显示游戏屏幕
        gameScreen.show(singleGameController);

        // 设置生命值和子弹数
        gameScreen.setPlayerLives(3);
        gameScreen.setBulletCount(10);

        // 立即更新所有显示元素
        gameView.updateHealthDisplay();
        gameScreen.updateLivesDisplay(gameScreen.getPlayerLives());
        gameScreen.updateEnemiesDisplay(singleGameController);
        gameScreen.updateBulletDisplay(gameScreen.getBulletCount());

        // 记录游戏开始时间
        gameView.resetGameStartTime();
        gameScreen.setLastBulletRefillTime(System.currentTimeMillis());

        // 确保画布获取焦点
        if (gameScreen.getGameCanvas() != null) {
            gameScreen.getGameCanvas().requestFocus();
        }

        // 启动游戏循环
        gameView.startGameLoop();
    }

    /**
     * 获取游戏控制器
     * 
     * @return the gameController
     */
    public SingleGameController getGameController() {
        return singleGameController;
    }

    /**
     * 设置游戏控制器
     * 
     * @param singleGameController the gameController to set
     */
    public void setGameController(SingleGameController singleGameController) {
        this.singleGameController = singleGameController;
    }

    /**
     * 获取游戏屏幕组件
     * 
     * @return 游戏屏幕实例
     */
    public SinglePlayerGameScreen getGameScreen() {
        return gameScreen;
    }


    @Override
    public void setGameController(GameController controller) {
        if (controller instanceof SingleGameController) {
            this.singleGameController = (SingleGameController) controller;
        }
    }
}