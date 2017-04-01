package com.chenenyu.router.util;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Process;

import java.util.List;

/**
 * Process utils.
 * <p>
 * Created by Cheney on 2017/3/30.
 */
public class ProcessUtils {
    public static final int PROCESS_UNKNOWN = -1;
    public static final int PROCESS_MAIN = 0;
    public static final int PROCESS_OTHER = 1;
    private static int sCurrentProcess = PROCESS_UNKNOWN;

    static {
        if (sCurrentProcess == PROCESS_UNKNOWN) {
            sCurrentProcess = AppUtils.INSTANCE.getPackageName()
                    .equals(getCurrentProcessName(AppUtils.INSTANCE)) ? PROCESS_MAIN : PROCESS_OTHER;
        }
    }

    public static String getCurrentProcessName(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        int pid = Process.myPid();
        List<ActivityManager.RunningAppProcessInfo> processInfos = am.getRunningAppProcesses();
        if (processInfos != null) {
            for (ActivityManager.RunningAppProcessInfo processInfo : processInfos) {
                if (pid == processInfo.pid) {
                    return processInfo.processName;
                }
            }
        }
        return null;
    }

    public static boolean isMainProcess(Context context) {
        return context.getPackageName().equals(getCurrentProcessName(context));
    }

    public static boolean isMainProcess() {
        return sCurrentProcess == PROCESS_MAIN;
    }

}
