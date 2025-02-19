/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.core.utils;

import org.aoju.bus.core.codec.Base64;
import org.aoju.bus.core.consts.FileType;
import org.aoju.bus.core.convert.Convert;
import org.aoju.bus.core.image.Image;
import org.aoju.bus.core.io.resource.Resource;
import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.lang.exception.InstrumentException;

import javax.imageio.*;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.RenderedImage;
import java.io.*;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * 图片处理工具类：
 * 功能：缩放图像、切割图像、旋转、图像类型转换、彩色转黑白、文字水印、图片水印等
 * 参考：http://blog.csdn.net/zhangzhikaixinya/article/details/8459400
 *
 * @author Kimi Liu
 * @version 5.2.5
 * @since JDK 1.8+
 */
public class ImageUtils {

    /**
     * 缩放图像（按比例缩放）
     * 缩放后默认为jpeg格式
     *
     * @param srcImageFile  源图像文件
     * @param destImageFile 缩放后的图像文件
     * @param scale         缩放比例 比例大于1时为放大,小于1大于0为缩小
     */
    public static void scale(File srcImageFile, File destImageFile, float scale) {
        scale(read(srcImageFile), destImageFile, scale);
    }

    /**
     * 缩放图像（按比例缩放）
     * 缩放后默认为jpeg格式,此方法并不关闭流
     *
     * @param srcStream  源图像来源流
     * @param destStream 缩放后的图像写出到的流
     * @param scale      缩放比例 比例大于1时为放大,小于1大于0为缩小
     * @since 3.1.9
     */
    public static void scale(InputStream srcStream, OutputStream destStream, float scale) {
        scale(read(srcStream), destStream, scale);
    }

    /**
     * 缩放图像（按比例缩放）
     * 缩放后默认为jpeg格式,此方法并不关闭流
     *
     * @param srcStream  源图像来源流
     * @param destStream 缩放后的图像写出到的流
     * @param scale      缩放比例 比例大于1时为放大,小于1大于0为缩小
     * @since 3.1.9
     */
    public static void scale(ImageInputStream srcStream, ImageOutputStream destStream, float scale) {
        scale(read(srcStream), destStream, scale);
    }

    /**
     * 缩放图像（按比例缩放）
     * 缩放后默认为jpeg格式,此方法并不关闭流
     *
     * @param srcImage 源图像来源流
     * @param destFile 缩放后的图像写出到的流
     * @param scale    缩放比例 比例大于1时为放大,小于1大于0为缩小
     * @throws InstrumentException IO异常
     * @since 5.2.5
     */
    public static void scale(java.awt.Image srcImage, File destFile, float scale) throws InstrumentException {
        Image.from(srcImage).setTargetImageType(FileUtils.extName(destFile)).scale(scale).write(destFile);
    }

    /**
     * 缩放图像（按比例缩放）
     * 缩放后默认为jpeg格式,此方法并不关闭流
     *
     * @param srcImage 源图像来源流
     * @param out      缩放后的图像写出到的流
     * @param scale    缩放比例 比例大于1时为放大,小于1大于0为缩小
     * @throws InstrumentException IO异常
     * @since 5.2.5
     */
    public static void scale(java.awt.Image srcImage, OutputStream out, float scale) throws InstrumentException {
        scale(srcImage, getImageOutputStream(out), scale);
    }

    /**
     * 缩放图像（按比例缩放）
     * 缩放后默认为jpeg格式,此方法并不关闭流
     *
     * @param srcImage        源图像来源流
     * @param destImageStream 缩放后的图像写出到的流
     * @param scale           缩放比例 比例大于1时为放大,小于1大于0为缩小
     * @throws InstrumentException IO异常
     * @since 3.1.9
     */
    public static void scale(java.awt.Image srcImage, ImageOutputStream destImageStream, float scale) throws InstrumentException {
        writeJpg(scale(srcImage, scale), destImageStream);
    }

    /**
     * 缩放图像（按比例缩放）
     *
     * @param srcImage 源图像来源流
     * @param scale    缩放比例 比例大于1时为放大,小于1大于0为缩小
     * @return {@link java.awt.Image}
     * @since 3.1.9
     */
    public static java.awt.Image scale(java.awt.Image srcImage, float scale) {
        return Image.from(srcImage).scale(scale).getImg();
    }

    /**
     * 缩放图像（按长宽缩放）
     * 注意：目标长宽与原图不成比例会变形
     *
     * @param srcImage 源图像来源流
     * @param width    目标宽度
     * @param height   目标高度
     * @return {@link java.awt.Image}
     * @since 3.1.9
     */
    public static java.awt.Image scale(java.awt.Image srcImage, int width, int height) {
        return Image.from(srcImage).scale(width, height).getImg();
    }

    /**
     * 缩放图像（按高度和宽度缩放）
     * 缩放后默认为jpeg格式
     *
     * @param srcImageFile  源图像文件地址
     * @param destImageFile 缩放后的图像地址
     * @param width         缩放后的宽度
     * @param height        缩放后的高度
     * @param fixedColor    比例不对时补充的颜色,不补充为<code>null</code>
     * @throws InstrumentException IO异常
     */
    public static void scale(File srcImageFile, File destImageFile, int width, int height, Color fixedColor) throws InstrumentException {
        write(scale(read(srcImageFile), width, height, fixedColor), destImageFile);
    }

    /**
     * 缩放图像（按高度和宽度缩放）
     * 缩放后默认为jpeg格式,此方法并不关闭流
     *
     * @param srcStream  源图像流
     * @param destStream 缩放后的图像目标流
     * @param width      缩放后的宽度
     * @param height     缩放后的高度
     * @param fixedColor 比例不对时补充的颜色,不补充为<code>null</code>
     * @throws InstrumentException IO异常
     */
    public static void scale(InputStream srcStream, OutputStream destStream, int width, int height, Color fixedColor) throws InstrumentException {
        scale(read(srcStream), getImageOutputStream(destStream), width, height, fixedColor);
    }

    /**
     * 缩放图像（按高度和宽度缩放）
     * 缩放后默认为jpeg格式,此方法并不关闭流
     *
     * @param srcStream  源图像流
     * @param destStream 缩放后的图像目标流
     * @param width      缩放后的宽度
     * @param height     缩放后的高度
     * @param fixedColor 比例不对时补充的颜色,不补充为<code>null</code>
     * @throws InstrumentException IO异常
     */
    public static void scale(ImageInputStream srcStream, ImageOutputStream destStream, int width, int height, Color fixedColor) throws InstrumentException {
        scale(read(srcStream), destStream, width, height, fixedColor);
    }

    /**
     * 缩放图像（按高度和宽度缩放）
     * 缩放后默认为jpeg格式,此方法并不关闭流
     *
     * @param srcImage        源图像
     * @param destImageStream 缩放后的图像目标流
     * @param width           缩放后的宽度
     * @param height          缩放后的高度
     * @param fixedColor      比例不对时补充的颜色,不补充为<code>null</code>
     * @throws InstrumentException IO异常
     */
    public static void scale(java.awt.Image srcImage, ImageOutputStream destImageStream, int width, int height, Color fixedColor) throws InstrumentException {
        writeJpg(scale(srcImage, width, height, fixedColor), destImageStream);
    }

    /**
     * 缩放图像（按高度和宽度缩放）
     * 缩放后默认为jpeg格式
     *
     * @param srcImage   源图像
     * @param width      缩放后的宽度
     * @param height     缩放后的高度
     * @param fixedColor 比例不对时补充的颜色,不补充为<code>null</code>
     * @return {@link java.awt.Image}
     */
    public static java.awt.Image scale(java.awt.Image srcImage, int width, int height, Color fixedColor) {
        return org.aoju.bus.core.image.Image.from(srcImage).scale(width, height, fixedColor).getImg();
    }

    /**
     * 图像切割(按指定起点坐标和宽高切割)
     *
     * @param srcImgFile  源图像文件
     * @param destImgFile 切片后的图像文件
     * @param rectangle   矩形对象,表示矩形区域的x,y,width,height
     * @since 3.1.9
     */
    public static void cut(File srcImgFile, File destImgFile, Rectangle rectangle) {
        cut(read(srcImgFile), destImgFile, rectangle);
    }

    /**
     * 图像切割(按指定起点坐标和宽高切割),此方法并不关闭流
     *
     * @param srcStream  源图像流
     * @param destStream 切片后的图像输出流
     * @param rectangle  矩形对象,表示矩形区域的x,y,width,height
     * @since 3.1.9
     */
    public static void cut(InputStream srcStream, OutputStream destStream, Rectangle rectangle) {
        cut(read(srcStream), destStream, rectangle);
    }

    /**
     * 图像切割(按指定起点坐标和宽高切割),此方法并不关闭流
     *
     * @param srcStream  源图像流
     * @param destStream 切片后的图像输出流
     * @param rectangle  矩形对象,表示矩形区域的x,y,width,height
     * @since 3.1.9
     */
    public static void cut(ImageInputStream srcStream, ImageOutputStream destStream, Rectangle rectangle) {
        cut(read(srcStream), destStream, rectangle);
    }

    /**
     * 图像切割(按指定起点坐标和宽高切割),此方法并不关闭流
     *
     * @param srcImage  源图像
     * @param destFile  输出的文件
     * @param rectangle 矩形对象,表示矩形区域的x,y,width,height
     * @throws InstrumentException IO异常
     * @since 5.2.5
     */
    public static void cut(java.awt.Image srcImage, File destFile, Rectangle rectangle) throws InstrumentException {
        write(cut(srcImage, rectangle), destFile);
    }

    /**
     * 图像切割(按指定起点坐标和宽高切割),此方法并不关闭流
     *
     * @param srcImage  源图像
     * @param out       切片后的图像输出流
     * @param rectangle 矩形对象,表示矩形区域的x,y,width,height
     * @throws InstrumentException IO异常
     * @since 3.1.9
     */
    public static void cut(java.awt.Image srcImage, OutputStream out, Rectangle rectangle) throws InstrumentException {
        cut(srcImage, getImageOutputStream(out), rectangle);
    }

    /**
     * 图像切割(按指定起点坐标和宽高切割),此方法并不关闭流
     *
     * @param srcImage        源图像
     * @param destImageStream 切片后的图像输出流
     * @param rectangle       矩形对象,表示矩形区域的x,y,width,height
     * @throws InstrumentException IO异常
     * @since 3.1.9
     */
    public static void cut(java.awt.Image srcImage, ImageOutputStream destImageStream, Rectangle rectangle) throws InstrumentException {
        writeJpg(cut(srcImage, rectangle), destImageStream);
    }

    /**
     * 图像切割(按指定起点坐标和宽高切割)
     *
     * @param srcImage  源图像
     * @param rectangle 矩形对象,表示矩形区域的x,y,width,height
     * @return {@link BufferedImage}
     * @since 3.1.9
     */
    public static java.awt.Image cut(java.awt.Image srcImage, Rectangle rectangle) {
        return Image.from(srcImage).setPositionBaseCentre(false).cut(rectangle).getImg();
    }

    /**
     * 图像切割(按指定起点坐标和宽高切割),填充满整个图片（直径取长宽最小值）
     *
     * @param srcImage 源图像
     * @param x        原图的x坐标起始位置
     * @param y        原图的y坐标起始位置
     * @return {@link java.awt.Image}
     */
    public static java.awt.Image cut(java.awt.Image srcImage, int x, int y) {
        return cut(srcImage, x, y, -1);
    }

    /**
     * 图像切割(按指定起点坐标和宽高切割)
     *
     * @param srcImage 源图像
     * @param x        原图的x坐标起始位置
     * @param y        原图的y坐标起始位置
     * @param radius   半径,小于0表示填充满整个图片（直径取长宽最小值）
     * @return {@link java.awt.Image}
     */
    public static java.awt.Image cut(java.awt.Image srcImage, int x, int y, int radius) {
        return Image.from(srcImage).cut(x, y, radius).getImg();
    }

    /**
     * 图像切片（指定切片的宽度和高度）
     *
     * @param srcImageFile 源图像
     * @param descDir      切片目标文件夹
     * @param destWidth    目标切片宽度 默认200
     * @param destHeight   目标切片高度 默认150
     */
    public static void slice(File srcImageFile, File descDir, int destWidth, int destHeight) {
        slice(read(srcImageFile), descDir, destWidth, destHeight);
    }

    /**
     * 图像切片（指定切片的宽度和高度）
     *
     * @param srcImage   源图像
     * @param descDir    切片目标文件夹
     * @param destWidth  目标切片宽度 默认200
     * @param destHeight 目标切片高度 默认150
     */
    public static void slice(java.awt.Image srcImage, File descDir, int destWidth, int destHeight) {
        if (destWidth <= 0) {
            destWidth = 200; // 切片宽度
        }
        if (destHeight <= 0) {
            destHeight = 150; // 切片高度
        }
        int srcWidth = srcImage.getHeight(null); // 源图宽度
        int srcHeight = srcImage.getWidth(null); // 源图高度

        try {
            if (srcWidth > destWidth && srcHeight > destHeight) {
                int cols = 0; // 切片横向数量
                int rows = 0; // 切片纵向数量
                // 计算切片的横向和纵向数量
                if (srcWidth % destWidth == 0) {
                    cols = srcWidth / destWidth;
                } else {
                    cols = (int) Math.floor(srcWidth / destWidth) + 1;
                }
                if (srcHeight % destHeight == 0) {
                    rows = srcHeight / destHeight;
                } else {
                    rows = (int) Math.floor(srcHeight / destHeight) + 1;
                }
                // 循环建立切片
                java.awt.Image tag;
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        // 四个参数分别为图像起点坐标和宽高
                        // 即: CropImageFilter(int x,int y,int width,int height)
                        tag = cut(srcImage, new Rectangle(j * destWidth, i * destHeight, destWidth, destHeight));
                        // 输出为文件
                        ImageIO.write(toRenderedImage(tag), FileType.IMAGE_TYPE_JPEG, new File(descDir, "_r" + i + "_c" + j + ".jpg"));
                    }
                }
            }
        } catch (IOException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 图像切割（指定切片的行数和列数）
     *
     * @param srcImageFile 源图像文件
     * @param destDir      切片目标文件夹
     * @param rows         目标切片行数 默认2,必须是范围 [1, 20] 之内
     * @param cols         目标切片列数 默认2,必须是范围 [1, 20] 之内
     */
    public static void sliceByRowsAndCols(File srcImageFile, File destDir, int rows, int cols) {
        try {
            sliceByRowsAndCols(ImageIO.read(srcImageFile), destDir, rows, cols);
        } catch (IOException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 图像切割（指定切片的行数和列数）
     *
     * @param srcImage 源图像
     * @param destDir  切片目标文件夹
     * @param rows     目标切片行数 默认2,必须是范围 [1, 20] 之内
     * @param cols     目标切片列数 默认2,必须是范围 [1, 20] 之内
     */
    public static void sliceByRowsAndCols(java.awt.Image srcImage, File destDir, int rows, int cols) {
        if (false == destDir.exists()) {
            FileUtils.mkdir(destDir);
        } else if (false == destDir.isDirectory()) {
            throw new IllegalArgumentException("Destination Dir must be a Directory !");
        }

        try {
            if (rows <= 0 || rows > 20) {
                rows = 2; // 切片行数
            }
            if (cols <= 0 || cols > 20) {
                cols = 2; // 切片列数
            }
            // 读取源图像
            final java.awt.Image bi = toBufferedImage(srcImage);
            int srcWidth = bi.getWidth(null); // 源图宽度
            int srcHeight = bi.getHeight(null); // 源图高度

            int destWidth = NumberUtils.partValue(srcWidth, cols); // 每张切片的宽度
            int destHeight = NumberUtils.partValue(srcHeight, rows); // 每张切片的高度

            // 循环建立切片
            java.awt.Image tag;
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    tag = cut(bi, new Rectangle(j * destWidth, i * destHeight, destWidth, destHeight));
                    // 输出为文件
                    ImageIO.write(toRenderedImage(tag), FileType.IMAGE_TYPE_JPEG, new File(destDir, "_r" + i + "_c" + j + ".jpg"));
                }
            }
        } catch (IOException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 图像类型转换：GIF=》JPG、GIF=》PNG、PNG=》JPG、PNG=》GIF(X)、BMP=》PNG
     *
     * @param srcImageFile  源图像文件
     * @param destImageFile 目标图像文件
     */
    public static void convert(File srcImageFile, File destImageFile) {
        Assert.notNull(srcImageFile);
        Assert.notNull(destImageFile);
        Assert.isFalse(srcImageFile.equals(destImageFile), "Src file is equals to dest file!");

        final String srcExtName = FileUtils.extName(srcImageFile);
        final String destExtName = FileUtils.extName(destImageFile);
        if (StringUtils.equalsIgnoreCase(srcExtName, destExtName)) {
            // 扩展名相同直接复制文件
            FileUtils.copy(srcImageFile, destImageFile, true);
        }

        ImageOutputStream imageOutputStream = null;
        try {
            imageOutputStream = getImageOutputStream(destImageFile);
            convert(read(srcImageFile), destExtName, imageOutputStream, StringUtils.equalsIgnoreCase(FileType.IMAGE_TYPE_PNG, srcExtName));
        } finally {
            IoUtils.close(imageOutputStream);
        }
    }

    /**
     * 图像类型转换：GIF=》JPG、GIF=》PNG、PNG=》JPG、PNG=》GIF(X)、BMP=》PNG
     * 此方法并不关闭流
     *
     * @param srcStream  源图像流
     * @param formatName 包含格式非正式名称的 String：如JPG、JPEG、GIF等
     * @param destStream 目标图像输出流
     * @since 3.1.9
     */
    public static void convert(InputStream srcStream, String formatName, OutputStream destStream) {
        write(read(srcStream), formatName, getImageOutputStream(destStream));
    }


    /**
     * 图像类型转换：GIF=》JPG、GIF=》PNG、PNG=》JPG、PNG=》GIF(X)、BMP=》PNG
     * 此方法并不关闭流
     *
     * @param srcImage        源图像流
     * @param formatName      包含格式非正式名称的 String：如JPG、JPEG、GIF等
     * @param destImageStream 目标图像输出流
     * @param isSrcPng        源图片是否为PNG格式
     */
    public static void convert(java.awt.Image srcImage, String formatName, ImageOutputStream destImageStream, boolean isSrcPng) {
        try {
            ImageIO.write(isSrcPng ? copyImage(srcImage, BufferedImage.TYPE_INT_RGB) : toBufferedImage(srcImage), formatName, destImageStream);
        } catch (IOException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 彩色转为黑白
     *
     * @param srcImageFile  源图像地址
     * @param destImageFile 目标图像地址
     */
    public static void gray(File srcImageFile, File destImageFile) {
        gray(read(srcImageFile), destImageFile);
    }

    /**
     * 彩色转为黑白
     * 此方法并不关闭流
     *
     * @param srcStream  源图像流
     * @param destStream 目标图像流
     * @since 3.1.9
     */
    public static void gray(InputStream srcStream, OutputStream destStream) {
        gray(read(srcStream), getImageOutputStream(destStream));
    }

    /**
     * 彩色转为黑白
     * 此方法并不关闭流
     *
     * @param srcStream  源图像流
     * @param destStream 目标图像流
     * @since 3.1.9
     */
    public static void gray(ImageInputStream srcStream, ImageOutputStream destStream) {
        gray(read(srcStream), destStream);
    }

    /**
     * 彩色转为黑白
     *
     * @param srcImage 源图像流
     * @param outFile  目标文件
     * @since 5.2.5
     */
    public static void gray(java.awt.Image srcImage, File outFile) {
        write(gray(srcImage), outFile);
    }

    /**
     * 彩色转为黑白
     * 此方法并不关闭流
     *
     * @param srcImage 源图像流
     * @param out      目标图像流
     * @since 5.2.5
     */
    public static void gray(java.awt.Image srcImage, OutputStream out) {
        gray(srcImage, getImageOutputStream(out));
    }

    /**
     * 彩色转为黑白
     * 此方法并不关闭流
     *
     * @param srcImage        源图像流
     * @param destImageStream 目标图像流
     * @throws InstrumentException IO异常
     * @since 3.1.9
     */
    public static void gray(java.awt.Image srcImage, ImageOutputStream destImageStream) throws InstrumentException {
        writeJpg(gray(srcImage), destImageStream);
    }

    /**
     * 彩色转为黑白
     *
     * @param srcImage 源图像流
     * @return {@link java.awt.Image}灰度后的图片
     * @since 3.1.9
     */
    public static java.awt.Image gray(java.awt.Image srcImage) {
        return Image.from(srcImage).gray().getImg();
    }

    /**
     * 彩色转为黑白二值化图片,根据目标文件扩展名确定转换后的格式
     *
     * @param srcImageFile  源图像地址
     * @param destImageFile 目标图像地址
     */
    public static void binary(File srcImageFile, File destImageFile) {
        binary(read(srcImageFile), destImageFile);
    }

    /**
     * 彩色转为黑白二值化图片
     * 此方法并不关闭流
     *
     * @param srcStream  源图像流
     * @param destStream 目标图像流
     * @param imageType  图片格式(扩展名)
     */
    public static void binary(InputStream srcStream, OutputStream destStream, String imageType) {
        binary(read(srcStream), getImageOutputStream(destStream), imageType);
    }

    /**
     * 彩色转为黑白黑白二值化图片
     * 此方法并不关闭流
     *
     * @param srcStream  源图像流
     * @param destStream 目标图像流
     * @param imageType  图片格式(扩展名)
     */
    public static void binary(ImageInputStream srcStream, ImageOutputStream destStream, String imageType) {
        binary(read(srcStream), destStream, imageType);
    }

    /**
     * 彩色转为黑白二值化图片,根据目标文件扩展名确定转换后的格式
     *
     * @param srcImage 源图像流
     * @param outFile  目标文件
     */
    public static void binary(java.awt.Image srcImage, File outFile) {
        write(binary(srcImage), outFile);
    }

    /**
     * 彩色转为黑白二值化图片
     * 此方法并不关闭流,输出JPG格式
     *
     * @param srcImage  源图像流
     * @param out       目标图像流
     * @param imageType 图片格式(扩展名)
     */
    public static void binary(java.awt.Image srcImage, OutputStream out, String imageType) {
        binary(srcImage, getImageOutputStream(out), imageType);
    }

    /**
     * 彩色转为黑白二值化图片
     * 此方法并不关闭流,输出JPG格式
     *
     * @param srcImage        源图像流
     * @param destImageStream 目标图像流
     * @param imageType       图片格式(扩展名)
     * @throws InstrumentException IO异常
     */
    public static void binary(java.awt.Image srcImage, ImageOutputStream destImageStream, String imageType) throws InstrumentException {
        write(binary(srcImage), imageType, destImageStream);
    }

    /**
     * 彩色转为黑白二值化图片
     *
     * @param srcImage 源图像流
     * @return {@link java.awt.Image}二值化后的图片
     */
    public static java.awt.Image binary(java.awt.Image srcImage) {
        return Image.from(srcImage).binary().getImg();
    }

    /**
     * 给图片添加文字水印
     *
     * @param imageFile 源图像文件
     * @param destFile  目标图像文件
     * @param pressText 水印文字
     * @param color     水印的字体颜色
     * @param font      {@link Font} 字体相关信息,如果默认则为{@code null}
     * @param x         修正值  默认在中间,偏移量相对于中间偏移
     * @param y         修正值  默认在中间,偏移量相对于中间偏移
     * @param alpha     透明度：alpha 必须是范围 [0.0, 1.0] 之内（包含边界值）的一个浮点数字
     */
    public static void pressText(File imageFile, File destFile, String pressText, Color color, Font font, int x, int y, float alpha) {
        pressText(read(imageFile), destFile, pressText, color, font, x, y, alpha);
    }

    /**
     * 给图片添加文字水印
     * 此方法并不关闭流
     *
     * @param srcStream  源图像流
     * @param destStream 目标图像流
     * @param pressText  水印文字
     * @param color      水印的字体颜色
     * @param font       {@link Font} 字体相关信息,如果默认则为{@code null}
     * @param x          修正值  默认在中间,偏移量相对于中间偏移
     * @param y          修正值  默认在中间,偏移量相对于中间偏移
     * @param alpha      透明度：alpha 必须是范围 [0.0, 1.0] 之内（包含边界值）的一个浮点数字
     */
    public static void pressText(InputStream srcStream, OutputStream destStream, String pressText, Color color, Font font, int x, int y, float alpha) {
        pressText(read(srcStream), getImageOutputStream(destStream), pressText, color, font, x, y, alpha);
    }

    /**
     * 给图片添加文字水印
     * 此方法并不关闭流
     *
     * @param srcStream  源图像流
     * @param destStream 目标图像流
     * @param pressText  水印文字
     * @param color      水印的字体颜色
     * @param font       {@link Font} 字体相关信息,如果默认则为{@code null}
     * @param x          修正值  默认在中间,偏移量相对于中间偏移
     * @param y          修正值  默认在中间,偏移量相对于中间偏移
     * @param alpha      透明度：alpha 必须是范围 [0.0, 1.0] 之内（包含边界值）的一个浮点数字
     */
    public static void pressText(ImageInputStream srcStream, ImageOutputStream destStream, String pressText, Color color, Font font, int x, int y, float alpha) {
        pressText(read(srcStream), destStream, pressText, color, font, x, y, alpha);
    }

    /**
     * 给图片添加文字水印
     * 此方法并不关闭流
     *
     * @param srcImage  源图像
     * @param destFile  目标流
     * @param pressText 水印文字
     * @param color     水印的字体颜色
     * @param font      {@link Font} 字体相关信息,如果默认则为{@code null}
     * @param x         修正值  默认在中间,偏移量相对于中间偏移
     * @param y         修正值  默认在中间,偏移量相对于中间偏移
     * @param alpha     透明度：alpha 必须是范围 [0.0, 1.0] 之内（包含边界值）的一个浮点数字
     * @throws InstrumentException IO异常
     * @since 5.2.5
     */
    public static void pressText(java.awt.Image srcImage, File destFile, String pressText, Color color, Font font, int x, int y, float alpha) throws InstrumentException {
        write(pressText(srcImage, pressText, color, font, x, y, alpha), destFile);
    }

    /**
     * 给图片添加文字水印
     * 此方法并不关闭流
     *
     * @param srcImage  源图像
     * @param to        目标流
     * @param pressText 水印文字
     * @param color     水印的字体颜色
     * @param font      {@link Font} 字体相关信息,如果默认则为{@code null}
     * @param x         修正值  默认在中间,偏移量相对于中间偏移
     * @param y         修正值  默认在中间,偏移量相对于中间偏移
     * @param alpha     透明度：alpha 必须是范围 [0.0, 1.0] 之内（包含边界值）的一个浮点数字
     * @throws InstrumentException IO异常
     * @since 5.2.5
     */
    public static void pressText(java.awt.Image srcImage, OutputStream to, String pressText, Color color, Font font, int x, int y, float alpha) throws InstrumentException {
        pressText(srcImage, getImageOutputStream(to), pressText, color, font, x, y, alpha);
    }

    /**
     * 给图片添加文字水印
     * 此方法并不关闭流
     *
     * @param srcImage        源图像
     * @param destImageStream 目标图像流
     * @param pressText       水印文字
     * @param color           水印的字体颜色
     * @param font            {@link Font} 字体相关信息,如果默认则为{@code null}
     * @param x               修正值  默认在中间,偏移量相对于中间偏移
     * @param y               修正值  默认在中间,偏移量相对于中间偏移
     * @param alpha           透明度：alpha 必须是范围 [0.0, 1.0] 之内（包含边界值）的一个浮点数字
     * @throws InstrumentException IO异常
     */
    public static void pressText(java.awt.Image srcImage, ImageOutputStream destImageStream, String pressText, Color color, Font font, int x, int y, float alpha) throws InstrumentException {
        writeJpg(pressText(srcImage, pressText, color, font, x, y, alpha), destImageStream);
    }

    /**
     * 给图片添加文字水印
     * 此方法并不关闭流
     *
     * @param srcImage  源图像
     * @param pressText 水印文字
     * @param color     水印的字体颜色
     * @param font      {@link Font} 字体相关信息,如果默认则为{@code null}
     * @param x         修正值  默认在中间,偏移量相对于中间偏移
     * @param y         修正值  默认在中间,偏移量相对于中间偏移
     * @param alpha     透明度：alpha 必须是范围 [0.0, 1.0] 之内（包含边界值）的一个浮点数字
     * @return 处理后的图像
     * @since 5.2.5
     */
    public static java.awt.Image pressText(java.awt.Image srcImage, String pressText, Color color, Font font, int x, int y, float alpha) {
        return Image.from(srcImage).pressText(pressText, color, font, x, y, alpha).getImg();
    }

    /**
     * 给图片添加图片水印
     *
     * @param srcImageFile  源图像文件
     * @param destImageFile 目标图像文件
     * @param pressImage    水印图片
     * @param x             修正值  默认在中间,偏移量相对于中间偏移
     * @param y             修正值  默认在中间,偏移量相对于中间偏移
     * @param alpha         透明度：alpha 必须是范围 [0.0, 1.0] 之内（包含边界值）的一个浮点数字
     */
    public static void pressImage(File srcImageFile, File destImageFile, java.awt.Image pressImage, int x, int y, float alpha) {
        pressImage(read(srcImageFile), destImageFile, pressImage, x, y, alpha);
    }

    /**
     * 给图片添加图片水印
     * 此方法并不关闭流
     *
     * @param srcStream  源图像流
     * @param destStream 目标图像流
     * @param pressImage 水印图片,可以使用{@link ImageIO#read(File)}方法读取文件
     * @param x          修正值  默认在中间,偏移量相对于中间偏移
     * @param y          修正值  默认在中间,偏移量相对于中间偏移
     * @param alpha      透明度：alpha 必须是范围 [0.0, 1.0] 之内（包含边界值）的一个浮点数字
     */
    public static void pressImage(InputStream srcStream, OutputStream destStream, java.awt.Image pressImage, int x, int y, float alpha) {
        pressImage(read(srcStream), getImageOutputStream(destStream), pressImage, x, y, alpha);
    }

    /**
     * 给图片添加图片水印
     * 此方法并不关闭流
     *
     * @param srcStream  源图像流
     * @param destStream 目标图像流
     * @param pressImage 水印图片,可以使用{@link ImageIO#read(File)}方法读取文件
     * @param x          修正值  默认在中间,偏移量相对于中间偏移
     * @param y          修正值  默认在中间,偏移量相对于中间偏移
     * @param alpha      透明度：alpha 必须是范围 [0.0, 1.0] 之内（包含边界值）的一个浮点数字
     * @throws InstrumentException IO异常
     */
    public static void pressImage(ImageInputStream srcStream, ImageOutputStream destStream, java.awt.Image pressImage, int x, int y, float alpha) throws InstrumentException {
        pressImage(read(srcStream), destStream, pressImage, x, y, alpha);
    }

    /**
     * 给图片添加图片水印
     * 此方法并不关闭流
     *
     * @param srcImage   源图像流
     * @param outFile    写出文件
     * @param pressImage 水印图片,可以使用{@link ImageIO#read(File)}方法读取文件
     * @param x          修正值  默认在中间,偏移量相对于中间偏移
     * @param y          修正值  默认在中间,偏移量相对于中间偏移
     * @param alpha      透明度：alpha 必须是范围 [0.0, 1.0] 之内（包含边界值）的一个浮点数字
     * @throws InstrumentException IO异常
     * @since 5.2.5
     */
    public static void pressImage(java.awt.Image srcImage, File outFile, java.awt.Image pressImage, int x, int y, float alpha) throws InstrumentException {
        write(pressImage(srcImage, pressImage, x, y, alpha), outFile);
    }

    /**
     * 给图片添加图片水印
     * 此方法并不关闭流
     *
     * @param srcImage   源图像流
     * @param out        目标图像流
     * @param pressImage 水印图片,可以使用{@link ImageIO#read(File)}方法读取文件
     * @param x          修正值  默认在中间,偏移量相对于中间偏移
     * @param y          修正值  默认在中间,偏移量相对于中间偏移
     * @param alpha      透明度：alpha 必须是范围 [0.0, 1.0] 之内（包含边界值）的一个浮点数字
     * @throws InstrumentException IO异常
     * @since 5.2.5
     */
    public static void pressImage(java.awt.Image srcImage, OutputStream out, java.awt.Image pressImage, int x, int y, float alpha) throws InstrumentException {
        pressImage(srcImage, getImageOutputStream(out), pressImage, x, y, alpha);
    }

    /**
     * 给图片添加图片水印
     * 此方法并不关闭流
     *
     * @param srcImage        源图像流
     * @param destImageStream 目标图像流
     * @param pressImage      水印图片,可以使用{@link ImageIO#read(File)}方法读取文件
     * @param x               修正值  默认在中间,偏移量相对于中间偏移
     * @param y               修正值  默认在中间,偏移量相对于中间偏移
     * @param alpha           透明度：alpha 必须是范围 [0.0, 1.0] 之内（包含边界值）的一个浮点数字
     * @throws InstrumentException IO异常
     */
    public static void pressImage(java.awt.Image srcImage, ImageOutputStream destImageStream, java.awt.Image pressImage, int x, int y, float alpha) throws InstrumentException {
        writeJpg(pressImage(srcImage, pressImage, x, y, alpha), destImageStream);
    }

    /**
     * 给图片添加图片水印
     * 此方法并不关闭流
     *
     * @param srcImage   源图像流
     * @param pressImage 水印图片,可以使用{@link ImageIO#read(File)}方法读取文件
     * @param x          修正值  默认在中间,偏移量相对于中间偏移
     * @param y          修正值  默认在中间,偏移量相对于中间偏移
     * @param alpha      透明度：alpha 必须是范围 [0.0, 1.0] 之内（包含边界值）的一个浮点数字
     * @return 结果图片
     */
    public static java.awt.Image pressImage(java.awt.Image srcImage, java.awt.Image pressImage, int x, int y, float alpha) {
        return org.aoju.bus.core.image.Image.from(srcImage).pressImage(pressImage, x, y, alpha).getImg();
    }

    /**
     * 给图片添加图片水印
     * 此方法并不关闭流
     *
     * @param srcImage   源图像流
     * @param pressImage 水印图片,可以使用{@link ImageIO#read(File)}方法读取文件
     * @param rectangle  矩形对象,表示矩形区域的x,y,width,height,x,y从背景图片中心计算
     * @param alpha      透明度：alpha 必须是范围 [0.0, 1.0] 之内（包含边界值）的一个浮点数字
     * @return 结果图片
     */
    public static java.awt.Image pressImage(java.awt.Image srcImage, java.awt.Image pressImage, Rectangle rectangle, float alpha) {
        return org.aoju.bus.core.image.Image.from(srcImage).pressImage(pressImage, rectangle, alpha).getImg();
    }

    /**
     * 旋转图片为指定角度
     * 此方法不会关闭输出流
     *
     * @param imageFile 被旋转图像文件
     * @param degree    旋转角度
     * @param outFile   输出文件
     * @throws InstrumentException IO异常
     * @since 5.2.5
     */
    public static void rotate(File imageFile, int degree, File outFile) throws InstrumentException {
        rotate(read(imageFile), degree, outFile);
    }

    /**
     * 旋转图片为指定角度
     * 此方法不会关闭输出流
     *
     * @param image   目标图像
     * @param degree  旋转角度
     * @param outFile 输出文件
     * @throws InstrumentException IO异常
     * @since 5.2.5
     */
    public static void rotate(java.awt.Image image, int degree, File outFile) throws InstrumentException {
        write(rotate(image, degree), outFile);
    }

    /**
     * 旋转图片为指定角度
     * 此方法不会关闭输出流
     *
     * @param image  目标图像
     * @param degree 旋转角度
     * @param out    输出流
     * @throws InstrumentException IO异常
     * @since 5.2.5
     */
    public static void rotate(java.awt.Image image, int degree, OutputStream out) throws InstrumentException {
        writeJpg(rotate(image, degree), getImageOutputStream(out));
    }

    /**
     * 旋转图片为指定角度
     * 此方法不会关闭输出流,输出格式为JPG
     *
     * @param image  目标图像
     * @param degree 旋转角度
     * @param out    输出图像流
     * @throws InstrumentException IO异常
     * @since 5.2.5
     */
    public static void rotate(java.awt.Image image, int degree, ImageOutputStream out) throws InstrumentException {
        writeJpg(rotate(image, degree), out);
    }

    /**
     * 旋转图片为指定角度
     * 来自：http://blog.51cto.com/cping1982/130066
     *
     * @param image  目标图像
     * @param degree 旋转角度
     * @return 旋转后的图片
     * @since 5.2.5
     */
    public static java.awt.Image rotate(java.awt.Image image, int degree) {
        return Image.from(image).rotate(degree).getImg();
    }

    /**
     * 水平翻转图像
     *
     * @param imageFile 图像文件
     * @param outFile   输出文件
     * @throws InstrumentException IO异常
     * @since 5.2.5
     */
    public static void flip(File imageFile, File outFile) throws InstrumentException {
        flip(read(imageFile), outFile);
    }

    /**
     * 水平翻转图像
     *
     * @param image   图像
     * @param outFile 输出文件
     * @throws InstrumentException IO异常
     * @since 5.2.5
     */
    public static void flip(java.awt.Image image, File outFile) throws InstrumentException {
        write(flip(image), outFile);
    }

    /**
     * 水平翻转图像
     *
     * @param image 图像
     * @param out   输出
     * @throws InstrumentException IO异常
     * @since 5.2.5
     */
    public static void flip(java.awt.Image image, OutputStream out) throws InstrumentException {
        flip(image, getImageOutputStream(out));
    }

    /**
     * 水平翻转图像,写出格式为JPG
     *
     * @param image 图像
     * @param out   输出
     * @throws InstrumentException IO异常
     * @since 5.2.5
     */
    public static void flip(java.awt.Image image, ImageOutputStream out) throws InstrumentException {
        writeJpg(flip(image), out);
    }

    /**
     * 水平翻转图像
     *
     * @param image 图像
     * @return 翻转后的图片
     * @since 5.2.5
     */
    public static java.awt.Image flip(java.awt.Image image) {
        return Image.from(image).flip().getImg();
    }

    /**
     * 压缩图像,输出图像只支持jpg文件
     *
     * @param imageFile 图像文件
     * @param outFile   输出文件,只支持jpg文件
     * @param quality   质量
     * @throws InstrumentException IO异常
     */
    public static void compress(File imageFile, File outFile, float quality) throws InstrumentException {
        org.aoju.bus.core.image.Image.from(imageFile).setQuality(quality).write(outFile);
    }

    /**
     * {@link java.awt.Image} 转 {@link RenderedImage}
     * 首先尝试强转,否则新建一个{@link BufferedImage}后重新绘制
     *
     * @param image {@link java.awt.Image}
     * @return {@link BufferedImage}
     */
    public static RenderedImage toRenderedImage(java.awt.Image image) {
        if (image instanceof RenderedImage) {
            return (RenderedImage) image;
        }

        return copyImage(image, BufferedImage.TYPE_INT_RGB);
    }

    /**
     * {@link java.awt.Image} 转 {@link BufferedImage}
     * 首先尝试强转,否则新建一个{@link BufferedImage}后重新绘制
     *
     * @param image {@link java.awt.Image}
     * @return {@link BufferedImage}
     */
    public static BufferedImage toBufferedImage(java.awt.Image image) {
        if (image instanceof BufferedImage) {
            return (BufferedImage) image;
        }

        return copyImage(image, BufferedImage.TYPE_INT_RGB);
    }

    /**
     * {@link java.awt.Image} 转 {@link BufferedImage}
     * 如果源图片的RGB模式与目标模式一致,则直接转换,否则重新绘制
     *
     * @param image     {@link java.awt.Image}
     * @param imageType 目标图片类型
     * @return {@link BufferedImage}
     */
    public static BufferedImage toBufferedImage(java.awt.Image image, String imageType) {
        BufferedImage bufferedImage;
        if (false == imageType.equalsIgnoreCase(FileType.IMAGE_TYPE_PNG)) {
            // 当目标为非PNG类图片时,源图片统一转换为RGB格式
            if (image instanceof BufferedImage) {
                bufferedImage = (BufferedImage) image;
                if (BufferedImage.TYPE_INT_RGB != bufferedImage.getType()) {
                    bufferedImage = copyImage(image, BufferedImage.TYPE_INT_RGB);
                }
            } else {
                bufferedImage = copyImage(image, BufferedImage.TYPE_INT_RGB);
            }
        } else {
            bufferedImage = toBufferedImage(image);
        }
        return bufferedImage;
    }

    /**
     * 将已有Image复制新的一份出来
     *
     * @param image     {@link java.awt.Image}
     * @param imageType 目标图片类型,{@link BufferedImage}中的常量,例如黑白等
     * @return {@link BufferedImage}
     * @see BufferedImage#TYPE_INT_RGB
     * @see BufferedImage#TYPE_INT_ARGB
     * @see BufferedImage#TYPE_INT_ARGB_PRE
     * @see BufferedImage#TYPE_INT_BGR
     * @see BufferedImage#TYPE_3BYTE_BGR
     * @see BufferedImage#TYPE_4BYTE_ABGR
     * @see BufferedImage#TYPE_4BYTE_ABGR_PRE
     * @see BufferedImage#TYPE_BYTE_GRAY
     * @see BufferedImage#TYPE_USHORT_GRAY
     * @see BufferedImage#TYPE_BYTE_BINARY
     * @see BufferedImage#TYPE_BYTE_INDEXED
     * @see BufferedImage#TYPE_USHORT_565_RGB
     * @see BufferedImage#TYPE_USHORT_555_RGB
     */
    public static BufferedImage copyImage(java.awt.Image image, int imageType) {
        return copyImage(image, imageType, null);
    }

    /**
     * 将已有Image复制新的一份出来
     *
     * @param image           {@link java.awt.Image}
     * @param imageType       目标图片类型,{@link BufferedImage}中的常量,例如黑白等
     * @param backgroundColor 背景色,{@code null} 表示默认背景色（黑色或者透明）
     * @return {@link BufferedImage}
     * @see BufferedImage#TYPE_INT_RGB
     * @see BufferedImage#TYPE_INT_ARGB
     * @see BufferedImage#TYPE_INT_ARGB_PRE
     * @see BufferedImage#TYPE_INT_BGR
     * @see BufferedImage#TYPE_3BYTE_BGR
     * @see BufferedImage#TYPE_4BYTE_ABGR
     * @see BufferedImage#TYPE_4BYTE_ABGR_PRE
     * @see BufferedImage#TYPE_BYTE_GRAY
     * @see BufferedImage#TYPE_USHORT_GRAY
     * @see BufferedImage#TYPE_BYTE_BINARY
     * @see BufferedImage#TYPE_BYTE_INDEXED
     * @see BufferedImage#TYPE_USHORT_565_RGB
     * @see BufferedImage#TYPE_USHORT_555_RGB
     */
    public static BufferedImage copyImage(java.awt.Image image, int imageType, Color backgroundColor) {
        final BufferedImage bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), imageType);
        final Graphics2D bGr = org.aoju.bus.core.image.Graphics.createGraphics(bimage, backgroundColor);
        bGr.drawImage(image, 0, 0, null);
        bGr.dispose();

        return bimage;
    }

    /**
     * 将Base64编码的图像信息转为 {@link BufferedImage}
     *
     * @param base64 图像的Base64表示
     * @return {@link BufferedImage}
     * @throws InstrumentException IO异常
     */
    public static BufferedImage toImage(String base64) throws InstrumentException {
        return toImage(Base64.decode(base64));
    }

    /**
     * 将的图像bytes转为 {@link BufferedImage}
     *
     * @param imageBytes 图像bytes
     * @return {@link BufferedImage}
     * @throws InstrumentException IO异常
     */
    public static BufferedImage toImage(byte[] imageBytes) throws InstrumentException {
        return read(new ByteArrayInputStream(imageBytes));
    }

    /**
     * 将图片对象转换为Base64形式
     *
     * @param image     图片对象
     * @param imageType 图片类型
     * @return Base64的字符串表现形式
     */
    public static String toBase64(java.awt.Image image, String imageType) {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        write(image, imageType, out);
        return Base64.encode(out.toByteArray());
    }

    /**
     * 根据文字创建PNG图片
     *
     * @param str             文字
     * @param font            字体{@link Font}
     * @param backgroundColor 背景颜色
     * @param fontColor       字体颜色
     * @param out             图片输出地
     * @throws InstrumentException IO异常
     */
    public static void createImage(String str, Font font, Color backgroundColor, Color fontColor, ImageOutputStream out) throws InstrumentException {
        // 获取font的样式应用在str上的整个矩形
        Rectangle2D r = font.getStringBounds(str, new FontRenderContext(AffineTransform.getScaleInstance(1, 1), false, false));
        int unitHeight = (int) Math.floor(r.getHeight());// 获取单个字符的高度
        // 获取整个str用了font样式的宽度这里用四舍五入后+1保证宽度绝对能容纳这个字符串作为图片的宽度
        int width = (int) Math.round(r.getWidth()) + 1;
        int height = unitHeight + 3;// 把单个字符的高度+3保证高度绝对能容纳字符串作为图片的高度
        // 创建图片
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_BGR);
        java.awt.Graphics g = image.getGraphics();
        g.setColor(backgroundColor);
        g.fillRect(0, 0, width, height);// 先用背景色填充整张图片,也就是背景
        g.setColor(fontColor);
        g.setFont(font);// 设置画笔字体
        g.drawString(str, 0, font.getSize());// 画出字符串
        g.dispose();
        writePng(image, out);
    }

    /**
     * 根据文件创建字体
     * 首先尝试创建{@link Font#TRUETYPE_FONT}字体,此类字体无效则创建{@link Font#TYPE1_FONT}
     *
     * @param fontFile 字体文件
     * @return {@link Font}
     * @since 3.1.9
     */
    public static Font createFont(File fontFile) {
        try {
            return Font.createFont(Font.TRUETYPE_FONT, fontFile);
        } catch (FontFormatException e) {
            // True Type字体无效时使用Type1字体
            try {
                return Font.createFont(Font.TYPE1_FONT, fontFile);
            } catch (Exception e1) {
                throw new InstrumentException(e);
            }
        } catch (IOException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 根据文件创建字体
     * 首先尝试创建{@link Font#TRUETYPE_FONT}字体,此类字体无效则创建{@link Font#TYPE1_FONT}
     *
     * @param fontStream 字体流
     * @return {@link Font}
     * @since 3.1.9
     */
    public static Font createFont(InputStream fontStream) {
        try {
            return Font.createFont(Font.TRUETYPE_FONT, fontStream);
        } catch (FontFormatException e) {
            // True Type字体无效时使用Type1字体
            try {
                return Font.createFont(Font.TYPE1_FONT, fontStream);
            } catch (Exception e1) {
                throw new InstrumentException(e1);
            }
        } catch (IOException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 创建{@link Graphics2D}
     *
     * @param image {@link BufferedImage}
     * @param color {@link Color}背景颜色以及当前画笔颜色
     * @return {@link Graphics2D}
     * @see org.aoju.bus.core.image.Graphics#createGraphics(BufferedImage, Color)
     * @since 3.2.3
     */
    public static Graphics2D createGraphics(BufferedImage image, Color color) {
        return org.aoju.bus.core.image.Graphics.createGraphics(image, color);
    }

    /**
     * 写出图像为JPG格式
     *
     * @param image           {@link java.awt.Image}
     * @param destImageStream 写出到的目标流
     * @throws InstrumentException IO异常
     */
    public static void writeJpg(java.awt.Image image, ImageOutputStream destImageStream) throws InstrumentException {
        write(image, FileType.IMAGE_TYPE_JPG, destImageStream);
    }

    /**
     * 写出图像为PNG格式
     *
     * @param image           {@link java.awt.Image}
     * @param destImageStream 写出到的目标流
     * @throws InstrumentException IO异常
     */
    public static void writePng(java.awt.Image image, ImageOutputStream destImageStream) throws InstrumentException {
        write(image, FileType.IMAGE_TYPE_PNG, destImageStream);
    }

    /**
     * 写出图像为JPG格式
     *
     * @param image {@link java.awt.Image}
     * @param out   写出到的目标流
     * @throws InstrumentException IO异常
     */
    public static void writeJpg(java.awt.Image image, OutputStream out) throws InstrumentException {
        write(image, FileType.IMAGE_TYPE_JPG, out);
    }

    /**
     * 写出图像为PNG格式
     *
     * @param image {@link java.awt.Image}
     * @param out   写出到的目标流
     * @throws InstrumentException IO异常
     */
    public static void writePng(java.awt.Image image, OutputStream out) throws InstrumentException {
        write(image, FileType.IMAGE_TYPE_PNG, out);
    }

    /**
     * 写出图像
     *
     * @param image     {@link java.awt.Image}
     * @param imageType 图片类型（图片扩展名）
     * @param out       写出到的目标流
     * @throws InstrumentException IO异常
     * @since 3.1.9
     */
    public static void write(java.awt.Image image, String imageType, OutputStream out) throws InstrumentException {
        write(image, imageType, getImageOutputStream(out));
    }

    /**
     * 写出图像为指定格式
     *
     * @param image           {@link java.awt.Image}
     * @param imageType       图片类型（图片扩展名）
     * @param destImageStream 写出到的目标流
     * @return 是否成功写出, 如果返回false表示未找到合适的Writer
     * @throws InstrumentException IO异常
     * @since 3.1.9
     */
    public static boolean write(java.awt.Image image, String imageType, ImageOutputStream destImageStream) throws InstrumentException {
        return write(image, imageType, destImageStream, 1);
    }

    /**
     * 写出图像为指定格式
     *
     * @param image           {@link java.awt.Image}
     * @param imageType       图片类型（图片扩展名）
     * @param destImageStream 写出到的目标流
     * @param quality         质量,数字为0~1（不包括0和1）表示质量压缩比,除此数字外设置表示不压缩
     * @return 是否成功写出, 如果返回false表示未找到合适的Writer
     * @throws InstrumentException IO异常
     */
    public static boolean write(java.awt.Image image, String imageType, ImageOutputStream destImageStream, float quality) throws InstrumentException {
        if (StringUtils.isBlank(imageType)) {
            imageType = FileType.IMAGE_TYPE_JPG;
        }

        final ImageWriter writer = getWriter(image, imageType);
        return write(toBufferedImage(image, imageType), writer, destImageStream, quality);
    }

    /**
     * 写出图像为目标文件扩展名对应的格式
     *
     * @param image      {@link java.awt.Image}
     * @param targetFile 目标文件
     * @throws InstrumentException IO异常
     * @since 3.1.9
     */
    public static void write(java.awt.Image image, File targetFile) throws InstrumentException {
        ImageOutputStream out = null;
        try {
            out = getImageOutputStream(targetFile);
            write(image, FileUtils.extName(targetFile), out);
        } finally {
            IoUtils.close(out);
        }
    }

    /**
     * 通过{@link ImageWriter}写出图片到输出流
     *
     * @param image   图片
     * @param writer  {@link ImageWriter}
     * @param output  输出的Image流{@link ImageOutputStream}
     * @param quality 质量,数字为0~1（不包括0和1）表示质量压缩比,除此数字外设置表示不压缩
     * @return 是否成功写出
     */
    public static boolean write(java.awt.Image image, ImageWriter writer, ImageOutputStream output, float quality) {
        if (writer == null) {
            return false;
        }

        writer.setOutput(output);
        final RenderedImage renderedImage = toRenderedImage(image);
        // 设置质量
        ImageWriteParam imgWriteParams = null;
        if (quality > 0 && quality < 1) {
            imgWriteParams = writer.getDefaultWriteParam();
            if (imgWriteParams.canWriteCompressed()) {
                imgWriteParams.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                imgWriteParams.setCompressionQuality(quality);
                final ColorModel colorModel = renderedImage.getColorModel();// ColorModel.getRGBdefault();
                imgWriteParams.setDestinationType(new ImageTypeSpecifier(colorModel, colorModel.createCompatibleSampleModel(16, 16)));
            }
        }

        try {
            if (null != imgWriteParams) {
                writer.write(null, new IIOImage(renderedImage, null, null), imgWriteParams);
            } else {
                writer.write(renderedImage);
            }
            output.flush();
        } catch (IOException e) {
            throw new InstrumentException(e);
        } finally {
            writer.dispose();
        }
        return true;
    }

    /**
     * 获得{@link ImageReader}
     *
     * @param type 图片文件类型,例如 "jpeg" 或 "tiff"
     * @return {@link ImageReader}
     */
    public static ImageReader getReader(String type) {
        final Iterator<ImageReader> iterator = ImageIO.getImageReadersByFormatName(type);
        if (iterator.hasNext()) {
            return iterator.next();
        }
        return null;
    }

    /**
     * 从文件中读取图片,请使用绝对路径,使用相对路径会相对于ClassPath
     *
     * @param imageFilePath 图片文件路径
     * @return 图片
     */
    public static BufferedImage read(String imageFilePath) {
        return read(FileUtils.file(imageFilePath));
    }

    /**
     * 从文件中读取图片
     *
     * @param imageFile 图片文件
     * @return 图片
     * @since 5.2.5
     */
    public static BufferedImage read(File imageFile) {
        try {
            return ImageIO.read(imageFile);
        } catch (IOException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 从{@link Resource}中读取图片
     *
     * @param resource 图片资源
     * @return 图片
     */
    public static BufferedImage read(Resource resource) {
        return read(resource.getStream());
    }

    /**
     * 从流中读取图片
     *
     * @param imageStream 图片文件
     * @return 图片
     * @since 5.2.5
     */
    public static BufferedImage read(InputStream imageStream) {
        try {
            return ImageIO.read(imageStream);
        } catch (IOException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 从图片流中读取图片
     *
     * @param imageStream 图片文件
     * @return 图片
     * @since 5.2.5
     */
    public static BufferedImage read(ImageInputStream imageStream) {
        try {
            return ImageIO.read(imageStream);
        } catch (IOException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 从URL中读取图片
     *
     * @param imageUrl 图片文件
     * @return 图片
     * @since 5.2.5
     */
    public static BufferedImage read(URL imageUrl) {
        try {
            return ImageIO.read(imageUrl);
        } catch (IOException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 获取{@link ImageOutputStream}
     *
     * @param out {@link OutputStream}
     * @return {@link ImageOutputStream}
     * @throws InstrumentException IO异常
     * @since 3.1.9
     */
    public static ImageOutputStream getImageOutputStream(OutputStream out) throws InstrumentException {
        try {
            return ImageIO.createImageOutputStream(out);
        } catch (IOException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 获取{@link ImageOutputStream}
     *
     * @param outFile {@link File}
     * @return {@link ImageOutputStream}
     * @throws InstrumentException IO异常
     * @since 5.2.5
     */
    public static ImageOutputStream getImageOutputStream(File outFile) throws InstrumentException {
        try {
            return ImageIO.createImageOutputStream(outFile);
        } catch (IOException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 获取{@link ImageInputStream}
     *
     * @param in {@link InputStream}
     * @return {@link ImageInputStream}
     * @throws InstrumentException IO异常
     * @since 3.1.9
     */
    public static ImageInputStream getImageInputStream(InputStream in) throws InstrumentException {
        try {
            return ImageIO.createImageInputStream(in);
        } catch (IOException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 根据给定的Image对象和格式获取对应的{@link ImageWriter},如果未找到合适的Writer,返回null
     *
     * @param image      {@link java.awt.Image}
     * @param formatName 图片格式,例如"jpg"、"png"
     * @return {@link ImageWriter}
     */
    public static ImageWriter getWriter(java.awt.Image image, String formatName) {
        final ImageTypeSpecifier type = ImageTypeSpecifier.createFromRenderedImage(toRenderedImage(image));
        final Iterator<ImageWriter> iter = ImageIO.getImageWriters(type, formatName);
        return iter.hasNext() ? iter.next() : null;
    }

    /**
     * 根据给定的图片格式或者扩展名获取{@link ImageWriter},如果未找到合适的Writer,返回null
     *
     * @param formatName 图片格式或扩展名,例如"jpg"、"png"
     * @return {@link ImageWriter}
     */
    public static ImageWriter getWriter(String formatName) {
        ImageWriter writer = null;
        Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName(formatName);
        if (iter.hasNext()) {
            writer = iter.next();
        }
        if (null == writer) {
            // 尝试扩展名获取
            iter = ImageIO.getImageWritersBySuffix(formatName);
            if (iter.hasNext()) {
                writer = iter.next();
            }
        }
        return writer;
    }

    /**
     * Color对象转16进制表示,例如#fcf6d6
     *
     * @param color {@link Color}
     * @return 16进制的颜色值, 例如#fcf6d6
     */
    public static String toHex(Color color) {
        String R = Integer.toHexString(color.getRed());
        R = R.length() < 2 ? ('0' + R) : R;
        String G = Integer.toHexString(color.getGreen());
        G = G.length() < 2 ? ('0' + G) : G;
        String B = Integer.toHexString(color.getBlue());
        B = B.length() < 2 ? ('0' + B) : B;
        return '#' + R + G + B;
    }

    /**
     * 16进制的颜色值转换为Color对象,例如#fcf6d6
     *
     * @param hex 16进制的颜色值,例如#fcf6d6
     * @return {@link Color}
     */
    public static Color hexToColor(String hex) {
        return getColor(Integer.parseInt(StringUtils.removePrefix("#", hex), 16));
    }

    /**
     * 获取一个RGB值对应的颜色
     *
     * @param rgb RGB值
     * @return {@link Color}
     */
    public static Color getColor(int rgb) {
        return new Color(rgb);
    }

    /**
     * 将颜色值转换成具体的颜色类型 汇集了常用的颜色集,支持以下几种形式：
     *
     * <pre>
     * 1. 颜色的英文名（大小写皆可）
     * 2. 16进制表示,例如：#fcf6d6或者$fcf6d6
     * 3. RGB形式,例如：13,148,252
     * </pre>
     * <p>
     * 方法来自：com.lnwazg.kit
     *
     * @param colorName 颜色的英文名,16进制表示或RGB表示
     * @return {@link Color}
     */
    public static Color getColor(String colorName) {
        if (StringUtils.isBlank(colorName)) {
            return null;
        }
        colorName = colorName.toUpperCase();

        if ("BLACK".equals(colorName)) {
            return Color.BLACK;
        } else if ("WHITE".equals(colorName)) {
            return Color.WHITE;
        } else if ("LIGHTGRAY".equals(colorName) || "LIGHT_GRAY".equals(colorName)) {
            return Color.LIGHT_GRAY;
        } else if ("GRAY".equals(colorName)) {
            return Color.GRAY;
        } else if ("DARK_GRAY".equals(colorName) || "DARK_GRAY".equals(colorName)) {
            return Color.DARK_GRAY;
        } else if ("RED".equals(colorName)) {
            return Color.RED;
        } else if ("PINK".equals(colorName)) {
            return Color.PINK;
        } else if ("ORANGE".equals(colorName)) {
            return Color.ORANGE;
        } else if ("YELLOW".equals(colorName)) {
            return Color.YELLOW;
        } else if ("GREEN".equals(colorName)) {
            return Color.GREEN;
        } else if ("MAGENTA".equals(colorName)) {
            return Color.MAGENTA;
        } else if ("CYAN".equals(colorName)) {
            return Color.CYAN;
        } else if ("BLUE".equals(colorName)) {
            return Color.BLUE;
        } else if ("DARKGOLD".equals(colorName)) {
            // 暗金色
            return hexToColor("#9e7e67");
        } else if ("LIGHTGOLD".equals(colorName)) {
            // 亮金色
            return hexToColor("#ac9c85");
        } else if (StringUtils.startWith(colorName, '#')) {
            return hexToColor(colorName);
        } else if (StringUtils.startWith(colorName, '$')) {
            // 由于#在URL传输中无法传输,因此用$代替#
            return hexToColor("#" + colorName.substring(1));
        } else {
            // rgb值
            final List<String> rgb = StringUtils.split(colorName, ',');
            if (3 == rgb.size()) {
                final Integer r = Convert.toInt(rgb.get(0));
                final Integer g = Convert.toInt(rgb.get(1));
                final Integer b = Convert.toInt(rgb.get(2));
                if (false == ArrayUtils.hasNull(r, g, b)) {
                    return new Color(r, g, b);
                }
            } else {
                return null;
            }
        }
        return null;
    }

    /**
     * 生成随机颜色
     *
     * @return 随机颜色
     * @since 3.1.9
     */
    public static Color randomColor() {
        return randomColor(null);
    }

    /**
     * 生成随机颜色
     *
     * @param random 随机对象 {@link Random}
     * @return 随机颜色
     * @since 3.1.9
     */
    public static Color randomColor(Random random) {
        if (null == random) {
            random = RandomUtils.getRandom();
        }
        return new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255));
    }

}
