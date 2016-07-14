package cn.blueshit.idgenerator.service.impl;

import cn.blueshit.idgenerator.domain.SequenceId;
import cn.blueshit.idgenerator.util.quene.AutoAddAndBoundedQueue;
import cn.blueshit.idgenerator.util.quene.OmnipotentQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by zhaoheng on 2016/7/13.
 * java 缓存层 队列
 * 双向队列 Deque
 */
public class MemoryQueue extends AutoAddAndBoundedQueue<SequenceId> {

    private static final Logger logger = LoggerFactory.getLogger(MemoryQueue.class);

    private OmnipotentQueue<SequenceId> sourceOmnipotentQueue;

    //双向队列 Deque
    private BlockingDeque<SequenceId> targetQueue;

    public MemoryQueue(OmnipotentQueue<SequenceId> sourceOmnipotentQueue, int queueMaxSize) {
        super(sourceOmnipotentQueue, queueMaxSize);
        this.sourceOmnipotentQueue = sourceOmnipotentQueue;
        targetQueue = new LinkedBlockingDeque<SequenceId>(queueMaxSize);
    }


    @Override
    public int size() {
        int size = targetQueue.size();
        logger.info("--MemoryQueue--size--" + Thread.currentThread().getName()+"-"+sourceOmnipotentQueue.getName() + "剩余数-" + size);
        return size;
    }

    @Override
    public List<SequenceId> peekFromIndex(int count, long fromIndex) {
        logger.info("--MemoryQueue--peekFromIndex--" + Thread.currentThread().getName()+"-"+sourceOmnipotentQueue.getName()+"-count-"+count+"-fromindex-"+fromIndex);
        List<SequenceId> sequenceIds = new ArrayList<SequenceId>(count);
        long cursor = 0, fetchedCount = 0;
        for (SequenceId sequenceId : targetQueue) {
            cursor++;
            if (cursor > fromIndex && fetchedCount < count) {
                sequenceIds.add(sequenceId);
                fetchedCount++;
            }
        }
        return sequenceIds;
    }

    @Override
    public boolean putToLast(SequenceId sequenceId) {
        if(null!=sequenceId){
            logger.info("--MemoryQueue--putToLast--" + Thread.currentThread().getName()+"-"+sourceOmnipotentQueue.getName() + "id为" + sequenceId.getId() + "-px-" + sequenceId.getIndex());
        }
        return targetQueue.offerLast(sequenceId);
    }

    @Override
    public SequenceId takeFromFirst() {
        //移除blocking栈(后入先出) 第一个元素 java内存中移除一个
        SequenceId sequenceId = targetQueue.pollFirst();
        if(null!=sequenceId){
            logger.info("--MemoryQueue--takeFromFirst--" + Thread.currentThread().getName()+"-"+sourceOmnipotentQueue.getName() + "sequenceId--" + sequenceId.getId());
        }else {
            logger.info("--MemoryQueue--takeFromFirst--" + Thread.currentThread().getName()+"-"+sourceOmnipotentQueue.getName());
        }
        //删除其 源队列中的数据..这里memoryqueue里的 sourcequeue就是 Dbqueue 再将h2数据库中的值删除掉
        if (sequenceId != null && sourceOmnipotentQueue.remove(sequenceId)) {
            return sequenceId;
        }
        return null;
    }

    @Override
    public boolean remove(SequenceId sequenceId) {
        logger.info("--MemoryQueue--remove--" + Thread.currentThread().getName()+"-"+sourceOmnipotentQueue.getName() + "-id-" + sequenceId.getId() + "-pk-" + sequenceId.getIndex());
        return targetQueue.remove(sequenceId);
    }

    @Override
    public SequenceId peekFirst() {
        SequenceId sequenceId = targetQueue.peekFirst();
        if(sequenceId!=null){
            logger.info("--MemoryQueue--peekFirst--"+Thread.currentThread().getName()+"-"+sourceOmnipotentQueue.getName() + "-id-" + sequenceId.getId() + "-pk-" + sequenceId.getIndex());
        }else {
            logger.info("--MemoryQueue--peekFirst--" + Thread.currentThread().getName()+"-"+sourceOmnipotentQueue.getName());
        }
        //Retrieves, but does not remove, the first element of this deque, or returns null if this deque is empty.
        return sequenceId;
    }

    @Override
    public SequenceId peekLast() {
        SequenceId sequenceId = targetQueue.peekLast();
        if(null!=sequenceId){
            logger.info("--MemoryQueue--peekLast--"+Thread.currentThread().getName()+"-"+sourceOmnipotentQueue.getName() + "-id-" + sequenceId.getId() + "-pk-" + sequenceId.getIndex());
        }else {
            logger.info("--MemoryQueue--peekLast--" + Thread.currentThread().getName()+"-"+sourceOmnipotentQueue.getName());
        }
        //Retrieves, but does not remove, the last element of this deque, or returns null if this deque is empty.
        return sequenceId;
    }

    @Override
    public boolean clear() {
        logger.info("--MemoryQueue--clear--"+Thread.currentThread().getName()+"-"+sourceOmnipotentQueue.getName());
        targetQueue.clear();
        return true;
    }
}
