package com.nau_yyf.controller;

import com.nau_yyf.view.GameView;
import com.nau_yyf.view.multiGame.MultiPlayerGameScreen;
import javafx.application.Platform;

/**
 * 多人游戏启动器
 * 负责初始化和启动双人游戏
 */
public class MultiPlayerGameStarter implements GameStarterController {

    private GameView gameView;
    private MultiGameController multiGameController;
    private MultiPlayerGameScreen gameScreen;

    /**
     * 构造函数
     *
     * @param gameView 游戏主视图引用
     */
    public MultiPlayerGameStarter(GameView gameView) {
        this.gameView = gameView;
        this.gameScreen = new MultiPlayerGameScreen(gameView);
    }

    /**
     * 以指定的两种坦克类型和关卡开始游戏
     *
     * @param player1TankType 玩家1的坦克类型
     * @param player2TankType 玩家2的坦克类型
     * @param level 关卡编号
     */
    public void startGame(String player1TankType, String player2TankType, int level) {
        // 设置游戏模式为多人游戏
        gameView.setGameMode(GameView.GAME_MODE_MULTI);

        // 创建游戏控制器并加载关卡
        multiGameController = new MultiGameController();
        multiGameController.loadLevel(level);

        // 设置两个玩家的坦克类型
        setPlayerTankTypes(player1TankType, player2TankType);

        // 设置对GameView的引用
        multiGameController.setGameView(gameView);

        // 确保GameView也拥有对此控制器的引用
        gameView.setGameController(multiGameController);

        // 设置事件监听器
        setGameEventListeners();

        // 初始化游戏状态
        gameScreen.setGamePaused(false);
        gameScreen.setIsPauseMenuOpen(false);

        // 显示游戏屏幕
        gameScreen.show(multiGameController);

        // 设置初始生命值和子弹数
        setupInitialGameState();

        // 立即更新所有显示元素
        updateDisplayElements();

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
     * 设置玩家坦克类型
     */
    private void setPlayerTankTypes(String player1TankType, String player2TankType) {
        // 创建两个玩家的坦克并设置类型
        multiGameController.setPlayer1TankType(player1TankType);
        multiGameController.setPlayer2TankType(player2TankType);
    }

    /**
     * 设置游戏事件监听器
     */
    private void setGameEventListeners() {
        multiGameController.setGameEventListener(new MultiGameController.GameEventListener() {
            @Override
            public void onPlayer1Destroyed() {
                // 在JavaFX应用线程中处理玩家1坦克摧毁事件
                Platform.runLater(() -> {
                    gameScreen.handlePlayer1Destroyed(
                            multiGameController,
                            "gameView.getP1TankType()",
                            gameScreen.getPlayer1Lives());
                });
            }

            @Override
            public void onPlayer2Destroyed() {
                // 在JavaFX应用线程中处理玩家2坦克摧毁事件
                Platform.runLater(() -> {
                    gameScreen.handlePlayer2Destroyed(
                            multiGameController,
                            "gameView.getP2TankType()",
                            gameScreen.getPlayer2Lives());
                });
            }

            @Override
            public void onBothPlayersDestroyed() {
                // 处理两个玩家都被摧毁的情况（游戏结束）
                Platform.runLater(() -> {
                    gameScreen.handleBothPlayersDestroyed(multiGameController);
                });
            }
        });
    }

    /**
     * 设置初始游戏状态
     */
    private void setupInitialGameState() {
        // 设置玩家1生命值和子弹数
        gameScreen.setPlayer1Lives(3);
        gameScreen.setPlayer1BulletCount(10);

        // 设置玩家2生命值和子弹数
        gameScreen.setPlayer2Lives(3);
        gameScreen.setPlayer2BulletCount(10);
        
        // 设置最后子弹补充时间
        gameScreen.setLastBulletRefillTime(System.currentTimeMillis());
    }

    /**
     * 更新显示元素
     */
    private void updateDisplayElements() {
        // 更新玩家1健康状态
        gameScreen.updatePlayer1HealthDisplay(multiGameController);

        // 更新玩家2健康状态
        gameScreen.updatePlayer2HealthDisplay(multiGameController);

        // 更新生命值显示
        gameScreen.updateLivesDisplay(gameScreen.getPlayer1Lives(), gameScreen.getPlayer2Lives());

        // 更新敌人显示
        gameScreen.updateEnemiesDisplay(multiGameController);

        // 更新子弹数量显示
        gameScreen.updateBulletDisplay(gameScreen.getPlayer1BulletCount(), gameScreen.getPlayer2BulletCount());
    }

    /**
     * 获取游戏控制器
     *
     * @return 多人游戏控制器
     */
    public MultiGameController getGameController() {
        return multiGameController;
    }

    /**
     * 设置游戏控制器
     *
     * @param multiGameController 多人游戏控制器
     */
    public void setGameController(MultiGameController multiGameController) {
        this.multiGameController = multiGameController;
    }

    /**
     * 获取游戏屏幕组件
     *
     * @return 多人游戏屏幕实例
     */
    public MultiPlayerGameScreen getGameScreen() {
        return gameScreen;
    }

    /**
     * 实现GameStarterController接口方法
     */
    @Override
    public void setGameController(GameController controller) {
        if (controller instanceof MultiGameController) {
            this.multiGameController = (MultiGameController) controller;
        }
    }

    @Override
    public void startGame(String tankType, int level) {
        // 在多人模式下，这个方法可能不完全适用，但需要提供实现
        // 可以使用默认逻辑或抛出异常
        throw new UnsupportedOperationException("多人游戏启动需要两种坦克类型。请使用 startGame(String p1TankType, String p2TankType, int level) 方法");
    }

    /**
     * 实现GameStarterController接口的getController方法
     * @return 游戏控制器
     */
    @Override
    public GameController getController() {
        return multiGameController;
    }
}