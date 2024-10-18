package com.cuadra.cuadra.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import com.cuadra.cuadra.model.Usuario;
import com.cuadra.cuadra.service.SaludService;
import com.cuadra.cuadra.service.UsuarioService;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "http://localhost:8080") 
public class UsuarioController {
    
    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private SaludService saludService;

    @GetMapping("/perfil/{idUsuario}")
    public ResponseEntity<Usuario> obtenerPerfil(@PathVariable Long idUsuario) {
        Usuario usuario = usuarioService.obtenerPorId(idUsuario);
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.ok(usuario);
    }

    // 1. Registrar Usuario
    @PostMapping("/registrar")
    public ResponseEntity<Usuario> registrarUsuario(@RequestBody @Valid Usuario usuario) {
        try {
            Usuario nuevoUsuario = usuarioService.registrarUsuario(usuario);
            return new ResponseEntity<>(nuevoUsuario, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // 2. Autenticar Usuario (modificado para recibir clave única)
    @PostMapping("/login")
    public ResponseEntity<Usuario> autenticarUsuario(
            @RequestParam String nombreUsuario, 
            @RequestParam String contrasena,
            @RequestParam String claveUnica) { 

        try {
            Usuario usuarioAutenticado = usuarioService.autenticarUsuario(nombreUsuario, contrasena, claveUnica);
            if (usuarioAutenticado != null) {
                return new ResponseEntity<>(usuarioAutenticado, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }


    // 3. Obtener todos los usuarios (solo para administradores - requiere autenticación y autorización)
    @GetMapping
    public ResponseEntity<List<Usuario>> obtenerTodosLosUsuarios() {
        try {
            List<Usuario> usuarios = usuarioService.obtenerTodosLosUsuarios();
            return new ResponseEntity<>(usuarios, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 4. Obtener usuario por ID (requiere autenticación y autorización)
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> obtenerUsuarioPorId(@PathVariable Long id) {
        try {
            Usuario usuario = usuarioService.obtenerUsuarioPorId(id);
            return new ResponseEntity<>(usuario, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // 5. Actualizar usuario (requiere autenticación y autorización)
    @PutMapping("/{id}")
    public ResponseEntity<Usuario> actualizarUsuario(@PathVariable Long id, @RequestBody @Valid Usuario usuarioActualizado) {
        try {
            Usuario usuario = usuarioService.actualizarUsuario(id, usuarioActualizado);
            return new ResponseEntity<>(usuario, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // 6. Desactivar usuario (solo para administradores - requiere autenticación y autorización)
    @PutMapping("/{id}/desactivar")
    public ResponseEntity<Void> desactivarUsuario(@PathVariable Long id) {
        try {
            usuarioService.desactivarUsuario(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // 7. Activar usuario (solo para administradores - requiere autenticación y autorización)
    @PutMapping("/{id}/activar")
    public ResponseEntity<Void> activarUsuario(@PathVariable Long id) {
        try {
            usuarioService.activarUsuario(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // 8. Calcular IMC 
    @GetMapping("/{id}/imc")
    public ResponseEntity<Double> calcularIMC(@PathVariable Long id) {
        try {
            Usuario usuario = usuarioService.obtenerUsuarioPorId(id);
            double imc = saludService.calcularIMC(usuario.getPesoActual(), usuario.getAltura());
            return new ResponseEntity<>(imc, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
