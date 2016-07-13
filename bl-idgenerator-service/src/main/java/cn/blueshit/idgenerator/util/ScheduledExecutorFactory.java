package cn.blueshit.idgenerator.util;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by zhaoheng on 2016/7/13.
 */
public class ScheduledExecutorFactory {

    public static ScheduledExecutorService getScheExeService(int coreThreadPoolSize, final String threadFactoryName) {
        return new ScheduledThreadPoolExecutor(coreThreadPoolSize, new ThreadFactory() {
            private final AtomicInteger threadCount = new AtomicInteger(0);
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, threadFactoryName + "--" + threadCount.getAndIncrement());
            }
        });
    }
}
