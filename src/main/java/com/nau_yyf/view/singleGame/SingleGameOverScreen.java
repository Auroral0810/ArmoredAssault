package com.nau_yyf.view.singleGame;

import com.jfoenix.controls.JFXButton;
import com.nau_yyf.view.GameView;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Separator;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;
import com.nau_yyf.view.GameOverScreen;
import java.util.Map;

/**
 * 游戏结束界面
 * 处理游戏失败时的界面显示
 */
public class SingleGameOverScreen implements GameOverScreen {
    private GameView gameView;
    private StackPane root;
    private Scene scene;
    private Color primaryColor;
    private StackPane containerPane;
    
    /**
     * 构造函数
     * @param gameView 游戏视图引用
     * @param root 根布局
     * @param scene 场景
     */
    public SingleGameOverScreen(GameView gameView, StackPane root, Scene scene) {
        this.gameView = gameView;
        this.root = root;
        this.scene = scene;
        this.primaryColor = gameView.getPrimaryColor();
    }
    
    /**
     * 显示游戏结束界面
     * @param gameData 游戏数据，包含得分、击败敌人数、剩余生命等信息
     */
    @Override
    public void show(Map<String, Object> gameData) {
        // 从gameData中提取单人游戏需要的数据
        int finalScore = (int) gameData.getOrDefault("finalScore", 0);
        
        Platform.runLater(() -> {
            // 如果已经显示了消息，不重复显示
            if (root.lookup("#gameOverScreen") != null) return;
            
            // 创建消息框容器
            containerPane = new StackPane();
            containerPane.setId("gameOverScreen");
            
            // 创建半透明背景
            Rectangle darkOverlay = new Rectangle(scene.getWidth(), scene.getHeight());
            darkOverlay.setFill(Color.rgb(0, 0, 0, 0.7));
            darkOverlay.setMouseTransparent(true);
            
            // 创建主要内容面板
            VBox messageBox = new VBox(20);
            messageBox.setAlignment(Pos.CENTER);
            messageBox.setPadding(new Insets(40));
            messageBox.setMaxWidth(550);
            messageBox.setMaxHeight(620);
            
            // 设置渐变背景和边框效果
            messageBox.setStyle("-fx-background-color: linear-gradient(to bottom, rgba(80, 20, 20, 0.95), rgba(40, 10, 10, 0.95));" +
                                "-fx-background-radius: 15;" +
                                "-fx-border-color: rgba(255, 100, 100, 0.4);" +
                                "-fx-border-width: 2;" +
                                "-fx-border-radius: 15;" + 
                                "-fx-effect: dropshadow(gaussian, rgba(255, 50, 50, 0.5), 20, 0, 0, 0);");
            
            // 创建游戏结束信息
            Text gameOverText = new Text("GAME OVER");
            gameOverText.setFont(Font.font("Impact", FontWeight.BOLD, 72));
            gameOverText.setFill(Color.RED);
            gameOverText.setStroke(Color.BLACK);
            gameOverText.setStrokeWidth(2);
            
            // 添加文字阴影
            DropShadow textShadow = new DropShadow();
            textShadow.setColor(Color.rgb(255, 0, 0, 0.7));
            textShadow.setRadius(15);
            textShadow.setSpread(0.4);
            gameOverText.setEffect(textShadow);
            
            // 分数显示
            Text scoreText = new Text("得分: " + finalScore);
            scoreText.setFont(Font.font("Arial", FontWeight.BOLD, 36));
            scoreText.setFill(Color.WHITE);
            
            // 按钮容器
            HBox buttonContainer = new HBox(30);
            buttonContainer.setAlignment(Pos.CENTER);
            
            // 创建按钮
            JFXButton retryButton = createActionButton("重新挑战", true);
            retryButton.setOnAction(e -> {
                // 移除当前界面
                root.getChildren().remove(containerPane);
                
                // 使用更可靠的方法重启游戏
                gameView.restartGame();
            });
            
            JFXButton mainMenuButton = createActionButton("返回主菜单", false);
            mainMenuButton.setOnAction(e -> {
                gameView.cleanupGameResources();
                gameView.showMainMenu();
            });
            
            buttonContainer.getChildren().addAll(mainMenuButton, retryButton);
            
            // 添加所有元素到主容器
            messageBox.getChildren().addAll(
                    gameOverText,
                    new Separator(),
                    scoreText,
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
            
            // 确保按钮可以获得焦点
            retryButton.setFocusTraversable(true);
            mainMenuButton.setFocusTraversable(true);
        });
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
            // 主要按钮 - 亮色
            button.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #F44336, #D32F2F);" +
                "-fx-background-radius: 25;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 18px;" +
                "-fx-font-weight: bold;" +
                "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.4), 8, 0, 0, 2);"
            );
        } else {
            // 次要按钮 - 较暗色
            button.setStyle(
                "-fx-background-color: linear-gradient(to bottom, " + toHexString(primaryColor) + ", #1A7CB8);" +
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
                    "-fx-background-color: linear-gradient(to bottom, #EF5350, #E53935);" +
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
                    "-fx-background-color: linear-gradient(to bottom, #F44336, #D32F2F);" +
                    "-fx-background-radius: 25;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 18px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.4), 8, 0, 0, 2);"
                );
            } else {
                button.setStyle(
                    "-fx-background-color: linear-gradient(to bottom, " + toHexString(primaryColor) + ", #1A7CB8);" +
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
     * 将Color对象转换为CSS hex字符串
     */
    private String toHexString(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

} 