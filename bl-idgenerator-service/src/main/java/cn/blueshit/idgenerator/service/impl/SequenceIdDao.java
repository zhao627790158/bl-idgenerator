package cn.blueshit.idgenerator.service.impl;

import cn.blueshit.idgenerator.domain.SequenceId;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;


public class SequenceIdDao {

    private JdbcTemplate jdbcTemplate;

    public SequenceIdDao(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public boolean contains(String tableName) {
        int count = jdbcTemplate.queryForInt(
                "select count(*) from information_schema.tables " +
                        " where table_schema='PUBLIC' and table_name='" + tableName.toUpperCase() + "';");
        return count == 1 ? true : false;
    }

    public int createTable(String sequenceName) {
        return jdbcTemplate.update(
                "create table " + sequenceName + "(pk bigint auto_increment(1,1) primary key, id bigint);");
    }

    public int insert(String sequenceName, Long id) {
        return jdbcTemplate.update("insert into " + sequenceName + "(id) values(" + id + ")");
    }

    public int deleteById(String sequenceName, Long pk) {
        return jdbcTemplate.update("delete from " + sequenceName + " where pk= " + pk);
    }

    public int lengthByName(String sequenceName) {
        return jdbcTemplate.queryForInt("select count(*) from " + sequenceName);
    }

    public List<SequenceId> findTopByName(String sequenceName, Long startPk, long count) {
        return jdbcTemplate.query(
                "select * from " + sequenceName + " where pk > " + startPk + " order by pk limit " + count,
                new RowMapper<SequenceId>() {
                    @Override
                    public SequenceId mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return new SequenceId(rs.getLong(1), rs.getLong(2));
                    }
                });
    }

    public int clearByName(String sequenceName) {
        return jdbcTemplate.update("truncate table " + sequenceName);
    }

    public SequenceId findMaxByName(String sequenceName) {
        List<SequenceId> sequenceIds = jdbcTemplate.query(
                "select * from " + sequenceName + " where pk = (select max(pk) from " + sequenceName + ")",
                new RowMapper<SequenceId>() {

                    @Override
                    public SequenceId mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return new SequenceId(rs.getLong(1), rs.getLong(2));
                    }
                });
        if (sequenceIds != null && !sequenceIds.isEmpty()) {
            return sequenceIds.get(0);
        }
        return null;
    }

}