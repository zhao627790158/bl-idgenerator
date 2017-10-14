import cn.blueshit.idgenerator.util.ScheduledExecutorFactory;
import com.google.common.base.Stopwatch;
import org.junit.Test;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by zhaoheng on 17/10/14.
 */
public class ScheduledDequeTest {


    private ScheduledExecutorService memoryQueueSupplierScheduler = ScheduledExecutorFactory.getScheExeService(10, "memoryMode");
    private ExecutorService executorService = Executors.newFixedThreadPool(10);


    @Test
    public synchronized void test() throws Exception {
        Stopwatch started = Stopwatch.createStarted();
        final ScheduledDeque scheduledDeque = new ScheduledDeque(200000);
        System.out.println(started.elapsed(TimeUnit.MILLISECONDS));
        memoryQueueSupplierScheduler.scheduleAtFixedRate(scheduledDeque, 0, 10, TimeUnit.MILLISECONDS);
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Stopwatch started = Stopwatch.createStarted();
                while (!memoryQueueSupplierScheduler.isShutdown()) {
                    for (int j = 2; j >= 0; j--) {
                        Long aLong = scheduledDeque.takeFromFirst();
                        if (null == aLong) {
                            System.out.println(j + ":拿完耗时:" + started.elapsed(TimeUnit.MILLISECONDS));
                            started.reset();
                            try {
                                Thread.currentThread().sleep(1000);
                                System.out.println("take from first" + scheduledDeque.peekFirst() + ":last:" + scheduledDeque.peekLast());
                                started.start();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        } else {
                            break;
                        }
                    }

                }
            }
        });
        while (!memoryQueueSupplierScheduler.isShutdown()) {
            this.wait(1000);
        }

    }
}

class ScheduledDeque implements Runnable {

    //双向队列 Deque
    private final BlockingDeque<Long> targetQueue;

    private final int queueMaxSize;

    private AtomicLong atomicLong = new AtomicLong(0);

    public ScheduledDeque(int queueMaxSize) {
        this.targetQueue = new LinkedBlockingDeque<Long>(queueMaxSize);
        this.queueMaxSize = queueMaxSize;
        init();
    }

    private void init() {
        for (int i = 0; i < queueMaxSize; i++) {
            boolean b = this.putToLast(atomicLong.incrementAndGet());
            if (!b) {
                System.out.println("填充deque失败");
            }
        }
    }

    public Long takeFromFirst() {
        return targetQueue.pollFirst();
    }


    public boolean putToLast(Long id) {
        return targetQueue.offerLast(id);
    }

    public Long peekFirst() {
        return targetQueue.peekFirst();
    }

    public long peekLast() {
        return targetQueue.peekLast();
    }

    @Override
    public void run() {

        int currentSize = targetQueue.size();
        //System.out.println("current size is" + currentSize);
        if (currentSize < queueMaxSize / 2) {
            Stopwatch started = Stopwatch.createStarted();
            for (int i = 0; i < queueMaxSize / 2; i++) {
                boolean b = this.putToLast(atomicLong.incrementAndGet());
                if (!b) {
                    System.out.println("定时填充deque失败");
                }
            }
            System.out.println("填充" + queueMaxSize / 2 + "个耗时:" + started.elapsed(TimeUnit.MILLISECONDS));
        }

    }


}
