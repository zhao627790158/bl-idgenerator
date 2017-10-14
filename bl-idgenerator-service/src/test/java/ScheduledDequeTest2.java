import cn.blueshit.idgenerator.util.ScheduledExecutorFactory;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by zhaoheng on 17/10/14.
 */
public class ScheduledDequeTest2 {


    public ScheduledExecutorService memoryQueueSupplierScheduler = ScheduledExecutorFactory.getScheExeService(10, "memoryMode");
    private ExecutorService executorService = Executors.newFixedThreadPool(10);

    @Test
    public void test1() throws Exception {
        for (; ; ) {
            System.out.println(new Random().nextInt(10));
            Thread.currentThread().sleep(100);
        }
    }


    @Test
    public synchronized void test() throws Exception {
        Stopwatch started = Stopwatch.createStarted();
        int dequeCount = 10;
        List<ScheduledDeque2> scheduledDequeList = Lists.newArrayList();
        for (int i = 0; i < dequeCount; i++) {
            ScheduledDeque2 scheduledDeque2 = new ScheduledDeque2(i, dequeCount, 200000);
            scheduledDequeList.add(scheduledDeque2);
        }
        System.out.println(started.elapsed(TimeUnit.MILLISECONDS));

        for (int i = 0; i < 10; i++) {
            final ScheduledDeque2 scheduledDeque2 = scheduledDequeList.get(i);
            memoryQueueSupplierScheduler.scheduleAtFixedRate(scheduledDeque2, 0, 10, TimeUnit.MILLISECONDS);
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    Stopwatch started = Stopwatch.createStarted();
                    while (true) {
                        for (int j = 2; j >= 0; j--) {
                            Long aLong = scheduledDeque2.takeFromFirst();
                            if (null == aLong) {
                                System.out.println(j + ":拿完耗时:" + started.elapsed(TimeUnit.MILLISECONDS));
                                started.reset();
                                try {
                                    Thread.sleep(100);
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
        }
        while (true) {
            this.wait(2000);
            for (ScheduledDeque2 scheduledDeque2 : scheduledDequeList) {
                System.out.println("peekFirst:" + scheduledDeque2.peekFirst() + " peekLast:" + scheduledDeque2.peekLast());
            }
        }

    }
}


class ScheduledDeque2 implements Runnable {

    //双向队列 Deque
    private final BlockingDeque<Long> targetQueue;

    private final int queueMaxSize;

    private volatile AtomicLong atomicLong;

    private final long step;

    public ScheduledDeque2(long start, long step, int queueMaxSize) {
        this.targetQueue = new LinkedBlockingDeque<Long>(queueMaxSize);
        this.queueMaxSize = queueMaxSize;
        this.atomicLong = new AtomicLong(start);
        this.step = step;
        init();
    }

    private void init() {
        for (int i = 0; i < queueMaxSize; i++) {
            boolean b = this.putToLast(atomicLong.getAndAdd(step));
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

    public Long peekLast() {
        return targetQueue.peekLast();
    }

    @Override
    public void run() {

        int currentSize = targetQueue.size();
        //System.out.println("current size is" + currentSize);
        if (currentSize < queueMaxSize / 2) {
            Stopwatch started = Stopwatch.createStarted();
            for (int i = 0; i < queueMaxSize / 2; i++) {
                boolean b = this.putToLast(atomicLong.getAndAdd(step));
                if (!b) {
                    System.out.println("定时填充deque失败");
                }
            }
            System.out.println("填充" + queueMaxSize / 2 + "个耗时:" + started.elapsed(TimeUnit.MILLISECONDS));
        }

    }


}
