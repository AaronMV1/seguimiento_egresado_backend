

package pe.edu.upsjb.seguimiento.dao;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import pe.edu.upsjb.seguimiento.dto.*;

import java.sql.*;
import java.util.*;
import java.time.LocalDate;


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
            long egresadoId;
            long seguimientoId;


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
                //region    ACTUALIZAR DATOS


                egresadoId = rs.getLong("egresado_id");

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
                psUpdate.setLong(9, egresadoId);

                int filasActualizadas = psUpdate.executeUpdate();

                if (filasActualizadas == 0) {
                    throw new SQLException("No se pudo actualizar al egresado.");
                }

                psUpdate.close();


                //endregion
            }

            else {
                //region    REGISTRAR EGRESADO


                PreparedStatement psInsertEgresado = con.prepareStatement(
                        "INSERT INTO seguimiento_egresado.egresado (" +
                                " tipo_documento, " +
                                " numero_documento, " +
                                " nombres_apellidos, " +
                                " genero, " +
                                " sede_id, " +
                                " facultad_id, " +
                                " carrera_id, " +
                                " anio_egreso, " +
                                " correo_electronico, " +
                                " numero_celular " +
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

                int filaInsertadaEgresado = psInsertEgresado.executeUpdate();

                if (filaInsertadaEgresado == 0) {
                    throw new SQLException("No se pudo registrar al egresado.");
                }

                ResultSet rsEgresadoIDGenerado = psInsertEgresado.getGeneratedKeys();

                if (rsEgresadoIDGenerado.next()) {

                    egresadoId = rsEgresadoIDGenerado.getLong(1);
                    System.out.println("ID del Egresado: " + egresadoId);

                } else {
                    throw new SQLException( "No se pudo obtener el ID del egresado." );
                }

                rsEgresadoIDGenerado.close();
                psInsertEgresado.close();


                //endregion
            }

            //region    REGISTRAR SEGUIMIENTO

            // int anioSeguimiento = LocalDate.now().getYear();         //  PRODUCCION
            int anioSeguimiento = 2021;                                 //  TEST

            PreparedStatement psInsertSeguimiento = con.prepareStatement(
                    " INSERT INTO seguimiento_egresado.seguimiento (" +
                            " egresado_id, " +
                            " fase, " +
                            " anio_seguimiento " +
                            ") VALUES (?, ?, ?) ",
                    Statement.RETURN_GENERATED_KEYS
            );

            psInsertSeguimiento.setLong(1, egresadoId);
            psInsertSeguimiento.setInt(2, request.getFase());
            psInsertSeguimiento.setInt(3, anioSeguimiento);

            int filaInsertadaSeguimiento = psInsertSeguimiento.executeUpdate();

            if (filaInsertadaSeguimiento == 0) {
                throw new SQLException("No se pudo registrar el seguimiento.");
            }

            ResultSet rsSeguimientoIDGenerado = psInsertSeguimiento.getGeneratedKeys();

            if (rsSeguimientoIDGenerado.next()) {

                seguimientoId = rsSeguimientoIDGenerado.getLong(1);
                System.out.println("ID del Seguimiento: " + seguimientoId);

            } else {
                throw new SQLException( "No se pudo obtener el ID del seguimiento." );
            }

            rsSeguimientoIDGenerado.close();
            psInsertSeguimiento.close();


            //endregion

            //region    REGISTRAR FASE DE SEGUIMIENTO


            switch (request.getFase()) {

                case 1:

                    System.out.println("Fase 1");

                    PreparedStatement psInsertFase1 = con.prepareStatement(
                            " INSERT INTO seguimiento_egresado.seguimiento_fase_1 (" +
                                    " seguimiento_id, " +
                                    " fase1_participacion, " +
                                    " fase1_situacion, " +
                                    " fase1_trabajando, " +
                                    " fase1_primerempleo, " +
                                    " fase1_medios " +
                                    ") VALUES (?, ?, ?, ?, ?, ?) "
                    );

                    psInsertFase1.setLong(1, seguimientoId);
                    psInsertFase1.setString(2, request.getFase1participacion());
                    psInsertFase1.setString(3, request.getFase1situacion());
                    psInsertFase1.setString(4, request.getFase1trabajando());
                    psInsertFase1.setString(5, request.getFase1primerempleo());
                    psInsertFase1.setString(6, request.getFase1medios());

                    int filaInsertadaFase1 = psInsertFase1.executeUpdate();

                    if (filaInsertadaFase1 == 0) {
                        throw new SQLException("No se pudo registrar la Fase 1.");
                    }

                    psInsertFase1.close();

                    break;

                case 2:

                    System.out.println("Fase 2");

                    PreparedStatement psInsertFase2 = con.prepareStatement(
                            " INSERT INTO seguimiento_egresado.seguimiento_fase_2 (" +
                                    " seguimiento_id, " +
                                    " fase2_satisfaccionestudios, " +
                                    " fase2_participacion, " +
                                    " fase2_satisfaccionservicio, " +
                                    " fase2_planificacion, " +
                                    " fase2_empresanombre, " +
                                    " fase2_empresaempleadornombre, " +
                                    " fase2_empresaempleadorcorreo, " +
                                    " fase2_empresaempleadornumero " +
                                    ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) "
                    );

                    psInsertFase2.setLong(1, seguimientoId);
                    psInsertFase2.setString(2, request.getFase2satisfaccionestudios());
                    psInsertFase2.setString(3, request.getFase2participacion());
                    psInsertFase2.setString(4, request.getFase2satisfaccionservicio());
                    psInsertFase2.setString(5, request.getFase2planificacion());
                    psInsertFase2.setString(6, request.getFase2empresanombre());
                    psInsertFase2.setString(7, request.getFase2empresaempleadornombre());
                    psInsertFase2.setString(8, request.getFase2empresaempleadorcorreo());
                    psInsertFase2.setString(9, request.getFase2empresaempleadornumero());

                    int filaInsertadaFase2 = psInsertFase2.executeUpdate();

                    if (filaInsertadaFase2 == 0) {
                        throw new SQLException("No se pudo registrar la Fase 2.");
                    }

                    psInsertFase2.close();

                    break;

                case 3:

                    System.out.println("Fase 3");

                    PreparedStatement psInsertFase3 = con.prepareStatement(
                            " INSERT INTO seguimiento_egresado.seguimiento_fase_3 (" +
                                    " seguimiento_id, " +
                                    " fase3_especialidad, " +
                                    " fase3_participacion, " +
                                    " fase3_educacioncontinua " +
                                    ") VALUES (?, ?, ?, ?) "
                    );

                    psInsertFase3.setLong(1, seguimientoId);
                    psInsertFase3.setString(2, request.getFase3especialidad());
                    psInsertFase3.setString(3, request.getFase3participacion());
                    psInsertFase3.setString(4, request.getFase3educacioncontinua());

                    int filaInsertadaFase3 = psInsertFase3.executeUpdate();

                    if (filaInsertadaFase3 == 0) {
                        throw new SQLException("No se pudo registrar la Fase 3.");
                    }

                    psInsertFase3.close();

                    break;

                case 4:

                    System.out.println("Fase 4");

                    PreparedStatement psInsertFase4 = con.prepareStatement(
                            " INSERT INTO seguimiento_egresado.seguimiento_fase_4 (" +
                                    " seguimiento_id, " +
                                    " fase4_investigacion, " +
                                    " fase4_participacion, " +
                                    " fase4_resultados, " +
                                    " fase4_innovacion, " +
                                    " fase4_capacitacion, " +
                                    " fase4_formacion " +
                                    ") VALUES (?, ?, ?, ?, ?, ?, ?) "
                    );

                    psInsertFase4.setLong(1, seguimientoId);
                    psInsertFase4.setString(2, request.getFase4investigacion());
                    psInsertFase4.setString(3, request.getFase4participacion());
                    psInsertFase4.setString(4, request.getFase4resultados());
                    psInsertFase4.setString(5, request.getFase4innovacion());
                    psInsertFase4.setString(6, request.getFase4capacitacion());
                    psInsertFase4.setString(7, request.getFase4formacion());

                    int filaInsertadaFase4 = psInsertFase4.executeUpdate();

                    if (filaInsertadaFase4 == 0) {
                        throw new SQLException("No se pudo registrar la Fase 3.");
                    }

                    psInsertFase4.close();

                    break;

                default:

                    throw new SQLException("La fase indicada no es válida.");

            }


            //endregion


            rs.close();
            psBuscarEgresado.close();
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

    public ListaEncuestadosResponse consultarEncuestados() {

        ListaEncuestadosResponse response = new ListaEncuestadosResponse();
        response.setLista(new ArrayList<>());

        try {

            Connection con = getConnection();

            PreparedStatement psSelect = con.prepareStatement(
                    " SELECT " +
                            " e.egresado_id, " +
                            " e.tipo_documento, " +
                            " e.numero_documento, " +
                            " e.nombres_apellidos, " +
                            " e.genero, " +

                            " e.sede_id, " +
                            " s.nombre AS sede_nombre, " +

                            " e.facultad_id, " +
                            " f.nombre AS facultad_nombre, " +

                            " e.carrera_id, " +
                            " c.nombre AS carrera_nombre, " +

                            " e.anio_egreso, " +
                            " e.correo_electronico, " +
                            " e.numero_celular " +

                            " FROM seguimiento_egresado.egresado e " +

                            " LEFT JOIN seguimiento_egresado.sede s " +
                            " ON e.sede_id = s.id " +

                            " LEFT JOIN seguimiento_egresado.facultad f " +
                            " ON e.facultad_id = f.id " +

                            " LEFT JOIN seguimiento_egresado.carrera c " +
                            " ON e.carrera_id = c.id"
            );

            ResultSet rs = psSelect.executeQuery();

            while (rs.next()) {
                EncuestaResponse dto = new EncuestaResponse();
                dto.setEgresadoId(rs.getInt("egresado_id"));
                dto.setTipoDocumento(rs.getString("tipo_documento"));
                dto.setNumeroDocumento(rs.getString("numero_documento"));
                dto.setNombresApellidos(rs.getString("nombres_apellidos"));
                dto.setGenero(rs.getString("genero"));
                dto.setSede(rs.getString("sede_nombre"));
                dto.setFacultad(rs.getString("facultad_nombre"));
                dto.setCarrera(rs.getString("carrera_nombre"));
                dto.setAnioEgreso(rs.getString("anio_egreso"));
                dto.setCorreoElectronico(rs.getString("correo_electronico"));
                dto.setNumeroCelular(rs.getString("numero_celular"));
                response.getLista().add(dto);
            }

            System.out.println("Obteniendo Lista de Encuestados");

            psSelect.close();
            con.close();

        } catch (SQLException e) {

            throw new RuntimeException(e);

        }

        return response;

    }


}

