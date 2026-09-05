

package pe.edu.upsjb.seguimiento.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pe.edu.upsjb.seguimiento.dto.*;
import pe.edu.upsjb.seguimiento.service.*;


@RestController


public class EncuestaController {

    @Autowired
    EncuestaService encuestaService;

    @PostMapping (value = "/enviar-encuesta")
    public @ResponseBody MensajeResponse enviarEncuesta (@RequestBody EncuestaRequest request) {
        return encuestaService.enviarEncuesta(request);
    }

    @GetMapping (value = "/verificar-encuesta")
    public @ResponseBody MensajeResponse verificarEncuesta (
            @RequestParam String tipoDocumento,
            @RequestParam String numeroDocumento
    ) {
        return encuestaService.verificarEncuesta(tipoDocumento, numeroDocumento);
    }

    @GetMapping (value = "/consultar-encuestados")
    public @ResponseBody ListaEncuestadosResponse consultarEncuestados() {
        return encuestaService.consultarEncuestados();
    }

}

