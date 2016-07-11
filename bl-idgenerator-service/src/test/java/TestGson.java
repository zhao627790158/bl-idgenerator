import cn.blueshit.idgenerator.domain.po.Sequence;
import cn.zh.blueshit.common.GsonUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by zhaoheng on 2016/7/11.
 */
public class TestGson {
    private static final Logger log = LoggerFactory.getLogger(TestGson.class);

    @Test
    public void testGson() {
        Sequence sequence = new Sequence();
        sequence.setCurrentValue(1000L);
        log.error(GsonUtils.toJson(sequence));
    }
}
