package cn.blueshit.idgenerator.service.impl;

import cn.blueshit.idgenerator.service.SequenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhaoheng on 2016/7/14.
 */
public class FailoverSequenceServiceImpl implements SequenceService {

    private static final Logger logger = LoggerFactory.getLogger(FailoverSequenceServiceImpl.class);


    //先后注入两个 实现 mysql  h2cache
    private List<SequenceService> sequenceServices;

    public FailoverSequenceServiceImpl(List<SequenceService> sequenceServices) {
        this.sequenceServices = sequenceServices;
    }

    public Long getNextVal(String sequenceName) {
        Long id = null;
        StringBuilder loggerText = new StringBuilder();
        for (SequenceService sequenceService : sequenceServices) {
            try {
                id = sequenceService.getNextVal(sequenceName);
                loggerText.append(sequenceService).append("取得").append(id == null ? 0 : 1);
                if (id != null) {
                    break;
                }
            } catch (Throwable t) {
                logger.error("执行SequenceService出错：" + sequenceService, t);
            }
        }
        if (id == null) {
            logger.error("空值，获取单个值：" + sequenceName + "，从" + loggerText);
        }
        return id;
    }

    @Override
    public Long[] getBatchNextVal(String sequenceName, int count) {
        logger.info(getClass().getName() + "开始获取ID，" + sequenceName + "," + count);
        List<Long> ids = new ArrayList<Long>();
        StringBuilder loggerText = new StringBuilder();
        for (SequenceService sequenceService : sequenceServices) {
            try {
                Long[] fetchedIds = sequenceService.getBatchNextVal(sequenceName, count);
                loggerText.append(sequenceService).append("取得").append(fetchedIds == null ? 0 : fetchedIds.length);
                if (logger.isInfoEnabled()) {
                    logger.info(sequenceName + "请求" + count + "，从" + sequenceService.getClass().getSimpleName()
                            + "中取得：" + (fetchedIds == null ? 0 : fetchedIds.length));
                }
                if (fetchedIds != null) {
                    for (Long id : fetchedIds) {
                        ids.add(id);
                    }
                }
                if (ids.size() == count) {
                    break;
                } else if (ids.size() < count) {
                    count = count - ids.size();
                }
            } catch (Throwable t) {
                logger.error("执行SequenceService出错：" + sequenceService, t);
            }
        }
        if (ids.isEmpty()) {
            logger.error("空值，获取多个值：" + sequenceName + "请求" + count + "，从" + loggerText);
        }
        return ids.isEmpty() ? null : ids.toArray(new Long[ids.size()]);
    }

}
