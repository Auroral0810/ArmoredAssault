package com.nau_yyf;

import com.nau_yyf.updater.UpdateManager;
import com.nau_yyf.view.GameView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
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
            // 检查更新
            UpdateManager.checkForUpdates();
            
            // 继续初始化游戏
            StackPane root = new StackPane();
            Scene scene = new Scene(root, 1280, 720);
            primaryStage.setScene(scene);
            primaryStage.setTitle("坦克大作战");
            primaryStage.setResizable(false);
            
            // 显示游戏主界面
            GameView gameView = new GameView(root, primaryStage);
            gameView.showMainMenu();
        } catch (Exception e) {
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
