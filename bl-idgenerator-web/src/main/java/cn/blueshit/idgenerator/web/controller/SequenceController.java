package cn.blueshit.idgenerator.web.controller;

import cn.blueshit.idgenerator.service.SequenceService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

/**
 * Created by zhaoheng on 2016/7/11.
 */
@Controller
@RequestMapping(value = "bl/seq")
public class SequenceController {


    @Resource(name = "failoverSequenceService")
    private SequenceService sequenceService;

    @RequestMapping(value = "getNextVal")
    @ResponseBody
    public Long getNextVal(String sequenceName) throws Exception {
        if (sequenceName == null || sequenceName.isEmpty()) {
            throw new RuntimeException("请传入正确的sequence名称");
        }
        return sequenceService.getNextVal(sequenceName);
    }


    @RequestMapping(value = "getBatchNextVal")
    @ResponseBody
    public Long[] getBatchNextVal(String sequenceName, int sequenceCount) throws Exception {
        if (sequenceName == null || sequenceName.isEmpty()) {
            throw new RuntimeException("请传入正确的sequence名称和数量");
        }
        return sequenceService.getBatchNextVal(sequenceName, sequenceCount);
    }


}
