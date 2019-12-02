package record.wilson.flutter.com.flutter_plugin_record.timer;

import android.text.TextUtils;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;

public class MTimer implements ITimer {
    //--real timer--
    private Timer timer;
    //--default task--
    private TimerTask task;
    //--default initdelay--
    private long initDelay = 0l;
    //--default delay--
    private long delay = 0l;
    //--call back--
    private ITimerChangeCallback[] callbacks = null;
    //--real time--
    private AtomicLong time;//时间的记录工具

    //--action--
    private static final int START = 0;
    private static final int PAUSE = 1;
    private static final int RESUME = 2;
    private static final int STOP = 3;

    private int status = STOP;//默认是stop

    private MTimer(long initDelay, long delay, ITimerChangeCallback[] callbacks) {
        this.initDelay = initDelay;
        this.delay = delay;
        this.callbacks = callbacks;
    }

    //-----------------外部方法------------------------

    /**
     * 用于生成MTimer 对象
     *
     * @return --MTimer
     */
    public static Builder makeTimerBuilder() {
        return new Builder();
    }

    /**
     * 开启 timer
     */
    @Override
    public void startTimer() {
        //判断当前是不是stop,是的话开始运行
        if (status != STOP) {
            return;
        }
        //切换当前状态为 start
        status = START;
        realStartTimer(true);
    }

    /**
     * 暂停timer
     */
    @Override
    public void pauseTimer() {
        //判断当前是不是start 是不是resume,如果是其中一个就可以
        if (status != START && status != RESUME) {
            return;
        }
        //切换当前状态为 pause
        status = PAUSE;
        realStopTimer(false);
    }

    /**
     * 重启timer
     */
    @Override
    public void resumeTimer() {
        //判断当前是不是pause ,如果是则恢复
        if (status != PAUSE) {
            return;
        }
        //切换当前状态为 resume
        status = RESUME;
        realStartTimer(false);
    }

    /**
     * 关闭timer
     */
    @Override
    public void stopTimer() {
        //无论当前处于那种状态都可以stop
        status = STOP;
        realStopTimer(true);
    }

    //-----------------内部方法------------------------

    /**
     * timer 真正的开始方法
     *
     * @param isToZero --是否清除数据
     */
    private void realStartTimer(boolean isToZero) {
        //清空记录时间
        if (isToZero) {
            time = new AtomicLong(0);
        }
        //重新生成timer、task
        if (timer == null && task == null) {
            timer = new Timer();
            task = createTask();
            timer.scheduleAtFixedRate(task, initDelay, delay);
        }
    }

    /**
     * timer 真正的关闭方法
     *
     * @param isToZero --是否清除数据
     */
    private void realStopTimer(boolean isToZero) {
        //清空记录时间
        if (isToZero) {
            time = new AtomicLong(0);
        }
        //关闭当前的timer
        if (timer != null) {
            timer.purge();
            timer.cancel();
            timer = null;

        }
        //关闭当前任务
        if (task != null) {
            task.cancel();
            task = null;
        }
    }


    /**
     * 判断是否设置监听回调
     *
     * @return -- true 表示设置了回调，反之表示没设置
     */
    private boolean checkCallback() {
        return callbacks != null && callbacks.length > 0;
    }

    /**
     * 创建task
     *
     * @return
     */
    private TimerTask createTask() {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                time.incrementAndGet();
                notifyCallback(time);
            }
        };
        return task;
    }

    /**
     * 通知callback
     *
     * @param time --间距走的次数（花费时间=次数*delay+initDelay）
     */
    private void notifyCallback(AtomicLong time) {
        if (checkCallback()) {
            for (ITimerChangeCallback callback : callbacks) {
                callback.onTimeChange(time.longValue());
            }
        }
    }

    public static class Builder {

        //--default initdelay--
        private long initDelay = 0l;
        //--default delay--
        private long delay = 0l;
        //--call back--
        private ITimerChangeCallback[] callbacks = null;
        //--tag--
        private String tag;

        public Builder setTag(String tag) {
            if (TextUtils.isEmpty(tag)) {
                throw new NullPointerException("设置的tag无效！=>setTag(String tag)");
            }
            this.tag = tag;
            return this;
        }


        /**
         * 设置执行当前任务的时候首次执行时的延迟时间
         *
         * @param initDelay --首次执行的延迟时间(ms)
         */
        public Builder setInitDelay(long initDelay) {
            this.initDelay = initDelay;
            return this;
        }

        /**
         * 设置时间回调
         *
         * @param callbacks
         */
        public Builder setCallbacks(ITimerChangeCallback... callbacks) {
            this.callbacks = callbacks;
            return this;
        }

        /**
         * 设置后续的延迟时间
         *
         * @param delay --后续延迟时间(ms)
         */
        public Builder setDelay(long delay) {
            this.delay = delay;
            return this;
        }

        /**
         * 外部会重用此对象，所以需要重置其参数
         */
        public void reset() {
            tag = null;
            initDelay = 0l;
            delay = 0l;
            callbacks = null;
        }

        /**
         * 最终的生成方法，如果不调用此处，timer无法运行
         */
        public MTimer build() {
            //--check delay--
            if (initDelay < 0 || delay < 0) {
                throw new AssertionError("initDelay或delay 不允许小于0");
            }
            //--build timer--
            MTimer timer = new MTimer(initDelay, delay, callbacks);
            //--add to cache--
            if (!TextUtils.isEmpty(tag)) {
                TimerUtils.addTimerToCache(tag, timer);
            }
            //--return timer--
            return timer;
        }

    }
}