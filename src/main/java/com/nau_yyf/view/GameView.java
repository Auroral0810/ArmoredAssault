package com.nau_yyf.view;

import com.nau_yyf.controller.*;
import com.nau_yyf.model.Tank;
import com.nau_yyf.service.serviceImpl.*;
import com.nau_yyf.view.multiGame.*;
import com.nau_yyf.view.singleGame.*;
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
import java.util.function.Consumer;

import com.nau_yyf.service.KeyboardService;
import com.nau_yyf.service.PlayerService;
import com.nau_yyf.service.GameStateService;
import com.nau_yyf.service.EffectService;
import com.nau_yyf.service.GameLoopService;
import com.nau_yyf.service.RenderService;
import com.nau_yyf.util.TankUtil;

/**
 * 游戏视图类，负责管理游戏界面和用户交互
 * 该类采用MVC架构中的View角色，协调各种服务组件与UI元素，
 * 处理游戏菜单、画面渲染及用户输入等功能，但不直接包含游戏逻辑
 */
public class GameView {
    /** 游戏窗口和场景 */
    private Stage stage;
    private Scene scene;
    private StackPane root;

    /** 游戏主题常量 */
    private final String GAME_TITLE = "ArmoredAssault";
    private final Color PRIMARY_COLOR = Color.rgb(37, 160, 218); // 蓝色
    private final Color TEXT_COLOR = Color.WHITE;
    private final Color BACKGROUND_COLOR = Color.rgb(27, 40, 56); // 深蓝灰色

    /** 游戏画布 */
    private Canvas gameCanvas;
    private GraphicsContext gc;

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

    /** 服务层组件 - 使用接口类型声明而非具体实现 */
    private KeyboardService keyboardService;
    private PlayerService.InputState inputState = new PlayerService.InputState();
    private GameStateService gameStateService;
    private EffectService effectService;
    private GameLoopService gameLoopService;
    private PlayerService playerService;
    private RenderService renderService;

    /** 服务层映射 - 存储不同游戏模式下的服务实现 */
    private Map<Integer, GameStateService> gameStateServices = new HashMap<>();
    private Map<Integer, EffectService> effectServices = new HashMap<>();
    private Map<Integer, PlayerService> playerServices = new HashMap<>();
    private Map<Integer, GameLoopService> gameLoopServices = new HashMap<>();
    private Map<Integer, RenderService> renderServices = new HashMap<>();
    private Map<Integer, KeyboardService> keyboardServices = new HashMap<>();

    /** 双人模式相关组件 */
    private MultiPlayerOptionsView multiPlayerOptionsView;
    private MultiTankSelectionView multiTankSelectionView;

    /** 双人游戏相关变量 */
    String p1TankType;
    String p2TankType;

    /** 游戏模式常量 */
    public static final int GAME_MODE_NONE = 0; // 无游戏模式（主菜单等）
    public static final int GAME_MODE_SINGLE = 1; // 单人游戏模式
    public static final int GAME_MODE_MULTI = 2; // 双人游戏模式
    public static final int GAME_MODE_ONLINE = 3; // 联机游戏模式

    /** 当前游戏模式 */
    private int currentGameMode = GAME_MODE_NONE;

    /** 游戏控制器相关字段 */
    private SingleGameController singleGameController; // 单人游戏控制器
    private MultiGameController multiGameController; // 未来的双人游戏控制器
    private OnlineGameController onlineGameController; // 未来的联机游戏控制器

    /** 当前活动的游戏控制器 */
    private GameController activeController;

    /** 添加多人游戏相关的界面组件 */
    private MultiGameOverScreen multiGameOverScreen;
    private MultiLevelCompletedView multiLevelCompletedView;

    /** 修改字段声明，使用接口类型 */
    private Map<Integer, GameOverScreen> gameOverScreens = new HashMap<>();
    private Map<Integer, LevelCompletedView> levelCompletedViews = new HashMap<>();

    /** 游戏启动器映射 */
    private Map<Integer, GameStarterController> gameStarters = new HashMap<>();

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

        // 初始化服务层 - 单人模式
        KeyboardService singleKeyboardService = new SingleKeyboardServiceImpl();
        GameStateService singleGameStateService = new SingleGameStateServiceImpl(this, stage);
        EffectService singleEffectService = new SingleEffectServiceImpl();
        PlayerService singlePlayerService = new SinglePlayerServiceImpl(this, singleEffectService);
        GameLoopService singleGameLoopService = new SingleGameLoopServiceImpl(this, singleEffectService,
                singlePlayerService);
        RenderService singleRenderService = new SingleRenderServiceImpl();

        // 存储单人模式服务
        keyboardServices.put(GAME_MODE_SINGLE, singleKeyboardService);
        gameStateServices.put(GAME_MODE_SINGLE, singleGameStateService);
        effectServices.put(GAME_MODE_SINGLE, singleEffectService);
        playerServices.put(GAME_MODE_SINGLE, singlePlayerService);
        gameLoopServices.put(GAME_MODE_SINGLE, singleGameLoopService);
        renderServices.put(GAME_MODE_SINGLE, singleRenderService);

        // 初始化服务层 - 多人模式（如果实现了）
        // KeyboardService multiKeyboardService = new MultiKeyboardServiceImpl();
        // GameStateService multiGameStateService = new MultiGameStateServiceImpl(this,
        // stage);
        // 以此类推...

        // 默认使用单人模式服务作为当前活动服务
        this.keyboardService = singleKeyboardService;
        this.gameStateService = singleGameStateService;
        this.effectService = singleEffectService;
        this.playerService = singlePlayerService;
        this.gameLoopService = singleGameLoopService;
        this.renderService = singleRenderService;

        // 初始化游戏结束和关卡完成视图
        this.gameOverScreens.put(GAME_MODE_SINGLE, new SingleGameOverScreen(this, root, scene));
        this.gameOverScreens.put(GAME_MODE_MULTI, new MultiGameOverScreen(this, root, scene));
        // this.gameOverScreens.put(GAME_MODE_ONLINE, new OnlineGameOverScreen(this,
        // root, scene)); // 未来可添加

        this.levelCompletedViews.put(GAME_MODE_SINGLE, new SingleLevelCompletedView(this, root, scene));
        this.levelCompletedViews.put(GAME_MODE_MULTI, new MultiLevelCompletedView(this, root, scene));
        // this.levelCompletedViews.put(GAME_MODE_ONLINE, new
        // OnlineLevelCompletedView(this, root, scene)); // 未来可添加

        // 初始化游戏启动器
        this.gameStarters.put(GAME_MODE_SINGLE, new SinglePlayerGameStarter(this));
        this.gameStarters.put(GAME_MODE_MULTI, new MultiPlayerGameStarter(this));
        // this.gameStarters.put(GAME_MODE_ONLINE, new OnlineGameStarter(this)); //
        // 未来可添加

        // 使用Platform.runLater处理后续的UI更新
        Platform.runLater(() -> {
            // 显示主菜单
            showMainMenu();
        });
    }


    /**
     * 清理游戏资源
     */
    public void cleanupGameResources() {
        // 停止游戏循环
        AnimationTimer gameLoop = getGameLoop();
        if (gameLoop != null) {
            gameLoopService.stopGameLoop(gameLoop);
        }

        // 清除所有事件监听器
        if (gameCanvas != null) {
            gameCanvas.setOnKeyPressed(null);
            gameCanvas.setOnKeyReleased(null);
        }

        // 根据游戏模式清理资源
        switch (currentGameMode) {
            case GAME_MODE_SINGLE:
                // 重置单人游戏变量
                if (singlePlayerGameStarter != null && singlePlayerGameStarter.getGameScreen() != null) {
                    singlePlayerGameStarter.getGameScreen().cleanupGameResources();
                    singlePlayerGameStarter.getGameScreen().setPlayerLives(3);
                }

                // 清理单人游戏控制器
                if (singleGameController != null) {
                    singleGameController = null;
                }
                break;

            case GAME_MODE_MULTI:
                // 重置多人游戏变量
                GameStarterController multiStarter = gameStarters.get(GAME_MODE_MULTI);
                if (multiStarter != null && multiStarter.getGameScreen() != null) {
                    multiStarter.getGameScreen().cleanupGameResources();
                }

                // 清理多人游戏控制器
                if (multiGameController != null) {
                    multiGameController = null;
                }
                break;

            case GAME_MODE_ONLINE:
                // 重置在线游戏变量
                GameStarterController onlineStarter = gameStarters.get(GAME_MODE_ONLINE);
                if (onlineStarter != null && onlineStarter.getGameScreen() != null) {
                    onlineStarter.getGameScreen().cleanupGameResources();
                }

                // 清理在线游戏控制器
                if (onlineGameController != null) {
                    onlineGameController = null;
                }
                break;
        }

        // 清空UI
        if (root != null) {
            root.getChildren().clear();
        }

        // 强制执行垃圾回收
        System.gc();

        // 确保游戏循环被停止（双重检查）
        if (gameLoop != null) {
            gameLoopService.stopGameLoop(gameLoop);
        }

        // 清除键盘控制
        if (keyboardService != null && gameCanvas != null) {
            keyboardService.clearKeyboardControls(gameCanvas);
        }

        // 重置按键状态
        inputState = new PlayerService.InputState();
    }

    /**
     * 加载游戏存档
     */
    public void loadGame() {
        boolean success = false;

        // 使用游戏状态服务加载游戏
        switch (currentGameMode) {
            case GAME_MODE_SINGLE:
                if (gameStateService instanceof SingleGameStateServiceImpl) {
                    success = ((SingleGameStateServiceImpl) gameStateService).showLoadGameDialog(singleGameController);
                }
                break;

            case GAME_MODE_MULTI:
                // 多人游戏存档加载
                if (gameStateService instanceof MultiGameStateServiceImpl) {
                    // success = ((MultiGameStateServiceImpl)
                    // gameStateService).showLoadGameDialog(multiGameController);
                    showMessage("多人游戏存档加载功能尚未实现");
                    return;
                }
                break;

            case GAME_MODE_ONLINE:
                // 在线游戏存档加载
                // if (gameStateService instanceof OnlineGameStateServiceImpl) {
                // success = ((OnlineGameStateServiceImpl)
                // gameStateService).showLoadGameDialog(onlineGameController);
                // }
                showMessage("在线游戏存档加载功能尚未实现");
                return;

            default:
                showMessage("无法识别的游戏模式");
                return;
        }

        if (success) {
            // 获取当前游戏屏幕
            GameScreen screen = getGameScreen();
            if (screen != null) {
                // 获取从存档加载的时间
                long loadedTime = screen.getTotalGameTime();

                // 重要：确保游戏循环中使用的最后更新时间是基于当前时间的
                long currentTime = System.currentTimeMillis();
                screen.setLastUpdateTime(currentTime);
            }

            // 启动游戏循环前强制更新一次UI
            Platform.runLater(() -> {
                // 更新UI显示
                switch (currentGameMode) {
                    case GAME_MODE_SINGLE:
                        // 更新单人游戏UI
                        updateBulletDisplay();
                        updateTimeDisplay();

                        // 更新生命显示
                        if (screen != null && screen instanceof SinglePlayerGameScreen) {
                            ((SinglePlayerGameScreen) screen).updateLivesDisplay(screen.getPlayerLives());
                        }
                        break;

                    case GAME_MODE_MULTI:
                        // 更新多人游戏UI
                        updateBulletDisplay();
                        updateTimeDisplay();

                        // 更新多人游戏特有的UI元素
                        if (screen != null && screen instanceof MultiPlayerGameScreen) {
                            MultiPlayerGameScreen multiScreen = (MultiPlayerGameScreen) screen;
                            multiScreen.updateLivesDisplay(multiScreen.getPlayer1Lives(),
                                    multiScreen.getPlayer2Lives());
                        }
                        break;

                    case GAME_MODE_ONLINE:
                        // 更新在线游戏UI
                        break;
                }
            });

            // 启动游戏循环
            startGameLoop();
        } else {
            // 加载失败，显示错误信息
            showMessage("游戏存档加载失败！");
        }
    }

    /**
     * 启动游戏循环
     */
    public void startGameLoop() {
        GameController controller = getActiveController();
        if (controller == null)
            return;

        // 获取当前游戏屏幕
        GameScreen screen = getGameScreen();
        if (screen == null)
            return;

        // 创建时间更新回调函数
        GameLoopService.TimeUpdateCallback timeUpdateCallback = (totalGameTime) -> {
            // 使用屏幕的方法更新总时间
            Platform.runLater(() -> {
                screen.setTotalGameTime(totalGameTime);
                updateTimeDisplay(); // 调用现有的更新时间显示方法
            });
        };

        // 创建游戏循环并设置到屏幕
        AnimationTimer loop;

        switch (currentGameMode) {
            case GAME_MODE_SINGLE:
                if (controller instanceof SingleGameController) {
                    loop = gameLoopService.createGameLoop(
                            controller,
                            this::renderGame,
                            timeUpdateCallback);
                    screen.setGameLoop(loop);
                }
                break;
            case GAME_MODE_MULTI:
                if (controller instanceof MultiGameController) {
                    loop = gameLoopService.createGameLoop(
                            controller,
                            this::renderGame,
                            timeUpdateCallback);
                    screen.setGameLoop(loop);
                }
                break;
            case GAME_MODE_ONLINE:
                // 在线模式游戏循环启动逻辑
                break;
        }
    }

    /**
     * 显示主菜单界面
     */
    public void showMainMenu() {
        mainMenuView.show();
    }

    /**
     * 显示坦克选择界面
     */
    public void showTankSelection() {
        // 根据当前或即将进入的游戏模式显示不同的坦克选择界面
        switch (currentGameMode) {
            case GAME_MODE_SINGLE:
            case GAME_MODE_NONE: // 如果未设置模式，默认为单人
                singleTankSelectionView.show();
                break;
            case GAME_MODE_MULTI:
                multiTankSelectionView.show();
                break;
            case GAME_MODE_ONLINE:
                // TODO: 显示联机游戏坦克选择界面
                // onlineTankSelectionView.show();
                showMessage("联机游戏坦克选择界面尚未实现");
                break;
        }
    }

    /**
     * 显示游戏选项界面
     * 
     * @param gameMode 要进入的游戏模式
     */
    public void showGameOptions(int gameMode) {
        // 设置游戏模式
        setGameMode(gameMode);

        // 根据游戏模式显示不同选项界面
        switch (gameMode) {
            case GAME_MODE_SINGLE:
                singlePlayerOptionsView.show();
                break;
            case GAME_MODE_MULTI:
                multiPlayerOptionsView.show();
                break;
            case GAME_MODE_ONLINE:
                // TODO: 显示联机游戏选项界面
                // onlinePlayerOptionsView.show();
                showMessage("联机游戏选项界面尚未实现");
                break;
        }
    }

    /**
     * 获取当前游戏模式的游戏启动器
     * 
     * @return 当前游戏模式的游戏启动器
     */
    public GameStarterController getGameStarter() {
        return gameStarters.get(currentGameMode);
    }

    /**
     * 获取指定游戏模式的游戏启动器
     * 
     * @param gameMode 游戏模式
     * @return 指定游戏模式的游戏启动器
     */
    public GameStarterController getGameStarter(int gameMode) {
        return gameStarters.get(gameMode);
    }

    /**
     * 获取当前游戏模式的游戏屏幕
     * 
     * @return 当前游戏模式的游戏屏幕
     */
    public GameScreen getGameScreen() {
        GameStarterController starter = getGameStarter();
        if (starter != null) {
            return starter.getGameScreen();
        }
        return null;
    }

    /**
     * 开始游戏，显示关卡选择对话框
     * 
     * @param tankTypes 坦克类型参数，根据游戏模式解析：
     *                 - 单人模式：第一个参数为玩家坦克类型
     *                 - 双人模式：前两个参数分别为玩家1和玩家2的坦克类型
     *                 - 在线模式：根据在线模式需求解析
     */
    public void startGame(String... tankTypes) {
        // 根据当前游戏模式处理参数并显示关卡选择对话框
        switch (currentGameMode) {
            case GAME_MODE_SINGLE:
                // 单人游戏只需要第一个坦克类型参数
                if (tankTypes.length >= 1) {
                    this.currentTankType = tankTypes[0];
                    levelSelectionDialog.show(currentTankType);
                } else {
                    showMessage("单人游戏需要指定坦克类型");
                }
                break;
                
            case GAME_MODE_MULTI:
                // 双人游戏需要两个坦克类型参数
                if (tankTypes.length >= 2) {
                    // 检查两个玩家是否选择了相同的坦克
                    if (tankTypes[0].equals(tankTypes[1])) {
                        showMessage("两位玩家不能选择相同的坦克！");
                        return;
                    }
                    
                    // 存储玩家坦克类型
                    this.p1TankType = tankTypes[0];
                    this.p2TankType = tankTypes[1];
                    
                    // 显示关卡选择对话框，使用玩家1的坦克类型作为参考
                    if (levelSelectionDialog == null) {
                        levelSelectionDialog = new LevelSelectionDialog(this, root);
                    }
                    levelSelectionDialog.show(p1TankType);
                } else {
                    showMessage("双人游戏需要指定两种坦克类型");
                }
                break;
                
            case GAME_MODE_ONLINE:
                // 联机游戏可能有不同的关卡选择机制
                showMessage("联机游戏关卡选择尚未实现");
                break;
                
            default:
                // 如果游戏模式未设置，默认使用单人游戏
                if (tankTypes.length >= 1) {
                    setGameMode(GAME_MODE_SINGLE);
                    this.currentTankType = tankTypes[0];
                    levelSelectionDialog.show(currentTankType);
                } else {
                    showMessage("请先选择游戏模式和坦克类型");
                }
                break;
        }
    }

    /**
     * 以指定的坦克类型和关卡开始游戏
     * 
     * @param tankTypes 坦克类型参数数组，根据游戏模式解析：
     *                 - 单人模式：数组第一个元素为玩家坦克类型
     *                 - 双人模式：数组前两个元素分别为玩家1和玩家2的坦克类型
     *                 - 在线模式：根据在线模式需求解析
     * @param level 选择的关卡
     */
    public void startGameWithLevel(String[] tankTypes, int level) {
        GameStarterController starter = getGameStarter();
        if (starter != null) {
            switch (currentGameMode) {
                case GAME_MODE_SINGLE:
                    // 单人游戏模式只需要第一个坦克类型
                    if (tankTypes.length >= 1) {
                        starter.startGame(tankTypes[0], level);
                    } else {
                        showMessage("单人游戏需要指定坦克类型");
                    }
                    break;
                    
                case GAME_MODE_MULTI:
                    // 双人游戏模式需要两种坦克类型
                    if (tankTypes.length >= 2 && starter instanceof MultiPlayerGameStarter) {
                        MultiPlayerGameStarter multiStarter = (MultiPlayerGameStarter) starter;
                        multiStarter.startGame(tankTypes[0], tankTypes[1], level);
                    } else {
                        showMessage("双人游戏需要指定两种坦克类型");
                    }
                    break;
                    
                case GAME_MODE_ONLINE:
                    showMessage("联机游戏启动功能尚未实现");
                    break;
                    
                default:
                    showMessage("无效的游戏模式");
                    break;
            }
        } else {
            showMessage("无法启动游戏：游戏启动器未找到");
        }
    }

    /**
     * 设置键盘控制
     */
    public void setupKeyboardControls() {
        // 根据当前游戏模式设置不同的控制方式
        switch (currentGameMode) {
            case GAME_MODE_SINGLE:
                // 使用键盘服务设置单人游戏控制
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
                break;
            case GAME_MODE_MULTI:
                // 设置双人游戏键盘控制
                // TODO: 实现双人游戏键盘控制
                // multiKeyboardService.setupKeyboardControls(
                // multiGameController, gameCanvas, this::showPauseMenu, this::closePauseMenu);
                break;
            case GAME_MODE_ONLINE:
                // 设置联机游戏键盘控制
                // TODO: 实现联机游戏键盘控制
                // onlineKeyboardService.setupKeyboardControls(
                // onlineGameController, gameCanvas, this::showPauseMenu, this::closePauseMenu);
                break;
        }
    }

    /**
     * 渲染游戏画面
     */
    private void renderGame() {
        // 使用渲染服务渲染游戏
        if (gc != null && gameCanvas != null) {
            switch (currentGameMode) {
                case GAME_MODE_SINGLE:
                    renderService.renderGame(
                            singleGameController,
                            gc,
                            gameCanvas.getWidth(),
                            gameCanvas.getHeight());
                    break;
                case GAME_MODE_MULTI:
                    // 使用双人游戏控制器进行渲染
                    // if (multiGameController != null) {
                    // renderService.renderGame(
                    // multiGameController,
                    // gc,
                    // gameCanvas.getWidth(),
                    // gameCanvas.getHeight()
                    // );
                    // }
                    break;
                case GAME_MODE_ONLINE:
                    // 使用联机游戏控制器进行渲染
                    // if (onlineGameController != null) {
                    // renderService.renderGame(
                    // onlineGameController,
                    // gc,
                    // gameCanvas.getWidth(),
                    // gameCanvas.getHeight()
                    // );
                    // }
                    break;
            }
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
        if (getIsPauseMenuOpen())
            return;

        // 暂停游戏前记录最后更新时间
        long pauseTime = System.currentTimeMillis();

        // 暂停游戏
        setGamePaused(true);
        setIsPauseMenuOpen(true);

        pauseMenuView.show();
    }

    /**
     * 关闭暂停菜单
     */
    public void closePauseMenu() {
        // 恢复游戏
        setGamePaused(false);
        setIsPauseMenuOpen(false);

        // 设置新的最后更新时间，从现在开始计时
        setLastUpdateTime(System.currentTimeMillis());

        pauseMenuView.close();
    }

    /**
     * 获取暂停菜单状态
     * 
     * @return 暂停菜单是否打开
     */
    public boolean getIsPauseMenuOpen() {
        switch (currentGameMode) {
            case GAME_MODE_SINGLE:
                if (singlePlayerGameStarter != null && singlePlayerGameStarter.getGameScreen() != null) {
                    return singlePlayerGameStarter.getGameScreen().isPauseMenuOpen();
                }
                break;
            case GAME_MODE_MULTI:
                // 获取双人游戏暂停菜单状态
                // TODO: 实现双人游戏暂停菜单状态获取
                break;
            case GAME_MODE_ONLINE:
                // 获取联机游戏暂停菜单状态
                // TODO: 实现联机游戏暂停菜单状态获取
                break;
        }
        return false;
    }

    /**
     * 重新开始当前关卡
     */
    public void restartGame() {
        Platform.runLater(() -> {
            // 关闭暂停菜单
            closePauseMenu();
            
            // 获取当前关卡和坦克信息
            int currentLevel = 1;
            String[] tankTypes = null;
            
            switch (currentGameMode) {
                case GAME_MODE_SINGLE:
                    if (singleGameController != null) {
                        currentLevel = singleGameController.getCurrentLevel();
                        if (singleGameController.getPlayerTank() != null) {
                            tankTypes = new String[]{singleGameController.getPlayerTank().getTypeString()};
                        } else {
                            tankTypes = new String[]{currentTankType}; // 使用默认或上次选择的坦克类型
                        }
                    } else {
                        tankTypes = new String[]{currentTankType};
                    }
                    break;
                    
                case GAME_MODE_MULTI:
                    if (multiGameController != null) {
                        currentLevel = multiGameController.getCurrentLevel();
                        tankTypes = new String[]{p1TankType, p2TankType};
                    } else {
                        tankTypes = new String[]{p1TankType, p2TankType};
                    }
                    break;
                    
                case GAME_MODE_ONLINE:
                    // 处理在线游戏模式的重启逻辑
                    return; // 暂时返回，因为尚未实现
            }
            
            // 为防止空引用，设置默认值
            if (tankTypes == null || tankTypes.length == 0) {
                tankTypes = new String[]{"standard"};
            }
            
            // 强制清理所有游戏资源
            cleanupGameResources();
            
            // 确保游戏设置为非暂停状态
            setGamePaused(false);
            
            // 记录最终要使用的参数
            final int finalLevel = currentLevel;
            final String[] finalTankTypes = tankTypes;
            
            // 短暂延迟后启动游戏，确保清理完成
            javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(javafx.util.Duration.millis(100));
            delay.setOnFinished(event -> {
                // 重新启动游戏
                startGameWithLevel(finalTankTypes, finalLevel);
            });
            delay.play();
        });
    }

    /**
     * 保存游戏进度
     */
    void saveGame() {
        // 获取当前活动的控制器
        GameController controller = getActiveController();
        if (controller == null)
            return;

        // 请求用户输入存档名称
        TextInputDialog dialog = new TextInputDialog("存档" + (System.currentTimeMillis() / 1000));
        dialog.setTitle("保存游戏");
        dialog.setHeaderText("请输入存档名称");
        dialog.setContentText("名称:");

        dialog.showAndWait().ifPresent(saveName -> {
            // 使用游戏状态服务保存游戏
            boolean success = gameStateService.saveGame(controller, saveName);

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
            setIsPauseMenuOpen(false);
            // 显示设置对话框
            settingsDialog.show();
        });
    }

    /**
     * 处理玩家输入
     * 
     * @param playerTank 玩家坦克对象
     */
    public void handlePlayerInput(Tank playerTank) {
        if (playerTank == null)
            return;

        switch (currentGameMode) {
            case GAME_MODE_SINGLE:
                if (singleGameController == null)
                    return;

                // 使用PlayerService处理输入
                int newBulletCount = playerService.handlePlayerInput(singleGameController, inputState,
                        getBulletCount());

                // 如果子弹数量变化，更新显示
                if (newBulletCount != getBulletCount()) {
                    setBulletCount(newBulletCount);
                }
                break;
            case GAME_MODE_MULTI:
                if (multiGameController == null)
                    return;

                // 多人模式的输入处理逻辑
                break;
            case GAME_MODE_ONLINE:
                if (onlineGameController == null)
                    return;

                // 在线模式的输入处理逻辑
                break;
        }
    }

    /**
     * 设置玩家生命值
     * 
     * @param lives 生命值
     */
    public void setPlayerLives(int lives) {
        switch (currentGameMode) {
            case GAME_MODE_SINGLE:
                if (singlePlayerGameStarter != null && singlePlayerGameStarter.getGameScreen() != null) {
                    singlePlayerGameStarter.getGameScreen().setPlayerLives(lives);
                }
                break;
            case GAME_MODE_MULTI:
                // 处理双人游戏模式
                // TODO: 实现双人游戏生命值设置
                break;
            case GAME_MODE_ONLINE:
                // 处理联机游戏模式
                // TODO: 实现联机游戏生命值设置
                break;
        }
    }

    /**
     * 设置子弹数量
     * 
     * @param count 子弹数量
     */
    public void setBulletCount(int count) {
        switch (currentGameMode) {
            case GAME_MODE_SINGLE:
                if (singlePlayerGameStarter != null && singlePlayerGameStarter.getGameScreen() != null) {
                    singlePlayerGameStarter.getGameScreen().setBulletCount(count);
                }
                break;
            case GAME_MODE_MULTI:
                // 处理双人游戏模式
                // TODO: 实现双人游戏子弹数量设置
                break;
            case GAME_MODE_ONLINE:
                // 处理联机游戏模式
                // TODO: 实现联机游戏子弹数量设置
                break;
        }
    }

    /**
     * 更新增益效果UI显示
     */
    public void updatePowerUpUIDisplay() {
        GameStarterController gameStarter = getGameStarter(currentGameMode);
        if (gameStarter == null)
            return;

        switch (currentGameMode) {
            case GAME_MODE_SINGLE:
                if (singleGameController == null || singleGameController.getPlayerTank() == null)
                    return;

                if (gameStarter.getGameScreen() instanceof SinglePlayerGameScreen) {
                    ((SinglePlayerGameScreen) gameStarter.getGameScreen())
                            .updatePowerUpUIDisplay(singleGameController, effectService);
                }
                break;
            case GAME_MODE_MULTI:
                if (multiGameController == null)
                    return;

                if (gameStarter.getGameScreen() instanceof MultiPlayerGameScreen) {
                    // 多人模式的增益效果更新逻辑
                    // ((MultiPlayerGameScreen)gameStarter.getGameScreen())
                    // .updatePowerUpUIDisplay(multiGameController, effectService);
                }
                break;
            case GAME_MODE_ONLINE:
                if (onlineGameController == null)
                    return;

                // 在线模式的增益效果更新逻辑
                break;
        }
    }

    /**
     * 重置游戏开始时间，保持总游戏时间不变
     */
    public void resetGameStartTime() {
        switch (currentGameMode) {
            case GAME_MODE_SINGLE:
                if (gameLoopService instanceof SingleGameLoopServiceImpl) {
                    ((SingleGameLoopServiceImpl) gameLoopService).resetGameStartTime();
                }
                break;
            case GAME_MODE_MULTI:
                // 多人模式的游戏时间重置逻辑
                if (gameLoopService instanceof MultiGameLoopServiceImpl) {
                    // ((MultiGameLoopServiceImpl) gameLoopService).resetGameStartTime();
                }
                break;
            case GAME_MODE_ONLINE:
                // 在线模式的游戏时间重置逻辑
                break;
        }
    }

    /**
     * 恢复游戏循环
     */
    public void resumeGameLoop() {
        gameLoopService.resumeGameLoop(getGameLoop());
        setGamePaused(false);
        setIsPauseMenuOpen(false);
    }

    /**
     * 停止游戏循环
     */
    public void stopGameLoop() {
        AnimationTimer gameLoop = getGameLoop();
        if (gameLoop != null) {
            gameLoopService.stopGameLoop(gameLoop);
        }
    }

    /**
     * 更新玩家血量显示
     */
    public void updateHealthDisplay() {
        GameStarterController gameStarter = getGameStarter(currentGameMode);
        if (gameStarter == null)
            return;

        switch (currentGameMode) {
            case GAME_MODE_SINGLE:
                if (singleGameController == null)
                    return;

                if (gameStarter.getGameScreen() instanceof SinglePlayerGameScreen) {
                    ((SinglePlayerGameScreen) gameStarter.getGameScreen())
                            .updateHealthDisplay(singleGameController);
                }
                break;
            case GAME_MODE_MULTI:
                if (multiGameController == null)
                    return;

                // 多人模式健康显示更新逻辑
                break;
            case GAME_MODE_ONLINE:
                // 在线模式健康显示更新逻辑
                break;
        }
    }

    /**
     * 更新子弹数量显示
     */
    public void updateBulletDisplay() {
        GameStarterController gameStarter = getGameStarter(currentGameMode);
        if (gameStarter == null)
            return;

        switch (currentGameMode) {
            case GAME_MODE_SINGLE:
                if (gameStarter.getGameScreen() instanceof SinglePlayerGameScreen) {
                    ((SinglePlayerGameScreen) gameStarter.getGameScreen())
                            .updateBulletDisplay(getBulletCount());
                }
                break;
            case GAME_MODE_MULTI:
                // 多人模式子弹显示更新逻辑
                break;
            case GAME_MODE_ONLINE:
                // 在线模式子弹显示更新逻辑
                break;
        }
    }

    /**
     * 获取游戏循环
     */
    private AnimationTimer getGameLoop() {
        GameScreen screen = getGameScreen();
        if (screen != null) {
            return screen.getGameLoop();
        }
        return null;
    }

    /**
     * 设置游戏暂停状态
     * 
     * @param paused 是否暂停
     */
    public void setGamePaused(boolean paused) {
        GameScreen screen = getGameScreen();
        if (screen != null) {
            screen.setGamePaused(paused);
        }
    }

    /**
     * 设置暂停菜单打开状态
     * 
     * @param open 是否打开
     */
    public void setIsPauseMenuOpen(boolean open) {
        GameScreen screen = getGameScreen();
        if (screen != null) {
            screen.setIsPauseMenuOpen(open);
        }
    }

    /**
     * 获取游戏总时间
     * 
     * @return 游戏总时间(毫秒)
     */
    public long getTotalGameTime() {
        GameScreen screen = getGameScreen();
        if (screen != null) {
            return screen.getTotalGameTime();
        }
        return 0;
    }

    /**
     * 设置游戏总时间
     * 
     * @param time 游戏总时间(毫秒)
     */
    public void setTotalGameTime(long time) {
        GameScreen screen = getGameScreen();
        if (screen != null) {
            screen.setTotalGameTime(time);
        }
    }

    /**
     * 获取上次子弹补充时间
     * 
     * @return 上次子弹补充时间(毫秒)
     */
    public long getLastBulletRefillTime() {
        GameScreen screen = getGameScreen();
        if (screen != null) {
            return screen.getLastBulletRefillTime();
        }
        return 0;
    }

    /**
     * 设置上次子弹补充时间
     * 
     * @param time 时间戳
     */
    public void setLastBulletRefillTime(long time) {
        GameScreen screen = getGameScreen();
        if (screen != null) {
            screen.setLastBulletRefillTime(time);
        }
    }

    /**
     * 获取玩家生命值
     * 
     * @return 玩家生命值
     */
    public int getPlayerLives() {
        GameScreen screen = getGameScreen();
        if (screen != null) {
            return screen.getPlayerLives();
        }
        return 3; // 默认生命值
    }

    /**
     * 获取子弹数量
     * 
     * @return 子弹数量
     */
    public int getBulletCount() {
        GameScreen screen = getGameScreen();
        if (screen != null) {
            return screen.getBulletCount();
        }
        return 10; // 默认子弹数
    }

    /**
     * 获取当前游戏得分
     * 
     * @return 当前得分
     */
    public int getScore() {
        // 使用游戏状态服务计算得分
        GameController controller = getActiveController();
        if (controller != null) {
            return gameStateService.getScore(controller);
        }
        return 0;
    }

    /**
     * 设置游戏画布
     * 
     * @param gameCanvas 游戏画布
     */
    public void setGameCanvas(Canvas gameCanvas) {
        this.gameCanvas = gameCanvas;
    }

    /**
     * 设置图形上下文
     * 
     * @param gc 图形上下文
     */
    public void setGraphicsContext(GraphicsContext gc) {
        this.gc = gc;
    }

    /**
     * 设置当前游戏模式
     * 
     * @param mode 游戏模式
     */
    public void setGameMode(int mode) {
        this.currentGameMode = mode;
    }

    /**
     * 获取当前游戏模式
     * 
     * @return 游戏模式
     */
    public int getCurrentGameMode() {
        return currentGameMode;
    }

    /**
     * 设置游戏控制器
     * 
     * @param controller 游戏控制器
     */
    public void setGameController(GameController controller) {
        this.activeController = controller;

        // 使用 switch 结构确定控制器类型
        int controllerType = GAME_MODE_NONE;

        if (controller instanceof SingleGameController) {
            controllerType = GAME_MODE_SINGLE;
        } else if (controller instanceof MultiGameController) {
            controllerType = GAME_MODE_MULTI;
        } else if (controller instanceof OnlineGameController) {
            controllerType = GAME_MODE_ONLINE;
        }

        // 根据控制器类型设置相应的引用和游戏模式
        switch (controllerType) {
            case GAME_MODE_SINGLE:
                this.singleGameController = (SingleGameController) controller;
                setGameMode(GAME_MODE_SINGLE);
                break;
            case GAME_MODE_MULTI:
                this.multiGameController = (MultiGameController) controller;
                setGameMode(GAME_MODE_MULTI);
                break;
            case GAME_MODE_ONLINE:
                this.onlineGameController = (OnlineGameController) controller;
                setGameMode(GAME_MODE_ONLINE);
                break;
            default:
                // 未知控制器类型不执行任何操作
                break;
        }
    }

    /**
     * 获取当前活动的游戏控制器
     * 
     * @return 当前活动的游戏控制器
     */
    public GameController getActiveController() {
        return activeController;
    }

    /**
     * 设置最后更新时间
     * 
     * @param time 时间
     */
    public void setLastUpdateTime(long time) {
        withGameScreen(screen -> screen.setLastUpdateTime(time));
    }

    /**
     * 设置时间信息文本
     * 
     * @param timeText 时间文本组件
     */
    public void setTimeInfo(Text timeText) {
        withGameScreen(screen -> screen.setTimeInfo(timeText));
    }

    /**
     * 设置游戏数据面板
     * 
     * @param panel 数据面板
     */
    public void setGameDataPanel(HBox panel) {
        withGameScreen(screen -> screen.setGameDataPanel(panel));
    }

    /**
     * 设置能力增强效果进度条映射
     * 
     * @param progressBars 进度条映射
     */
    public void setPowerUpProgressBars(Map<String, ProgressBar> progressBars) {
        this.powerUpProgressBars = progressBars;
    }

    /**
     * 通用方法：获取当前活动游戏屏幕
     * 
     * @return 当前游戏屏幕
     */
    private GameScreen getActiveGameScreen() {
        switch (currentGameMode) {
            case GAME_MODE_SINGLE:
                return singlePlayerGameStarter != null ? singlePlayerGameStarter.getGameScreen() : null;
            case GAME_MODE_MULTI:
                // TODO: 实现双人游戏屏幕获取
                return null;
            case GAME_MODE_ONLINE:
                // TODO: 实现联机游戏屏幕获取
                return null;
            default:
                return null;
        }
    }

    /**
     * 统一的游戏屏幕方法调用
     * 
     * @param action 要执行的操作
     */
    private void withGameScreen(Consumer<GameScreen> action) {
        GameScreen screen = getActiveGameScreen();
        if (screen != null) {
            action.accept(screen);
        }
    }

    /**
     * 获取游戏画布
     * 
     * @return 游戏画布
     */
    public Canvas getGameCanvas() {
        return gameCanvas;
    }

    /**
     * 获取主题色
     * 
     * @return 主题颜色
     */
    public Color getPrimaryColor() {
        return PRIMARY_COLOR;
    }

    /**
     * 获取文本颜色
     * 
     * @return 文本颜色
     */
    public Color getTextColor() {
        return TEXT_COLOR;
    }

    /**
     * 获取游戏状态服务
     * 
     * @return 游戏状态服务
     */
    public GameStateService getGameStateService() {
        return gameStateService;
    }

    /**
     * 获取玩家服务
     * 
     * @return 玩家服务
     */
    public PlayerService getPlayerService() {
        return playerService;
    }

    /**
     * 获取效果服务
     * 
     * @return 效果服务
     */
    public EffectService getEffectService() {
        return effectService;
    }

    /**
     * 获取能力增强效果进度条映射
     * 
     * @return 进度条映射
     */
    public Map<String, ProgressBar> getPowerUpProgressBars() {
        return powerUpProgressBars;
    }

    /**
     * 获取根布局容器
     * 
     * @return 根StackPane
     */
    public StackPane getRoot() {
        return root;
    }

    /**
     * 获取当前游戏模式对应的关卡完成视图
     * 
     * @return 关卡完成视图
     */
    public LevelCompletedView getLevelCompletedView() {
        switch (getCurrentGameMode()) {
            case GAME_MODE_SINGLE:
                return singleLevelCompletedView;
            case GAME_MODE_MULTI:
                return multiLevelCompletedView;
            case GAME_MODE_ONLINE:
                // 联机模式目前尚未实现
                // return onlineLevelCompletedView;
                return null;
            default:
                return null;
        }
    }

    /**
     * 获取当前游戏模式对应的游戏结束屏幕
     * 
     * @return 游戏结束屏幕
     */
    public GameOverScreen getGameOverScreen() {
        switch (getCurrentGameMode()) {
            case GAME_MODE_SINGLE:
                return singleGameOverScreen;
            case GAME_MODE_MULTI:
                return multiGameOverScreen;
            case GAME_MODE_ONLINE:
                // 联机模式目前尚未实现
                // return onlineGameOverScreen;
                return null;
            default:
                return null;
        }
    }

    /**
     * 更新游戏时间显示
     */
    public void updateTimeDisplay() {
        GameScreen screen = getGameScreen();
        if (screen != null) {
            try {
                long totalTime = screen.getTotalGameTime();
                long seconds = totalTime / 1000;
                long minutes = seconds / 60;
                seconds = seconds % 60;

                long finalSeconds = seconds;
                Platform.runLater(() -> {
                    try {
                        // 尝试获取时间文本组件并更新
                        java.lang.reflect.Method getTimeInfoMethod = screen.getClass().getMethod("getTimeInfo");
                        javafx.scene.text.Text timeInfo = (javafx.scene.text.Text) getTimeInfoMethod.invoke(screen);
                        if (timeInfo != null) {
                            timeInfo.setText(String.format("%02d:%02d", minutes, finalSeconds));
                        }
                    } catch (Exception e) {
                        // 如果方法不存在，打印错误但不中断游戏
                        System.err.println("无法更新时间显示: " + e.getMessage());
                    }
                });
            } catch (Exception e) {
                System.err.println("更新游戏时间时发生错误: " + e.getMessage());
            }
        }
    }

}
