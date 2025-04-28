package com.nau_yyf.view.multiGame;

import com.jfoenix.controls.JFXButton;
import com.nau_yyf.controller.GameController;
import com.nau_yyf.controller.MultiGameController;
import com.nau_yyf.model.Tank;
import com.nau_yyf.service.EffectService;
import com.nau_yyf.service.GameStateService;
import com.nau_yyf.service.PlayerService;
import com.nau_yyf.view.GameOverScreen;
import com.nau_yyf.view.GameScreen;
import com.nau_yyf.view.GameView;
import com.nau_yyf.view.LevelCompletedView;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.HashMap;
import java.util.Map;


/**
 * 多人游戏主屏幕界面
 */
public class MultiPlayerGameScreen implements GameScreen {

    private GameView gameView;
    private Canvas gameCanvas;
    private GraphicsContext gc;
    private HBox gameDataPanel;
    
    // 双人游戏状态变量
    private boolean gamePaused = false;
    private boolean isPauseMenuOpen = false;
    private Text timeInfo;
    private int player1BulletCount = 10;
    private int player2BulletCount = 10;
    private long lastBulletRefillTime = 0;
    private AnimationTimer gameLoop;
    private int player1Lives = 3;
    private int player2Lives = 3;
    private long lastUpdateTime = 0;
    private long pauseTime = 0;

    /**
     * 构造函数
     * 
     * @param gameView 游戏主视图引用
     */
    public MultiPlayerGameScreen(GameView gameView) {
        this.gameView = gameView;
    }

    /**
     * 实现GameScreen接口方法
     */
    @Override
    public void show(GameController controller) {
        if (controller instanceof MultiGameController) {
            show((MultiGameController) controller);
        } else {
            throw new IllegalArgumentException("多人游戏屏幕需要MultiGameController类型的控制器");
        }
    }

    /**
     * 显示多人游戏主屏幕
     * 
     * @param multiGameController 多人游戏控制器
     */
    public void show(MultiGameController multiGameController) {
        // 清除当前内容
        gameView.getRoot().getChildren().clear();

        // 创建游戏主布局
        BorderPane gameLayout = new BorderPane();
        gameLayout.setStyle("-fx-background-color: #1a2634;");

        // ---------- 顶部区域 ----------
        HBox topInfoBar = createTopInfoBar(multiGameController);

        // ---------- 中央游戏区域 ----------
        HBox gameWithSidePanels = createGameArea();

        // ---------- 底部数据面板 ----------
        gameDataPanel = createDataPanel(multiGameController);
        
        // 将各部分添加到布局
        gameLayout.setTop(topInfoBar);
        gameLayout.setCenter(gameWithSidePanels);
        gameLayout.setBottom(gameDataPanel);

        // 初始化游戏时间和子弹
        gameView.resetGameStartTime();
        gameView.setLastUpdateTime(System.currentTimeMillis());
        gameView.setTotalGameTime(0);
        gameView.setGamePaused(false);
        gameView.setLastBulletRefillTime(System.currentTimeMillis());

        // 添加游戏布局到根
        gameView.getRoot().getChildren().add(gameLayout);

        // 设置键盘控制
        gameView.setupKeyboardControls();

        // 确保画布获取焦点
        Platform.runLater(() -> gameCanvas.requestFocus());
    }

    /**
     * 创建顶部信息栏
     */
    private HBox createTopInfoBar(MultiGameController multiGameController) {
        HBox topInfoBar = new HBox(20);
        topInfoBar.setAlignment(Pos.CENTER_LEFT);
        topInfoBar.setPadding(new Insets(10, 20, 10, 20));
        topInfoBar.setStyle("-fx-background-color: rgba(0, 0, 0, 0.4);");

        // 关卡信息
        Text levelInfo = new Text("第" + multiGameController.getCurrentLevel() + "关");
        levelInfo.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        levelInfo.setFill(gameView.getPrimaryColor());

        // 双人模式标识
        Text modeInfo = new Text("双人模式");
        modeInfo.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        modeInfo.setFill(Color.ORANGE);

        // 游戏时间
        timeInfo = new Text("00:00");
        timeInfo.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        timeInfo.setFill(gameView.getTextColor());
        gameView.setTimeInfo(timeInfo);

        // 添加设置按钮
        JFXButton settingsButton = new JFXButton();
        settingsButton.getStyleClass().add("settings-button");
        settingsButton.setButtonType(JFXButton.ButtonType.FLAT);
        settingsButton.setStyle("-fx-background-color: rgba(30, 87, 153, 0.7); " +
                               "-fx-background-radius: 50%; " +
                               "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0, 0, 1); " +
                               "-fx-cursor: hand;");
        settingsButton.setPrefSize(42, 42);
        settingsButton.setMinSize(42, 42);
        settingsButton.setRipplerFill(Color.WHITE);

        try {
            Text settingsIcon = new Text("\uf013");
            settingsIcon.setFont(Font.font("FontAwesome", 16));
            settingsIcon.setFill(Color.WHITE);
            settingsButton.setGraphic(settingsIcon);
        } catch (Exception e) {
            settingsButton.setText("⚙");
            settingsButton.setTextFill(Color.WHITE);
        }

        settingsButton.setOnAction(e -> gameView.showPauseMenu());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        topInfoBar.getChildren().addAll(levelInfo, modeInfo, timeInfo, spacer, settingsButton);

        return topInfoBar;
    }

    /**
     * 创建游戏区域
     */
    private HBox createGameArea() {
        gameCanvas = new Canvas(800, 600);
        gc = gameCanvas.getGraphicsContext2D();

        // 设置GameView的Canvas和GraphicsContext
        gameView.setGameCanvas(gameCanvas);
        gameView.setGraphicsContext(gc);

        // 确保画布可以获取焦点
        gameCanvas.setFocusTraversable(true);

        // 创建游戏区域容器
        StackPane gameArea = new StackPane();
        gameArea.getChildren().add(gameCanvas);
        gameArea.setStyle("-fx-border-color: #37a0da; -fx-border-width: 3; -fx-background-color: #0a1624;");

        // 创建左右两侧的面板
        VBox leftPanel = new VBox(10);
        leftPanel.setPrefWidth(120);
        leftPanel.setPadding(new Insets(10));
        leftPanel.setStyle("-fx-background-color: #2a3645; -fx-border-color: #37a0da; -fx-border-width: 0 2 0 0;");

        // 玩家1信息面板
        VBox player1Panel = createPlayerInfoPanel("玩家1");
        player1Panel.setId("player1Panel");
        leftPanel.getChildren().add(player1Panel);
        
        VBox rightPanel = new VBox(10);
        rightPanel.setPrefWidth(120);
        rightPanel.setPadding(new Insets(10));
        rightPanel.setStyle("-fx-background-color: #2a3645; -fx-border-color: #37a0da; -fx-border-width: 0 0 0 2;");
        
        // 玩家2信息面板
        VBox player2Panel = createPlayerInfoPanel("玩家2");
        player2Panel.setId("player2Panel");
        rightPanel.getChildren().add(player2Panel);

        // 创建包含左侧面板、游戏区域和右侧面板的水平布局
        HBox gameWithSidePanels = new HBox();
        gameWithSidePanels.getChildren().addAll(leftPanel, gameArea, rightPanel);
        HBox.setHgrow(gameArea, Priority.ALWAYS);

        // 初始绘制内容到画布
        gc.setFill(Color.rgb(10, 30, 50));
        gc.fillRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());
        gc.setStroke(Color.CYAN);
        gc.strokeRect(5, 5, gameCanvas.getWidth() - 10, gameCanvas.getHeight() - 10);
        gc.setFill(Color.WHITE);
        gc.fillText("多人游戏画布已初始化", 20, 30);

        return gameWithSidePanels;
    }
    
    /**
     * 创建玩家信息面板
     */
    private VBox createPlayerInfoPanel(String playerTitle) {
        VBox playerPanel = new VBox(10);
        playerPanel.setAlignment(Pos.TOP_CENTER);
        playerPanel.setPadding(new Insets(5));
        playerPanel.setStyle("-fx-background-color: rgba(0, 0, 0, 0.3); -fx-border-color: #555555; -fx-border-radius: 5;");
        
        Text title = new Text(playerTitle);
        title.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        title.setFill(Color.WHITE);
        
        // 生命值显示
        HBox livesBox = new HBox(3);
        livesBox.setAlignment(Pos.CENTER);
        
        for (int i = 0; i < 3; i++) {
            ImageView heartIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/ui/heart.png")));
            heartIcon.setFitWidth(18);
            heartIcon.setFitHeight(18);
            livesBox.getChildren().add(heartIcon);
        }
        
        // 坦克类型
        Text tankType = new Text("坦克: -");
        tankType.setFont(Font.font("Arial", 12));
        tankType.setFill(Color.LIGHTGRAY);
        
        // 血量
        Text health = new Text("血量: 3");
        health.setFont(Font.font("Arial", 12));
        health.setFill(Color.LIGHTGRAY);
        
        // 子弹
        Text bullets = new Text("子弹: 10");
        bullets.setFont(Font.font("Arial", 12));
        bullets.setFill(Color.LIGHTGRAY);
        
        playerPanel.getChildren().addAll(title, livesBox, tankType, health, bullets);
        
        return playerPanel;
    }

    /**
     * 创建数据面板
     */
    private HBox createDataPanel(MultiGameController multiGameController) {
        HBox dataPanel = new HBox(20);
        dataPanel.setPadding(new Insets(10));
        dataPanel.setAlignment(Pos.CENTER);
        dataPanel.setBackground(new Background(new BackgroundFill(
                Color.rgb(0, 20, 40), CornerRadii.EMPTY, Insets.EMPTY)));

        // 创建共享信息显示
        VBox enemiesInfo = createInfoBox("关卡目标", multiGameController.getDefeatedEnemiesCount() + "/" + multiGameController.getTotalEnemyTarget());
        enemiesInfo.setId("enemiesInfo");
        
        // 创建增益效果信息框
        VBox powerUpsInfo = createPowerUpsInfoBox(multiGameController, gameView.getEffectService());
        powerUpsInfo.setId("powerUpsInfo");

        // 在数据面板中添加所有信息
        dataPanel.getChildren().addAll(enemiesInfo, powerUpsInfo);

        // 为所有子元素设置HGrow优先级
        for (Node node : dataPanel.getChildren()) {
            HBox.setHgrow(node, Priority.ALWAYS);
        }

        // 设置GameView的数据面板
        gameView.setGameDataPanel(dataPanel);

        return dataPanel;
    }

    /**
     * 创建信息显示框
     */
    private VBox createInfoBox(String title, String value) {
        VBox infoBox = new VBox(5);
        infoBox.setAlignment(Pos.CENTER);

        Text titleText = new Text(title);
        titleText.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        titleText.setFill(gameView.getPrimaryColor());

        Text valueText = new Text(value);
        valueText.setFont(Font.font("Arial", 16));
        valueText.setFill(gameView.getTextColor());

        infoBox.getChildren().addAll(titleText, valueText);
        return infoBox;
    }

    /**
     * 创建增益信息框
     */
    private VBox createPowerUpsInfoBox(MultiGameController multiGameController, EffectService effectService) {
        VBox powerUpsBox = new VBox(5);
        powerUpsBox.setAlignment(Pos.CENTER);
        powerUpsBox.setMinWidth(120);
        
        Text titleText = new Text("增益");
        titleText.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        titleText.setFill(gameView.getPrimaryColor());
        
        HBox iconsContainer = new HBox(10);
        iconsContainer.setAlignment(Pos.CENTER);
        
        // 左侧玩家1的增益
        VBox player1PowerUps = new VBox(3);
        player1PowerUps.setAlignment(Pos.CENTER);
        Text p1Label = new Text("P1");
        p1Label.setFill(Color.LIGHTBLUE);
        p1Label.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        
        HBox p1Icons = new HBox(3);
        p1Icons.setAlignment(Pos.CENTER);
        p1Icons.setId("p1PowerUps");
        
        player1PowerUps.getChildren().addAll(p1Label, p1Icons);
        
        // 右侧玩家2的增益
        VBox player2PowerUps = new VBox(3);
        player2PowerUps.setAlignment(Pos.CENTER);
        Text p2Label = new Text("P2");
        p2Label.setFill(Color.LIGHTGREEN);
        p2Label.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        
        HBox p2Icons = new HBox(3);
        p2Icons.setAlignment(Pos.CENTER);
        p2Icons.setId("p2PowerUps");
        
        player2PowerUps.getChildren().addAll(p2Label, p2Icons);
        
        // 将两侧都添加到容器
        iconsContainer.getChildren().addAll(player1PowerUps, player2PowerUps);
        
        // 将图标添加到VBox
        powerUpsBox.getChildren().addAll(titleText, iconsContainer);
        
        // TODO: 初始化增益效果显示
        // 这里需要为双人模式创建一个新的效果服务实现类
        
        return powerUpsBox;
    }

    /**
     * 更新敌人数量显示
     */
    public void updateEnemiesDisplay(MultiGameController multiGameController) {
        if (multiGameController == null || gameDataPanel == null)
            return;

        Platform.runLater(() -> {
        // 找到敌人信息框
        VBox enemiesInfo = (VBox) gameDataPanel.lookup("#enemiesInfo");
            if (enemiesInfo != null && enemiesInfo.getChildren().size() > 1) {
            // 找到值文本
            Text enemiesValue = (Text) enemiesInfo.getChildren().get(1);
            if (enemiesValue != null) {
                // 获取已摧毁的敌人数量和总目标数量
                    int defeated = multiGameController.getDefeatedEnemiesCount();
                    int total = multiGameController.getTotalEnemyTarget();

                    // 更新显示格式
                enemiesValue.setText(defeated + "/" + total);

                // 如果接近完成，显示绿色
                if (defeated >= total * 0.8) {
                    enemiesValue.setFill(Color.GREEN);
                } else {
                    enemiesValue.setFill(gameView.getTextColor());
                }

                // 检查是否完成关卡
                GameStateService gameStateService = gameView.getGameStateService();
                    if (gameStateService.isLevelCompleted(multiGameController)) {
                        showLevelCompletedMessage(multiGameController);
                    }
                }
            }
        });
    }

    /**
     * 更新两名玩家的生命显示
     */
    public void updateLivesDisplay(int player1Lives, int player2Lives) {
        Platform.runLater(() -> {
            // 更新玩家1生命
            VBox player1Panel = (VBox) gameView.getRoot().lookup("#player1Panel");
            if (player1Panel != null) {
                HBox livesBox = (HBox) player1Panel.getChildren().get(1);
                updatePlayerLivesIcons(livesBox, player1Lives);
            }
            
            // 更新玩家2生命
            VBox player2Panel = (VBox) gameView.getRoot().lookup("#player2Panel");
            if (player2Panel != null) {
                HBox livesBox = (HBox) player2Panel.getChildren().get(1);
                updatePlayerLivesIcons(livesBox, player2Lives);
            }
        });
    }
    
    /**
     * 更新玩家生命图标
     */
    private void updatePlayerLivesIcons(HBox livesBox, int lives) {
        if (livesBox == null) return;
        
        livesBox.getChildren().clear();
        
        // 添加活跃的生命图标
        for (int i = 0; i < lives; i++) {
            ImageView heartIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/ui/heart.png")));
            heartIcon.setFitWidth(18);
            heartIcon.setFitHeight(18);
            livesBox.getChildren().add(heartIcon);
        }
        
        // 添加暗色的空心生命图标
        for (int i = lives; i < 3; i++) {
            ImageView emptyHeartIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/ui/empty_heart.png")));
            emptyHeartIcon.setFitWidth(18);
            emptyHeartIcon.setFitHeight(18);
            livesBox.getChildren().add(emptyHeartIcon);
        }
    }

    /**
     * 更新玩家1健康状态显示
     */
    public void updatePlayer1HealthDisplay(MultiGameController multiGameController) {
        if (multiGameController == null) return;
        
        Platform.runLater(() -> {
            Tank player1Tank = multiGameController.getPlayer1Tank();
            if (player1Tank == null) return;
            
            VBox player1Panel = (VBox) gameView.getRoot().lookup("#player1Panel");
            if (player1Panel != null && player1Panel.getChildren().size() > 3) {
                Text healthText = (Text) player1Panel.getChildren().get(3);
                int health = player1Tank.getHealth();
                healthText.setText("血量: " + health);

                // 根据血量设置颜色
                if (health <= 1) {
                    healthText.setFill(Color.RED);
                } else if (health <= 2) {
                    healthText.setFill(Color.ORANGE);
                } else {
                    healthText.setFill(Color.LIGHTGRAY);
                }
            }
        });
    }

    /**
     * 更新玩家2健康状态显示
     */
    public void updatePlayer2HealthDisplay(MultiGameController multiGameController) {
        if (multiGameController == null) return;
        
        Platform.runLater(() -> {
            Tank player2Tank = multiGameController.getPlayer2Tank();
            if (player2Tank == null) return;
            
            VBox player2Panel = (VBox) gameView.getRoot().lookup("#player2Panel");
            if (player2Panel != null && player2Panel.getChildren().size() > 3) {
                Text healthText = (Text) player2Panel.getChildren().get(3);
                int health = player2Tank.getHealth();
                healthText.setText("血量: " + health);

                // 根据血量设置颜色
                if (health <= 1) {
                    healthText.setFill(Color.RED);
                } else if (health <= 2) {
                    healthText.setFill(Color.ORANGE);
                } else {
                    healthText.setFill(Color.LIGHTGRAY);
                }
            }
        });
    }

    /**
     * 更新子弹显示 - 双人游戏
     */
    public void updateBulletDisplay(int player1BulletCount, int player2BulletCount) {
        Platform.runLater(() -> {
            // 更新玩家1子弹
            VBox player1Panel = (VBox) gameView.getRoot().lookup("#player1Panel");
            if (player1Panel != null && player1Panel.getChildren().size() > 4) {
                Text bulletText = (Text) player1Panel.getChildren().get(4);
                bulletText.setText("子弹: " + player1BulletCount);
            }
            
            // 更新玩家2子弹
            VBox player2Panel = (VBox) gameView.getRoot().lookup("#player2Panel");
            if (player2Panel != null && player2Panel.getChildren().size() > 4) {
                Text bulletText = (Text) player2Panel.getChildren().get(4);
                bulletText.setText("子弹: " + player2BulletCount);
            }
        });
    }

    /**
     * 处理玩家1坦克被摧毁的情况
     */
    public void handlePlayer1Destroyed(MultiGameController multiGameController, String tankType, int lives) {
        if (multiGameController == null) return;
        
        // 获取多人游戏的玩家服务
        PlayerService playerService = gameView.getPlayerService();

        // 将生命值减一
        player1Lives = Math.max(0, player1Lives - 1);

        // 如果还有生命，可以重生
        boolean playerRespawned = player1Lives > 0;

        if (playerRespawned) {
            // TODO: 调用重生方法
            // 例如: multiPlayerService.respawnPlayer1(multiGameController, tankType);
        }

        // 如果两位玩家都没有生命了，游戏结束
        if (player1Lives <= 0 && player2Lives <= 0) {
            showGameOverScreen(multiGameController);
        }
    }

    /**
     * 处理玩家2坦克被摧毁的情况
     */
    public void handlePlayer2Destroyed(MultiGameController multiGameController, String tankType, int lives) {
        if (multiGameController == null) return;

        // 获取多人游戏的玩家服务
        PlayerService playerService = gameView.getPlayerService();

        // 将生命值减一
        player2Lives = Math.max(0, player2Lives - 1);

        // 如果还有生命，可以重生
        boolean playerRespawned = player2Lives > 0;

        if (playerRespawned) {
            // TODO: 调用重生方法
            // 例如: multiPlayerService.respawnPlayer2(multiGameController, tankType);
        }

        // 如果两位玩家都没有生命了，游戏结束
        if (player1Lives <= 0 && player2Lives <= 0) {
            showGameOverScreen(multiGameController);
        }
    }

    /**
     * 处理两名玩家都被摧毁的情况
     */
    public void handleBothPlayersDestroyed(MultiGameController multiGameController) {
        // 游戏结束
        showGameOverScreen(multiGameController);
    }

    /**
     * 显示游戏结束界面
     */
    
    private void showGameOverScreen(MultiGameController multiGameController) {
        // 暂停游戏
        gameView.setGamePaused(true);

        // 停止游戏循环
        gameView.stopGameLoop();

        // 准备游戏数据
        Map<String, Object> gameData = new HashMap<>();
        int defeatedEnemies = multiGameController.getDefeatedEnemiesCount();
        int p1DefeatedEnemies = defeatedEnemies / 2; // 假设玩家1击败了一半敌人
        int p2DefeatedEnemies = defeatedEnemies - p1DefeatedEnemies; // 剩余的由玩家2击败
        
        int p1Score = p1DefeatedEnemies * 200 + player1Lives * 500;
        int p2Score = p2DefeatedEnemies * 200 + player2Lives * 500;
        int totalScore = p1Score + p2Score;
        
        gameData.put("totalScore", totalScore);
        gameData.put("p1Score", p1Score);
        gameData.put("p2Score", p2Score);
        gameData.put("p1TanksDestroyed", p1DefeatedEnemies);
        gameData.put("p2TanksDestroyed", p2DefeatedEnemies);
        gameData.put("p1LivesRemaining", player1Lives);
        gameData.put("p2LivesRemaining", player2Lives);
        gameData.put("gameTime", (int)(gameView.getTotalGameTime() / 1000));
        gameData.put("currentLevel", multiGameController.getCurrentLevel());

        // 显示游戏结束屏幕
        GameOverScreen gameOverScreen = gameView.getGameOverScreen();
        if (gameOverScreen != null) {
            gameOverScreen.show(gameData);
        }
    }
    
    /**
     * 显示关卡完成消息
     */
    private void showLevelCompletedMessage(MultiGameController multiGameController) {
        if (gameView.getRoot().lookup("#multiLevelCompletedMessage") != null)
            return;

        // 暂停游戏
        gameView.setGamePaused(true);

        // 准备关卡数据
        Map<String, Object> levelData = new HashMap<>();
        int currentLevel = multiGameController.getCurrentLevel();
        int defeatedEnemies = multiGameController.getDefeatedEnemiesCount();
        int totalLevels = 5; // 游戏总关卡数

        levelData.put("currentLevel", currentLevel);
        levelData.put("p1DefeatedEnemies", defeatedEnemies / 2); // 假设玩家1击败了一半敌人
        levelData.put("p2DefeatedEnemies", defeatedEnemies / 2); // 假设玩家2击败了一半敌人
        levelData.put("totalGameTime", gameView.getTotalGameTime());
        levelData.put("p1Lives", player1Lives);
        levelData.put("p2Lives", player2Lives);
        levelData.put("p1TankType", multiGameController.getPlayer1Tank().getTypeString());
        levelData.put("p2TankType", multiGameController.getPlayer2Tank().getTypeString());
        levelData.put("totalLevels", totalLevels);

        // 显示关卡完成视图
        LevelCompletedView levelCompletedView = gameView.getLevelCompletedView();
        if (levelCompletedView != null) {
            levelCompletedView.show(levelData);
        }
    }

    /**
     * 获取游戏画布
     */
    public Canvas getGameCanvas() {
        return gameCanvas;
    }

    /**
     * 获取游戏数据面板
     */
    @Override
    public HBox getGameDataPanel() {
        return gameDataPanel;
    }

    /**
     * 设置游戏数据面板
     */
    @Override
    public void setGameDataPanel(HBox panel) {
        this.gameDataPanel = panel;
    }

    /**
     * 设置时间信息文本
     */
    @Override
    public void setTimeInfo(Text timeInfo) {
        this.timeInfo = timeInfo;
    }

    /**
     * 获取游戏总时间
     */
    @Override
    public long getTotalGameTime() {
        return gameView.getTotalGameTime(); // 委托给GameView
    }

    /**
     * 设置游戏总时间
     */
    @Override
    public void setTotalGameTime(long time) {
        Platform.runLater(() -> {
            if (timeInfo != null) {
                long seconds = time / 1000;
                long minutes = seconds / 60;
                seconds = seconds % 60;
                timeInfo.setText(String.format("%02d:%02d", minutes, seconds));
            }
        });
    }

    /**
     * 获取上次更新时间
     */
    @Override
    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    /**
     * 设置上次更新时间
     */
    @Override
    public void setLastUpdateTime(long time) {
        this.lastUpdateTime = time;
    }

    @Override
    public boolean isGamePaused() {
        return gamePaused;
    }

    /**
     * 设置游戏暂停状态
     * @param paused 是否暂停
     */
    @Override
    public void setGamePaused(boolean paused) {
        // 如果状态没有变化，不执行任何操作
        if (this.gamePaused == paused) return;
        
        this.gamePaused = paused;
        
        if (gameLoop != null) {
            if (paused) {
                // 暂停时停止游戏循环并记录暂停时间
                gameLoop.stop();
                this.pauseTime = System.currentTimeMillis();
            } else {
                // 恢复时完全重置最后更新时间为当前时间
                // 这确保不计算暂停期间的时间
                this.lastUpdateTime = System.currentTimeMillis();
                
                // 启动游戏循环
                gameLoop.start();
                
                // 确保画布重新获得焦点
                if (gameCanvas != null) {
                    gameCanvas.requestFocus();
                }
            }
        }
    }

    @Override
    public boolean isPauseMenuOpen() {
        return false;
    }

    @Override
    public void setIsPauseMenuOpen(boolean open) {

    }

    /**
     * 获取游戏循环
     */
    @Override
    public AnimationTimer getGameLoop() {
        return gameLoop;
    }

    @Override
    public void setGameLoop(AnimationTimer gameLoop) {
        this.gameLoop = gameLoop;
    }

    @Override
    public Text getTimeInfo() {
        return null;
    }

    /**
     * 清理游戏资源
     */
    @Override
    public void cleanupGameResources() {
        // 重置游戏状态变量
        this.player1BulletCount = 10;
        this.player2BulletCount = 10;
        this.gamePaused = false;
        this.isPauseMenuOpen = false;
        this.lastBulletRefillTime = 0;
        this.player1Lives = 3;
        this.player2Lives = 3;
        
        // 停止游戏循环
        if (gameLoop != null) {
            gameLoop.stop();
            gameLoop = null;
        }
        
        // 其他资源清理
        timeInfo = null;
    }

    /**
     * 设置玩家1生命值
     */
    public void setPlayer1Lives(int lives) {
        this.player1Lives = lives;
        updateLivesDisplay(player1Lives, player2Lives);
    }

    /**
     * 设置玩家2生命值
     */
    public void setPlayer2Lives(int lives) {
        this.player2Lives = lives;
        updateLivesDisplay(player1Lives, player2Lives);
    }

    /**
     * 获取玩家1生命值
     */
    public int getPlayer1Lives() {
        return player1Lives;
    }

    /**
     * 获取玩家2生命值
     */
    public int getPlayer2Lives() {
        return player2Lives;
    }

    /**
     * 设置玩家1子弹数量
     */
    public void setPlayer1BulletCount(int count) {
        this.player1BulletCount = count;
        updateBulletDisplay(player1BulletCount, player2BulletCount);
    }

    /**
     * 设置玩家2子弹数量
     */
    public void setPlayer2BulletCount(int count) {
        this.player2BulletCount = count;
        updateBulletDisplay(player1BulletCount, player2BulletCount);
    }

    /**
     * 获取玩家1子弹数量
     */
    public int getPlayer1BulletCount() {
        return player1BulletCount;
    }

    /**
     * 获取玩家2子弹数量
     */
    public int getPlayer2BulletCount() {
        return player2BulletCount;
    }

    /**
     * 设置上次子弹补充时间
     * @param time 时间戳
     */
    public void setLastBulletRefillTime(long time) {
        this.lastBulletRefillTime = time;
    }

    /**
     * 获取上次子弹补充时间
     * @return 上次子弹补充时间(毫秒)
     */
    public long getLastBulletRefillTime() {
        return lastBulletRefillTime;
    }

    @Override
    public int getPlayerLives() {
        // 多人模式下，可以返回两个玩家生命值的平均值，或者第一个玩家的生命值
        return player1Lives;
    }

    @Override
    public int getBulletCount() {
        // 多人模式下，可以返回两个玩家子弹数量的平均值，或者第一个玩家的子弹数量
        return player1BulletCount;
    }

    /**
     * 实现GameScreen接口的setBulletCount方法
     * 在多人游戏中，这个方法会设置玩家1的子弹数量
     */
    @Override
    public void setBulletCount(int count) {
        // 在多人游戏中，我们将这个通用方法映射到玩家1的子弹数量
        this.player1BulletCount = count;
        updateBulletDisplay(player1BulletCount, player2BulletCount);
    }

    /**
     * 实现GameScreen接口的setPlayerLives方法
     * 在多人游戏中，这个方法会设置玩家1的生命值
     */
    @Override
    public void setPlayerLives(int lives) {
        // 在多人游戏中，我们将这个通用方法映射到玩家1的生命值
        this.player1Lives = lives;
        updateLivesDisplay(player1Lives, player2Lives);
    }
}