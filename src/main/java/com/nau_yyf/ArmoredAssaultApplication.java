package com.nau_yyf;

import com.nau_yyf.updater.UpdateManager;
import com.nau_yyf.view.GameView;
import javafx.application.Application;
import javafx.application.Platform;
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
        
        // macOS兼容性修复
        System.setProperty("glass.disable.nestedloop", "true");
        System.setProperty("prism.order", "sw");
        
        // 防止渲染线程死锁
        System.setProperty("quantum.multithreaded", "false");
    }

    /**
     * JavaFX应用程序入口方法
     *
     * @param primaryStage 主舞台
     */
    @Override
    public void start(Stage primaryStage) {
        try {
            // 设置stage不可调整大小，减少可能导致崩溃的窗口操作
            primaryStage.setResizable(false);
            
            // 检查更新
            boolean updateInProgress = UpdateManager.checkForUpdates();
            
            // 如果不在更新过程中，则启动游戏
            if (!updateInProgress) {
                Platform.runLater(() -> {
                    try {
                        // 创建游戏视图并显示主菜单
                        GameView gameView = new GameView(primaryStage);
                        gameView.showMainMenu();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
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
