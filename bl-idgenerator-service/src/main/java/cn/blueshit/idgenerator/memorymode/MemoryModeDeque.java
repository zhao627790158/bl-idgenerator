package cn.blueshit.idgenerator.memorymode;

import cn.blueshit.idgenerator.domain.SequenceId;
import cn.blueshit.idgenerator.util.quene.AutoAddAndBoundedQueue;
import cn.blueshit.idgenerator.util.quene.OmnipotentQueue;

import java.util.List;

/**
 * Created by zhaoheng on 17/10/14.
 */
public class MemoryModeDeque extends AutoAddAndBoundedQueue<SequenceId> {

    public MemoryModeDeque(OmnipotentQueue<SequenceId> sourceOmnipotentQueue, int queueMaxSize) {
        super(sourceOmnipotentQueue, queueMaxSize);
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public List<SequenceId> peekFromIndex(int count, long fromIndex) {
        return null;
    }

    @Override
    public boolean putToLast(SequenceId element) {
        return false;
    }

    @Override
    public SequenceId takeFromFirst() {
        return null;
    }

    @Override
    public boolean remove(SequenceId element) {
        return false;
    }

    @Override
    public SequenceId peekFirst() {
        return null;
    }

    @Override
    public SequenceId peekLast() {
        return null;
    }

    @Override
    public boolean clear() {
        return false;
    }
}
