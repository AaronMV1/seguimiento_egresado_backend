

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


        System.out.println("Enviar Encuesta");


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

            int anioSeguimiento = LocalDate.now().getYear();         //  PRODUCCION
            // int anioSeguimiento = 2022;                                 //  TEST

            /* Validar que el egresado no haya completado ya la encuesta este año. */
            PreparedStatement psVerificarSeguimiento = con.prepareStatement(
                    "SELECT seguimiento_id " +
                            "FROM seguimiento_egresado.seguimiento " +
                            "WHERE egresado_id = ? " +
                            "AND anio_seguimiento = ?"
            );

            psVerificarSeguimiento.setLong(1, egresadoId);
            psVerificarSeguimiento.setInt(2, anioSeguimiento);

            ResultSet rsVerificarSeguimiento = psVerificarSeguimiento.executeQuery();
            boolean yaCompletoEsteAnio = rsVerificarSeguimiento.next();

            rsVerificarSeguimiento.close();
            psVerificarSeguimiento.close();

            if (yaCompletoEsteAnio) {

                con.rollback();

                response.setEstado("409");
                response.setMensaje("El egresado ya completó la encuesta de seguimiento correspondiente al año " + anioSeguimiento + ".");

                return response;

            }

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


            System.out.println("Recibir Encuesta");


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

    public MensajeResponse verificarEncuesta(String tipoDocumento, String numeroDocumento) {

        MensajeResponse response = new MensajeResponse();

        int anioSeguimiento = LocalDate.now().getYear();

        try (Connection con = getConnection()) {

            PreparedStatement psVerificar = con.prepareStatement(
                    "SELECT s.seguimiento_id " +
                            "FROM seguimiento_egresado.seguimiento s " +
                            "JOIN seguimiento_egresado.egresado e ON e.egresado_id = s.egresado_id " +
                            "WHERE e.tipo_documento = ? " +
                            "AND e.numero_documento = ? " +
                            "AND s.anio_seguimiento = ?"
            );

            psVerificar.setString(1, tipoDocumento);
            psVerificar.setString(2, numeroDocumento);
            psVerificar.setInt(3, anioSeguimiento);

            ResultSet rs = psVerificar.executeQuery();

            if (rs.next()) {

                response.setEstado("409");
                response.setMensaje("El egresado ya completó la encuesta de seguimiento correspondiente al año " + anioSeguimiento + ".");

            } else {

                response.setEstado("200");
                response.setMensaje("El egresado aún no ha completado la encuesta de este año.");

            }

            rs.close();
            psVerificar.close();

        } catch (Exception e) {

            response.setEstado("500");
            response.setMensaje("Error al verificar la encuesta: " + e.getMessage());

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
                            " e.numero_celular, " +
                            " f1.fase1_participacion, f1.fase1_situacion, f1.fase1_trabajando, " +
                            " f1.fase1_primerempleo, f1.fase1_medios, " +
                            " f2.fase2_satisfaccionestudios, f2.fase2_participacion, " +
                            " f2.fase2_satisfaccionservicio, f2.fase2_planificacion, " +
                            " f2.fase2_empresanombre, f2.fase2_empresaempleadornombre, " +
                            " f2.fase2_empresaempleadorcorreo, f2.fase2_empresaempleadornumero, " +
                            " f3.fase3_especialidad, f3.fase3_participacion, f3.fase3_educacioncontinua, " +
                            " f4.fase4_investigacion, f4.fase4_participacion, f4.fase4_resultados, " +
                            " f4.fase4_innovacion, f4.fase4_capacitacion, f4.fase4_formacion " +

                            " FROM seguimiento_egresado.egresado e " +

                            " LEFT JOIN seguimiento_egresado.sede s " +
                            " ON e.sede_id = s.id " +

                            " LEFT JOIN seguimiento_egresado.facultad f " +
                            " ON e.facultad_id = f.id " +

                            " LEFT JOIN seguimiento_egresado.carrera c " +
                            " ON e.carrera_id = c.id " +
                            " LEFT JOIN LATERAL (SELECT sf.* FROM seguimiento_egresado.seguimiento_fase_1 sf " +
                            " JOIN seguimiento_egresado.seguimiento s ON s.seguimiento_id = sf.seguimiento_id " +
                            " WHERE s.egresado_id = e.egresado_id ORDER BY s.anio_seguimiento DESC, s.seguimiento_id DESC LIMIT 1) f1 ON TRUE " +
                            " LEFT JOIN LATERAL (SELECT sf.* FROM seguimiento_egresado.seguimiento_fase_2 sf " +
                            " JOIN seguimiento_egresado.seguimiento s ON s.seguimiento_id = sf.seguimiento_id " +
                            " WHERE s.egresado_id = e.egresado_id ORDER BY s.anio_seguimiento DESC, s.seguimiento_id DESC LIMIT 1) f2 ON TRUE " +
                            " LEFT JOIN LATERAL (SELECT sf.* FROM seguimiento_egresado.seguimiento_fase_3 sf " +
                            " JOIN seguimiento_egresado.seguimiento s ON s.seguimiento_id = sf.seguimiento_id " +
                            " WHERE s.egresado_id = e.egresado_id ORDER BY s.anio_seguimiento DESC, s.seguimiento_id DESC LIMIT 1) f3 ON TRUE " +
                            " LEFT JOIN LATERAL (SELECT sf.* FROM seguimiento_egresado.seguimiento_fase_4 sf " +
                            " JOIN seguimiento_egresado.seguimiento s ON s.seguimiento_id = sf.seguimiento_id " +
                            " WHERE s.egresado_id = e.egresado_id ORDER BY s.anio_seguimiento DESC, s.seguimiento_id DESC LIMIT 1) f4 ON TRUE"
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
                dto.setFase1Participacion(rs.getString("fase1_participacion"));
                dto.setFase1Situacion(rs.getString("fase1_situacion"));
                dto.setFase1Trabajando(rs.getString("fase1_trabajando"));
                dto.setFase1Primerempleo(rs.getString("fase1_primerempleo"));
                dto.setFase1Medios(rs.getString("fase1_medios"));
                dto.setFase2Satisfaccionestudios(rs.getString("fase2_satisfaccionestudios"));
                dto.setFase2Participacion(rs.getString("fase2_participacion"));
                dto.setFase2Satisfaccionservicio(rs.getString("fase2_satisfaccionservicio"));
                dto.setFase2Planificacion(rs.getString("fase2_planificacion"));
                dto.setFase2Empresanombre(rs.getString("fase2_empresanombre"));
                dto.setFase2Empresaempleadornombre(rs.getString("fase2_empresaempleadornombre"));
                dto.setFase2Empresaempleadorcorreo(rs.getString("fase2_empresaempleadorcorreo"));
                dto.setFase2Empresaempleadornumero(rs.getString("fase2_empresaempleadornumero"));
                dto.setFase3Especialidad(rs.getString("fase3_especialidad"));
                dto.setFase3Participacion(rs.getString("fase3_participacion"));
                dto.setFase3Educacioncontinua(rs.getString("fase3_educacioncontinua"));
                dto.setFase4Investigacion(rs.getString("fase4_investigacion"));
                dto.setFase4Participacion(rs.getString("fase4_participacion"));
                dto.setFase4Resultados(rs.getString("fase4_resultados"));
                dto.setFase4Innovacion(rs.getString("fase4_innovacion"));
                dto.setFase4Capacitacion(rs.getString("fase4_capacitacion"));
                dto.setFase4Formacion(rs.getString("fase4_formacion"));
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

