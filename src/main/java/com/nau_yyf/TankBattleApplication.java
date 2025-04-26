package com.nau_yyf;

import com.nau_yyf.view.GameView;
import javafx.application.Application;
import javafx.stage.Stage;
import java.awt.Taskbar;

public class TankBattleApplication extends Application {

    static {
        // macOS应用设置
        System.setProperty("apple.awt.application.name", "Tank2025");
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Tank2025");
        System.setProperty("apple.awt.application.appearance", "system");
        System.setProperty("apple.awt.brushMetalLook", "true");
        System.setProperty("javafx.macosx.embedMenuBar", "false");
        System.setProperty("glass.enableGestureSupport", "false");
        System.setProperty("prism.verbose", "true");

        try {
            if (System.getProperty("os. name").toLowerCase().contains("mac")) {
                final Taskbar taskbar = Taskbar.getTaskbar();
                try {
                    java.awt.Image image = new javax.swing.ImageIcon(
                            TankBattleApplication.class.getResource("/images/logo/tank_logo.png")).getImage();
                    taskbar.setIconImage(image);
                } catch (final UnsupportedOperationException | SecurityException e) {
                    System.err.println("Taskbar操作不支持: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("设置Dock图标失败: " + e.getMessage());
        }
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            GameView gameView = new GameView(primaryStage);
            gameView.showMainMenu();
        } catch (Exception e) {
            System.err.println("启动应用时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    // 提供非JavaFX入口点，可以处理模块路径
    public static class Launcher {
        public static void main(String[] args) {
            TankBattleApplication.main(args);
        }
    }
}
