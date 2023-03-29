package com.literam.matrix.music.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.literam.matrix.common.base.BaseActivity;
import com.literam.matrix.common.data.DataUtils;
import com.literam.matrix.common.utils.SoftKeyboardListener;
import com.literam.matrix.common.utils.ToastUtils;
import com.literam.matrix.music.R;
import com.literam.matrix.music.callback.OnValueChangeListener;
import com.literam.matrix.music.dialog.PopupChooseMusic;
import com.literam.matrix.music.entity.Music;

/**
 * 传入的值 有：
 * id           歌曲id
 * music_name   歌名
 * signer       歌手
 * path         歌曲路径
 *
 *
 *
 * 返回的值，通过setResult方法设置Intent对象返回
 * lyric_text   编辑后的歌词文本
 * id           歌曲id
 * music_name   歌名
 * signer       歌手
 * path         歌曲路径
 */
public class AddLyricActivity extends BaseActivity {
    private final int CHOOSE_MUSIC = 0x01;
    private TextView tvMusic, tvSigner;
    private EditText etInput;
    private PopupChooseMusic popupChooseMusic;
    private String musicName,signer,musicPath;
    private int id;
    private boolean isKeyBoardShow = false;

    @Override
    protected int setLayoutId() {
        return R.layout.activity_add_lyric;
    }

    //初始化传入的歌曲
    @Override
    protected void onInit() {
        Intent intent = getIntent();
        if(intent == null) return;
        id = intent.getIntExtra("id",-1);
        musicName = intent.getStringExtra("music_name");
        signer = intent.getStringExtra("signer");
        musicPath = intent.getStringExtra("path");
        if(musicName!=null && signer!=null){
            tvMusic.setText(musicName);
            tvSigner.setText(signer);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onInitView() {
        setToolbarTitle("添加歌词文本");
        addOnClickListener(R.id.btn_sure);
        addOnClickListener(R.id.ll_add_lyric);
        tvMusic = findViewById(R.id.tv_add_lyric_music);
        tvSigner = findViewById(R.id.tv_add_lyric_signer);
        etInput = findViewById(R.id.et_input);
        etInput.setFocusable(false);//第一次初始化EditText，失去焦点不显示软键盘
        etInput.setFocusableInTouchMode(false);
        etInput.setCursorVisible(false);

        //软键盘显示/隐藏监听
        SoftKeyboardListener softKeyBoardListener = new SoftKeyboardListener(this);
        softKeyBoardListener.setListener(new SoftKeyboardListener.OnSoftKeyBoardChangeListener() {
            @Override
            public void keyBoardShow(int height) {
                etInput.setFocusable(true);
                etInput.setFocusableInTouchMode(true);
                etInput.setCursorVisible(true);
                etInput.requestFocus();
                isKeyBoardShow = true;
            }

            @Override
            public void keyBoardHide(int height) {
                etInput.setFocusable(false);
                etInput.setFocusableInTouchMode(false);
                etInput.setCursorVisible(false);
                isKeyBoardShow = false;
            }
        });
        etInput.setOnTouchListener(new View.OnTouchListener() {
            boolean isMove;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN: break;
                    case MotionEvent.ACTION_MOVE: if (!isMove) isMove = true;break;
                    case MotionEvent.ACTION_UP:
                        if(!isKeyBoardShow && !isMove){
                            InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            manager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                        }
                        break;
                }
                return false;
            }
        });

    }

    @Override
    protected void onViewClick(View view) {
        if(view.getId() == R.id.btn_sure){
            addText();
        }else if(view.getId() == R.id.ll_add_lyric){//如果点击LinearLayout 重新选择歌曲
            showChooseMusicDialog();
        }
    }

    private void showChooseMusicDialog(){
        if (popupChooseMusic == null){
            popupChooseMusic = new PopupChooseMusic(this,musicChooseListener);
        }
        popupChooseMusic.showDialog();
    }

    private void addText(){
        if (musicPath == null || musicPath.length() == 0){
            ToastUtils.Show(this,"请选择歌曲");
            return;
        }
        String input = etInput.getText().toString();
        if(input.length() == 0){
            ToastUtils.Show(this,"请输入文本");
            return;
        }
        input = "  \r\n" + input;
        Intent intent = new Intent(this,LyricMakeActivity.class);
        intent.putExtra("lyric_text", input);
        intent.putExtra("music_name",musicName);
        intent.putExtra("signer",signer);
        intent.putExtra("path",musicPath);
        intent.putExtra("id",id);
        startActivityForResult(intent,100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CHOOSE_MUSIC && resultCode == DataUtils.ChangeMusic){
            onInit();
        }

        if (requestCode != 100) return;
        if (resultCode == 1){//制作歌词成功
            setResult(DataUtils.RES_MAKE_LRC);
            super.onBackPressed();
        }else if (resultCode == 0){//制作歌词失败
            finish();
        }
    }

    //选择歌曲回调
    private PopupChooseMusic.OnMusicChooseListener musicChooseListener = new PopupChooseMusic.OnMusicChooseListener() {
        @Override
        public void onMusicChoose(Music music) {
            id = music.getId();
            musicName = music.getTitle();
            signer = music.getSinger();
            musicPath = music.getPath();
            if(musicName != null && signer != null){
                tvMusic.setText(musicName);
                tvSigner.setText(signer);
            }
            popupChooseMusic.dismiss();
        }
    };

    @Override
    protected boolean onExit() {
        if (popupChooseMusic != null){
            if (popupChooseMusic.isShowPopupWindow()) {
                popupChooseMusic.dismiss();
                return false;
            }
        }
        return true;
    }
}
