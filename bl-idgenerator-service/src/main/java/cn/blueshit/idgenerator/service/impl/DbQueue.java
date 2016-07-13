package cn.blueshit.idgenerator.service.impl;

import cn.blueshit.idgenerator.domain.SequenceId;
import cn.blueshit.idgenerator.util.quene.AutoAddAndBoundedQueue;
import cn.blueshit.idgenerator.util.quene.OmnipotentQueue;

import javax.sql.DataSource;
import java.util.List;

/**
 * Created by zhaoheng on 2016/7/13.
 * h2 db队列
 */
public class DbQueue extends AutoAddAndBoundedQueue<SequenceId> {

    private SequenceIdDao targetSequenceIdDao;
    private String sequenceName;

    public DbQueue(OmnipotentQueue<SequenceId> sourceOmnipotentQueue,
                   int queueMaxSize,
                   String sequenceName,
                   DataSource dataSource) {
        //源队列-现在创建时传入EntryOmnipotentQueue
        super(sourceOmnipotentQueue, queueMaxSize);
        this.sequenceName = sequenceName;
        targetSequenceIdDao = new SequenceIdDao(dataSource);
        createTableIfNotExisted();
    }

    private void createTableIfNotExisted() {
        if (!targetSequenceIdDao.contains(sequenceName)) {
            try {
                targetSequenceIdDao.createTable(sequenceName);
            } catch (Exception e) {
                logger.error("创建Table和索引出错，已经存在", e);
                throw new RuntimeException("创建表：" + sequenceName + "出错", e);
            }
        }
    }
    @Override
    public int size() {
        return targetSequenceIdDao.lengthByName(sequenceName);
    }

    @Override
    public List<SequenceId> peekFromIndex(int count, long fromIndex) {
        List<SequenceId> sequenceIds = targetSequenceIdDao.findTopByName(sequenceName, fromIndex, count);
        return sequenceIds;
    }

    @Override
    public boolean putToLast(SequenceId sequenceId) {
        int effectSize = targetSequenceIdDao.insert(sequenceName, sequenceId.getId());
        return effectSize == 1 ? true : false;
    }

    public SequenceId takeFromFirst() {
        throw new UnsupportedOperationException("DB队列不支持此方法，请使用DAO单个delete");
    }

    @Override
    public boolean remove(SequenceId element) {
        int effectSize = targetSequenceIdDao.deleteById(sequenceName, element.getPk());
        return effectSize == 1 ? true : false;
    }

    /**
     * 查询当前表中最小的
     * @return
     */
    @Override
    public SequenceId peekFirst() {
        List<SequenceId> sequenceIds = targetSequenceIdDao.findTopByName(sequenceName, -1L, 1);
        return (sequenceIds != null && !sequenceIds.isEmpty()) ? sequenceIds.get(0) : null;
    }

    @Override
    public SequenceId peekLast() {
        return targetSequenceIdDao.findMaxByName(sequenceName);
    }

    @Override
    public boolean clear() {
        targetSequenceIdDao.clearByName(sequenceName);
        return true;
    }
}
