package com.literam.matrix.music.utils;

import android.util.Log;


import com.literam.matrix.music.entity.LrcRow;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 解析歌词，得到LrcRow的集合
 */
public class DefaultLrcBuilder implements ILrcBuilder {
    //static final String TAG = "DefaultLrcBuilder";

    /*
     * 解析歌词的第一种方法
     *
     * 可以解析多个时间的歌词
     *
     */

    /**
     * 把文本类型的歌词解析成List对象
     * @param lrc 歌词文本
     * @return list
     */
    public List<LrcRow> getLrcRows(String lrc){
        return AnalyzeLrc(new ByteArrayInputStream(lrc.getBytes()));
    }

    /**
     * 通过路径获取歌词文本
     * @param path 路径
     * @return 歌词文本
     */
    public String getLrcString(String path){
        File file = new File(path);
        if (!file.exists()) {
            Log.i("test","文件不存在");
            return null;
        }

        BufferedReader reader;
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            while (null != (line = reader.readLine())) {
                if(line.trim().length() == 0) continue;
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    /**
     * 通过路径获取List集合LrcRow对象的歌词
     * @param path 路径
     * @return list
     */
    public List<LrcRow> getLrcRowsByPath(String path){
        File file = new File(path);
        if (!file.exists()) {
            //Log.i("test","文件不存在");
            return null;
        }

        try {
            return AnalyzeLrc(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    //解析歌词
    private List<LrcRow> AnalyzeLrc(InputStream inputStream) {
        BufferedReader reader = null;
        String line;
        List<LrcRow> rows = new ArrayList<>();
        try {
            reader = new BufferedReader(new InputStreamReader(inputStream));
            while (null != (line = reader.readLine())) {
                 /*
                 一行歌词只有一个时间的  例如：徐佳莹   《我好想你》
                 [01:15.33]我好想你 好想你

                 一行歌词有多个时间的  例如：草蜢 《失恋战线联盟》
                 [02:34.14][01:07.00]当你我不小心又想起她
                 [02:45.69][02:42.20][02:37.69][01:10.60]就在记忆里画一个叉
                 **/
                if (line.length() > 0) {
                    //解析每一行歌词 得到每行歌词的集合，因为有些歌词重复有多个时间，就可以解析出多个歌词行来
                    List<LrcRow> lrcRows = createRows(line.trim());
                    if (lrcRows != null && lrcRows.size() > 0) {
                        rows.addAll(lrcRows);
                    }
                }
            }

            if (rows.size() > 0) {
                // 根据歌词行的时间排序
                Collections.sort(rows);
                if (rows.size() > 0) {
                    int size = rows.size();
                    for (int i = 0; i < size; i++) {
                        LrcRow lrcRow = rows.get(i);
                        /*
                         * 这里设置下 每一行歌词 结尾的时间
                         * 设置为 下一行歌词的开始时间
                         *
                         * 这样还是有点不准，只是粗略的这么计算，仅做demo使用。
                         * 如果要做为商用使用的话，自己重新计算每一行的结束时间
                         */
                        if (i < size - 1) {
                            //lrcRow.setEndTime(rows.get(i + 1).getStartTime());
                            lrcRow.setTotalTime(rows.get(i + 1).getStartTime());
                        } else {
                            //lrcRow.setEndTime(lrcRow.getStartTime() + 10000);
                            lrcRow.setTotalTime(lrcRow.getStartTime() + 10000);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return rows;
    }

    /**
     * 读取歌词的每一行内容，转换为LrcRow，加入到集合中
     */
    private List<LrcRow> createRows(String standardLrcLine) {
        /*
         一行歌词只有一个时间的  例如：徐佳莹   《我好想你》
         [01:15.33]我好想你 好想你

         一行歌词有多个时间的  例如：草蜢 《失恋战线联盟》
         [02:34.14][01:07.00]当你我不小心又想起她
         [02:45.69][02:42.20][02:37.69][01:10.60]就在记忆里画一个叉
         **/

        try {
            if(standardLrcLine.charAt(0) != '['){
                return null;
            }
            if(standardLrcLine.charAt(9) != ']' && standardLrcLine.charAt(10) != ']'){
                return null;
            }

            /*if (standardLrcLine.indexOf("[") != 0 || standardLrcLine.indexOf("]") != 9) {
                return null;
            }*/

            //[02:34.14][01:07.00]当你我不小心又想起她
            //找到最后一个 ‘]’ 的位置
            int lastIndexOfRightBracket = standardLrcLine.lastIndexOf("]");

            //歌词内容就是 ‘]’ 的位置之后的文本   eg:   当你我不小心又想起她
            String content = standardLrcLine.substring(lastIndexOfRightBracket + 1);
            //歌词时间就是 ‘]’ 的位置之前的文本   eg:   [02:34.14][01:07.00]

            /*
             将时间格式转换一下  [mm:ss.SS][mm:ss.SS] 转换为  -mm:ss.SS--mm:ss.SS-
             即：[02:34.14][01:07.00]  转换为      -02:34.14--01:07.00-
             */
            String times = standardLrcLine.substring(0, lastIndexOfRightBracket + 1).replace("[", "-").replace("]", "-");
            //通过 ‘-’ 来拆分字符串
            String[] arrTimes = times.split("-");
            List<LrcRow> listTimes = new ArrayList<>();
            for (String temp : arrTimes) {
                if (temp.trim().length() == 0) {
                    continue;
                }

                /* [02:34.14][01:07.00]当你我不小心又想起她
                 *
                 上面的歌词的就可以拆分为下面两句歌词了
                 [02:34.14]当你我不小心又想起她
                 [01:07.00]当你我不小心又想起她
                 */
                LrcRow lrcRow = new LrcRow();
                lrcRow.setContent(content);
                lrcRow.setStartTimeStr(temp);
                int startTime = timeConvert(temp);
                lrcRow.setStartTime(startTime);
                listTimes.add(lrcRow);
            }
            return listTimes;
        } catch (Exception e) {
            //e.printStackTrace();
            return null;
        }
    }

    /**
     * 将解析得到的表示时间的字符转化为Long型
     */
    private int timeConvert(String timeString) {
        //因为给如的字符串的时间格式为XX:XX.XX,返回的long要求是以毫秒为单位
        //将字符串 XX:XX.XX 转换为 XX:XX:XX
        timeString = timeString.replace('.', ':');
        //将字符串 XX:XX:XX 拆分
        String[] times = timeString.split(":");
        // mm:ss:SS
        return Integer.valueOf(times[0]) * 60 * 1000 +//分
                Integer.valueOf(times[1]) * 1000 +//秒
                Integer.valueOf(times[2]);//毫秒
    }


}
