

package pe.edu.upsjb.seguimiento.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pe.edu.upsjb.seguimiento.dto.*;
import pe.edu.upsjb.seguimiento.service.*;


@RestController


public class AccesoController {


    @Autowired
    AccesoService accesoService;

    @PostMapping (value="/enviar-login")
    public @ResponseBody MensajeResponse enviarLogin (@RequestBody LoginRequest request) {
        return accesoService.enviarLogin(request);
    }

}

