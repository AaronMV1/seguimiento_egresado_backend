

package pe.edu.upsjb.seguimiento.dao;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import pe.edu.upsjb.seguimiento.dto.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;


@Repository


public class EncuestaDaoImpl extends Dao implements EncuestaDao {


    @Autowired
    private JdbcTemplate jdbcTemplate;


    public MensajeResponse enviarEncuesta(EncuestaRequest request) {


        MensajeResponse response = new MensajeResponse();
        Connection con = null;


        try {


            con = getConnection();
            con.setAutoCommit(false);
            int egresadoId;


            /* 1. Buscar al egresado. */
            PreparedStatement psBuscarEgresado = con.prepareStatement(
                    "SELECT egresado_id " +
                            "FROM seguimiento_egresado.egresado " +
                            "WHERE tipo_documento = ? " +
                            "AND numero_documento = ?"
            );

            psBuscarEgresado.setString(1, request.getTipoDocumento());
            psBuscarEgresado.setString(2, request.getNumeroDocumento());

            ResultSet rs = psBuscarEgresado.executeQuery();


            if (rs.next()) {

                /* El egresado existe: actualizar sus datos. */
                egresadoId = rs.getInt("egresado_id");

                PreparedStatement psUpdate = con.prepareStatement(
                        "UPDATE seguimiento_egresado.egresado SET " +
                                "nombres_apellidos = ?, " +
                                "genero = ?, " +
                                "sede_id = ?, " +
                                "facultad_id = ?, " +
                                "carrera_id = ?, " +
                                "anio_egreso = ?, " +
                                "correo_electronico = ?, " +
                                "numero_celular = ?, " +
                                "fecha_modificacion = NOW() " +
                                "WHERE egresado_id = ?"
                );

                psUpdate.setString(1, request.getNombresApellidos());
                psUpdate.setString(2, request.getGenero());
                psUpdate.setInt(3, request.getSede());
                psUpdate.setInt(4, request.getFacultad());
                psUpdate.setInt(5, request.getCarrera());
                psUpdate.setInt(6, request.getAnioEgreso());
                psUpdate.setString(7, request.getCorreoElectronico());
                psUpdate.setString(8, request.getNumeroCelular());
                psUpdate.setInt(9, egresadoId);

                int filasActualizadas = psUpdate.executeUpdate();

                if (filasActualizadas == 0) {
                    throw new SQLException("No se pudo actualizar al egresado.");
                }

                psUpdate.close();

            } else {

                /* El egresado no existe: registrar sus datos. */
                PreparedStatement psInsertEgresado = con.prepareStatement(
                        "INSERT INTO seguimiento_egresado.egresado (" +
                                "tipo_documento, " +
                                "numero_documento, " +
                                "nombres_apellidos, " +
                                "genero, " +
                                "sede_id, " +
                                "facultad_id, " +
                                "carrera_id, " +
                                "anio_egreso, " +
                                "correo_electronico, " +
                                "numero_celular" +
                                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS
                );

                psInsertEgresado.setString( 1, request.getTipoDocumento());
                psInsertEgresado.setString( 2, request.getNumeroDocumento());
                psInsertEgresado.setString( 3, request.getNombresApellidos());
                psInsertEgresado.setString( 4, request.getGenero());
                psInsertEgresado.setInt( 5, request.getSede());
                psInsertEgresado.setInt( 6, request.getFacultad());
                psInsertEgresado.setInt( 7, request.getCarrera());
                psInsertEgresado.setInt( 8, request.getAnioEgreso());
                psInsertEgresado.setString( 9,request.getCorreoElectronico());
                psInsertEgresado.setString( 10, request.getNumeroCelular());

                int filasInsertadas = psInsertEgresado.executeUpdate();

                if (filasInsertadas == 0) {
                    throw new SQLException("No se pudo registrar al egresado.");
                }

                ResultSet rsGenerado = psInsertEgresado.getGeneratedKeys();

                if (rsGenerado.next()) {
                    egresadoId = rsGenerado.getInt(1);
                } else {
                    throw new SQLException( "No se pudo obtener el ID del egresado." );
                }

                rsGenerado.close();
                psInsertEgresado.close();
            }

            rs.close();
            psBuscarEgresado.close();


            /* Aquí podrás insertar seguimiento y seguimiento_fase_X, usando la variable egresadoId. */


            System.out.println("Egresado ID: " + egresadoId);

            con.commit();

            response.setEstado("200");
            response.setMensaje("Datos del egresado guardados correctamente.");


        } catch (Exception e) {


            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException rollbackError) {
                    rollbackError.printStackTrace();
                }
            }


            response.setEstado("500");
            response.setMensaje( "Error al registrar la encuesta: " + e.getMessage());


        } finally {

            if (con != null) {

                try {

                    con.close();

                } catch (SQLException closeError) {

                    closeError.printStackTrace();

                }

            }

        }

        return response;

    }

}

