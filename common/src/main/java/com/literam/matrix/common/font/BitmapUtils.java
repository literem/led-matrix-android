package com.literam.matrix.common.font;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ThumbnailUtils;

public class BitmapUtils {

    public Bitmap convertToBMW(Bitmap bitmap,int tmp){
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];

        bitmap.getPixels(pixels,0,width,0,0,width,height);

        int alpha;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int grey = pixels[width * i +j];
                alpha = grey & -0x1000000 >> 24;
                int red = grey & 0x00FF0000 >> 16;
                int green = grey & 0x0000FF00 >> 8;
                int blue = grey & 0x000000FF;
                red = red > tmp ? 255 : 0;
                blue = blue > tmp ? 255 : 0;
                green = green > tmp ? 255 : 0;

                pixels[width * i + j] = (alpha << 24 | (red << 16) | (green << 8) | blue);
                if (pixels[width * i + j] == -1) {
                    pixels[width * i + j] = -1;
                } else {
                    pixels[width * i + j] = -16777216;
                }

            }
        }

        // 新建图片
        Bitmap newBmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        // 设置图片数据
        newBmp.setPixels(pixels, 0, width, 0, 0, width, height);

        //imageView5.setImageBitmap(bitmap);
        return ThumbnailUtils.extractThumbnail(newBmp, width, height);
    }

    public Bitmap convertGreyImgByFloyd(Bitmap img){
        int width = img.getWidth(); //获取位图的宽
        int height = img.getHeight(); //获取位图的高
        int[] pixels = new int[width * height]; //通过位图的大小创建像素点数组
        img.getPixels(pixels, 0, width, 0, 0, width, height);
        int[] gray = new int[width * height];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int grey = pixels[width * i + j];
                int red = grey & 0x00FF0000 >> 16;
                gray[width * i + j] = red;
            }
        }
        int e;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int g = gray[width * i + j];
                if (g >= 128) {
                    pixels[width * i + j] = -0x1;
                    e = g - 255;
                } else {
                    pixels[width * i + j] = -0x1000000;
                    e = g;
                }
                if (j < width - 1 && i < height - 1) {
                    gray[width * i + j + 1] += 3 * e / 8;//右边像素处理
                    gray[width * (i + 1) + j] += 3 * e / 8;//下
                    gray[width * (i + 1) + j + 1] += e / 4;//右下
                } else if (j == width - 1 && i < height - 1) {//靠右或靠下边的像素的情况
                    gray[width * (i + 1) + j] += 3 * e / 8; //下方像素处理
                } else if (j < width - 1 && i == height - 1) {
                    gray[width * i + j + 1] += e / 4;//右边像素处理
                }
            }
        }
        Bitmap mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        mBitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        //imageView5.setImageBitmap(mBitmap);
        //saveBmp(mBitmap);
        return mBitmap;
    }

    public static Bitmap scaleBitmap(Bitmap origin, int newWidth, int newHeight) {
        if (origin == null) {
            return null;
        }
        int height = origin.getHeight();
        int width = origin.getWidth();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);// 使用后乘
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        /*if (!origin.isRecycled()) {
            origin.recycle();
        }*/
        return newBM;
    }

    //转灰度
    public static Bitmap toGrayScale(Bitmap bmpOriginal) {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }

    public static Bitmap switchBlackNWhiteColor(Bitmap switchBitmap){
        int width=switchBitmap.getWidth();
        int height=switchBitmap.getHeight();

        // Turn the picture black and white
        Bitmap newBitmap = Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawBitmap(switchBitmap, new Matrix(), new Paint());

        int current_color,red,green,blue,alpha,avg=0;
        for (int i=0;i<width;i++){
            for (int j=0;j<height;j++){
                //get the color of each bit
                current_color=switchBitmap.getPixel(i, j);
                //achieve  three-primary color
                red = Color.red(current_color);
                green = Color.green(current_color);
                blue = Color.blue(current_color);
                alpha = Color.alpha(current_color);
                avg = (red+green+blue)/3;// RGB average


                if (avg>=126){
                    newBitmap.setPixel(i, j, Color.argb(alpha, 255, 255, 255));// white
                } else if (avg >= 115){
                    newBitmap.setPixel(i, j, Color.argb(alpha, 108	,108,108));//grey
                } else{
                    newBitmap.setPixel(i, j, Color.argb(alpha, 0, 0, 0));// black
                }


            }
        }
        return newBitmap;
    }

    //图片缩放成scaleSize，并且转换成黑白带灰度的图片
    public static Bitmap ScaleAndGray(Bitmap origin,int scaleSize){
        int height = origin.getHeight();
        int width = origin.getWidth();
        float scaleWidth = ((float) scaleSize) / width;
        float scaleHeight = ((float) scaleSize) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);// 使用后乘
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        /*if (!origin.isRecycled()) {
            origin.recycle();
        }*/

        width = newBM.getWidth();
        height = newBM.getHeight();

        Bitmap newBitmap = Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawBitmap(newBM, new Matrix(), new Paint());
        int current_color,red,green,blue,alpha,avg;
        for (int i=0;i<width;i++){
            for (int j=0;j<height;j++){
                //获得每一个位点的颜色
                current_color = newBM.getPixel(i, j);
                //获得三原色
                red=Color.red(current_color);
                green=Color.green(current_color);
                blue=Color.blue(current_color);
                alpha=Color.alpha(current_color);
                avg=(red+green+blue)/3;// RGB average
                if (avg >= 210){  //亮度：avg>=126
                    //设置颜色
                    newBitmap.setPixel(i, j, Color.argb(alpha, 255, 255, 255));// white
                } else if (avg >= 80){  //avg<126 && avg>=115
                    newBitmap.setPixel(i, j, Color.argb(alpha, 108,108,108));//grey
                }else{
                    newBitmap.setPixel(i, j, Color.argb(alpha, 0, 0, 0));// black
                }
            }
        }
        return newBitmap;
    }

    public static Bitmap switchColor(Bitmap switchBitmap){
        int width = switchBitmap.getWidth();
        int height = switchBitmap.getHeight();

        // Turn the picture black and white
//		Bitmap newBitmap=Bitmap.createBitmap(width,height,Bitmap.Config.RGB_565);
        Bitmap newBitmap = Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawBitmap(switchBitmap, new Matrix(), new Paint());

        int current_color,red,green,blue,alpha,avg;
        for (int i=0;i<width;i++){
            for (int j=0;j<height;j++){
                //获得每一个位点的颜色
                current_color=switchBitmap.getPixel(i, j);
                //获得三原色
                red=Color.red(current_color);
                green=Color.green(current_color);
                blue=Color.blue(current_color);
                alpha=Color.alpha(current_color);
                avg=(red+green+blue)/3;// RGB average
                if (avg>=210){  //亮度：avg>=126
                    //设置颜色
                    newBitmap.setPixel(i, j, Color.argb(alpha, 255, 255, 255));// white
                } else if (avg >= 80){  //avg<126 && avg>=115
                    newBitmap.setPixel(i, j, Color.argb(alpha, 108,108,108));//grey
                }else{
                    newBitmap.setPixel(i, j, Color.argb(alpha, 0, 0, 0));// black
                }
            }
        }
        return newBitmap;
    }
}
