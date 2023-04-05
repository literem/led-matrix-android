package com.literem.matrix.music.dialog;

import android.content.Context;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;

import com.literem.matrix.common.base.BaseDialog;
import com.literem.matrix.common.utils.ToastUtils;
import com.literem.matrix.music.R;
import com.literem.matrix.music.callback.OnMusicDetailsDialogListener;
import com.literem.matrix.music.entity.Music;
import com.literem.matrix.music.fragment.MusicPlayFragment;
import com.literem.matrix.music.utils.SQLiteMusicUtils;

public class DialogMusicUpdateInfo extends BaseDialog {

    private OnMusicDetailsDialogListener listener;
    private Music music;

    private EditText etName,etSigner;

    public DialogMusicUpdateInfo(@NonNull Context context, Music music, OnMusicDetailsDialogListener listener) {
        super(context);
        this.music = music;
        this.listener = listener;
    }

    @Override
    protected int setLayoutId() {
        return R.layout.dialog_update_music;
    }

    @Override
    protected void initView(View view) {
        addOnClickListener(view,R.id.btn_close);
        addOnClickListener(view,R.id.btn_sure);
        etName = view.findViewById(R.id.et_input_name);
        etSigner = view.findViewById(R.id.et_input_signer);
        etName.setText(music.getTitle());
        etSigner.setText(music.getSinger());
        etName.setSelection(music.getTitle().length());
        setCanceledOnTouchOutside(false);
    }


    @Override
    public void onViewClick(View view) {
        if(view.getId() == R.id.btn_sure){
            saveInput();
        }else if(view.getId() == R.id.btn_close){
            dismiss();
        }
    }

    private void saveInput(){
        String name,signer;
        name = etName.getText().toString();
        signer = etSigner.getText().toString();
        if(name.isEmpty() || signer.isEmpty()){
            ToastUtils.Show(context,"请输入完整的信息");
            return;
        }
        SQLiteMusicUtils sql = SQLiteMusicUtils.getInstance(context);
        boolean isSuccess = sql.updateMusic(music.getId(), name, signer);
        ToastUtils.Show(context,isSuccess ? "更改成功" : "更改失败");

        if(isSuccess && listener != null){
            listener.onValueChange(music.getId(), MusicPlayFragment.MusicUpdateInfo);
        }
        dismiss();
    }
}
