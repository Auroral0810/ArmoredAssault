package com.nau_yyf.util;

import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SVGToPNGConverter {

    public static void main(String[] args) {
        // 设置图片资源根目录
        String resourceDir = "src/main/resources/images/backgrounds";

        try {
            // 递归遍历目录，找到所有SVG文件
            Files.walk(Paths.get(resourceDir))
                    .filter(path -> path.toString().endsWith(".svg"))
                    .forEach(path -> {
                        try {
                            convertSVGToPNG(path);
                        } catch (Exception e) {
                            
                            e.printStackTrace();
                        }
                    });

            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void convertSVGToPNG(Path svgPath) throws Exception {
        // 创建PNG输出路径（与SVG相同目录，但扩展名为.png）
        String pngPath = svgPath.toString().replace(".svg", ".png");
        File pngFile = new File(pngPath);

        

        // 设置转码器
        PNGTranscoder transcoder = new PNGTranscoder();

        // 设置高质量参数（使用正确的常量名）
        // 高像素密度可确保图像清晰
        transcoder.addTranscodingHint(PNGTranscoder.KEY_PIXEL_UNIT_TO_MILLIMETER, 0.084667f / 16.0f); // 相当于约300 DPI

        // 设置输出图像尺寸（如果需要特定尺寸）
        // 对于小图标，可以设置更大的宽高以确保清晰度
        float width = 800; // 足够大以确保清晰
        float height = 800;
        transcoder.addTranscodingHint(PNGTranscoder.KEY_WIDTH, width);
        transcoder.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, height);

        // 确保按比例缩放
        transcoder.addTranscodingHint(PNGTranscoder.KEY_CONSTRAIN_SCRIPT_ORIGIN, Boolean.TRUE);

        // 读取SVG文件
        TranscoderInput input = new TranscoderInput(svgPath.toUri().toString());

        // 设置输出
        FileOutputStream outStream = new FileOutputStream(pngFile);
        TranscoderOutput output = new TranscoderOutput(outStream);

        // 执行转换
        transcoder.transcode(input, output);

        // 关闭流
        outStream.flush();
        outStream.close();
    }
} 