package cn.blueshit.idgenerator.job;

import cn.blueshit.idgenerator.util.quene.AutoAddAndBoundedQueue;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhaoheng on 2016/7/13.
 */
public class QueueSupplierStarter implements Starter {


    private AutoAddAndBoundedQueue queue;

    private ScheduledExecutorService scheduledExecutorService;

    private int period;

    public QueueSupplierStarter(AutoAddAndBoundedQueue queue,
                                ScheduledExecutorService scheduledExecutorService,
                                int period) {
        this.queue = queue;
        this.scheduledExecutorService = scheduledExecutorService;
        this.period = period;
    }

    @Override
    public boolean start() {
        scheduledExecutorService.scheduleAtFixedRate(queue, 0, period, TimeUnit.MILLISECONDS);
        return true;
    }

    @Override
    public boolean startCompleted() {
        return queue.isInitialized();
    }
}
