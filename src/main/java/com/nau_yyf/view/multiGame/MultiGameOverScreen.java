package com.nau_yyf.view.multiGame;

import com.jfoenix.controls.JFXButton;
import com.nau_yyf.view.GameView;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import com.nau_yyf.view.GameOverScreen;
import java.util.Map;

/**
 * 双人游戏结束界面
 * 展示双方玩家数据及游戏统计信息
 */
public class MultiGameOverScreen implements GameOverScreen {
    private GameView gameView;
    private StackPane root;
    private Scene scene;
    
    // 玩家颜色定义
    private final Color PLAYER1_COLOR = Color.rgb(37, 160, 218);    // 蓝色
    private final Color PLAYER2_COLOR = Color.rgb(212, 57, 43);     // 红色
    private final Color NEUTRAL_COLOR = Color.rgb(250, 250, 250);   // 白色/中性色
    
    private StackPane containerPane;
    
    /**
     * 构造函数
     * @param gameView 游戏视图引用
     * @param root 根布局
     * @param scene 场景
     */
    public MultiGameOverScreen(GameView gameView, StackPane root, Scene scene) {
        this.gameView = gameView;
        this.root = root;
        this.scene = scene;
    }
    
    /**
     * 显示游戏结束界面 - 默认表示闯关失败
     * @param totalScore 总分数
     * @param p1Score 玩家1分数
     * @param p2Score 玩家2分数
     * @param p1TanksDestroyed 玩家1击败坦克数
     * @param p2TanksDestroyed 玩家2击败坦克数
     * @param p1LivesRemaining 玩家1剩余生命
     * @param p2LivesRemaining 玩家2剩余生命
     * @param gameTime 游戏时长(秒)
     * @param currentLevel 当前关卡
     */
    public void show(int totalScore, int p1Score, int p2Score, 
                     int p1TanksDestroyed, int p2TanksDestroyed,
                     int p1LivesRemaining, int p2LivesRemaining,
                     int gameTime, int currentLevel) {
        Platform.runLater(() -> {
            // 如果已经显示了消息，不重复显示
            if (root.lookup("#multiGameOverScreen") != null) return;
            
            // 创建消息框容器
            containerPane = new StackPane();
            containerPane.setId("multiGameOverScreen");
            
            // 创建半透明背景
            Rectangle darkOverlay = new Rectangle(scene.getWidth(), scene.getHeight());
            darkOverlay.setFill(Color.rgb(0, 0, 0, 0.85));
            darkOverlay.setMouseTransparent(true);
            
            // 创建主要内容面板
            VBox messageBox = new VBox(25);
            messageBox.setAlignment(Pos.CENTER);
            messageBox.setPadding(new Insets(35));
            messageBox.setMaxWidth(650);
            messageBox.setMaxHeight(700);
            
            // 设置渐变背景和边框效果
            LinearGradient gradient = new LinearGradient(0, 0, 0, 1, true, 
                    javafx.scene.paint.CycleMethod.NO_CYCLE,
                    new Stop(0, Color.rgb(40, 45, 65, 0.95)),
                    new Stop(1, Color.rgb(25, 30, 45, 0.95)));
            
            // 设置背景
            Rectangle background = new Rectangle();
            background.widthProperty().bind(messageBox.widthProperty());
            background.heightProperty().bind(messageBox.heightProperty());
            background.setArcWidth(30);
            background.setArcHeight(30);
            background.setFill(gradient);
            background.setStroke(Color.rgb(100, 100, 120, 0.6));
            background.setStrokeWidth(2);
            
            // 添加阴影效果
            DropShadow dropShadow = new DropShadow();
            dropShadow.setColor(Color.rgb(0, 0, 0, 0.6));
            dropShadow.setRadius(20);
            dropShadow.setSpread(0.1);
            background.setEffect(dropShadow);
            
            // 创建内容的StackPane，方便设置背景
            StackPane contentPane = new StackPane(background, messageBox);
            
            // =========================== 标题区域 ===========================
            // 闯关失败标题
            Text gameOverText = new Text("闯关失败");
            Color titleColor = Color.rgb(220, 100, 100); // 失败红色
            
            gameOverText.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 54));
            gameOverText.setFill(titleColor);
            
            // 添加文字阴影效果
            DropShadow textShadow = new DropShadow();
            textShadow.setColor(Color.rgb(0, 0, 0, 0.7));
            textShadow.setRadius(5);
            textShadow.setSpread(0.2);
            gameOverText.setEffect(textShadow);
            
            // 显示当前关卡信息
            Text levelText = new Text("第 " + currentLevel + " 关");
            levelText.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 28));
            levelText.setFill(Color.WHITE);
            
            VBox titleBox = new VBox(10, gameOverText, levelText);
            titleBox.setAlignment(Pos.CENTER);
            
            // =========================== 总分区域 ===========================
            HBox totalScoreBox = new HBox(20);
            totalScoreBox.setAlignment(Pos.CENTER);
            
            Text totalScoreLabel = new Text("总分:");
            totalScoreLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 30));
            totalScoreLabel.setFill(Color.WHITE);
            
            Text totalScoreValue = new Text("" + totalScore);
            totalScoreValue.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 40));
            totalScoreValue.setFill(Color.rgb(255, 215, 0)); // 金色
            
            // 添加金色阴影效果
            DropShadow goldShadow = new DropShadow();
            goldShadow.setColor(Color.rgb(255, 215, 0, 0.7));
            goldShadow.setRadius(10);
            totalScoreValue.setEffect(goldShadow);
            
            totalScoreBox.getChildren().addAll(totalScoreLabel, totalScoreValue);
            
            // 添加游戏时间统计
            Text timePlayedText = new Text(formatTime(gameTime));
            timePlayedText.setFont(Font.font("Microsoft YaHei", FontWeight.NORMAL, 18));
            timePlayedText.setFill(Color.LIGHTGRAY);
            
            // 总击败坦克数
            Text totalTanksText = new Text("总击败坦克: " + (p1TanksDestroyed + p2TanksDestroyed) + " 辆");
            totalTanksText.setFont(Font.font("Microsoft YaHei", FontWeight.NORMAL, 18));
            totalTanksText.setFill(Color.LIGHTGRAY);
            
            VBox scoreAndTimeBox = new VBox(10, totalScoreBox, totalTanksText, timePlayedText);
            scoreAndTimeBox.setAlignment(Pos.CENTER);
            
            // =========================== 玩家数据区域 ===========================
            HBox playersContainer = new HBox(30);
            playersContainer.setAlignment(Pos.CENTER);
            
            // 玩家1数据卡
            VBox player1Card = createPlayerCard(
                "玩家 1", p1Score, p1TanksDestroyed, p1LivesRemaining, PLAYER1_COLOR);
            
            // 玩家2数据卡
            VBox player2Card = createPlayerCard(
                "玩家 2", p2Score, p2TanksDestroyed, p2LivesRemaining, PLAYER2_COLOR);
            
            playersContainer.getChildren().addAll(player1Card, player2Card);
            
            // =========================== 按钮区域 ===========================
            HBox buttonsBox = new HBox(25);
            buttonsBox.setAlignment(Pos.CENTER);
            
            // 修改"重新尝试"按钮的处理逻辑，返回到坦克选择界面而不是直接开始游戏
            JFXButton retryButton = createActionButton("重新尝试", true);
            retryButton.setOnAction(e -> {
                // 返回到坦克选择界面，不直接启动游戏
                gameView.showMultiTankSelection();
            });
            
            JFXButton mainMenuButton = createActionButton("返回主菜单", false);
            mainMenuButton.setOnAction(e -> {
                gameView.cleanupGameResources();
                gameView.showMainMenu();
            });
            
            buttonsBox.getChildren().addAll(mainMenuButton, retryButton);
            
            // =========================== 组合所有元素 ===========================
            messageBox.getChildren().addAll(
                titleBox,
                new Separator(),
                scoreAndTimeBox,
                playersContainer,
                buttonsBox
            );
            
            // 构建最终布局
            containerPane.getChildren().addAll(darkOverlay, contentPane);
            root.getChildren().add(containerPane);
            
            // 添加进入动画效果
            ScaleTransition scaleIn = new ScaleTransition(Duration.millis(400), contentPane);
            scaleIn.setFromX(0.7);
            scaleIn.setFromY(0.7);
            scaleIn.setToX(1.0);
            scaleIn.setToY(1.0);
            
            FadeTransition fadeIn = new FadeTransition(Duration.millis(400), contentPane);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            
            ParallelTransition enterAnimation = new ParallelTransition(scaleIn, fadeIn);
            enterAnimation.play();
            
            // 确保按钮可以获得焦点
            retryButton.setFocusTraversable(true);
            mainMenuButton.setFocusTraversable(true);
        });
    }
    
    /**
     * 创建玩家数据卡片
     */
    private VBox createPlayerCard(String playerName, int score, int tanksDestroyed, int livesRemaining, Color playerColor) {
        // 创建卡片容器
        VBox card = new VBox(15);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(15));
        card.setMinWidth(250);
        card.setMinHeight(220);
        
        // 设置卡片样式
        String colorHex = toHexString(playerColor);
        
        // 创建卡片背景
        Rectangle cardBg = new Rectangle();
        cardBg.widthProperty().bind(card.widthProperty());
        cardBg.heightProperty().bind(card.heightProperty());
        cardBg.setArcWidth(20);
        cardBg.setArcHeight(20);
        cardBg.setFill(Color.rgb(30, 30, 40, 0.6));
        cardBg.setStroke(playerColor.deriveColor(0, 1, 1, 0.7));
        cardBg.setStrokeWidth(3);
        
        // 添加内部阴影
        InnerShadow innerGlow = new InnerShadow();
        innerGlow.setBlurType(BlurType.GAUSSIAN);
        innerGlow.setColor(playerColor.deriveColor(0, 0.8, 1.5, 0.3));
        innerGlow.setRadius(5);
        innerGlow.setChoke(0.2);
        cardBg.setEffect(innerGlow);
        
        // 玩家标题
        Text nameText = new Text(playerName);
        nameText.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 24));
        nameText.setFill(playerColor);
        nameText.setTextAlignment(TextAlignment.CENTER);
        
        // 创建数据网格
        GridPane dataGrid = new GridPane();
        dataGrid.setAlignment(Pos.CENTER);
        dataGrid.setHgap(10);
        dataGrid.setVgap(10);
        
        // 添加数据行
        addDataRow(dataGrid, "得分:", String.valueOf(score), 0, playerColor);
        addDataRow(dataGrid, "击败坦克:", String.valueOf(tanksDestroyed), 1, playerColor);
        addDataRow(dataGrid, "剩余生命:", String.valueOf(livesRemaining), 2, playerColor);
        
        // 装饰线
        Rectangle decorLine = new Rectangle(80, 3);
        decorLine.setFill(playerColor);
        decorLine.setArcWidth(3);
        decorLine.setArcHeight(3);
        
        // 组合卡片内容
        VBox cardContent = new VBox(12, nameText, decorLine, dataGrid);
        cardContent.setAlignment(Pos.CENTER);
        
        // 创建最终的卡片
        StackPane cardPane = new StackPane(cardBg, cardContent);
        
        return new VBox(cardPane);
    }
    
    /**
     * 添加数据行到网格
     */
    private void addDataRow(GridPane grid, String label, String value, int row, Color playerColor) {
        Text labelText = new Text(label);
        labelText.setFont(Font.font("Microsoft YaHei", FontWeight.NORMAL, 16));
        labelText.setFill(Color.LIGHTGRAY);
        
        Text valueText = new Text(value);
        valueText.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 20));
        valueText.setFill(playerColor.brighter());
        
        grid.add(labelText, 0, row);
        grid.add(valueText, 1, row);
    }
    
    /**
     * 创建动作按钮
     */
    private JFXButton createActionButton(String text, boolean isPrimary) {
        JFXButton button = new JFXButton(text);
        button.setPrefWidth(180);
        button.setPrefHeight(50);
        button.setButtonType(JFXButton.ButtonType.RAISED);
        
        // 设置按钮样式
        if (isPrimary) {
            // 主要按钮 - 亮色调
            button.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #4CAF50, #388E3C);" +
                "-fx-background-radius: 25;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 18px;" +
                "-fx-font-weight: bold;" +
                "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.4), 8, 0, 0, 2);"
            );
        } else {
            // 次要按钮 - 蓝色调
            button.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #2196F3, #1976D2);" +
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
                    "-fx-background-color: linear-gradient(to bottom, #66BB6A, #43A047);" +
                    "-fx-background-radius: 25;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 18px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.6), 12, 0, 0, 3);"
                );
            } else {
                button.setStyle(
                    "-fx-background-color: linear-gradient(to bottom, #42A5F5, #1E88E5);" +
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
                    "-fx-background-color: linear-gradient(to bottom, #4CAF50, #388E3C);" +
                    "-fx-background-radius: 25;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 18px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.4), 8, 0, 0, 2);"
                );
            } else {
                button.setStyle(
                    "-fx-background-color: linear-gradient(to bottom, #2196F3, #1976D2);" +
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
    
    /**
     * 格式化游戏时间
     */
    private String formatTime(int seconds) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        return String.format("游戏时长: %d分%d秒", minutes, remainingSeconds);
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

    @Override
    public void show(Map<String, Object> gameData) {
        // 从gameData中提取多人游戏需要的数据
        int totalScore = (int) gameData.getOrDefault("totalScore", 0);
        int p1Score = (int) gameData.getOrDefault("p1Score", 0);
        int p2Score = (int) gameData.getOrDefault("p2Score", 0);
        int p1TanksDestroyed = (int) gameData.getOrDefault("p1TanksDestroyed", 0);
        int p2TanksDestroyed = (int) gameData.getOrDefault("p2TanksDestroyed", 0);
        int p1LivesRemaining = (int) gameData.getOrDefault("p1LivesRemaining", 0);
        int p2LivesRemaining = (int) gameData.getOrDefault("p2LivesRemaining", 0);
        int gameTime = (int) gameData.getOrDefault("gameTime", 0);
        int currentLevel = (int) gameData.getOrDefault("currentLevel", 1);
        
        // 显示多人游戏结束界面
        show(totalScore, p1Score, p2Score, p1TanksDestroyed, p2TanksDestroyed, p1LivesRemaining, p2LivesRemaining, gameTime, currentLevel);
    }
    

} 