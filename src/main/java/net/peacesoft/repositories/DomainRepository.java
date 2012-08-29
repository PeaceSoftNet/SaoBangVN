package net.peacesoft.repositories;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import net.peacesoft.domain.DomainBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

/**
 *
 * @author Tran Anh Tuan <tuanta2@peacesoft.net>
 */
public class DomainRepository {

    private DataSource dataSource;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    private JdbcTemplate jdbcTemplate;

    private JdbcTemplate getJdbcTemplate() throws Exception {
        jdbcTemplate = new JdbcTemplate(dataSource);
        return jdbcTemplate;
    }

    public int save(DomainBean bean) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO DOMAIN (ID, DATE, CATEGORY, NAME)");
        sql.append("VALUES ( ?, ?, ?, ?)");
        return getJdbcTemplate().update(
                sql.toString(),
                new Object[]{bean.id, bean.date,
                    bean.category, bean.name});
    }

    public int updateById(long oldId, long newId) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE DOMAIN SET ID = ?");
        sql.append(" WHERE ID = ?");
        return getJdbcTemplate().update(
                sql.toString(),
                new Object[]{newId, oldId});
    }

    public int updateStatusById(long id) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE DOMAIN SET STATUS = 1");
        sql.append(" WHERE ID = ?");
        return getJdbcTemplate().update(
                sql.toString(),
                new Object[]{id});
    }

    public int delete(long id) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("DELETE FROM DOMAIN");
        sql.append(" WHERE ID = ?");
        return getJdbcTemplate().update(
                sql.toString(),
                new Object[]{id});
    }

    public DomainBean getById(long id) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ID, DATE, CATEGORY, NAME FROM DOMAIN"
                + " WHERE ID = ?");

        RowMapper<DomainBean> mapper = new RowMapper<DomainBean>() {
            @Override
            public DomainBean mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                DomainBean mtbean = new DomainBean();
                mtbean.id = rs.getLong("ID");
                mtbean.date = rs.getString("DATE");
                mtbean.category = rs.getString("CATEGORY");
                mtbean.name = rs.getString("NAME");
                return mtbean;
            }
        };
        return getJdbcTemplate().queryForObject(sql.toString(),
                new Object[]{id}, mapper);
    }

    public List<DomainBean> getByName(String name) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ID, DATE, CATEGORY, NAME FROM DOMAIN"
                + " WHERE NAME = ?");

        RowMapper<DomainBean> mapper = new RowMapper<DomainBean>() {
            @Override
            public DomainBean mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                DomainBean mtbean = new DomainBean();
                mtbean.id = rs.getLong("ID");
                mtbean.date = rs.getString("DATE");
                mtbean.category = rs.getString("CATEGORY");
                mtbean.name = rs.getString("NAME");
                return mtbean;
            }
        };
        return getJdbcTemplate().query(sql.toString(),
                new Object[]{name}, mapper);
    }

    public List<DomainBean> getAll() throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ID, DATE, CATEGORY, NAME FROM DOMAIN WHERE STATUS = 0");

        RowMapper<DomainBean> mapper = new RowMapper<DomainBean>() {
            @Override
            public DomainBean mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                DomainBean mtbean = new DomainBean();
                mtbean.id = rs.getLong("ID");
                mtbean.date = rs.getString("DATE");
                mtbean.category = rs.getString("CATEGORY");
                mtbean.name = rs.getString("NAME");
                return mtbean;
            }
        };
        return getJdbcTemplate().query(sql.toString(), mapper);
    }
}
