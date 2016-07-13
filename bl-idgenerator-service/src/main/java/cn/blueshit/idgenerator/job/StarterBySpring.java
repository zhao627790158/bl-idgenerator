package cn.blueshit.idgenerator.job;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * Created by zhaoheng on 2016/7/13.
 */
public class StarterBySpring implements ApplicationListener<ContextRefreshedEvent> {

    private Starter starter;

    public void setStarter(Starter starter) {
        this.starter = starter;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        new Thread() {
            @Override
            public void run() {
                starter.start();
            }
        }.start();
    }
}
