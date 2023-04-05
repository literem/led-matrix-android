package com.literem.matrix.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.transition.Slide;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.literem.matrix.R;
import com.literem.matrix.callback.OnFontCreateCallback;
import com.literem.matrix.common.base.BaseActivity;
import com.literem.matrix.common.data.DataUtils;
import com.literem.matrix.common.dialog.DialogList;
import com.literem.matrix.common.dialog.DialogPrompt;
import com.literem.matrix.common.utils.DisplayUtil;
import com.literem.matrix.common.utils.ShowLoadingUtils;
import com.literem.matrix.common.utils.ToastUtils;
import com.literem.matrix.common.widget.DrawableTextView;
import com.literem.matrix.common.widget.MultipleCircleView;
import com.literem.matrix.dialog.PopupCharFont;
import com.literem.matrix.dialog.PopupGifFont;
import com.literem.matrix.dialog.PopupImageFont;
import com.literem.matrix.utils.PreViewMultipleCircleUtils;
import com.literem.matrix.utils.PreviewCircleButtonUtils;

import java.util.ArrayList;
import java.util.List;

import static com.literem.matrix.common.data.DataUtils.ModuleSize;

public class DisplayFrameActivity extends BaseActivity {
    private final int AnalyzerGIF = 1;
    private LinearLayout llRoot;//添加预览点阵的根
    private FrameLayout flPen,flRubber;//笔和橡皮的切换
    private MultipleCircleView multipleCircleView;//点阵编辑控件
    private DrawableTextView tvSelectModule;
    private TextView tvFrameCount,tvMaxTips;
    private PreviewCircleButtonUtils previewButtonState;
    private int selectPreviewIndex = -1;//当前选中的预览索引
    private int previewCount = -1;//全部预览索引
    private int selectModuleIndex = 0;//选择哪个模块作为动态帧，从1开始算
    private String strFrameCount = "共(%1$d)帧，应用于";
    private String strTips = "提示：最多保留(%1$d)帧\n超过的部分将会丢弃";
    private String[] moduleItems;
    private List<boolean[][]> listFrame;
    private List<PreViewMultipleCircleUtils> listPreView;
    private Handler handler;
    private ShowLoadingUtils showLoading;
    private PopupCharFont popupCharFont;//各种弹出框
    private PopupImageFont popupImageFont;
    private PopupGifFont popupGifFont;
    private LinearLayout.LayoutParams layoutParams;

    @Override
    public boolean isShowBackKey() {
        return false;
    }

    @Override
    protected int setLayoutId() {
        return R.layout.activity_display_frame;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Slide slide = new Slide();
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        getWindow().setExitTransition(slide);
        getWindow().setEnterTransition(slide);
        getWindow().setReenterTransition(slide);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onInitView() {
        multipleCircleView = findViewById(R.id.multiple_circle);
        flPen = findAndAddOnClickListener(R.id.fl_pen);
        flRubber = findAndAddOnClickListener(R.id.fl_rubber);
        tvSelectModule = findAndAddOnClickListener(R.id.dtv_select_module);
        llRoot = findViewById(R.id.ll_root);
        tvFrameCount = findViewById(R.id.tv_frame_count);
        tvMaxTips = findViewById(R.id.tv_tip);
        addOnClickListener(R.id.fl_char);
        addOnClickListener(R.id.fl_image);
        addOnClickListener(R.id.fl_clean);
        addOnClickListener(R.id.fl_delete);
        addOnClickListener(R.id.tv_gif);
        addOnClickListener(R.id.tv_clear_all);
        addOnClickListener(R.id.btn_finish);
    }

    @Override
    protected void onInit() {
        setToolbarTitle("帧管理");
        strTips = "提示：最多保留(%1$d)帧\n超过的部分将会丢弃";
        moduleItems = new String[ModuleSize+1];
        moduleItems[0] = "无模块";
        for (int i = 1; i <= ModuleSize; i++) {
            moduleItems[i] = "模块" + i;
        }
        initFrameAndPreview();
    }


    //region 各种事件
    //--------------------- 点击事件 ------------------------
    @Override
    protected void onViewClick(View view) {
        switch (view.getId()){
            case R.id.fl_pen:   switchBrushStatus(true);break;
            case R.id.fl_rubber:switchBrushStatus(false);break;
            case R.id.fl_char:  showCharDialog(view);break;
            case R.id.fl_image: showImageDialog(view);break;
            case R.id.fl_clean: clearFrame();break;
            case R.id.tv_clear_all:showClearAllFrameDialog();break;
            case R.id.fl_delete:showDeleteDialog();break;
            case R.id.tv_gif:showGifDescDialog(view);break;
            case R.id.btn_finish:finishFrame();break;
            case R.id.dtv_select_module:showSelectModuleDialog();break;
        }
    }

    //--------------------- 监听字模生成 ------------------------
    private OnFontCreateCallback onFontCreateListener = new OnFontCreateCallback() {
        @Override
        public void onCreateFont(int length, int module, PopupWindow popupWindow) {
            if (popupWindow != null)popupWindow.dismiss();
            if(length == 1){
                listPreView.get(module).setData(listFrame.get(module));
                multipleCircleView.setData(listFrame.get(module));
                ToastUtils.Show(DisplayFrameActivity.this,"成功添加1个字模数据");
            }else{
                for (int i = 0; i < length; i++) {
                    final boolean[][] b = listFrame.get(i);
                    listPreView.get(i).setData(b);
                    multipleCircleView.setData(b);
                }
                ToastUtils.Show(DisplayFrameActivity.this,"成功添加"+length+"+个字模数据");
            }
        }
    };


    //------------------ 点阵预览模块的点击事件 ----------------
    private View.OnClickListener onPreViewClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int tag = (int)view.getTag();
            if(tag == 1000){
                addFrame();
                return;
            }
            if(tag < 0 || tag == selectPreviewIndex) return;
            if(selectPreviewIndex != -1){//存在上一个，先取消上一个的
                listPreView.get(selectPreviewIndex).setButtonUncheck();
            }
            selectPreviewIndex = tag;
            listPreView.get(tag).setButtonCheck();
            multipleCircleView.setData(listPreView.get(tag).getData());
        }
    };
    //endregion

    //region 各种dialog
    private void showCharDialog(View view){
        if(selectPreviewIndex == -1){
            ToastUtils.Show(this,"请选择帧");
            return;
        }
        if(popupCharFont == null){
            popupCharFont = new PopupCharFont(this,listFrame,onFontCreateListener);
        }
        popupCharFont.setMaxCharSize(listFrame.size());
        popupCharFont.showPopupWindow(view, selectPreviewIndex);
    }

    private void showImageDialog(View view){
        if(selectPreviewIndex == -1){
            ToastUtils.Show(this,"请选择帧");
            return;
        }
        if(popupImageFont == null){
            popupImageFont = new PopupImageFont(this,onFontCreateListener);
        }
        popupImageFont.showPopupWindow(view,listFrame.get(selectPreviewIndex), selectPreviewIndex);
    }

    private void showDeleteDialog(){
        if(selectPreviewIndex == -1){
            ToastUtils.Show(this,"请选择帧");
            return;
        }
        DialogPrompt.builder(this)
                .setContent("确定要删除当前帧吗？")
                .setOnSureListener(null, new DialogPrompt.OnDialogPromptListener() {
                    @Override
                    public void onButtonClick(DialogPrompt dialog) {
                        dialog.dismiss();
                        removeFrame();
                    }
                }).showDialog();
    }

    private void showGifDescDialog(View v){
        if(popupGifFont == null) popupGifFont = new PopupGifFont(this);
        popupGifFont.show(v);
    }

    private void showSelectModuleDialog(){
        DialogList.Builder(this)
                .setTitle("选择要播放动画的模块")
                .setItems(moduleItems)
                .setItemListener(new DialogList.OnDialogListListener() {
                    @Override
                    public void dialogListItemClick(DialogList dialog, String itemName, int itemId) {
                        dialog.dismiss();
                        selectModuleIndex = itemId;
                        tvSelectModule.setText(itemName);
                    }
                }).show();
    }

    private void showClearAllFrameDialog(){
        DialogPrompt.builder(this)
                .setContent("确定要清空所有帧吗？")
                .setOnSureListener(null, new DialogPrompt.OnDialogPromptListener() {
                    @Override
                    public void onButtonClick(DialogPrompt dialog) {
                        listFrame.clear();
                        listPreView.clear();
                        multipleCircleView.setData(null);
                        tvMaxTips.setVisibility(View.INVISIBLE);
                        llRoot.removeAllViews();
                        @SuppressLint("InflateParams")
                        View view = LayoutInflater.from(DisplayFrameActivity.this).inflate(R.layout.item_preview_multiple_circle_add, null);
                        view.setTag(1000);
                        view.setOnClickListener(onPreViewClick);
                        llRoot.addView(view,layoutParams);
                        previewCount = selectPreviewIndex = -1;
                        dialog.dismiss();
                    }
                }).showDialog();
    }

    //endregion

    //在笔和橡皮之间切换
    private void switchBrushStatus(boolean isPen){
        multipleCircleView.setPen(isPen);
        if (isPen){
            flPen.setBackgroundResource(R.drawable.bg_circle_50_blue);
            flRubber.setBackgroundResource(R.drawable.bg_circle_50_grey);
        }else{
            flPen.setBackgroundResource(R.drawable.bg_circle_50_grey);
            flRubber.setBackgroundResource(R.drawable.bg_circle_50_blue);
        }
    }

    //region 各种Frame方法
    private void finishFrame(){
        //如果超过最大帧，则从最大帧开始到list.size位置的删掉
        if(listFrame.size() > DataUtils.MaxFrame){
            listFrame.subList(DataUtils.MaxFrame, listFrame.size()).clear();
        }
        onBackPressed();
    }

    private void clearFrame(){
        if(selectPreviewIndex == -1){
            ToastUtils.Show(this,"请选择帧");
            return;
        }
        boolean[][] b = listFrame.get(selectPreviewIndex);
        for (int j = 0; j < 16; j++) {
            for (int k = 0; k < 16; k++) {
                b[j][k] = false;
            }
        }
        multipleCircleView.setData(b);
        listPreView.get(selectPreviewIndex).setData(b);
    }

    private void initFrameAndPreview(){
        if(DataUtils.listFrame == null){
            DataUtils.listFrame = new ArrayList<>();
        }
        this.listFrame = DataUtils.listFrame;
        listPreView = new ArrayList<>();
        previewButtonState = new PreviewCircleButtonUtils(this);
        previewButtonState.setTextFormat("第%1$d帧");
        layoutParams = new LinearLayout.LayoutParams(DisplayUtil.dip2px(this,70),DisplayUtil.dip2px(this,95));

        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(this).inflate(R.layout.item_preview_multiple_circle_add, null);
        view.setTag(1000);
        view.setOnClickListener(onPreViewClick);
        llRoot.addView(view,layoutParams);

        for (int i = 0; i < listFrame.size(); i++) {
            previewCount++;
            PreViewMultipleCircleUtils utils = new PreViewMultipleCircleUtils(this, previewCount, previewButtonState);
            llRoot.addView(utils.getView(),llRoot.getChildCount()-1,layoutParams);
            utils.setOnClickListener(onPreViewClick);
            utils.setData(listFrame.get(previewCount));
            listPreView.add(utils);
            tvFrameCount.setText(String.format(strFrameCount, previewCount + 1));
            utils.invalidate();
        }
        checkMaxFrameAndTip();
    }

    private void addFrame(){
        boolean[][] b = new boolean[16][16];
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                b[i][j] = false;
            }
        }
        listFrame.add(b);
        previewCount++;
        PreViewMultipleCircleUtils utils = new PreViewMultipleCircleUtils(this, previewCount, previewButtonState);
        llRoot.addView(utils.getView(),llRoot.getChildCount()-1,layoutParams);
        utils.setOnClickListener(onPreViewClick);
        utils.setData(listFrame.get(previewCount));
        listPreView.add(utils);
        tvFrameCount.setText(String.format(strFrameCount, previewCount + 1));
        checkMaxFrameAndTip();
    }

    public void addFrame(boolean[][] b){
        listFrame.add(b);
        previewCount++;
        PreViewMultipleCircleUtils utils = new PreViewMultipleCircleUtils(this, previewCount, previewButtonState);
        llRoot.addView(utils.getView(),llRoot.getChildCount()-1,layoutParams);
        utils.setOnClickListener(onPreViewClick);
        utils.setData(listFrame.get(previewCount));
        listPreView.add(utils);
        tvFrameCount.setText(String.format(strFrameCount, previewCount + 1));
        utils.invalidate();
        checkMaxFrameAndTip();
    }

    private void removeFrame(){
        if(selectPreviewIndex < 0) return;
        listFrame.remove(selectPreviewIndex);
        listPreView.remove(selectPreviewIndex);
        llRoot.removeViewAt(selectPreviewIndex);
        tvFrameCount.setText(String.format(strFrameCount, selectPreviewIndex));
        if(selectPreviewIndex != previewCount){//如果不是最后一个，则要更新前面的tag
            for (int i = selectPreviewIndex; i < previewCount; i++) {
                listPreView.get(i).setTag(i);
            }
        }
        selectPreviewIndex--;
        previewCount--;
        if(selectPreviewIndex >= 0){//如果前面还有帧，则选中上一帧
            listPreView.get(selectPreviewIndex).setButtonCheck();
            multipleCircleView.setData(listPreView.get(selectPreviewIndex).getData());
        }else{
            selectPreviewIndex = previewCount = -1;
        }
        checkMaxFrameAndTip();
    }

    private void checkMaxFrameAndTip(){
        if(previewCount >= DataUtils.MaxFrame){//大于最大帧数，并且为隐藏状态，则显示
            if(tvMaxTips.getVisibility() == View.INVISIBLE){
                tvMaxTips.setText(String.format(strTips,DataUtils.MaxFrame));
                tvMaxTips.setVisibility(View.VISIBLE);
            }
        }else{
            if(tvMaxTips.getVisibility() == View.VISIBLE) tvMaxTips.setVisibility(View.INVISIBLE);
        }
    }

    //endregion

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 2 && data != null && popupGifFont!=null){//选择图片后回调
            if (handler == null) handler = new Handler(getMainLooper(),resultHandleCallback);
            if(showLoading == null) showLoading = new ShowLoadingUtils(this);
            Message msg = Message.obtain();
            msg.obj = data.getData();
            msg.what = AnalyzerGIF;
            handler.sendMessageDelayed(msg,1000);
            showLoading.show("正在解析GIF···");
        }
        /*else if(requestCode == 0xff && resultCode == 0xff){//背景颜色过滤
            if(data == null) return;
            int[] rounds = data.getIntArrayExtra("round");
            if (rounds == null) return;
            //test(rounds);
        }*/
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        //如果listFrame帧列表超过两帧才是selectModuleIndex，否则是-1
        intent.putExtra("index",listFrame.size() > 2 ? selectModuleIndex : 0);
        setResult(DataUtils.EditFrame,intent);
        super.onBackPressed();
    }

    private Handler.Callback resultHandleCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            if(msg.what == AnalyzerGIF){
                popupGifFont.GIFDesc((Uri) msg.obj);
                showLoading.dismiss();
                ToastUtils.Show(DisplayFrameActivity.this,"GIF解析完成！");
            }
            return true;
        }
    };
}
