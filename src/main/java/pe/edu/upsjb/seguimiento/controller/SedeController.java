

package pe.edu.upsjb.seguimiento.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pe.edu.upsjb.seguimiento.dto.*;
import pe.edu.upsjb.seguimiento.service.*;


@RestController
@CrossOrigin(origins = {
        "http://localhost:4200",
})

public class SedeController {

    @Autowired
    SedeService sedeService;

    @GetMapping(value = "/consultar-sede")
    public @ResponseBody ListaSedeResponse consultarSede() { return sedeService.consultarSede(); }

}
