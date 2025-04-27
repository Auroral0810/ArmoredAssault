package com.nau_yyf.util;

import java.util.Arrays;
import java.util.List;

/**
 * 坦克信息工具类
 * 提供坦克类型、名称和描述等信息
 */
public class TankUtil {
    
    // 坦克类型列表
    public static final List<String> TANK_TYPES = Arrays.asList("light", "standard", "heavy");
    
    /**
     * 获取坦克显示名称
     * @param tankType 坦克类型
     * @return 坦克显示名称
     */
    public static String getTankDisplayName(String tankType) {
        switch (tankType) {
            case "light":
                return "轻型坦克";
            case "standard":
                return "标准坦克";
            case "heavy":
                return "重型坦克";
            default:
                return "未知坦克";
        }
    }
    
    /**
     * 获取坦克描述
     * @param tankType 坦克类型
     * @return 坦克描述
     */
    public static String getTankDescription(String tankType) {
        switch (tankType) {
            case "light":
                return "速度快，机动性强，但装甲薄，攻击力较弱。";
            case "standard":
                return "各项性能均衡，适合大多数战斗场景。";
            case "heavy":
                return "装甲厚，火力强大，但速度较慢，不适合快速战术。";
            default:
                return "";
        }
    }
    
    /**
     * 获取坦克图片路径
     * @param tankType 坦克类型
     * @param isFriendly 是否为友方坦克
     * @return 坦克图片路径
     */
    public static String getTankImagePath(String tankType, boolean isFriendly) {
        String teamFolder = isFriendly ? "friendly" : "enemy";
        return "/images/tanks/" + teamFolder + "/" + tankType + "/1.png";
    }
} 