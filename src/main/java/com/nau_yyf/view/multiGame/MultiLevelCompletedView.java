package com.nau_yyf.view.multiGame;

import com.jfoenix.controls.JFXButton;
import com.nau_yyf.view.GameView;
import com.nau_yyf.view.LevelCompletedView;
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
import javafx.scene.effect.InnerShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

import java.util.Map;

/**
 * 双人模式关卡完成视图
 * 用于显示关卡完成后的双人游戏统计信息和动画效果
 */
public class MultiLevelCompletedView implements LevelCompletedView {

    private GameView gameView;
    private StackPane root;
    private Scene scene;
    private StackPane containerPane;
    
    // 界面颜色定义
    private final Color PRIMARY_COLOR = Color.rgb(37, 160, 218);   // 蓝色 - 玩家1
    private final Color SECONDARY_COLOR = Color.rgb(212, 57, 43);  // 红色 - 玩家2
    private final Color NEUTRAL_COLOR = Color.rgb(76, 175, 80);    // 绿色 - 中性/共同
    private final Color TEXT_COLOR = Color.WHITE;
    private final Color GOLD_COLOR = Color.rgb(255, 215, 0);       // 金色 - 奖励相关
    
    /**
     * 构造函数
     * @param gameView 游戏视图引用
     * @param root 根布局
     * @param scene 场景
     */
    public MultiLevelCompletedView(GameView gameView, StackPane root, Scene scene) {
        this.gameView = gameView;
        this.root = root;
        this.scene = scene;
    }
    
    /**
     * 显示关卡完成界面
     * @param levelData 关卡数据，包含当前关卡、击败敌人数、游戏时间等信息
     */
    @Override
    public void show(Map<String, Object> levelData) {
        // 从levelData中提取多人游戏需要的数据
        int currentLevel = (int) levelData.getOrDefault("currentLevel", 1);
        int p1DefeatedEnemies = (int) levelData.getOrDefault("p1DefeatedEnemies", 0);
        int p2DefeatedEnemies = (int) levelData.getOrDefault("p2DefeatedEnemies", 0);
        long totalGameTime = (long) levelData.getOrDefault("totalGameTime", 0L);
        int p1Lives = (int) levelData.getOrDefault("p1Lives", 0);
        int p2Lives = (int) levelData.getOrDefault("p2Lives", 0);
        String p1TankType = (String) levelData.getOrDefault("p1TankType", "standard");
        String p2TankType = (String) levelData.getOrDefault("p2TankType", "standard");
        int totalLevels = (int) levelData.getOrDefault("totalLevels", 5);
        
        // 调用原有的显示方法
        show(currentLevel, p1DefeatedEnemies, p2DefeatedEnemies, totalGameTime, 
             p1Lives, p2Lives, p1TankType, p2TankType, totalLevels);
    }
    
    /**
     * 显示关卡完成界面
     * @param currentLevel 当前关卡
     * @param p1DefeatedEnemies 玩家1击败的敌人数量
     * @param p2DefeatedEnemies 玩家2击败的敌人数量
     * @param totalGameTime 游戏总时间（毫秒）
     * @param p1Lives 玩家1剩余生命
     * @param p2Lives 玩家2剩余生命
     * @param p1TankType 玩家1坦克类型
     * @param p2TankType 玩家2坦克类型
     * @param totalLevels 游戏总关卡数
     */
    private void show(int currentLevel, 
                     int p1DefeatedEnemies, int p2DefeatedEnemies, 
                     long totalGameTime, 
                     int p1Lives, int p2Lives,
                     String p1TankType, String p2TankType,
                     int totalLevels) {
        // 如果已经显示了消息，不重复显示
        if (root.lookup("#multiLevelCompletedMessage") != null) return;
        
        Platform.runLater(() -> {
            // 创建消息框容器
            containerPane = new StackPane();
            containerPane.setId("multiLevelCompletedMessage");
            
            // 添加全屏暗色背景
            Rectangle darkOverlay = new Rectangle(scene.getWidth(), scene.getHeight());
            darkOverlay.setFill(Color.rgb(0, 0, 0, 0.8));
            
            // 创建主要内容面板
            VBox messageBox = new VBox(25);
            messageBox.setAlignment(Pos.CENTER);
            messageBox.setPadding(new Insets(40));
            messageBox.setMaxWidth(600);
            messageBox.setMaxHeight(680);
            
            // 设置渐变背景和边框效果
            messageBox.setStyle("-fx-background-color: linear-gradient(to bottom, rgba(20, 40, 70, 0.95), rgba(10, 20, 40, 0.95));" +
                                "-fx-background-radius: 20;" +
                                "-fx-border-color: linear-gradient(to right, " + toHexString(PRIMARY_COLOR) + "80, " + toHexString(SECONDARY_COLOR) + "80);" +
                                "-fx-border-width: 2;" +
                                "-fx-border-radius: 20;" + 
                                "-fx-effect: dropshadow(gaussian, rgba(0, 150, 255, 0.5), 20, 0, 0, 0);");
            
            // 标题区域
            Text victoryText = new Text("关卡完成");
            victoryText.setFont(Font.font("Microsoft YaHei", FontWeight.BLACK, 48));
            victoryText.setFill(Color.WHITE);
            
            // 添加金色描边
            victoryText.setStroke(GOLD_COLOR.deriveColor(0, 1, 1, 0.8));
            victoryText.setStrokeWidth(1.5);
            
            // 添加文字阴影
            DropShadow textShadow = new DropShadow();
            textShadow.setColor(Color.rgb(0, 150, 255, 0.7));
            textShadow.setRadius(15);
            textShadow.setSpread(0.4);
            victoryText.setEffect(textShadow);
            
            // 计算统计数据
            int totalDefeatedEnemies = p1DefeatedEnemies + p2DefeatedEnemies;
            int levelScore = currentLevel * 1000;
            int enemyScore = totalDefeatedEnemies * 200;
            
            // 计算玩家1和玩家2的个人分数
            int p1EnemyScore = p1DefeatedEnemies * 200;
            int p2EnemyScore = p2DefeatedEnemies * 200;
            int p1LifeScore = p1Lives * 500;
            int p2LifeScore = p2Lives * 500;
            
            // 计算玩家比例
            double p1Percentage = (double)p1DefeatedEnemies / (totalDefeatedEnemies == 0 ? 1 : totalDefeatedEnemies);
            p1Percentage = Math.round(p1Percentage * 100.0) / 100.0; // 保留两位小数
            
            // 通关时间计算
            long seconds = totalGameTime / 1000;
            long minutes = seconds / 60;
            seconds = seconds % 60;
            String timeString = String.format("%02d:%02d", minutes, seconds);
            
            // 时间得分（越快越高）
            int timeScore = Math.max(0, 300000 - (int) totalGameTime) / 1000;
            
            // 计算总分
            int totalScore = calculateTotalScore(
                currentLevel, p1DefeatedEnemies, p2DefeatedEnemies,
                totalGameTime, p1Lives, p2Lives
            );
            
            // 创建总体战斗统计卡片
            HBox overallStatsCard = createOverallStatsCard(
                currentLevel, levelScore,
                totalDefeatedEnemies, enemyScore,
                timeString, timeScore,
                totalScore
            );
            
            // 创建玩家比较统计卡片
            HBox playersComparisonCard = createPlayersComparisonCard(
                p1DefeatedEnemies, p2DefeatedEnemies,
                p1Lives, p2Lives,
                p1EnemyScore, p2EnemyScore,
                p1LifeScore, p2LifeScore
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
                    // 先移除当前界面
                    root.getChildren().remove(containerPane);
                    
                    // 强制清理游戏资源和所有UI界面
                    gameView.cleanupGameResources();
                    
                    // 确保游戏状态设置为非暂停
                    gameView.setGamePaused(false);
                    
                    // 短暂延迟启动下一关，确保清理完成
                    javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(javafx.util.Duration.millis(100));
                    delay.setOnFinished(event -> {
                        // 使用数组形式的参数调用startGameWithLevel方法
                        String[] tankTypes = new String[]{p1TankType, p2TankType};
                        gameView.startGameWithLevel(tankTypes, currentLevel + 1);
                    });
                    delay.play();
                });
                buttonContainer.getChildren().addAll(menuButton, nextButton);
            } else {
                // 最终关卡通关
                Text completionText = new Text("恭喜双方玩家已完成所有关卡!");
                completionText.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 24));
                completionText.setFill(GOLD_COLOR);
                completionText.setStroke(Color.rgb(100, 100, 100, 0.3));
                completionText.setStrokeWidth(0.5);
                
                VBox completionBox = new VBox(10, completionText);
                completionBox.setAlignment(Pos.CENTER);
                
                buttonContainer.getChildren().add(menuButton);
                
                // 添加完成文本到消息框内容中
                messageBox.getChildren().add(completionBox);
            }
            
            // 添加所有元素到主容器
            messageBox.getChildren().addAll(
                victoryText,
                new Separator(),
                overallStatsCard,
                new Separator(),
                playersComparisonCard,
                new Separator(),
                buttonContainer
            );
            
            // 如果是最终关卡通关，调整子元素顺序
            if (currentLevel >= totalLevels) {
                messageBox.getChildren().remove(buttonContainer);
                messageBox.getChildren().add(buttonContainer);
            }
            
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
     * 创建总体战斗统计卡片
     */
    private HBox createOverallStatsCard(int level, int levelScore,
                                       int totalEnemies, int enemyScore,
                                       String time, int timeScore,
                                       int totalScore) {
        // 创建卡片主容器
        HBox cardContainer = new HBox(10);
        
        // 创建左侧统计区域
        VBox statsContainer = new VBox(10);
        statsContainer.setPadding(new Insets(15));
        statsContainer.setMinWidth(250);
        statsContainer.setStyle("-fx-background-color: rgba(20, 40, 70, 0.6);" +
                               "-fx-background-radius: 10;" +
                               "-fx-border-color: rgba(255, 255, 255, 0.2);" +
                               "-fx-border-width: 1;" +
                               "-fx-border-radius: 10;");
        
        // 标题
        Text statsTitle = new Text("总体战斗统计");
        statsTitle.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 20));
        statsTitle.setFill(NEUTRAL_COLOR);
        
        // 创建数据网格
        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(20);
        statsGrid.setVgap(15);
        
        // 关卡数据行
        addStatRow(statsGrid, 0, "关卡奖励", level + "关 × 1000", levelScore, createIcon("/images/ui/jiangbei_icon.png"));
        
        // 总击败敌人数据行
        addStatRow(statsGrid, 1, "总击败敌人", totalEnemies + "个 × 200", enemyScore, createIcon("/images/ui/enemy_icon.png"));
        
        // 通关时间数据行
        addStatRow(statsGrid, 2, "通关时间", time, timeScore, createIcon("/images/ui/clock_icon.png"));
        
        // 将统计数据添加到容器
        statsContainer.getChildren().addAll(statsTitle, statsGrid);
        
        // 创建右侧总分显示区域
        VBox scoreContainer = new VBox(10);
        scoreContainer.setAlignment(Pos.CENTER);
        scoreContainer.setPadding(new Insets(20));
        scoreContainer.setMinWidth(150);
        scoreContainer.setStyle("-fx-background-color: rgba(40, 60, 100, 0.6);" +
                               "-fx-background-radius: 10;" +
                               "-fx-border-color: rgba(255, 215, 0, 0.3);" +
                               "-fx-border-width: 1;" +
                               "-fx-border-radius: 10;");
        
        // 总分标题
        Text scoreTitleText = new Text("总分");
        scoreTitleText.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 22));
        scoreTitleText.setFill(TEXT_COLOR);
        
        // 装饰线
        Rectangle decorLine = new Rectangle(60, 3);
        decorLine.setFill(GOLD_COLOR);
        decorLine.setArcWidth(3);
        decorLine.setArcHeight(3);
        
        // 总分值
        Text scoreValueText = new Text(String.valueOf(totalScore));
        scoreValueText.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 46));
        scoreValueText.setFill(GOLD_COLOR);
        
        // 添加金色光效
        DropShadow goldGlow = new DropShadow();
        goldGlow.setColor(GOLD_COLOR.deriveColor(0, 1, 1, 0.7));
        goldGlow.setRadius(15);
        goldGlow.setSpread(0.3);
        scoreValueText.setEffect(goldGlow);
        
        // 添加到容器
        scoreContainer.getChildren().addAll(scoreTitleText, decorLine, scoreValueText);
        
        // 将左右两部分添加到主容器
        cardContainer.getChildren().addAll(statsContainer, scoreContainer);
        
        return cardContainer;
    }
    
    /**
     * 创建玩家比较统计卡片
     */
    private HBox createPlayersComparisonCard(int p1Enemies, int p2Enemies,
                                            int p1Lives, int p2Lives,
                                            int p1EnemyScore, int p2EnemyScore,
                                            int p1LifeScore, int p2LifeScore) {
        // 创建玩家比较容器
        HBox container = new HBox(10);
        
        // 玩家1卡片
        VBox player1Card = createPlayerCard(
            "玩家1 (蓝方)", 
            p1Enemies, p1EnemyScore,
            p1Lives, p1LifeScore,
            p1EnemyScore + p1LifeScore,
            PRIMARY_COLOR
        );
        
        // 中间比较图表
        VBox comparisonChart = createComparisonChart(p1Enemies, p2Enemies);
        
        // 玩家2卡片
        VBox player2Card = createPlayerCard(
            "玩家2 (红方)", 
            p2Enemies, p2EnemyScore,
            p2Lives, p2LifeScore,
            p2EnemyScore + p2LifeScore,
            SECONDARY_COLOR
        );
        
        // 修改玩家卡片宽度
        player1Card.setPrefWidth(210);
        player2Card.setPrefWidth(210);
        
        // 添加到容器
        container.getChildren().addAll(player1Card, comparisonChart, player2Card);
        
        return container;
    }
    
    /**
     * 创建玩家卡片
     */
    private VBox createPlayerCard(String playerName, 
                                 int enemies, int enemyScore,
                                 int lives, int lifeScore,
                                 int totalScore,
                                 Color playerColor) {
        // 创建卡片容器
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setPrefWidth(210);
        
        // 设置卡片样式
        String colorHex = toHexString(playerColor);
        card.setStyle("-fx-background-color: rgba(20, 40, 70, 0.6);" +
                     "-fx-background-radius: 10;" +
                     "-fx-border-color: " + colorHex + "80;" +
                     "-fx-border-width: 2;" +
                     "-fx-border-radius: 10;");
        
        // 添加内部发光效果
        InnerShadow innerGlow = new InnerShadow();
        innerGlow.setColor(playerColor.deriveColor(0, 1, 1.2, 0.3));
        innerGlow.setRadius(5);
        innerGlow.setChoke(0.2);
        card.setEffect(innerGlow);
        
        // 玩家标题
        Text titleText = new Text(playerName);
        titleText.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 20));
        titleText.setFill(playerColor);
        
        // 装饰线
        Rectangle decorLine = new Rectangle(80, 3);
        decorLine.setFill(playerColor);
        decorLine.setArcWidth(3);
        decorLine.setArcHeight(3);
        
        // 创建数据网格
        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(10);
        statsGrid.setVgap(12);
        statsGrid.setAlignment(Pos.CENTER_LEFT);
        
        // 击败敌人数据行
        addPlayerStatRow(statsGrid, 0, "击败敌人", enemies + "个", enemyScore + "分", playerColor);
        
        // 剩余生命数据行
        addPlayerStatRow(statsGrid, 1, "剩余生命", lives + "条", lifeScore + "分", playerColor);
        
        // 总分行
        addPlayerStatRow(statsGrid, 2, "个人总分", "", totalScore + "分", playerColor);
        
        // 添加到卡片
        card.getChildren().addAll(titleText, decorLine, statsGrid);
        
        return card;
    }
    
    /**
     * 创建比较图表
     */
    private VBox createComparisonChart(int p1Enemies, int p2Enemies) {
        // 创建比较容器
        VBox container = new VBox(15);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(20, 10, 20, 10));
        
        // 计算总坦克数和百分比
        int totalEnemies = p1Enemies + p2Enemies;
        double p1Percentage = totalEnemies > 0 ? (double)p1Enemies / totalEnemies : 0.5;
        double p2Percentage = totalEnemies > 0 ? (double)p2Enemies / totalEnemies : 0.5;
        
        // 标题
        Text titleText = new Text("贡献比例");
        titleText.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 16));
        titleText.setFill(TEXT_COLOR);
        
        // 创建比例图
        StackPane chartPane = new StackPane();
        chartPane.setMinHeight(150);
        chartPane.setPrefWidth(120);
        
        // 背景
        Rectangle chartBg = new Rectangle(100, 150);
        chartBg.setFill(Color.rgb(30, 40, 60, 0.6));
        chartBg.setArcWidth(5);
        chartBg.setArcHeight(5);
        
        // 玩家1比例条
        Rectangle p1Bar = new Rectangle(100, p1Percentage * 150);
        p1Bar.setFill(PRIMARY_COLOR.deriveColor(0, 0.9, 0.9, 0.9));
        p1Bar.setArcWidth(0);
        p1Bar.setArcHeight(0);
        p1Bar.setTranslateY((150 - p1Percentage * 150) / 2);
        
        // 玩家2比例条
        Rectangle p2Bar = new Rectangle(100, p2Percentage * 150);
        p2Bar.setFill(SECONDARY_COLOR.deriveColor(0, 0.9, 0.9, 0.9));
        p2Bar.setArcWidth(0);
        p2Bar.setArcHeight(0);
        p2Bar.setTranslateY(-(150 - p2Percentage * 150) / 2);
        
        // 比例文本显示
        Text p1Text = new Text(String.format("%.1f%%", p1Percentage * 100));
        p1Text.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 14));
        p1Text.setFill(Color.WHITE);
        p1Text.setTranslateY(-40);
        
        Text p2Text = new Text(String.format("%.1f%%", p2Percentage * 100));
        p2Text.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 14));
        p2Text.setFill(Color.WHITE);
        p2Text.setTranslateY(40);
        
        // 图例
        HBox legend = new HBox(10);
        legend.setAlignment(Pos.CENTER);
        
        // 玩家1图例
        Rectangle p1Color = new Rectangle(12, 12);
        p1Color.setFill(PRIMARY_COLOR);
        p1Color.setArcWidth(2);
        p1Color.setArcHeight(2);
        
        Text p1Legend = new Text("玩家1");
        p1Legend.setFont(Font.font("Microsoft YaHei", 12));
        p1Legend.setFill(PRIMARY_COLOR);
        
        HBox p1LegendBox = new HBox(5, p1Color, p1Legend);
        p1LegendBox.setAlignment(Pos.CENTER);
        
        // 玩家2图例
        Rectangle p2Color = new Rectangle(12, 12);
        p2Color.setFill(SECONDARY_COLOR);
        p2Color.setArcWidth(2);
        p2Color.setArcHeight(2);
        
        Text p2Legend = new Text("玩家2");
        p2Legend.setFont(Font.font("Microsoft YaHei", 12));
        p2Legend.setFill(SECONDARY_COLOR);
        
        HBox p2LegendBox = new HBox(5, p2Color, p2Legend);
        p2LegendBox.setAlignment(Pos.CENTER);
        
        // 组合图例
        VBox legendBox = new VBox(5, p1LegendBox, p2LegendBox);
        legendBox.setAlignment(Pos.CENTER);
        
        // 组合图表
        chartPane.getChildren().addAll(chartBg, p1Bar, p2Bar, p1Text, p2Text);
        
        // 添加到容器
        container.getChildren().addAll(titleText, chartPane, legendBox);
        
        return container;
    }
    
    /**
     * 添加统计行
     */
    private void addStatRow(GridPane grid, int row, String label, String value, int points, ImageView icon) {
        // 标签
        Text labelText = new Text(label);
        labelText.setFont(Font.font("Microsoft YaHei", FontWeight.MEDIUM, 16));
        labelText.setFill(Color.rgb(200, 220, 255));
        
        // 值
        Text valueText = new Text(value);
        valueText.setFont(Font.font("Microsoft YaHei", FontWeight.NORMAL, 16));
        valueText.setFill(TEXT_COLOR);
        
        // 分数
        Text pointsText = new Text(points + " 分");
        pointsText.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 16));
        pointsText.setFill(GOLD_COLOR);
        
        // 添加到网格
        if (icon != null) {
            grid.add(icon, 0, row);
            grid.add(labelText, 1, row);
            grid.add(valueText, 2, row);
            grid.add(pointsText, 3, row);
        } else {
            // 如果没有图标，使用一个空占位符保持对齐
            Region spacer = new Region();
            spacer.setMinWidth(24);
            grid.add(spacer, 0, row);
            grid.add(labelText, 1, row);
            grid.add(valueText, 2, row);
            grid.add(pointsText, 3, row);
        }
    }
    
    /**
     * 添加玩家统计行
     */
    private void addPlayerStatRow(GridPane grid, int row, String label, String value, String points, Color playerColor) {
        // 标签
        Text labelText = new Text(label);
        labelText.setFont(Font.font("Microsoft YaHei", FontWeight.MEDIUM, 14));
        labelText.setFill(Color.rgb(200, 220, 255));
        
        // 值
        Text valueText = new Text(value);
        valueText.setFont(Font.font("Microsoft YaHei", FontWeight.NORMAL, 14));
        valueText.setFill(TEXT_COLOR);
        
        // 分数
        Text pointsText = new Text(points);
        pointsText.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 16));
        pointsText.setFill(playerColor.brighter());
        
        // 添加到网格
        grid.add(labelText, 0, row);
        
        if (!value.isEmpty()) {
            grid.add(valueText, 1, row);
        }
        
        grid.add(pointsText, 2, row);
    }
    
    /**
     * 创建图标
     */
    private ImageView createIcon(String path) {
        try {
            Image img = new Image(getClass().getResourceAsStream(path));
            ImageView icon = new ImageView(img);
            icon.setFitWidth(24);
            icon.setFitHeight(24);
            return icon;
        } catch (Exception e) {
            // 如果无法加载图标，返回一个空图标
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
            // 主要按钮 - 绿色
            button.setStyle(
                "-fx-background-color: linear-gradient(to bottom, " + toHexString(NEUTRAL_COLOR) + ", #388E3C);" +
                "-fx-background-radius: 25;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 18px;" +
                "-fx-font-weight: bold;" +
                "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.4), 8, 0, 0, 2);"
            );
        } else {
            // 次要按钮 - 蓝色
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
                    "-fx-background-color: linear-gradient(to bottom, " + toHexString(NEUTRAL_COLOR) + ", #388E3C);" +
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
        
        // 确保按钮可以获得焦点
        button.setFocusTraversable(true);
        
        return button;
    }
    
    /**
     * 计算总分
     */
    private int calculateTotalScore(int level, int p1DefeatedEnemies, int p2DefeatedEnemies, 
                                  long totalGameTime, int p1Lives, int p2Lives) {
        // 关卡分数
        int levelScore = level * 1000;
        
        // 击败敌人分数
        int totalEnemiesScore = (p1DefeatedEnemies + p2DefeatedEnemies) * 200;
        
        // 时间分数 (越快越高)
        int timeScore = Math.max(0, 300000 - (int)totalGameTime) / 1000;
        
        // 生命分数
        int livesScore = (p1Lives + p2Lives) * 500;
        
        // 计算总分
        return levelScore + totalEnemiesScore + timeScore + livesScore;
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