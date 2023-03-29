package com.literam.matrix.music.entity;

/**
 * 歌词行
 * 包括该行歌词的时间，歌词内容
 */
public class LrcRow implements Comparable<LrcRow> {

    //开始时间 为00:10:00
    private String startTimeStr;


    //该行歌词要开始播放的时间，由[02:34.14]格式转换为long型，
    //即将2分34秒14毫秒都转为毫秒后 得到的long型值：startTime=02*60*1000+34*1000+14
    //开始时间 毫米数  00:10:00  为10000
    private int startTime;

    //歌词内容
    private String content;

    //该行歌词显示的总时间
    private int totalTime;

    public int getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(int totalTime) {
        this.totalTime = totalTime;
    }

    public String getStartTimeStr() {
        return startTimeStr;
    }

    public void setStartTimeStr(String startTimeStr) {
        this.startTimeStr = startTimeStr;
    }

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LrcRow() {
        super();
    }

    /*private LrcRow(String startTimeStr, int startTime, String content) {
        super();
        this.startTimeStr = startTimeStr;
        this.startTime = startTime;
        this.content = content;
    }*/

    /**
     * 将歌词文件中的某一行 解析成一个List<LrcRow>
     * 因为一行中可能包含了多个LrcRow对象
     * 比如  [03:33.02][00:36.37]当鸽子不再象征和平  ，就包含了2个对象
     *
     * @param lrcLine
     * @return
     */
    /*public static final List<LrcRow> createRows(String lrcLine) {
        if (!lrcLine.startsWith("[")) {
            return null;
        }
        //最后一个"]"
        int lastIndexOfRightBracket = lrcLine.lastIndexOf("]");
        //歌词内容
        String content = lrcLine.substring(lastIndexOfRightBracket + 1);
        //截取出歌词时间，并将"[" 和"]" 替换为"-"   [offset:0]
        Log.e("歌词","lrcLine=" + lrcLine);
        // -03:33.02--00:36.37-
        String times = lrcLine.substring(0, lastIndexOfRightBracket + 1).replace("[", "-").replace("]", "-");
        String[] timesArray = times.split("-");
        List<LrcRow> lrcRows = new ArrayList<>();
        for (String tem : timesArray) {
            if (TextUtils.isEmpty(tem.trim())) {
                continue;
            }
            //
            try {
                LrcRow lrcRow = new LrcRow(tem, formatTime(tem), content);
                lrcRows.add(lrcRow);
            } catch (Exception e) {
                Log.w("LrcRow", e.getMessage());
            }
        }
        return lrcRows;
    }*/

    /****
     * 把歌词时间转换为毫秒值  如 将00:10.00  转为10000
     *
     * @return
     */
    /*private static int formatTime(String timeStr) {
        timeStr = timeStr.replace('.', ':');
        String[] times = timeStr.split(":");

        return Integer.parseInt(times[0]) * 60 * 1000
                + Integer.parseInt(times[1]) * 1000
                + Integer.parseInt(times[2]);
    }*/

    @Override
    public int compareTo(LrcRow anotherLrcRow) {
        return this.startTime - anotherLrcRow.startTime;
    }

    @Override
    public String toString() {
        return "LrcRow [startTimeStr=" + startTimeStr + ", startTime=" + startTime + ", content="
                + content + "]";
    }
}