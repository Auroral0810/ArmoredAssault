package com.nau_yyf.view;

import com.jfoenix.controls.JFXButton;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Separator;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import javafx.scene.layout.HBox;

/**
 * 暂停菜单视图
 * 负责显示游戏暂停时的菜单界面
 */
public class PauseMenuView {
    
    private GameView gameView;
    private StackPane root;
    private Scene scene;
    private StackPane pauseRoot;
    
    private final Color PRIMARY_COLOR;
    private final Color SECONDARY_COLOR;
    private final Color TEXT_COLOR;
    
    /**
     * 构造函数
     * @param gameView 游戏视图引用
     * @param root 根布局
     * @param scene 场景
     */
    public PauseMenuView(GameView gameView, StackPane root, Scene scene) {
        this.gameView = gameView;
        this.root = root;
        this.scene = scene;
        
        this.PRIMARY_COLOR = gameView.getPrimaryColor();
        this.SECONDARY_COLOR = Color.rgb(76, 175, 80); // 绿色
        this.TEXT_COLOR = Color.WHITE;
    }
    
    /**
     * 显示暂停菜单
     */
    public void show() {
        if (gameView.getIsPauseMenuOpen()) return;
        
        Platform.runLater(() -> {
            // 通知GameView暂停状态已改变
            gameView.setIsPauseMenuOpen(true);
            gameView.setGamePaused(true);
            
            // 停止游戏循环 - 由GameView处理
            
            // 创建半透明背景
            Rectangle overlay = new Rectangle(
                    scene.getWidth(), 
                    scene.getHeight(), 
                    Color.rgb(0, 0, 0, 0.7));
            
            // 添加模糊效果
            GaussianBlur blur = new GaussianBlur(3);
            overlay.setEffect(blur);
            
            // 创建暂停菜单容器
            VBox pauseMenu = new VBox(18); // 增加间距
            pauseMenu.setAlignment(Pos.CENTER);
            pauseMenu.setPadding(new Insets(40));
            pauseMenu.setMaxWidth(450);
            
            // 设置更好看的背景和边框
            pauseMenu.setStyle(
                "-fx-background-color: linear-gradient(to bottom, rgba(40, 70, 100, 0.95), rgba(20, 35, 50, 0.95));" +
                "-fx-background-radius: 15;" +
                "-fx-border-color: rgba(100, 180, 255, 0.6);" +
                "-fx-border-width: 1.5;" +
                "-fx-border-radius: 15;" +
                "-fx-effect: dropshadow(gaussian, rgba(0, 150, 255, 0.3), 15, 0, 0, 0);"
            );
            
            // 暂停标题
            Text pauseTitle = new Text("游戏已暂停");
            pauseTitle.setFont(Font.font("Arial", FontWeight.BOLD, 36));
            pauseTitle.setFill(Color.WHITE);
            
            // 添加标题阴影效果
            DropShadow titleShadow = new DropShadow();
            titleShadow.setColor(Color.rgb(0, 150, 255, 0.6));
            titleShadow.setRadius(10);
            titleShadow.setSpread(0.2);
            pauseTitle.setEffect(titleShadow);
            
            // 创建菜单按钮
            JFXButton resumeButton = createMenuButton("继续游戏", e -> close());
            JFXButton restartButton = createMenuButton("重新开始", e -> gameView.restartGame());
            JFXButton saveButton = createMenuButton("保存进度", e -> gameView.saveGame());
            JFXButton settingsButton = createMenuButton("设置", e -> gameView.showSettings());
            JFXButton mainMenuButton = createMenuButton("返回主菜单", e -> confirmReturnToMainMenu());
            JFXButton exitButton = createMenuButton("退出游戏", e -> confirmExitGame());
            
            // 美化分隔线
            Separator separator = new Separator();
            separator.setStyle("-fx-background-color: rgba(100, 180, 255, 0.4);");
            separator.setPrefWidth(380);
            
            // 将组件添加到菜单中
            pauseMenu.getChildren().addAll(
                    pauseTitle,
                    separator,
                    resumeButton,
                    restartButton,
                    saveButton,
                    settingsButton,
                    mainMenuButton,
                    exitButton
            );
            
            // 创建菜单场景
            pauseRoot = new StackPane(overlay, pauseMenu);
            pauseRoot.setAlignment(Pos.CENTER);
            
            // 添加到根布局
            root.getChildren().add(pauseRoot);
            
            // 添加进入动画
            ScaleTransition scaleIn = new ScaleTransition(Duration.millis(300), pauseMenu);
            scaleIn.setFromX(0.8);
            scaleIn.setFromY(0.8);
            scaleIn.setToX(1.0);
            scaleIn.setToY(1.0);
            
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), pauseRoot);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            
            ParallelTransition enterAnimation = new ParallelTransition(scaleIn, fadeIn);
            enterAnimation.play();
            
            // 确保焦点在菜单上，避免按键继续控制游戏
            pauseMenu.requestFocus();
        });
    }
    
    /**
     * 关闭暂停菜单
     */
    public void close() {
        Platform.runLater(() -> {
            if (pauseRoot != null) {
                // 添加退出动画
                FadeTransition fadeOut = new FadeTransition(Duration.millis(200), pauseRoot);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);
                fadeOut.setOnFinished(e -> {
                    // 移除菜单
                    root.getChildren().remove(pauseRoot);
                    
                    // 重置状态
                    gameView.setIsPauseMenuOpen(false);
                    gameView.setGamePaused(false);
                    pauseRoot = null;
                    
                    // 明确调用GameView的重启游戏循环方法
                    gameView.resumeGameLoop();
                });
                fadeOut.play();
            } else {
                // 没有动画时直接处理
                if (root.getChildren().size() > 1) {
                    root.getChildren().remove(root.getChildren().size() - 1);
                }
                
                gameView.setIsPauseMenuOpen(false);
                gameView.setGamePaused(false);
                
                // 明确调用GameView的重启游戏循环方法
                gameView.resumeGameLoop();
            }
        });
    }
    
    /**
     * 创建菜单按钮
     */
    private JFXButton createMenuButton(String text, javafx.event.EventHandler<javafx.event.ActionEvent> action) {
        JFXButton button = new JFXButton(text);
        button.setPrefWidth(320);
        button.setPrefHeight(50);
        button.setButtonType(JFXButton.ButtonType.RAISED);
        button.setStyle(
            "-fx-background-color: rgba(40, 80, 120, 0.7);" +
            "-fx-background-radius: 25;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 16px;" +
            "-fx-font-weight: bold;"
        );
        
        button.setOnAction(action);
        
        // 添加悬停效果
        button.setOnMouseEntered(e -> 
            button.setStyle(
                "-fx-background-color: " + toHexString(PRIMARY_COLOR) + ";" +
                "-fx-background-radius: 25;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 16px;" +
                "-fx-font-weight: bold;" +
                "-fx-effect: dropshadow(gaussian, rgba(0, 150, 255, 0.6), 10, 0, 0, 0);"
            )
        );
        
        button.setOnMouseExited(e -> 
            button.setStyle(
                "-fx-background-color: rgba(40, 80, 120, 0.7);" +
                "-fx-background-radius: 25;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 16px;" +
                "-fx-font-weight: bold;"
            )
        );
        
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
     * 确认返回主菜单
     */
    private void confirmReturnToMainMenu() {
        // 创建确认对话框
        JFXDialogLayout content = new JFXDialogLayout();
        content.setHeading(new Text("返回主菜单"));
        content.setBody(new Text("确定要返回主菜单吗？当前游戏进度将丢失。"));
        
        JFXDialog dialog = new JFXDialog(root, content, JFXDialog.DialogTransition.CENTER);
        
        JFXButton cancelButton = new JFXButton("取消");
        cancelButton.setPrefWidth(100);
        cancelButton.setPrefHeight(40);
        cancelButton.setStyle("-fx-background-color: #4d4d4d; -fx-text-fill: white;");
        cancelButton.setOnAction(e -> dialog.close());
        
        JFXButton confirmButton = new JFXButton("确定");
        confirmButton.setPrefWidth(100);
        confirmButton.setPrefHeight(40);
        confirmButton.setStyle("-fx-background-color: " + toHexString(PRIMARY_COLOR) + "; -fx-text-fill: white;");
        confirmButton.setOnAction(e -> {
            dialog.close();
            
            // 先停止游戏循环 - 仍需调用GameView的方法
            close(); // 关闭暂停菜单
            
            // 清理资源并显示主菜单
            Platform.runLater(() -> {
                gameView.cleanupGameResources();
                gameView.showMainMenu();
            });
        });
        
        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(cancelButton, confirmButton);
        
        content.setActions(buttonBox);
        dialog.show();
    }
    
    /**
     * 确认退出游戏
     */
    private void confirmExitGame() {
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
    }
} 