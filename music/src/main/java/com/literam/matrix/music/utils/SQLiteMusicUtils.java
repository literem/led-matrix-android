package com.literam.matrix.music.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;

import com.literam.matrix.common.sqlite.SQLiteHelper;
import com.literam.matrix.music.entity.LocalMusicBean;
import com.literam.matrix.music.entity.Music;
import com.literam.matrix.music.entity.PlayList;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SQLiteMusicUtils {

    private static SQLiteMusicUtils singleton = null;
    private SQLiteHelper helper = null;
    public static SQLiteMusicUtils getInstance(Context context){
        if(singleton == null){
            synchronized (SQLiteHelper.class){
                if(singleton == null){
                    singleton = new SQLiteMusicUtils();
                    singleton.helper = new SQLiteHelper(context);
                }
            }
        }
        return singleton;
    }

    //------------------- 歌曲操作 ------------------------
    /**
     * 新增一首歌曲
     */
    public int insertMusic(LocalMusicBean bean) {
        try{
            SQLiteDatabase db = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            //values.put("id",0);
            values.put("song_id",bean.getId());
            values.put("title",bean.getTitle());
            values.put("singer",bean.getSigner());
            values.put("album",bean.getAlbum());
            values.put("length",bean.getLength());
            values.put("path",bean.getUri());
            values.put("image",bean.getImage());
            values.put("lyric","");
            values.put("sort",0);
            values.put("add_time",getNowTime());
            int result = (int) db.insert("music",null,values);
            db.close();
            return result;
        }catch (SQLiteConstraintException e){
            return -1;
        }
    }


    public void insertMusicList(List<LocalMusicBean> list){
        SQLiteDatabase db = null;
        try{
            db = helper.getWritableDatabase();
            db.beginTransaction();
            for (LocalMusicBean bean : list){
                ContentValues values = new ContentValues();
                //values.put("id",0);
                values.put("song_id",bean.getId());
                values.put("title",bean.getTitle());
                values.put("singer",bean.getSigner());
                values.put("album",bean.getAlbum());
                values.put("length",bean.getLength());
                values.put("path",bean.getUri());
                values.put("image",bean.getImage());
                values.put("lyric","");
                values.put("sort",0);
                values.put("add_time",getNowTime());
                db.insert("music",null,values);
            }
            db.setTransactionSuccessful();//设置回滚
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(null != db){
                db.endTransaction();//提交事务
                db.close();
            }
        }

    }

    /**
     * 根据id删除歌曲
     * @param id id
     */
    public boolean deleteMusic(int id) {
        SQLiteDatabase db = helper.getWritableDatabase();
        int i = db.delete("music","id=?",new String[]{String.valueOf(id)});
        db.close();
        return i != 0;
    }

    public boolean removeLyricByMusicId(int musicId){
        SQLiteDatabase db = helper.getWritableDatabase();
        try{
            db.execSQL("update music set lyric=? where id=?",new String[]{"", String.valueOf(musicId)});
            db.close();
            return true;
        }catch (Exception e){
            if(db != null) db.close();
            return false;
        }
        //int result = db.update("music",values,"id=?",new String[]{String.valueOf(musicId)});
    }


    /**
     * 设置sort id为插入的id
     * @param id id
     */
    private void setMusicSortId(String id){
        SQLiteDatabase db = helper.getWritableDatabase();
        db.execSQL("update music set sort_id=? where id=?",new String[]{id,id});
        db.close();
    }

    /**
     * 通过id修改歌曲 名称、歌手
     */
    public boolean updateMusic(int id, String name, String signer) {
        SQLiteDatabase sqlWrite = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("title",name);
        values.put("signer",signer);
        int i = sqlWrite.update("music",values,"id=?",new String[]{String.valueOf(id)});
        return i > 0;
    }

    /**
     * 通过id查询音乐
     * @param id 数据库中的唯一id
     * @return music对象
     */
    public Music queryOneMusicById(int id){
        SQLiteDatabase sqlRead = helper.getReadableDatabase();
        Cursor cursor = sqlRead.rawQuery("select * from music where id=?",new String[]{String.valueOf(id)});
        if(cursor == null) return null;
        Music music = null;
        if(cursor.moveToFirst()){
            music = new Music();
            music.setId(cursor.getInt(0));
            music.setSongId(cursor.getInt(1));
            music.setTitle(cursor.getString(2));
            music.setSinger(cursor.getString(3));
            music.setAlbum(cursor.getString(4));
            music.setDuration(cursor.getInt(5));
            music.setPath(cursor.getString(6));
            music.setImage(cursor.getString(7));
            music.setLyric(cursor.getString(8));
            music.setSortId(cursor.getInt(9));
            music.setAddTime(cursor.getString(10));
        }
        sqlRead.close();
        cursor.close();
        return music;
    }

    /**
     * 查询数据库中的所有音乐
     * @return list_tag
     */
    public List<Music> queryAllMusic() {
        SQLiteDatabase sqlRead = helper.getReadableDatabase();
        List<Music> list = new ArrayList<>();
        Cursor cursor = sqlRead.rawQuery("select * from music",null);
        if(cursor == null) return list;
        while (cursor.moveToNext()) {
            Music music = new Music();
            music.setId(cursor.getInt(0));
            music.setSongId(cursor.getInt(1));
            music.setTitle(cursor.getString(2));
            music.setSinger(cursor.getString(3));
            music.setAlbum(cursor.getString(4));
            music.setDuration(cursor.getInt(5));
            music.setPath(cursor.getString(6));
            music.setImage(cursor.getString(7));
            music.setLyric(cursor.getString(8));
            music.setSortId(cursor.getInt(9));
            music.setAddTime(cursor.getString(10));
            list.add(music);
        }
        cursor.close();
        return list;
    }

    /**
     * 更新歌词
     * @param musicId id
     * @param lrcContent 歌词
     * @return 是否成功
     */
    public boolean updateMusicLyric(int musicId, String lrcContent){
        SQLiteDatabase sqlWrite = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("lyric",lrcContent);
        int i = sqlWrite.update("music",values,"id=?",new String[]{String.valueOf(musicId)});
        return i>0;
    }

    public String queryMusicLyric(int musicId){
        SQLiteDatabase sqlRead = helper.getReadableDatabase();
        Cursor cursor = sqlRead.rawQuery("select lyric from music where id=?",new String[]{String.valueOf(musicId)});
        String result = "";
        if(cursor.moveToFirst()){
            result = cursor.getString(0);
        }
        cursor.close();
        return result;
    }


    public void clearMusicList(){
        SQLiteDatabase sqlWrite = helper.getWritableDatabase();
        sqlWrite.execSQL("delete from music;");
        sqlWrite.execSQL("delete from lyric;");
        sqlWrite.execSQL("delete from play_list;");
        sqlWrite.close();
    }


    /************************** PlayList相关 ********************************/
    //region

    //插入一条播放列表信息
    public int insertPlayList(PlayList playList){
        SQLiteDatabase sqlWrite = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("music_id",playList.getMusicId());
        values.put("title",playList.getTitle());
        values.put("singer",playList.getSinger());
        long i = sqlWrite.insert("play_list",null,values);
        if(i < 0) return -1;
        sqlWrite.execSQL("update play_list set sort=" + i + " where id=" + i);
        sqlWrite.close();
        return (int) i;
    }

    //查询所有播放列表
    public List<PlayList> queryPlayList(){
        SQLiteDatabase sqlRead = helper.getReadableDatabase();
        List<PlayList> list = new ArrayList<>();
        Cursor cursor = sqlRead.rawQuery("select * from play_list",null);
        if(cursor == null) return list;
        int num = 1;
        while (cursor.moveToNext()) {
            PlayList playList = new PlayList();
            playList.setId(cursor.getInt(0));
            playList.setMusicId(cursor.getInt(1));
            playList.setTitle(cursor.getString(2));
            playList.setSinger(cursor.getString(3));
            playList.setSort(cursor.getInt(4));
            playList.setNumber(num++);
            list.add(playList);
        }
        cursor.close();
        sqlRead.close();
        return list;
    }

    //通过id删除一条播放列表信息
    public boolean deleteOnePlayListById(int id){
        SQLiteDatabase sqlWrite = helper.getWritableDatabase();
        int i = sqlWrite.delete("play_list","id=?",new String[]{String.valueOf(id)});
        sqlWrite.close();
        return i == 1;
    }

    public boolean removePlayListByMusicId(int musicId){
        SQLiteDatabase db = helper.getWritableDatabase();
        int i = db.delete("play_list","music_id=?",new String[]{String.valueOf(musicId)});
        db.close();
        return i == 1;
    }

    //清空播放列表
    public void cleanPlayList(){
        String sql = "delete from play_list;";
        SQLiteDatabase sqlWrite = helper.getWritableDatabase();
        sqlWrite.execSQL(sql);
        sqlWrite.close();
    }
    //endregion


    /**
     * 获取当前时间
     * @return String类型的时间
     */
    private String getNowTime() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        return sdf.format(date);
    }
}