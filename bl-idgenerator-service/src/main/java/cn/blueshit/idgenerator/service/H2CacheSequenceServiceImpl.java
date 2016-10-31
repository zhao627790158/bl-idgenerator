package cn.blueshit.idgenerator.service;

import cn.blueshit.idgenerator.domain.SequenceId;
import cn.blueshit.idgenerator.domain.po.Sequence;
import cn.blueshit.idgenerator.job.BatchStater;
import cn.blueshit.idgenerator.job.QueueSupplierStarter;
import cn.blueshit.idgenerator.job.Starter;
import cn.blueshit.idgenerator.job.SyncStarter;
import cn.blueshit.idgenerator.service.impl.DbQueue;
import cn.blueshit.idgenerator.service.impl.EntryOmnipotentQueue;
import cn.blueshit.idgenerator.service.impl.MemoryQueue;
import cn.blueshit.idgenerator.util.ScheduledExecutorFactory;
import cn.blueshit.idgenerator.util.hash.Sharded;
import cn.blueshit.idgenerator.util.quene.OmnipotentQueue;
import cn.blueshit.idgenerator.util.quene.QueueShardInfo;
import cn.blueshit.idgenerator.util.quene.QueueShardResource;
import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by zhaoheng on 2016/7/13.
 */
public class H2CacheSequenceServiceImpl implements SequenceService, Starter {

    private static final Logger logger = LoggerFactory.getLogger(H2CacheSequenceServiceImpl.class);

    //此处注入的是mysqlsequenceServiceImpl
    private SequenceService sourceSequenceService;

    //配置相关--查询mysql库中有多个个 类型的队列: 如 user_id 或者 order_id
    private SequenceConfigService sequenceConfigService;

    /**
     * cache用到的数据源，默认是h2
     */
    private Map<String, DataSource> dataSources;

    //批量获取多条记录
    private int batchMaxSize = 500;
    // 队列集群 一致性集群 QueueShardResource数据源和其之内的所有的队列
    private Sharded<QueueShardResource, QueueShardInfo<QueueShardResource>> queueCluster;

    //用来保存所有的队列
    private List<OmnipotentQueue<SequenceId>> omnipotentQueues = new ArrayList<OmnipotentQueue<SequenceId>>();
    //随机数的长度
    private int memoryQueueShardValueSize = 128;

    //是否执行完成
    private boolean startCompleted;

    //h2 db补充启动器
    private List<Starter> dbQueueSupplierStarters = new ArrayList<Starter>();
    //memory补充启动器
    private List<Starter> memoryQueueSupplierStarters = new ArrayList<Starter>();
    //dbqueue是否自动补充
    private boolean dbQueueSupplyEnable = true;

    //多久检查一次 是否启动完成
    private int checkStarterCompletedInterval = 1000;

    //dbqueue最大长度
    private int dbQueueMaxSize = 10000;
    //小于多少补充
    private int dbQueueSupplyWhenLackSize = 100;
    //多久检查补充一次
    private int dbQueueSupplyInterval = 1000;
    //dbqueue 最大线程数
    private int dbQueueSupplyThreadSize = 50;
    //是否启动 memory自动补充
    private boolean memoryQueueSupplyEnable = true;
    //memoryqueue最大长度
    private int memoryQueueMaxSize = 10000;

    private int memoryQueueSupplyWhenLackSize = 100;

    private int memoryQueueSupplyInterval = 1000;

    private int memoryQueueSupplyThreadSize = 50;

    private int memoryQueueShardWeight = 10;

    @Override
    public Long getNextVal(String sequenceName) {
        //获取随机数来做 一致性hash
        String randomKey = RandomStringUtils.random(memoryQueueShardValueSize);
        //获取一个队列分片信息 h2数据源集群
        QueueShardResource queueShardResource = queueCluster.getShard(randomKey);
        //根据队列名获取到这个分片数据源中的队列信息 获取到里面的memoryQueue
        OmnipotentQueue<SequenceId> omnipotentQueue = queueShardResource.getOmnipotentQueues().get(sequenceName);
        if (omnipotentQueue == null) {
            throw new RuntimeException("没有找到sequence=" + sequenceName + "使用前请先配置");
        }
        SequenceId sequenceId = omnipotentQueue.takeFromFirst();
        if (LoggerFactory.getLogger("testLog").isDebugEnabled()) {
            LoggerFactory.getLogger("testLog").debug("id:{}", sequenceId.getId());
        }
        return sequenceId != null ? sequenceId.getId() : null;
    }

    /**
     * 批量获取
     *
     * @param sequenceName
     * @param count
     * @return
     */
    @Override
    public Long[] getBatchNextVal(String sequenceName, int count) {
        if (count > batchMaxSize) {
            logger.error("请求ID数量：" + count + "，超过了最大限制："
                    + batchMaxSize + "，自动设置成：" + batchMaxSize);
            count = batchMaxSize;
        }
        List<Long> ids = new ArrayList<Long>(count);
        for (int i = 0; i < count; i++) {
            Long id = getNextVal(sequenceName);
            if (id != null) {
                ids.add(id);
            }
        }
        return ids.isEmpty() ? null : ids.toArray(new Long[ids.size()]);
    }

    /**
     * 核心方法 核心
     * 入口初始化方法
     *
     * @return
     */
    @Override
    public boolean start() {
        List<Sequence> sequences = sequenceConfigService.getAllSequences();
        if (dataSources == null || dataSources.isEmpty() || sequences == null || sequences.isEmpty()) {
            throw new RuntimeException("Cache数据源和Sequence配置都不能为空");
        }
        //可能的最大线程db和memory都是一样的，即每个库每个队列一个线程
        int maybeMaxSize = dataSources.size() * sequences.size();
        //计算h2 db调度线程
        int dbThreadPoolSize = dbQueueSupplyThreadSize > 0 ? Math.min(maybeMaxSize, dbQueueSupplyThreadSize) : maybeMaxSize;
        //计算memory调度线程
        int memoryThreadPoolSize = memoryQueueSupplyThreadSize > 0 ? Math.min(maybeMaxSize, memoryQueueSupplyThreadSize) : maybeMaxSize;
        /**
         * corePoolSize - the number of threads to keep in the pool, even if they are idle
         * threadFactory - the factory to use when the executor creates a new thread
         * 创建 db  memory调度器
         */
        ScheduledExecutorService dbQueueSupplierScheduler = ScheduledExecutorFactory.getScheExeService(dbThreadPoolSize, "dbQueueSupplier");
        ScheduledExecutorService memoryQueueSupplierScheduler = ScheduledExecutorFactory.getScheExeService(memoryThreadPoolSize, "memoryQueueSupplier");
        //创建和 h2数据数量一致的 分片源信息 作为集群节点
        List<QueueShardInfo<QueueShardResource>> queueClusterNodes = new ArrayList<QueueShardInfo<QueueShardResource>>(dataSources.size());
        //遍历每一个数据源
        for (Map.Entry<String, DataSource> dsPair : dataSources.entrySet()) {
            String dsName = dsPair.getKey();
            DataSource ds = dsPair.getValue();
            //次数据源中 队列的数量 看要为几个业务线生成序列号了
            Map<String, OmnipotentQueue<SequenceId>> queuesOfDs = new HashMap<String, OmnipotentQueue<SequenceId>>(sequences.size());
            //封装 数据源 和他里面的 队列
            QueueShardResource resource = new QueueShardResource(dsName, queuesOfDs);
            //封装 队列分片源信息 h2 datasource name 和其对应的 队列信息
            QueueShardInfo<QueueShardResource> queueShardInfo = new QueueShardInfo<QueueShardResource>(memoryQueueShardWeight, resource, dsName);
            //加入集群节点
            queueClusterNodes.add(queueShardInfo);
            //开始为没一个类别提供 dbqueue 和meoryqueue
            for (Sequence sequence : sequences) {
                String sequenceName = sequence.getSeqName();
                //入口队列 sourceSequenceService注入的是 MysqlSequenceServiceImpl
                EntryOmnipotentQueue entryQueue = new EntryOmnipotentQueue(sourceSequenceService, sequenceName);
                //嵌入式h2 db队列 ds传递过去数据源..让dao直接给jdbctemplement来使用
                DbQueue dbQueue = new DbQueue(entryQueue, dbQueueMaxSize, sequenceName, ds);
                dbQueue.setName("H2-" + dsName + "-" + sequenceName);
                dbQueue.setQueueSupplyWhenLackSize(dbQueueSupplyWhenLackSize);
                //加入队列方便管理
                omnipotentQueues.add(dbQueue);
                //内存队列
                MemoryQueue memoryQueue = new MemoryQueue(dbQueue, memoryQueueMaxSize);
                memoryQueue.setName("MEMORY-" + dsName + "-" + sequenceName);
                memoryQueue.setQueueSupplyWhenLackSize(memoryQueueSupplyWhenLackSize);
                //加入队列方便管理，只加入队列链的最后一个节点即可
                omnipotentQueues.add(memoryQueue);
                //加入启动器，DB和Memory放入到启动链中，实现先后同步启动
                if (dbQueueSupplyEnable) {
                    Starter dbStarter = new QueueSupplierStarter(dbQueue, dbQueueSupplierScheduler, dbQueueSupplyInterval);
                    //一个 数据源 一个sequneceName 一个 线程 使用dbQueueSupplierScheduler
                    dbQueueSupplierStarters.add(dbStarter);
                }
                if (memoryQueueSupplyEnable) {
                    Starter memoryStarter = new QueueSupplierStarter(memoryQueue, memoryQueueSupplierScheduler, memoryQueueSupplyInterval);
                    //一个 数据源 一个sequneceName 一个 线程,使用memoryQueueSupplierScheduler
                    memoryQueueSupplierStarters.add(memoryStarter);
                }
                //memoryQueue 再加入全局启动器中 加入集群节点
                queuesOfDs.put(sequenceName, memoryQueue);
            }
        }
        //创建队列集群，使用一致性哈希算法
        queueCluster = new Sharded<QueueShardResource, QueueShardInfo<QueueShardResource>>(queueClusterNodes);
        //不db和memory两类补充线程进行整体同步执行
        List<Starter> synStarters = new ArrayList<Starter>();
        synStarters.add(new BatchStater(dbQueueSupplierStarters) {
            @Override
            public String toString() {
                return "所有Db队列";
            }
        });
        synStarters.add(new BatchStater(memoryQueueSupplierStarters) {
            @Override
            public String toString() {
                return "所有Memory队列";
            }
        });
        //批量同步去执行
        new SyncStarter(synStarters, checkStarterCompletedInterval).start();
        //执行完毕
        startCompleted = true;
        return true;
    }

    @Override
    public boolean startCompleted() {
        return startCompleted;
    }

    public List<OmnipotentQueue<SequenceId>> geQueues() {
        return Collections.unmodifiableList(omnipotentQueues);
    }


    public void setMemoryQueueShardWeight(int memoryQueueShardWeight) {
        this.memoryQueueShardWeight = memoryQueueShardWeight;
    }

    public void setMemoryQueueShardValueSize(int memoryQueueShardValueSize) {
        this.memoryQueueShardValueSize = memoryQueueShardValueSize;
    }

    public void setMemoryQueueSupplyThreadSize(int memoryQueueSupplyThreadSize) {
        this.memoryQueueSupplyThreadSize = memoryQueueSupplyThreadSize;
    }

    public void setMemoryQueueSupplyWhenLackSize(int memoryQueueSupplyWhenLackSize) {
        this.memoryQueueSupplyWhenLackSize = memoryQueueSupplyWhenLackSize;
    }

    public void setMemoryQueueMaxSize(int memoryQueueMaxSize) {
        this.memoryQueueMaxSize = memoryQueueMaxSize;
    }

    public void setDbQueueSupplyThreadSize(int dbQueueSupplyThreadSize) {
        this.dbQueueSupplyThreadSize = dbQueueSupplyThreadSize;
    }

    public void setDbQueueSupplyWhenLackSize(int dbQueueSupplyWhenLackSize) {
        this.dbQueueSupplyWhenLackSize = dbQueueSupplyWhenLackSize;
    }

    public void setDbQueueMaxSize(int dbQueueMaxSize) {
        this.dbQueueMaxSize = dbQueueMaxSize;
    }

    public void setDbQueueSupplyEnable(boolean dbQueueSupplyEnable) {
        this.dbQueueSupplyEnable = dbQueueSupplyEnable;
    }

    public void setCheckStarterCompletedInterval(int checkStarterCompletedInterval) {
        this.checkStarterCompletedInterval = checkStarterCompletedInterval;
    }

    public void setSequenceConfigService(SequenceConfigService sequenceConfigService) {
        this.sequenceConfigService = sequenceConfigService;
    }

    public void setDataSources(Map<String, DataSource> dataSources) {
        this.dataSources = dataSources;
    }

    public void setSourceSequenceService(SequenceService sourceSequenceService) {
        this.sourceSequenceService = sourceSequenceService;
    }

    public void setBatchMaxSize(int batchMaxSize) {
        this.batchMaxSize = batchMaxSize;
    }

    public void setMemoryQueueSupplyInterval(int memoryQueueSupplyInterval) {
        this.memoryQueueSupplyInterval = memoryQueueSupplyInterval;
    }

    public void setDbQueueSupplyInterval(int dbQueueSupplyInterval) {
        this.dbQueueSupplyInterval = dbQueueSupplyInterval;
    }

    public void setMemoryQueueSupplyEnable(boolean memoryQueueSupplyEnable) {
        this.memoryQueueSupplyEnable = memoryQueueSupplyEnable;
    }
}
