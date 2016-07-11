package cn.blueshit.idgenerator.service;

import cn.blueshit.idgenerator.dao.SequenceDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by zhaoheng on 2016/7/11.
 * 操作mysql数据库,每一个方法必须有事务,否则并发是可能会出现id生成重复的问题!
 * 事务必须!
 */
public class MysqlSequenceServiceImpl implements SequenceService {

    private static Logger logger = LoggerFactory.getLogger(MysqlSequenceServiceImpl.class);


    /**
     * Mysql分库个数
     */
    private int shardSize;

    //默认最大批量添加数量
    private int batchMaxSize = 1000;


    private SequenceDao sequenceDao;

    public void setSequenceDao(SequenceDao sequenceDao) {
        this.sequenceDao = sequenceDao;
    }


    /**
     * Mysql实现 id序列增加
     * 在memory 获取失败时走此方法
     *
     * @param sequenceName
     * @return
     */
    @Override
    public Long getNextVal(String sequenceName) {
        Long val = null;
        int changeCount = sequenceDao.autoIncrease(sequenceName);
        if (changeCount == 1) {
            val = sequenceDao.getCurrVal(sequenceName);
        }
        return val;
    }

    /**
     * 批量获取一定数量的id用来填充h2数据库
     * 一定要有事务
     *
     * @param sequenceName
     * @param count
     * @return
     */
    @Override
    public Long[] getBatchNextVal(String sequenceName, int count) {
        if (count > batchMaxSize) {
            logger.error("请求生成id数量超过最大限制,自动设置为" + batchMaxSize);
            count = batchMaxSize;
        }
        //例: count=10 shardsize=4
        int increment = shardSize * count;//增加后的值 需要乘以 分表的数量
        //因为事务的存在 这里先update再select 不会出现并发问题-->where条件后一定要带更新列的主键,主键行锁,非主键表锁
        int changeCount = sequenceDao.increase(sequenceName, increment);
        Long lastVal = null;
        if (changeCount > 0) {
            lastVal = sequenceDao.getCurrVal(sequenceName);
        }
        Long[] batchVal = null;
        if (lastVal != null) {
            batchVal = new Long[count];
            for (int i = count - 1, j = 0; i >= 0; i--) {
                //例: count=10 shardsize=4
                //40 36 32 28 24 20.............
                batchVal[j++] = lastVal - shardSize * i;
            }
        }
        return batchVal;
    }


    public int getShardSize() {
        return shardSize;
    }

    public void setShardSize(int shardSize) {
        this.shardSize = shardSize;
    }

    public int getBatchMaxSize() {
        return batchMaxSize;
    }

    public void setBatchMaxSize(int batchMaxSize) {
        this.batchMaxSize = batchMaxSize;
    }

    public SequenceDao getSequenceDao() {
        return sequenceDao;
    }
}
