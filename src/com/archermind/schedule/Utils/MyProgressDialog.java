
package com.archermind.schedule.Utils;

import android.app.ProgressDialog;
import android.content.Context;

public class MyProgressDialog {

    private static ProgressDialog mpDialog;

    public static ProgressDialog getProgressDialog(Context context) {
        
            mpDialog = new ProgressDialog(context);
            // 实例化
            mpDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            // 设置进度条风格，风格为圆形，旋转的
            mpDialog.setMessage("正在获取数据");
            mpDialog.setIndeterminate(false);
            // 设置ProgressDialog 的进度条是否不明确
            mpDialog.setCancelable(true);
            // 设置ProgressDialog 是否可以按退回按键取消
            
        return mpDialog;
    }
}
