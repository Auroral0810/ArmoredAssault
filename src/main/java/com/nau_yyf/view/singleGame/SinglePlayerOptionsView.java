package com.nau_yyf.view.singleGame;

import com.jfoenix.controls.JFXButton;
import com.nau_yyf.view.BaseMenuView;
import com.nau_yyf.view.GameView;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * 单人游戏选项界面视图组件
 */
public class SinglePlayerOptionsView extends BaseMenuView {

    /**
     * 构造函数
     *
     * @param gameView 游戏主视图的引用
     * @param root     根布局容器
     * @param stage    主舞台
     */
    public SinglePlayerOptionsView(GameView gameView, StackPane root, Stage stage) {
        super(gameView, root, stage);
    }

    /**
     * 显示单人游戏选项界面
     */
    @Override
    public void show() {
        Platform.runLater(() -> {
            // 清除当前内容
            root.getChildren().clear();
            
            // 设置背景
            setGameBackground(root);

            // 创建内容面板
            VBox optionsContainer = createContentPanel(20);
            optionsContainer.setAlignment(Pos.CENTER);
            optionsContainer.setPadding(new Insets(50, 0, 50, 0));
            optionsContainer.setMaxWidth(600);

            // 界面标题
            optionsContainer.getChildren().add(createTitle("单人游戏 —— 闯关模式"));

            // 间隔
            Region spacer1 = new Region();
            VBox.setVgrow(spacer1, Priority.ALWAYS);
            optionsContainer.getChildren().add(spacer1);

            // 创建按钮
            JFXButton newGameButton = createMenuButton("新游戏", e -> {
                // 确保保留当前子模式
                int currentSubMode = gameView.getCurrentGameMode();
                
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
} 