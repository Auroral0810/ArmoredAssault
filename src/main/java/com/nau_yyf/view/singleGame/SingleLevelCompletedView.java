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
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;
import com.nau_yyf.view.LevelCompletedView;
import java.util.Map;

/**
 * 关卡完成视图
 * 用于显示关卡完成后的统计信息和动画效果
 */
public class SingleLevelCompletedView implements LevelCompletedView {

    private GameView gameView;
    private StackPane root;
    private Scene scene;
    private StackPane containerPane;
    
    private final Color PRIMARY_COLOR;
    private final Color SECONDARY_COLOR;
    private final Color TEXT_COLOR;
    
    /**
     * 构造函数
     * @param gameView 游戏视图引用
     * @param root 根布局
     * @param scene 场景
     */
    public SingleLevelCompletedView(GameView gameView, StackPane root, Scene scene) {
        this.gameView = gameView;
        this.root = root;
        this.scene = scene;
        
        this.PRIMARY_COLOR = gameView.getPrimaryColor();
        this.SECONDARY_COLOR = Color.rgb(76, 175, 80); // 绿色
        this.TEXT_COLOR = Color.WHITE;
    }
    
    /**
     * 显示关卡完成界面
     * @param levelData 关卡数据，包含当前关卡、击败敌人数、游戏时间等信息
     */
    @Override
    public void show(Map<String, Object> levelData) {
        // 从levelData中提取单人游戏需要的数据
        int currentLevel = (int) levelData.getOrDefault("currentLevel", 1);
        int defeatedEnemies = (int) levelData.getOrDefault("defeatedEnemies", 0);
        long totalGameTime = (long) levelData.getOrDefault("totalGameTime", 0L);
        int playerLives = (int) levelData.getOrDefault("playerLives", 3);
        String playerTankType = (String) levelData.getOrDefault("playerTankType", "standard");
        int totalLevels = (int) levelData.getOrDefault("totalLevels", 5);
        
        // 调用原有的显示方法
        show(currentLevel, defeatedEnemies, totalGameTime, playerLives, playerTankType, totalLevels);
    }
    
    /**
     * 显示关卡完成界面
     * @param currentLevel 当前关卡
     * @param defeatedEnemies 击败的敌人数量
     * @param totalGameTime 游戏总时间（毫秒）
     * @param playerLives 玩家剩余生命
     * @param playerTankType 玩家坦克类型
     * @param totalLevels 游戏总关卡数
     */
    private void show(int currentLevel, int defeatedEnemies, long totalGameTime, 
                     int playerLives, String playerTankType, int totalLevels) {
        // 如果已经显示了消息，不重复显示
        if (root.lookup("#levelCompletedMessage") != null) return;
        
        Platform.runLater(() -> {
            // 创建消息框容器
            containerPane = new StackPane();
            containerPane.setId("levelCompletedMessage");
            
            // 添加全屏暗色背景
            Rectangle darkOverlay = new Rectangle(scene.getWidth(), scene.getHeight());
            darkOverlay.setFill(Color.rgb(0, 0, 0, 0.7));
            
            // 创建主要内容面板
            VBox messageBox = new VBox(20);
            messageBox.setAlignment(Pos.CENTER);
            messageBox.setPadding(new Insets(40));
            messageBox.setMaxWidth(550);
            messageBox.setMaxHeight(620);
            
            // 设置渐变背景和边框效果
            messageBox.setStyle("-fx-background-color: linear-gradient(to bottom, rgba(20, 40, 80, 0.95), rgba(10, 20, 40, 0.95));" +
                                "-fx-background-radius: 15;" +
                                "-fx-border-color: linear-gradient(to bottom, " + toHexString(PRIMARY_COLOR) + "80, " + toHexString(SECONDARY_COLOR) + "80);" +
                                "-fx-border-width: 2;" +
                                "-fx-border-radius: 15;" + 
                                "-fx-effect: dropshadow(gaussian, rgba(0, 150, 255, 0.5), 20, 0, 0, 0);");
            
            // 添加顶部光效装饰
            Rectangle topGlow = new Rectangle(500, 4);
            topGlow.setArcWidth(4);
            topGlow.setArcHeight(4);
            topGlow.setFill(Color.rgb(100, 200, 255, 0.7));
            GaussianBlur glow = new GaussianBlur(10);
            topGlow.setEffect(glow);
            
            // 标题区域
            Text victoryText = new Text("任务完成");
            victoryText.setFont(Font.font("Arial", FontWeight.BLACK, 42));
            victoryText.setFill(Color.WHITE);
            
            // 添加金色描边
            victoryText.setStroke(Color.rgb(255, 215, 0, 0.8));
            victoryText.setStrokeWidth(1.5);
            
            // 添加文字阴影
            DropShadow textShadow = new DropShadow();
            textShadow.setColor(Color.rgb(0, 150, 255, 0.7));
            textShadow.setRadius(15);
            textShadow.setSpread(0.4);
            victoryText.setEffect(textShadow);
            
            // 计算各项得分
            int enemyScore = defeatedEnemies * 200;
            int levelScore = currentLevel * 1000;
            
            // 通关时间计算
            long seconds = totalGameTime / 1000;
            long minutes = seconds / 60;
            seconds = seconds % 60;
            String timeString = String.format("%02d:%02d", minutes, seconds);
            
            // 时间得分（越快越高）
            int timeScore = Math.max(0, 300000 - (int) totalGameTime) / 1000;
            
            // 生命得分
            int lifeScore = playerLives * 500;
            
            // 使用计算函数获取总分
            int totalScore = calculateScore(currentLevel, defeatedEnemies, totalGameTime, playerLives);
            
            // 创建详细统计卡片
            VBox statsCard = createStatsCard(
                currentLevel, 
                defeatedEnemies, enemyScore,
                timeString, timeScore,
                playerLives, lifeScore,
                totalScore
            );
            
            // 按钮容器
            HBox buttonContainer = new HBox(30);
            buttonContainer.setAlignment(Pos.CENTER);
            
            // 创建按钮
            JFXButton menuButton = createActionButton("返回主菜单", false);
            menuButton.setOnAction(e -> {
                // 先移除当前界面
                root.getChildren().remove(containerPane);
                
                // 然后清理游戏资源
                gameView.cleanupGameResources();
                
                // 最后显示主菜单
                Platform.runLater(() -> {
                    gameView.showMainMenu();
                });
            });
            
            if (currentLevel < totalLevels) {
                JFXButton nextButton = createActionButton("下一关", true);
                nextButton.setOnAction(e -> {
                    // 确保移除当前界面
                    root.getChildren().remove(containerPane);
                    
                    // 重要：在启动新游戏前重置游戏状态
                    gameView.cleanupGameResources();
                    
                    // 设置游戏为非暂停状态
                    gameView.setGamePaused(false);
                    
                    // 启动下一关
                    Platform.runLater(() -> {
                        gameView.startGameWithLevel(playerTankType, currentLevel + 1);
                    });
                });
                buttonContainer.getChildren().addAll(menuButton, nextButton);
            } else {
                // 最终关卡通关
                Text completionText = new Text("恭喜你已完成所有关卡!");
                completionText.setFont(Font.font("Arial", FontWeight.BOLD, 22));
                completionText.setFill(Color.GOLD);
                completionText.setStroke(Color.rgb(100, 100, 100, 0.3));
                completionText.setStrokeWidth(0.5);
                statsCard.getChildren().add(0, completionText);
                statsCard.getChildren().add(1, new Region());
                buttonContainer.getChildren().add(menuButton);
            }
            
            // 添加所有元素到主容器
            messageBox.getChildren().addAll(
                victoryText,
                new Separator(),
                statsCard,
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
        });
    }
    
    /**
     * 创建统计卡片
     */
    private VBox createStatsCard(int level, int enemies, int enemyScore, 
                                String time, int timeScore, 
                                int lives, int lifeScore, 
                                int totalScore) {
        // 创建主容器
        VBox card = new VBox(12);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(25, 20, 25, 20));
        card.setStyle("-fx-background-color: rgba(0, 30, 60, 0.7);" +
                      "-fx-background-radius: 10;" +
                      "-fx-border-color: rgba(100, 150, 255, 0.4);" +
                      "-fx-border-width: 1;" +
                      "-fx-border-radius: 10;");
        
        // 标题行
        Text statsTitle = new Text("战斗统计");
        statsTitle.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        statsTitle.setFill(Color.rgb(180, 220, 255));
        
        // 创建漂亮的统计行
        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(15);
        statsGrid.setVgap(20);
        statsGrid.setAlignment(Pos.CENTER);
        
        // 计算关卡分数
        int levelScore = level * 1000;
        
        // 关卡奖励 - 带图标
        ImageView jiangbeiIcon = createIcon("/images/ui/jiangbei_icon.png", "🏆");
        addStatRow(statsGrid, 0, "关卡奖励", level + "关 × 1000", levelScore, jiangbeiIcon);
        
        // 击败敌人 - 带图标
        ImageView enemyIcon = createIcon("/images/ui/enemy_icon.png", "⚔");
        addStatRow(statsGrid, 1, "击败敌人", enemies + "个 × 200", enemyScore, enemyIcon);
        
        // 通关时间 - 带图标
        ImageView clockIcon = createIcon("/images/ui/clock_icon.png", "⏱");
        addStatRow(statsGrid, 2, "通关时间", time, timeScore, clockIcon);
        
        // 剩余生命 - 带图标
        ImageView heartIcon = createIcon("/images/ui/heart.png", "♥");
        addStatRow(statsGrid, 3, "剩余生命", lives + "条 × 500", lifeScore, heartIcon);
        
        // 分隔线
        Separator sep = new Separator();
        sep.setPrefWidth(400);
        
        // 总分显示
        HBox totalScoreBox = new HBox(10);
        totalScoreBox.setAlignment(Pos.CENTER);
        
        Text totalLabel = new Text("总积分");
        totalLabel.setFont(Font.font("Arial", FontWeight.BOLD, 26));
        totalLabel.setFill(Color.WHITE);
        
        Text totalValue = new Text(Integer.toString(totalScore));
        totalValue.setFont(Font.font("Arial", FontWeight.BLACK, 40));
        totalValue.setFill(Color.rgb(255, 215, 0));
        
        // 添加金色光效
        DropShadow goldGlow = new DropShadow();
        goldGlow.setColor(Color.rgb(255, 200, 0, 0.7));
        goldGlow.setRadius(10);
        goldGlow.setSpread(0.2);
        totalValue.setEffect(goldGlow);
        
        totalScoreBox.getChildren().addAll(totalLabel, totalValue);
        
        // 组装卡片
        card.getChildren().addAll(statsTitle, new Separator(), statsGrid, sep, totalScoreBox);
        
        return card;
    }
    
    /**
     * 添加统计行
     */
    private void addStatRow(GridPane grid, int row, String label, String value, int points, ImageView icon) {
        // 标签
        Text labelText = new Text(label);
        labelText.setFont(Font.font("Arial", FontWeight.MEDIUM, 18));
        labelText.setFill(Color.rgb(200, 220, 255));
        
        // 值
        Text valueText = new Text(value);
        valueText.setFont(Font.font("Arial", FontWeight.NORMAL, 18));
        valueText.setFill(Color.WHITE);
        
        // 分数
        Text pointsText = new Text(points + " 分");
        pointsText.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        pointsText.setFill(Color.rgb(255, 220, 100));
        
        // 添加到网格
        int col = 0;
        if (icon != null) {
            grid.add(icon, col, row);
        } else {
            // 如果没有图标，添加一个空的占位区域保持对齐
            Region placeholder = new Region();
            placeholder.setMinWidth(24);
            placeholder.setPrefWidth(24);
            grid.add(placeholder, col, row);
        }
        col++;
        
        grid.add(labelText, col++, row);
        grid.add(valueText, col++, row);
        grid.add(pointsText, col, row);
        
        // 设置列约束 - 只在第一行设置
        if (row == 0) {
            ColumnConstraints col1 = new ColumnConstraints();
            col1.setHalignment(javafx.geometry.HPos.LEFT);
            col1.setMinWidth(30);  // 确保图标列宽度一致
            
            ColumnConstraints col2 = new ColumnConstraints();
            col2.setHalignment(javafx.geometry.HPos.LEFT);
            col2.setMinWidth(120);
            
            ColumnConstraints col3 = new ColumnConstraints();
            col3.setHalignment(javafx.geometry.HPos.LEFT);
            col3.setPrefWidth(150);
            
            ColumnConstraints col4 = new ColumnConstraints();
            col4.setHalignment(javafx.geometry.HPos.RIGHT);
            col4.setPrefWidth(100);
            
            grid.getColumnConstraints().addAll(col1, col2, col3, col4);
        }
    }
    
    /**
     * 创建统计行图标
     */
    private ImageView createIcon(String path, String fallbackText) {
        try {
            Image img = new Image(getClass().getResourceAsStream(path));
            ImageView icon = new ImageView(img);
            icon.setFitWidth(24);
            icon.setFitHeight(24);
            return icon;
        } catch (Exception e) {
            // 如果图标加载失败，使用文本替代
            Text text = new Text(fallbackText);
            text.setFont(Font.font("Arial", FontWeight.BOLD, 18));
            text.setFill(Color.LIGHTSKYBLUE);
            
            StackPane iconPane = new StackPane(text);
            iconPane.setMinSize(24, 24);
            
            // 转换为ImageView (实际上不是真正的ImageView，但作为占位符使用)
            return new ImageView();
        }
    }
    
    /**
     * 创建按钮
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
                "-fx-background-color: linear-gradient(to bottom, " + toHexString(SECONDARY_COLOR) + ", #2D8D31);" +
                "-fx-background-radius: 25;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 18px;" +
                "-fx-font-weight: bold;" +
                "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.4), 8, 0, 0, 2);"
            );
        } else {
            // 次要按钮 - 较暗色
            button.setStyle(
                "-fx-background-color: linear-gradient(to bottom, " + toHexString(PRIMARY_COLOR) + ", #1A7CB8);" +
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
                    "-fx-background-color: linear-gradient(to bottom, #4CAF50, #388E3C);" +
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
                    "-fx-background-color: linear-gradient(to bottom, " + toHexString(SECONDARY_COLOR) + ", #2D8D31);" +
                    "-fx-background-radius: 25;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 18px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.4), 8, 0, 0, 2);"
                );
            } else {
                button.setStyle(
                    "-fx-background-color: linear-gradient(to bottom, " + toHexString(PRIMARY_COLOR) + ", #1A7CB8);" +
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
    
    /**
     * 计算单人游戏得分
     * @param level 当前关卡
     * @param defeatedEnemies 击败的敌人数量
     * @param totalGameTime 游戏总时间（毫秒）
     * @param playerLives 剩余生命数
     * @return 计算得到的总分
     */
    public int calculateScore(int level, int defeatedEnemies, long totalGameTime, int playerLives) {
        // 根据关卡、击败敌人数量和剩余时间计算得分
        int levelScore = level * 1000;
        int enemyScore = defeatedEnemies * 200;
        int timeScore = Math.max(0, 300000 - (int) totalGameTime) / 1000;
        int lifeScore = playerLives * 500;
        
        // 返回总分
        return levelScore + enemyScore + timeScore + lifeScore;
    }
} 