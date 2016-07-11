package cn.blueshit.idgenerator.util.quene;

import cn.blueshit.idgenerator.domain.SequenceId;

import java.util.Arrays;
import java.util.Map;

/**
 * Created by zhaoheng on 2016/7/11.
 * 数据源 和其对应的 队列(可能是多个,主要看有几个sequenceName)
 */
public class QueueShardResource {
    private String dataSourceName;
    private Map<String, OmnipotentQueue<SequenceId>> omnipotentQueues;

    public QueueShardResource(String dataSourceName, Map<String, OmnipotentQueue<SequenceId>> omnipotentQueues) {
        this.dataSourceName = dataSourceName;
        this.omnipotentQueues = omnipotentQueues;
    }

    public String getDataSourceName() {
        return dataSourceName;
    }

    public void setDataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
    }

    public Map<String, OmnipotentQueue<SequenceId>> getOmnipotentQueues() {
        return omnipotentQueues;
    }

    public void setOmnipotentQueues(Map<String, OmnipotentQueue<SequenceId>> omnipotentQueues) {
        this.omnipotentQueues = omnipotentQueues;
    }

    @Override
    public String toString() {
        return "ShardResource{" +
                "dataSourceName='" + dataSourceName + '\'' +
                ", omnipotentQueues=" + Arrays.toString(omnipotentQueues.keySet().toArray()) +
                '}';
    }

}
