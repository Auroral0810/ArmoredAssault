package com.nau_yyf.test;

//import com.nau_yyf.controller.MultiGameController;
import com.nau_yyf.controller.MultiGameController;
import com.nau_yyf.service.PlayerService.InputState;
import com.nau_yyf.service.serviceImpl.MultiKeyboardServiceImpl;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 双人键盘输入测试程序
 */
public class MultiKeyboardTest extends Application {

    private MultiKeyboardServiceImpl keyboardService;
    private Canvas gameCanvas;
    private Label player1StatusLabel;
    private Label player2StatusLabel;
    private MultiGameController dummyController;

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();

        // 创建标题
        Text titleText = new Text("双人键盘输入测试");
        titleText.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 24));
        titleText.setFill(Color.WHITE);

        // 控制说明
        VBox controlsBox = new VBox(10);
        controlsBox.setAlignment(Pos.CENTER);
        controlsBox.setPadding(new Insets(10));

        // 玩家1控制说明
        Text p1Title = new Text("玩家1控制");
        p1Title.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 18));
        p1Title.setFill(Color.LIGHTBLUE);

        GridPane p1Controls = new GridPane();
        p1Controls.setHgap(10);
        p1Controls.setVgap(5);
        p1Controls.add(new Text("向上移动: W"), 0, 0);
        p1Controls.add(new Text("向下移动: S"), 0, 1);
        p1Controls.add(new Text("向左移动: A"), 0, 2);
        p1Controls.add(new Text("向右移动: D"), 0, 3);
        p1Controls.add(new Text("射击: 空格键"), 0, 4);
        p1Controls.add(new Text("炸弹: E"), 0, 5);

        // 玩家2控制说明
        Text p2Title = new Text("玩家2控制");
        p2Title.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 18));
        p2Title.setFill(Color.LIGHTCORAL);

        GridPane p2Controls = new GridPane();
        p2Controls.setHgap(10);
        p2Controls.setVgap(5);
        p2Controls.add(new Text("向上移动: ↑"), 0, 0);
        p2Controls.add(new Text("向下移动: ↓"), 0, 1);
        p2Controls.add(new Text("向左移动: ←"), 0, 2);
        p2Controls.add(new Text("向右移动: →"), 0, 3);
        p2Controls.add(new Text("射击: 回车键"), 0, 4);
        p2Controls.add(new Text("炸弹: J"), 0, 5);

        // 所有文本设置为白色
        for (javafx.scene.Node node : p1Controls.getChildren()) {
            if (node instanceof Text) {
                ((Text) node).setFill(Color.WHITE);
            }
        }

        for (javafx.scene.Node node : p2Controls.getChildren()) {
            if (node instanceof Text) {
                ((Text) node).setFill(Color.WHITE);
            }
        }

        // 添加到控制说明容器
        controlsBox.getChildren().addAll(p1Title, p1Controls, p2Title, p2Controls);

        // 状态显示
        player1StatusLabel = new Label("玩家1状态: 未检测到输入");
        player1StatusLabel.setTextFill(Color.LIGHTBLUE);
        player1StatusLabel.setFont(Font.font("Microsoft YaHei", 16));

        player2StatusLabel = new Label("玩家2状态: 未检测到输入");
        player2StatusLabel.setTextFill(Color.LIGHTCORAL);
        player2StatusLabel.setFont(Font.font("Microsoft YaHei", 16));

        VBox statusBox = new VBox(10, player1StatusLabel, player2StatusLabel);
        statusBox.setAlignment(Pos.CENTER);
        statusBox.setPadding(new Insets(20));

        // 提示信息
        Label infoLabel = new Label("请在窗口中按下相应按键，查看输入状态变化。按下ESC键退出测试。");
        infoLabel.setTextFill(Color.YELLOW);
        infoLabel.setFont(Font.font("Microsoft YaHei", 14));
        infoLabel.setPadding(new Insets(10));

        // 游戏画布（用于接收键盘输入）
        gameCanvas = new Canvas(600, 150);
        gameCanvas.setFocusTraversable(true);

        // 创建虚拟的MultiGameController
        dummyController = new MultiGameController();

        // 初始化键盘服务
        keyboardService = new MultiKeyboardServiceImpl();
        keyboardService.setupKeyboardControls(
            dummyController,
            gameCanvas,
            () -> System.out.println("游戏暂停"),
            () -> System.out.println("游戏继续")
        );

        // 添加键盘事件处理
        gameCanvas.setOnKeyPressed(event -> {
            String code = event.getCode().toString();
            if (code.equals("ESCAPE")) {
                System.out.println("退出测试程序");
                Platform.exit();
            } else {
                keyboardService.handleKeyPressed(event, false, false);
            }
        });

        gameCanvas.setOnKeyReleased(event -> {
            keyboardService.handleKeyReleased(event);
        });

        // 布局
        VBox topBox = new VBox(10, titleText);
        topBox.setAlignment(Pos.CENTER);
        topBox.setPadding(new Insets(20));

        VBox centerBox = new VBox(20, controlsBox, statusBox, infoLabel);
        centerBox.setAlignment(Pos.CENTER);

        StackPane canvasPane = new StackPane(gameCanvas);
        canvasPane.setPadding(new Insets(10));

        root.setTop(topBox);
        root.setCenter(centerBox);
        root.setBottom(canvasPane);
        root.setStyle("-fx-background-color: #1E3A5F;");

        // 创建场景
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("双人游戏键盘输入测试");
        primaryStage.setResizable(false);
        primaryStage.show();

        // 设置定时器，定期更新显示
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> updateStatusDisplay());
            }
        }, 0, 50); // 50毫秒更新一次

        // 确保画布获得焦点以接收键盘事件
        gameCanvas.requestFocus();
    }

    /**
     * 更新状态显示
     */
    private void updateStatusDisplay() {
        // 获取当前输入状态
        InputState p1State = keyboardService.getPlayer1InputState();
        InputState p2State = keyboardService.getPlayer2InputState();

        // 更新玩家1状态显示
        String p1Status = "玩家1状态: " + formatInputState(p1State);
        player1StatusLabel.setText(p1Status);

        // 更新玩家2状态显示
        String p2Status = "玩家2状态: " + formatInputState(p2State);
        player2StatusLabel.setText(p2Status);
    }

    /**
     * 格式化输入状态为字符串
     */
    private String formatInputState(InputState state) {
        if (!state.isUp() && !state.isDown() && !state.isLeft() && !state.isRight() && !state.isFire()) {
            return "未检测到输入";
        }

        StringBuilder sb = new StringBuilder();
        if (state.isUp()) sb.append("上 ");
        if (state.isDown()) sb.append("下 ");
        if (state.isLeft()) sb.append("左 ");
        if (state.isRight()) sb.append("右 ");
        if (state.isFire()) sb.append("射击 ");

        return sb.toString().trim();
    }

    /**
     * 主方法 - 启动测试程序
     */
    public static void main(String[] args) {
        launch(args);
    }
}