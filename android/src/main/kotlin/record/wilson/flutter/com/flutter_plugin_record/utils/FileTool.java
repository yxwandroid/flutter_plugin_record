package record.wilson.flutter.com.flutter_plugin_record.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.ExifInterface;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.os.Environment.MEDIA_MOUNTED;

public final class FileTool {

    private FileTool() {
    }

    public static final String FILE_TYPE_PDF = "pdf";

    public static final String FILE_TYPE_APK = "vnd.android.package-archive";

    public static final String FILE_TYPE_EXCEL  = "ms-excel";
    public static final String FILE_TYPE_EXCELX = "vnd.openxmlformats-officedocument.spreadsheetml";

    public static final String FILE_TYPE_PPT  = "powerpoint";
    public static final String FILE_TYPE_PPTX = "vnd.openxmlformats-officedocument.presentationml";

    public static final String FILE_TYPE_WORD  = "word";
    public static final String FILE_TYPE_WORDX = "vnd.openxmlformats-officedocument.wordprocessingml";

    public static final String FILE_TYPE_RAR   = "rar";
    public static final String FILE_TYPE_ZIP   = "zip";
    public static final String FILE_TYPE_AUDIO = "audio";
    public static final String FILE_TYPE_TEXT  = "text";
    public static final String FILE_TYPE_XML   = "xml";
    public static final String FILE_TYPE_HTML  = "html";
    public static final String FILE_TYPE_IMAGE = "image";
    public static final String FILE_TYPE_VIDEO = "video";
    public static final String FILE_TYPE_APP   = "application";

    /**
     * @Fields maxFileSize : 最大允许文件大小
     **/
    private static final int MAX_FILE_SIZE = 2 * 1024 * 1024;

    public static byte[] GetFileDataBytes(File file, int fileLen) {
        ByteArrayOutputStream bos = null;
        BufferedInputStream in = null;
        try {
            bos = new ByteArrayOutputStream(fileLen);
            in = new BufferedInputStream(new FileInputStream(file));
            int buf_size = 1024;
            byte[] buffer = new byte[buf_size];
            int len = 0;
            while (-1 != (len = in.read(buffer, 0, buf_size))) {
                bos.write(buffer, 0, len);
            }
            return bos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeIO(bos, in);
        }
        return null;
    }

    public static String getFileRootPathString(Context context) {
        return new String(getFileRootPath(context).getAbsolutePath());

    }

    public static String getDefaultApkSavePath() {
        return new StringBuffer(Environment.getExternalStorageDirectory().getAbsolutePath()).append(File.separator).append(Environment.DIRECTORY_DOWNLOADS).toString();
    }


    public static File getFileRootPath(Context context) {
        File file = null;
        if (context == null) return null;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            file = context.getExternalCacheDir();
        }
        if (file == null) {
            file = context.getCacheDir();
        }
        return file;
    }

//	public static String getHeadImagePathString(Context mContext) {
//		return getHeadImagePath(mContext).getAbsolutePath();
//	}

    public static void copyFile2(String oldFilePath, String newFilePath) {
        FileOutputStream fs = null;
        FileInputStream inStream = null;
        try {
            fs = new FileOutputStream(newFilePath);
            inStream = new FileInputStream(oldFilePath);
            int byteread = 0;
            if (inStream.available() <= MAX_FILE_SIZE) {
                byte[] buffer = new byte[1024];
                while ((byteread = inStream.read(buffer)) != -1) {
                    fs.write(buffer, 0, byteread);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeIO(fs, inStream);
        }
    }

    public static boolean isValidName(String text) {
        Pattern pattern = Pattern.compile(
                "# Match a valid Windows filename (unspecified file system).          \n" +
                        "^                                # Anchor to start of string.        \n" +
                        "(?!                              # Assert filename is not: CON, PRN, \n" +
                        "  (?:                            # AUX, NUL, COM1, COM2, COM3, COM4, \n" +
                        "    CON|PRN|AUX|NUL|             # COM5, COM6, COM7, COM8, COM9,     \n" +
                        "    COM[1-9]|LPT[1-9]            # LPT1, LPT2, LPT3, LPT4, LPT5,     \n" +
                        "  )                              # LPT6, LPT7, LPT8, and LPT9...     \n" +
                        "  (?:\\.[^.]*)?                  # followed by optional extension    \n" +
                        "  $                              # and end of string                 \n" +
                        ")                                # End negative lookahead assertion. \n" +
                        "[^<>:\"/\\\\|?*\\x00-\\x1F]*     # Zero or more valid filename chars.\n" +
                        "[^<>:\"/\\\\|?*\\x00-\\x1F\\ .]  # Last char is not a space or dot.  \n" +
                        "$                                # Anchor to end of string.            ",
                Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.COMMENTS);
        Matcher matcher = pattern.matcher(text);
        boolean isMatch = matcher.matches();
        return isMatch;
    }

    public static void deleteFile(File f) {
        if (f.exists()) {
            if (f.isDirectory()) {
                File[] files = f.listFiles();
                for (int i = 0; i < files.length; i++) {
                    deleteFile(files[i]);
                }
            }
            f.delete();
        }
    }

    public static String getFileMD5String(File f) {
        byte[] buffer = new byte[4 * 1024];
        BufferedInputStream bis = null;
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            bis = new BufferedInputStream(new FileInputStream(f));
            int lent;
            while ((lent = bis.read(buffer)) != -1) {
                digest.update(buffer, 0, lent);
            }
            byte[] hash = digest.digest();
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                if ((b & 0xFF) < 0x10) hex.append("0");
                hex.append(Integer.toHexString(b & 0xFF));
            }
            return hex.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } finally {
            FileTool.closeIO(bis);
        }
        return null;
    }

    public static String md5(String string) {
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Huh, MD5 should be supported?", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Huh, UTF-8 should be supported?", e);
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10) hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString();
    }

    /**
     * <p>
     * 判断照片角度
     * </p>
     *
     * @param path @return int @throws
     */
    public static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    public static String getFileExtension(File file) {
        String fileName = file.getName();
        return getFileExtension(fileName);
    }

    /**
     * 获取文件扩展名
     *
     * @param fileName
     * @return
     */
    public static String getFileExtension(String fileName) {

        if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0) {
            return fileName.substring(fileName.lastIndexOf(".") + 1);
        } else {
            return "";
        }
    }

    public static String urlDelStoken(String url) {
        url = url.trim().toLowerCase();
        // 是否为http并且是否包含Stoken
        if (!(url.startsWith("http") && url.contains("stoken"))) {
            return url;
        }
        String[] tampStrings = url.split("[?&]");
        for (String temp : tampStrings) {
            if (temp.trim().startsWith("stoken=")) {
                url = url.replace(temp, "").replace("?&", "?");
                break;
            }
        }
        return url;
    }

    /*******************************************************************************
     * Copyright 2011-2014 Sergey Tarasevich
     * <p/>
     * Licensed under the Apache License, Version 2.0 (the "License"); you may
     * not use this file except in compliance with the License. You may obtain a
     * copy of the License at
     * <p/>
     * http://www.apache.org/licenses/LICENSE-2.0
     * <p/>
     * Unless required by applicable law or agreed to in writing, software
     * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
     * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
     * License for the specific language governing permissions and limitations
     * under the License.
     *******************************************************************************/

    private static final String EXTERNAL_STORAGE_PERMISSION = "android.permission.WRITE_EXTERNAL_STORAGE";
    private static final String INDIVIDUAL_DIR_NAME         = "uil-images";
    private static final String INDIVIDUAL_LOG_FOLDER        = "logs";
    private static final String INDIVIDUAL_UPLOAD_CACHE     = "upload-cache";
    private static final String FILE_DIR_NAME               = "file_cache";
    private static final String FILE_AUDIO_DIR_NAME         = "Audio";
    private static final String FILE_IMAGE_DIR_NAME         = "image";

    /**
     * Returns application cache directory. Cache directory will be created on
     * SD card <i>("/Android/data/[app_package_name]/cache")</i> if card is
     * mounted and app has appropriate permission. Else - Android defines cache
     * directory on device's file system.
     *
     * @return Cache {@link File directory}.<br />
     * <b>NOTE:</b> Can be null in some unpredictable cases (if SD card
     * is unmounted and {@link Context#getCacheDir()
     * Context.getCacheDir()} returns null).
     */
    public static File getCacheDirectory(Context context) {
        return getCacheDirectory(context, true);
    }

    /**
     * Returns application cache directory. Cache directory will be created on
     * SD card <i>("/Android/data/[app_package_name]/cache")</i> (if card is
     * mounted and app has appropriate permission) or on device's file system
     * depending incoming parameters.
     *
     * @param preferExternal Whether prefer external location for cache
     * @return Cache {@link File directory}.<br />
     * <b>NOTE:</b> Can be null in some unpredictable cases (if SD card
     * is unmounted and {@link Context#getCacheDir()
     * Context.getCacheDir()} returns null).
     */
    public static File getCacheDirectory(Context context, boolean preferExternal) {
        assert context != null;
        File appCacheDir = null;
        String externalStorageState;
        try {
            externalStorageState = Environment.getExternalStorageState();
        } catch (NullPointerException e) { // (sh)it happens (Issue #660)
            externalStorageState = "";
        } catch (IncompatibleClassChangeError e) { // (sh)it happens too (Issue
            // #989)
            externalStorageState = "";
        }
        if (preferExternal && MEDIA_MOUNTED.equals(externalStorageState) && hasExternalStoragePermission(context)) {
            appCacheDir = getExternalCacheDir(context);
        }
        if (appCacheDir == null) {
            appCacheDir = context.getCacheDir();
        }
        if (appCacheDir == null) {
            String cacheDirPath = "/data/data/" + context.getPackageName() + "/cache/";
            appCacheDir = new File(cacheDirPath);
        }
        return appCacheDir;
    }

    /**
     * Returns individual application cache directory (for only image caching
     * from ImageLoader). Cache directory will be created on SD card
     * <i>("/Android/data/[app_package_name]/cache/uil-images")</i> if card is
     * mounted and app has appropriate permission. Else - Android defines cache
     * directory on device's file system.
     *
     * @return Cache {@link File directory}
     */
    public static File getIndividualImageCacheDirectory(Context context) {
        return getIndividualCacheDirectory(context, INDIVIDUAL_DIR_NAME);
    }

    public static File getIndividualUploadCacheDirectory(Context context) {
        return getIndividualCacheDirectory(context, INDIVIDUAL_UPLOAD_CACHE);
    }

    public static File getIndividualLogCacheDirectory(Context context) {
        return getIndividualCacheDirectory(context, INDIVIDUAL_LOG_FOLDER);
    }


    public static File getIndividualAudioCacheDirectory(Context context) {
        File file = new File(getIndividualCacheDirectory(context, FILE_DIR_NAME), FILE_AUDIO_DIR_NAME);
        if(!file.exists()) file.mkdirs();
        return file;
    }

    public static File getIndividualImageFileDirectory(Context context) {
        return new File(getIndividualCacheDirectory(context, FILE_DIR_NAME), FILE_IMAGE_DIR_NAME);
    }

    /**
     * Returns individual application cache directory (for only image caching
     * from ImageLoader). Cache directory will be created on SD card
     * <i>("/Android/data/[app_package_name]/cache/uil-images")</i> if card is
     * mounted and app has appropriate permission. Else - Android defines cache
     * directory on device's file system.
     *
     * @param cacheDir Cache directory path (e.g.: "AppCacheDir",
     *                 "AppDir/cache/images")
     * @return Cache {@link File directory}
     */
    public static File getIndividualCacheDirectory(Context context, String cacheDir) {
        File appCacheDir = getCacheDirectory(context);
        File individualCacheDir = new File(appCacheDir, cacheDir);
        if (!individualCacheDir.exists()) {
            if (!individualCacheDir.mkdir()) {
                individualCacheDir = appCacheDir;
            }
            try {
                new File(individualCacheDir, ".nomedia").createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return individualCacheDir;
    }

    /**
     * Returns specified application cache directory. Cache directory will be
     * created on SD card by defined path if card is mounted and app has
     * appropriate permission. Else - Android defines cache directory on
     * device's file system.
     *
     * @param cacheDir Cache directory path (e.g.: "AppCacheDir",
     *                 "AppDir/cache/images")
     * @return Cache {@link File directory}
     */
    public static File getOwnCacheDirectory(Context context, String cacheDir) {
        File appCacheDir = null;
        if (MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) && hasExternalStoragePermission(context)) {
            appCacheDir = new File(Environment.getExternalStorageDirectory(), cacheDir);
        }
        if (appCacheDir == null || (!appCacheDir.exists() && !appCacheDir.mkdirs())) {
            appCacheDir = context.getCacheDir();
        }
        return appCacheDir;
    }

    /**
     * Returns specified application cache directory. Cache directory will be
     * created on SD card by defined path if card is mounted and app has
     * appropriate permission. Else - Android defines cache directory on
     * device's file system.
     *
     * @param cacheDir Cache directory path (e.g.: "AppCacheDir",
     *                 "AppDir/cache/images")
     * @return Cache {@link File directory}
     */
    public static File getOwnCacheDirectory(Context context, String cacheDir, boolean preferExternal) {
        assert context != null;
        File appCacheDir = null;
        if (preferExternal && MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                && hasExternalStoragePermission(context)) {
            appCacheDir = new File(Environment.getExternalStorageDirectory(), cacheDir);
        }
        if (appCacheDir == null || (!appCacheDir.exists() && !appCacheDir.mkdirs())) {
            appCacheDir = context.getCacheDir();
        }
        return appCacheDir;
    }

    @SuppressLint("NewApi")
    public static long getDiskSizeRemain(Context context) {
        File path = getIndividualImageCacheDirectory(context);
        StatFs stat = new StatFs(path.getPath());
        long result;
        long block;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR1) {
            result = stat.getAvailableBlocksLong();
            block = stat.getBlockSizeLong();
        } else {
            result = stat.getAvailableBlocks();
            block = stat.getBlockSize();
        }
        return result * block;
    }

    private static File getExternalCacheDir(Context context) {
        assert context != null;
        File dataDir = new File(new File(Environment.getExternalStorageDirectory(), "Android"), "data");
        File appCacheDir = new File(new File(dataDir, context.getPackageName()), "cache");
        if (!appCacheDir.exists()) {
            if (!appCacheDir.mkdirs()) {
                return null;
            }
            try {
                new File(appCacheDir, ".nomedia").createNewFile();
            } catch (IOException e) {
            }
        }
        return appCacheDir;
    }

    private static boolean hasExternalStoragePermission(Context context) {
        assert context != null;
        int perm = context.checkCallingOrSelfPermission(EXTERNAL_STORAGE_PERMISSION);
        return perm == PackageManager.PERMISSION_GRANTED;
    }

    public static void closeIO(Closeable... io) {
        for (Closeable close : io) {
            if (close != null) {
                try {
                    close.close();
                    close = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String getMimeType(String path) {
        String defaultContentType = "application/octet-stream";
        if (TextUtils.isEmpty(path))
            return defaultContentType;
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String contentTypeFor = null;
        try {
            contentTypeFor = fileNameMap.getContentTypeFor(path);
        } catch (Throwable t) {
            PrintStream stream = null;
//            try {
//                stream = LogUtils.getErrorPrintStream();
//                if (stream != null) {
//                    t.printStackTrace(stream);
//                }
//                contentTypeFor = getMimeType1(path);
//            } catch (Throwable e) {
//                e.printStackTrace();
//            } finally {
//                closeIO(stream);
//            }
        }
        return TextUtils.isEmpty(contentTypeFor) ? defaultContentType : contentTypeFor;
    }

    /**
     * @return The MIME type for the given file.
     */
    public static String getMimeType(File file) {
        String extension = getFileExtension(file.getName());
        return getMimeTypeByExtension(extension);
//        if (extension.length() > 0)
//            return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.substring(1));
//        return "application/octet-stream";
    }

    /**
     * @return The MIME type for the given file.
     */
    public static String getMimeType1(String path) {
        String extension = getFileExtension(path);
        return getMimeTypeByExtension(extension);
//        if (extension.length() > 0)
//            return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.substring(1));
//        return "application/octet-stream";
    }

    /**
     * @return The MIME type for the given file.
     */
    private static String getMimeTypeByExtension(String extension) {
        if (extension.length() > 0)
            return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        return "application/octet-stream";
    }

    public static String getDefaultSavePath() {
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "oMiniBox");
        if (!file.exists()) {
            file.mkdirs();
        }
        return file.getAbsolutePath();
    }

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    public static long getFolderSize(File file) {
        long size = 0;
        File[] fileList = file.listFiles();
        for (int i = 0; i < fileList.length; i++) {
            if (fileList[i].isDirectory()) {
                size = size + getFolderSize(fileList[i]);
            } else {
                size = size + fileList[i].length();
            }
        }
        return size;
    }


    public static void deleteFolderFile(String filePath, boolean deleteThisPath)
            throws IOException {
        if (!TextUtils.isEmpty(filePath)) {
            File file = new File(filePath);

            if (file.isDirectory()) {// 处理目录
                File files[] = file.listFiles();
                for (int i = 0; i < files.length; i++) {
                    deleteFolderFile(files[i].getAbsolutePath(), true);
                }
            }
            if (deleteThisPath) {
                if (!file.isDirectory()) {// 如果是文件，删除
                    file.delete();
                } else {// 目录
                    if (file.listFiles().length == 0) {// 目录下没有文件或者目录，删除
                        file.delete();
                    }
                }
            }
        }
    }

}
