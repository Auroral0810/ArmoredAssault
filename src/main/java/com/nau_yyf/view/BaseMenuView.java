package com.nau_yyf.view;

import com.jfoenix.controls.JFXButton;
import com.nau_yyf.util.BackgroundManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * 菜单视图基类
 * 提供共享的背景设置和UI创建功能
 */
public abstract class BaseMenuView {
    
    protected GameView gameView;
    protected StackPane root;
    protected Stage stage;
    
    // 通用颜色定义
    protected final Color PRIMARY_COLOR = Color.rgb(37, 160, 218);    // 蓝色
    protected final Color SECONDARY_COLOR = Color.rgb(76, 175, 80);   // 绿色 
    protected final Color TEXT_COLOR = Color.WHITE;
    protected final Color BACKGROUND_COLOR = Color.rgb(27, 40, 56);   // 深蓝灰色
    protected static final javafx.scene.paint.Color DISABLED_COLOR = javafx.scene.paint.Color.GRAY; // 或其他适当的颜色
    
    /**
     * 构造函数
     * 
     * @param gameView 游戏主视图的引用
     * @param root 根布局容器
     * @param stage 主舞台
     */
    public BaseMenuView(GameView gameView, StackPane root, Stage stage) {
        this.gameView = gameView;
        this.root = root;
        this.stage = stage;
    }
    
    /**
     * 显示视图（抽象方法，需要子类实现）
     */
    public abstract void show();
    
    /**
     * 为容器设置游戏背景
     * 
     * @param container 要设置背景的容器
     */
    protected void setGameBackground(Region container) {
        BackgroundManager.setGameBackground(container);
    }
    
    /**
     * 创建内容面板
     * 
     * @param spacing 元素间距
     * @return 配置好的内容面板
     */
    protected VBox createContentPanel(double spacing) {
        VBox panel = new VBox(spacing);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(40));
        panel.setMaxWidth(600);
        panel.setStyle("-fx-background-radius: 15; " +
                      "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 10, 0, 0, 5);");
        return panel;
    }
    
    /**
     * 创建页面标题
     * 
     * @param titleText 标题文本
     * @return 配置好的标题文本组件
     */
    protected Text createTitle(String titleText) {
        Text title = new Text(titleText);
        title.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        title.setFill(PRIMARY_COLOR);
        return title;
    }
    
    /**
     * 创建菜单按钮
     * 
     * @param text 按钮文本
     * @param action 按钮动作
     * @param isPrimary 是否是主按钮
     * @return 配置好的JFXButton
     */
    protected JFXButton createMenuButton(String text, javafx.event.EventHandler<javafx.event.ActionEvent> action, boolean isPrimary) {
        JFXButton button = new JFXButton(text);
        button.setButtonType(JFXButton.ButtonType.RAISED);
        button.setTextFill(TEXT_COLOR);
        
        if (isPrimary) {
            button.setStyle("-fx-background-color: " + toHexString(SECONDARY_COLOR) + "; -fx-font-size: 16px;");
        } else {
            button.setStyle("-fx-background-color: " + toHexString(PRIMARY_COLOR) + "; -fx-font-size: 16px;");
        }
        
        button.setPrefWidth(200);
        button.setPrefHeight(40);
        button.setOnAction(action);
        return button;
    }
    
    /**
     * 简化版菜单按钮创建（非主按钮）
     */
    protected JFXButton createMenuButton(String text, javafx.event.EventHandler<javafx.event.ActionEvent> action) {
        return createMenuButton(text, action, false);
    }
    
    /**
     * 将Color对象转换为十六进制字符串
     * 
     * @param color 颜色
     * @return 十六进制表示
     */
    protected String toHexString(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }
    
    /**
     * 创建带有背景和面板的完整菜单
     * 
     * @param spacing 元素间距
     * @return 配置好的内容面板
     */
    protected VBox createCompleteMenuPanel(double spacing) {
        // 设置根布局背景
        setGameBackground(root);
        
        // 创建内容面板
        return createContentPanel(spacing);
    }
} 