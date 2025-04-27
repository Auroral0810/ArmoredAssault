package com.nau_yyf.view.multiGame;

import com.jfoenix.controls.JFXButton;
import com.nau_yyf.util.TankUtil;
import com.nau_yyf.view.GameView;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 双人坦克选择界面视图组件
 */
public class MultiTankSelectionView {
    
    private GameView gameView;
    private StackPane root;
    private Stage stage;
    
    // 使用与单人游戏相同的主色调
    private final Color PRIMARY_COLOR = Color.rgb(37, 160, 218);    // 蓝色
    private final Color SECONDARY_COLOR = Color.rgb(212, 57, 43);     // 红色 - 仅用于标识玩家2
    private final Color TEXT_COLOR = Color.WHITE;
    private final Color BACKGROUND_COLOR = Color.rgb(27, 40, 56);   // 深蓝灰色
    private final Color DISABLED_COLOR = Color.rgb(100, 100, 100);  // 灰色 - 禁用状态
    
    // 坦克选择界面组件
    private BorderPane tankSelectionLayout;
    private List<VBox> p1TankOptionContainers = new ArrayList<>();
    private List<VBox> p2TankOptionContainers = new ArrayList<>();
    private List<JFXButton> p1TankSelectButtons = new ArrayList<>();
    private List<JFXButton> p2TankSelectButtons = new ArrayList<>();
    private int p1SelectedTankType = 0; // 默认选择light坦克
    private int p2SelectedTankType = 1; // 默认选择standard坦克
    
    /**
     * 构造函数
     * @param gameView 游戏主视图的引用
     * @param root 根布局容器
     * @param stage 主舞台
     */
    public MultiTankSelectionView(GameView gameView, StackPane root, Stage stage) {
        this.gameView = gameView;
        this.root = root;
        this.stage = stage;
    }
    
    /**
     * 显示坦克选择界面
     */
    public void show() {
        Platform.runLater(() -> {
            // 清除当前内容
            root.getChildren().clear();
            
            // 设置根布局背景色
            root.setStyle("-fx-background-color: " + toHexString(BACKGROUND_COLOR) + ";");

            // 如果选择界面尚未初始化，创建它
            if (tankSelectionLayout == null) {
                initializeTankSelectionUI();
            } else {
                // 只更新选择状态
                updateTankSelection();
            }

            // 将主布局添加到根布局
            root.getChildren().add(tankSelectionLayout);
        });
    }
    
    /**
     * 初始化坦克选择界面UI
     */
    private void initializeTankSelectionUI() {
        // 创建主布局
        tankSelectionLayout = new BorderPane();
        
        // 标题
        Text titleText = new Text("选择你们的坦克");
        titleText.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        titleText.setFill(PRIMARY_COLOR);
        
        StackPane titlePane = new StackPane(titleText);
        titlePane.setPadding(new Insets(20, 0, 10, 0));
        tankSelectionLayout.setTop(titlePane);
        
        // 创建可滚动的玩家区域容器
        VBox playersContainer = new VBox(15);
        playersContainer.setAlignment(Pos.CENTER);
        
        // 玩家1区域
        VBox player1Area = createPlayerSelectionArea("玩家1 (蓝色)", PRIMARY_COLOR, p1TankOptionContainers, 
                                                     p1TankSelectButtons, p1SelectedTankType, 1);
        
        // 分隔线
        Separator separator = new Separator();
        separator.setPrefWidth(600);
        
        // 玩家2区域
        VBox player2Area = createPlayerSelectionArea("玩家2 (红色)", SECONDARY_COLOR, p2TankOptionContainers, 
                                                     p2TankSelectButtons, p2SelectedTankType, 2);
        
        // 将玩家区域添加到容器
        playersContainer.getChildren().addAll(player1Area, separator, player2Area);
        playersContainer.setPadding(new Insets(15));
        
        // 创建滚动面板
        ScrollPane scrollPane = new ScrollPane(playersContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPannable(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        
        tankSelectionLayout.setCenter(scrollPane);
        
        // 底部按钮区域
        HBox bottomButtons = new HBox(20);
        bottomButtons.setAlignment(Pos.CENTER);
        bottomButtons.setPadding(new Insets(20));
        
        // 返回按钮
        JFXButton backButton = createMenuButton("返回", e -> gameView.showMultiPlayerOptions(), false);
        
        // 开始游戏按钮
        JFXButton startButton = createMenuButton("开始游戏", e -> startMultiplayerGame(), true);
        
        bottomButtons.getChildren().addAll(backButton, startButton);
        
        // 底部提示
        Text tipText = new Text("提示: 两位玩家必须选择不同的坦克类型");
        tipText.setFill(TEXT_COLOR);
        tipText.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        
        VBox bottomContainer = new VBox(10, bottomButtons, tipText);
        bottomContainer.setAlignment(Pos.CENTER);
        bottomContainer.setPadding(new Insets(10, 0, 20, 0));
        
        tankSelectionLayout.setBottom(bottomContainer);
        
        // 初始更新选择状态
        updateTankSelection();
        
        // 初始禁用已选坦克
        disableSelectedTanks();
    }
    
    /**
     * 创建玩家选择区域
     */
    private VBox createPlayerSelectionArea(String playerTitle, Color playerColor, 
                                          List<VBox> tankOptionContainers,
                                          List<JFXButton> tankSelectButtons, 
                                          int selectedTankType,
                                          int playerNumber) {
        
        VBox playerArea = new VBox(10);
        playerArea.setAlignment(Pos.CENTER);
        
        // 玩家标题
        Text playerText = new Text(playerTitle);
        playerText.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        playerText.setFill(playerColor);
        
        // 坦克选择区域
        HBox tankSelectionArea = new HBox(25);
        tankSelectionArea.setAlignment(Pos.CENTER);
        
        // 添加三种类型的坦克
        for (int i = 0; i < TankUtil.TANK_TYPES.size(); i++) {
            final int index = i;
            String tankType = TankUtil.TANK_TYPES.get(i);
            
            VBox tankOption = new VBox(8);
            tankOption.setAlignment(Pos.CENTER);
            tankOption.setPadding(new Insets(8));
            
            // 坦克预览
            ImageView tankImage = null;
            try {
                // 使用PNG格式加载图片
                String imagePath = TankUtil.getTankImagePath(tankType, true);
                
                InputStream imageStream = getClass().getResourceAsStream(imagePath);
                if (imageStream != null) {
                    Image tankImg = new Image(imageStream);
                    tankImage = new ImageView(tankImg);
                } else {
                    // PNG加载失败，创建占位符
                    throw new Exception("PNG图片未找到");
                }
                
                // 设置图片大小
                tankImage.setFitWidth(70);
                tankImage.setFitHeight(70);
                tankImage.setPreserveRatio(true);
            } catch (Exception e) {
                // 创建一个简单的占位符
                javafx.scene.shape.Rectangle rect = new javafx.scene.shape.Rectangle(70, 70);
                if (tankType.equals("light")) {
                    rect.setFill(Color.rgb(120, 200, 80));  // 浅绿色
                } else if (tankType.equals("standard")) {
                    rect.setFill(Color.rgb(80, 150, 220));  // 蓝色
                } else {
                    rect.setFill(Color.rgb(200, 80, 80));   // 红色
                }
                rect.setArcWidth(15);
                rect.setArcHeight(15);
                
                // 创建坦克形状轮廓
                javafx.scene.shape.Rectangle body = new javafx.scene.shape.Rectangle(45, 25);
                body.setFill(Color.rgb(50, 50, 50));
                body.setArcWidth(8);
                body.setArcHeight(8);
                
                javafx.scene.shape.Rectangle barrel = new javafx.scene.shape.Rectangle(35, 8);
                barrel.setFill(Color.rgb(70, 70, 70));
                barrel.setArcWidth(3);
                barrel.setArcHeight(3);
                
                Group tankShape = new Group(body, barrel);
                barrel.setTranslateX(20);
                
                StackPane placeholder = new StackPane(rect, tankShape);
                placeholder.setMaxSize(70, 70);
                
                // 将占位符添加到布局中
                tankOption.getChildren().add(placeholder);
                
                System.err.println("无法加载坦克图片: " + e.getMessage());
                continue;
            }
            
            // 坦克名称和属性
            Text tankName = new Text(TankUtil.getTankDisplayName(tankType));
            tankName.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            tankName.setFill(TEXT_COLOR);
            
            Text tankDesc = new Text(TankUtil.getTankDescription(tankType));
            tankDesc.setFont(Font.font("Arial", 12));
            tankDesc.setFill(TEXT_COLOR);
            tankDesc.setWrappingWidth(150);
            tankDesc.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
            
            // 选择按钮 - 使用玩家对应的颜色
            JFXButton selectButton = new JFXButton("选择");
            selectButton.getStyleClass().add("tank-select-button");
            selectButton.setButtonType(JFXButton.ButtonType.RAISED);
            
            if (index == selectedTankType) {
                selectButton.setStyle("-fx-background-color: " + toHexString(playerColor) + ";");
                selectButton.setText("已选择");
            } else {
                selectButton.setStyle("-fx-background-color: #555555;");
            }
            
            // 添加点击事件
            final int finalIndex = index;
            selectButton.setOnAction(e -> {
                // 更新选择
                if (playerNumber == 1) {
                    p1SelectedTankType = finalIndex;
                } else {
                    p2SelectedTankType = finalIndex;
                }
                
                // 更新选择状态和禁用状态
                updateTankSelection();
                disableSelectedTanks();
            });
            
            // 保存按钮引用
            tankSelectButtons.add(selectButton);
            
            // 将元素添加到坦克选项容器
            tankOption.getChildren().addAll(tankImage, tankName, tankDesc, selectButton);
            
            // 保存容器引用
            tankOptionContainers.add(tankOption);
            
            // 添加到选择区域
            tankSelectionArea.getChildren().add(tankOption);
        }
        
        // 添加到玩家区域
        playerArea.getChildren().addAll(playerText, tankSelectionArea);
        
        return playerArea;
    }
    
    /**
     * 更新坦克选择状态
     */
    private void updateTankSelection() {
        // 更新玩家1的选择
        for (int i = 0; i < p1TankOptionContainers.size(); i++) {
            VBox tankOption = p1TankOptionContainers.get(i);
            JFXButton selectButton = p1TankSelectButtons.get(i);
            
            if (i == p1SelectedTankType) {
                // 选中样式
                tankOption.setStyle("-fx-border-color: " + toHexString(PRIMARY_COLOR) + "; " +
                        "-fx-border-width: 3; " +
                        "-fx-border-radius: 5; " +
                        "-fx-padding: 5;");
                selectButton.setStyle("-fx-background-color: " + toHexString(PRIMARY_COLOR) + ";");
                selectButton.setText("已选择");
            } else {
                // 未选中样式
                tankOption.setStyle("-fx-padding: 8;"); // 保持相同的总尺寸
                selectButton.setStyle("-fx-background-color: #555555;");
                selectButton.setText("选择");
            }
        }
        
        // 更新玩家2的选择
        for (int i = 0; i < p2TankOptionContainers.size(); i++) {
            VBox tankOption = p2TankOptionContainers.get(i);
            JFXButton selectButton = p2TankSelectButtons.get(i);
            
            if (i == p2SelectedTankType) {
                // 选中样式
                tankOption.setStyle("-fx-border-color: " + toHexString(SECONDARY_COLOR) + "; " +
                        "-fx-border-width: 3; " +
                        "-fx-border-radius: 5; " +
                        "-fx-padding: 5;");
                selectButton.setStyle("-fx-background-color: " + toHexString(SECONDARY_COLOR) + ";");
                selectButton.setText("已选择");
            } else {
                // 未选中样式
                tankOption.setStyle("-fx-padding: 8;"); // 保持相同的总尺寸
                selectButton.setStyle("-fx-background-color: #555555;");
                selectButton.setText("选择");
            }
        }
    }
    
    /**
     * 禁用已被另一玩家选择的坦克
     */
    private void disableSelectedTanks() {
        // 禁用玩家2已选择的坦克给玩家1
        JFXButton p1DisabledButton = p1TankSelectButtons.get(p2SelectedTankType);
        if (p1SelectedTankType != p2SelectedTankType) {
            p1DisabledButton.setDisable(true);
            p1DisabledButton.setStyle("-fx-background-color: " + toHexString(DISABLED_COLOR) + ";");
            p1DisabledButton.setText("已被选择");
        }
        
        // 禁用玩家1已选择的坦克给玩家2
        JFXButton p2DisabledButton = p2TankSelectButtons.get(p1SelectedTankType);
        if (p1SelectedTankType != p2SelectedTankType) {
            p2DisabledButton.setDisable(true);
            p2DisabledButton.setStyle("-fx-background-color: " + toHexString(DISABLED_COLOR) + ";");
            p2DisabledButton.setText("已被选择");
        }
        
        // 启用所有其他按钮
        for (int i = 0; i < TankUtil.TANK_TYPES.size(); i++) {
            if (i != p1SelectedTankType && i != p2SelectedTankType) {
                p1TankSelectButtons.get(i).setDisable(false);
                p2TankSelectButtons.get(i).setDisable(false);
                
                // 重置未选中按钮样式
                if (i != p1SelectedTankType) {
                    p1TankSelectButtons.get(i).setStyle("-fx-background-color: #555555;");
                    p1TankSelectButtons.get(i).setText("选择");
                }
                
                if (i != p2SelectedTankType) {
                    p2TankSelectButtons.get(i).setStyle("-fx-background-color: #555555;");
                    p2TankSelectButtons.get(i).setText("选择");
                }
            }
        }
    }
    
    /**
     * 开始双人游戏
     */
    private void startMultiplayerGame() {
        String p1TankType = TankUtil.TANK_TYPES.get(p1SelectedTankType);
        String p2TankType = TankUtil.TANK_TYPES.get(p2SelectedTankType);
        
        // 如果两个玩家选择了相同的坦克，显示提示信息
        if (p1SelectedTankType == p2SelectedTankType) {
            gameView.showMessage("两位玩家不能选择相同的坦克！");
            return;
        }
        
        // 显示关卡选择对话框
        gameView.showMultiPlayerLevelSelection(p1TankType, p2TankType);
    }
    
    /**
     * 创建菜单按钮
     * @param text 按钮文本
     * @param action 按钮动作
     * @param isPrimary 是否是主按钮
     * @return 配置好的JFXButton
     */
    private JFXButton createMenuButton(String text, javafx.event.EventHandler<javafx.event.ActionEvent> action, boolean isPrimary) {
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
    
    /**
     * 获取玩家1选择的坦克类型索引
     * @return 坦克类型索引
     */
    public int getP1SelectedTankType() {
        return p1SelectedTankType;
    }
    
    /**
     * 获取玩家2选择的坦克类型索引
     * @return 坦克类型索引
     */
    public int getP2SelectedTankType() {
        return p2SelectedTankType;
    }
} 