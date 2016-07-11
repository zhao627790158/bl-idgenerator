package cn.blueshit.idgenerator.dao;

/**
 * Created by zhaoheng on 2016/7/11.
 * 用于操作mysql数据库
 */
public interface SequenceDao {

    /**
     * mysql自增
     *
     * @param sequenceName 队列名
     * @return
     */
    int autoIncrease(String sequenceName);


}
