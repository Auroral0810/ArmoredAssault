package com.nau_yyf.util;

import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

/**
 * 背景管理工具类
 * 负责加载和管理游戏各界面的背景图片
 */
public class BackgroundManager {
    
    private static Image gameBackgroundImage;
    
    /**
     * 获取游戏背景图片
     */
    public static Image getGameBackgroundImage() {
        if (gameBackgroundImage == null) {
            try {
                gameBackgroundImage = new Image(BackgroundManager.class.getResourceAsStream("/images/backgrounds/game_background.png"));
            } catch (Exception e) {
                
                e.printStackTrace();
            }
        }
        return gameBackgroundImage;
    }
    
    /**
     * 为容器设置背景图片
     * 
     * @param region 要设置背景的容器
     * @param imagePath 背景图片路径（相对于资源文件夹）
     * @return 是否设置成功
     */
    public static boolean setBackground(Region region, String imagePath) {
        try {
            Image backgroundImage = new Image(BackgroundManager.class.getResourceAsStream(imagePath));
            
            BackgroundImage background = new BackgroundImage(
                backgroundImage,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(
                    BackgroundSize.AUTO, 
                    BackgroundSize.AUTO, 
                    false, 
                    false, 
                    true, 
                    true  // true表示图片会保持宽高比并充满整个区域
                )
            );
            
            region.setBackground(new Background(background));
            return true;
        } catch (Exception e) {
            
            return false;
        }
    }
    
    /**
     * 为容器设置默认游戏背景
     * 
     * @param region 要设置背景的容器
     * @return 是否设置成功
     */
    public static boolean setGameBackground(Region region) {
        return setBackground(region, "/images/backgrounds/game_background.png");
    }
    
    /**
     * 为容器设置带有半透明覆盖层的背景
     * 
     * @param container 要设置背景的容器
     * @param color 覆盖层颜色
     * @param opacity 覆盖层不透明度（0.0-1.0）
     */
    public static void addOverlay(Pane container, Color color, double opacity) {
        Region overlay = new Region();
        overlay.setPrefSize(container.getWidth(), container.getHeight());
        overlay.setBackground(new Background(new BackgroundFill(
            color.deriveColor(0, 1, 1, opacity),
            CornerRadii.EMPTY,
            javafx.geometry.Insets.EMPTY
        )));
        
        // 确保覆盖层是第一个子元素，这样它会显示在最下面
        if (!container.getChildren().isEmpty()) {
            container.getChildren().add(0, overlay);
            
            // 绑定覆盖层大小到容器
            overlay.prefWidthProperty().bind(container.widthProperty());
            overlay.prefHeightProperty().bind(container.heightProperty());
        }
    }
} 