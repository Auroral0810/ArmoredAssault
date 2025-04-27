package com.nau_yyf;

import com.nau_yyf.view.GameView;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * ArmoredAssault主应用类
 * 负责初始化和启动游戏
 */
public class ArmoredAssaultApplication extends Application {

    static {
        // macOS应用设置
        System.setProperty("apple.awt.application.name", "ArmoredAssault");
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "ArmoredAssault");
    }

    /**
     * JavaFX应用程序入口方法
     *
     * @param primaryStage 主舞台
     */
    @Override
    public void start(Stage primaryStage) {
        try {
            // 创建游戏视图并显示主菜单
            GameView gameView = new GameView(primaryStage);
            gameView.showMainMenu();
        } catch (Exception e) {
            System.err.println("启动应用时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 程序主入口点
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * 提供非JavaFX环境的入口点
     * 用于处理模块路径问题
     */
    public static class Launcher {
        public static void main(String[] args) {
            ArmoredAssaultApplication.main(args);
        }
    }
}
