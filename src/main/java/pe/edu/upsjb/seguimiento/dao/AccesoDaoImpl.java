

package pe.edu.upsjb.seguimiento.dao;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;


import pe.edu.upsjb.seguimiento.dto.*;
import java.sql.*;


@Repository


public class AccesoDaoImpl extends Dao implements AccesoDao {


    @Autowired
    private JdbcTemplate jdbcTemplate;


    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();


    @Override
    public MensajeResponse enviarLogin(LoginRequest request) {

        MensajeResponse response = new MensajeResponse();
        Connection con = null;

        try {

            con = getConnection();

            PreparedStatement ps = con.prepareStatement(
                    " SELECT administrador_id, correo_electronico, contrasena_hash, rol_usuario " +
                            " FROM seguimiento_egresado.administrador " +
                            " WHERE correo_electronico = ? " +
                            " AND activo = TRUE "
            );

            ps.setString(1, request.getCorreo());

            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {           //  SI NO EXISTE UN REGISTRO

                response.setEstado("401");
                response.setMensaje("Datos incorrectos.");

                rs.close();
                ps.close();

                return response;
            }

            boolean matchRol = request.getRol().equals(rs.getString("rol_usuario"));

            if (!matchRol) {           //  SI CORREO O ROL NO COINCIDEN

                response.setEstado("401");
                response.setMensaje("Usuario o contraseña incorrectos. 2");

                rs.close();
                ps.close();

                return response;
            }

            String hashGuardado = rs.getString("contrasena_hash");

            boolean loginCorrecto = passwordEncoder.matches(
                    request.getContrasena(),
                    hashGuardado
            );

            rs.close();
            ps.close();

            if (!loginCorrecto) {       //  SI EL PASSWORD NO COINCIDE

                response.setEstado("401");
                response.setMensaje("Usuario o contraseña incorrectos. 3");

                return response;

            }

            response.setEstado("200");
            response.setMensaje("Inicio de sesión correcto.");

        } catch (Exception e) {

            response.setEstado("500");
            response.setMensaje(e.getMessage());

        } finally {

            if (con != null) {
                try {
                    con.close();
                } catch (SQLException ignored) {
                }
            }

        }

        return response;

    }

}