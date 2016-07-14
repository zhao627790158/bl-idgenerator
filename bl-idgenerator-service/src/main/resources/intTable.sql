DROP TABLE  bl_sequence;
CREATE TABLE bl_sequence (
  seqName varchar(50) NOT NULL comment '序列名称',
  currentValue int(11) NOT NULL comment '当前序列值',
  increment int(11) NOT NULL DEFAULT '1' comment '序列增量',
  version int(11) NOT  NULL  comment '版本号',
  PRIMARY KEY (seqName),
  UNIQUE KEY name (seqName)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 comment='序列表，表自动生产主键';

INSERT INTO sequence(seqName,currentValue,increment) VALUES ('seq_orderinfo',1,1);

