package com.literam.matrix.activity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.SystemClock;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import androidx.annotation.Nullable;

import com.literam.matrix.R;
import com.literam.matrix.callback.OnFontCreateCallback;
import com.literam.matrix.common.base.BaseBTActivity;
import com.literam.matrix.common.data.DataUtils;
import com.literam.matrix.common.dialog.DialogList;
import com.literam.matrix.common.dialog.DialogPrompt;
import com.literam.matrix.common.font.CommandUtils;
import com.literam.matrix.common.font.HexUtils;
import com.literam.matrix.common.utils.ToastUtils;
import com.literam.matrix.common.widget.DrawableTextView;
import com.literam.matrix.common.widget.MultipleCircleView;
import com.literam.matrix.dialog.DialogNumberInput;
import com.literam.matrix.dialog.PopupCharFont;
import com.literam.matrix.dialog.PopupImageFont;
import com.literam.matrix.utils.PreViewMultipleCircleUtils;
import com.literam.matrix.utils.PreviewCircleButtonUtils;

import java.util.ArrayList;
import java.util.List;

import static com.literam.matrix.common.data.DataUtils.ModuleSize;

public class DisplayModuleActivity extends BaseBTActivity {

    private static final int PART_LEN = 20;
    private DisplayModuleActivity context;

    //各种视图View
    private LinearLayout llRoot;//添加预览点阵的根
    private FrameLayout flPen,flRubber;
    private DrawableTextView tvFrameMore;
    private MultipleCircleView multipleCircleView;//点阵编辑控件
    private LinearLayout llToolbar;

    //各种弹出框
    private PopupCharFont popupCharFont;
    private PopupImageFont popupImageFont;

    //各种数据
    private String[] moduleItems;
    private int currentFrameModule = -1;//当前帧
    private int currentPreViewPos = -1;//当前模块
    private boolean isPlayFrame = false;
    private int playFrameIndex = 0;
    private List<boolean[][]> listData;
    private List<PreViewMultipleCircleUtils> listPreView;
    private PreviewCircleButtonUtils buttonStateUtils;
    private String[] itemName = {"发送开始帧数据","发送停止帧数据","设置帧切换速度","播放预览帧","停止预览帧","删除该帧"};
    private int currentSpeed = 200;

    @Override
    protected int setLayoutId() {
        return R.layout.activity_display_module;
    }

    @Override
    protected void onInit() {
        context = this;
        listData = new ArrayList<>(ModuleSize);
        listPreView = new ArrayList<>(ModuleSize);
        buttonStateUtils = new PreviewCircleButtonUtils(this);
        createCirclePreView();
    }

    @Override
    protected void onInitView() {
        setTitle("模块显示");
        multipleCircleView = findViewById(R.id.multiple_circle);
        multipleCircleView.setOnDataChangeListener(new MultipleCircleView.OnXYPositionListener() {
            @Override
            public void onPositionChange(int x, int y) {
                if(currentPreViewPos != -1 && currentPreViewPos < ModuleSize){
                    listPreView.get(currentPreViewPos).invalidate();
                }
            }
        });
        llRoot = findViewById(R.id.ll_root);
        llToolbar = findViewById(R.id.ll_toolbar);
        flPen = findAndAddOnClickListener(R.id.fl_pen);
        flRubber = findAndAddOnClickListener(R.id.fl_rubber);
        tvFrameMore = findAndAddOnClickListener(R.id.dtv_frame_more);
        addOnClickListener(R.id.iv_more);
        addOnClickListener(R.id.fl_clean);
        addOnClickListener(R.id.fl_sync);
        addOnClickListener(R.id.fl_char);
        addOnClickListener(R.id.fl_image);
        addOnClickListener(R.id.fl_copy);
        addOnClickListener(R.id.tv_frame_manage);
    }

    //region 监听事件
    //--------------------- 点击事件 ------------------------
    @Override
    protected void onViewClick(View view) {
        switch (view.getId()){
            case R.id.fl_char:  showCharDialog(view);break;//文字转字模
            case R.id.fl_image: showImageDialog(view);break;//图片转字模
            //case R.id.iv_more:  openModuleMoreDialog(view);break;//打开更多操作
            case R.id.fl_copy:  showCopyDialog();break;//复制数据
            case R.id.fl_clean: showCleanFontDialog();break;//清空当前模块
            case R.id.fl_pen:   switchBrushStatus(true);break;
            case R.id.fl_rubber:switchBrushStatus(false);break;
            case R.id.fl_sync:  sendSyncCurrentModule();break;
            case R.id.dtv_frame_more: showFrameMoreDialog();break;//打开帧的更多操作
            case R.id.tv_sync_all: sendSyncAllModule();break;//同步全部模块
            case R.id.tv_frame_manage://帧管理
                Intent intent = new Intent(context, DisplayFrameActivity.class);
                startActivityForResult(intent, DataUtils.EditFrame, ActivityOptions.makeSceneTransitionAnimation(context).toBundle());
                break;
        }
    }

    //--------------------- 监听字模生成 ------------------------
    private OnFontCreateCallback onFontCreateListener = new OnFontCreateCallback() {
        @Override
        public void onCreateFont(int length, int module, PopupWindow popupWindow) {
            if(length == 1){
                if (currentFrameModule == module){
                    ToastUtils.Show(context,"当前模块已使用动态帧，不能添加");
                    return;
                }
                listPreView.get(module).setData(listData.get(module));
                multipleCircleView.setData(listData.get(module));
                ToastUtils.Show(context,"成功添加1个字模数据");
            }else{
                for (int i = 0; i < length; i++) {
                    if(currentFrameModule == i) continue;
                    final boolean[][] b = listData.get(i);
                    listPreView.get(i).setData(b);
                    multipleCircleView.setData(b);
                }
                ToastUtils.Show(context,"成功添加"+length+"+个字模数据");
            }
            if (popupWindow != null) popupWindow.dismiss();
        }
    };

    //------------------ 点阵预览模块的点击事件 ----------------
    private View.OnClickListener onPreViewClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int tag = (int)view.getTag();
            if(tag < 0 || tag == currentPreViewPos) return;

            if (tag == currentFrameModule) {//如果点击的是当前帧，则显示第1帧到multipleCircleView
                setFrameState(true,tag);
                return;
            }

            tvFrameMore.setVisibility(View.GONE);
            llToolbar.setVisibility(View.VISIBLE);

            isPlayFrame = false;
            multipleCircleView.setEditState(true);
            //存在上一个，先取消上一个的
            if(currentPreViewPos != -1 && currentPreViewPos != currentFrameModule)
                listPreView.get(currentPreViewPos).setButtonUncheck();

            currentPreViewPos = tag;
            listPreView.get(tag).setButtonCheck();
            multipleCircleView.setData(listPreView.get(tag).getData());
        }
    };


    //------------------ 模块更多菜单的点击回调事件 ----------------
    /*private PopupMenu.OnMenuItemClickListener onMenuItemClickListener = new PopupMenu.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            if (item.getItemId() == R.id.menu_show_hex) {//显示当前模块的全部十六进制
                if(currentPreViewPos == -1){
                    ToastUtils.Show(context,"请选择模块");
                    return true;
                }
                String text = HexUtils.getHexByBoolean(listData.get(currentPreViewPos),true);
                DialogWarn.builder(context).showDialog(text);
            }else if(item.getItemId() == R.id.menu_show_hex_2){//显示当前模块的左8和右8部分的十六进制
                if(currentPreViewPos == -1){
                    ToastUtils.Show(context,"请选择模块");
                    return true;
                }
                String text = HexUtils.get8BitHexByBoolean(listData.get(currentPreViewPos),true);
                DialogWarn.builder(context).showDialog(text);
            }else if(item.getItemId() == R.id.menu_save){
                createHex();
                ToastUtils.Show(context,"已保存到剪切板");
            }
            return true;
        }
    };*/


    private DialogList.OnDialogListListener onDialogListListener = new DialogList.OnDialogListListener() {
        @Override
        public void dialogListItemClick(DialogList dialog, String itemName, int itemId) {
            switch (itemId){
                case 0: startFrameData();break;//发送开始帧数据
                case 1: stopFrameData();break;//发送停止帧数据
                case 2: showSetSpeedDialog();break;
                case 3: startPlayFrame();break;
                case 4: isPlayFrame = false;break;
                case 5: setFrameState(false,currentFrameModule);break;//清除动态帧当前帧（如果当前帧存在的话）
            }
            dialog.dismiss();
        }
    };
    /* ********************************************* 监听事件 ************************************/
    //endregion


    //region 各种dialog
    private void showCharDialog(View view){
        if(currentPreViewPos == -1){
            ToastUtils.Show(context,"请选择模块");
            return;
        }
        if(popupCharFont == null){
            popupCharFont = new PopupCharFont(context,listData,onFontCreateListener);
            popupCharFont.setMaxCharSize(ModuleSize);
        }
        popupCharFont.showPopupWindow(view, currentPreViewPos);
    }

    private void showImageDialog(View view){
        if(currentPreViewPos == -1){
            ToastUtils.Show(context,"请选择模块");
            return;
        }
        if(popupImageFont == null){
            popupImageFont = new PopupImageFont(this,onFontCreateListener);
        }
        popupImageFont.showPopupWindow(view,listData.get(currentPreViewPos),currentPreViewPos);
    }

    private void showSetSpeedDialog(){
        DialogNumberInput.Builder(this)
                .setTitle("设置帧切换速度","范围(50-10000)")
                .setEdgeValue(50,5000)
                .setStep(20)
                .setInitialValue(currentSpeed)
                .setListener(null, new DialogNumberInput.OnSetTimeListener() {
                    @Override
                    public void onSetValue(DialogNumberInput dialog, int value) {
                        currentSpeed = value;
                        dialog.dismiss();
                        handler.sendOrAddQueue(CommandUtils.getInstance().setDisplayFrameSpeed(value));
                    }
                }).showDialog();
    }

    //复制模块数据
    private void showCopyDialog(){
        if(currentPreViewPos == -1){
            ToastUtils.Show(context,"请选择模块");
            return;
        }
        if(moduleItems == null){
            moduleItems = new String[ModuleSize];
            for (int i = 0; i < ModuleSize; i++) {
                moduleItems[i] = "模块" + (i+1);
            }
        }
        DialogList.Builder(context)
                .setTitle("选择要复制的模块")
                .setItems(moduleItems)
                .setItemListener(new DialogList.OnDialogListListener() {
                    @Override
                    public void dialogListItemClick(DialogList dialog, String itemName, int itemId) {
                        if(itemId == currentPreViewPos){
                            ToastUtils.Show(context,"不能复制到当前模块！");
                        }else{
                            copyData(itemId);
                            dialog.dismiss();
                            ToastUtils.Show(context,"复制成功");
                        }
                    }
                }).show();
    }

    //显示清空点阵模块对话框
    private void showCleanFontDialog(){
        if(currentPreViewPos == -1){
            ToastUtils.Show(context,"请选择模块");
            return;
        }
        DialogPrompt.builder(context)
                .setContent("确定要清空当前模块的数据吗？")
                .setOnSureListener(null, new DialogPrompt.OnDialogPromptListener() {
                    @Override
                    public void onButtonClick(DialogPrompt dialog) {
                        dialog.dismiss();
                        boolean[][] b = listData.get(currentPreViewPos);
                        for (int j = 0; j < 16; j++) {
                            for (int k = 0; k < 16; k++) {
                                b[j][k] = false;
                            }
                        }
                        multipleCircleView.setData(b);
                        listPreView.get(currentPreViewPos).setData(b);
                    }
                }).showDialog();
    }


    //打开更多菜单
    /*private void openModuleMoreDialog(View view){
        PopupMenu pop = new PopupMenu(context,view);
        pop.getMenuInflater().inflate(R.menu.menu_self_make_activity, pop.getMenu());
        pop.show();
    }*/

    private void showFrameMoreDialog(){
        DialogList.Builder(context)
                .setTitle("更多操作")
                .setItems(itemName)
                .setItemListener(onDialogListListener)
                .show();
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

    private void copyData(int module){
        if(currentPreViewPos == -1) return;
        if(module == currentPreViewPos) return;
        boolean[][] src = listData.get(currentPreViewPos);
        boolean[][] des = listData.get(module);
        System.arraycopy(src,0,des,0,16);
        listPreView.get(module).invalidate();//复制完成后刷新目标模块数据
    }

    private void sendSyncCurrentModule(){
        if(currentPreViewPos == -1){
            ToastUtils.Show(context,"请选择模块");
            return;
        }
        byte[] bytes = HexUtils.getByteByBoolean(listData.get(currentPreViewPos), true);
        byte[] send = CommandUtils.getInstance().setDisplayCustom(currentPreViewPos, bytes);
        handler.sendOrAddQueue(send);
    }

    private void sendSyncAllModule() {
        ToastUtils.Show(this,"暂无此功能！");
    }

    private void startFrameData(){
        int size = DataUtils.listFrame.size();
        if (size == 0 || currentFrameModule == -1){
            ToastUtils.Show(context,"没有帧可以发送");
            return;
        }
        if(currentPreViewPos == -1){
            ToastUtils.Show(context,"请选择模块");
            return;
        }
        if(size == 1){
            ToastUtils.Show(context,"需要两帧以上才能发送！");
            return;
        }
        showLoadingDelayDismiss("发送帧数据中···");
        int page = size / PART_LEN;
        int remind  = size - (page * PART_LEN);
        handler.queue.clearQueue();
        appendToQueue(CommandUtils.getInstance().setDisplayFrameStart());
        for (int i = 0; i < page; i++) {
            byte[] fontData = new byte[32*PART_LEN];
            for (int j = 0; j < PART_LEN; j++) {
                byte[] bytes = HexUtils.getByteByBoolean(DataUtils.listFrame.get(i*PART_LEN+j), true);
                System.arraycopy(bytes,0,fontData,j*32,32);
            }
            appendToQueue(CommandUtils.getInstance().setDisplayFramePart(fontData));
        }

        if (remind > 0){
            byte[] fontData = new byte[32*remind];
            for(int i=0;i<remind;i++) {
                byte[] bytes = HexUtils.getByteByBoolean(DataUtils.listFrame.get(i), true);
                System.arraycopy(bytes,0,fontData,i*32,32);
            }
            appendToQueue(CommandUtils.getInstance().setDisplayFramePart(fontData));
        }
        appendToQueue(CommandUtils.getInstance().setDisplayFrameEnd(currentPreViewPos));
        sendQueue(100);
    }

    private void stopFrameData(){
        byte[] d = CommandUtils.getInstance().setDisplayFrameSpeed(0);
        handler.sendOrAddQueue(d);
    }

    private void startPlayFrame(){
        if(isPlayFrame) return;
        isPlayFrame = true;
        playFrameIndex = 0;
        ToastUtils.Show(context,"开始播放帧数据");
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isPlayFrame){
                    SystemClock.sleep(200);
                    if(isPlayFrame){
                        handler.sendEmptyMessage(10001);
                    }
                }
                handler.sendEmptyMessage(10000);
            }
        }).start();
    }

    /**
     * 生成预览点阵视图
     */
    private void createCirclePreView(){
        for (int k = 0; k < ModuleSize; k++) {
            boolean[][] b = new boolean[16][16];
            for (int i = 0; i < 16; i++) {
                for (int j = 0; j < 16; j++) {
                    b[i][j] = false;
                }
            }
            listData.add(b);
        }
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
        for (int i = 0; i < ModuleSize; i++) {
            PreViewMultipleCircleUtils utils = new PreViewMultipleCircleUtils(context,i,buttonStateUtils);
            llRoot.addView(utils.getView(),layoutParams);
            utils.setOnClickListener(onPreViewClick);
            utils.setData(listData.get(i));
            listPreView.add(utils);
        }
    }

    /**
     * 选中某个模块作为当前动态帧
     * @param state true：先取消上一个，再设置当前的，false：只取消当前的
     */
    private void setFrameState(boolean state,int frameIndex){
        //这里仅仅作为取消上一帧
        if(!state){
            if(frameIndex == -1) return;//如果上一帧未选中，则不会取消
            tvFrameMore.setVisibility(View.GONE);
            llToolbar.setVisibility(View.VISIBLE);
            listPreView.get(frameIndex).setButtonUncheck();
            listPreView.get(frameIndex).setData(listData.get(frameIndex));
            multipleCircleView.setData(null);
            currentFrameModule = currentPreViewPos = -1;
            ToastUtils.Show(context,"已清除动态帧");
            return;
        }

        //切换到当前帧（注意：此时当前帧已经显示了，这里只显示编辑区内容和按钮）
        if(currentFrameModule == frameIndex){
            multipleCircleView.setData(DataUtils.listFrame.get(0));//当前帧是编辑区的内容，则禁用编辑区编辑功能
            multipleCircleView.setEditState(false);
            tvFrameMore.setVisibility(View.VISIBLE);
            llToolbar.setVisibility(View.GONE);
            if(currentPreViewPos != -1) listPreView.get(currentPreViewPos).setButtonUncheck();
            currentPreViewPos = frameIndex;
            return;
        }

        //选择下一帧，这里会强制把第一帧显示到编辑区上
        if(currentFrameModule != -1){//如果选中上一个，先取消上一个的
            listPreView.get(currentFrameModule).setButtonUncheck();
            listPreView.get(currentFrameModule).setData(listData.get(currentFrameModule));
        }
        currentFrameModule = frameIndex;

        tvFrameMore.setVisibility(View.VISIBLE);
        llToolbar.setVisibility(View.GONE);
        listPreView.get(frameIndex).setButtonDisable();//设置按钮为红色
        listPreView.get(frameIndex).setData(DataUtils.listFrame.get(frameIndex));//显示第一帧预览
        multipleCircleView.setData(DataUtils.listFrame.get(0));//当前帧是编辑区的内容，则禁用编辑区编辑功能
        multipleCircleView.setEditState(false);
        if(currentPreViewPos != -1){//存在上一个，先取消上一个的
            listPreView.get(currentPreViewPos).setButtonUncheck();
        }
        currentPreViewPos = frameIndex;
    }

    public void onPlayFrame() {
        if(playFrameIndex == DataUtils.listFrame.size()) playFrameIndex = 0;
        multipleCircleView.setData(DataUtils.listFrame.get(playFrameIndex));
        playFrameIndex++;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 2 && data != null){//选择图片，从相册返回的数据
            if (popupImageFont != null) popupImageFont.addImage(data.getData());
        }else if(resultCode == DataUtils.EditFrame && data != null){
            int moduleIndex = data.getIntExtra("index",0);
            if(moduleIndex == 0 || DataUtils.listFrame == null || DataUtils.listFrame.size() == 0){
                return;
            }
            setFrameState(true,moduleIndex - 1);
        }
    }

    @Override
    protected boolean onExit() {
        if(popupCharFont != null && popupCharFont.isShowPopupWindow()){
            popupCharFont.dismiss();
            return false;
        }else if(popupImageFont != null && popupImageFont.isShowPopupWindow()){
            popupImageFont.dismiss();
            return false;
        }
        return true;
    }

    @Override
    protected void handlerData(int what, Object obj) {
        super.handlerData(what, obj);
        if (what == 10001) onPlayFrame();
        else if(what == 10000) ToastUtils.Show(this,"已停止播放帧数据");
    }

    @Override
    protected void handleSendSuccess(Object msg, int result) {
        super.handleSendSuccess(msg, result);
        System.out.println("发送成功！！！");
    }
}
