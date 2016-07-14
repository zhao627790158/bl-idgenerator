package cn.blueshit.idgenerator.service.impl;

import cn.blueshit.idgenerator.domain.SequenceId;
import cn.blueshit.idgenerator.service.SequenceService;
import cn.blueshit.idgenerator.util.quene.OmnipotentQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhaoheng on 2016/7/11.
 * 入口队列..刚启动时或者需要补充h2时,从mysql数据库中查询当前序列号
 */
public class EntryOmnipotentQueue implements OmnipotentQueue<SequenceId> {

    private static final Logger logger = LoggerFactory.getLogger(EntryOmnipotentQueue.class);


    //默认使用mysqlSequenceService
    private SequenceService sourceSequenceServcie;

    //入口队列名称: 如 主键队列 or 订单队列
    private String sequenceName;

    public EntryOmnipotentQueue(SequenceService sourceSequenceServcie, String sequenceName) {
        this.sourceSequenceServcie = sourceSequenceServcie;
        this.sequenceName = sequenceName;
    }

    @Override
    public String getName() {
        return "入口Queue";
    }

    @Override
    public int size() {
        throw new UnsupportedOperationException("入口Queue不支持此方法");
    }

    /**
     * 从给点个的 index来获取一定数量的 id
     *
     * @param count
     * @param fromIndex
     * @return
     */
    @Override
    public List<SequenceId> peekFromIndex(int count, long fromIndex) {
        logger.info("EntryQueue--peekFromIndex--"+Thread.currentThread().getName());
        //对应的是 嵌入数据库中的表属性po
        List<SequenceId> sequenceIds = new ArrayList<SequenceId>(count);
        Long[] ids = sourceSequenceServcie.getBatchNextVal(sequenceName, count);
        if (null != ids && ids.length > 0) {
            for (Long id : ids) {
                sequenceIds.add(new SequenceId(null, id));
            }
        }
        return sequenceIds;
    }

    @Override
    public boolean putToLast(SequenceId element) {
        throw new UnsupportedOperationException("入口Queue不支持此方法");
    }

    @Override
    public SequenceId takeFromFirst() {
        throw new UnsupportedOperationException("入口Queue不支持此方法");
    }

    @Override
    public boolean remove(SequenceId element) {
        throw new UnsupportedOperationException("入口Queue不支持此方法");
    }

    @Override
    public SequenceId peekFirst() {
        throw new UnsupportedOperationException("入口Queue不支持此方法");
    }

    @Override
    public SequenceId peekLast() {
        throw new UnsupportedOperationException("入口Queue不支持此方法");
    }

    @Override
    public boolean clear() {
        throw new UnsupportedOperationException("入口Queue不支持此方法");
    }
}
