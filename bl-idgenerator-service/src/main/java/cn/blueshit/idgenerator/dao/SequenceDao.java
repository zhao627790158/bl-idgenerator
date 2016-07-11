package cn.blueshit.idgenerator.dao;

import cn.blueshit.idgenerator.domain.po.Sequence;

import java.util.List;

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


    /**
     * 批量增加
     * @param sequenceName
     * @param increment
     * @return
     */
    int increase( String sequenceName, int increment);


    Long getCurrVal(String sequenceName);

    List<Sequence> getAllSequences();



}
