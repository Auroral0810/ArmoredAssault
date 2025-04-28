package com.nau_yyf.view.singleGame;

import com.jfoenix.controls.JFXButton;
import com.nau_yyf.view.GameView;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * 单人游戏选项界面视图组件
 */
public class SinglePlayerOptionsView {

    private GameView gameView;
    private StackPane root;
    private Stage stage;

    // 从GameView中提取的常量
    private final Color PRIMARY_COLOR = Color.rgb(37, 160, 218);    // 蓝色
    private final Color TEXT_COLOR = Color.WHITE;

    /**
     * 构造函数
     *
     * @param gameView 游戏主视图的引用
     * @param root     根布局容器
     * @param stage    主舞台
     */
    public SinglePlayerOptionsView(GameView gameView, StackPane root, Stage stage) {
        this.gameView = gameView;
        this.root = root;
        this.stage = stage;
    }

    /**
     * 显示单人游戏选项界面
     */
    public void show() {
        Platform.runLater(() -> {
            // 清除当前内容
            root.getChildren().clear();

            // 创建垂直布局容器
            VBox optionsContainer = new VBox(20);
            optionsContainer.setAlignment(Pos.CENTER);
            optionsContainer.setPadding(new Insets(50, 0, 50, 0));
            optionsContainer.setMaxWidth(600);

            // 界面标题
            Text titleText = new Text("单人游戏 —— 闯关模式");
            titleText.setFont(Font.font("Arial", FontWeight.BOLD, 36));
            titleText.setFill(PRIMARY_COLOR);
            optionsContainer.getChildren().add(titleText);

            // 间隔
            Region spacer1 = new Region();
            VBox.setVgrow(spacer1, Priority.ALWAYS);
            optionsContainer.getChildren().add(spacer1);

            // 创建按钮
            JFXButton newGameButton = createMenuButton("新游戏", e -> {
                // 确保保留当前子模式
                int currentSubMode = gameView.getCurrentGameMode();
                System.out.println("新游戏按钮点击，当前模式: " + gameView.getGameModeName(currentSubMode));

                // 子模式处理
                if (currentSubMode == GameView.GAME_MODE_SINGLE) {
                    // 如果是主模式，改为对应的子模式
                    gameView.setGameMode(GameView.GAME_MODE_SINGLE_CAMPAIGN);
                }

                // 显示坦克选择界面
                gameView.showTankSelection();
            });
            JFXButton loadGameButton = createMenuButton("加载游戏", e -> {
                // 确保在加载前设置正确的子模式
                if (gameView.getCurrentGameMode() == GameView.GAME_MODE_SINGLE) {
                    // 如果是主模式，改为对应的子模式
                    gameView.setGameMode(GameView.GAME_MODE_SINGLE_CAMPAIGN);
                }
                // 调用加载游戏功能
                gameView.loadGame();
            });
            JFXButton tutorialButton = createMenuButton("新手教程", e -> gameView.showMessage("新手教程功能即将推出"));
            JFXButton backButton = createMenuButton("返回主菜单", e -> gameView.showMainMenu());

            // 将按钮添加到布局容器
            optionsContainer.getChildren().addAll(
                    newGameButton,
                    loadGameButton,
                    tutorialButton,
                    backButton
            );

            // 额外的间隔
            Region spacer2 = new Region();
            VBox.setVgrow(spacer2, Priority.ALWAYS);
            optionsContainer.getChildren().add(spacer2);

            // 提示标签
            Label tipLabel = new Label("提示: 新游戏将让你选择坦克类型和关卡");
            tipLabel.setTextFill(TEXT_COLOR);
            tipLabel.setStyle("-fx-font-style: italic;");
            optionsContainer.getChildren().add(tipLabel);

            // 将选项容器添加到根布局
            root.getChildren().add(optionsContainer);

            // 显示舞台
            stage.show();
        });
    }

    /**
     * 创建菜单按钮
     *
     * @param text   按钮文本
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
     *
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