package cn.blueshit.idgenerator.service.impl;

import cn.blueshit.idgenerator.domain.SequenceId;
import cn.blueshit.idgenerator.util.quene.AutoAddAndBoundedQueue;
import cn.blueshit.idgenerator.util.quene.OmnipotentQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.List;

/**
 * Created by zhaoheng on 2016/7/13.
 * h2 db队列
 */
public class DbQueue extends AutoAddAndBoundedQueue<SequenceId> {

    private static final Logger logger = LoggerFactory.getLogger(DbQueue.class);

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
        logger.info("DbQueue-createTableIfNotExisted--创建表"+Thread.currentThread().getName());
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
        logger.info("DbQueue-size--给定的数据源的队列大小"+Thread.currentThread().getName());
        return targetSequenceIdDao.lengthByName(sequenceName);
    }

    @Override
    public List<SequenceId> peekFromIndex(int count, long fromIndex) {
        logger.info("DbQueue-peekFromIndex--"+Thread.currentThread().getName());
        List<SequenceId> sequenceIds = targetSequenceIdDao.findTopByName(sequenceName, fromIndex, count);
        return sequenceIds;
    }

    @Override
    public boolean putToLast(SequenceId sequenceId) {
        logger.info("DbQueue-putToLast--"+Thread.currentThread().getName());
        int effectSize = targetSequenceIdDao.insert(sequenceName, sequenceId.getId());
        return effectSize == 1 ? true : false;
    }

    public SequenceId takeFromFirst() {
        logger.info("DbQueue-takeFromFirst--"+Thread.currentThread().getName());
        throw new UnsupportedOperationException("DB队列不支持此方法，请使用DAO单个delete");
    }

    @Override
    public boolean remove(SequenceId element) {
        logger.info("DbQueue-remove--"+Thread.currentThread().getName());
        int effectSize = targetSequenceIdDao.deleteById(sequenceName, element.getPk());
        return effectSize == 1 ? true : false;
    }

    /**
     * 查询当前表中最小的
     * @return
     */
    @Override
    public SequenceId peekFirst() {
        logger.info("DbQueue-peekFirst--"+Thread.currentThread().getName());
        List<SequenceId> sequenceIds = targetSequenceIdDao.findTopByName(sequenceName, -1L, 1);
        return (sequenceIds != null && !sequenceIds.isEmpty()) ? sequenceIds.get(0) : null;
    }

    @Override
    public SequenceId peekLast() {
        logger.info("DbQueue-peekLast--"+Thread.currentThread().getName());
        return targetSequenceIdDao.findMaxByName(sequenceName);
    }

    @Override
    public boolean clear() {
        logger.info("DbQueue-clear--"+Thread.currentThread().getName());
        targetSequenceIdDao.clearByName(sequenceName);
        return true;
    }
}
