package com.nau_yyf.controller;

import com.nau_yyf.view.GameView;
import com.nau_yyf.view.multiGame.MultiPlayerGameScreen;
import javafx.application.Platform;

/**
 * 双人游戏启动器
 * 负责初始化和启动双人游戏
 */
public class MultiPlayerGameStarter {

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
     * 以指定的两种坦克类型和关卡开始双人游戏
     * 
     * @param p1TankType 玩家1的坦克类型
     * @param p2TankType 玩家2的坦克类型
     * @param level 关卡编号
     */
    public void startGame(String p1TankType, String p2TankType, int level) {
        // 设置游戏模式为双人游戏
        gameView.setGameMode(GameView.GAME_MODE_MULTI);
        
        // 创建多人游戏控制器并加载关卡
        multiGameController = new MultiGameController();
        
        // 加载指定关卡
        loadLevel(level);
        
        // 设置玩家坦克类型
        setPlayerTankTypes(p1TankType, p2TankType);
        
        // 设置对GameView的引用
        setGameViewReference();
        
        // 设置事件监听器
        setupEventListeners();
        
        // 初始化游戏状态
        initializeGameState();
        
        // 显示游戏屏幕
        displayGameScreen();
        
        // 更新界面元素
        updateUIElements();
        
        // 记录游戏开始时间
        recordGameStartTime();
        
        // 确保画布获取焦点
        ensureCanvasFocus();
        
        // 启动游戏循环
        startGameLoop();
    }

    /**
     * 加载指定关卡
     */
    private void loadLevel(int level) {
        // TODO: 实现双人游戏关卡加载
        System.out.println("加载双人游戏第" + level + "关");
        // multiGameController.loadLevel(level);
    }

    /**
     * 设置玩家坦克类型
     */
    private void setPlayerTankTypes(String p1TankType, String p2TankType) {
        // TODO: 实现设置玩家坦克类型
        System.out.println("设置玩家1坦克类型: " + p1TankType);
        System.out.println("设置玩家2坦克类型: " + p2TankType);
        // multiGameController.setPlayerTankTypes(p1TankType, p2TankType);
    }

    /**
     * 设置对GameView的引用
     */
    private void setGameViewReference() {
        // 设置MultiGameController对GameView的引用
        // multiGameController.setGameView(gameView);

        // 确保GameView也拥有对此控制器的引用
        // gameView.setMultiGameController(multiGameController);
    }

    /**
     * 设置事件监听器
     */
    private void setupEventListeners() {
        // TODO: 为多人游戏设置事件监听器
        // 监听玩家1被摧毁事件
        // 监听玩家2被摧毁事件
        // 监听游戏结束事件
        /*
        multiGameController.setGameEventListener(new MultiGameController.GameEventListener() {
            @Override
            public void onPlayer1Destroyed() {
                Platform.runLater(() -> {
                    gameScreen.handlePlayer1Destroyed(
                            multiGameController,
                            gameView.getP1TankType(),
                            player1Lives);
                });
            }
            
            @Override
            public void onPlayer2Destroyed() {
                Platform.runLater(() -> {
                    gameScreen.handlePlayer2Destroyed(
                            multiGameController,
                            gameView.getP2TankType(),
                            player2Lives);
                });
            }
            
            @Override
            public void onBothPlayersDestroyed() {
                Platform.runLater(() -> {
                    gameScreen.handleBothPlayersDestroyed();
                });
            }
        });
        */
    }

    /**
     * 初始化游戏状态
     */
    private void initializeGameState() {
        gameView.setGamePaused(false);
        gameView.setIsPauseMenuOpen(false);
    }

    /**
     * 显示游戏屏幕
     */
    private void displayGameScreen() {
        // 显示多人游戏屏幕
        // gameScreen.show(multiGameController);
    }

    /**
     * 更新界面元素
     */
    private void updateUIElements() {
        // 设置玩家生命值和子弹数量
        // 更新健康状态、生命显示、敌人数量等
        /*
        // 设置玩家1的初始生命值和子弹数
        player1Lives = 3;
        player1BulletCount = 10;
        
        // 设置玩家2的初始生命值和子弹数
        player2Lives = 3;
        player2BulletCount = 10;
        
        // 更新显示
        gameScreen.updatePlayer1HealthDisplay(multiGameController);
        gameScreen.updatePlayer2HealthDisplay(multiGameController);
        gameScreen.updateLivesDisplay(player1Lives, player2Lives);
        gameScreen.updateEnemiesDisplay(multiGameController);
        gameScreen.updateBulletDisplay(player1BulletCount, player2BulletCount);
        */
    }

    /**
     * 记录游戏开始时间
     */
    private void recordGameStartTime() {
        gameView.resetGameStartTime();
        gameView.setLastBulletRefillTime(System.currentTimeMillis());
    }

    /**
     * 确保画布获取焦点
     */
    private void ensureCanvasFocus() {
        if (gameScreen.getGameCanvas() != null) {
            gameScreen.getGameCanvas().requestFocus();
        }
    }

    /**
     * 启动游戏循环
     */
    private void startGameLoop() {
        gameView.startGameLoop();
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
}