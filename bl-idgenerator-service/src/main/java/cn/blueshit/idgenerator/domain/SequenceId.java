package cn.blueshit.idgenerator.domain;

import cn.blueshit.idgenerator.util.quene.Element;

/**
 * Created by zhaoheng on 2016/7/11.
 * 对应嵌入式数据库
 */
public class SequenceId implements Element {
    //h2主键
    private Long pk;
    //将要生成的主键值
    private Long id;

    public SequenceId() {
    }

    public SequenceId(Long pk, Long id) {
        this.pk = pk;
        this.id = id;
    }

    public Long getPk() {
        return pk;
    }

    public void setPk(Long pk) {
        this.pk = pk;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SequenceId that = (SequenceId) o;

        if (!id.equals(that.id)) return false;
        if (!pk.equals(that.pk)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = pk.hashCode();
        result = 31 * result + id.hashCode();
        return result;
    }

    @Override
    public long getIndex() {
        return pk;
    }
}
