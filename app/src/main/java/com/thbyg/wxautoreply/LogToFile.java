package com.thbyg.wxautoreply;

import android.content.Context;
import android.os.Environment;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
/**
 * Created by myxiang on 2017/10/5.
 */

public class LogToFile {
    /**
     * 上下文对象
     */
    private static Context      mContext;
    /**
     * FileLogUtils类的实例
     */
    private static LogToFile instance;
    /**
     * 用于保存日志的文件
     */
    public static File         logFile;
    /**
     * 日志中的时间显示格式
     */
    private static       SimpleDateFormat logSDF       = new SimpleDateFormat("MMdd HH:mm:ss.SSS");
    /**
     * 日志的最大占用空间 - 单位：字节
     * <p>
     * 注意：为了性能，没有每次写入日志时判断，故日志在写入第二次初始化之前，不会受此变量限制，所以，请注意日志工具类的初始化时间
     * <p>
     * 为了衔接上文，日志超出设定大小后不会被直接删除，而是存储一个副本，所以实际占用空间是两份日志大小
     * <p>
     * 除了第一次超出大小后存为副本外，第二次及以后再次超出大小，则会覆盖副本文件，所以日志文件最多也只有两份
     * <p>
     * 默认10M
     */
    private static final int              LOG_MAX_SIZE = 10 * 1024 * 1024;
    /**
     * 以调用者的类名作为TAG
     */
    private static String tag;

    private static final String MY_TAG = "LogToFile";

    /**
     * 初始化日志库
     *
     * @param context
     */
    public static void init(Context context) {
        Log.i(MY_TAG, "init ...");
        if (null == mContext || null == instance || null == logFile || !logFile.exists()) {
            mContext = context;
            instance = new LogToFile();
            logFile = getLogFile();
            Log.i(MY_TAG, "LogFilePath is: " + logFile.getPath());
            // 获取当前日志文件大小
            long logFileSize = getFileSize(logFile);
            Log.d(MY_TAG, "Log max size is: " + Formatter.formatFileSize(context, LOG_MAX_SIZE));
            Log.i(MY_TAG, "log now size is: " + Formatter.formatFileSize(context, logFileSize));
            // 若日志文件超出了预设大小，则重置日志文件
            if (LOG_MAX_SIZE < logFileSize) {
                resetLogFile();
            }
        } else {
            Log.i(MY_TAG, "LogToFileUtils has been init ...");
        }
    }

    public static void toast(String str) {
        Toast.makeText(AppContext.getContext(), str, Toast.LENGTH_LONG).show();
    }
    /**
     * 写入日志文件的数据
     *
     * @param str 需要写入的数据
     */
    public static void write(Object str) {
        // 判断是否初始化或者初始化是否成功
        logFile = getLogFile();
        if (null == mContext || null == instance || null == logFile || !logFile.exists()) {
            Log.e(MY_TAG, "Initialization failure !!!");
            return;
        }
        String logStr = getFunctionInfo() + " - " + str.toString();
        Log.i(tag, logStr);

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(logFile, true));
            bw.write("\r\n" + logStr);
            bw.write("\r\n");
            bw.flush();
        } catch (Exception e) {
            Log.e(tag, "Write failure !!! " + e.toString());
        }
    }
    /**
     * 写入到特定的日志文件的数据
     *
     * @param str 需要写入的数据
     */
    public static void write(Object str,String filename) {
        // 判断是否初始化或者初始化是否成功
        //delLogFile(filename);
        logFile = getLogFile(filename);
        if (null == mContext || null == instance || null == logFile || !logFile.exists()) {
            Log.e(MY_TAG, "Initialization failure !!!");
            return;
        }
        String logStr = getFunctionInfo() + " - " + str.toString();
        Log.i(tag, logStr);

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(logFile, true));
            bw.write("\r\n" + logStr);
            bw.write("\r\n");
            bw.flush();
        } catch (Exception e) {
            Log.e(tag, "Write failure !!! " + e.toString());
        }
    }

    /**
     * 重置日志文件
     * <p>
     * 若日志文件超过一定大小，则把日志改名为lastLog.txt，然后新日志继续写入日志文件
     * <p>
     * 每次仅保存一个上一份日志，日志文件最多有两份
     * <p/>
     */
    private static void resetLogFile() {
        Log.i(MY_TAG, "Reset Log File ... ");
        // 创建lastLog.txt，若存在则删除
        File lastLogFile = new File(logFile.getParent() + "/lastLog.txt");
        if (lastLogFile.exists()) {
            lastLogFile.delete();
        }
        // 将日志文件重命名为 lastLog.txt
        logFile.renameTo(lastLogFile);
        // 新建日志文件
        try {
            logFile.createNewFile();
        } catch (Exception e) {
            Log.e(MY_TAG, "Create log file failure !!! " + e.toString());
        }
    }

    /**
     * 获取文件大小
     *
     * @param file 文件
     * @return
     */
    private static long getFileSize(File file) {
        long size = 0;
        if (file.exists()) {
            try {
                FileInputStream fis = new FileInputStream(file);
                size = fis.available();
            } catch (Exception e) {
                Log.e(MY_TAG, e.toString());
            }
        }
        return size;
    }

    /**
     * 获取APP日志文件
     *
     * @return APP日志文件
     */
    public static File getLogFile() {
        File file;
        // 判断是否有SD卡或者外部存储器
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            // 有SD卡则使用SD - PS:没SD卡但是有外部存储器，会使用外部存储器
            // SD\Android\data\包名\files\Log\logs.txt
            file = new File(mContext.getExternalFilesDir("Log").getPath() + "/");
        } else {
            // 没有SD卡或者外部存储器，使用内部存储器
            // \data\data\包名\files\Log\logs.txt
            file = new File(mContext.getFilesDir().getPath() + "/Log/");
        }
        // 若目录不存在则创建目录
        if (!file.exists()) {
            file.mkdir();
        }
        File logFile = new File(file.getPath() + "/logs.txt");
        //Log.d(MY_TAG,"logFile Path:" + file.getPath() + "/logs.txt");
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (Exception e) {
                Log.e(MY_TAG, "Create log file failure !!! " + e.toString());
            }
        }
        return logFile;
    }
    /**
     * 获取APP日志文件，根据文件名
     *
     * @return APP日志文件
     */
    public static File getLogFile(String filename) {
        File file;
        // 判断是否有SD卡或者外部存储器
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            // 有SD卡则使用SD - PS:没SD卡但是有外部存储器，会使用外部存储器
            // SD\Android\data\包名\files\Log\logs.txt
            file = new File(mContext.getExternalFilesDir("Log").getPath() + "/");
        } else {
            // 没有SD卡或者外部存储器，使用内部存储器
            // \data\data\包名\files\Log\logs.txt
            file = new File(mContext.getFilesDir().getPath() + "/Log/");
        }
        // 若目录不存在则创建目录
        if (!file.exists()) {
            file.mkdir();
        }
        File logFile = new File(file.getPath() + "/" + filename + ".txt");
        //Log.d(MY_TAG,"logFile Path:" + file.getPath() + "/logs.txt");
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (Exception e) {
                Log.e(MY_TAG, "Create log file failure !!! " + e.toString());
            }
        }
        return logFile;
    }
    /**
     * 删除APP日志文件
     *
     * @return 删除成功返回true，否则false。
     */
    public static boolean delLogFile() {
        File file;
        boolean f = false;
        // 判断是否有SD卡或者外部存储器
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            // 有SD卡则使用SD - PS:没SD卡但是有外部存储器，会使用外部存储器
            // SD\Android\data\包名\files\Log\logs.txt
            file = new File(mContext.getExternalFilesDir("Log").getPath() + "/");
        } else {
            // 没有SD卡或者外部存储器，使用内部存储器
            // \data\data\包名\files\Log\logs.txt
            file = new File(mContext.getFilesDir().getPath() + "/Log/");
        }
        // 若目录不存在则创建目录
        if (!file.exists()) {
            f = true;
            return f;
        }
        File logFile = new File(file.getPath() + "/logs.txt");
        //Log.d(MY_TAG,"logFile Path:" + file.getPath() + "/logs.txt");
        if (logFile.exists()) {
            try {
                logFile.delete();
                f = true;
            } catch (Exception e) {
                f = false;
                Log.e(MY_TAG, "Delete log file failure !!! " + e.toString());
            }
        }
        return f;
    }
    /**
     * 删除APP指定日志文件
     *
     * @return 删除成功返回true，否则false。
     */
    public static boolean delLogFile(String filename) {
        File file;
        boolean f = false;
        // 判断是否有SD卡或者外部存储器
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            // 有SD卡则使用SD - PS:没SD卡但是有外部存储器，会使用外部存储器
            // SD\Android\data\包名\files\Log\logs.txt
            file = new File(mContext.getExternalFilesDir("Log").getPath() + "/");
        } else {
            // 没有SD卡或者外部存储器，使用内部存储器
            // \data\data\包名\files\Log\logs.txt
            file = new File(mContext.getFilesDir().getPath() + "/Log/");
        }
        // 若目录不存在则创建目录
        if (!file.exists()) {
            f = true;
            return f;
        }
        File logFile = new File(file.getPath() + "/" + filename + ".txt");
        //Log.d(MY_TAG,"logFile Path:" + file.getPath() + "/logs.txt");
        if (logFile.exists()) {
            try {
                logFile.delete();
                f = true;
            } catch (Exception e) {
                f = false;
                Log.e(MY_TAG, "Delete log file failure !!! " + e.toString());
            }
        }
        return f;
    }

    /**
     * 获取当前函数的信息
     *
     * @return 当前函数的信息
     */
    private static String getFunctionInfo() {
        StackTraceElement[] sts = Thread.currentThread().getStackTrace();
        if (sts == null) {
            return null;
        }
        for (StackTraceElement st : sts) {
            if (st.isNativeMethod()) {
                continue;
            }
            if (st.getClassName().equals(Thread.class.getName())) {
                continue;
            }
            if (st.getClassName().equals(instance.getClass().getName())) {
                continue;
            }
            tag = st.getFileName();
            return "[" + logSDF.format(new java.util.Date()) + " " + st.getMethodName() + " L" + st.getLineNumber() + "]";
            //return "[" + logSDF.format(new java.util.Date()) + " " + st.getClassName() + " " + st.getMethodName() + " Line:" + st.getLineNumber() + "]";
        }
        return null;
    }

}
