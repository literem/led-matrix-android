package com.literem.matrix.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.literem.matrix.R;
import com.literem.matrix.activity.AboutActivity;
import com.literem.matrix.activity.DisplayModuleActivity;
import com.literem.matrix.activity.DisplayScrollActivity;
import com.literem.matrix.activity.DisplayStaticActivity;
import com.literem.matrix.activity.DisplayClockActivity;
import com.literem.matrix.activity.TerminalActivity;
import com.literem.matrix.music.activity.AddLyricActivity;
import com.literem.matrix.music.activity.MusicActivity;

public class MainMoreFragment extends Fragment {

    private Context context;

    public MainMoreFragment(Context context){
        this.context = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_more,container,false);
        initView(view);
        return view;
    }


    private void initView(View view){
        view.findViewById(R.id.tv_mode_lyric).setOnClickListener(onClickListener);
        view.findViewById(R.id.tv_mode_lyric_make).setOnClickListener(onClickListener);
        view.findViewById(R.id.tv_mode_terminal).setOnClickListener(onClickListener);
        view.findViewById(R.id.tv_mode_lyric_make).setOnClickListener(onClickListener);
        view.findViewById(R.id.tv_mode_time_clock).setOnClickListener(onClickListener);
        view.findViewById(R.id.tv_mode_scroll).setOnClickListener(onClickListener);
        view.findViewById(R.id.tv_mode_custom).setOnClickListener(onClickListener);
        view.findViewById(R.id.tv_mode_static).setOnClickListener(onClickListener);
        view.findViewById(R.id.tv_mode_about).setOnClickListener(onClickListener);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.tv_mode_lyric: startActivity(new Intent(context, MusicActivity.class));break;//歌词显示模式
                case R.id.tv_mode_lyric_make: startActivity(new Intent(context, AddLyricActivity.class));break;//歌词显示模式
                case R.id.tv_mode_static: startActivity(new Intent(context, DisplayStaticActivity.class));break;
                case R.id.tv_mode_scroll: startActivity(new Intent(context, DisplayScrollActivity.class));break;
                case R.id.tv_mode_time_clock: startActivity(new Intent(context, DisplayClockActivity.class));break;
                case R.id.tv_mode_terminal: startActivity(new Intent(context, TerminalActivity.class));break;//终端
                case R.id.tv_mode_custom:startActivity(new Intent(context, DisplayModuleActivity.class));break;
                case R.id.tv_mode_about:startActivity(new Intent(context, AboutActivity.class));break;
            }
        }
    };
}
