

package pe.edu.upsjb.seguimiento.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.edu.upsjb.seguimiento.dto.*;
import pe.edu.upsjb.seguimiento.dao.*;


@Service
@Transactional


public class EncuestaServiceImpl implements EncuestaService {


    @Autowired
    private EncuestaDao encuestaDao;


    public MensajeResponse enviarEncuesta (EncuestaRequest request) {
        return encuestaDao.enviarEncuesta(request);
    }

    public ListaEncuestadosResponse consultarEncuestados() {
        return encuestaDao.consultarEncuestados();
    }

}

