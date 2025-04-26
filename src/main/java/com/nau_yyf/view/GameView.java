package com.nau_yyf.view;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import com.nau_yyf.controller.GameController;
import com.nau_yyf.model.Bullet;
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
import com.nau_yyf.util.SVGLoader;
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

import java.util.Arrays;
import java.util.List;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

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
        // 先彻底清理资源
        cleanupGameResources();
        
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
        
        // 创建全新的游戏控制器
        gameController = new GameController();
        
        // 加载指定关卡
        gameController.loadLevel(level);
        
        // 设置正确的坦克类型 - 这会创建新的Tank对象
        gameController.setPlayerTankType(selectedTankType);
        
        // 显示全新的游戏界面
        root.getChildren().clear(); // 确保完全清除UI
        showGameScreen();
        
        // 确保画布获取焦点
        Platform.runLater(() -> {
            if (gameCanvas != null) {
                gameCanvas.requestFocus();
            }
        });
    }
    
    private void showGameScreen() {
        // 清除当前内容
        root.getChildren().clear();
        
        // 创建游戏主布局
        BorderPane gameLayout = new BorderPane();
        gameLayout.setStyle("-fx-background-color: #1a2634;");
        
        // ---------- 顶部区域 ----------
        HBox topInfoBar = new HBox(20);
        topInfoBar.setAlignment(Pos.CENTER_LEFT);
        topInfoBar.setPadding(new Insets(10, 20, 10, 20));
        topInfoBar.setStyle("-fx-background-color: rgba(0, 0, 0, 0.4); -fx-background-radius: 5;");
        
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
        settingsButton.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5); -fx-background-radius: 5;");
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
        
        StackPane canvasHolder = new StackPane(gameCanvas);
        canvasHolder.setAlignment(Pos.CENTER);
        canvasHolder.setStyle("-fx-border-color: #37a0da; -fx-border-width: 3; -fx-background-color: #000;");
        
        // ---------- 底部数据面板 ----------
        gameDataPanel = new HBox(40);
        gameDataPanel.setAlignment(Pos.CENTER);
        gameDataPanel.setPadding(new Insets(15));
        gameDataPanel.setStyle("-fx-background-color: rgba(0, 0, 0, 0.6); -fx-background-radius: 5;");
        
        VBox playerInfo = createInfoBox("坦克类型", gameController.getPlayerTank().getTypeString());
        VBox healthInfo = createInfoBox("血量", Integer.toString(gameController.getPlayerTank().getHealth()));
        VBox bulletInfo = createInfoBox("子弹", Integer.toString(bulletCount));
        VBox enemiesInfo = createInfoBox("剩余敌人", Integer.toString(gameController.getRemainingEnemies()));
        
        // 添加生命数和增益效果显示
        HBox livesDisplay = createLivesDisplay();
        HBox powerUpDisplay = createPowerUpDisplay();
        
        gameDataPanel.getChildren().addAll(playerInfo, healthInfo, bulletInfo, enemiesInfo, livesDisplay, powerUpDisplay);
        
        // 将各部分添加到布局
        gameLayout.setTop(topInfoBar);
        gameLayout.setCenter(canvasHolder);
        gameLayout.setBottom(gameDataPanel);
        
        BorderPane.setMargin(canvasHolder, new Insets(10, 20, 10, 20));
        
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
        
        System.out.println("键盘监听器设置完成");
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
            }
        };
        gameLoop.start();
    }
    
    // 新增方法：固定时间步长逻辑更新
    private void updateGame(double deltaTime) {
        if (!gamePaused) {
            // 处理玩家输入
            handlePlayerInput();
            
            // 更新游戏控制器中的状态
            if (gameController != null) {
                gameController.updateBullets();
                gameController.updateEnemyTanks();
                
                // 更新坦克特效状态
                if (gameController.getPlayerTank() != null) {
                    for (Tank.PowerUpType effect : gameController.getPlayerTank().getActiveEffects().keySet()) {
                        gameController.getPlayerTank().updateEffects(deltaTime);
                    }
                    
                    // 添加子弹恢复逻辑
                    updateBulletRefill(deltaTime);
                }
            }
            
            // 更新其他游戏状态...
        }
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
                    refillDelay = 1500; // 轻型坦克恢复最快：1.5秒
                    break;
                case HEAVY:
                    refillDelay = 3000; // 重型坦克恢复最慢：3秒
                    break;
                case STANDARD:
                default:
                    refillDelay = 2000; // 标准坦克：2秒
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
                
                System.out.println("子弹已恢复，当前数量: " + bulletCount);
            }
        } else if (bulletCount >= 10) {
            // 子弹已满，重置计时器
            lastBulletRefillTime = currentTime;
        }
    }
    
    // 渲染方法 - 每帧调用一次
    private void renderGame() {
        if (gameCanvas != null && gc != null) {
            // 清除画布
            gc.clearRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());
            
            // 渲染地图和游戏对象
            if (gameController != null) {
                gameController.renderMap(gc);
            }
        }
    }
    
    // 修改handlePlayerInput方法，增加null检查
    private void handlePlayerInput() {
        if (gameController == null) return;
        
        Tank playerTank = gameController.getPlayerTank();
        if (playerTank == null) return;
        
        boolean moved = false;
        
        // 根据按键状态处理移动方向
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
            playerTank.move(); // 使用Tank自身的移动方法
            
            // 边界检查
            if (playerTank.getX() < 0) {
                playerTank.setX(0);
            } else if (playerTank.getX() > gameCanvas.getWidth() - playerTank.getWidth()) {
                playerTank.setX((int)gameCanvas.getWidth() - playerTank.getWidth());
            }
            
            if (playerTank.getY() < 0) {
                playerTank.setY(0);
            } else if (playerTank.getY() > gameCanvas.getHeight() - playerTank.getHeight()) {
                playerTank.setY((int)gameCanvas.getHeight() - playerTank.getHeight());
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
        
        // 更新子弹状态
        gameController.updateBullets();
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
                    "3. 收集道具提升能力");
            
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
        Platform.runLater(() -> {
            JFXDialogLayout content = new JFXDialogLayout();
            content.setHeading(new Text("返回主菜单"));
            content.setBody(new Text("确定要返回主菜单吗？未保存的进度将丢失。"));
            
            JFXButton cancelButton = new JFXButton("取消");
            cancelButton.setButtonType(JFXButton.ButtonType.FLAT);
            cancelButton.setTextFill(TEXT_COLOR);
            cancelButton.setStyle("-fx-background-color: rgba(60, 80, 100, 0.8);");
            
            JFXButton confirmButton = new JFXButton("确定");
            confirmButton.setButtonType(JFXButton.ButtonType.RAISED);
            confirmButton.setStyle("-fx-background-color: " + toHexString(SECONDARY_COLOR) + ";");
            confirmButton.setTextFill(TEXT_COLOR);
            
            content.setActions(cancelButton, confirmButton);
            
            JFXDialog dialog = new JFXDialog(root, content, JFXDialog.DialogTransition.CENTER);
            
            cancelButton.setOnAction(e -> {
                dialog.close();
            });
            
            confirmButton.setOnAction(e -> {
                dialog.close();
                closePauseMenu();
                // 彻底清理游戏状态
                cleanupGameResources();
                showMainMenu();
            });
            
            dialog.show();
        });
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
    }
    
    /**
     * 创建生命数显示区域
     */
    private HBox createLivesDisplay() {
        HBox livesBox = new HBox(5);
        livesBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        Text livesLabel = new Text("生命:");
        livesLabel.setFill(TEXT_COLOR);
        livesLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        livesBox.getChildren().add(livesLabel);
        
        // 使用爱心图标而不是增加生命的图标
        for (int i = 0; i < playerLives; i++) {
            try {
                ImageView heartIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/ui/heart.png")));
                heartIcon.setFitWidth(20);
                heartIcon.setFitHeight(20);
                livesBox.getChildren().add(heartIcon);
            } catch (Exception e) {
                System.err.println("无法加载爱心图标: " + e.getMessage());
                // 如果图标加载失败，使用文字替代
                Text heartText = new Text("♥");
                heartText.setFill(Color.RED);
                heartText.setFont(Font.font("Arial", FontWeight.BOLD, 18));
                livesBox.getChildren().add(heartText);
            }
        }
        
        return livesBox;
    }
    
    /**
     * 创建增益效果显示区域
     */
    private HBox createPowerUpDisplay() {
        HBox powerUpBox = new HBox(10);
        powerUpBox.setAlignment(Pos.CENTER);
        
        Text powerUpLabel = new Text("增益效果:");
        powerUpLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        powerUpLabel.setFill(PRIMARY_COLOR);
        
        HBox powerUpContainer = new HBox(5);
        powerUpContainer.setAlignment(Pos.CENTER);
        powerUpContainer.setPrefHeight(30);
        
        // 添加所有可能的增益效果图标，但初始时设为不可见
        for (Tank.PowerUpType type : Tank.PowerUpType.values()) {
            try {
                String imagePath = "/images/powerups/" + type.getName() + ".png";
                Image powerUpImage = new Image(getClass().getResourceAsStream(imagePath));
                ImageView powerUpView = new ImageView(powerUpImage);
                powerUpView.setFitWidth(25);
                powerUpView.setFitHeight(25);
                powerUpView.setVisible(false); // 初始不可见
                
                powerUpContainer.getChildren().add(powerUpView);
                powerUpIndicators.put(type.getName(), powerUpView);
            } catch (Exception e) {
                System.err.println("无法加载增益效果图片: " + type.getName() + ", " + e.getMessage());
            }
        }
        
        powerUpBox.getChildren().addAll(powerUpLabel, powerUpContainer);
        return powerUpBox;
    }
    
    /**
     * 更新增益效果显示
     */
    private void updatePowerUpDisplay() {
        if (gameController == null || gameController.getPlayerTank() == null) return;
        
        Map<Tank.PowerUpType, Double> activeEffects = gameController.getPlayerTank().getActiveEffects();
        
        // 首先隐藏所有增益图标
        for (ImageView indicator : powerUpIndicators.values()) {
            indicator.setVisible(false);
        }
        
        // 显示活跃的增益效果
        for (Map.Entry<Tank.PowerUpType, Double> entry : activeEffects.entrySet()) {
            String effectName = entry.getKey().getName();
            ImageView indicator = powerUpIndicators.get(effectName);
            if (indicator != null) {
                indicator.setVisible(true);
            }
        }
    }
    
    /**
     * 更新生命显示
     */
    private void updateLivesDisplay() {
        if (gameDataPanel != null) {
            for (javafx.scene.Node node : gameDataPanel.getChildren()) {
                if (node instanceof VBox && ((VBox) node).getChildren().get(0) instanceof Text) {
                    Text title = (Text) ((VBox) node).getChildren().get(0);
                    if (title.getText().equals("生命")) {
                        VBox livesBox = (VBox) node;
                        HBox heartsContainer = new HBox(5);
                        heartsContainer.setAlignment(javafx.geometry.Pos.CENTER);
                        
                        // 使用爱心图标
                        for (int i = 0; i < playerLives; i++) {
                            try {
                                ImageView heartIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/ui/heart.png")));
                                heartIcon.setFitWidth(20);
                                heartIcon.setFitHeight(20);
                                heartsContainer.getChildren().add(heartIcon);
                            } catch (Exception e) {
                                Text heartText = new Text("♥");
                                heartText.setFill(Color.RED);
                                heartText.setFont(Font.font("Arial", FontWeight.BOLD, 18));
                                heartsContainer.getChildren().add(heartText);
                            }
                        }
                        
                        // 更新显示
                        if (livesBox.getChildren().size() > 1) {
                            livesBox.getChildren().set(1, heartsContainer);
                        } else {
                            livesBox.getChildren().add(heartsContainer);
                        }
                        break;
                    }
                }
            }
        }
    }
}
