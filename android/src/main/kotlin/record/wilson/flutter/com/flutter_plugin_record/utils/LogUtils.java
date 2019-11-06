/*
 * Copyright 2014 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package record.wilson.flutter.com.flutter_plugin_record.utils;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;

public class LogUtils {
    public static String LOG_PREFIX         = "";
    private static int    LOG_PREFIX_LENGTH  = LOG_PREFIX.length();
    private static int    MAX_LOG_TAG_LENGTH = 23;

    public static boolean LOGGING_ENABLED = true;

    public static String makeLogTag(String str) {
        if (str.length() > MAX_LOG_TAG_LENGTH - LOG_PREFIX_LENGTH) {
            return LOG_PREFIX + str.substring(0, MAX_LOG_TAG_LENGTH - LOG_PREFIX_LENGTH - 1);
        }
        return LOG_PREFIX + str;
    }

    /**
     * Don't use this when obfuscating class names!
     */
    public static String makeLogTag(Class cls) {
        return makeLogTag(cls.getSimpleName());
    }

    public static String getTAG() {
        return LOG_PREFIX;
    }

    public static void LOGD(String message) {
        if (LOGGING_ENABLED) {
            Log.d(getTag(), message != null ? message : "message is null");
        }
    }

    public static void LOGD(final String tag, String message) {
        if (LOGGING_ENABLED) {
            if (Log.isLoggable(tag, Log.DEBUG)) {
                Log.d(tag, message);
            }
        }
    }

    public static void LOGDPrintProcess(final String tag, String message) {
        if (LOGGING_ENABLED) {
            if (Log.isLoggable(tag, Log.DEBUG)) {
                String msg = getLogMessage(message);
                Log.d(tag, msg);
            }
        }
    }

    public static void LOGD(final String tag, String message, Throwable cause) {
        if (LOGGING_ENABLED) {
            if (Log.isLoggable(tag, Log.DEBUG)) {
                Log.d(tag, message, cause);
            }
        }
    }

    public static void LOGV(String message) {
        if (LOGGING_ENABLED) {
            Log.v(getTag(), message != null ? message : "message is null");
        }
    }

    public static void LOGV(final String tag, String message) {
        if (LOGGING_ENABLED) {
            if (Log.isLoggable(tag, Log.VERBOSE)) {
                Log.v(tag, message);
            }
        }
    }

    public static void LOGV(final String tag, String message, Throwable cause) {
        if (LOGGING_ENABLED) {
            if (Log.isLoggable(tag, Log.VERBOSE)) {
                Log.v(tag, message, cause);
            }
        }
    }

    public static void LOGI(String message) {
        if (LOGGING_ENABLED) {
            Log.i(getTag(), message != null ? message : "message is null");
        }
    }

    public static void LOGI(final String tag, String message) {
        if (LOGGING_ENABLED) {
            Log.i(tag, message);
        }
    }

    public static void LOGI(final String tag, String message, Throwable cause) {
        if (LOGGING_ENABLED) {
            Log.i(tag, message, cause);
        }
    }

    public static void LOGW(String message) {
        if (LOGGING_ENABLED) {
            Log.w(getTag(), message != null ? message : "message is null");
        }
    }

    public static void LOGW(final String tag, String message) {
        if (LOGGING_ENABLED) {
            Log.w(tag, message);
        }
    }

    public static void LOGW(final String tag, String message, Throwable cause) {
        if (LOGGING_ENABLED) {
            Log.w(tag, message, cause);
        }
    }

    public static void LOGE(String message) {
        if (LOGGING_ENABLED) {
            Log.e(getTag(), message != null ? message : "message is null");
        }
    }

    public static void LOGE(final String tag, String message) {
        if (LOGGING_ENABLED) {
            Log.e(tag, message);
        }
    }

    private static final int MAX_CHAR_ONE_LINE = 82;

    public static void LOGEPrintProcess(final String tag, String message) {
        if (LOGGING_ENABLED) {
            String msg = getLogMessage(message);
            Log.e(tag, msg);
        }
    }

    private static final int LOG_LENGTH = 100;

    private static String getLogMessage(String msg) {
        StringBuilder sb = new StringBuilder();
        String[] split = msg.split("\n");
        addPrintHead(sb);
        sb.append("\n")
                .append(getFullFillTopLine(new Date().toString(), LOG_PREFIX))
                .append("\n");
        for (int i = 0; i < split.length; i++) {
            sb.append(getLine(split[i])).append("\n");
        }
        sb.append(getFullFillBottomLine("pid : " + android.os.Process.myPid(), "tid : " + Thread.currentThread().getId())).append("\n");
        addPrintBottom(sb);
        return sb.toString();
    }

    private static void addPrintBottom(StringBuilder sb) {
        sb.append("└");
        fillLine(sb, LOG_LENGTH, "-");
        sb.append("┘");
    }

    private static final int PADDING_LEFT = 4;

    private static String getLine(String s) {
        int remind = LOG_LENGTH - 4 - s.getBytes().length - PADDING_LEFT;
        StringBuilder temp = new StringBuilder();
        temp.append("├┤");
        for (int j = 0; j < PADDING_LEFT; j++) {
            temp.append(" ");
        }
        temp.append(s);
        fillLine(temp, remind, " ");
        temp.append("├┤");
        return temp.toString();
    }

    private static final int POSITION_TOP    = 0;
    private static final int POSITION_MIDDLE = 1;
    private static final int POSITION_BOTTOM = 2;

    private static String getFullFillTopLine(String... s) {
        return getFullFillLine(POSITION_TOP, s);
    }

    private static String getFullFillBottomLine(String... s) {
        return getFullFillLine(POSITION_BOTTOM, s);
    }

    private static String getFullFillLine(int position, String... s) {
        int length = 0;
        for (int i = 0; i < s.length; i++) {
            int l = s[i].getBytes().length;
            length += l;
        }
        int remind = LOG_LENGTH - length - 4;
        int each = remind / (s.length + 1);
        int fix = each * (s.length + 1) - remind;
        StringBuilder temp = new StringBuilder();
        switch (position) {
            case POSITION_TOP:
                temp.append("├┬");
                break;
            case POSITION_MIDDLE:
                temp.append("├┤");
                break;
            case POSITION_BOTTOM:
                temp.append("├┴");
                break;
        }
        for (int j = 0; j < s.length; j++) {
            fillLine(temp, each, "-");
            temp.append(s[j]);
        }
        fillLine(temp, each - fix, "-");
        switch (position) {
            case POSITION_TOP:
                temp.append("┬┤");
                break;
            case POSITION_MIDDLE:
                temp.append("├┤");
                break;
            case POSITION_BOTTOM:
                temp.append("┴┤");
                break;
        }
        return temp.toString();
    }

    private static void fillLine(StringBuilder sb, int length, String filler) {
        int fill = length / filler.length();
        for (int k = 0; k < fill; k++) {
            sb.append(filler);
        }
    }

    private static void addPrintHead(StringBuilder sb) {
        sb.append("┌");
        fillLine(sb, LOG_LENGTH, "-");
        sb.append("┐");
    }


    public static void LOGE(final String tag, String message, Throwable cause) {
        if (LOGGING_ENABLED) {
            Log.e(tag, message, cause);
        }
    }

    public static void write2Log(Context context, String msg) {
        write2Log(context, msg, new StringBuilder(new Date().toString()).append("_log").toString());
    }

    public static PrintStream getErrorPrintStream(Context context) throws FileNotFoundException {
        File logFile = new File(FileTool.getIndividualLogCacheDirectory(context), new StringBuilder(new Date().toString()).append("_log").toString() + ".txt");
        return new PrintStream(new FileOutputStream(logFile));
    }

    public static void write2Log(Context context, String msg, String name) {
        if (LOGGING_ENABLED) {
            File logFile = new File(FileTool.getCacheDirectory(context), name + ".txt");
            BufferedWriter writer = null;
            try {
                if (!logFile.exists()) logFile.createNewFile();
                writer = new BufferedWriter(new FileWriter(logFile, true));
                writer.newLine();
                writer.append(msg);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                FileTool.closeIO(writer);
            }
        }
    }


    private static String getTag() {
        StackTraceElement[] trace = new Throwable().fillInStackTrace()
                .getStackTrace();
        String callingClass = "";
        for (int i = 2; i < trace.length; i++) {
            Class<?> clazz = trace[i].getClass();
            if (!clazz.equals(Log.class)) {
                callingClass = trace[i].getClassName();
                callingClass = callingClass.substring(callingClass
                        .lastIndexOf('.') + 1);
                break;
            }
        }
        return callingClass;
    }

    private LogUtils() {
    }

    public static IntervalCounter getIntervalCounter() {
        return new IntervalCounter();
    }

    public static class IntervalCounter {
        private long timeStamps;

        private IntervalCounter() {
            timeStamps = System.currentTimeMillis();
        }

        public long getInterval() {
            long result = System.currentTimeMillis() - timeStamps;
            timeStamps = System.currentTimeMillis();
            return result;
        }

        public long getTimeStamps() {
            timeStamps = System.currentTimeMillis();
            return timeStamps;
        }

        public String getIntervalStr() {
            return " interval is " + getInterval();
        }
    }

    public static String getCurCpuFreq() {
        String result = "N/A";
        try {
            FileReader fr = new FileReader(
                    "/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq");
            BufferedReader br = new BufferedReader(fr);
            String text = br.readLine();
            result = text.trim();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

}
