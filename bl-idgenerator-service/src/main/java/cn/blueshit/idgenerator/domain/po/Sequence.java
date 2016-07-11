package cn.blueshit.idgenerator.domain.po;

/**
 * Created by zhaoheng on 2016/7/11.
 * 对应mysql数据库
 */
public class Sequence {

    private String seqName;
    private Long currentValue;
    private Long increment;
    private Long version;

    public String getSeqName() {
        return seqName;
    }

    public void setSeqName(String seqName) {
        this.seqName = seqName;
    }

    public Long getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(Long currentValue) {
        this.currentValue = currentValue;
    }

    public Long getIncrement() {
        return increment;
    }

    public void setIncrement(Long increment) {
        this.increment = increment;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "Sequence{" +
                "seqName='" + seqName + '\'' +
                ", currentValue=" + currentValue +
                ", increment=" + increment +
                ", version=" + version +
                '}';
    }
}
