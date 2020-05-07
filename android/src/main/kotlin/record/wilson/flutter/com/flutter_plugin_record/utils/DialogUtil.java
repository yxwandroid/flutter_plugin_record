package record.wilson.flutter.com.flutter_plugin_record.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.app.AlertDialog;
/**
 * 对话框管理
 * 打开app应用程序信息界面
 */
public class DialogUtil {

    public static void Dialog(final Activity activity, String content) {
         Dialog deleteDialog = new AlertDialog.Builder(activity)
                .setTitle("提示")
                .setMessage("请进入应用信息界面开启录音权限")
                .setPositiveButton("确定",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                startSetting(activity);
                            }
                        })
                .create();
        deleteDialog.setCanceledOnTouchOutside(false);
        deleteDialog.setCancelable(false);
        deleteDialog.show();
    }


    /**
     * 启动app设置应用程序信息界面
     */
    public static void startSetting(Context context) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (Build.VERSION.SDK_INT >= 9) {
            intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            intent.setData(Uri.fromParts("package", context.getPackageName(), null));
        } else if (Build.VERSION.SDK_INT <= 8) {
            intent.setAction(Intent.ACTION_VIEW);
            intent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
            intent.putExtra(context.getPackageName(), context.getPackageName());
        }
        context.startActivity(intent);
    }
}

