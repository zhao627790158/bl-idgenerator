package cn.blueshit.idgenerator.service;

/**
 * Created by zhaoheng on 2016/7/11.
 * id生产者顶级接口
 */
public interface SequenceService {

    Long getNextVal(String sequenceName);

    Long[] getBatchNextVal(String sequenceName, int count);
}
