package com.nau_yyf.view;

import com.jfoenix.controls.JFXButton;
import com.nau_yyf.view.multiGame.MultiPlayerModeSelectionView;
import com.nau_yyf.view.singleGame.SinglePlayerModeSelectionView;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
 * 游戏主菜单视图组件
 */
public class MainMenuView extends BaseMenuView {

    // 从GameView中提取的常量
    private final String GAME_TITLE = "ArmoredAssault";
    private ImageView logoImageView;

    /**
     * 构造函数
     *
     * @param gameView 游戏主视图的引用
     * @param root     根布局容器
     * @param stage    主舞台
     */
    public MainMenuView(GameView gameView, StackPane root, Stage stage) {
        super(gameView, root, stage);
    }

    /**
     * 显示主菜单
     */
    @Override
    public void show() {
        Platform.runLater(() -> {
            // 清除当前内容
            root.getChildren().clear();
            
            // 设置背景图片（使用基类方法）
            setGameBackground(root);

            // 创建菜单容器（使用基类方法）
            VBox menuContainer = createContentPanel(20);
            menuContainer.setAlignment(Pos.CENTER);
            menuContainer.setPadding(new Insets(50, 0, 50, 0));
            menuContainer.setMaxWidth(600);

            // 加载游戏Logo
            try {
                Image logoImage = new Image(getClass().getResourceAsStream("/images/logo/tank_logo.png"));
                logoImageView = new ImageView(logoImage);
                logoImageView.setFitWidth(250);
                logoImageView.setFitHeight(250);
                logoImageView.setPreserveRatio(true);
            } catch (Exception e) {
                
                // 使用文字替代
                logoImageView = null;
            }

            if (logoImageView != null) {
                menuContainer.getChildren().add(logoImageView);
            } else {
                // 游戏标题作为备用
                Text titleText = new Text(GAME_TITLE);
                titleText.setFont(Font.font("Impact", FontWeight.BOLD, 72));
                titleText.setFill(PRIMARY_COLOR);

                // 添加阴影效果
                DropShadow dropShadow = new DropShadow();
                dropShadow.setColor(Color.rgb(0, 0, 0, 0.5));
                dropShadow.setRadius(5);
                dropShadow.setOffsetX(3);
                dropShadow.setOffsetY(3);
                titleText.setEffect(dropShadow);

                menuContainer.getChildren().add(titleText);
            }

            // 版本标签
            Label versionLabel = new Label("Version 1.0");
            versionLabel.setTextFill(TEXT_COLOR);
            versionLabel.setStyle("-fx-font-style: italic;");
            menuContainer.getChildren().add(versionLabel);

            // 间隔
            Region spacer = new Region();
            VBox.setVgrow(spacer, Priority.ALWAYS);
            menuContainer.getChildren().add(spacer);

            // 创建按钮（使用基类方法）
            JFXButton singlePlayerButton = createMenuButton("单人游戏", e -> showSinglePlayerModeSelection());
            JFXButton multiPlayerButton = createMenuButton("双人游戏", e -> showMultiPlayerModeSelection());
            JFXButton onlineButton = createMenuButton("远程联机", e -> showOnlinePlayerModeSelection());
            JFXButton instructionsButton = createMenuButton("游戏说明", e -> gameView.showInstructions());
            JFXButton settingsButton = createMenuButton("设置", e -> gameView.showMessage("设置功能即将推出"));
            JFXButton exitButton = createMenuButton("退出游戏", e -> Platform.exit());

            // 将按钮添加到布局容器
            menuContainer.getChildren().addAll(
                    singlePlayerButton,
                    multiPlayerButton,
                    onlineButton,
                    instructionsButton,
                    settingsButton,
                    exitButton
            );

            // 将菜单容器添加到根布局
            root.getChildren().add(menuContainer);

            // 显示舞台
            stage.show();
        });
    }

    /**
     * 显示单人游戏模式选择界面
     */
    private void showSinglePlayerModeSelection() {
        SinglePlayerModeSelectionView modeSelectionView = new SinglePlayerModeSelectionView(gameView, root, stage);
        modeSelectionView.show();
    }

    /**
     * 显示双人游戏模式选择界面
     */
    private void showMultiPlayerModeSelection() {
        MultiPlayerModeSelectionView modeSelectionView = new MultiPlayerModeSelectionView(gameView, root, stage);
        modeSelectionView.show();
    }

    /**
     * 显示联机游戏模式选择界面
     */
    private void showOnlinePlayerModeSelection() {
        // 如果尚未实现，显示消息
        gameView.showMessage("联机游戏模式选择界面即将推出");
        // 未来实现时使用
        // OnlinePlayerModeSelectionView modeSelectionView = new OnlinePlayerModeSelectionView(gameView, root, stage);
        // modeSelectionView.show();
    }
}