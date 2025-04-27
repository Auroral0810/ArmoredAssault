package com.nau_yyf.view;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * 关卡选择对话框组件 - 简化版
 */
public class LevelSelectionDialog {

    private GameView gameView;
    private StackPane root;
    
    // 从GameView中提取的常量
    private final Color PRIMARY_COLOR = Color.rgb(37, 160, 218);    // 蓝色
    private final Color TEXT_COLOR = Color.WHITE;
    private final Color BACKGROUND_COLOR = Color.rgb(20, 30, 48, 0.95); // 深蓝灰色半透明
    
    /**
     * 构造函数
     * @param gameView 游戏主视图的引用
     * @param root 根布局容器
     */
    public LevelSelectionDialog(GameView gameView, StackPane root) {
        this.gameView = gameView;
        this.root = root;
    }
    
    /**
     * 显示关卡选择对话框
     * @param selectedTankType 选中的坦克类型
     */
    public void show(String selectedTankType) {
        Platform.runLater(() -> {
            // 创建对话框布局
            JFXDialogLayout content = new JFXDialogLayout();
            content.setStyle("-fx-background-color: transparent;");
            
            // 创建主容器
            VBox mainContainer = new VBox(20);
            mainContainer.setAlignment(Pos.CENTER);
            mainContainer.setPadding(new Insets(30));
            mainContainer.setMaxWidth(500);
            
            // 设置现代化背景
            Rectangle background = new Rectangle();
            background.widthProperty().bind(mainContainer.widthProperty());
            background.heightProperty().bind(mainContainer.heightProperty());
            background.setArcWidth(20);
            background.setArcHeight(20);
            
            // 渐变背景
            LinearGradient gradient = new LinearGradient(0, 0, 0, 1, true, 
                    javafx.scene.paint.CycleMethod.NO_CYCLE,
                    new Stop(0, Color.rgb(30, 60, 90, 0.95)),
                    new Stop(1, Color.rgb(20, 40, 70, 0.95)));
            background.setFill(gradient);
            
            // 添加阴影效果
            DropShadow dropShadow = new DropShadow();
            dropShadow.setColor(Color.rgb(0, 0, 0, 0.5));
            dropShadow.setRadius(15);
            background.setEffect(dropShadow);
            
            // 创建标题
            Text titleText = new Text("选择关卡");
            titleText.setFont(Font.font("Arial", FontWeight.BOLD, 32));
            titleText.setFill(TEXT_COLOR);
            
            mainContainer.getChildren().add(titleText);
            
            // 创建关卡选择按钮
            JFXDialog dialog = new JFXDialog(root, content, JFXDialog.DialogTransition.CENTER);
            
            // 创建关卡按钮容器
            VBox levelButtonsContainer = new VBox(15);
            levelButtonsContainer.setAlignment(Pos.CENTER);
            levelButtonsContainer.setPadding(new Insets(20, 0, 20, 0));
            
            // 添加关卡按钮
            for (int i = 1; i <= 5; i++) {
                final int level = i;
                JFXButton levelButton = createLevelButton("关卡 " + i, level, selectedTankType, dialog);
                levelButtonsContainer.getChildren().add(levelButton);
            }
            
            mainContainer.getChildren().add(levelButtonsContainer);
            
            // 添加返回按钮
            JFXButton backButton = createDialogButton("返回", false);
            backButton.setOnAction(e -> dialog.close());
            
            mainContainer.getChildren().add(backButton);
            
            // 创建内容容器
            StackPane contentContainer = new StackPane(background, mainContainer);
            
            // 设置对话框内容
            content.setBody(contentContainer);
            
            // 显示对话框
            dialog.show();
            
            // 添加淡入效果
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), contentContainer);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });
    }
    
    /**
     * 创建关卡按钮
     */
    private JFXButton createLevelButton(String text, int level, String selectedTankType, JFXDialog dialog) {
        JFXButton button = new JFXButton(text);
        button.setPrefWidth(300);
        button.setPrefHeight(60);
        button.setButtonType(JFXButton.ButtonType.RAISED);
        button.setTextFill(TEXT_COLOR);
        button.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        
        // 设置默认样式
        button.setStyle(
            "-fx-background-color: " + toHexString(PRIMARY_COLOR) + ";" +
            "-fx-background-radius: 30;"
        );
        
        // 添加悬停效果（只改变颜色，没有动画）
        button.setOnMouseEntered(e -> {
            button.setStyle(
                "-fx-background-color: #2196F3;" +
                "-fx-background-radius: 30;"
            );
        });
        
        button.setOnMouseExited(e -> {
            button.setStyle(
                "-fx-background-color: " + toHexString(PRIMARY_COLOR) + ";" +
                "-fx-background-radius: 30;"
            );
        });
        
        // 添加点击事件
        button.setOnAction(e -> {
            // 关闭对话框
            dialog.close();
            
            // 执行关卡选择逻辑
            Platform.runLater(() -> {
                // 检查是否为双人游戏模式
                if (selectedTankType.equals(gameView.getP1TankType())) {
                    // 双人游戏模式
                    gameView.startMultiPlayerGame(level);
                } else {
                    // 单人游戏模式
                    gameView.startGameWithLevel(selectedTankType, level);
                }
            });
        });
        
        return button;
    }
    
    /**
     * 创建对话框按钮
     */
    private JFXButton createDialogButton(String text, boolean isPrimary) {
        JFXButton button = new JFXButton(text);
        button.setPrefWidth(150);
        button.setPrefHeight(40);
        button.setButtonType(JFXButton.ButtonType.RAISED);
        button.setTextFill(TEXT_COLOR);
        
        if (isPrimary) {
            button.setStyle(
                "-fx-background-color: " + toHexString(PRIMARY_COLOR) + ";" +
                "-fx-background-radius: 20;" +
                "-fx-font-size: 16px;" +
                "-fx-font-weight: bold;"
            );
        } else {
            button.setStyle(
                "-fx-background-color: rgba(60, 60, 60, 0.6);" +
                "-fx-background-radius: 20;" +
                "-fx-font-size: 16px;" +
                "-fx-font-weight: bold;"
            );
        }
        
        // 添加悬停效果（只改变颜色，没有动画）
        button.setOnMouseEntered(e -> {
            if (isPrimary) {
                button.setStyle(
                    "-fx-background-color: #2196F3;" +
                    "-fx-background-radius: 20;" +
                    "-fx-font-size: 16px;" +
                    "-fx-font-weight: bold;"
                );
            } else {
                button.setStyle(
                    "-fx-background-color: rgba(80, 80, 80, 0.7);" +
                    "-fx-background-radius: 20;" +
                    "-fx-font-size: 16px;" +
                    "-fx-font-weight: bold;"
                );
            }
        });
        
        button.setOnMouseExited(e -> {
            if (isPrimary) {
                button.setStyle(
                    "-fx-background-color: " + toHexString(PRIMARY_COLOR) + ";" +
                    "-fx-background-radius: 20;" +
                    "-fx-font-size: 16px;" +
                    "-fx-font-weight: bold;"
                );
            } else {
                button.setStyle(
                    "-fx-background-color: rgba(60, 60, 60, 0.6);" +
                    "-fx-background-radius: 20;" +
                    "-fx-font-size: 16px;" +
                    "-fx-font-weight: bold;"
                );
            }
        });
        
        return button;
    }
    
    /**
     * 将Color对象转换为十六进制字符串
     * @param color 颜色
     * @return 十六进制表示
     */
    private String toHexString(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }
} 