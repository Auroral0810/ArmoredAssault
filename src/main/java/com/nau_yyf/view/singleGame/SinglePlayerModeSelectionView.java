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
 * 单人游戏模式选择界面
 */
public class SinglePlayerModeSelectionView extends BaseMenuView {
    
    /**
     * 构造函数
     * @param gameView 游戏主视图的引用
     * @param root 根布局容器
     * @param stage 主舞台
     */
    public SinglePlayerModeSelectionView(GameView gameView, StackPane root, Stage stage) {
        super(gameView, root, stage);
    }
    
    /**
     * 显示单人游戏模式选择界面
     */
    @Override
    public void show() {
        Platform.runLater(() -> {
            // 清除当前内容
            root.getChildren().clear();
            
            // 设置游戏背景（使用基类方法）
            setGameBackground(root);
            
            // 创建半透明内容面板（使用基类方法）
            VBox optionsContainer = createContentPanel(20);
            optionsContainer.setAlignment(Pos.CENTER);
            optionsContainer.setPadding(new Insets(50, 0, 50, 0));
            optionsContainer.setMaxWidth(600);
            
            // 界面标题（使用基类方法）
            optionsContainer.getChildren().add(createTitle("单人游戏模式"));
            
            // 间隔
            Region spacer1 = new Region();
            VBox.setVgrow(spacer1, Priority.ALWAYS);
            optionsContainer.getChildren().add(spacer1);
            
            // 创建不同模式的按钮（使用基类方法）
            JFXButton campaignButton = createMenuButton("闯关模式", e -> 
                gameView.showGameOptions(GameView.GAME_MODE_SINGLE_CAMPAIGN));
            
            JFXButton vsAIButton = createMenuButton("对战电脑", e -> 
                gameView.showGameOptions(GameView.GAME_MODE_SINGLE_VS_AI));
            
            JFXButton endlessButton = createMenuButton("无尽模式", e -> 
                gameView.showGameOptions(GameView.GAME_MODE_SINGLE_ENDLESS));
            
            JFXButton backButton = createMenuButton("返回主菜单", e -> 
                gameView.showMainMenu());
            
            // 将按钮添加到布局容器
            optionsContainer.getChildren().addAll(
                    campaignButton,
                    vsAIButton,
                    endlessButton,
                    backButton
            );
            
            // 额外的间隔
            Region spacer2 = new Region();
            VBox.setVgrow(spacer2, Priority.ALWAYS);
            optionsContainer.getChildren().add(spacer2);
            
            // 提示标签
            Label tipLabel = new Label("提示: 选择一种游戏模式开始游戏");
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