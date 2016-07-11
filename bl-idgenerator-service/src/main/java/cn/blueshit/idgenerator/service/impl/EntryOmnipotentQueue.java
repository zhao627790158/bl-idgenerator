package cn.blueshit.idgenerator.service.impl;

import cn.blueshit.idgenerator.domain.SequenceId;
import cn.blueshit.idgenerator.util.quene.OmnipotentQueue;

import java.util.List;

/**
 * Created by zhaoheng on 2016/7/11.
 * 入口队列..刚启动时或者需要补充h2时,从mysql数据库中查询当前序列号
 */
public class EntryOmnipotentQueue implements OmnipotentQueue<SequenceId> {


    @Override
    public String getName() {
        return "入口Queue";
    }

    @Override
    public int size() {
        throw new UnsupportedOperationException("入口Queue不支持此方法");
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
