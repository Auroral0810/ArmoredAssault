package com.nau_yyf.view.singleGame;

import com.jfoenix.controls.JFXButton;
import com.nau_yyf.util.TankUtil;
import com.nau_yyf.view.GameView;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
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
 * 坦克选择界面视图组件
 */
public class SingleTankSelectionView {
    
    private GameView gameView;
    private StackPane root;
    private Stage stage;
    
    // 从GameView中提取的常量
    private final Color PRIMARY_COLOR = Color.rgb(37, 160, 218);    // 蓝色
    private final Color SECONDARY_COLOR = Color.rgb(76, 175, 80);   // 绿色
    private final Color TEXT_COLOR = Color.WHITE;
    
    // 坦克选择界面组件
    private BorderPane tankSelectionLayout;
    private List<VBox> tankOptionContainers = new ArrayList<>();
    private List<ImageView> tankImages = new ArrayList<>();
    private List<JFXButton> tankSelectButtons = new ArrayList<>();
    private int selectedTankType = 1; // 默认选择standard坦克
    
    /**
     * 构造函数
     * @param gameView 游戏主视图的引用
     * @param root 根布局容器
     * @param stage 主舞台
     */
    public SingleTankSelectionView(GameView gameView, StackPane root, Stage stage) {
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
        Text titleText = new Text("选择你的坦克");
        titleText.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        titleText.setFill(PRIMARY_COLOR);

        StackPane titlePane = new StackPane(titleText);
        titlePane.setPadding(new Insets(30, 0, 30, 0));
        tankSelectionLayout.setTop(titlePane);

        // 坦克选择区域
        HBox tankSelectionArea = new HBox(50);
        tankSelectionArea.setAlignment(Pos.CENTER);
        tankSelectionArea.setPadding(new Insets(50));

        // 添加三种类型的坦克
        for (int i = 0; i < TankUtil.TANK_TYPES.size(); i++) {
            final int index = i;
            String tankType = TankUtil.TANK_TYPES.get(i);

            VBox tankOption = new VBox(15);
            tankOption.setAlignment(Pos.CENTER);

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
                tankImage.setFitWidth(120);
                tankImage.setFitHeight(120);
                tankImage.setPreserveRatio(true);

                // 保存到列表中
                tankImages.add(tankImage);
            } catch (Exception e) {
                // 创建一个简单的占位符
                javafx.scene.shape.Rectangle rect = new javafx.scene.shape.Rectangle(120, 120);
                if (tankType.equals("light")) {
                    rect.setFill(Color.rgb(120, 200, 80));  // 浅绿色
                } else if (tankType.equals("standard")) {
                    rect.setFill(Color.rgb(80, 150, 220));  // 蓝色
                } else {
                    rect.setFill(Color.rgb(200, 80, 80));   // 红色
                }
                rect.setArcWidth(20);
                rect.setArcHeight(20);

                // 创建坦克形状轮廓
                javafx.scene.shape.Rectangle body = new javafx.scene.shape.Rectangle(80, 40);
                body.setFill(Color.rgb(50, 50, 50));
                body.setArcWidth(10);
                body.setArcHeight(10);

                javafx.scene.shape.Rectangle barrel = new javafx.scene.shape.Rectangle(60, 16);
                barrel.setFill(Color.rgb(70, 70, 70));
                barrel.setArcWidth(5);
                barrel.setArcHeight(5);

                Group tankShape = new Group(body, barrel);
                barrel.setTranslateX(30);

                StackPane placeholder = new StackPane(rect, tankShape);
                placeholder.setMaxSize(120, 120);

                // 将占位符添加到布局中
                tankOption.getChildren().add(placeholder);

                System.err.println("无法加载坦克图片: " + e.getMessage());
                continue;
            }

            // 坦克名称和属性
            Text tankName = new Text(TankUtil.getTankDisplayName(tankType));
            tankName.setFont(Font.font("Arial", FontWeight.BOLD, 18));
            tankName.setFill(TEXT_COLOR);

            Text tankDesc = new Text(TankUtil.getTankDescription(tankType));
            tankDesc.setFont(Font.font("Arial", 14));
            tankDesc.setFill(TEXT_COLOR);
            tankDesc.setWrappingWidth(200);
            tankDesc.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

            // 选择按钮
            JFXButton selectButton = new JFXButton("选择");
            selectButton.getStyleClass().add("tank-select-button");
            selectButton.setButtonType(JFXButton.ButtonType.RAISED);

            if (index == selectedTankType) {
                selectButton.setStyle("-fx-background-color: " + toHexString(SECONDARY_COLOR) + ";");
                selectButton.setText("已选择");
            } else {
                selectButton.setStyle("-fx-background-color: " + toHexString(PRIMARY_COLOR) + ";");
            }

            // 保存按钮引用
            tankSelectButtons.add(selectButton);

            selectButton.setOnAction(e -> {
                selectedTankType = index;
                updateTankSelection(); // 只更新选择状态，不重新加载
            });

            // 将元素添加到坦克选项容器
            tankOption.getChildren().addAll(tankImage, tankName, tankDesc, selectButton);

            // 保存容器引用
            tankOptionContainers.add(tankOption);

            // 添加到选择区域
            tankSelectionArea.getChildren().add(tankOption);
        }

        tankSelectionLayout.setCenter(tankSelectionArea);

        // 底部按钮区域
        HBox bottomButtons = new HBox(20);
        bottomButtons.setAlignment(Pos.CENTER);
        bottomButtons.setPadding(new Insets(30));

        JFXButton backButton = createMenuButton("返回", e -> gameView.showGameOptions(GameView.GAME_MODE_SINGLE));
        JFXButton startButton = createMenuButton("开始游戏", e -> startGame());
        startButton.setStyle("-fx-background-color: " + toHexString(SECONDARY_COLOR) + ";");

        bottomButtons.getChildren().addAll(backButton, startButton);
        tankSelectionLayout.setBottom(bottomButtons);

        // 初始更新选择状态
        updateTankSelection();
    }
    
    /**
     * 更新坦克选择状态
     */
    private void updateTankSelection() {
        for (int i = 0; i < tankOptionContainers.size(); i++) {
            VBox tankOption = tankOptionContainers.get(i);
            JFXButton selectButton = tankSelectButtons.get(i);

            if (i == selectedTankType) {
                // 选中样式
                tankOption.setStyle("-fx-border-color: " + toHexString(SECONDARY_COLOR) + "; " +
                        "-fx-border-width: 3; " +
                        "-fx-border-radius: 5; " +
                        "-fx-padding: 10;");
                selectButton.setStyle("-fx-background-color: " + toHexString(SECONDARY_COLOR) + ";");
                selectButton.setText("已选择");
            } else {
                // 未选中样式
                tankOption.setStyle("-fx-padding: 13;"); // 保持相同的总尺寸
                selectButton.setStyle("-fx-background-color: " + toHexString(PRIMARY_COLOR) + ";");
                selectButton.setText("选择");
            }
        }
    }
    
    /**
     * 开始游戏
     */
    private void startGame() {
        String selectedTankType = TankUtil.TANK_TYPES.get(this.selectedTankType);
        gameView.startGame(selectedTankType);
    }
    
    /**
     * 创建菜单按钮
     * @param text 按钮文本
     * @param action 按钮动作
     * @return 配置好的JFXButton
     */
    private JFXButton createMenuButton(String text, javafx.event.EventHandler<javafx.event.ActionEvent> action) {
        JFXButton button = new JFXButton(text);
        button.setButtonType(JFXButton.ButtonType.RAISED);
        button.setTextFill(TEXT_COLOR);
        button.setStyle("-fx-background-color: " + toHexString(PRIMARY_COLOR) + "; -fx-font-size: 16px;");
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
     * 获取当前选择的坦克类型索引
     * @return 坦克类型索引
     */
    public int getSelectedTankType() {
        return selectedTankType;
    }
    
    /**
     * 设置选择的坦克类型索引
     * @param index 坦克类型索引
     */
    public void setSelectedTankType(int index) {
        if (index >= 0 && index < TankUtil.TANK_TYPES.size()) {
            this.selectedTankType = index;
            if (tankSelectionLayout != null) {
                updateTankSelection();
            }
        }
    }
}