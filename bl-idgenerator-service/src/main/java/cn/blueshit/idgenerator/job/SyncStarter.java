package cn.blueshit.idgenerator.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhaoheng on 2016/7/13.
 * 同步启动器..h2cache中有调用
 */
public class SyncStarter implements Starter {

    private static final Logger logger = LoggerFactory.getLogger(SyncStarter.class);

    private List<Starter> starters;

    private boolean completed = false;

    private int checkCompletedInterval = 1000;

    private DelayQueue<DelayElement> starterQueue = new DelayQueue<DelayElement>();

    public SyncStarter(List<Starter> starters) {
        this.starters = starters;
    }

    public SyncStarter(List<Starter> starters, int checkCompletedInterval) {
        this.starters = starters;
        this.checkCompletedInterval = checkCompletedInterval;
    }

    @Override
    public boolean start() {
        if (starters != null && !starters.isEmpty()) {
            //同步去执行 list的 Runnable
            for (Starter starter : starters) {
                logger.info("开始启动" + starter);
                long start = System.currentTimeMillis();
                //QueueSupplierStarter 去执行AutoAddAndBoundedQueue run方法
                //先执行 dbqueue  后执行 memoryqueue
                boolean success = starter.start();
                if (!success) {
                    return false;
                }
                //直到执行完成为止
                while (!starter.startCompleted()) {
                    starterQueue.add(new DelayElement(checkCompletedInterval, TimeUnit.MILLISECONDS));
                    try {
                        starterQueue.take();
                    } catch (InterruptedException e) {
                        logger.error("检查" + starter + "是否completed时进行暂停时失败", e);
                    }
                }
                logger.info(starter + "启动成功, 耗时：" + (System.currentTimeMillis() - start));
            }
        }
        completed = true;
        return completed;
    }


    @Override
    public boolean startCompleted() {
        return completed;
    }

    private class DelayElement implements Delayed {

        private long delayTime;

        private long createdTime = System.nanoTime();

        public DelayElement(long delayTime, TimeUnit unit) {
            this.delayTime = unit.toNanos(delayTime);
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return unit.convert(delayTime - (System.nanoTime() - createdTime), TimeUnit.NANOSECONDS);
        }

        @Override
        public int compareTo(Delayed other) {
            if (other == this)
                return 0;
            if (other instanceof DelayElement) {
                DelayElement x = (DelayElement) other;
                long diff = delayTime - x.delayTime;
                if (diff < 0)
                    return -1;
                else if (diff > 0)
                    return 1;
                else
                    return 1;
            }
            long d = (getDelay(TimeUnit.NANOSECONDS) - other.getDelay(TimeUnit.NANOSECONDS));
            return (d == 0) ? 0 : ((d < 0) ? -1 : 1);
        }
    }
}
