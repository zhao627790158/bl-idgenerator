package cn.blueshit.idgenerator.util.quene;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by zhaoheng on 2016/7/13.
 * 被dbquence和 memoryquence继承的队列,实现runnable接口
 * 可以当队列长度小于某值时,动态去添加队列
 */
public abstract class AutoAddAndBoundedQueue<E extends Element> implements OmnipotentQueue<E>, Runnable {

    protected static Logger logger = LoggerFactory.getLogger(AutoAddAndBoundedQueue.class);

    //队列名称
    private String name;
    //每一个sequenceName对应的队列的最大长度
    private int queueMaxSize = 10000;
    //补充因子
    private int queueSupplyWhenLackSize = 200;
    //是否执行完毕
    private boolean initialized;
    //当从db中获取过多但是,队列已经满时,存放剩余id,用于下次补充使用
    //Deque应该是栈和队列的结合吧，（deque，全名double-ended queue）是一种具有队列和栈的性质的数据结构。双端队列中的元素可以从两端弹出，其限定插入和删除操作在表的两端进行。
    private final BlockingDeque<E> supplement = new LinkedBlockingDeque<E>();

    //源队列名称 h2 dbqueue源队列为EntryOmnipotentQueue 入口queue
    //memoryqueue源队列为
    private OmnipotentQueue<E> sourceOmnipotentQueue;

    public AutoAddAndBoundedQueue(OmnipotentQueue<E> sourceOmnipotentQueue, int queueMaxSize) {
        this.sourceOmnipotentQueue = sourceOmnipotentQueue;
        this.queueMaxSize = queueMaxSize;
    }

    @Override
    public void run() {
        try {
            int currentSize = size();
            int lackSize = queueMaxSize - currentSize;//需要补充多少
            if (lackSize >= queueSupplyWhenLackSize) {
                logger.info(sourceOmnipotentQueue.getName()+"-需要补充数为"+lackSize);
                //需要补充的 减去上次没有用完的
                int needFetchSize = lackSize - supplement.size();
                List<E> fetchedSupplement = null;
                try {
                    long fromIndex = -1L;
                    E lastElement = peekLast();
                    if (lastElement != null) {
                        fromIndex = lastElement.getIndex();
                    }
                    fetchedSupplement = sourceOmnipotentQueue.peekFromIndex(needFetchSize, fromIndex);
                } catch (Exception e) {
                    logger.error("获取补给" + name + "出错取消本次补给，下次再重试", e);
                }
                int fetchedCount = 0;
                if (fetchedSupplement != null && !fetchedSupplement.isEmpty()) {
                    fetchedCount = fetchedSupplement.size();
                    for (E element : fetchedSupplement) {
                        supplement.addLast(element);
                    }
                }
                int supplySuccessCount = 0;
                while (!supplement.isEmpty()) {
                    E element = supplement.pollFirst();
                    try {
                        //放到dbqueue或者memoryqueue中
                        if (putToLast(element)) {
                            supplySuccessCount++;
                        }
                    } catch (Exception e) {
                        logger.error("补给" + name + "出错，剩下的" + supplement.size() + "个留待下次补给使用", e);
                        break;
                    }
                }
                logger.info(
                        name + ":需要补充数据"
                                + "，当前=" + currentSize
                                + "，需要=" + lackSize
                                + "，得到=" + fetchedCount
                                + "，成功=" + supplySuccessCount);
            } else {
                initialized = true;
            }
        } catch (Throwable throwable) {
            logger.error("调度出现问题,队列名" + name, throwable);

        }
    }

    public boolean isInitialized() {
        return initialized;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setQueueSupplyWhenLackSize(int queueSupplyWhenLackSize) {
        this.queueSupplyWhenLackSize = queueSupplyWhenLackSize;
    }
}
