package cn.blueshit.idgenerator.job;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhaoheng on 2016/7/13.
 */
public class BatchStater implements Starter {

    private static final Logger logger = LoggerFactory.getLogger(BatchStater.class);

    private List<Starter> starters;

    private List<Starter> uncompletedStarters = new ArrayList<Starter>();

    public BatchStater(List<Starter> starters) {
        this.starters = starters;
    }

    @Override
    public boolean start() {
        for (Starter starter : starters) {
            if (starter.start()) {
                uncompletedStarters.add(starter);
            } else {
                logger.error("Starter=" + starter + "启动失败");
                throw new RuntimeException("Starter=" + starter + "启动失败");
            }
        }
        return true;
    }

    @Override
    public boolean startCompleted() {
        for (; ; ) {
            if (uncompletedStarters.isEmpty()) {
                return true;
            }
            for (Starter starter : uncompletedStarters) {
                if (starter.startCompleted()) {
                    uncompletedStarters.remove(starter);
                    break;
                } else {
                    return false;
                }
            }
        }
    }


}
