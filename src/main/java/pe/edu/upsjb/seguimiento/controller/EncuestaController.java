

package pe.edu.upsjb.seguimiento.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pe.edu.upsjb.seguimiento.dto.*;
import pe.edu.upsjb.seguimiento.service.*;


@RestController
@CrossOrigin(origins = {
        "http://localhost:4200",
        "http://172.20.14.250:4200",
})


public class EncuestaController {

    @Autowired
    EncuestaService encuestaService;

    @PostMapping (value = "/enviar-encuesta")
    public @ResponseBody MensajeResponse enviarEncuesta (@RequestBody EncuestaRequest request) {
        return encuestaService.enviarEncuesta(request);
    }

    @GetMapping (value = "/consultar-encuestados")
    public @ResponseBody ListaEncuestadosResponse consultarEncuestados() {
        return encuestaService.consultarEncuestados();
    }

}

