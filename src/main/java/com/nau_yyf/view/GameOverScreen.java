package com.nau_yyf.view;

import com.jfoenix.controls.JFXButton;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Separator;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/**
 * 游戏结束界面
 * 处理游戏失败时的界面显示
 */
public class GameOverScreen {
    private GameView gameView;
    private StackPane root;
    private Scene scene;
    private Color primaryColor;
    
    /**
     * 构造函数
     * @param gameView 游戏视图引用
     * @param root 根布局
     * @param scene 场景
     */
    public GameOverScreen(GameView gameView, StackPane root, Scene scene) {
        this.gameView = gameView;
        this.root = root;
        this.scene = scene;
        this.primaryColor = gameView.getPrimaryColor();
    }
    
    /**
     * 显示游戏结束界面
     * @param score 玩家最终得分
     */
    public void show(int score) {
        Platform.runLater(() -> {
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
            
            Text scoreText = new Text("得分: " + score);
            scoreText.setFont(Font.font("Arial", FontWeight.BOLD, 36));
            scoreText.setFill(Color.WHITE);
            
            // 创建按钮
            JFXButton retryButton = createMenuButton("重新挑战", e -> gameView.restartGame());
            JFXButton mainMenuButton = createMenuButton("返回主菜单", e -> {
                gameView.cleanupGameResources();
                gameView.showMainMenu();
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
            gameOverRoot.setId("gameOverScreen");
            
            // 添加到根布局
            root.getChildren().add(gameOverRoot);
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
        button.setStyle("-fx-background-color: " + toHexString(primaryColor) + ";" +
                        "-fx-background-radius: 25;");
        button.setTextFill(Color.WHITE);
        button.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        button.setOnAction(action);
        
        // 添加悬停效果
        button.setOnMouseEntered(e -> {
            button.setStyle("-fx-background-color: #2196F3;" +
                           "-fx-background-radius: 25;");
        });
        
        button.setOnMouseExited(e -> {
            button.setStyle("-fx-background-color: " + toHexString(primaryColor) + ";" +
                           "-fx-background-radius: 25;");
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