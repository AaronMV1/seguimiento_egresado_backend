

package pe.edu.upsjb.seguimiento.dao;


import pe.edu.upsjb.seguimiento.dto.*;


public interface EncuestaDao {


    public MensajeResponse enviarEncuesta (EncuestaRequest request);

    public MensajeResponse verificarEncuesta (String tipoDocumento, String numeroDocumento);

    public ListaEncuestadosResponse consultarEncuestados();

}

