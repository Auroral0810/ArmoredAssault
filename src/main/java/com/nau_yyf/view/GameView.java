package com.nau_yyf.view;

import com.nau_yyf.controller.SingleGameController;
import com.nau_yyf.controller.SinglePlayerGameStarter;
import com.nau_yyf.model.Tank;
import com.nau_yyf.view.singleGame.SingleGameOverScreen;
import com.nau_yyf.view.singleGame.SingleLevelCompletedView;
import com.nau_yyf.view.singleGame.SinglePlayerOptionsView;
import com.nau_yyf.view.singleGame.SingleTankSelectionView;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

import com.nau_yyf.service.KeyboardService;
import com.nau_yyf.service.PlayerService;
import com.nau_yyf.service.serviceImpl.SingleKeyboardServiceImpl;
import com.nau_yyf.service.GameStateService;
import com.nau_yyf.service.serviceImpl.SingleGameStateServiceImpl;
import com.nau_yyf.service.EffectService;
import com.nau_yyf.service.serviceImpl.SingleEffectServiceImpl;
import com.nau_yyf.service.GameLoopService;
import com.nau_yyf.service.serviceImpl.SingleGameLoopServiceImpl;
import com.nau_yyf.service.serviceImpl.SinglePlayerServiceImpl;
import com.nau_yyf.service.RenderService;
import com.nau_yyf.service.serviceImpl.SingleRenderServiceImpl;
import com.nau_yyf.view.multiGame.MultiPlayerOptionsView;
import com.nau_yyf.view.multiGame.MultiTankSelectionView;
import com.nau_yyf.util.TankUtil;

/**
 * 游戏视图类，负责管理游戏界面和用户交互
 * <p>
 * 该类采用MVC架构中的View角色，协调各种服务组件与UI元素，
 * 处理游戏菜单、画面渲染及用户输入等功能，但不直接包含游戏逻辑
 * </p>
 */
public class GameView {
    /** 游戏窗口和场景 */
    private Stage stage;
    private Scene scene;
    private StackPane root;
    private SingleGameController singleGameController;

    /** 游戏主题常量 */
    private final String GAME_TITLE = "TANK 2025";
    private final Color PRIMARY_COLOR = Color.rgb(37, 160, 218); // 蓝色
    private final Color TEXT_COLOR = Color.WHITE;
    private final Color BACKGROUND_COLOR = Color.rgb(27, 40, 56); // 深蓝灰色

    /** 游戏画布 */
    private Canvas gameCanvas;
    private GraphicsContext gc;

    /** 游戏状态变量 */
    private long totalGameTime; // 游戏总时间（毫秒）
    private long lastUpdateTime; // 上次更新时间
    private boolean gamePaused = false; // 游戏是否暂停
    private boolean isPauseMenuOpen = false; // 暂停菜单是否打开
    private Text timeInfo; // 时间显示文本
    private int bulletCount = 10; // 初始子弹数量
    private long lastBulletRefillTime = 0; // 上次子弹补充时间
    private HBox gameDataPanel;
    private AnimationTimer gameLoop;
    private int playerLives = 3; // 初始生命数为3

    /** UI元素映射 */
    private Map<String, ImageView> powerUpIndicators = new HashMap<>(); // 存储增益效果指示器
    private Map<String, ProgressBar> powerUpProgressBars = new HashMap<>(); // 存储增益效果进度条
    private Map<String, HBox> effectBoxMap = new HashMap<>(); // 存储每种效果的容器

    /** 游戏视图组件 */
    private MainMenuView mainMenuView;
    private SinglePlayerOptionsView singlePlayerOptionsView;
    private SingleTankSelectionView singleTankSelectionView;
    private LevelSelectionDialog levelSelectionDialog;
    private SinglePlayerGameStarter singlePlayerGameStarter;
    private String currentTankType = "standard"; // 默认使用标准坦克
    private PauseMenuView pauseMenuView;
    private SingleLevelCompletedView singleLevelCompletedView;
    private SingleGameOverScreen singleGameOverScreen;
    private SettingsDialog settingsDialog;

    /** 服务层组件 */
    private KeyboardService keyboardService;
    private PlayerService.InputState inputState = new PlayerService.InputState();
    private GameStateService gameStateService;
    private EffectService effectService;
    private GameLoopService gameLoopService;
    private PlayerService playerService;
    private RenderService renderService;

    /** 双人模式相关组件 */
    private MultiPlayerOptionsView multiPlayerOptionsView;
    private MultiTankSelectionView multiTankSelectionView;

    /** 双人游戏相关变量 */
    private String p1TankType;
    private String p2TankType;

    /**
     * 构造函数，初始化游戏视图
     * 
     * @param stage JavaFX主舞台
     */
    public GameView(Stage stage) {
        this.stage = stage;
        this.stage.setTitle(GAME_TITLE);
        this.stage.setResizable(false);

        // 设置应用图标
        try {
            Image appIcon = new Image(getClass().getResourceAsStream("/images/logo/tank_logo.png"));
            stage.getIcons().add(appIcon);
        } catch (Exception e) {
            System.err.println("无法加载应用图标: " + e.getMessage());
        }

        // 初始化根布局
        root = new StackPane();

        // 加载背景图
        try {
            Image backgroundImage = new Image(
                    getClass().getResourceAsStream("/images/backgrounds/game_background.png"));
            BackgroundImage background = new BackgroundImage(
                    backgroundImage,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.CENTER,
                    new BackgroundSize(100, 100, true, true, false, true));
            root.setBackground(new Background(background));
        } catch (Exception e) {
            System.err.println("无法加载背景图: " + e.getMessage());
            // 回退到纯色背景
            root.setBackground(new Background(new BackgroundFill(
                    BACKGROUND_COLOR, CornerRadii.EMPTY, Insets.EMPTY)));
        }

        scene = new Scene(root, 1024, 768);

        // 先设置场景
        stage.setScene(scene);

        // 延迟加载样式表
        try {
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        } catch (Exception e) {
            System.err.println("无法加载样式表: " + e.getMessage());
        }

        // 初始化视图组件
        this.mainMenuView = new MainMenuView(this, root, stage);
        this.singlePlayerOptionsView = new SinglePlayerOptionsView(this, root, stage);
        this.singleTankSelectionView = new SingleTankSelectionView(this, root, stage);
        this.levelSelectionDialog = new LevelSelectionDialog(this, root);
        this.singlePlayerGameStarter = new SinglePlayerGameStarter(this);
        this.pauseMenuView = new PauseMenuView(this, root, scene);
        this.singleLevelCompletedView = new SingleLevelCompletedView(this, root, scene);
        this.singleGameOverScreen = new SingleGameOverScreen(this, root, scene);
        this.settingsDialog = new SettingsDialog(this, root);

        // 初始化双人模式组件
        this.multiPlayerOptionsView = new MultiPlayerOptionsView(this, root, stage);
        this.multiTankSelectionView = new MultiTankSelectionView(this, root, stage);

        // 初始化服务层
        this.keyboardService = new SingleKeyboardServiceImpl();
        this.gameStateService = new SingleGameStateServiceImpl(this, stage);
        this.effectService = new SingleEffectServiceImpl();
        this.playerService = new SinglePlayerServiceImpl(this, effectService);
        this.gameLoopService = new SingleGameLoopServiceImpl(this, effectService, playerService);
        this.renderService = new SingleRenderServiceImpl();

        // 使用Platform.runLater处理后续的UI更新
        Platform.runLater(() -> {
            // 显示主菜单
            showMainMenu();
        });
    }

    /**
     * 显示主菜单界面
     */
    public void showMainMenu() {
        mainMenuView.show();
    }

    /**
     * 显示单人游戏选项界面
     */
    public void showSinglePlayerOptions() {
        singlePlayerOptionsView.show();
    }

    /**
     * 显示坦克选择界面
     */
    public void showTankSelection() {
        singleTankSelectionView.show();
    }

    /**
     * 开始游戏，显示关卡选择对话框
     * 
     * @param selectedTankType 选择的坦克类型
     */
    public void startGame(String selectedTankType) {
        this.currentTankType = selectedTankType; // 保存当前使用的坦克类型
        levelSelectionDialog.show(selectedTankType);
    }

    /**
     * 以指定的坦克类型和关卡开始游戏
     * 
     * @param selectedTankType 选择的坦克类型
     * @param level 选择的关卡
     */
    public void startGameWithLevel(String selectedTankType, int level) {
        // 使用SinglePlayerGameStarter启动游戏
        singlePlayerGameStarter.startGame(selectedTankType, level);
    }

    /**
     * 设置键盘控制
     */
    public void setupKeyboardControls() {
        // 使用键盘服务设置控制
        keyboardService.setupKeyboardControls(
                singleGameController,
                gameCanvas,
                this::showPauseMenu, // 暂停回调
                this::closePauseMenu // 恢复回调
        );

        // 获取初始输入状态
        if (keyboardService instanceof SingleKeyboardServiceImpl) {
            inputState = ((SingleKeyboardServiceImpl) keyboardService).getInputState();
        }
    }

    /**
     * 更新时间显示
     * 
     * @param totalTimeMillis 总游戏时间(毫秒)
     */
    private void updateTimeDisplay(long totalTimeMillis) {
        long seconds = totalTimeMillis / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;

        timeInfo.setText(String.format("%02d:%02d", minutes, seconds));
    }

    /**
     * 启动游戏循环
     */
    public void startGameLoop() {
        // 先停止旧的游戏循环
        if (gameLoop != null) {
            gameLoopService.stopGameLoop(gameLoop);
        }

        // 创建并启动新的游戏循环
        gameLoop = gameLoopService.createGameLoop(
                singleGameController,
                this::renderGame, // 渲染回调
                this::updateTimeDisplay // 时间更新回调
        );
    }

    /**
     * 渲染游戏画面
     */
    private void renderGame() {
        // 使用渲染服务渲染游戏
        if (gc != null && gameCanvas != null) {
            renderService.renderGame(
                    singleGameController,
                gc,
                gameCanvas.getWidth(),
                gameCanvas.getHeight()
            );
        }
    }

    /**
     * 显示游戏说明界面
     */
    public void showInstructions() {
        InstructionsView instructionsView = new InstructionsView(root);
        instructionsView.show();
    }

    /**
     * 显示消息对话框
     * 
     * @param message 要显示的消息内容
     */
    public void showMessage(String message) {
        Platform.runLater(() -> {
            // 使用MessageDialog类显示消息
            MessageDialog messageDialog = new MessageDialog(root, PRIMARY_COLOR, TEXT_COLOR);
            messageDialog.show(message);
        });
    }

    /**
     * 显示暂停菜单
     */
    public void showPauseMenu() {
        if (isPauseMenuOpen)
            return;

        // 暂停游戏循环
        gameLoopService.pauseGameLoop(gameLoop);

        pauseMenuView.show();
    }

    /**
     * 关闭暂停菜单
     */
    public void closePauseMenu() {
        pauseMenuView.close();
    }

    /**
     * 获取暂停菜单状态
     * 
     * @return 暂停菜单是否打开
     */
    public boolean getIsPauseMenuOpen() {
        return isPauseMenuOpen;
    }

    /**
     * 重新开始当前关卡
     */
    public void restartGame() {
        Platform.runLater(() -> {
            // 关闭暂停菜单
            closePauseMenu();

            if (singleGameController != null) {
                // 获取当前关卡和坦克类型
                int currentLevel = singleGameController.getCurrentLevel();
                String tankType = singleGameController.getPlayerTank().getTypeString();

                // 使用游戏状态服务重启游戏
                gameStateService.restartGame(singleGameController, tankType, currentLevel);
            }
        });
    }

    /**
     * 保存游戏进度
     */
    void saveGame() {
        // 请求用户输入存档名称
        TextInputDialog dialog = new TextInputDialog("存档" + (System.currentTimeMillis() / 1000));
        dialog.setTitle("保存游戏");
        dialog.setHeaderText("请输入存档名称");
        dialog.setContentText("名称:");

        dialog.showAndWait().ifPresent(saveName -> {
            // 使用游戏状态服务保存游戏
            boolean success = gameStateService.saveGame(singleGameController, saveName);

            // 显示结果
            if (success) {
                showMessage("游戏保存成功！");
            } else {
                showMessage("游戏保存失败！");
            }
        });
    }

    /**
     * 显示设置菜单
     */
    void showSettings() {
        Platform.runLater(() -> {
            // 关闭暂停菜单
            if (root.getChildren().size() > 1) {
                root.getChildren().remove(root.getChildren().size() - 1);
            }
            isPauseMenuOpen = false;
            // 显示设置对话框
            settingsDialog.show();
        });
    }

    /**
     * 清理游戏资源
     */
    public void cleanupGameResources() {
        // 停止游戏循环
        if (gameLoop != null) {
            gameLoopService.stopGameLoop(gameLoop);
            gameLoop = null;
        }

        // 清除所有事件监听器
        if (gameCanvas != null) {
            gameCanvas.setOnKeyPressed(null);
            gameCanvas.setOnKeyReleased(null);
        }

        // 重置所有游戏变量
        bulletCount = 10;
        gamePaused = false;
        isPauseMenuOpen = false;
        totalGameTime = 0;

        // 设置gameController为null前确保没有引用它的任务在运行
        if (singleGameController != null) {
            singleGameController = null;
        }

        // 清空UI
        if (root != null) {
            root.getChildren().clear();
        }

        // 重置生命数
        playerLives = 3;

        // 强制执行垃圾回收
        System.gc();

        // 确保游戏循环被停止
        if (gameLoop != null) {
            gameLoopService.stopGameLoop(gameLoop);
            gameLoop = null; // 将引用设为null以便垃圾回收
        }

        // 清除键盘控制
        if (keyboardService != null && gameCanvas != null) {
            keyboardService.clearKeyboardControls(gameCanvas);
        }

        // 重置按键状态
        inputState = new PlayerService.InputState();
    }

    /**
     * 处理玩家输入
     * 
     * @param playerTank 玩家坦克对象
     */
    public void handlePlayerInput(Tank playerTank) {
        if (playerTank == null || singleGameController == null)
            return;

        // 使用PlayerService处理输入
        int newBulletCount = playerService.handlePlayerInput(singleGameController, inputState, bulletCount);

        // 如果子弹数量变化，更新显示
        if (newBulletCount != bulletCount) {
            bulletCount = newBulletCount;

            // 使用SinglePlayerGameScreen更新子弹显示
            if (singlePlayerGameStarter != null && singlePlayerGameStarter.getGameScreen() != null) {
                singlePlayerGameStarter.getGameScreen().updateBulletDisplay(bulletCount);
            }
        }
    }

    /**
     * 设置玩家生命值
     * 
     * @param lives 生命值
     */
    public void setPlayerLives(int lives) {
        this.playerLives = lives;

        // 使用SinglePlayerGameScreen更新生命显示
        if (singlePlayerGameStarter != null && singlePlayerGameStarter.getGameScreen() != null) {
            singlePlayerGameStarter.getGameScreen().updateLivesDisplay(lives);
        }
    }

    /**
     * 设置子弹数量
     * 
     * @param count 子弹数量
     */
    public void setBulletCount(int count) {
        this.bulletCount = count;

        // 使用SinglePlayerGameScreen更新子弹显示
        if (singlePlayerGameStarter != null && singlePlayerGameStarter.getGameScreen() != null) {
            singlePlayerGameStarter.getGameScreen().updateBulletDisplay(count);
        }
    }

    /**
     * 更新增益效果UI显示
     */
    public void updatePowerUpUIDisplay() {
        if (singleGameController == null || singleGameController.getPlayerTank() == null)
            return;

        if (singlePlayerGameStarter != null && singlePlayerGameStarter.getGameScreen() != null) {
            singlePlayerGameStarter.getGameScreen()
                    .updatePowerUpUIDisplay(singleGameController, effectService);
        }
    }

    /**
     * 重置游戏开始时间，保持总游戏时间不变
     */
    public void resetGameStartTime() {
        if (gameLoopService instanceof SingleGameLoopServiceImpl) {
            ((SingleGameLoopServiceImpl) gameLoopService).resetGameStartTime();
        }
    }

    /**
     * 加载游戏存档
     */
    public void loadGame() {
        // 使用游戏状态服务加载游戏
        boolean success = ((SingleGameStateServiceImpl) gameStateService).showLoadGameDialog(singleGameController);

        if (success) {
            // 加载成功，切换到游戏界面
            showGameScreen();
            // 重置游戏开始时间
            resetGameStartTime();
            // 启动游戏循环
            startGameLoop();
        } else {
            // 加载失败，显示错误信息
            showMessage("游戏存档加载失败！");
        }
    }

    /**
     * 显示游戏主界面
     */
    private void showGameScreen() {
        if (singlePlayerGameStarter != null && singleGameController != null) {
            singlePlayerGameStarter.getGameScreen().show(singleGameController);
        } else {
            System.err.println("无法显示游戏屏幕: gameController 或 singlePlayerGameStarter 为 null");
        }
    }

    /**
     * 恢复游戏循环
     */
    public void resumeGameLoop() {
        gameLoopService.resumeGameLoop(gameLoop);
        gamePaused = false;
        isPauseMenuOpen = false;
    }

    /**
     * 停止游戏循环
     */
    public void stopGameLoop() {
        if (gameLoop != null) {
            gameLoopService.stopGameLoop(gameLoop);
        }
    }

    /**
     * 更新玩家血量显示
     */
    public void updateHealthDisplay() {
        if (singleGameController == null)
            return;

        if (singlePlayerGameStarter != null && singlePlayerGameStarter.getGameScreen() != null) {
            singlePlayerGameStarter.getGameScreen().updateHealthDisplay(singleGameController);
        }
    }

    /**
     * 更新子弹数量显示
     */
    public void updateBulletDisplay() {
        if (singleGameController == null) return;
        
        if (singlePlayerGameStarter != null && singlePlayerGameStarter.getGameScreen() != null) {
            singlePlayerGameStarter.getGameScreen().updateBulletDisplay(bulletCount);
        }
    }

    /**
     * 显示双人游戏选项界面
     */
    public void showMultiPlayerOptions() {
        multiPlayerOptionsView.show();
    }

    /**
     * 显示双人坦克选择界面
     */
    public void showMultiTankSelection() {
        multiTankSelectionView.show();
    }

    /**
     * 显示双人游戏关卡选择对话框
     * @param p1TankType 玩家1坦克类型
     * @param p2TankType 玩家2坦克类型
     */
    public void showMultiPlayerLevelSelection(String p1TankType, String p2TankType) {
        // 先将两种坦克类型存储起来
        // 然后调用关卡选择对话框
        // 注：这里使用的是与单人游戏相同的对话框，但后续可能需要创建专门的双人游戏关卡选择对话框
        this.p1TankType = p1TankType;
        this.p2TankType = p2TankType;
        if (levelSelectionDialog == null) {
            levelSelectionDialog = new LevelSelectionDialog(this, root);
        }
        levelSelectionDialog.show(p1TankType); // 传递玩家1的坦克类型作为参考
    }

    /**
     * 启动双人游戏
     * @param level 关卡编号
     */
    public void startMultiPlayerGame(int level) {
        showMessage("即将开始双人游戏第" + level + "关\n玩家1坦克: " + 
                    TankUtil.getTankDisplayName(p1TankType) + 
                    "\n玩家2坦克: " + TankUtil.getTankDisplayName(p2TankType));
        
        // 以下是实际的游戏启动代码，需要在实现双人游戏逻辑后添加
        // multiPlayerGameStarter.startGame(p1TankType, p2TankType, level);
    }

    /* ------ getter/setter方法 ------ */
    
    /**
     * 获取根布局容器
     * @return 根StackPane
     */
    public StackPane getRoot() {
        return root;
    }

    /**
     * 获取主题色
     * @return 主题颜色
     */
    public Color getPrimaryColor() {
        return PRIMARY_COLOR;
    }

    /**
     * 获取文本颜色
     * @return 文本颜色
     */
    public Color getTextColor() {
        return TEXT_COLOR;
    }

    /**
     * 获取上次子弹补充时间
     * @return 上次子弹补充时间(毫秒)
     */
    public long getLastBulletRefillTime() {
        return lastBulletRefillTime;
    }

    /**
     * 获取游戏画布
     * @return 游戏画布
     */
    public Canvas getGameCanvas() {
        return gameCanvas;
    }

    /**
     * 获取玩家服务
     * @return 玩家服务
     */
    public PlayerService getPlayerService() {
        return playerService;
    }

    /**
     * 获取游戏状态服务
     * @return 游戏状态服务
     */
    public GameStateService getGameStateService() {
        return gameStateService;
    }

    /**
     * 获取关卡完成视图
     * @return 关卡完成视图
     */
    public SingleLevelCompletedView getLevelCompletedView() {
        return singleLevelCompletedView;
    }

    /**
     * 获取游戏结束界面
     * @return 游戏结束界面
     */
    public SingleGameOverScreen getGameOverScreen() {
        return singleGameOverScreen;
    }

    /**
     * 获取单人游戏启动器
     * @return 单人游戏启动器
     */
    public SinglePlayerGameStarter getSinglePlayerGameStarter() {
        return singlePlayerGameStarter;
    }

    /**
     * 获取能力增强效果进度条映射
     * @return 进度条映射
     */
    public Map<String, ProgressBar> getPowerUpProgressBars() {
        return powerUpProgressBars;
    }

    /**
     * 获取游戏控制器
     * @return 游戏控制器
     */
    public SingleGameController getGameController() {
        return singleGameController;
    }

    /**
     * 获取效果服务
     * @return 效果服务
     */
    public EffectService getEffectService() {
        return effectService;
    }

    /**
     * 获取游戏总时间
     * @return 游戏总时间(毫秒)
     */
    public long getTotalGameTime() {
        return totalGameTime;
    }

    /**
     * 获取当前游戏得分
     * @return 当前得分
     */
    public int getScore() {
        // 使用游戏状态服务计算得分
        return ((SingleGameStateServiceImpl) gameStateService).getScore(singleGameController);
    }

    /**
     * 获取玩家生命值
     * @return 玩家生命值
     */
    public int getPlayerLives() {
        return playerLives;
    }

    /**
     * 获取子弹数量
     * @return 子弹数量
     */
    public int getBulletCount() {
        return bulletCount;
    }

    /**
     * 设置游戏总时间
     * @param time
     */
    public void setTotalGameTime(long time) {
        this.totalGameTime = time;
        updateTimeDisplay(totalGameTime);
    }

    /**
     * 设置得分
     * @param score 得分
     */
    public void setScore(int score) {
        // 暂未实现
    }

    /**
     * 设置时间信息显示文本
     * @param timeInfo 时间文本组件
     */
    public void setTimeInfo(Text timeInfo) {
        this.timeInfo = timeInfo;
    }

    /**
     * 设置游戏画布
     * @param gameCanvas 游戏画布
     */
    public void setGameCanvas(Canvas gameCanvas) {
        this.gameCanvas = gameCanvas;
    }

    /**
     * 设置图形上下文
     * @param gc 图形上下文
     */
    public void setGraphicsContext(GraphicsContext gc) {
        this.gc = gc;
    }

    /**
     * 设置游戏数据面板
     * @param gameDataPanel 游戏数据面板
     */
    public void setGameDataPanel(HBox gameDataPanel) {
        this.gameDataPanel = gameDataPanel;
    }

    /**
     * 设置游戏暂停状态
     * @param paused 是否暂停
     */
    public void setGamePaused(boolean paused) {
        this.gamePaused = paused;
    }

    /**
     * 设置暂停菜单打开状态
     * @param open 是否打开
     */
    public void setIsPauseMenuOpen(boolean open) {
        this.isPauseMenuOpen = open;
    }

    /**
     * 设置上次更新时间
     * @param time 时间戳
     */
    public void setLastUpdateTime(long time) {
        this.lastUpdateTime = time;
    }

    /**
     * 设置上次子弹补充时间
     * @param time 时间戳
     */
    public void setLastBulletRefillTime(long time) {
        this.lastBulletRefillTime = time;
    }

    /**
     * 设置游戏控制器
     * @param singleGameController 游戏控制器
     */
    public void setGameController(SingleGameController singleGameController) {
        this.singleGameController = singleGameController;
    }

    /**
     * 设置能力增强效果进度条映射
     * @param progressBars 进度条映射
     */
    public void setPowerUpProgressBars(Map<String, ProgressBar> progressBars) {
        this.powerUpProgressBars = progressBars;
    }

    /**
     * 获取玩家1的坦克类型
     * @return 玩家1坦克类型
     */
    public String getP1TankType() {
        return p1TankType;
    }

    /**
     * 获取玩家2的坦克类型
     * @return 玩家2坦克类型
     */
    public String getP2TankType() {
        return p2TankType;
    }
}
