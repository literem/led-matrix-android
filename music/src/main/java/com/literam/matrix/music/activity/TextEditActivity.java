package com.literam.matrix.music.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.literam.matrix.common.base.BaseActivity;
import com.literam.matrix.common.data.DataUtils;
import com.literam.matrix.common.utils.SoftKeyboardListener;
import com.literam.matrix.common.utils.ToastUtils;
import com.literam.matrix.music.R;


/**
 * 编辑文本
 *
 *
 * 通过getIntent方法获取值，需要传入的值有：
 * title    显示的标题
 * text     要编辑的文本，可为空
 * flag     标志，某些页面需要回传传入的值，可以不需要
 *
 *
 *
 * 返回的值
 * text     编辑后的文本
 * flag     标志，传回传入的flag
 */
public class TextEditActivity extends BaseActivity {

    private EditText etInput;
    private int flag = -1;
    private int id;//歌曲id
    private boolean isKeyBoardShow = false;

    @Override
    protected int setLayoutId() {
        return R.layout.activity_text_edit;
    }

    @Override
    protected void onInit() {
        initIntent();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onInitView() {
        addOnClickListener(R.id.btn_sure);
        etInput = findViewById(R.id.et_input);
        /*etInput.setFocusable(false);//第一次初始化EditText，失去焦点不显示软键盘
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
        });*/
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
        if(view.getId() == R.id.btn_sure) EditFinish();

    }

    private void initIntent(){
        Intent intent = getIntent();
        if(intent != null){
            String title = intent.getStringExtra("title");
            String edit_text = intent.getStringExtra("text");
            flag = intent.getIntExtra("flag",-1);
            id = intent.getIntExtra("id",-1);

            if(title == null || title.isEmpty()){
                setToolbarTitle("文本编辑");
            }else{
                setToolbarTitle(title);
            }

            if(edit_text != null && !edit_text.isEmpty()){
                etInput.setText(edit_text);
            }
        }else{
            setToolbarTitle("文本编辑");
        }
    }

    private void EditFinish(){
        String input = etInput.getText().toString();
        if(input.length() == 0){
            ToastUtils.Show(this,"请输入文本");
            return;
        }
        Intent intent = new Intent();
        intent.putExtra("flag",flag);
        intent.putExtra("text",input);
        intent.putExtra("id",id);
        setResult(DataUtils.EditText,intent);
        super.onBackPressed();
    }

}
