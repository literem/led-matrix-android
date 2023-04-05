package com.literem.matrix.common.utils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FileUtils {

    // 将字符串写入到文本文件中
    public static boolean writeTxtToFile(String content, String filePath, String fileName) {
        try {
            File file = createFileIfNotExists(filePath, fileName);
            if (file == null) return false;
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    return false;
                }
            }

            RandomAccessFile mRandomAccessFile = new RandomAccessFile(file, "rwd");
            mRandomAccessFile.seek(file.length());
            mRandomAccessFile.write(content.getBytes());
            mRandomAccessFile.close();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    //生成文件
    private static File createFileIfNotExists(String filePath, String fileName) {
        if(!createDirectory(filePath)) return null;//若创建文件夹失败返回空，文件存在或创建成功继续执行

        try {
            File file = new File(filePath + fileName);
            if (file.exists()){//文件存在，加上时间戳再创建文件
                File newFile = new File(filePath + getNewName(fileName));
                if (newFile.createNewFile()) {
                    return newFile;
                }
                return null;
            }
            if (!file.createNewFile()) {//文件不存在才创建文件，若创建失败返回空
                return null;
            }
            return file;
        } catch (IOException e) {
            return null;
        }
    }

    //生成文件夹
    private static boolean createDirectory(String filePath) {
        File file;
        try {
            file = new File(filePath);
            if (file.exists()) return true;
            return file.mkdir();
        } catch (Exception e) {
            return false;
        }
    }

    private static String getNewName(String name){
        int pos = name.lastIndexOf('.');
        if (pos < 0){
            return name;
        }
        String fileName = name.substring(0,pos);
        String suffix = name.substring(pos);
        return fileName + TimeUtils.getNowTime() + suffix;
    }

}
