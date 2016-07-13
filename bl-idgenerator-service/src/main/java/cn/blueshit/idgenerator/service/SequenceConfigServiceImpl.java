package cn.blueshit.idgenerator.service;


import cn.blueshit.idgenerator.dao.SequenceDao;
import cn.blueshit.idgenerator.domain.po.Sequence;

import java.util.List;

public class SequenceConfigServiceImpl implements SequenceConfigService {

  private SequenceDao sequenceDao;

  public void setSequenceDao(SequenceDao sequenceDao) {
    this.sequenceDao = sequenceDao;
  }

  @Override
  public List<Sequence> getAllSequences() {

    return sequenceDao.getAllSequences();
  }
}
