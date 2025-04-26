package com.nau_yyf.util;

import javafx.scene.image.Image;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;

public class SVGLoader {
    
    /**
     * 加载SVG并转换为JavaFX Image
     */
    public static Image loadSVG(String resourcePath, double width, double height) throws Exception {
        // 从资源目录获取SVG文件
        InputStream svgStream = SVGLoader.class.getResourceAsStream(resourcePath);
        if (svgStream == null) {
            throw new IllegalArgumentException("未找到资源: " + resourcePath);
        }
        
        // 创建临时文件 
        File tempSvgFile = File.createTempFile("tank_", ".svg");
        Files.copy(svgStream, tempSvgFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        svgStream.close();
        
        // 创建临时PNG文件
        File tempPngFile = File.createTempFile("tank_", ".png");
        
        try {
            // 设置转换器
            TranscoderInput input = new TranscoderInput(tempSvgFile.toURI().toString());
            TranscoderOutput output = new TranscoderOutput(new FileOutputStream(tempPngFile));
            
            PNGTranscoder transcoder = new PNGTranscoder();
            transcoder.addTranscodingHint(PNGTranscoder.KEY_WIDTH, (float) width);
            transcoder.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, (float) height);
            
            // 执行转换
            transcoder.transcode(input, output);
            
            // 加载图像
            return new Image(tempPngFile.toURI().toString());
        } finally {
            // 清理临时文件
            tempSvgFile.deleteOnExit();
            tempPngFile.deleteOnExit();
        }
    }
    
    /**
     * 备用方法：直接从资源文件加载图像
     */
    public static Image loadImageDirectly(String resourcePath) {
        InputStream stream = SVGLoader.class.getResourceAsStream(resourcePath);
        if (stream == null) {
            throw new IllegalArgumentException("未找到资源: " + resourcePath);
        }
        return new Image(stream);
    }
}
