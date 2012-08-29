package net.peacesoft.repositories;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import net.peacesoft.domain.MetaBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

/**
 *
 * @author Tran Anh Tuan <tuanta2@peacesoft.net>
 */
public class MetaRepository {

    private DataSource dataSource;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    private JdbcTemplate jdbcTemplate;

    private JdbcTemplate getJdbcTemplate() throws Exception {
        jdbcTemplate = new JdbcTemplate(dataSource);
        return jdbcTemplate;
    }

    public int save(MetaBean bean) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO META (ID, DOMAIN_ID, TITLE, DES, IMAGE, TIME, SOURCE_TIME, URL)");
        sql.append("VALUES ( ?, ?, ?, ?, ?, ?, ?, ?)");
        return getJdbcTemplate().update(
                sql.toString(),
                new Object[]{bean.remoteid, bean.domainId, bean.title, bean.des, bean.image, bean.time, bean.sourceTime, bean.url});
    }

    public int updateByIDAndDomain(long id, long domainNewId) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE META SET DOMAIN_ID = ?");
        sql.append(" WHERE ID = ?");
        return getJdbcTemplate().update(
                sql.toString(),
                new Object[]{domainNewId, id});
    }
    
    public int updateByIDAndTitleAndDescription(long id, String title, String description) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE META SET TITLE = ?, DES = ?");
        sql.append(" WHERE ID = ?");
        return getJdbcTemplate().update(
                sql.toString(),
                new Object[]{title, description, id});
    }
    
    public int updateByDomain(long oldId, long newId) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE META SET DOMAIN_ID = ?");
        sql.append(" WHERE DOMAIN_ID = ?");
        return getJdbcTemplate().update(
                sql.toString(),
                new Object[]{newId, oldId});
    }

    public int updateByStatus(long id) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE META SET STATUS = 1");
        sql.append(" WHERE ID = ?");
        return getJdbcTemplate().update(
                sql.toString(),
                new Object[]{id});
    }

    public MetaBean getById(long id) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ID, DOMAIN_ID, TITLE, DES, IMAGE, TIME, SOURCE_TIME, URL, STATUS FROM META"
                + " WHERE ID = ?");

        RowMapper<MetaBean> mapper = new RowMapper<MetaBean>() {

            @Override
            public MetaBean mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                MetaBean mobean = new MetaBean();
                mobean.id = rs.getLong("ID");
                mobean.remoteid = mobean.id;
                mobean.domainId = rs.getLong("DOMAIN_ID");
                mobean.title = rs.getString("TITLE");
                mobean.des = rs.getString("DES");
                mobean.image = rs.getString("IMAGE");
                mobean.time = rs.getString("TIME");
                mobean.sourceTime = rs.getString("SOURCE_TIME");
                mobean.url = rs.getString("URL");
                mobean.status = rs.getBoolean("STATUS");
                return mobean;
            }
        };
        return getJdbcTemplate().queryForObject(sql.toString(),
                new Object[]{id}, mapper);
    }

    public List<MetaBean> getByURLAndDomain(String url, long domainid) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ID, DOMAIN_ID, TITLE, DES, IMAGE, TIME, SOURCE_TIME, URL, STATUS FROM META"
                + " WHERE URL = ? AND DOMAIN_ID = ?");

        RowMapper<MetaBean> mapper = new RowMapper<MetaBean>() {

            @Override
            public MetaBean mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                MetaBean mobean = new MetaBean();
                mobean.id = rs.getLong("ID");
                mobean.remoteid = mobean.id;
                mobean.domainId = rs.getLong("DOMAIN_ID");
                mobean.title = rs.getString("TITLE");
                mobean.des = rs.getString("DES");
                mobean.image = rs.getString("IMAGE");
                mobean.time = rs.getString("TIME");
                mobean.sourceTime = rs.getString("SOURCE_TIME");
                mobean.url = rs.getString("URL");
                mobean.status = rs.getBoolean("STATUS");
                return mobean;
            }
        };
        return getJdbcTemplate().query(sql.toString(),
                new Object[]{url, domainid}, mapper);
    }

    public List<MetaBean> getByStatus(boolean status) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ID, DOMAIN_ID, TITLE, DES, IMAGE, TIME, SOURCE_TIME, URL, STATUS FROM META"
                + " WHERE STATUS = ?");

        RowMapper<MetaBean> mapper = new RowMapper<MetaBean>() {

            @Override
            public MetaBean mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                MetaBean mobean = new MetaBean();
                mobean.id = rs.getLong("ID");
                mobean.remoteid = mobean.id;
                mobean.domainId = rs.getLong("DOMAIN_ID");
                mobean.title = rs.getString("TITLE");
                mobean.des = rs.getString("DES");
                mobean.image = rs.getString("IMAGE");
                mobean.time = rs.getString("TIME");
                mobean.sourceTime = rs.getString("SOURCE_TIME");
                mobean.url = rs.getString("URL");
                mobean.status = rs.getBoolean("STATUS");
                return mobean;
            }
        };
        return getJdbcTemplate().query(sql.toString(),
                new Object[]{status}, mapper);
    }

    public List<MetaBean> getAll() throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ID, DOMAIN_ID, TITLE, DES, IMAGE, TIME, SOURCE_TIME, URL, STATUS FROM META"
                + " ORDER BY TIME DESC");

        RowMapper<MetaBean> mapper = new RowMapper<MetaBean>() {

            @Override
            public MetaBean mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                MetaBean mobean = new MetaBean();
                mobean.id = rs.getLong("ID");
                mobean.remoteid = mobean.id;
                mobean.domainId = rs.getLong("DOMAIN_ID");
                mobean.title = rs.getString("TITLE");
                mobean.des = rs.getString("DES");
                mobean.image = rs.getString("IMAGE");
                mobean.time = rs.getString("TIME");
                mobean.sourceTime = rs.getString("SOURCE_TIME");
                mobean.url = rs.getString("URL");
                mobean.status = rs.getBoolean("STATUS");
                return mobean;
            }
        };
        return getJdbcTemplate().query(sql.toString(), mapper);
    }
}
