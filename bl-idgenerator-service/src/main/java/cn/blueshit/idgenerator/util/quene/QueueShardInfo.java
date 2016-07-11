package cn.blueshit.idgenerator.util.quene;

import cn.blueshit.idgenerator.util.hash.ShardInfo;

/**
 * Created by zhaoheng on 2016/7/11.
 * 队列分片信息
 */
public class QueueShardInfo<T> extends ShardInfo<T> {

    private T resource;

    private String name;

    public QueueShardInfo(int weight, T resource, String name) {
        super(weight);
        this.resource = resource;
        this.name = name;
    }

    @Override
    protected T createResource() {
        return resource;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "QueueShardInfo{" +
                "resource=" + resource +
                ", name='" + name + '\'' +
                '}';
    }
}
