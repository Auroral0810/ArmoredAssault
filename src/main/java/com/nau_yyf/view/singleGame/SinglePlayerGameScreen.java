package com.nau_yyf.view.singleGame;

import com.jfoenix.controls.JFXButton;
import com.nau_yyf.controller.GameController;
import com.nau_yyf.controller.SingleGameController;
import com.nau_yyf.model.Tank;
import com.nau_yyf.service.EffectService;
import com.nau_yyf.service.GameStateService;
import com.nau_yyf.service.PlayerService;
import com.nau_yyf.service.serviceImpl.SingleEffectServiceImpl;
import com.nau_yyf.service.serviceImpl.SingleGameStateServiceImpl;
import com.nau_yyf.view.GameScreen;
import com.nau_yyf.view.GameView;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.shape.Rectangle;
import javafx.scene.control.ProgressBar;
import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

import static com.nau_yyf.util.TankUtil.getTankDisplayName;

import java.util.HashMap;
import java.util.Map;

/**
 * 单人游戏主屏幕界面
 */
public class SinglePlayerGameScreen implements GameScreen {

    private GameView gameView;
    private Canvas gameCanvas;
    private GraphicsContext gc;
    private HBox gameDataPanel;

    /** 游戏状态变量 */
    private long totalGameTime; // 游戏总时间（毫秒）
    private long lastUpdateTime; // 上次更新时间
    private boolean gamePaused = false; // 游戏是否暂停
    private boolean isPauseMenuOpen = false; // 暂停菜单是否打开
    private Text timeInfo; // 时间显示文本
    private int bulletCount = 10; // 初始子弹数量
    private long lastBulletRefillTime = 0; // 上次子弹补充时间
    private AnimationTimer gameLoop;
    private int playerLives = 3; // 初始生命数为3
    private long pauseTime = 0;

    /**
     * 构造函数
     * 
     * @param gameView 游戏主视图引用
     */
    public SinglePlayerGameScreen(GameView gameView) {
        this.gameView = gameView;
    }

    /**
     * 显示游戏主屏幕
     * 
     * @param singleGameController 游戏控制器
     */
    public void show(SingleGameController singleGameController) {
        // 清除当前内容
        gameView.getRoot().getChildren().clear();

        // 创建简化的游戏主布局
        BorderPane gameLayout = new BorderPane();
        gameLayout.setStyle("-fx-background-color: #1a2634;");

        // ---------- 顶部区域 ----------
        HBox topInfoBar = createTopInfoBar(singleGameController);

        // ---------- 中央游戏区域 ----------
        HBox gameWithSidePanels = createGameArea();

        // ---------- 底部数据面板 ----------
        gameDataPanel = createDataPanel(singleGameController);
        
        // 将各部分添加到布局
        gameLayout.setTop(topInfoBar);
        gameLayout.setCenter(gameWithSidePanels);
        gameLayout.setBottom(gameDataPanel); // 直接使用gameDataPanel作为底部组件

        // 初始化游戏时间和子弹
        gameView.resetGameStartTime();
        gameView.setLastUpdateTime(System.currentTimeMillis());
        gameView.setTotalGameTime(0);
        gameView.setGamePaused(false);
        gameView.setBulletCount(10);
        gameView.setLastBulletRefillTime(0);

        // 添加游戏布局到根
        gameView.getRoot().getChildren().add(gameLayout);

        // 设置键盘控制（放在添加到场景之后）
        gameView.setupKeyboardControls();

        // 在添加到场景之后立即更新一次时间显示
        Platform.runLater(() -> {
            // 强制更新一次
            if (timeInfo != null) {
                updateTimeDisplay(getTotalGameTime());
                System.out.println("初始化时间显示: " + timeInfo.getText());
                
                // 同时也确保子弹显示更新
                updateBulletDisplay(bulletCount);
            } else {
                System.err.println("严重错误: 初始化后timeInfo仍为空!");
            }
            gameCanvas.requestFocus();
        });
    }

    /**
     * 创建顶部信息栏
     */
    private HBox createTopInfoBar(SingleGameController singleGameController) {
        HBox topInfoBar = new HBox(20);
        topInfoBar.setAlignment(Pos.CENTER_LEFT);
        topInfoBar.setPadding(new Insets(10, 20, 10, 20));
        topInfoBar.setStyle("-fx-background-color: rgba(0, 0, 0, 0.4);");

        // 关卡信息
        Text levelInfo = new Text("第" + singleGameController.getCurrentLevel() + "关");
        levelInfo.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        levelInfo.setFill(gameView.getPrimaryColor());

        // 游戏时间
        timeInfo = new Text("00:00");
        timeInfo.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        timeInfo.setFill(gameView.getTextColor());
        // 确保设置timeInfo引用
        this.timeInfo = timeInfo;
        // 同时也通知GameView
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

        topInfoBar.getChildren().addAll(levelInfo, timeInfo, spacer, settingsButton);

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

        // 重要：确保画布可以获取焦点
        gameCanvas.setFocusTraversable(true);

        // 创建游戏区域容器，添加明显的边框和背景
        StackPane gameArea = new StackPane();
        gameArea.getChildren().add(gameCanvas); // 确保画布被添加到容器中
        gameArea.setStyle("-fx-border-color: #37a0da; -fx-border-width: 3; -fx-background-color: #0a1624;");

        // 创建左右两侧的面板，使游戏区域更加明显
        VBox leftPanel = new VBox();
        leftPanel.setPrefWidth(100);
        leftPanel.setStyle("-fx-background-color: #2a3645; -fx-border-color: #37a0da; -fx-border-width: 0 2 0 0;");

        VBox rightPanel = new VBox();
        rightPanel.setPrefWidth(100);
        rightPanel.setStyle("-fx-background-color: #2a3645; -fx-border-color: #37a0da; -fx-border-width: 0 0 0 2;");

        // 创建包含左侧面板、游戏区域和右侧面板的水平布局
        HBox gameWithSidePanels = new HBox();
        gameWithSidePanels.getChildren().addAll(leftPanel, gameArea, rightPanel);
        HBox.setHgrow(gameArea, Priority.ALWAYS);

        // 初始绘制一些内容到画布，确认它正在工作
        gc.setFill(Color.rgb(10, 30, 50));
        gc.fillRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());
        gc.setStroke(Color.CYAN);
        gc.strokeRect(5, 5, gameCanvas.getWidth() - 10, gameCanvas.getHeight() - 10);
        gc.setFill(Color.WHITE);
        gc.fillText("游戏画布已初始化", 20, 30);

        return gameWithSidePanels;
    }

    /**
     * 创建数据面板
     */
    private HBox createDataPanel(SingleGameController singleGameController) {
        HBox dataPanel = new HBox(20);
        dataPanel.setPadding(new Insets(10));
        dataPanel.setAlignment(Pos.CENTER); // 确保所有内容居中对齐
        dataPanel.setBackground(new Background(new BackgroundFill(
                Color.rgb(0, 20, 40), CornerRadii.EMPTY, Insets.EMPTY)));

        // 使用方法创建各种信息显示
        VBox playerInfo = createInfoBox("坦克类型", getTankDisplayName(singleGameController.getPlayerTank().getTypeString()));
        playerInfo.setId("playerInfo");

        VBox healthInfo = createInfoBox("血量", Integer.toString(singleGameController.getPlayerTank().getHealth()));
        healthInfo.setId("healthInfo");

        VBox bulletInfo = createInfoBox("子弹", Integer.toString(gameView.getBulletCount()));
        bulletInfo.setId("bulletInfo");

        VBox enemiesInfo = createInfoBox("关卡目标", 
                singleGameController.getDefeatedEnemiesCount() + "/" + singleGameController.getTotalEnemyTarget());
        enemiesInfo.setId("enemiesInfo");

        // 添加生命显示
        HBox livesDisplay = createLivesDisplay(gameView.getPlayerLives());
        livesDisplay.setId("livesDisplay");
        
        // 创建增益效果信息框
        VBox powerUpsInfo = createPowerUpsInfoBox(singleGameController, gameView.getEffectService());
        powerUpsInfo.setId("powerUpsInfo");

        // 在数据面板中添加所有信息，均匀分布
        dataPanel.getChildren().addAll(playerInfo, healthInfo, bulletInfo, enemiesInfo, livesDisplay, powerUpsInfo);

        // 为所有子元素设置相同的HGrow优先级，使它们均匀分布
        for (Node node : dataPanel.getChildren()) {
            HBox.setHgrow(node, Priority.ALWAYS);
        }

        // 设置GameView的数据面板 - 确保在访问前设置
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

    @Override
    public void show(GameController controller) {

    }

    /**
     * 获取游戏画布
     */
    public Canvas getGameCanvas() {
        return gameCanvas;
    }

    /**
     * 获取图形上下文
     */
    public GraphicsContext getGraphicsContext() {
        return gc;
    }

    /**
     * 获取游戏数据面板
     */
    public HBox getGameDataPanel() {
        return gameDataPanel;
    }

    /**
     * 创建增益信息框，风格与其他信息框一致
     */
    private VBox createPowerUpsInfoBox(SingleGameController singleGameController, EffectService effectService) {
        VBox powerUpsBox = new VBox(5);
        powerUpsBox.setAlignment(Pos.CENTER);
        powerUpsBox.setMinWidth(120); // 设置最小宽度，与其他信息框保持一致
        
        // 使用与其他信息框相同的标题样式
        Text titleText = new Text("增益");
        titleText.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        titleText.setFill(gameView.getPrimaryColor());
        
        // 创建图标容器，水平放置图标
        HBox iconsContainer = new HBox(5);
        iconsContainer.setAlignment(Pos.CENTER);
        
        // 存储增益效果指示器和进度条的映射
        Map<String, ImageView> powerUpIndicators = new HashMap<>();
        Map<String, ProgressBar> powerUpProgressBars = new HashMap<>();
        
        // 防止gameController为null时出错
        if (singleGameController == null) {
            powerUpsBox.getChildren().addAll(titleText, iconsContainer);
            return powerUpsBox;
        }
        
        // 为每种增益效果创建图标，但不包括即时效果(HEALTH)
        for (Tank.PowerUpType type : Tank.PowerUpType.values()) {
            if (type == Tank.PowerUpType.HEALTH)
                continue;
            
            String typeName = type.getName();
            
            // 紧凑布局
            HBox effectBox = new HBox(2);
            effectBox.setAlignment(Pos.CENTER);
            effectBox.setPadding(new Insets(1));
            
            // 使用StackPane让进度条能够显示在图标底部
            StackPane iconContainer = new StackPane();
            iconContainer.setMinSize(22, 22);
            iconContainer.setMaxSize(22, 22);
            
            // 创建图标
            ImageView iconView = new ImageView();
            iconView.setFitWidth(22);
            iconView.setFitHeight(22);
            
            // 使用安全的方式加载图标
            try {
                Image icon = null;
                if (singleGameController != null) {
                    icon = singleGameController.getPowerUpImage(typeName);
                }
                
                if (icon != null) {
                    iconView.setImage(icon);
                } else {
                    // 占位符
                    Rectangle placeholder = new Rectangle(22, 22);
                    placeholder.setFill(Color.rgb(80, 200, 255, 0.7));
                    placeholder.setArcWidth(5);
                    placeholder.setArcHeight(5);
                    iconContainer.getChildren().add(placeholder);
                }
            } catch (Exception e) {
                // 占位符
                Rectangle placeholder = new Rectangle(22, 22);
                placeholder.setFill(Color.rgb(200, 100, 100, 0.7));
                placeholder.setArcWidth(5);
                placeholder.setArcHeight(5);
                iconContainer.getChildren().add(placeholder);
            }
            
            // 将图标添加到容器
            iconContainer.getChildren().add(iconView);
            
            // 创建进度条
            ProgressBar progressBar = new ProgressBar(0);
            progressBar.setPrefWidth(22);
            progressBar.setPrefHeight(3);
            progressBar.setStyle("-fx-accent: #00AAFF;");
            
            // 创建垂直布局用于图标和进度条
            VBox iconWithProgress = new VBox(0);
            iconWithProgress.setAlignment(Pos.CENTER);
            iconWithProgress.getChildren().addAll(iconContainer, progressBar);
            
            // 将垂直布局添加到效果盒子
            effectBox.getChildren().add(iconWithProgress);
            
            // 添加边框效果
            effectBox.setStyle("-fx-border-color: #004466; -fx-border-radius: 3;");
            
            // 初始设置为不可见
            effectBox.setVisible(false);
            
            // 存储引用以供更新
            powerUpIndicators.put(typeName, iconView);
            powerUpProgressBars.put(typeName, progressBar);
            
            // 将效果盒子添加到图标容器
            iconsContainer.getChildren().add(effectBox);
            
            // 将引用存储到服务
            if (effectService instanceof SingleEffectServiceImpl) {
                ((SingleEffectServiceImpl)effectService).registerEffectBox(typeName, effectBox);
            }
        }
        
        // 将图标添加到VBox
        powerUpsBox.getChildren().addAll(titleText, iconsContainer);
        
        // 保存进度条引用到GameView中
        gameView.setPowerUpProgressBars(powerUpProgressBars);
        
        return powerUpsBox;
    }

    /**
     * 更新敌人数量显示 - 单人游戏专用
     */
    public void updateEnemiesDisplay(SingleGameController singleGameController) {
        if (singleGameController == null || gameDataPanel == null)
            return;

        Platform.runLater(() -> {
            // 找到敌人信息框
            VBox enemiesInfo = (VBox) gameDataPanel.lookup("#enemiesInfo");
            if (enemiesInfo != null) {
                // 找到值文本
                Text enemiesValue = (Text) enemiesInfo.getChildren().get(1);
                if (enemiesValue != null) {
                    // 获取已摧毁的敌人数量和总目标数量
                    int defeated = singleGameController.getDefeatedEnemiesCount();
                    int total = singleGameController.getTotalEnemyTarget();

                    String newValue = defeated + "/" + total;
                    
                    // 避免不必要的更新
                    if (!enemiesValue.getText().equals(newValue)) {
                        String oldValue = enemiesValue.getText();
                        enemiesValue.setText(newValue);
                        System.out.println("敌人目标显示更新: " + oldValue + " -> " + newValue);

                        // 强制敌人值颜色变化以引起注意
                        Color originalColor = gameView.getTextColor();
                        
                        // 如果接近完成，显示绿色
                        if (defeated >= total * 0.8) {
                            originalColor = Color.GREEN;
                        }
                        
                        enemiesValue.setFill(Color.YELLOW);
                        
                        // 添加淡出效果，0.5秒后恢复正常颜色
                        final Color finalColor = originalColor;
                        PauseTransition pause = new PauseTransition(Duration.seconds(0.5));
                        pause.setOnFinished(e -> enemiesValue.setFill(finalColor));
                        pause.play();

                        // 检查是否完成关卡
                        GameStateService gameStateService = gameView.getGameStateService();
                        if (gameStateService.isLevelCompleted(singleGameController)) {
                            showLevelCompletedMessage(singleGameController);
                        }
                    }
                }
            }
        });
    }

    /**
     * 更新生命显示 - 单人游戏专用
     */
    public void updateLivesDisplay(int playerLives) {
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
        HBox livesDisplay = createLivesDisplay(playerLives);
        gameDataPanel.getChildren().add(livesDisplay);
    }

    /**
     * 创建生命数显示区域 - 单人游戏专用
     */
    private HBox createLivesDisplay(int playerLives) {
        HBox livesBox = new HBox(10);
        livesBox.setAlignment(Pos.CENTER_LEFT);
        livesBox.setPadding(new Insets(5));
        livesBox.setMinWidth(120); // 设置固定最小宽度
        livesBox.setPrefWidth(120); // 设置固定首选宽度

        // 创建标题
        Label livesLabel = new Label("生命");
        livesLabel.setTextFill(gameView.getPrimaryColor()); // 使用蓝色
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

    /**
     * 更新能力增强效果UI显示
     */
    public void updatePowerUpUIDisplay(SingleGameController singleGameController, EffectService effectService) {
        if (singleGameController == null || singleGameController.getPlayerTank() == null)
            return;
        
        if (effectService instanceof SingleEffectServiceImpl) {
            ((SingleEffectServiceImpl)effectService).updateUIDisplay(
                singleGameController.getPlayerTank(),
                gameView.getPowerUpProgressBars()
            );
        }
    }

    /**
     * 更新血量显示
     */
    public void updateHealthDisplay(SingleGameController singleGameController) {
        if (singleGameController == null || gameDataPanel == null)
            return;

        // 找到血量信息框
        VBox healthInfo = (VBox) gameDataPanel.lookup("#healthInfo");
        if (healthInfo != null && healthInfo.getChildren().size() > 1) {
            // 找到值文本
            Text healthValue = (Text) healthInfo.getChildren().get(1);
            if (healthValue != null && singleGameController.getPlayerTank() != null) {
                // 更新血量值
                int currentHealth = singleGameController.getPlayerTank().getHealth();
                healthValue.setText(Integer.toString(currentHealth));

                // 根据血量设置颜色
                if (currentHealth <= 1) {
                    healthValue.setFill(Color.RED);
                } else if (currentHealth <= 2) {
                    healthValue.setFill(Color.ORANGE);
                } else {
                    healthValue.setFill(gameView.getTextColor());
                }
            }
        }
    }

    /**
     * 更新子弹显示
     */
    public void updateBulletDisplay(int bulletCount) {
        System.out.println("更新子弹显示UI: " + bulletCount);
        
        if (gameDataPanel == null) {
            System.err.println("错误: gameDataPanel为null，无法更新子弹显示");
            return;
        }
        
        // 使用Platform.runLater确保UI更新在JavaFX线程上执行
        Platform.runLater(() -> {
            boolean updated = false;
            
            // 使用索引遍历，避免ConcurrentModificationException
            int childCount = gameDataPanel.getChildren().size();
            for (int i = 0; i < childCount; i++) {
                Node node = gameDataPanel.getChildren().get(i);
                if (node instanceof VBox) {
                    VBox box = (VBox) node;
                    if (box.getChildren().size() > 1 && box.getChildren().get(0) instanceof Text) {
                        Text title = (Text) box.getChildren().get(0);
                        if (title.getText().equals("子弹")) {
                            Text value = (Text) box.getChildren().get(1);
                            String oldValue = value.getText();
                            value.setText(Integer.toString(bulletCount));
                            System.out.println("子弹显示更新: " + oldValue + " -> " + bulletCount);
                            
                            // 强制子弹值颜色变化以引起注意
                            value.setFill(Color.YELLOW);
                            
                            // 添加淡出效果，0.5秒后恢复正常颜色
                            PauseTransition pause = new PauseTransition(Duration.seconds(0.5));
                            pause.setOnFinished(e -> value.setFill(gameView.getTextColor()));
                            pause.play();
                            
                            updated = true;
                            break;
                        }
                    }
                }
            }
            
            if (!updated) {
                System.err.println("警告: 未找到子弹显示组件，无法更新");
                
                // 尝试使用ID查询
                VBox bulletInfo = (VBox) gameDataPanel.lookup("#bulletInfo");
                if (bulletInfo != null && bulletInfo.getChildren().size() > 1) {
                    Text value = (Text) bulletInfo.getChildren().get(1);
                    value.setText(Integer.toString(bulletCount));
                    System.out.println("通过ID更新子弹显示: " + bulletCount);
                } else {
                    System.err.println("通过ID查找也失败，需要重建子弹显示组件");
                    // 这里可以添加重建组件的逻辑
                }
            }
            
            // 强制刷新布局
            gameDataPanel.layout();
        });
    }

    /**
     * 处理玩家坦克被摧毁的情况 - 单人游戏专用
     */
    public void handlePlayerDestroyed(SingleGameController singleGameController, String currentTankType, int playerLives) {
        if (singleGameController == null)
            return;

        // 获取PlayerService
        PlayerService playerService = gameView.getPlayerService();

        // 使用PlayerService处理玩家坦克被摧毁
        boolean playerRespawned = playerService.handlePlayerDestroyed(
                singleGameController, currentTankType, playerLives);

        // 如果玩家未重生，显示游戏结束
        if (!playerRespawned && gameView.getPlayerLives() <= 0) {
            showGameOverScreen(singleGameController);
        }
    }

    /**
     * 显示游戏结束界面 - 单人游戏专用
     */
    private void showGameOverScreen(SingleGameController singleGameController) {
        // 暂停游戏
        gameView.setGamePaused(true);

        // 停止游戏循环
        gameView.stopGameLoop();

        // 计算最终得分，并确保不为负数
        int finalScore = ((SingleGameStateServiceImpl) gameView.getGameStateService()).getScore(singleGameController);
        finalScore = Math.max(0, finalScore); // 确保分数不为负

        // 创建包含游戏数据的Map
        Map<String, Object> gameData = new HashMap<>();
        gameData.put("finalScore", finalScore);
        gameData.put("defeatedEnemies", singleGameController.getDefeatedEnemiesCount());
        gameData.put("gameTime", (int)(gameView.getTotalGameTime() / 1000));
        gameData.put("currentLevel", singleGameController.getCurrentLevel());
        gameData.put("playerLives", gameView.getPlayerLives());

        // 显示游戏结束界面
        gameView.getGameOverScreen().show(gameData);
    }

    /**
     * 显示关卡完成消息 - 单人游戏专用
     */
    private void showLevelCompletedMessage(SingleGameController singleGameController) {
        if (gameView.getRoot().lookup("#levelCompletedMessage") != null)
            return;

        // 暂停游戏
        gameView.setGamePaused(true);

        // 获取所需数据
        int currentLevel = singleGameController.getCurrentLevel();
        int defeatedEnemies = singleGameController.getDefeatedEnemiesCount();
        String playerTankType = singleGameController.getPlayerTank().getTypeString();
        int totalLevels = 5; // 游戏总关卡数

        // 创建包含关卡数据的Map
        Map<String, Object> levelData = new HashMap<>();
        levelData.put("currentLevel", currentLevel);
        levelData.put("defeatedEnemies", defeatedEnemies);
        levelData.put("totalGameTime", gameView.getTotalGameTime());
        levelData.put("playerLives", gameView.getPlayerLives());
        levelData.put("playerTankType", playerTankType);
        levelData.put("totalLevels", totalLevels);

        // 显示完成界面
        gameView.getLevelCompletedView().show(levelData);
    }

    /**
     * 获取游戏总时间
     * @return 游戏总时间(毫秒)
     */
    public long getTotalGameTime() {
        return totalGameTime;
    }

    /**
     * 设置游戏总时间
     * @param time 游戏总时间(毫秒)
     */
    @Override
    public void setTotalGameTime(long time) {
        System.out.println("设置游戏总时间: " + time + "毫秒");
        this.totalGameTime = time;
        
        // 使用Platform.runLater确保在JavaFX线程中更新UI
        Platform.runLater(() -> {
            if (timeInfo != null) {
                long seconds = time / 1000;
                long minutes = seconds / 60;
                seconds = seconds % 60;
                
                String formattedTime = String.format("%02d:%02d", minutes, seconds);
                
                // 直接设置文本
                timeInfo.setText(formattedTime);
                System.out.println("时间显示更新为: " + formattedTime);
                
                // 强制时间值颜色变化以引起注意
                timeInfo.setFill(Color.YELLOW);
                
                // 添加淡出效果，0.5秒后恢复正常颜色
                PauseTransition pause = new PauseTransition(Duration.seconds(0.5));
                pause.setOnFinished(e -> timeInfo.setFill(gameView.getTextColor()));
                pause.play();
                
                // 确保布局更新
                if (timeInfo.getParent() != null) {
                    timeInfo.getParent().layout();
                }
            } else {
                System.err.println("警告: timeInfo为空，无法更新时间显示");
            }
        });
    }

    /**
     * 获取上次更新时间
     * @return 上次更新时间
     */
    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    /**
     * 设置上次更新时间
     * @param time 时间戳
     */
    public void setLastUpdateTime(long time) {
        this.lastUpdateTime = time;
    }

    /**
     * 获取游戏是否暂停
     * @return 是否暂停
     */
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
                // 恢复时更新最后更新时间为当前时间
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

    /**
     * 获取暂停菜单是否打开
     * @return 是否打开
     */
    @Override
    public boolean isPauseMenuOpen() {
        return isPauseMenuOpen;
    }

    /**
     * 设置暂停菜单打开状态
     * @param open 是否打开
     */
    @Override
    public void setIsPauseMenuOpen(boolean open) {
        this.isPauseMenuOpen = open;
    }

    /**
     * 设置时间信息显示文本
     * @param timeInfo 时间文本组件
     */
    public void setTimeInfo(Text timeInfo) {
        this.timeInfo = timeInfo;
    }

    /**
     * 获取子弹数量
     * @return 子弹数量
     */
    public int getBulletCount() {
        return bulletCount;
    }

    /**
     * 设置子弹数量并更新UI显示
     * @param count 子弹数量
     */
    @Override
    public void setBulletCount(int count) {
        // 确保子弹数量不会为负
        if (count < 0) count = 0;
        if (count > 10) count = 10;
        
        // 如果子弹数量没有变化，不更新UI
        if (this.bulletCount == count) return;
        
        System.out.println("设置子弹数量: " + count + "（之前: " + this.bulletCount + "）");
        this.bulletCount = count;
        
        // 使用Platform.runLater确保在JavaFX线程中更新UI
        int finalCount = count;
        Platform.runLater(() -> {
            updateBulletDisplay(finalCount);
        });
    }

    /**
     * 获取上次子弹补充时间
     * @return 上次子弹补充时间(毫秒)
     */
    public long getLastBulletRefillTime() {
        return lastBulletRefillTime;
    }

    /**
     * 设置上次子弹补充时间
     * @param time 时间戳
     */
    public void setLastBulletRefillTime(long time) {
        this.lastBulletRefillTime = time;
    }

    /**
     * 设置游戏数据面板
     * @param gameDataPanel 游戏数据面板
     */
    public void setGameDataPanel(HBox gameDataPanel) {
        this.gameDataPanel = gameDataPanel;
    }

    /**
     * 获取游戏循环
     * @return 游戏循环
     */
    public AnimationTimer getGameLoop() {
        return gameLoop;
    }

    /**
     * 设置游戏循环
     * @param gameLoop 游戏循环
     */
    public void setGameLoop(AnimationTimer gameLoop) {
        this.gameLoop = gameLoop;
    }

    /**
     * 获取时间信息文本组件
     * @return 时间信息文本组件
     */
    @Override
    public Text getTimeInfo() {
        return this.timeInfo; // 修改返回实际的timeInfo对象而不是null
    }

    /**
     * 获取玩家生命值
     * @return 玩家生命值
     */
    public int getPlayerLives() {
        return playerLives;
    }

    /**
     * 设置玩家生命值
     * @param lives 生命值
     */
    public void setPlayerLives(int lives) {
        this.playerLives = lives;
        updateLivesDisplay(lives);
    }

    /**
     * 清理游戏资源
     */
    public void cleanupGameResources() {
        // 重置游戏状态变量
        this.bulletCount = 10;
        this.gamePaused = false;
        this.isPauseMenuOpen = false;
        this.totalGameTime = 0;
        this.lastUpdateTime = 0;
        this.lastBulletRefillTime = 0;
        this.playerLives = 3;
        
        // 停止游戏循环
        if (gameLoop != null) {
            gameLoop.stop();
            gameLoop = null;
        }
        
        // 其他资源清理
        timeInfo = null;
    }

    // 添加辅助方法确保时间显示更新
    private void updateTimeDisplay(long time) {
        if (timeInfo == null) {
            System.err.println("updateTimeDisplay: timeInfo为空!");
            return;
        }
        
        long seconds = time / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        
        String formattedTime = String.format("%02d:%02d", minutes, seconds);
        timeInfo.setText(formattedTime);
        System.out.println("直接更新时间显示: " + formattedTime);
    }

}