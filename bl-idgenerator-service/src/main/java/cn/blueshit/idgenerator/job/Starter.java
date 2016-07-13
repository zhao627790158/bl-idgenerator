package cn.blueshit.idgenerator.job;

/**
 * Created by zhaoheng on 2016/7/13.
 * 入口类为 StarterBySpring
 */
public interface Starter {
    //启动
    boolean start();

    //是否启动完成  会线性启动
    boolean startCompleted();

}
