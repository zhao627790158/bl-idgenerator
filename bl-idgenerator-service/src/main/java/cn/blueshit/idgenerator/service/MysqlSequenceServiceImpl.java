package cn.blueshit.idgenerator.service;

import cn.blueshit.idgenerator.dao.SequenceDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * Created by zhaoheng on 2016/7/11.
 * 操作mysql数据库,每一个方法必须有事务,否则并发是可能会出现id生成重复的问题!
 * 事务必须!
 */
public class MysqlSequenceServiceImpl implements SequenceService {

    private static Logger logger = LoggerFactory.getLogger(MysqlSequenceServiceImpl.class);

    private PlatformTransactionManager transactionManager;

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
     * 定义事物
     */
    protected TransactionStatus initTansactionStatus(PlatformTransactionManager transactionManager, int propagetion) {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(propagetion);
        return transactionManager.getTransaction(def);
    }



    /**
     * Mysql实现 id序列增加
     * 在memory 获取失败时走此方法
     *事务必须
     * @param sequenceName
     * @return
     */
    @Override
    public Long getNextVal(String sequenceName) {
        TransactionStatus transactionStatus = null;
        Long val = null;
        try {
            transactionStatus = this.initTansactionStatus(transactionManager, TransactionDefinition.PROPAGATION_REQUIRED);
            int changeCount = sequenceDao.autoIncrease(sequenceName);
            if (changeCount == 1) {
                val = sequenceDao.getCurrVal(sequenceName);
            }
            transactionManager.commit(transactionStatus);
        } catch (Exception e) {
            transactionManager.rollback(transactionStatus);
            logger.error("Mysql实现 id序列增加出错,exception:{}", e.getMessage());
        }
        return val;
    }

    /**
     * 批量获取一定数量的id用来填充h2数据库
     * 一定要有事务
     * @param sequenceName
     * @param count
     * @return
     */
    @Override
    public Long[] getBatchNextVal(String sequenceName, int count) {
        TransactionStatus transactionStatus = null;
        Long lastVal = null;
        //========================================
        //可以尝试用threadlock防不同的数据源,不同的数据源有不同的transactionManager
        //根据数据源来获取相应的transactionmanager来手动管理事务
        //========================================
        if (count > batchMaxSize) {
            logger.error("请求生成id数量超过最大限制,自动设置为" + batchMaxSize);
            count = batchMaxSize;
        }
        //例: count=10 shardsize=4
        int increment = shardSize * count;//增加后的值 需要乘以 分表的数量
        //因为事务的存在 这里先update再select 不会出现并发问题-->where条件后一定要带更新列的主键,主键行锁,非主键表锁
        try {
            transactionStatus = this.initTansactionStatus(transactionManager, TransactionDefinition.PROPAGATION_REQUIRED);
            int changeCount = sequenceDao.increase(sequenceName, increment);
            if (changeCount > 0) {
                lastVal = sequenceDao.getCurrVal(sequenceName);
            }
            transactionManager.commit(transactionStatus);
        } catch (Exception ex) {
            transactionManager.rollback(transactionStatus);
            logger.error("Mysql实现 id序列getBatchNextVal出错,exception:{}", ex.getMessage());
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

    public PlatformTransactionManager getTransactionManager() {
        return transactionManager;
    }

    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }
}
