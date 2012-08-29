package net.peacesoft.repositories;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import net.peacesoft.domain.ContentBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

/**
 *
 * @author Tran Anh Tuan <tuanta2@peacesoft.net>
 */
public class ContentRepository {

    private DataSource dataSource;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    private JdbcTemplate jdbcTemplate;

    private JdbcTemplate getJdbcTemplate() throws Exception {
        jdbcTemplate = new JdbcTemplate(dataSource);
        return jdbcTemplate;
    }

    public int save(ContentBean bean) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO CONTENT (META_ID, DATE, CONTENT)");
        sql.append("VALUES ( ?, ?, ?)");
        return getJdbcTemplate().update(
                sql.toString(),
                new Object[]{bean.id, bean.date,
                    bean.content});
    }

    public int update(String content, long id) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE CONTENT SET CONTENT = ?");
        sql.append(" WHERE META_ID = ? ");
        return getJdbcTemplate().update(
                sql.toString(),
                new Object[]{content, id});
    }

    public ContentBean getById(long id) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT META_ID, DATE, CONTENT FROM CONTENT"
                + " WHERE META_ID = ?");

        RowMapper<ContentBean> mapper = new RowMapper<ContentBean>() {

            @Override
            public ContentBean mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                ContentBean contentBean = new ContentBean();
                contentBean.id = rs.getLong("META_ID");
                contentBean.date = rs.getString("DATE");
                contentBean.content = rs.getString("CONTENT");
                return contentBean;
            }
        };
        return getJdbcTemplate().queryForObject(sql.toString(),
                new Object[]{id}, mapper);
    }

    public List<ContentBean> getByName(String name) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT META_ID, DATE, CONTENT FROM CONTENT"
                + " WHERE NAME = ?");

        RowMapper<ContentBean> mapper = new RowMapper<ContentBean>() {

            @Override
            public ContentBean mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                ContentBean contentBean = new ContentBean();
                contentBean.id = rs.getLong("META_ID");
                contentBean.date = rs.getString("DATE");
                contentBean.content = rs.getString("CONTENT");
                return contentBean;
            }
        };
        return getJdbcTemplate().query(sql.toString(),
                new Object[]{name}, mapper);
    }

    public List<ContentBean> getAll() throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT META_ID, DATE, CONTENT FROM CONTENT");

        RowMapper<ContentBean> mapper = new RowMapper<ContentBean>() {

            @Override
            public ContentBean mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                ContentBean contentBean = new ContentBean();
                contentBean.id = rs.getLong("META_ID");
                contentBean.date = rs.getString("DATE");
                contentBean.content = rs.getString("CONTENT");
                return contentBean;
            }
        };
        return getJdbcTemplate().query(sql.toString(), mapper);
    }
}