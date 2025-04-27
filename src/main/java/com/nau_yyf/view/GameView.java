package com.nau_yyf.view;

import com.jfoenix.controls.*;
import com.nau_yyf.controller.GameController;
import com.nau_yyf.model.Bullet;
import com.nau_yyf.model.LevelMap;
import com.nau_yyf.model.PowerUp;
import com.nau_yyf.model.Tank;
import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.util.*;

public class GameView {
    private Stage stage;
    private Scene scene;
    private StackPane root;
    private GameController gameController;

    // 游戏标题和背景
    private final String GAME_TITLE = "TANK 2025";
    private final Color PRIMARY_COLOR = Color.rgb(37, 160, 218); // 蓝色
    private final Color SECONDARY_COLOR = Color.rgb(76, 175, 80); // 绿色
    private final Color TEXT_COLOR = Color.WHITE;
    private final Color BACKGROUND_COLOR = Color.rgb(27, 40, 56); // 深蓝灰色

    // 游戏Logo
    private ImageView logoImageView;

    private Canvas gameCanvas;
    private GraphicsContext gc;

    // 游戏计时相关变量
    private long gameStartTime; // 游戏开始时间
    private long totalGameTime; // 游戏总时间（毫秒）
    private long lastUpdateTime; // 上次更新时间
    private boolean gamePaused = false; // 游戏是否暂停
    private boolean isPauseMenuOpen = false; // 暂停菜单是否打开
    private Text timeInfo; // 时间显示文本

    // 键盘控制相关变量
    private boolean up = false;
    private boolean down = false;
    private boolean left = false;
    private boolean right = false;
    private boolean shooting = false;

    // 新增子弹数量和子弹补充时间
    private int bulletCount = 10; // 初始子弹数量
    private long lastBulletRefillTime = 0; // 上次子弹补充时间

    // 添加成员变量
    private HBox gameDataPanel;

    // 添加一个成员变量来跟踪游戏循环
    private AnimationTimer gameLoop;

    // 添加生命数相关变量
    private int playerLives = 3; // 初始生命数为3
    private Map<String, ImageView> powerUpIndicators = new HashMap<>(); // 存储增益效果指示器
    private Map<String, ProgressBar> powerUpProgressBars = new HashMap<>(); // 存储增益效果进度条

    // 添加新的成员变量
    private Map<String, Label> powerUpLabels;

    // 在GameView类中添加成员变量
    private Map<String, HBox> effectBoxMap = new HashMap<>(); // 存储每种效果的容器

    // 添加以下成员变量，用于存储坦克选择界面的UI元素
    private BorderPane tankSelectionLayout;
    private List<VBox> tankOptionContainers = new ArrayList<>();
    private List<ImageView> tankImages = new ArrayList<>();
    private List<JFXButton> tankSelectButtons = new ArrayList<>();

    // 添加MainMenuView组件引用
    private MainMenuView mainMenuView;
    private SinglePlayerOptionsView singlePlayerOptionsView;
    private TankSelectionView tankSelectionView;
    private LevelSelectionDialog levelSelectionDialog;
    private SinglePlayerGameStarter singlePlayerGameStarter;

    // 添加一个字段记录当前使用的坦克类型
    private String currentTankType = "standard"; // 默认使用标准坦克

    // 添加PauseMenuView成员变量
    private PauseMenuView pauseMenuView;

    // 添加LevelCompletedView成员变量
    private LevelCompletedView levelCompletedView;

    // 添加GameOverScreen成员变量
    private GameOverScreen gameOverScreen;

    // 添加SettingsDialog成员变量
    private SettingsDialog settingsDialog;

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

        // 初始化根布局 - 将这部分从Platform.runLater中移出
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

        // 初始化视图组件 - 在Platform.runLater之外初始化
        this.mainMenuView = new MainMenuView(this, root, stage);
        this.singlePlayerOptionsView = new SinglePlayerOptionsView(this, root, stage);
        this.tankSelectionView = new TankSelectionView(this, root, stage);
        this.levelSelectionDialog = new LevelSelectionDialog(this, root);
        this.singlePlayerGameStarter = new SinglePlayerGameStarter(this);

        // 在GameView构造函数初始化部分中添加：
        this.pauseMenuView = new PauseMenuView(this, root, scene);
        this.levelCompletedView = new LevelCompletedView(this, root, scene);
        this.gameOverScreen = new GameOverScreen(this, root, scene);
        this.settingsDialog = new SettingsDialog(this, root);

        // 使用Platform.runLater处理后续的UI更新
        Platform.runLater(() -> {
            // 显示主菜单
            showMainMenu();
        });
    }

    /**
     * 显示主菜单
     */
    public void showMainMenu() {
        mainMenuView.show();
    }

    /**
     * 显示单人游戏选项
     */
    public void showSinglePlayerOptions() {
        singlePlayerOptionsView.show();
    }

    /**
     * 显示坦克选择界面
     */
    public void showTankSelection() {
        tankSelectionView.show();
    }

    /**
     * 开始游戏
     */
    public void startGame(String selectedTankType) {
        this.currentTankType = selectedTankType; // 保存当前使用的坦克类型
        levelSelectionDialog.show(selectedTankType);
    }

    /**
     * 以指定的坦克类型和关卡开始游戏
     */
    public void startGameWithLevel(String selectedTankType, int level) {
        // 使用SinglePlayerGameStarter启动游戏
        singlePlayerGameStarter.startGame(selectedTankType, level);
    }

    // 键盘控制设置方法
    void setupKeyboardControls() {

        // 重置按键状态
        up = down = left = right = shooting = false;

        // 1. 清除画布上的监听器
        if (gameCanvas != null) {
            gameCanvas.setOnKeyPressed(null);
            gameCanvas.setOnKeyReleased(null);

            // 移除所有键盘事件处理器
            gameCanvas.removeEventHandler(javafx.scene.input.KeyEvent.KEY_PRESSED, event -> {
            });
            gameCanvas.removeEventHandler(javafx.scene.input.KeyEvent.KEY_RELEASED, event -> {
            });
        }

        // 2. 清除场景上存在的监听器
        if (scene != null) {
            scene.setOnKeyPressed(null);
            scene.setOnKeyReleased(null);
        }

        // 3. 清除根布局上存在的监听器
        if (root != null) {
            root.setOnKeyPressed(null);
            root.setOnKeyReleased(null);
        }

        // 仅在画布级别添加新的监听器
        gameCanvas.setOnKeyPressed(e -> {
            String code = e.getCode().toString();

            // 如果暂停菜单已打开
            if (isPauseMenuOpen) {
                if (code.equals("ESCAPE")) {
                    closePauseMenu();
                }
                return;
            }

            // 处理游戏按键
            if (!gamePaused) {
                if (code.equals("UP") || code.equals("W")) {
                    up = true;
                }
                if (code.equals("DOWN") || code.equals("S")) {
                    down = true;
                }
                if (code.equals("LEFT") || code.equals("A")) {
                    left = true;
                }
                if (code.equals("RIGHT") || code.equals("D")) {
                    right = true;
                }
                if (code.equals("SPACE")) {
                    shooting = true;
                }
                if (code.equals("ESCAPE")) {
                    showPauseMenu();
                }
                // 添加E键放置炸弹
                if (code.equals("E")) {
                    if (gameController != null) {
                        gameController.placeBomb();
                    }
                }
            }
            e.consume(); // 阻止事件继续传播
        });

        gameCanvas.setOnKeyReleased(e -> {
            String code = e.getCode().toString();

            if (code.equals("UP") || code.equals("W")) {
                up = false;
            }
            if (code.equals("DOWN") || code.equals("S")) {
                down = false;
            }
            if (code.equals("LEFT") || code.equals("A")) {
                left = false;
            }
            if (code.equals("RIGHT") || code.equals("D")) {
                right = false;
            }
            if (code.equals("SPACE")) {
                shooting = false;
            }
            e.consume(); // 阻止事件继续传播
        });
    }

    // 添加时间显示更新方法
    private void updateTimeDisplay(long totalTimeMillis) {
        long seconds = totalTimeMillis / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;

        timeInfo.setText(String.format("%02d:%02d", minutes, seconds));
    }

    // 游戏循环
    void startGameLoop() {
        // 先停止旧的游戏循环
        if (gameLoop != null) {
            gameLoop.stop();
        }

        // 重置时间相关变量
        gameStartTime = System.currentTimeMillis();
        lastUpdateTime = gameStartTime;

        gameLoop = new AnimationTimer() {
            private long lastFrameTime = 0;
            private long focusCheckTime = 0;
            private final long FRAME_TIME = 16_666_667; // 约60FPS (纳秒)
            private double accumulator = 0;
            private final double TIME_STEP = 1.0 / 60.0; // 固定逻辑更新步长

            @Override
            public void handle(long now) {
                // 检查gameController是否为null
                if (gameController == null) {
                    return; // 如果控制器为null，不执行更新
                }

                if (lastFrameTime == 0) {
                    lastFrameTime = now;
                    return;
                }

                // 计算自上一帧以来经过的时间(秒)
                double deltaTime = (now - lastFrameTime) / 1_000_000_000.0;
                lastFrameTime = now;

                // 防止过大的时间步长(例如，应用暂停后恢复)
                if (deltaTime > 0.25)
                    deltaTime = 0.25;

                // 积累时间用于固定时间步长
                accumulator += deltaTime;

                // 执行固定时间步长的逻辑更新
                while (accumulator >= TIME_STEP) {
                    updateGame(TIME_STEP);
                    accumulator -= TIME_STEP;
                }

                // 渲染画面 - 渲染是每帧执行，与逻辑更新分离
                renderGame();

                // 更新游戏时间显示和焦点检查(与原始代码保持一致)
                long currentTime = System.currentTimeMillis();
                totalGameTime += currentTime - lastUpdateTime;
                updateTimeDisplay(totalGameTime);

                if (now - focusCheckTime > 3_000_000_000L) {
                    focusCheckTime = now;
                    if (!gameCanvas.isFocused()) {
                        gameCanvas.requestFocus();
                    }
                }

                lastUpdateTime = currentTime;

                // 添加对玩家坦克状态的检查
                gameController.updatePlayerTank();
            }
        };
        gameLoop.start();
    }

    /**
     * 固定时间步长逻辑更新
     */
    private void updateGame(double deltaTime) {
        // 获取玩家坦克
        Tank playerTank = gameController.getPlayerTank();

        // 检查玩家坦克是否已死亡但未触发死亡处理
        if (playerTank != null && playerTank.isDead()) {
            // 确保调用玩家死亡处理
            handlePlayerDestroyed();
            return; // 玩家已死亡，不再进行其他更新
        }

        // 记录更新前的玩家血量
        int oldHealth = 0;
        if (gameController.getPlayerTank() != null) {
            oldHealth = gameController.getPlayerTank().getHealth();
        }

        // 处理玩家输入
        handlePlayerInput();

        // 更新敌方坦克
        gameController.updateEnemyTanks();

        // 更新子弹
        gameController.updateBullets(deltaTime);

        // 检查碰撞
        boolean playerLostLife = gameController.updateBulletsAndCheckCollisions();
        if (playerLostLife) {
            handlePlayerDestroyed();
        }

        // 检查玩家血量是否改变，如果改变则更新显示
        if (gameController.getPlayerTank() != null &&
                gameController.getPlayerTank().getHealth() != oldHealth) {
            updateHealthDisplay();
            updateLivesDisplay(); // 确保生命值也更新
        }

        // 检查敌方坦克与玩家坦克碰撞
        if (gameController.checkEnemyPlayerCollisions()) {
            // 更新生命显示
            updateHealthDisplay();
            updateLivesDisplay();
        }

        // 刷新子弹补给
        updateBulletRefill(deltaTime);

        // 更新敌人显示 - 每帧更新确保数量显示正确
        updateEnemiesDisplay();

        // 更新增益效果
        gameController.updatePowerUps(deltaTime);

        // 更新坦克的增益效果持续时间
        if (gameController.getPlayerTank() != null) {
            gameController.getPlayerTank().updateEffects(deltaTime);
        }

        // 更新增益效果显示
        updatePowerUpDisplay();

        // 更新闪烁效果
        for (PowerUp powerUp : gameController.getPowerUps()) {
            if (powerUp.shouldBlink()) {
                if (Math.random() < 0.1) { // 每帧10%概率切换闪烁状态
                    powerUp.toggleBlinking();
                }
            }
        }

        // 更新UI显示
        updatePowerUpUIDisplay();
    }

    /**
     * 根据坦克类型恢复子弹
     */
    private void updateBulletRefill(double deltaTime) {
        long currentTime = System.currentTimeMillis();
        Tank playerTank = gameController.getPlayerTank();

        if (playerTank != null && bulletCount < 10) {
            // 根据坦克类型设置不同的子弹恢复速率（单位：毫秒）
            int refillDelay;
            Tank.TankType tankType = playerTank.getType();

            switch (tankType) {
                case LIGHT:
                    refillDelay = 1000; // 轻型坦克恢复最快：1.5秒
                    break;
                case HEAVY:
                    refillDelay = 1800; // 重型坦克恢复最慢：3秒
                    break;
                case STANDARD:
                default:
                    refillDelay = 1500; // 标准坦克：2秒
                    break;
            }

            // 如果有攻击力增强效果，恢复速度提高20%
            if (playerTank.isEffectActive(Tank.PowerUpType.ATTACK)) {
                refillDelay = (int) (refillDelay * 0.8);
            }

            // 检查是否到达恢复时间
            if (currentTime - lastBulletRefillTime > refillDelay) {
                bulletCount++;
                lastBulletRefillTime = currentTime;
                updateBulletDisplay();
            }
        } else if (bulletCount >= 10) {
            // 子弹已满，重置计时器
            lastBulletRefillTime = currentTime;
        }
    }

    // 渲染方法
    private void renderGame() {
        // 添加空检查以防止崩溃
        if (gameController == null) {
            return; // 如果gameController为null，直接返回不渲染
        }

        // 清屏
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());

        // 渲染地图元素
        LevelMap map = gameController.getMap();
        for (LevelMap.MapElement element : map.getElements()) {
            // 根据元素类型获取对应图片并渲染
            Image elementImage = gameController.getElementImage(element.getType());
            gc.drawImage(elementImage, element.getX(), element.getY(),
                    element.getWidth(), element.getHeight());
        }

        // 渲染其他游戏对象
        if (gameController != null) {
            gameController.renderMap(gc);
        }

        // 渲染玩家坦克
        Tank playerTank = gameController.getPlayerTank();
        if (playerTank != null && !playerTank.isDead()) {
            boolean shouldRender = true;

            // 检查是否处于复活无敌状态，并且需要闪烁
            if (playerTank.isRespawnInvincible()) {
                shouldRender = playerTank.isVisibleDuringRespawnInvincible();
            }

            if (shouldRender) {
                String imageKey = "player_" + playerTank.getType().name().toLowerCase();
                Image[] tankImgs = gameController.getTankImages().get(imageKey);

                if (tankImgs != null && playerTank.getDirection().ordinal() < tankImgs.length) {
                    gc.drawImage(tankImgs[playerTank.getDirection().ordinal()],
                            playerTank.getX(), playerTank.getY(), 40, 40);

                    // 如果处于无敌状态，添加视觉效果
                    if (playerTank.isRespawnInvincible() || playerTank.isInvincible()) {
                        gc.setGlobalAlpha(0.3);
                        gc.setFill(Color.YELLOW);
                        gc.fillOval(playerTank.getX() - 5, playerTank.getY() - 5, 50, 50);
                        gc.setGlobalAlpha(1.0);
                    } else if (playerTank.isShielded()) {
                        // 如果有护盾，绘制蓝色保护罩
                        gc.setGlobalAlpha(0.3);
                        gc.setFill(Color.BLUE);
                        gc.fillOval(playerTank.getX() - 5, playerTank.getY() - 5, 50, 50);
                        gc.setGlobalAlpha(1.0);
                    }
                }
            }
        }
    }

    /**
     * 处理玩家输入
     */
    private void handlePlayerInput() {
        // 获取玩家坦克
        Tank playerTank = gameController.getPlayerTank();

        // 如果坦克不存在或已经死亡，不处理任何输入
        if (playerTank == null || playerTank.isDead()) {
            return;
        }

        if (gameController == null)
            return;

        boolean anyKeyPressed = false;

        // 根据按键状态设置方向
        if (up) {
            playerTank.setDirection(Tank.Direction.UP);
            anyKeyPressed = true;
        } else if (down) {
            playerTank.setDirection(Tank.Direction.DOWN);
            anyKeyPressed = true;
        } else if (left) {
            playerTank.setDirection(Tank.Direction.LEFT);
            anyKeyPressed = true;
        } else if (right) {
            playerTank.setDirection(Tank.Direction.RIGHT);
            anyKeyPressed = true;
        }

        // 设置是否加速
        playerTank.setAccelerating(anyKeyPressed);

        // 执行移动逻辑（无论是否按键都要调用，以处理减速）
        playerTank.move(gameController);

        // 水池伤害处理
        if (playerTank.isInWaterLastFrame()) {
            updateHealthDisplay();
        }

        // 处理射击
        if (shooting && bulletCount > 0 && playerTank.canFire()) {
            Bullet bullet = playerTank.fire();
            if (bullet != null) {
                bulletCount--;
                updateBulletDisplay();
                gameController.addBullet(bullet);
            }
        }
    }

    /**
     * 更新血量显示方法
     */
    public void updateHealthDisplay() {
        if (gameController == null || gameDataPanel == null)
            return;

        // 找到血量信息框
        VBox healthInfo = (VBox) gameDataPanel.lookup("#healthInfo");
        if (healthInfo != null && healthInfo.getChildren().size() > 1) {
            // 找到值文本
            Text healthValue = (Text) healthInfo.getChildren().get(1);
            if (healthValue != null && gameController.getPlayerTank() != null) {
                // 更新血量值
                int currentHealth = gameController.getPlayerTank().getHealth();
                healthValue.setText(Integer.toString(currentHealth));

                // 根据血量设置颜色
                if (currentHealth <= 1) {
                    healthValue.setFill(Color.RED);
                } else if (currentHealth <= 2) {
                    healthValue.setFill(Color.ORANGE);
                } else {
                    healthValue.setFill(TEXT_COLOR);
                }
            }
        }
    }

    /**
     * 显示游戏说明
     */
    public void showInstructions() {
        InstructionsView instructionsView = new InstructionsView(root);
        instructionsView.show();
    }

    /**
     * 显示消息
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

        // 停止游戏循环
        if (gameLoop != null) {
            gameLoop.stop();
        }

        pauseMenuView.show();
    }

    /**
     * 关闭暂停菜单
     */
    public void closePauseMenu() {
        pauseMenuView.close();
    }

    // 用于PauseMenuView访问
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

            // 获取当前关卡和坦克类型
            int currentLevel = gameController.getCurrentLevel();
            String tankType = gameController.getPlayerTank().getTypeString();

            // 彻底清除所有事件监听器
            if (gameCanvas != null) {
                gameCanvas.setOnKeyPressed(null);
                gameCanvas.setOnKeyReleased(null);
            }

            // 完全清空游戏控制器和状态
            gameController = null;

            // 重置所有游戏变量
            bulletCount = 10;
            up = down = left = right = shooting = false;
            gamePaused = false;
            isPauseMenuOpen = false;

            // 重置游戏时间
            totalGameTime = 0;
            gameStartTime = System.currentTimeMillis();
            lastUpdateTime = gameStartTime;
            lastBulletRefillTime = 0;

            // 重置生命数
            playerLives = 3;

            // 确保游戏UI元素完全重建
            root.getChildren().clear(); // 确保UI完全清除

            // 使用新接口启动游戏
            startGameWithLevel(tankType, currentLevel);
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
            // 调用GameController的保存方法
            boolean success = gameController.saveGame(saveName);

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

    // 添加子弹显示更新方法
    void updateBulletDisplay() {
        Platform.runLater(() -> {
            // 找到子弹信息框并更新
            for (Node node : gameDataPanel.getChildren()) {
                if (node instanceof VBox) {
                    VBox box = (VBox) node;
                    if (box.getChildren().size() > 1 && box.getChildren().get(0) instanceof Text) {
                        Text title = (Text) box.getChildren().get(0);
                        if (title.getText().equals("子弹")) {
                            Text value = (Text) box.getChildren().get(1);
                            value.setText(Integer.toString(bulletCount));
                            break;
                        }
                    }
                }
            }
        });
    }

    // 清理游戏资源
    void cleanupGameResources() {
        // 停止游戏循环
        if (gameLoop != null) {
            gameLoop.stop();
            gameLoop = null;
        }

        // 清除所有事件监听器
        if (gameCanvas != null) {
            gameCanvas.setOnKeyPressed(null);
            gameCanvas.setOnKeyReleased(null);
            gameCanvas.removeEventHandler(javafx.scene.input.KeyEvent.KEY_PRESSED, event -> {
            });
            gameCanvas.removeEventHandler(javafx.scene.input.KeyEvent.KEY_RELEASED, event -> {
            });
        }

        // 重置所有游戏变量
        bulletCount = 10;
        up = down = left = right = shooting = false;
        gamePaused = false;
        isPauseMenuOpen = false;
        totalGameTime = 0;

        // 设置gameController为null前确保没有引用它的任务在运行
        if (gameController != null) {
            gameController = null;
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
            gameLoop.stop();
            gameLoop = null; // 将引用设为null以便垃圾回收
        }
    }

    /**
     * 创建生命数显示区域
     */
    HBox createLivesDisplay() {
        HBox livesBox = new HBox(10);
        livesBox.setAlignment(Pos.CENTER_LEFT);
        livesBox.setPadding(new Insets(5));
        livesBox.setMinWidth(120); // 设置固定最小宽度
        livesBox.setPrefWidth(120); // 设置固定首选宽度

        // 创建标题
        Label livesLabel = new Label("生命");
        livesLabel.setTextFill(PRIMARY_COLOR); // 使用蓝色
        livesLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        // 创建一个VBox包含标题和内容
        VBox infoBox = new VBox(5);
        infoBox.setAlignment(Pos.CENTER_LEFT);

        // 创建一个HBox用于放置爱心图标
        HBox heartIcons = new HBox(5);
        heartIcons.setAlignment(Pos.CENTER_LEFT);

        // 添加爱心图标显示生命数
        for (int i = 0; i < playerLives; i++) {
            ImageView heartIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/ui/heart.png")));
            heartIcon.setFitWidth(20);
            heartIcon.setFitHeight(20);
            heartIcons.getChildren().add(heartIcon);
        }

        // 将标题和心形图标添加到VBox中
        infoBox.getChildren().addAll(livesLabel, heartIcons);
        livesBox.getChildren().add(infoBox);

        return livesBox;
    }

    // 更新增益效果显示
    private void updatePowerUpDisplay() {
        if (gameController == null || gameController.getPlayerTank() == null)
            return;

        // 添加调试输出
        Tank playerTank = gameController.getPlayerTank();
        Map<Tank.PowerUpType, Double> activeEffects = playerTank.getActiveEffects();

    }

    /**
     * 处理玩家坦克被摧毁的情况
     */
    public void handlePlayerDestroyed() {
        if (gameController == null)
            return;

        // 避免多次调用此方法，检查玩家坦克是否已经处理
        Tank playerTank = gameController.getPlayerTank();
        if (playerTank == null || !playerTank.isDead())
            return;

        // 减少玩家生命值
        playerLives--;

        // 更新生命显示
        updateLivesDisplay();

        // 判断是否游戏结束
        if (playerLives <= 0) {
            // 游戏结束，显示游戏结束界面
            showGameOverScreen();
        } else {
            // 还有生命，立即重生玩家
            respawnPlayer();
        }
    }

    /**
     * 复活玩家坦克
     */
    private void respawnPlayer() {
        if (playerLives > 0) {
            // 寻找有效的重生位置
            LevelMap.MapPosition spawnPos = gameController.findValidSpawnPosition();
            if (spawnPos != null) {
                // 使用当前选择的坦克类型重生
                gameController.respawnPlayerTank(currentTankType, spawnPos.getX(), spawnPos.getY());

                // 确保记录重生状态
                Tank playerTank = gameController.getPlayerTank();
                if (playerTank != null) {
                    // 添加重生效果
                    addRespawnEffect(spawnPos.getX(), spawnPos.getY());

                    // 明确确认无敌状态已设置
                    System.out.println("玩家坦克已重生并设置无敌状态: " +
                            (playerTank.isRespawnInvincible() ? "成功" : "失败"));
                }
            }
        } else {
            showGameOverScreen();
        }
    }

    /**
     * 添加坦克重生特效
     */
    private void addRespawnEffect(int x, int y) {
        try {
            // 创建特效容器
            Group effectGroup = new Group();

            // 创建一个圆形特效
            javafx.scene.shape.Circle circle = new javafx.scene.shape.Circle(20, Color.TRANSPARENT);
            circle.setStroke(Color.LIGHTBLUE);
            circle.setStrokeWidth(3);

            // 设置特效位置
            effectGroup.setTranslateX(x + 20); // 坦克中心点X
            effectGroup.setTranslateY(y + 20); // 坦克中心点Y
            effectGroup.getChildren().add(circle);

            // 添加到游戏画布上层
            StackPane gameArea = (StackPane) gameCanvas.getParent();
            if (gameArea != null) {
                gameArea.getChildren().add(effectGroup);

                // 创建动画：从小到大再消失
                ScaleTransition scale = new ScaleTransition(Duration.millis(500), circle);
                scale.setFromX(0.2);
                scale.setFromY(0.2);
                scale.setToX(2);
                scale.setToY(2);

                FadeTransition fade = new FadeTransition(Duration.millis(500), effectGroup);
                fade.setFromValue(1.0);
                fade.setToValue(0.0);
                fade.setDelay(Duration.millis(200));

                // 播放动画结束后移除特效
                ParallelTransition transition = new ParallelTransition(scale, fade);
                transition.setOnFinished(e -> gameArea.getChildren().remove(effectGroup));
                transition.play();
            }
        } catch (Exception e) {
            System.err.println("无法创建重生特效: " + e.getMessage());
        }
    }

    /**
     * 显示游戏结束界面
     */
    private void showGameOverScreen() {
        // 暂停游戏
        gamePaused = true;

        // 停止游戏循环
        if (gameLoop != null) {
            gameLoop.stop();
        }

        // 计算最终得分
        int finalScore = 0;
        if (gameController != null) {
            int level = gameController.getCurrentLevel();
            int defeatedEnemies = gameController.getDefeatedEnemiesCount();
            // 使用LevelCompletedView中的计算函数
            finalScore = levelCompletedView.calculateScore(level, defeatedEnemies, totalGameTime, playerLives);
        }

        // 显示游戏结束界面
        gameOverScreen.show(finalScore);
    }

    // 更新敌人数量显示
    void updateEnemiesDisplay() {
        if (gameController == null || gameDataPanel == null)
            return;

        // 找到敌人信息框
        VBox enemiesInfo = (VBox) gameDataPanel.lookup("#enemiesInfo");
        if (enemiesInfo != null) {
            // 找到值文本
            Text enemiesValue = (Text) enemiesInfo.getChildren().get(1);
            if (enemiesValue != null) {
                // 获取已摧毁的敌人数量和总目标数量
                int defeated = gameController.getDefeatedEnemiesCount();
                int total = gameController.getTotalEnemyTarget();

                // 更新显示格式：已消灭/总数
                enemiesValue.setText(defeated + "/" + total);

                // 如果接近完成，显示绿色
                if (defeated >= total * 0.8) {
                    enemiesValue.setFill(Color.GREEN);
                } else {
                    enemiesValue.setFill(TEXT_COLOR);
                }

                // 检查是否完成关卡
                if (gameController.isLevelCompleted()) {
                    showLevelCompletedMessage();
                }
            }
        }
    }

    /**
     * 更新生命显示
     */
    void updateLivesDisplay() {
        // 找到生命显示区域
        for (Node node : gameDataPanel.getChildren()) {
            if (node instanceof HBox) {
                HBox box = (HBox) node;
                if (box.getChildren().size() > 0 && box.getChildren().get(0) instanceof VBox) {
                    VBox infoBox = (VBox) box.getChildren().get(0);
                    if (infoBox.getChildren().size() > 0 && infoBox.getChildren().get(0) instanceof Label) {
                        Label label = (Label) infoBox.getChildren().get(0);
                        if (label.getText().equals("生命")) {
                            // 找到了生命区域
                            HBox heartIcons = (HBox) infoBox.getChildren().get(1);
                            heartIcons.getChildren().clear();

                            // 添加爱心图标显示当前生命数
                            for (int i = 0; i < playerLives; i++) {
                                ImageView heartIcon = new ImageView(
                                        new Image(getClass().getResourceAsStream("/images/ui/heart.png")));
                                heartIcon.setFitWidth(20);
                                heartIcon.setFitHeight(20);
                                heartIcons.getChildren().add(heartIcon);
                            }

                            return;
                        }
                    }
                }
            }
        }

        // 如果没有找到生命显示区域，重新创建一个
        HBox livesDisplay = createLivesDisplay();
        gameDataPanel.getChildren().add(livesDisplay);
    }

    // 美化后的关卡完成消息界面
    private void showLevelCompletedMessage() {
        if (root.lookup("#levelCompletedMessage") != null)
            return;

        // 暂停游戏
        gamePaused = true;

        // 获取所需数据
        int currentLevel = gameController.getCurrentLevel();
        int defeatedEnemies = gameController.getDefeatedEnemiesCount();
        String playerTankType = gameController.getPlayerTank().getTypeString();
        int totalLevels = 5; // 游戏总关卡数

        // 使用LevelCompletedView显示完成界面
        levelCompletedView.show(
                currentLevel,
                defeatedEnemies,
                totalGameTime,
                playerLives,
                playerTankType,
                totalLevels);
    }

    // 修改创建增益效果状态栏的方法
    HBox createPowerUpStatusBar() {
        // 创建一个紧凑的HBox容器
        HBox powerUpBar = new HBox(8);
        powerUpBar.setId("powerUpStatusBar");
        powerUpBar.setPadding(new Insets(5, 8, 5, 8));
        powerUpBar.setAlignment(Pos.CENTER_LEFT);
        powerUpBar.setStyle(
                "-fx-background-color: rgba(0, 20, 40, 0.7); -fx-border-color: #00AAFF; -fx-border-radius: 4;");

        // 简化标题
        Label titleLabel = new Label("增益:");
        titleLabel.setTextFill(Color.CYAN);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        powerUpBar.getChildren().add(titleLabel);

        // 添加一个小间隔
        Region spacer = new Region();
        spacer.setPrefWidth(8);
        powerUpBar.getChildren().add(spacer);

        // 创建一个图标容器，水平放置图标
        HBox iconsContainer = new HBox(10);
        iconsContainer.setAlignment(Pos.CENTER);

        // 防止gameController为null时出错
        if (gameController == null) {
            // 如果控制器为空，只返回空的状态栏
            powerUpBar.getChildren().add(iconsContainer);
            return powerUpBar;
        }

        // 为每种增益效果创建图标，但不包括即时效果(HEALTH)
        for (Tank.PowerUpType type : Tank.PowerUpType.values()) {
            if (type == Tank.PowerUpType.HEALTH)
                continue;

            String typeName = type.getName();

            // 创建一个HBox而不是VBox，使其水平布局
            HBox effectBox = new HBox(2);
            effectBox.setAlignment(Pos.CENTER);
            effectBox.setPadding(new Insets(2));

            // 使用StackPane让进度条能够显示在图标底部
            StackPane iconContainer = new StackPane();
            iconContainer.setMinSize(30, 30);
            iconContainer.setMaxSize(30, 30);

            // 创建图标
            ImageView iconView = new ImageView();
            iconView.setFitWidth(30);
            iconView.setFitHeight(30);

            // 使用安全的方式加载图标
            try {
                Image icon = null;
                if (gameController != null) {
                    icon = gameController.getPowerUpImage(typeName);
                }

                if (icon != null) {
                    iconView.setImage(icon);
                } else {
                    // 如果图标加载失败，显示一个占位符
                    Rectangle placeholder = new Rectangle(30, 30);
                    placeholder.setFill(Color.rgb(80, 200, 255, 0.7));
                    placeholder.setArcWidth(8);
                    placeholder.setArcHeight(8);
                    iconContainer.getChildren().add(placeholder);
                }
            } catch (Exception e) {
                System.err.println("无法加载" + typeName + "图标: " + e.getMessage());
                // 添加一个占位符
                Rectangle placeholder = new Rectangle(30, 30);
                placeholder.setFill(Color.rgb(200, 100, 100, 0.7));
                placeholder.setArcWidth(8);
                placeholder.setArcHeight(8);
                iconContainer.getChildren().add(placeholder);
            }

            // 将图标添加到容器
            iconContainer.getChildren().add(iconView);

            // 创建进度条
            ProgressBar progressBar = new ProgressBar(0);
            progressBar.setPrefWidth(30);
            progressBar.setPrefHeight(4);
            progressBar.setStyle("-fx-accent: #00AAFF;");

            // 创建垂直布局用于图标和进度条，然后添加到HBox
            VBox iconWithProgress = new VBox(0);
            iconWithProgress.setAlignment(Pos.CENTER);
            iconWithProgress.getChildren().addAll(iconContainer, progressBar);

            // 将垂直布局添加到效果盒子
            effectBox.getChildren().add(iconWithProgress);

            // 添加边框效果
            effectBox.setStyle("-fx-border-color: #004466; -fx-border-radius: 4;");

            // 初始设置为不可见
            effectBox.setVisible(false);

            // 存储引用以供更新
            powerUpIndicators.put(typeName, iconView);
            powerUpProgressBars.put(typeName, progressBar);
            effectBoxMap.put(typeName, effectBox); // 现在存储的是HBox，类型匹配

            // 将效果盒子添加到图标容器
            iconsContainer.getChildren().add(effectBox);
        }

        // 将图标容器添加到状态栏
        powerUpBar.getChildren().add(iconsContainer);

        return powerUpBar;
    }

    // 修改更新增益效果UI显示的方法
    private void updatePowerUpUIDisplay() {
        if (gameController == null || gameController.getPlayerTank() == null)
            return;

        Tank playerTank = gameController.getPlayerTank();
        Map<Tank.PowerUpType, Double> activeEffects = playerTank.getActiveEffects();

        // 更新每种效果的显示状态
        for (Tank.PowerUpType type : Tank.PowerUpType.values()) {
            if (type == Tank.PowerUpType.HEALTH)
                continue; // 跳过生命恢复

            String typeName = type.getName();
            HBox effectBox = effectBoxMap.get(typeName);

            if (effectBox != null) {
                // 检查效果是否激活
                boolean isActive = activeEffects.containsKey(type);
                effectBox.setVisible(isActive);

                // 如果效果激活，更新进度条
                if (isActive) {
                    double remainingTime = activeEffects.get(type);
                    double maxTime = type.getDuration();
                    double progress = remainingTime / maxTime;

                    // 更新进度条
                    ProgressBar progressBar = powerUpProgressBars.get(typeName);
                    if (progressBar != null) {
                        progressBar.setProgress(progress);
                    }

                    // 当剩余时间少于3秒时，添加闪烁效果
                    if (remainingTime < 3.0) {
                        // 闪烁效果
                        if (Math.random() > 0.5) {
                            effectBox.setStyle("-fx-border-color: #FF4400; -fx-border-radius: 4;");
                            progressBar.setStyle("-fx-accent: #FF4400;");
                        } else {
                            effectBox.setStyle("-fx-border-color: #004466; -fx-border-radius: 4;");
                            progressBar.setStyle("-fx-accent: #00AAFF;");
                        }
                    } else {
                        effectBox.setStyle("-fx-border-color: #004466; -fx-border-radius: 4;");
                        progressBar.setStyle("-fx-accent: #00AAFF;");
                    }
                }
            }
        }
    }

    // 添加获取/设置游戏数据的方法
    public long getTotalGameTime() {
        return totalGameTime;
    }

    public void setTotalGameTime(long time) {
        this.totalGameTime = time;
        updateTimeDisplay(totalGameTime);
    }

    /**
     * 获取当前游戏得分
     * 
     * @return 当前得分
     */
    public int getScore() {
        // 改为使用LevelCompletedView计算得分
        if (gameController != null) {
            int level = gameController.getCurrentLevel();
            int defeatedEnemies = gameController.getDefeatedEnemiesCount();
            return levelCompletedView.calculateScore(level, defeatedEnemies, totalGameTime, playerLives);
        }
        return 0; // 如果游戏控制器不存在，返回0分
    }

    public void setScore(int score) {

    }

    public int getPlayerLives() {
        return playerLives;
    }

    public void setPlayerLives(int lives) {
        this.playerLives = lives;
        updateLivesDisplay();
    }

    public int getBulletCount() {
        return bulletCount;
    }

    public void setBulletCount(int count) {
        this.bulletCount = count;
        updateBulletDisplay();
    }

    // 重置游戏开始时间，保持总游戏时间不变
    public void resetGameStartTime() {
        gameStartTime = System.currentTimeMillis() - totalGameTime;
    }

    // 添加加载游戏功能
    void loadGame() {
        // 确保gameController不为空
        if (gameController == null) {
            // 实例化一个新的GameController
            gameController = new GameController();
            // 设置必要的监听器
            gameController.setGameEventListener(new GameController.GameEventListener() {
                @Override
                public void onPlayerDestroyed() {
                    handlePlayerDestroyed();
                }
            });
            // 设置视图引用
            gameController.setGameView(this);
        }

        // 创建文件选择器
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("加载游戏存档");

        // 设置默认目录（如果存在）
        File savesDir = new File("saves");
        if (savesDir.exists() && savesDir.isDirectory()) {
            fileChooser.setInitialDirectory(savesDir);
        }

        // 设置文件过滤器
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("游戏存档文件", "*.json"));

        // 显示对话框并获取选择的文件
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            // 尝试加载游戏
            boolean success = gameController.loadGame(selectedFile);
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
    }

    // 添加必要的getter/setter方法
    public StackPane getRoot() {
        return root;
    }

    public Color getPrimaryColor() {
        return PRIMARY_COLOR;
    }

    public Color getTextColor() {
        return TEXT_COLOR;
    }

    public void setTimeInfo(Text timeInfo) {
        this.timeInfo = timeInfo;
    }

    public void setGameCanvas(Canvas gameCanvas) {
        this.gameCanvas = gameCanvas;
    }

    public void setGraphicsContext(GraphicsContext gc) {
        this.gc = gc;
    }

    public void setGameDataPanel(HBox gameDataPanel) {
        this.gameDataPanel = gameDataPanel;
    }

    public void setGamePaused(boolean paused) {
        this.gamePaused = paused;
    }

    public void setIsPauseMenuOpen(boolean open) {
        this.isPauseMenuOpen = open;
    }

    public void setLastUpdateTime(long time) {
        this.lastUpdateTime = time;
    }

    public void setLastBulletRefillTime(long time) {
        this.lastBulletRefillTime = time;
    }

    private void showGameScreen() {
        if (singlePlayerGameStarter != null && gameController != null) {
            singlePlayerGameStarter.getGameScreen().show(gameController);
        } else {
            System.err.println("无法显示游戏屏幕: gameController 或 singlePlayerGameStarter 为 null");
        }
    }

    /**
     * 设置游戏控制器
     * 
     * @param gameController 游戏控制器
     */
    public void setGameController(GameController gameController) {
        this.gameController = gameController;
    }

    /**
     * 恢复游戏循环
     * 由PauseMenuView调用以确保游戏循环正确重启
     */
    public void resumeGameLoop() {
        Platform.runLater(() -> {
            if (gameLoop != null) {
                gameLoop.start();

                // 确保游戏状态正确
                gamePaused = false;
                isPauseMenuOpen = false;

                // 如果游戏画布存在，请求焦点以确保键盘控制恢复正常
                if (gameCanvas != null) {
                    gameCanvas.requestFocus();
                }

                System.out.println("游戏循环已重启");
            }
        });
    }
}
