package com.gaoxx.yasuo.picaicompress;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.gaoxx.yasuo.picaicompress.utils.PictureUtil;
import com.uzmap.pkg.uzcore.UZWebView;
import com.uzmap.pkg.uzcore.uzmodule.UZModule;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import me.nereo.multi_image_selector.MultiImageSelector;
import me.nereo.multi_image_selector.MultiImageSelectorActivity;

import static android.app.Activity.RESULT_OK;


/**
 * 创建时间: 2018/2/2
 * gxx
 * 注释描述:图片压缩，打开相册处理类
 */

public class PicAiCompressActivity extends UZModule {
    private static final String TAG = PicAiCompressActivity.class.getSimpleName();
    private static final int REQUEST_IMAGE = 2;
    JSONObject ret = new JSONObject();
    JSONObject err = new JSONObject();
    private String STATUS="status";
    private static int SUCCESSSTATUS=1;
    private static int SUCCESSCOMPRESS=2;//压缩中
    private static int FAILSTATUS=0;
    private String PICPATHS="picPaths";
    private String ERRORMESSAGE="errorMessage";


    private UZModuleContext moduleContext;
    //是否显示相机
    private boolean isShowCamera=false;
    //是否多选
    private boolean isSingle = false;
    //默认选择数量
    private int requestNum=9;
    //是否压缩图片
    private boolean isComProgress = false;
    //图片目标大小，默认500k
    private int targetSize = 500;


    public PicAiCompressActivity(UZWebView webView) {
        super(webView);
       //检测是否含有权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN // Permission was added in API Level 16
                && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                new AlertDialog.Builder(mContext)
                        .setTitle(R.string.YaSuo_help)
                        .setCancelable(false)
                        .setMessage(R.string.YaSuo_permissions_help_text)
                        .setPositiveButton(R.string.YaSuo_OK, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    err.put(STATUS,FAILSTATUS);
                                    err.put(ERRORMESSAGE,"没有读权限");
                                    moduleContext.error(err,null,true);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        })
                        .setNegativeButton(R.string.YaSuo_quit, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    err.put(STATUS,FAILSTATUS);
                                    err.put(ERRORMESSAGE,"没有读权限");
                                    moduleContext.error(err,null,true);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        })
                        .create().show();
        }
    }




   /**
    *作者：GaoXiaoXiong
    *创建时间:2018/2/2
    *注释描述:打开相册
    */
    public void jsmethod_openAiBum(final UZModuleContext moduleContext){
        this.moduleContext = moduleContext;
        isShowCamera = moduleContext.optBoolean("showCamera");//是否显示相机
        isSingle = moduleContext.optBoolean("single");//是否单选
        requestNum = moduleContext.optInt("requestNum");//选择数量
        isComProgress = moduleContext.optBoolean("comProgress");//是否压缩图片
        targetSize = moduleContext.optInt("targetSize");//目标大小K
        int maxNum = 9;
        if (isSingle){//单选
            maxNum = 1;
        }else {
            if (requestNum<=0){
                requestNum = maxNum;
            }else {
                maxNum=requestNum;
            }
        }
        MultiImageSelector selector = MultiImageSelector.create();
        selector.showCamera(isShowCamera);
        selector.count(maxNum);
        if (isSingle){//单选
            selector.single();
        }else {
            selector.multi();
        }
        //打开相册
        Intent intent = new Intent(getContext(), MultiImageSelectorActivity.class);
        intent.putExtra("show_camera", isShowCamera);
        intent.putExtra("max_select_count", maxNum);
        intent.putExtra("select_count_mode",isSingle);
        startActivityForResult(intent,REQUEST_IMAGE);
    }

    //线程执行操作
    public class MyThread implements Runnable{
        private List<String> listPic;

        public MyThread(List<String> listPic) {
            this.listPic = listPic;
        }

        @Override
        public void run() {
            picComPress(listPic);
        }
    }


    /**
     *作者：GaoXiaoXiong
     *创建时间:2018/2/2
     *注释描述:图片压缩
     */
    private void picComPress(List<String> listPic){
        if (listPic!=null&&listPic.size()>0){
            StringBuilder sb = new StringBuilder();
            for (String picString : listPic) {
                Bitmap bitmap = PictureUtil.file2Bitmap(picString);//先旋转为正
                File file = PictureUtil.compressImage(bitmap,targetSize);
                sb.append(file.getPath());
                sb.append(",");
            }
            try {
                ret.put(STATUS,SUCCESSSTATUS);
                ret.put(PICPATHS,sb.toString());
                moduleContext.success(ret,true);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG,e.getMessage());
            }
        }else {
            try {
                err.put(STATUS,FAILSTATUS);
                err.put(ERRORMESSAGE,"未知错误");
                moduleContext.error(err,null,true);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG,e.getMessage());
            }

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_IMAGE){
            if(resultCode == RESULT_OK){
               final ArrayList<String>  mSelectPath = data.getStringArrayListExtra(MultiImageSelector.EXTRA_RESULT);
                if (!isComProgress){//不压缩图片
                    StringBuilder sb = new StringBuilder();
                    for(String p: mSelectPath){
                        sb.append(p);
                        sb.append(",");
                    }
                    try {
                        ret.put(STATUS,SUCCESSSTATUS);
                        ret.put(PICPATHS,sb.toString());
                        moduleContext.success(ret,true);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e(TAG,e.getMessage());
                    }
                }else {//压缩图片
                    try {
                        ret.put(STATUS,SUCCESSCOMPRESS);//压缩法通知
                        moduleContext.success(ret,false);
                        new Thread(new MyThread(mSelectPath)).start();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e(TAG,e.getMessage());
                    }
                }
            }
        }
    }

/*

    //dialog初始化
    public BAFWaitingDialog getMainWaitingDialog(){
        if (mainWaitingDialog==null) {
            initMainWaitingDialog();
        }
        return mainWaitingDialog;
    }

    private void initMainWaitingDialog(){
        mainWaitingDialog = BAFWaitingDialog.newInstance();
        mainWaitingDialog.setTitle(null);
        mainWaitingDialog.setCanCancel(false);
        mainWaitingDialog.setWaitingText("");
    }

    protected void showMainWaitingDialog(boolean cancelable) {
        if (mainWaitingDialog==null) {
            initMainWaitingDialog();
        }
        if (mainWaitingDialog.isAdded()) {
            return;
        }
        mainWaitingDialog.setCancelable(cancelable);
        //由于这个方法有可能在使用异步任务或者多线程中回调使用，这时Activity可能已经被onDestroy,这种情况下会引起程序崩溃，所以这里直接捕获异常
        try {
            mainWaitingDialog.show(getContext().getFragmentManager(), TAG);
        } catch (Exception e) {
        }
    }

    //隐藏默认等待框
    public void dismissMainWaitingDialog(){
        if (mainWaitingDialog==null) {
            return;
        }
        try {
            mainWaitingDialog.dismiss();
        }catch (Exception e){
            Log.e(TAG,e.getMessage());
        }

    }
*/

}
