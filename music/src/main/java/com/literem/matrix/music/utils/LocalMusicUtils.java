package com.literem.matrix.music.utils;

import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.literem.matrix.common.base.BaseApplication;
import com.literem.matrix.music.entity.LocalMusicBean;
import com.literem.matrix.music.entity.Lrc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LocalMusicUtils {
	/**
	 * 根据id获取歌曲uri
	 * @deprecated
	 * @param musicId music id
	 * @return uri
	 */
	public static String queryMusicById(int musicId) {
		String result = null;
		Cursor cursor = BaseApplication.getContext().getContentResolver().query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				new String[] { MediaStore.Audio.Media.DATA },
				MediaStore.Audio.Media._ID + "=?",
				new String[] { String.valueOf(musicId) }, null);

		if(cursor == null) return null;

		cursor.moveToFirst();
		if(!cursor.isAfterLast()) {
			result = cursor.getString(0);
		}
		cursor.close();
		return result;
	}

	public static ArrayList<LocalMusicBean> queryLocalAllMusic(){
		return queryLocalMusic(getBaseDir());
	}

	/**
	 * 获取指定路径下的所有歌曲
	 * @param dirName music path
	 */
	 private static ArrayList<LocalMusicBean> queryLocalMusic(String dirName) {
		ArrayList<LocalMusicBean> results = new ArrayList<>();

		Cursor cursor = BaseApplication.getContext().getContentResolver().query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,
				MediaStore.Audio.Media.DATA + " like ?",
				new String[] { dirName + "%" },
				MediaStore.Audio.Media.DEFAULT_SORT_ORDER);

		if(cursor == null) return results;
		
		// id title singer data time image
		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {

			// 如果不是音乐
			String isMusic = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_MUSIC));
			if (isMusic != null && isMusic.equals("")) continue;

			LocalMusicBean music = new LocalMusicBean();
			music.setId((int)cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)));
			music.setLength((int)cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)));
			music.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)));
			music.setSigner( cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)));
			music.setUri(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)));
			music.setAlbum(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)));
			music.setImage(getAlbumImage(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))));
			music.setCheck(false);
			results.add(music);
		}

		cursor.close();
		return results;
	}

	/**
	 * 根据歌曲id获取图片
	 * @param albumId 歌曲id
	 * @return 图片
	 */
	private static String getAlbumImage(int albumId) {
		String result = "";
		try (Cursor cursor = BaseApplication.getContext().getContentResolver().query(
				Uri.parse("content://media/external/audio/albums/"
						+ albumId), new String[]{"album_art"}, null,
				null, null)) {

			if(cursor!=null){
				cursor.moveToFirst();
				if(!cursor.isAfterLast())
					result = cursor.getString(0);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	public static String getLocalLrcPath(String title){
		String strLrcPath = "%1$s%2$s.lrc";
		return String.format(strLrcPath,getLrcDir(),title);
	}

	/**
	 * 根据歌词的路径，获取该路径下的所有歌词文件名称、文件路径
	 * @param path 父路径
	 * @return 返回：List<Lyric>，Lyric的name，Lyric的content（path）
	 */
	public static List<Lrc> queryLocalLyric(String path){
		if (path == null) {
			return null;
		}
		File file = new File(path);
		if(!file.exists() || !file.isDirectory())//路径不存在或不是文件夹，返回null
			return null;
		File[] files = file.listFiles();
		if(files == null) return null;

		List<Lrc> list = new ArrayList<>();

		for(File f : files){
			if (f.isDirectory()) {
				continue;
			}
			Lrc lyric = new Lrc();
			lyric.setName(f.getName());
			lyric.setPath(f.getAbsolutePath());
			list.add(lyric);
		}
		return list;
	}


	/**
	 * 通过歌词文件路径，获取该歌词文件的内容
	 * @param path 路径
	 * @return 返回：歌词的内容
	 */
	public static String getStringLyricByPath(String path){
		if (TextUtils.isEmpty(path)) { return null;}
		File file = new File(path);
		if (!file.exists()) {
			return null;
		}
		ILrcBuilder lrcBuilder = new DefaultLrcBuilder();
		return lrcBuilder.getLrcString(path);
	}




	//-------------------- 路径相关 ------------------------------
	/**
	 * 获取内存卡根
	 * @return base path
	 */
	public static String getBaseDir() {
		String dir;
		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_UNMOUNTED)) {
			dir = Environment.getExternalStorageDirectory() + File.separator;
		} else {
			dir = BaseApplication.getContext().getFilesDir() + File.separator;
		}
		return dir;
	}

	/**
	 * 获取应用程序使用的本地目录
	 * @return path
	 */
	private static String getAppLocalDir() {
		String dir;
		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_UNMOUNTED)) {
			dir = Environment.getExternalStorageDirectory() + File.separator
					+ "LedMatrix" + File.separator;
		} else {
			dir = BaseApplication.getContext().getFilesDir() + File.separator + "LedMatrix" + File.separator;
		}
		//return mkdir(dir);
		return dir;
	}

	/**
	 * 获取音乐存放目录
	 * @return music path
	 */
	public static String getMusicDir() {
		String musicDir = getAppLocalDir() + "Music" + File.separator;
		//return mkdir(musicDir);
		return musicDir;
	}

	/**
	 * 获取歌词存放目录
	 *
	 * @return lrc path
	 */
	public static String getLrcDir() {
		String lrcDir = getAppLocalDir() + "Lrc" + File.separator;
		return mkdir(lrcDir);
	}

	/**
	 * 创建文件夹
	 * @param dir 路径
	 * @return 路径
	 */
	private static String mkdir(String dir) {
		File f = new File(dir);
		if (!f.exists()) {
			for (int i = 0; i < 5; i++) {
				if(f.mkdirs()) return dir;
			}
			return null;
		}
		return dir;
	}
}
