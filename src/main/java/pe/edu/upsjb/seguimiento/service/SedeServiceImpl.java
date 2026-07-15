

package pe.edu.upsjb.seguimiento.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.edu.upsjb.seguimiento.dto.*;
import pe.edu.upsjb.seguimiento.dao.*;


@Service
@Transactional


public class SedeServiceImpl implements SedeService {

    @Autowired
    private SedeDao sedeDao;

    public ListaSedeResponse consultarSede() {
        return sedeDao.consultarSede();
    }

}
