package com.nau_yyf.view;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import com.jfoenix.controls.JFXRadioButton;
import com.jfoenix.controls.JFXSlider;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * 游戏设置对话框
 * 提供游戏各种设置的调整界面
 */
public class SettingsDialog {
    private GameView gameView;
    private StackPane root;
    private JFXDialog dialog;
    private Color primaryColor;
    private Color textColor;
    
    // 设置项
    private JFXSlider volumeSlider;
    private JFXRadioButton easyBtn;
    private JFXRadioButton normalBtn;
    private JFXRadioButton hardBtn;
    
    /**
     * 构造函数
     * @param gameView 游戏视图引用
     * @param root 根布局
     */
    public SettingsDialog(GameView gameView, StackPane root) {
        this.gameView = gameView;
        this.root = root;
        this.primaryColor = gameView.getPrimaryColor();
        this.textColor = gameView.getTextColor();
        
        initializeDialog();
    }
    
    /**
     * 初始化对话框
     */
    private void initializeDialog() {
        JFXDialogLayout content = new JFXDialogLayout();
        content.setHeading(new Text("游戏设置"));
        
        VBox settingsContent = createSettingsContent();
        content.setBody(settingsContent);
        
        JFXButton closeButton = createCloseButton();
        content.setActions(closeButton);
        
        dialog = new JFXDialog(root, content, JFXDialog.DialogTransition.CENTER);
        
        closeButton.setOnAction(e -> {
            dialog.close();
            
            // 保存设置值(将来实现)
            saveSettings();
            
            // 关闭设置对话框后重新显示暂停菜单
            gameView.showPauseMenu();
        });
    }
    
    /**
     * 创建设置内容
     * @return 设置界面内容
     */
    private VBox createSettingsContent() {
        VBox settingsContent = new VBox(15);
        settingsContent.setAlignment(Pos.CENTER);
        settingsContent.setMinWidth(350);
        settingsContent.setMaxWidth(450);
        
        // 1. 音量设置
        Label audioLabel = new Label("音量");
        audioLabel.setFont(Font.font("Arial", 14));
        audioLabel.setTextFill(primaryColor);
        
        volumeSlider = new JFXSlider(0, 100, 50);
        volumeSlider.setIndicatorPosition(JFXSlider.IndicatorPosition.RIGHT);
        volumeSlider.setStyle("-fx-accent: " + toHexString(primaryColor) + ";");
        
        // 2. 难度设置
        Label difficultyLabel = new Label("难度");
        difficultyLabel.setFont(Font.font("Arial", 14));
        difficultyLabel.setTextFill(primaryColor);
        
        HBox difficultyOptions = new HBox(15);
        difficultyOptions.setAlignment(Pos.CENTER);
        
        ToggleGroup difficultyGroup = new ToggleGroup();
        
        easyBtn = new JFXRadioButton("简单");
        easyBtn.setToggleGroup(difficultyGroup);
        easyBtn.setTextFill(textColor);
        
        normalBtn = new JFXRadioButton("普通");
        normalBtn.setToggleGroup(difficultyGroup);
        normalBtn.setTextFill(textColor);
        normalBtn.setSelected(true);
        
        hardBtn = new JFXRadioButton("困难");
        hardBtn.setToggleGroup(difficultyGroup);
        hardBtn.setTextFill(textColor);
        
        difficultyOptions.getChildren().addAll(easyBtn, normalBtn, hardBtn);
        
        // 组装设置内容
        settingsContent.getChildren().addAll(
                audioLabel, volumeSlider,
                new Separator(),
                difficultyLabel, difficultyOptions
        );
        
        return settingsContent;
    }
    
    /**
     * 创建关闭按钮
     * @return 关闭按钮
     */
    private JFXButton createCloseButton() {
        JFXButton closeButton = new JFXButton("确定");
        closeButton.setPrefWidth(120);
        closeButton.setPrefHeight(40);
        closeButton.setButtonType(JFXButton.ButtonType.RAISED);
        closeButton.setStyle("-fx-background-color: " + toHexString(primaryColor) + ";");
        closeButton.setTextFill(textColor);
        return closeButton;
    }
    
    /**
     * 显示设置对话框
     */
    public void show() {
        // 加载当前设置值(将来实现)
        loadSettings();
        
        // 显示对话框
        dialog.show();
    }
    
    /**
     * 保存设置
     */
    private void saveSettings() {
        // 将来实现：保存设置到配置文件或游戏状态
        int volume = (int) volumeSlider.getValue();
        String difficulty = getDifficulty();
        
        System.out.println("设置已保存 - 音量: " + volume + ", 难度: " + difficulty);
    }
    
    /**
     * 加载设置
     */
    private void loadSettings() {
        // 将来实现：从配置文件或游戏状态加载设置
    }
    
    /**
     * 获取当前选择的难度
     * @return 难度字符串
     */
    private String getDifficulty() {
        if (easyBtn.isSelected()) return "简单";
        if (hardBtn.isSelected()) return "困难";
        return "普通";
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