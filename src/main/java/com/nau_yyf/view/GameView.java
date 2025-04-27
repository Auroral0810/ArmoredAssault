package com.nau_yyf.view;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import com.nau_yyf.controller.GameController;
import com.nau_yyf.model.Bullet;
import com.nau_yyf.model.LevelMap;
import com.nau_yyf.model.PowerUp;
import com.nau_yyf.model.Tank;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.animation.AnimationTimer;
import javafx.scene.shape.Rectangle;
import javafx.scene.control.Separator;
import com.jfoenix.controls.JFXSlider;
import com.jfoenix.controls.JFXRadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.Node;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
import java.util.Timer;
import java.util.TimerTask;

import java.util.Arrays;
import java.util.List;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.ScaleTransition;
import javafx.animation.ParallelTransition;
import javafx.geometry.HPos;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.KeyCode;

public class GameView {
    private Stage stage;
    private Scene scene;
    private StackPane root;
    private GameController gameController;
    
    // 游戏标题和背景
    private final String GAME_TITLE = "TANK 2025";
    private final Color PRIMARY_COLOR = Color.rgb(37, 160, 218);    // 蓝色
    private final Color SECONDARY_COLOR = Color.rgb(76, 175, 80);   // 绿色
    private final Color TEXT_COLOR = Color.WHITE;
    private final Color BACKGROUND_COLOR = Color.rgb(27, 40, 56);   // 深蓝灰色
    
    // 坦克类型和方向
    private final List<String> TANK_TYPES = Arrays.asList("light", "standard", "heavy");
    private int selectedTankType = 1; // 默认选择standard坦克
    
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
        
        Platform.runLater(() -> {
            // 初始化根布局
            root = new StackPane();
            
            // 加载背景图
            try {
                Image backgroundImage = new Image(getClass().getResourceAsStream("/images/backgrounds/game_background.png"));
                BackgroundImage background = new BackgroundImage(
                        backgroundImage,
                        BackgroundRepeat.NO_REPEAT,
                        BackgroundRepeat.NO_REPEAT,
                        BackgroundPosition.CENTER,
                        new BackgroundSize(100, 100, true, true, false, true)
                );
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
            
            // 显示主菜单
            showMainMenu();
        });
    }
    
    /**
     * 显示主菜单
     */
    public void showMainMenu() {
        Platform.runLater(() -> {
            // 清除当前内容
            root.getChildren().clear();
            
            // 创建垂直布局容器
            VBox menuContainer = new VBox(20);
            menuContainer.setAlignment(Pos.CENTER);
            menuContainer.setPadding(new Insets(50, 0, 50, 0));
            menuContainer.setMaxWidth(600);
            
            // 加载游戏Logo
            try {
                Image logoImage = new Image(getClass().getResourceAsStream("/images/logo/tank_logo.png"));
                logoImageView = new ImageView(logoImage);
                logoImageView.setFitWidth(250);
                logoImageView.setFitHeight(250);
                logoImageView.setPreserveRatio(true);
            } catch (Exception e) {
                System.err.println("无法加载Logo: " + e.getMessage());
                // 使用文字替代
                logoImageView = null;
            }
            
            if (logoImageView != null) {
                menuContainer.getChildren().add(logoImageView);
            } else {
                // 游戏标题作为备用
                Text titleText = new Text(GAME_TITLE);
                titleText.setFont(Font.font("Impact", FontWeight.BOLD, 72));
                titleText.setFill(PRIMARY_COLOR);
                
                // 添加阴影效果
                DropShadow dropShadow = new DropShadow();
                dropShadow.setColor(Color.rgb(0, 0, 0, 0.5));
                dropShadow.setRadius(5);
                dropShadow.setOffsetX(3);
                dropShadow.setOffsetY(3);
                titleText.setEffect(dropShadow);
                
                menuContainer.getChildren().add(titleText);
            }
            
            // 版本标签
            Label versionLabel = new Label("Version 1.0");
            versionLabel.setTextFill(TEXT_COLOR);
            versionLabel.setStyle("-fx-font-style: italic;");
            menuContainer.getChildren().add(versionLabel);
            
            // 间隔
            Region spacer = new Region();
            VBox.setVgrow(spacer, Priority.ALWAYS);
            menuContainer.getChildren().add(spacer);
            
            // 创建按钮
            JFXButton singlePlayerButton = createMenuButton("单人游戏", e -> showSinglePlayerOptions());
            JFXButton multiPlayerButton = createMenuButton("双人游戏", e -> showMessage("双人游戏功能即将推出"));
            JFXButton onlineButton = createMenuButton("远程联机", e -> showMessage("联机功能即将推出"));
            JFXButton instructionsButton = createMenuButton("游戏说明", e -> showInstructions());
            JFXButton settingsButton = createMenuButton("设置", e -> showMessage("设置功能即将推出"));
            JFXButton exitButton = createMenuButton("退出游戏", e -> Platform.exit());
            
            // 将按钮添加到布局容器
            menuContainer.getChildren().addAll(
                    singlePlayerButton,
                    multiPlayerButton,
                    onlineButton,
                    instructionsButton,
                    settingsButton,
                    exitButton
            );
            
            // 将菜单容器添加到根布局
            root.getChildren().add(menuContainer);
            
            // 显示舞台
            stage.show();
        });
    }
    
    /**
     * 显示单人游戏选项
     */
    private void showSinglePlayerOptions() {
        Platform.runLater(() -> {
            // 清除当前内容
            root.getChildren().clear();
            
            // 创建垂直布局容器
            VBox optionsContainer = new VBox(20);
            optionsContainer.setAlignment(Pos.CENTER);
            optionsContainer.setPadding(new Insets(50, 0, 50, 0));
            optionsContainer.setMaxWidth(600);
            
            // 标题
            Text titleText = new Text("单人游戏");
            titleText.setFont(Font.font("Arial", FontWeight.BOLD, 48));
            titleText.setFill(PRIMARY_COLOR);
            
            // 添加阴影效果
            DropShadow dropShadow = new DropShadow();
            dropShadow.setColor(Color.rgb(0, 0, 0, 0.5));
            dropShadow.setRadius(5);
            dropShadow.setOffsetX(3);
            dropShadow.setOffsetY(3);
            titleText.setEffect(dropShadow);
            
            // 创建按钮
            JFXButton newGameButton = createMenuButton("开始新游戏", e -> showTankSelection());
            JFXButton loadGameButton = createMenuButton("加载存档", e -> showMessage("加载存档功能即将推出"));
            JFXButton backButton = createMenuButton("返回", e -> showMainMenu());
            
            // 将元素添加到布局容器
            optionsContainer.getChildren().addAll(
                    titleText,
                    new Region(), // 间隔
                    newGameButton,
                    loadGameButton,
                    backButton
            );
            
            // 设置VBox垂直增长属性
            VBox.setVgrow(optionsContainer.getChildren().get(1), Priority.ALWAYS);
            
            // 将菜单容器添加到根布局
            root.getChildren().add(optionsContainer);
        });
    }
    
    /**
     * 显示坦克选择界面
     */
    private void showTankSelection() {
        Platform.runLater(() -> {
            // 清除当前内容
            root.getChildren().clear();
            
            // 创建主布局
            BorderPane mainLayout = new BorderPane();
            
            // 标题
            Text titleText = new Text("选择你的坦克");
            titleText.setFont(Font.font("Arial", FontWeight.BOLD, 36));
            titleText.setFill(PRIMARY_COLOR);
            
            StackPane titlePane = new StackPane(titleText);
            titlePane.setPadding(new Insets(30, 0, 30, 0));
            mainLayout.setTop(titlePane);
            
            // 坦克选择区域
            HBox tankSelectionArea = new HBox(50);
            tankSelectionArea.setAlignment(Pos.CENTER);
            tankSelectionArea.setPadding(new Insets(50));
            
            // 添加三种类型的坦克
            for (int i = 0; i < TANK_TYPES.size(); i++) {
                final int index = i;
                String tankType = TANK_TYPES.get(i);
                
                VBox tankOption = new VBox(15);
                tankOption.setAlignment(Pos.CENTER);
                
                // 坦克预览
                ImageView tankImage = null;
                try {
                    // 直接使用PNG格式加载图片
                    String imagePath = "/images/tanks/friendly/" + tankType + "/1.png";
                    System.out.println("尝试加载坦克PNG图片: " + imagePath);
                    
                    InputStream imageStream = getClass().getResourceAsStream(imagePath);
                    if (imageStream != null) {
                        Image tankImg = new Image(imageStream);
                        tankImage = new ImageView(tankImg);
                        System.out.println("成功加载PNG图片: " + imagePath);
                    } else {
                        // PNG加载失败，尝试创建占位符
                        System.out.println("PNG图片未找到，创建占位符");
                        throw new Exception("PNG图片未找到");
                    }
                    
                    // 设置图片大小
                    tankImage.setFitWidth(120);
                    tankImage.setFitHeight(120);
                    tankImage.setPreserveRatio(true);
                } catch (Exception e) {
                    System.err.println("无法加载坦克图片: " + e.getMessage());
                    
                    // 创建一个简单的占位符
                    javafx.scene.shape.Rectangle rect = new javafx.scene.shape.Rectangle(120, 120);
                    if (tankType.equals("light")) {
                        rect.setFill(Color.rgb(120, 200, 80));  // 浅绿色
                    } else if (tankType.equals("standard")) {
                        rect.setFill(Color.rgb(80, 150, 220));  // 蓝色
                    } else {
                        rect.setFill(Color.rgb(200, 80, 80));   // 红色
                    }
                    rect.setArcWidth(20);
                    rect.setArcHeight(20);
                    
                    // 创建坦克形状轮廓
                    javafx.scene.shape.Rectangle body = new javafx.scene.shape.Rectangle(80, 40);
                    body.setFill(Color.rgb(50, 50, 50));
                    body.setArcWidth(10);
                    body.setArcHeight(10);
                    
                    javafx.scene.shape.Rectangle barrel = new javafx.scene.shape.Rectangle(60, 16);
                    barrel.setFill(Color.rgb(70, 70, 70));
                    barrel.setArcWidth(5);
                    barrel.setArcHeight(5);
                    
                    Group tankShape = new Group(body, barrel);
                    barrel.setTranslateX(30);
                    
                    StackPane placeholder = new StackPane(rect, tankShape);
                    placeholder.setMaxSize(120, 120);
                    
                    // 将占位符添加到布局中
                    tankOption.getChildren().add(placeholder);
                    
                    // 跳过其他步骤
                    return;
                }
                
                // 坦克名称和属性
                Text tankName = new Text(getTankDisplayName(tankType));
                tankName.setFont(Font.font("Arial", FontWeight.BOLD, 18));
                tankName.setFill(TEXT_COLOR);
                
                Text tankDesc = new Text(getTankDescription(tankType));
                tankDesc.setFont(Font.font("Arial", 14));
                tankDesc.setFill(TEXT_COLOR);
                tankDesc.setWrappingWidth(200);
                tankDesc.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
                
                // 选择按钮
                JFXButton selectButton = new JFXButton("选择");
                selectButton.getStyleClass().add("tank-select-button");
                selectButton.setButtonType(JFXButton.ButtonType.RAISED);
                if (index == selectedTankType) {
                    selectButton.setStyle("-fx-background-color: " + toHexString(SECONDARY_COLOR) + ";");
                    selectButton.setText("已选择");
                } else {
                    selectButton.setStyle("-fx-background-color: " + toHexString(PRIMARY_COLOR) + ";");
                }
                
                selectButton.setOnAction(e -> {
                    selectedTankType = index;
                    showTankSelection(); // 刷新界面
                });
                
                // 将元素添加到坦克选项容器
                tankOption.getChildren().addAll(tankImage, tankName, tankDesc, selectButton);
                
                // 添加边框效果（如果是当前选中的）
                if (index == selectedTankType) {
                    tankOption.setStyle("-fx-border-color: " + toHexString(SECONDARY_COLOR) + "; " +
                                       "-fx-border-width: 3; " +
                                       "-fx-border-radius: 5; " +
                                       "-fx-padding: 10;");
                } else {
                    tankOption.setStyle("-fx-padding: 13;"); // 保持相同的总尺寸
                }
                
                // 添加到选择区域
                tankSelectionArea.getChildren().add(tankOption);
            }
            
            mainLayout.setCenter(tankSelectionArea);
            
            // 底部按钮区域
            HBox bottomButtons = new HBox(20);
            bottomButtons.setAlignment(Pos.CENTER);
            bottomButtons.setPadding(new Insets(30));
            
            JFXButton backButton = createMenuButton("返回", e -> showSinglePlayerOptions());
            JFXButton startButton = createMenuButton("开始游戏", e -> startGame());
            startButton.setStyle("-fx-background-color: " + toHexString(SECONDARY_COLOR) + ";");
            
            bottomButtons.getChildren().addAll(backButton, startButton);
            mainLayout.setBottom(bottomButtons);
            
            // 将主布局添加到根布局
            root.getChildren().add(mainLayout);
        });
    }
    
    /**
     * 获取坦克显示名称
     */
    private String getTankDisplayName(String tankType) {
        switch (tankType) {
            case "light": return "轻型坦克";
            case "standard": return "标准坦克";
            case "heavy": return "重型坦克";
            default: return "未知坦克";
        }
    }
    
    /**
     * 获取坦克描述
     */
    private String getTankDescription(String tankType) {
        switch (tankType) {
            case "light":
                return "速度快，机动性强，但装甲薄，攻击力较弱。";
            case "standard":
                return "各项性能均衡，适合大多数战斗场景。";
            case "heavy":
                return "装甲厚，火力强大，但速度较慢，不适合快速战术。";
            default:
                return "";
        }
    }
    
    /**
     * 开始游戏
     */
    private void startGame() {
        String selectedTank = TANK_TYPES.get(selectedTankType);
        
        // 显示关卡选择对话框
        showLevelSelectionDialog(selectedTank);
    }
    
    // 添加关卡选择对话框方法
    private void showLevelSelectionDialog(String selectedTankType) {
        Platform.runLater(() -> {
            JFXDialogLayout content = new JFXDialogLayout();
            content.setHeading(new Text("选择关卡"));
            
            VBox levelOptions = new VBox(10);
            levelOptions.setAlignment(Pos.CENTER);
            
            // 先创建对话框
            JFXDialog dialog = new JFXDialog(root, content, JFXDialog.DialogTransition.CENTER);
            
            // 创建5个关卡选择按钮
            for (int i = 1; i <= 5; i++) {
                final int level = i;
                JFXButton levelButton = new JFXButton("第 " + i + " 关");
                levelButton.setPrefWidth(200);
                levelButton.setButtonType(JFXButton.ButtonType.RAISED);
                levelButton.setStyle("-fx-background-color: " + toHexString(PRIMARY_COLOR) + ";");
                levelButton.setTextFill(TEXT_COLOR);
                
                // 使用已创建的对话框变量
                levelButton.setOnAction(e -> {
                    dialog.close();
                    startGameWithLevel(selectedTankType, level);
                });
                
                levelOptions.getChildren().add(levelButton);
            }
            
            // 添加返回按钮
            JFXButton backButton = new JFXButton("返回");
            backButton.setPrefWidth(200);
            backButton.setButtonType(JFXButton.ButtonType.RAISED);
            backButton.setStyle("-fx-background-color: #999999;");
            backButton.setTextFill(TEXT_COLOR);
            
            // 使用已创建的对话框变量
            backButton.setOnAction(e -> dialog.close());
            levelOptions.getChildren().add(backButton);
            
            content.setBody(levelOptions);
            dialog.show();
        });
    }
    
    // 完全优化的游戏启动方法
    private void startGameWithLevel(String selectedTankType, int level) {
        // 创建游戏控制器并加载关卡
        gameController = new GameController();
        gameController.loadLevel(level);
        gameController.setPlayerTankType(selectedTankType);
        
        // 设置对GameView的引用
        gameController.setGameView(this);
        
        // 设置事件监听器
        gameController.setGameEventListener(new GameController.GameEventListener() {
            @Override
            public void onPlayerDestroyed() {
                // 在JavaFX应用线程中处理玩家坦克摧毁事件
                javafx.application.Platform.runLater(() -> {
            handlePlayerDestroyed();
                });
            }
        });
        
        // 初始化游戏状态
        gamePaused = false;
        isPauseMenuOpen = false;
        playerLives = 3;
        bulletCount = 10;
        
        // 显示游戏屏幕
        showGameScreen();
        
        // 立即更新所有显示元素
        updateHealthDisplay();
        updateLivesDisplay();
        updateEnemiesDisplay();
        updateBulletDisplay();
        
        // 记录游戏开始时间
        gameStartTime = System.currentTimeMillis();
        lastBulletRefillTime = gameStartTime;
        
        // 启动游戏循环
        startGameLoop();
    }
    
    private void showGameScreen() {
        // 清除当前内容
        root.getChildren().clear();
        
        // 创建简化的游戏主布局
        BorderPane gameLayout = new BorderPane();
        gameLayout.setStyle("-fx-background-color: #1a2634;");
        
        // ---------- 顶部区域 ----------
        HBox topInfoBar = new HBox(20);
        topInfoBar.setAlignment(Pos.CENTER_LEFT);
        topInfoBar.setPadding(new Insets(10, 20, 10, 20));
        topInfoBar.setStyle("-fx-background-color: rgba(0, 0, 0, 0.4);");
        
        // 关卡信息
        Text levelInfo = new Text("第" + gameController.getCurrentLevel() + "关");
        levelInfo.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        levelInfo.setFill(PRIMARY_COLOR);
        
        // 游戏时间
        timeInfo = new Text("00:00");
        timeInfo.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        timeInfo.setFill(TEXT_COLOR);
        
        // 添加设置按钮
        JFXButton settingsButton = new JFXButton();
        settingsButton.getStyleClass().add("settings-button");
        settingsButton.setButtonType(JFXButton.ButtonType.RAISED);
        settingsButton.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");
        settingsButton.setPrefSize(40, 40);
        
        try {
            Text settingsIcon = new Text("\uf013");
            settingsIcon.setFont(javafx.scene.text.Font.font("FontAwesome", 16));
            settingsIcon.setFill(Color.WHITE);
            settingsButton.setGraphic(settingsIcon);
        } catch (Exception e) {
            settingsButton.setText("⚙");
            settingsButton.setTextFill(Color.WHITE);
        }
        
        settingsButton.setOnAction(e -> showPauseMenu());
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        topInfoBar.getChildren().addAll(levelInfo, timeInfo, spacer, settingsButton);
        
        // ---------- 中央游戏区域 ----------
        gameCanvas = new Canvas(800, 600);
        gc = gameCanvas.getGraphicsContext2D();
        
        // 重要：确保画布可以获取焦点
        gameCanvas.setFocusTraversable(true);
        
        // 创建游戏区域容器，添加明显的边框和背景
        StackPane gameArea = new StackPane(gameCanvas);
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
        
        // ---------- 底部数据面板 ----------
        gameDataPanel = new HBox(20);
        gameDataPanel.setPadding(new Insets(10));
        gameDataPanel.setAlignment(Pos.CENTER);
        gameDataPanel.setBackground(new Background(new BackgroundFill(
            Color.rgb(0, 20, 40), CornerRadii.EMPTY, Insets.EMPTY)));
        
        // 使用方法创建各种信息显示
        VBox playerInfo = createInfoBox("坦克类型", getTankDisplayName(gameController.getPlayerTank().getTypeString()));
        playerInfo.setId("playerInfo");

        VBox healthInfo = createInfoBox("血量", Integer.toString(gameController.getPlayerTank().getHealth()));
        healthInfo.setId("healthInfo");

        VBox bulletInfo = createInfoBox("子弹", Integer.toString(bulletCount));
        bulletInfo.setId("bulletInfo");

        VBox enemiesInfo = createInfoBox("关卡目标", gameController.getDefeatedEnemiesCount() + "/" + gameController.getTotalEnemyTarget());
        enemiesInfo.setId("enemiesInfo");

        // 添加生命显示和增益效果区域
        HBox livesDisplay = createLivesDisplay();
        livesDisplay.setId("livesDisplay");
        
        // 在数据面板中添加所有信息
        gameDataPanel.getChildren().addAll(playerInfo, healthInfo, bulletInfo, enemiesInfo, livesDisplay);
        
        // 添加增益效果状态栏
        HBox powerUpStatusBar = createPowerUpStatusBar();
        gameDataPanel.getChildren().add(powerUpStatusBar);
        
        // 将各部分添加到布局
        gameLayout.setTop(topInfoBar);
        gameLayout.setCenter(gameWithSidePanels);
        gameLayout.setBottom(gameDataPanel);
        
        // 初始化游戏时间和子弹
        gameStartTime = System.currentTimeMillis();
        lastUpdateTime = gameStartTime;
        totalGameTime = 0;
        gamePaused = false;
        bulletCount = 10;
        lastBulletRefillTime = 0;
        
        // 添加游戏布局到根
        root.getChildren().add(gameLayout);
        
        // 设置键盘控制（放在添加到场景之后）
        setupKeyboardControls();
        
        // 确保画布获取焦点
        Platform.runLater(() -> gameCanvas.requestFocus());
        
        // 开始游戏循环
        startGameLoop();
    }
    /**
     * 创建信息显示框
     */
    private VBox createInfoBox(String title, String value) {
        VBox infoBox = new VBox(5);
        infoBox.setAlignment(Pos.CENTER);
        
        Text titleText = new Text(title);
        titleText.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        titleText.setFill(PRIMARY_COLOR);
        
        Text valueText = new Text(value);
        valueText.setFont(Font.font("Arial", 16));
        valueText.setFill(TEXT_COLOR);
        
        infoBox.getChildren().addAll(titleText, valueText);
        return infoBox;
    }
    // 更健壮的键盘控制设置方法
    private void setupKeyboardControls() {
        System.out.println("重新设置键盘监听器...");
        
        // 重置按键状态
        up = down = left = right = shooting = false;
        
        // 清除所有可能存在的键盘监听器
        // 1. 清除画布上的监听器
        if (gameCanvas != null) {
            gameCanvas.setOnKeyPressed(null);
            gameCanvas.setOnKeyReleased(null);
            
            // 移除所有键盘事件处理器，确保彻底清除
            gameCanvas.removeEventHandler(javafx.scene.input.KeyEvent.KEY_PRESSED, event -> {});
            gameCanvas.removeEventHandler(javafx.scene.input.KeyEvent.KEY_RELEASED, event -> {});
        }
        
        // 2. 清除场景上可能存在的监听器
        if (scene != null) {
            scene.setOnKeyPressed(null);
            scene.setOnKeyReleased(null);
        }
        
        // 3. 清除根布局上可能存在的监听器
        if (root != null) {
            root.setOnKeyPressed(null);
            root.setOnKeyReleased(null);
        }
        
        // 仅在画布级别添加新的监听器，使用更清晰的命名
        gameCanvas.setOnKeyPressed(e -> {
            String code = e.getCode().toString();
            
            // 如果暂停菜单已打开，只处理ESC键
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
    // 修改startGameLoop方法，将AnimationTimer保存为成员变量
    private void startGameLoop() {
        // 先停止旧的游戏循环（如果存在）
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
                if (deltaTime > 0.25) deltaTime = 0.25;
                
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
    
    // 新增方法：固定时间步长逻辑更新
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
    
    // 新增方法：根据坦克类型恢复子弹
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
                refillDelay = (int)(refillDelay * 0.8);
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
    
    // 渲染方法 - 每帧调用一次
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
     * 处理玩家输入 - 简化版本，移除重复的碰撞检测
     */
    private void handlePlayerInput() {
        // 获取玩家坦克
        Tank playerTank = gameController.getPlayerTank();
        
        // 如果坦克不存在或已经死亡，不处理任何输入
        if (playerTank == null || playerTank.isDead()) {
            return;
        }
        
        if (gameController == null) return;
        
        boolean moved = false;
        
        // 根据按键状态设置方向
        if (up) {
            playerTank.setDirection(Tank.Direction.UP);
            moved = true;
        } 
        else if (down) {
            playerTank.setDirection(Tank.Direction.DOWN);
            moved = true;
        }
        else if (left) {
            playerTank.setDirection(Tank.Direction.LEFT);
            moved = true;
        } 
        else if (right) {
            playerTank.setDirection(Tank.Direction.RIGHT);
            moved = true;
        }
        
        // 如果有按键被按下，执行移动
        if (moved) {
            // 调用坦克的move方法
            playerTank.move(gameController);
            
            // 移动后更新健康值显示
            if (playerTank.isInWaterLastFrame()) {
                updateHealthDisplay();
            }
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
    
    // 更新血量显示方法
    public void updateHealthDisplay() {
        if (gameController == null || gameDataPanel == null) return;
        
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
    private void showInstructions() {
        Platform.runLater(() -> {
            JFXDialogLayout content = new JFXDialogLayout();
            content.setHeading(new Text("游戏说明"));
            
            VBox instructionsContent = new VBox(10);
            
            Text controlsTitle = new Text("控制方式：");
            controlsTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            
            Text controlsText = new Text(
                    "W/↑ - 向上移动\n" +
                    "S/↓ - 向下移动\n" +
                    "A/← - 向左移动\n" +
                    "D/→ - 向右移动\n" +
                    "空格键 - 发射子弹\n");
            
            Text goalTitle = new Text("游戏目标：");
            goalTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            
            Text goalText = new Text(
                    "1. 消灭所有敌方坦克\n" +
                    "2. 保护己方基地不被摧毁\n" +
                    "3. 收集道具提升能力\n\n炸弹使用：拾取炸弹道具后，按E键放置炸弹，5秒后爆炸");
            
            instructionsContent.getChildren().addAll(
                    controlsTitle, controlsText, 
                    new Region(), // 间隔
                    goalTitle, goalText);
            
            content.setBody(instructionsContent);
            
            JFXButton closeButton = new JFXButton("关闭");
            closeButton.setButtonType(JFXButton.ButtonType.RAISED);
            closeButton.setStyle("-fx-background-color: " + toHexString(PRIMARY_COLOR) + ";");
            content.setActions(closeButton);
            
            JFXDialog dialog = new JFXDialog(root, content, JFXDialog.DialogTransition.CENTER);
            
            closeButton.setOnAction(e -> dialog.close());
            
            dialog.show();
        });
    }
    
    /**
     * 显示消息对话框
     */
    private void showMessage(String message) {
        Platform.runLater(() -> {
            JFXDialogLayout content = new JFXDialogLayout();
            content.setHeading(new Text("消息"));
            content.setBody(new Text(message));
            
            JFXButton closeButton = new JFXButton("确定");
            closeButton.setButtonType(JFXButton.ButtonType.RAISED);
            closeButton.setStyle("-fx-background-color: " + toHexString(PRIMARY_COLOR) + ";");
            content.setActions(closeButton);
            
            JFXDialog dialog = new JFXDialog(root, content, JFXDialog.DialogTransition.CENTER);
            
            closeButton.setOnAction(e -> dialog.close());
            
            dialog.show();
        });
    }
    
    /**
     * 创建菜单按钮
     */
    private JFXButton createMenuButton(String text, javafx.event.EventHandler<javafx.event.ActionEvent> action) {
        JFXButton button = new JFXButton(text);
        button.getStyleClass().add("menu-button");
        button.setPrefWidth(300);
        button.setPrefHeight(50);
        button.setButtonType(JFXButton.ButtonType.RAISED);
        button.setStyle("-fx-background-color: " + toHexString(PRIMARY_COLOR) + ";" +
                        "-fx-background-radius: 25;");
        button.setTextFill(TEXT_COLOR);
        button.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        button.setOnAction(action);
        
        // 添加按钮阴影效果
        DropShadow dropShadow = new DropShadow();
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.5));
        dropShadow.setRadius(10);
        dropShadow.setOffsetX(0);
        dropShadow.setOffsetY(3);
        button.setEffect(dropShadow);
        
        return button;
    }
    
    /**
     * 将Color对象转换为CSS hex字符串
     */
    private String toHexString(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }
    
    /**
     * 显示暂停菜单
     */
    private void showPauseMenu() {
        if (isPauseMenuOpen) return; // 防止重复打开
        
        Platform.runLater(() -> {
            isPauseMenuOpen = true;
            gamePaused = true;
            
            // 创建半透明背景
            Rectangle overlay = new Rectangle(
                    scene.getWidth(), 
                    scene.getHeight(), 
                    Color.rgb(0, 0, 0, 0.7));
            
            // 创建暂停菜单容器
            VBox pauseMenu = new VBox(15);
            pauseMenu.setAlignment(Pos.CENTER);
            pauseMenu.setPadding(new Insets(30));
            pauseMenu.setMaxWidth(400);
            pauseMenu.setStyle("-fx-background-color: rgba(40, 60, 80, 0.9); " +
                             "-fx-background-radius: 10; " +
                             "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.5), 10, 0, 0, 5);");
            
            // 暂停标题
            Text pauseTitle = new Text("游戏已暂停");
            pauseTitle.setFont(Font.font("Arial", FontWeight.BOLD, 28));
            pauseTitle.setFill(PRIMARY_COLOR);
            
            // 创建菜单按钮
            JFXButton resumeButton = createPauseMenuButton("继续游戏", e -> closePauseMenu());
            JFXButton restartButton = createPauseMenuButton("重新开始", e -> restartGame());
            JFXButton saveButton = createPauseMenuButton("保存进度", e -> saveGame());
            JFXButton settingsButton = createPauseMenuButton("设置", e -> showSettings());
            JFXButton mainMenuButton = createPauseMenuButton("返回主菜单", e -> confirmReturnToMainMenu());
            JFXButton exitButton = createPauseMenuButton("退出游戏", e -> confirmExitGame());
            
            // 将组件添加到菜单中
            pauseMenu.getChildren().addAll(
                    pauseTitle,
                    new Separator(),
                    resumeButton,
                    restartButton,
                    saveButton,
                    settingsButton,
                    mainMenuButton,
                    exitButton
            );
            
            // 创建菜单场景
            StackPane pauseRoot = new StackPane(overlay, pauseMenu);
            pauseRoot.setAlignment(Pos.CENTER);
            
            // 添加到根布局
            root.getChildren().add(pauseRoot);
            
            // 确保焦点在菜单上，避免按键继续控制游戏
            pauseMenu.requestFocus();
        });
    }
    
    /**
     * 关闭暂停菜单
     */
    private void closePauseMenu() {
        Platform.runLater(() -> {
            // 移除最后添加的子节点（暂停菜单）
            if (root.getChildren().size() > 1) {
                root.getChildren().remove(root.getChildren().size() - 1);
            }
            
            isPauseMenuOpen = false;
            gamePaused = false;
        });
    }
    
    /**
     * 重新开始当前关卡 - 加强版
     */
    private void restartGame() {
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
            
            // 完全重新创建游戏控制器和游戏状态
            gameController = new GameController();
            gameController.loadLevel(currentLevel);
            gameController.setPlayerTankType(tankType);
            
            // 重置生命数
            playerLives = 3;
            
            // 确保游戏UI元素完全重建
            root.getChildren().clear(); // 确保UI完全清除
            
            // 显示全新的游戏画面，包含全新的Canvas对象
            showGameScreen();
            
            System.out.println("游戏已完全重置，创建了新的Canvas和键盘监听器");
        });
    }
    
    /**
     * 保存游戏进度
     */
    private void saveGame() {
        Platform.runLater(() -> {
            // 这里实现保存逻辑
            showMessage("游戏进度已保存");
            closePauseMenu();
        });
    }
    
    /**
     * 显示设置菜单 - 优化版本
     */
    private void showSettings() {
        Platform.runLater(() -> {
            // 关闭暂停菜单但保持游戏暂停状态
            if (root.getChildren().size() > 1) {
                root.getChildren().remove(root.getChildren().size() - 1);
            }
            isPauseMenuOpen = false;
            // 注意：不改变gamePaused状态
            
            // 显示设置对话框
            JFXDialogLayout content = new JFXDialogLayout();
            content.setHeading(new Text("游戏设置"));
            
            VBox settingsContent = new VBox(15);
            settingsContent.setAlignment(Pos.CENTER);
            
            // 在这里添加设置选项
            Label audioLabel = new Label("音量");
            JFXSlider volumeSlider = new JFXSlider(0, 100, 50);
            volumeSlider.setIndicatorPosition(JFXSlider.IndicatorPosition.RIGHT);
            
            Label difficultyLabel = new Label("难度");
            HBox difficultyOptions = new HBox(10);
            difficultyOptions.setAlignment(Pos.CENTER);
            
            JFXRadioButton easyBtn = new JFXRadioButton("简单");
            JFXRadioButton normalBtn = new JFXRadioButton("普通");
            JFXRadioButton hardBtn = new JFXRadioButton("困难");
            
            ToggleGroup difficultyGroup = new ToggleGroup();
            easyBtn.setToggleGroup(difficultyGroup);
            normalBtn.setToggleGroup(difficultyGroup);
            hardBtn.setToggleGroup(difficultyGroup);
            normalBtn.setSelected(true);
            
            difficultyOptions.getChildren().addAll(easyBtn, normalBtn, hardBtn);
            
            settingsContent.getChildren().addAll(
                    audioLabel, volumeSlider,
                    new Separator(),
                    difficultyLabel, difficultyOptions
            );
            
            content.setBody(settingsContent);
            
            JFXButton closeButton = new JFXButton("关闭");
            closeButton.setButtonType(JFXButton.ButtonType.RAISED);
            closeButton.setStyle("-fx-background-color: " + toHexString(PRIMARY_COLOR) + ";");
            closeButton.setTextFill(TEXT_COLOR);
            content.setActions(closeButton);
            
            JFXDialog dialog = new JFXDialog(root, content, JFXDialog.DialogTransition.CENTER);
            
            closeButton.setOnAction(e -> {
                dialog.close();
                // 关闭设置对话框后重新显示暂停菜单
                showPauseMenu();
            });
            
            dialog.show();
        });
    }
    
    /**
     * 确认返回主菜单
     */
    private void confirmReturnToMainMenu() {
        // 创建确认对话框
        JFXDialogLayout content = new JFXDialogLayout();
        content.setHeading(new Text("返回主菜单"));
        content.setBody(new Text("确定要返回主菜单吗？当前游戏进度将丢失。"));
        
        JFXDialog dialog = new JFXDialog(root, content, JFXDialog.DialogTransition.CENTER);
        
        JFXButton cancelButton = new JFXButton("取消");
        cancelButton.setOnAction(e -> dialog.close());
        
        JFXButton confirmButton = new JFXButton("确定");
        confirmButton.setOnAction(e -> {
            dialog.close();
            
            // 先停止游戏循环
            if (gameLoop != null) {
                gameLoop.stop();
            }
            
            // 清理资源
            cleanupGameResources();
            
            // 确保在完全清理后再切换到主菜单
            Platform.runLater(() -> {
                showMainMenu();
            });
        });
        
        content.setActions(cancelButton, confirmButton);
        dialog.show();
    }
    
    /**
     * 确认退出游戏
     */
    private void confirmExitGame() {
        Platform.runLater(() -> {
            JFXDialogLayout content = new JFXDialogLayout();
            content.setHeading(new Text("退出游戏"));
            content.setBody(new Text("确定要退出游戏吗？未保存的进度将丢失。"));
            
            // 明确设置取消按钮样式确保可见
            JFXButton cancelButton = new JFXButton("取消");
            cancelButton.setPrefWidth(100);
            cancelButton.setPrefHeight(40);
            cancelButton.setButtonType(JFXButton.ButtonType.RAISED);
            cancelButton.setStyle("-fx-background-color: #4d4d4d; -fx-text-fill: white;");
            
            JFXButton confirmButton = new JFXButton("确定");
            confirmButton.setPrefWidth(100);
            confirmButton.setPrefHeight(40);
            confirmButton.setButtonType(JFXButton.ButtonType.RAISED);
            confirmButton.setStyle("-fx-background-color: " + toHexString(SECONDARY_COLOR) + "; -fx-text-fill: white;");
            
            HBox buttonBox = new HBox(20);
            buttonBox.setAlignment(Pos.CENTER);
            buttonBox.getChildren().addAll(cancelButton, confirmButton);
            
            content.setActions(buttonBox);
            
            JFXDialog dialog = new JFXDialog(root, content, JFXDialog.DialogTransition.CENTER);
            
            cancelButton.setOnAction(e -> dialog.close());
            confirmButton.setOnAction(e -> {
                dialog.close();
                Platform.exit();
            });
            
            dialog.show();
        });
    }
    
    /**
     * 创建暂停菜单按钮
     */
    private JFXButton createPauseMenuButton(String text, javafx.event.EventHandler<javafx.event.ActionEvent> action) {
        JFXButton button = new JFXButton(text);
        button.getStyleClass().add("pause-menu-button");
        button.setPrefWidth(250);
        button.setPrefHeight(40);
        button.setButtonType(JFXButton.ButtonType.RAISED);
        button.setStyle("-fx-background-color: rgba(60, 80, 100, 0.8);" +
                        "-fx-text-fill: white;");
        button.setTextFill(TEXT_COLOR);
        button.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        button.setOnAction(action);
        
        // 添加悬停效果
        button.setOnMouseEntered(e -> 
            button.setStyle("-fx-background-color: " + toHexString(PRIMARY_COLOR) + ";" +
                            "-fx-text-fill: white;"));
        
        button.setOnMouseExited(e -> 
            button.setStyle("-fx-background-color: rgba(60, 80, 100, 0.8);" +
                            "-fx-text-fill: white;"));
        
        return button;
    }
    
    // 添加子弹显示更新方法
    private void updateBulletDisplay() {
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
    
    // 修改cleanupGameResources方法，确保停止游戏循环
    private void cleanupGameResources() {
        // 停止游戏循环
        if (gameLoop != null) {
            gameLoop.stop();
            gameLoop = null;
        }
        
        // 清除所有事件监听器
        if (gameCanvas != null) {
            gameCanvas.setOnKeyPressed(null);
            gameCanvas.setOnKeyReleased(null);
            gameCanvas.removeEventHandler(javafx.scene.input.KeyEvent.KEY_PRESSED, event -> {});
            gameCanvas.removeEventHandler(javafx.scene.input.KeyEvent.KEY_RELEASED, event -> {});
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
            gameLoop = null;  // 将引用设为null以便垃圾回收
        }
    }
    
    /**
     * 创建生命数显示区域
     */
    private HBox createLivesDisplay() {
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
    
    // 复原增益效果显示
    private HBox createPowerUpDisplay() {
        HBox powerUpBox = new HBox(10);
        powerUpBox.setAlignment(Pos.CENTER_LEFT);
        powerUpBox.setPadding(new Insets(5));
        powerUpBox.setMinWidth(200); // 设置固定最小宽度
        powerUpBox.setPrefWidth(200); // 设置固定首选宽度
        
        // 创建标题
        Label powerUpLabel = new Label("增益效果");
        powerUpLabel.setTextFill(PRIMARY_COLOR); // 使用蓝色
        powerUpLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        // 创建一个VBox包含标题和内容
        VBox infoBox = new VBox(5);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        
        // 创建一个HBox用于放置增益效果图标
        HBox effectIcons = new HBox(10);
        effectIcons.setAlignment(Pos.CENTER_LEFT);
        
        // 将标题和增益效果图标添加到VBox中
        infoBox.getChildren().addAll(powerUpLabel, effectIcons);
        powerUpBox.getChildren().add(infoBox);
        
        // 清空并重新创建增益效果指示器Map
        powerUpIndicators.clear();
        
        return powerUpBox;
    }
    
    // 更新增益效果显示
    private void updatePowerUpDisplay() {
        if (gameController == null || gameController.getPlayerTank() == null) return;
        
        // 添加调试输出
        Tank playerTank = gameController.getPlayerTank();
        Map<Tank.PowerUpType, Double> activeEffects = playerTank.getActiveEffects();

        
        // 原有的更新逻辑...
    }
    
    /**
     * 处理玩家坦克被摧毁的情况 - 修正版本
     */
    private void handlePlayerDestroyed() {
        if (gameController == null) return;
        
        // 避免多次调用此方法，检查玩家坦克是否已经处理
        Tank playerTank = gameController.getPlayerTank();
        if (playerTank == null || !playerTank.isDead()) return;
        
        // 减少玩家生命值
        playerLives--;
        System.out.println("玩家失去一条生命，剩余: " + playerLives);
        
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
     * 复活玩家坦克 - 简化直接版本
     */
    private void respawnPlayer() {
        if (playerLives > 0) {
            // 寻找有效的重生位置
        LevelMap.MapPosition spawnPos = gameController.findValidSpawnPosition();
        if (spawnPos != null) {
                // 使用当前选择的坦克类型重生
                String currentTankType = TANK_TYPES.get(selectedTankType);
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
            StackPane gameArea = (StackPane)gameCanvas.getParent();
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
        Platform.runLater(() -> {
            // 暂停游戏
            gamePaused = true;
            
            // 停止游戏循环
            if (gameLoop != null) {
                gameLoop.stop();
            }
            
            // 创建半透明背景
            Rectangle overlay = new Rectangle(
                    scene.getWidth(), 
                    scene.getHeight(), 
                    Color.rgb(0, 0, 0, 0.8));
            
            // 创建游戏结束信息
            Text gameOverText = new Text("GAME OVER");
            gameOverText.setFont(Font.font("Impact", FontWeight.BOLD, 72));
            gameOverText.setFill(Color.RED);
            gameOverText.setStroke(Color.BLACK);
            gameOverText.setStrokeWidth(2);
            
            Text scoreText = new Text("得分: " + calculateScore());
            scoreText.setFont(Font.font("Arial", FontWeight.BOLD, 36));
            scoreText.setFill(Color.WHITE);
            
            // 创建按钮
            JFXButton retryButton = createMenuButton("重新挑战", e -> restartGame());
            JFXButton mainMenuButton = createMenuButton("返回主菜单", e -> {
                cleanupGameResources();
                showMainMenu();
            });
            
            // 创建布局
            VBox gameOverMenu = new VBox(30);
            gameOverMenu.setAlignment(Pos.CENTER);
            gameOverMenu.getChildren().addAll(
                    gameOverText,
                    scoreText,
                    new Separator(),
                    retryButton,
                    mainMenuButton
            );
            
            // 创建场景
            StackPane gameOverRoot = new StackPane(overlay, gameOverMenu);
            gameOverRoot.setAlignment(Pos.CENTER);
            
            // 添加到根布局
            root.getChildren().add(gameOverRoot);
        });
    }

    /**
     * 计算游戏得分
     */
    private int calculateScore() {
        if (gameController == null) return 0;
        
        // 根据关卡、击败敌人数量和剩余时间计算得分
        int levelScore = gameController.getCurrentLevel() * 1000;
        int enemyScore = gameController.getDefeatedEnemiesCount() * 200;
        int timeScore = Math.max(0, 300000 - (int)totalGameTime) / 1000;
        
        return levelScore + enemyScore + timeScore;
    }
    
    // 更新敌人数量显示
    private void updateEnemiesDisplay() {
        if (gameController == null || gameDataPanel == null) return;
        
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
    private void updateLivesDisplay() {
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
                                ImageView heartIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/ui/heart.png")));
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
        // 如果已经显示了消息，不重复显示
        if (root.lookup("#levelCompletedMessage") != null) return;
        
        // 暂停游戏
        gamePaused = true;
        
        // 创建消息框容器
        StackPane containerPane = new StackPane();
        containerPane.setId("levelCompletedMessage");
        
        // 添加全屏暗色背景
        Rectangle darkOverlay = new Rectangle(scene.getWidth(), scene.getHeight());
        darkOverlay.setFill(Color.rgb(0, 0, 0, 0.7));
        
        // 创建主要内容面板
        VBox messageBox = new VBox(20);
        messageBox.setAlignment(Pos.CENTER);
        messageBox.setPadding(new Insets(40));
        messageBox.setMaxWidth(550);
        messageBox.setMaxHeight(620);
        
        // 设置渐变背景和边框效果
        messageBox.setStyle("-fx-background-color: linear-gradient(to bottom, rgba(20, 40, 80, 0.95), rgba(10, 20, 40, 0.95));" +
                            "-fx-background-radius: 15;" +
                            "-fx-border-color: linear-gradient(to bottom, " + toHexString(PRIMARY_COLOR) + "80, " + toHexString(SECONDARY_COLOR) + "80);" +
                            "-fx-border-width: 2;" +
                            "-fx-border-radius: 15;" + 
                            "-fx-effect: dropshadow(gaussian, rgba(0, 150, 255, 0.5), 20, 0, 0, 0);");
        
        // 添加顶部光效装饰
        Rectangle topGlow = new Rectangle(500, 4);
        topGlow.setArcWidth(4);
        topGlow.setArcHeight(4);
        topGlow.setFill(Color.rgb(100, 200, 255, 0.7));
        // 修复错误: 直接使用GaussianBlur而不是尝试实例化BlurType枚举
        GaussianBlur glow = new GaussianBlur(10);
        topGlow.setEffect(glow);
        
        // 标题区域
        Text victoryText = new Text("任务完成");
        victoryText.setFont(Font.font("Arial", FontWeight.BLACK, 42));
        victoryText.setFill(Color.WHITE);
        
        // 添加金色描边
        victoryText.setStroke(Color.rgb(255, 215, 0, 0.8));
        victoryText.setStrokeWidth(1.5);
        
        // 添加文字阴影
        DropShadow textShadow = new DropShadow();
        textShadow.setColor(Color.rgb(0, 150, 255, 0.7));
        textShadow.setRadius(15);
        textShadow.setSpread(0.4);
        victoryText.setEffect(textShadow);
        
        // 计算各项得分
        int currentLevel = gameController.getCurrentLevel();
        int defeatedEnemies = gameController.getDefeatedEnemiesCount();
        int enemyScore = defeatedEnemies * 200;
        int levelScore = currentLevel * 1000;
        
        // 通关时间计算
        long seconds = totalGameTime / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        String timeString = String.format("%02d:%02d", minutes, seconds);
        
        // 时间得分（越快越高）
        int timeScore = Math.max(0, 300000 - (int)totalGameTime) / 1000;
        
        // 生命得分
        int lifeScore = playerLives * 500;
        
        // 总分
        int totalScore = enemyScore + levelScore + timeScore + lifeScore;
        
        // 创建详细统计卡片
        VBox statsCard = createStatsCard(
            currentLevel, 
            defeatedEnemies, enemyScore,
            timeString, timeScore,
            playerLives, lifeScore,
            totalScore
        );
        
        // 按钮容器
        HBox buttonContainer = new HBox(30);
        buttonContainer.setAlignment(Pos.CENTER);
        
        // 创建按钮
        int totalLevels = 5;
        JFXButton menuButton = createActionButton("返回主菜单", false);
        menuButton.setOnAction(e -> {
            cleanupGameResources();
            showMainMenu();
        });
        
        if (currentLevel < totalLevels) {
            JFXButton nextButton = createActionButton("下一关", true);
            nextButton.setOnAction(e -> {
                gamePaused = false;
                root.getChildren().remove(containerPane);
                startGameWithLevel(gameController.getPlayerTank().getTypeString(), currentLevel + 1);
            });
            buttonContainer.getChildren().addAll(menuButton, nextButton);
        } else {
            // 最终关卡通关
            Text completionText = new Text("恭喜你已完成所有关卡!");
            completionText.setFont(Font.font("Arial", FontWeight.BOLD, 22));
            completionText.setFill(Color.GOLD);
            completionText.setStroke(Color.rgb(100, 100, 100, 0.3));
            completionText.setStrokeWidth(0.5);
            statsCard.getChildren().add(0, completionText);
            statsCard.getChildren().add(1, new Region());
            buttonContainer.getChildren().add(menuButton);
        }
        
        // 添加所有元素到主容器
        messageBox.getChildren().addAll(
            victoryText,
            new Separator(),
            statsCard,
            new Separator(),
            buttonContainer
        );
        
        // 构建最终布局
        containerPane.getChildren().addAll(darkOverlay, messageBox);
        root.getChildren().add(containerPane);
        
        // 添加进入动画效果
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(400), messageBox);
        scaleIn.setFromX(0.7);
        scaleIn.setFromY(0.7);
        scaleIn.setToX(1.0);
        scaleIn.setToY(1.0);
        
        FadeTransition fadeIn = new FadeTransition(Duration.millis(400), messageBox);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        
        ParallelTransition enterAnimation = new ParallelTransition(scaleIn, fadeIn);
        enterAnimation.play();
    }

    // 创建美观的统计卡片面板
    private VBox createStatsCard(int level, int enemies, int enemyScore, 
                                String time, int timeScore, 
                                int lives, int lifeScore, 
                                int totalScore) {
        // 创建主容器
        VBox card = new VBox(12);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(25, 20, 25, 20));
        card.setStyle("-fx-background-color: rgba(0, 30, 60, 0.7);" +
                      "-fx-background-radius: 10;" +
                      "-fx-border-color: rgba(100, 150, 255, 0.4);" +
                      "-fx-border-width: 1;" +
                      "-fx-border-radius: 10;");
        
        // 标题行
        Text statsTitle = new Text("战斗统计");
        statsTitle.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        statsTitle.setFill(Color.rgb(180, 220, 255));
        
        // 创建漂亮的统计行
        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(15);
        statsGrid.setVgap(20);
        statsGrid.setAlignment(Pos.CENTER);
        
        // 计算关卡分数
        int levelScore = level * 1000;
        
        // 关卡奖励
        addStatRow(statsGrid, 0, "关卡奖励", level + "关 × 1000", levelScore, null);
        
        // 击败敌人 - 带图标
        ImageView enemyIcon = createIcon("/images/ui/enemy_icon.png", "⚔");
        addStatRow(statsGrid, 1, "击败敌人", enemies + "个 × 200", enemyScore, enemyIcon);
        
        // 通关时间 - 带图标
        ImageView clockIcon = createIcon("/images/ui/clock_icon.png", "⏱");
        addStatRow(statsGrid, 2, "通关时间", time, timeScore, clockIcon);
        
        // 剩余生命 - 带图标
        ImageView heartIcon = createIcon("/images/ui/heart.png", "♥");
        addStatRow(statsGrid, 3, "剩余生命", lives + "条 × 500", lifeScore, heartIcon);
        
        // 分隔线
        Separator sep = new Separator();
        sep.setPrefWidth(400);
        
        // 总分显示
        HBox totalScoreBox = new HBox(10);
        totalScoreBox.setAlignment(Pos.CENTER);
        
        Text totalLabel = new Text("总积分");
        totalLabel.setFont(Font.font("Arial", FontWeight.BOLD, 26));
        totalLabel.setFill(Color.WHITE);
        
        Text totalValue = new Text(Integer.toString(totalScore));
        totalValue.setFont(Font.font("Arial", FontWeight.BLACK, 40));
        totalValue.setFill(Color.rgb(255, 215, 0));
        
        // 添加金色光效
        DropShadow goldGlow = new DropShadow();
        goldGlow.setColor(Color.rgb(255, 200, 0, 0.7));
        goldGlow.setRadius(10);
        goldGlow.setSpread(0.2);
        totalValue.setEffect(goldGlow);
        
        totalScoreBox.getChildren().addAll(totalLabel, totalValue);
        
        // 组装卡片
        card.getChildren().addAll(statsTitle, new Separator(), statsGrid, sep, totalScoreBox);
        
        return card;
    }

    // 添加统计行
    private void addStatRow(GridPane grid, int row, String label, String value, int points, ImageView icon) {
        // 标签
        Text labelText = new Text(label);
        labelText.setFont(Font.font("Arial", FontWeight.MEDIUM, 18));
        labelText.setFill(Color.rgb(200, 220, 255));
        
        // 值
        Text valueText = new Text(value);
        valueText.setFont(Font.font("Arial", FontWeight.NORMAL, 18));
        valueText.setFill(Color.WHITE);
        
        // 分数
        Text pointsText = new Text(points + " 分");
        pointsText.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        pointsText.setFill(Color.rgb(255, 220, 100));
        
        // 添加到网格
        int col = 0;
        if (icon != null) {
            grid.add(icon, col, row);
        } else {
            // 如果没有图标，添加一个空的占位区域保持对齐
            Region placeholder = new Region();
            placeholder.setMinWidth(24);
            placeholder.setPrefWidth(24);
            grid.add(placeholder, col, row);
        }
        col++;
        
        grid.add(labelText, col++, row);
        grid.add(valueText, col++, row);
        grid.add(pointsText, col, row);
        
        // 设置列约束 - 只在第一行设置
        if (row == 0) {
            ColumnConstraints col1 = new ColumnConstraints();
            col1.setHalignment(javafx.geometry.HPos.LEFT);
            col1.setMinWidth(30);  // 确保图标列宽度一致
            
            ColumnConstraints col2 = new ColumnConstraints();
            col2.setHalignment(javafx.geometry.HPos.LEFT);
            col2.setMinWidth(120);
            
            ColumnConstraints col3 = new ColumnConstraints();
            col3.setHalignment(javafx.geometry.HPos.LEFT);
            col3.setPrefWidth(150);
            
            ColumnConstraints col4 = new ColumnConstraints();
            col4.setHalignment(javafx.geometry.HPos.RIGHT);
            col4.setPrefWidth(100);
            
            grid.getColumnConstraints().addAll(col1, col2, col3, col4);
        }
    }

    // 创建统计行图标
    private ImageView createIcon(String path, String fallbackText) {
        try {
            Image img = new Image(getClass().getResourceAsStream(path));
            ImageView icon = new ImageView(img);
            icon.setFitWidth(24);
            icon.setFitHeight(24);
            return icon;
        } catch (Exception e) {
            // 如果图标加载失败，使用文本替代
            Text text = new Text(fallbackText);
            text.setFont(Font.font("Arial", FontWeight.BOLD, 18));
            text.setFill(Color.LIGHTSKYBLUE);
            
            StackPane iconPane = new StackPane(text);
            iconPane.setMinSize(24, 24);
            
            // 转换为ImageView (实际上不是真正的ImageView，但作为占位符使用)
            return new ImageView();
        }
    }

    // 创建漂亮的按钮
    private JFXButton createActionButton(String text, boolean isPrimary) {
        JFXButton button = new JFXButton(text);
        button.setPrefWidth(180);
        button.setPrefHeight(50);
        button.setButtonType(JFXButton.ButtonType.RAISED);
        
        // 设置按钮样式
        if (isPrimary) {
            // 主要按钮 - 亮色
            button.setStyle(
                "-fx-background-color: linear-gradient(to bottom, " + toHexString(SECONDARY_COLOR) + ", #2D8D31);" +
                "-fx-background-radius: 25;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 18px;" +
                "-fx-font-weight: bold;" +
                "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.4), 8, 0, 0, 2);"
            );
        } else {
            // 次要按钮 - 较暗色
            button.setStyle(
                "-fx-background-color: linear-gradient(to bottom, " + toHexString(PRIMARY_COLOR) + ", #1A7CB8);" +
                "-fx-background-radius: 25;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 18px;" +
                "-fx-font-weight: bold;" +
                "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.4), 8, 0, 0, 2);"
            );
        }
        
        // 添加悬停效果
        button.setOnMouseEntered(e -> {
            if (isPrimary) {
                button.setStyle(
                    "-fx-background-color: linear-gradient(to bottom, #4CAF50, #388E3C);" +
                    "-fx-background-radius: 25;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 18px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.6), 12, 0, 0, 3);"
                );
            } else {
                button.setStyle(
                    "-fx-background-color: linear-gradient(to bottom, #2196F3, #1976D2);" +
                    "-fx-background-radius: 25;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 18px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.6), 12, 0, 0, 3);"
                );
            }
        });
        
        // 恢复原始样式
        button.setOnMouseExited(e -> {
            if (isPrimary) {
                button.setStyle(
                    "-fx-background-color: linear-gradient(to bottom, " + toHexString(SECONDARY_COLOR) + ", #2D8D31);" +
                    "-fx-background-radius: 25;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 18px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.4), 8, 0, 0, 2);"
                );
            } else {
                button.setStyle(
                    "-fx-background-color: linear-gradient(to bottom, " + toHexString(PRIMARY_COLOR) + ", #1A7CB8);" +
                    "-fx-background-radius: 25;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 18px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.4), 8, 0, 0, 2);"
                );
            }
        });
        
        return button;
    }

    // 初始化增益效果UI
    private void initializePowerUpUI() {
        HBox powerUpContainer = new HBox(10); // 10像素间距
        powerUpContainer.setAlignment(Pos.CENTER);
        
        // 为每种增益效果创建显示组件
        for (Tank.PowerUpType type : Tank.PowerUpType.values()) {
            String typeName = type.getName();
            
            // 创建包含图标和进度条的VBox
            VBox powerUpBox = new VBox(5);
            powerUpBox.setAlignment(Pos.CENTER);
            
            // 创建图标
            ImageView iconView = new ImageView();
            iconView.setFitWidth(30);
            iconView.setFitHeight(30);
            Image icon = gameController.getPowerUpImage(typeName);
            if (icon != null) {
                iconView.setImage(icon);
            }
            iconView.setVisible(false); // 默认不可见
            
            // 创建进度条
            ProgressBar progressBar = new ProgressBar(1.0);
            progressBar.setPrefWidth(30);
            progressBar.setPrefHeight(5);
            progressBar.setVisible(false);
            
            // 添加到映射中以便更新
            powerUpIndicators.put(typeName, iconView);
            powerUpProgressBars.put(typeName, progressBar);
            
            // 添加到布局
            powerUpBox.getChildren().addAll(iconView, progressBar);
            powerUpContainer.getChildren().add(powerUpBox);
        }
        
        // 添加到游戏界面
        gameDataPanel.getChildren().add(powerUpContainer);
    }

    // 修改创建增益效果状态栏的方法
    private HBox createPowerUpStatusBar() {
        // 创建一个紧凑的HBox容器
        HBox powerUpBar = new HBox(8);
        powerUpBar.setId("powerUpStatusBar");
        powerUpBar.setPadding(new Insets(5, 8, 5, 8));
        powerUpBar.setAlignment(Pos.CENTER_LEFT);
        powerUpBar.setStyle("-fx-background-color: rgba(0, 20, 40, 0.7); -fx-border-color: #00AAFF; -fx-border-radius: 4;");
        
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
        
        // 为每种增益效果创建图标，但不包括即时效果(HEALTH)
        for (Tank.PowerUpType type : Tank.PowerUpType.values()) {
            if (type == Tank.PowerUpType.HEALTH) continue;
            
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
            
            // 尝试加载图标
            try {
                Image icon = gameController.getPowerUpImage(typeName);
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
        if (gameController == null || gameController.getPlayerTank() == null) return;
        
        Tank playerTank = gameController.getPlayerTank();
        Map<Tank.PowerUpType, Double> activeEffects = playerTank.getActiveEffects();
        
        // 更新每种效果的显示状态
        for (Tank.PowerUpType type : Tank.PowerUpType.values()) {
            if (type == Tank.PowerUpType.HEALTH) continue; // 跳过生命恢复
            
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
}

