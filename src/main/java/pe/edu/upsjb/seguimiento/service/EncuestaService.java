

package pe.edu.upsjb.seguimiento.service;


import pe.edu.upsjb.seguimiento.dto.*;


public interface EncuestaService {


    public MensajeResponse enviarEncuesta(EncuestaRequest request);

    public ListaEncuestadosResponse consultarEncuestados();


}
