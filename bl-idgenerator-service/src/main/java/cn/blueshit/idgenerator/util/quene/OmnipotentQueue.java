package cn.blueshit.idgenerator.util.quene;

import java.util.List;

/**
 * Created by zhaoheng on 2016/7/11.
 * 队列的顶级接口
 * dbquence->H2
 * memoryquence-->linkedblockquence
 */
public interface OmnipotentQueue<E extends Element> {


    //队列名称
    String getName();

    //队列大小..EntryOmnipotentQueue入口quence不支持
    int size();

    List<E> peekFromIndex(int count, long fromIndex);

    boolean putToLast(E element);

    E takeFromFirst();

    boolean remove(E element);

    E peekFirst();

    E peekLast();

    boolean clear();


}
