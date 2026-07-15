

package pe.edu.upsjb.seguimiento.dao;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import pe.edu.upsjb.seguimiento.dto.*;

import javax.xml.transform.Result;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;


@Repository


public class SedeDaoImpl extends Dao implements SedeDao {


    @Autowired
    private JdbcTemplate jdbcTemplate;

    public ListaSedeResponse consultarSede () {

        ListaSedeResponse response = new ListaSedeResponse();
        response.setLista(new ArrayList<>());

        try {

            Connection con = getConnection();

            PreparedStatement psSelect = con.prepareStatement(
                    " SELECT * " +
                            " FROM seguimiento_egresado.sede " +
                            " WHERE activo = true "
            );

            ResultSet rs = psSelect.executeQuery();

            while (rs.next()) {
                SedeResponse dto = new SedeResponse();
                dto.setId(rs.getInt("id"));
                dto.setNombre(rs.getString("nombre"));
                dto.setDescripcion(rs.getString("descripcion"));
                dto.setFechaCreacion(rs.getString("fecha_creacion"));
                dto.setFechaModificacion(rs.getString("fecha_modificacion"));
                dto.setActivo(rs.getBoolean("activo"));
                response.getLista().add(dto);
            }

            System.out.println("Obteniendo Lista de Sedes");

            psSelect.close();
            con.close();

        } catch (SQLException e) {

            throw new RuntimeException(e);

        }

        return response;

    }

}

