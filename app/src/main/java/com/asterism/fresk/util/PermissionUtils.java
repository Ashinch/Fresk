package com.asterism.fresk.util;

import android.app.Activity;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;

/**
 * 权限工具类
 *
 * @author Ashinch
 * @email Glaxyinfinite@outlook.com
 * @date on 2019-07-06 17:19
 */
public class PermissionUtils {
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            //从SDCard读取数据权限
            "android.permission.READ_EXTERNAL_STORAGE",
            //往SDCard写入数据权限
            "android.permission.WRITE_EXTERNAL_STORAGE",
            // 在SDCard中创建与删除文件权限
            "android.permission.MOUNT_UNMOUNT_FILESYSTEMS"};

    /**
     * 权限申请，判断是否有对SDCard操作的的权限,若没有将会弹出消息进行申请
     *
     * @param activity 传入要进行申请的activity
     */
    public static void requestPermissionsRWM(Activity activity) {

        try {
            int pmsWrite = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            int pmsRead = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.READ_EXTERNAL_STORAGE");
            int pmsMount = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.MOUNT_UNMOUNT_FILESYSTEMS");
            if (pmsWrite != PackageManager.PERMISSION_GRANTED ||
                    pmsRead != PackageManager.PERMISSION_GRANTED ||
                    pmsMount != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
