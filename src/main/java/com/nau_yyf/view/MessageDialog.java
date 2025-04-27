package com.nau_yyf.view;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * 消息对话框组件
 * 用于显示美化的消息对话框
 */
public class MessageDialog {
    
    private StackPane root;
    private Color primaryColor;
    private Color textColor;
    
    /**
     * 构造函数
     * @param root 根布局容器
     * @param primaryColor 主题色
     * @param textColor 文本颜色
     */
    public MessageDialog(StackPane root, Color primaryColor, Color textColor) {
        this.root = root;
        this.primaryColor = primaryColor;
        this.textColor = textColor;
    }
    
    /**
     * 显示消息对话框
     * @param message 消息内容
     */
    public void show(String message) {
        show(message, "消息", null);
    }
    
    /**
     * 显示带有自定义标题的消息对话框
     * @param message 消息内容
     * @param title 标题
     */
    public void show(String message, String title) {
        show(message, title, null);
    }
    
    /**
     * 显示带有自定义标题和图标的消息对话框
     * @param message 消息内容
     * @param title 标题
     * @param iconPath 图标路径（可为null）
     */
    public void show(String message, String title, String iconPath) {
        // 创建对话框布局
        JFXDialogLayout content = new JFXDialogLayout();
        
        // 设置样式
        content.setStyle("-fx-background-color: linear-gradient(to bottom, #1E3A5F, #0F1A2A);" +
                         "-fx-background-radius: 15;" + 
                         "-fx-border-color: " + toHexString(primaryColor.brighter()) + ";" +
                         "-fx-border-radius: 15;" +
                         "-fx-border-width: 1.5;");
        
        // 创建标题区域
        HBox titleBox = new HBox(15);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        
        // 设置图标（如果提供）
        if (iconPath != null) {
            try {
                Image icon = new Image(getClass().getResourceAsStream(iconPath));
                ImageView iconView = new ImageView(icon);
                iconView.setFitHeight(28);
                iconView.setFitWidth(28);
                titleBox.getChildren().add(iconView);
            } catch (Exception e) {
                System.err.println("无法加载消息图标: " + e.getMessage());
            }
        } else {
            // 默认使用信息图标
            try {
                Image icon = new Image(getClass().getResourceAsStream("/images/ui/info_icon.png"));
                ImageView iconView = new ImageView(icon);
                iconView.setFitHeight(28);
                iconView.setFitWidth(28);
                titleBox.getChildren().add(iconView);
            } catch (Exception e) {
                // 如果无法加载图标，忽略并继续
            }
        }
        
        // 创建并设置标题文本
        Text titleText = new Text(title);
        titleText.setFill(primaryColor);
        titleText.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        
        // 添加阴影效果
        DropShadow titleShadow = new DropShadow();
        titleShadow.setColor(Color.rgb(0, 0, 0, 0.4));
        titleShadow.setRadius(2);
        titleShadow.setOffsetY(1);
        titleText.setEffect(titleShadow);
        
        titleBox.getChildren().add(titleText);
        content.setHeading(titleBox);
        
        // 创建消息内容
        BorderPane messagePane = new BorderPane();
        messagePane.setPadding(new Insets(10, 20, 10, 20));
        
        Text messageText = new Text(message);
        messageText.setFill(textColor);
        messageText.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        messageText.setWrappingWidth(320); // 设置文本换行宽度
        
        messagePane.setCenter(messageText);
        content.setBody(messagePane);
        
        // 创建按钮
        JFXButton closeButton = new JFXButton("确定");
        closeButton.setPrefWidth(100);
        closeButton.setPrefHeight(40);
        closeButton.setButtonType(JFXButton.ButtonType.RAISED);
        closeButton.setStyle("-fx-background-color: " + toHexString(primaryColor) + ";" +
                            "-fx-background-radius: 20;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;");
        
        // 添加按钮悬停效果
        closeButton.setOnMouseEntered(e -> 
            closeButton.setStyle("-fx-background-color: " + toHexString(primaryColor.brighter()) + ";" +
                               "-fx-background-radius: 20;" +
                               "-fx-text-fill: white;" +
                               "-fx-font-weight: bold;"));
        
        closeButton.setOnMouseExited(e -> 
            closeButton.setStyle("-fx-background-color: " + toHexString(primaryColor) + ";" +
                               "-fx-background-radius: 20;" +
                               "-fx-text-fill: white;" +
                               "-fx-font-weight: bold;"));
        
        // 创建按钮容器，使按钮居中
        VBox buttonBox = new VBox(closeButton);
        buttonBox.setAlignment(Pos.CENTER);
        content.setActions(buttonBox);
        
        // 创建对话框
        JFXDialog dialog = new JFXDialog(root, content, JFXDialog.DialogTransition.CENTER);
        
        // 设置按钮动作
        closeButton.setOnAction(e -> dialog.close());
        
        // 添加动画效果
        dialog.setOnDialogOpened(event -> {
            ScaleTransition scaleIn = new ScaleTransition(Duration.millis(250), content);
            scaleIn.setFromX(0.7);
            scaleIn.setFromY(0.7);
            scaleIn.setToX(1.0);
            scaleIn.setToY(1.0);
            
            FadeTransition fadeIn = new FadeTransition(Duration.millis(250), content);
            fadeIn.setFromValue(0.3);
            fadeIn.setToValue(1.0);
            
            scaleIn.play();
            fadeIn.play();
        });
        
        // 显示对话框
        dialog.show();
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