package record.wilson.flutter.com.flutter_plugin_record.timer;

import android.text.TextUtils;
import android.util.Log;

import java.util.Locale;
import java.util.WeakHashMap;
/**
 * description: 定时器工具类
 * author: Simon
 * created at 2017/8/10 上午9:47
 */

public final class TimerUtils {
    //--tag--
    private static final String TAG = "TimerUtils";
    //--err info--
    private static final String ERR_INFO = "未找到对应的MTimer,确认是否设置过Tag!=>new Builder().setTag(String tag)";
    //--cache--
    private static WeakHashMap<String, MTimer> cacheTimerMap = new WeakHashMap<>();
    //--action--
    private static final int START = 0;
    private static final int PAUSE = 1;
    private static final int RESUME = 2;
    private static final int STOP = 3;

    //--recycle build--
    private static final MTimer.Builder BUILDER = new MTimer.Builder();

    private TimerUtils() {
        throw new AssertionError("you can't init me！");
    }

    /**
     * 注意此方法，会重复利用Builder 对象，所以每次build()完成后再重新使用该方法！！
     *
     * @return --builder
     */
    public static MTimer.Builder makeBuilder() {
        BUILDER.reset();//每次执行的时候都会重置一次
        return BUILDER;
    }

    /**
     * 开启timer ，时间清零
     */
    public static void startTimer(String tag) {
        actionTimer(START, tag);
    }

    /**
     * 恢复timer，不清零
     */
    public static void resumeTimer(String tag) {
        actionTimer(RESUME, tag);
    }

    /**
     * 暂停timer
     */
    public static void pauseTimer(String tag) {
        actionTimer(PAUSE, tag);
    }

    /**
     * 关闭 timer
     */
    public static void stopTimer(String tag) {
        actionTimer(STOP, tag);
    }

    /**
     * 格式化 时间 格式为  hh:mm:ss
     *
     * @param cnt
     * @return
     */
    public static String formatTime(long cnt) {
        long hour = cnt / 3600;
        long min = cnt % 3600 / 60;
        long second = cnt % 60;
        return String.format(Locale.CHINA, "%02d:%02d:%02d", hour, min, second);
    }

    //------------------------私有方法/内部类------------------------------

    /**
     * 添加timer到缓存
     *
     * @param tag   --tag
     * @param timer --timer
     */
    public static void addTimerToCache(String tag, MTimer timer) {
        if (cacheTimerMap == null) {
            cacheTimerMap = new WeakHashMap<>();
        }
        cacheTimerMap.put(tag, timer);
    }

    /**
     * 真正的执行方法
     *
     * @param action --行为
     * @param tag    --tag
     */
    private static void actionTimer(int action, String tag) {
        //-----check tag----
        if (!checkTag(tag)) {
            Log.e(TAG, "The tag is empty or null！");
            return;
        }
        //-----check timer----
        MTimer timer = findMTimerByTag(tag);
        if (timer == null) {
            Log.e(TAG, "Can't found timer by tag!");
            return;
        }

        //-----action timer----
        switch (action) {
            case START:
                timer.startTimer();
                break;
            case RESUME:
                timer.resumeTimer();
                break;
            case PAUSE:
                timer.pauseTimer();
                break;
            case STOP:
                timer.stopTimer();
                break;
        }


    }


    /**
     * 通过tag获取mtimer
     *
     * @param tag --设置的tag
     * @return --MTimer
     */
    private static MTimer findMTimerByTag(String tag) {
        if (!checkTag(tag) || cacheTimerMap == null || cacheTimerMap.size() == 0) {//tag无效，没有缓存数据，返回null
            return null;
        } else {//反之根据tag返回
            return cacheTimerMap.get(tag);
        }
    }

    /**
     * 判断tag 是否有效
     *
     * @param tag --tag
     * @return true表示有效，反之无效
     */
    private static boolean checkTag(String tag) {
        return !TextUtils.isEmpty(tag);
    }


}
