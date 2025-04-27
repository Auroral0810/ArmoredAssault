package com.nau_yyf.view;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

/**
 * 关卡选择对话框组件
 */
public class LevelSelectionDialog {

    private GameView gameView;
    private StackPane root;
    
    // 从GameView中提取的常量
    private final Color PRIMARY_COLOR = Color.rgb(37, 160, 218);    // 蓝色
    private final Color TEXT_COLOR = Color.WHITE;
    
    /**
     * 构造函数
     * @param gameView 游戏主视图的引用
     * @param root 根布局容器
     */
    public LevelSelectionDialog(GameView gameView, StackPane root) {
        this.gameView = gameView;
        this.root = root;
    }
    
    /**
     * 显示关卡选择对话框
     * @param selectedTankType 选中的坦克类型
     */
    public void show(String selectedTankType) {
        Platform.runLater(() -> {
            JFXDialogLayout content = new JFXDialogLayout();
            content.setHeading(new Text("选择关卡"));

            VBox levelOptions = new VBox(10);
            levelOptions.setAlignment(Pos.CENTER);

            // 先创建对话框
            JFXDialog dialog = new JFXDialog(root, content, JFXDialog.DialogTransition.CENTER);

            // 创建5个关卡选择按钮
            for (int i = 1; i <= 5; i++) {
                final int level = i;
                JFXButton levelButton = new JFXButton("第 " + i + " 关");
                levelButton.setPrefWidth(200);
                levelButton.setButtonType(JFXButton.ButtonType.RAISED);
                levelButton.setStyle("-fx-background-color: " + toHexString(PRIMARY_COLOR) + ";");
                levelButton.setTextFill(TEXT_COLOR);

                // 使用已创建的对话框变量
                levelButton.setOnAction(e -> {
                    dialog.close();
                    gameView.startGameWithLevel(selectedTankType, level);
                });

                levelOptions.getChildren().add(levelButton);
            }

            // 添加返回按钮
            JFXButton backButton = new JFXButton("返回");
            backButton.setPrefWidth(200);
            backButton.setButtonType(JFXButton.ButtonType.RAISED);
            backButton.setStyle("-fx-background-color: #999999;");
            backButton.setTextFill(TEXT_COLOR);

            // 使用已创建的对话框变量
            backButton.setOnAction(e -> dialog.close());
            levelOptions.getChildren().add(backButton);

            content.setBody(levelOptions);
            dialog.show();
        });
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
} 