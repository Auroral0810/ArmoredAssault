package com.nau_yyf.view;

import com.jfoenix.controls.JFXButton;
import com.nau_yyf.controller.GameController;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import static com.nau_yyf.util.TankUtil.getTankDisplayName;

/**
 * 单人游戏主屏幕界面
 */
public class SinglePlayerGameScreen {
    
    private GameView gameView;
    private Canvas gameCanvas;
    private GraphicsContext gc;
    private HBox gameDataPanel;
    
    /**
     * 构造函数
     * @param gameView 游戏主视图引用
     */
    public SinglePlayerGameScreen(GameView gameView) {
        this.gameView = gameView;
    }
    
    /**
     * 显示游戏主屏幕
     * @param gameController 游戏控制器
     */
    public void show(GameController gameController) {
        // 清除当前内容
        gameView.getRoot().getChildren().clear();
        
        // 创建简化的游戏主布局
        BorderPane gameLayout = new BorderPane();
        gameLayout.setStyle("-fx-background-color: #1a2634;");
        
        // ---------- 顶部区域 ----------
        HBox topInfoBar = createTopInfoBar(gameController);
        
        // ---------- 中央游戏区域 ----------
        HBox gameWithSidePanels = createGameArea();
        
        // ---------- 底部数据面板 ----------
        gameDataPanel = createDataPanel(gameController);
        
        // 将各部分添加到布局
        gameLayout.setTop(topInfoBar);
        gameLayout.setCenter(gameWithSidePanels);
        gameLayout.setBottom(gameDataPanel);
        
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
        
        // 确保画布获取焦点
        Platform.runLater(() -> gameCanvas.requestFocus());
    }
    
    /**
     * 创建顶部信息栏
     */
    private HBox createTopInfoBar(GameController gameController) {
        HBox topInfoBar = new HBox(20);
        topInfoBar.setAlignment(Pos.CENTER_LEFT);
        topInfoBar.setPadding(new Insets(10, 20, 10, 20));
        topInfoBar.setStyle("-fx-background-color: rgba(0, 0, 0, 0.4);");
        
        // 关卡信息
        Text levelInfo = new Text("第" + gameController.getCurrentLevel() + "关");
        levelInfo.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        levelInfo.setFill(gameView.getPrimaryColor());
        
        // 游戏时间
        Text timeInfo = new Text("00:00");
        timeInfo.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        timeInfo.setFill(gameView.getTextColor());
        gameView.setTimeInfo(timeInfo);
        
        // 添加设置按钮
        JFXButton settingsButton = new JFXButton();
        settingsButton.getStyleClass().add("settings-button");
        settingsButton.setButtonType(JFXButton.ButtonType.RAISED);
        settingsButton.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");
        settingsButton.setPrefSize(40, 40);
        
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
        
        // 添加一些调试信息，确保我们知道画布被添加
        System.out.println("游戏画布已创建，大小: " + gameCanvas.getWidth() + "x" + gameCanvas.getHeight());
        
        // 初始绘制一些内容到画布，确认它正在工作
        gc.setFill(Color.rgb(10, 30, 50));
        gc.fillRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());
        gc.setStroke(Color.CYAN);
        gc.strokeRect(5, 5, gameCanvas.getWidth()-10, gameCanvas.getHeight()-10);
        gc.setFill(Color.WHITE);
        gc.fillText("游戏画布已初始化", 20, 30);
        
        return gameWithSidePanels;
    }
    
    /**
     * 创建数据面板
     */
    private HBox createDataPanel(GameController gameController) {
        HBox dataPanel = new HBox(20);
        dataPanel.setPadding(new Insets(10));
        dataPanel.setAlignment(Pos.CENTER);
        dataPanel.setBackground(new Background(new BackgroundFill(
                Color.rgb(0, 20, 40), CornerRadii.EMPTY, Insets.EMPTY)));
        
        // 使用方法创建各种信息显示
        VBox playerInfo = createInfoBox("坦克类型", getTankDisplayName(gameController.getPlayerTank().getTypeString()));
        playerInfo.setId("playerInfo");
        
        VBox healthInfo = createInfoBox("血量", Integer.toString(gameController.getPlayerTank().getHealth()));
        healthInfo.setId("healthInfo");
        
        VBox bulletInfo = createInfoBox("子弹", Integer.toString(gameView.getBulletCount()));
        bulletInfo.setId("bulletInfo");
        
        VBox enemiesInfo = createInfoBox("关卡目标", gameController.getDefeatedEnemiesCount() + "/" + gameController.getTotalEnemyTarget());
        enemiesInfo.setId("enemiesInfo");
        
        // 添加生命显示和增益效果区域
        HBox livesDisplay = gameView.createLivesDisplay();
        livesDisplay.setId("livesDisplay");
        
        // 在数据面板中添加所有信息
        dataPanel.getChildren().addAll(playerInfo, healthInfo, bulletInfo, enemiesInfo, livesDisplay);
        
        // 添加增益效果状态栏
        HBox powerUpStatusBar = gameView.createPowerUpStatusBar();
        dataPanel.getChildren().add(powerUpStatusBar);
        
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
} 