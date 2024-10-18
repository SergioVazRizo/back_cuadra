package com.cuadra.cuadra.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cuadra.cuadra.model.SaludDTO;
import com.cuadra.cuadra.service.SaludService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
public class SaludController {
 
    @Autowired
    private SaludService saludService;

    @GetMapping("/salud/{idUsuario}")
    public ResponseEntity<Map<String, Object>> obtenerInformacionSaludPorIdUsuario(@PathVariable Long idUsuario) {
        var informacionSalud = saludService.obtenerInformacionSaludPorIdUsuario(idUsuario);

        if (informacionSalud != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("imc", informacionSalud.getImc());
            response.put("pesoIdeal", informacionSalud.getPesoIdeal());
            response.put("presionArterialIdeal", informacionSalud.getPresionArterialIdeal());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
