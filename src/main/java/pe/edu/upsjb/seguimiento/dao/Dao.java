

package pe.edu.upsjb.seguimiento.dao;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;


public abstract class Dao {

    @Autowired

    protected JdbcTemplate jdbcTemplate;

    protected Connection getConnection() throws SQLException {

        return jdbcTemplate.getDataSource().getConnection();

    }

    protected PreparedStatement getStatement(Connection conn, String sql) throws SQLException {
        PreparedStatement psUpdate = conn.prepareStatement(sql);
        return psUpdate;
    }

}
