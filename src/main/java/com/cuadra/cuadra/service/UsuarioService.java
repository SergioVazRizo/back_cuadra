package com.cuadra.cuadra.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cuadra.cuadra.model.Dispositivo;
import com.cuadra.cuadra.model.Usuario;
import com.cuadra.cuadra.repository.DispositivoRepository;
import com.cuadra.cuadra.repository.UsuarioRepository;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private DispositivoRepository dispositivoRepository;

    @Autowired
    private SaludService saludService; 

    public Usuario obtenerPorId(Long idUsuario) {
        return usuarioRepository.findById(idUsuario).orElse(null);
    }

    // 1. Registrar Usuario
    @Transactional
    public Usuario registrarUsuario(Usuario usuario) {
        // Verificar si se proporcionó un IMEI
        if (usuario.getDispositivo().getImei() == null || usuario.getDispositivo().getImei().isEmpty()) {
            // Generar un IMEI ficticio si no se proporciona
            usuario.getDispositivo().setImei(generarImeiFicticio());
        } else {
            // Validar el IMEI proporcionado
            if (!esImeiValido(usuario.getDispositivo().getImei())) {
                throw new IllegalArgumentException("El IMEI proporcionado no es válido.");
            }
        }
        // Generar clave única y configurar el dispositivo
        usuario.getDispositivo().setClaveUnica(UUID.randomUUID().toString());
        usuario.getDispositivo().setActivo(true); 
        usuario.getDispositivo().setActivo(true);
        Usuario nuevoUsuario = usuarioRepository.save(usuario);
        return nuevoUsuario;
    }

    private String generarImeiFicticio() {
        Random random = new Random();
        StringBuilder imeiBuilder = new StringBuilder();
        for (int i = 0; i < 15; i++) {
            imeiBuilder.append(random.nextInt(10)); // Agregar un dígito aleatorio entre 0 y 9
        }
        return imeiBuilder.toString();
    }
    private boolean esImeiValido(String imei) {
        return imei.matches("\\d{15}"); // Validación simple: 15 dígitos numéricos.
    }

    // 2. Autenticar Usuario (con validación de clave única y actualización de último acceso)
    @Transactional
    public Usuario autenticarUsuario(String nombreUsuario, String contrasena) throws Exception {
        Optional<Usuario> usuarioOptional = usuarioRepository.findByNombreUsuario(nombreUsuario);

        if (usuarioOptional.isPresent()) {
            Usuario usuario = usuarioOptional.get();
            Dispositivo dispositivo = usuario.getDispositivo();

            if (contrasena.equals(usuario.getContrasena()) && 
                dispositivo.isActivo()) { 

                // Actualizar último acceso y tiempo activo
                LocalDateTime ahora = LocalDateTime.now();
                dispositivo.setUltimoAcceso(ahora);

                dispositivoRepository.save(dispositivo);
                return usuario;
            } else {
                throw new Exception("Credenciales incorrectas o dispositivo no activo."); 
            }
        } else {
            throw new Exception("Usuario no encontrado."); 
        }
    }

    // 3. Obtener todos los usuarios (para administradores)
    public List<Usuario> obtenerTodosLosUsuarios() {
        return usuarioRepository.findAll();
    }

    // 4. Obtener usuario por ID
    public Usuario obtenerUsuarioPorId(Long usuarioId) {
        try {
            return usuarioRepository.findById(usuarioId).orElseThrow();
        } catch (NoSuchElementException e) {
            throw new RuntimeException("Usuario no encontrado con ID: " + usuarioId);
        }
    }

    // 5. Actualizar usuario
    @Transactional
    public Usuario actualizarUsuario(Long usuarioId, Usuario usuarioActualizado) {
        try {
            Usuario usuarioExistente = usuarioRepository.findById(usuarioId).orElseThrow();
            usuarioExistente.setNombreCompleto(usuarioActualizado.getNombreCompleto());
            // ... Actualizar otros campos si es necesario
            return usuarioRepository.save(usuarioExistente);
        } catch (NoSuchElementException e) {
            throw new RuntimeException("Usuario no encontrado con ID: " + usuarioId);
        }
    }

    // 6. Desactivar usuario
    @Transactional
    public void desactivarUsuario(Long usuarioId) {
        try {
            Usuario usuario = usuarioRepository.findById(usuarioId).orElseThrow();
            usuario.getDispositivo().setActivo(false);
            usuarioRepository.save(usuario);
        } catch (NoSuchElementException e) {
            throw new RuntimeException("Usuario no encontrado con ID: " + usuarioId);
        }
    }

    // 7. Activar usuario
    @Transactional
    public void activarUsuario(Long usuarioId) {
        try {
            Usuario usuario = usuarioRepository.findById(usuarioId).orElseThrow();
            usuario.getDispositivo().setActivo(true);
            usuarioRepository.save(usuario);
        } catch (NoSuchElementException e) {
            throw new RuntimeException("Usuario no encontrado con ID: " + usuarioId);
        }
    }

    // 8. Obtener información de salud del usuario
    public com.cuadra.cuadra.model.SaludDTO obtenerInformacionSalud(Long usuarioId) {
        Usuario usuario = obtenerUsuarioPorId(usuarioId);

        double imc = saludService.calcularIMC(usuario.getPesoActual(), usuario.getAltura());
        double pesoIdeal = saludService.calcularPesoIdeal(usuario.getSexo(), usuario.getAltura());
        String presionArterialIdeal = saludService.calcularPresionArterialIdeal(calcularEdad(usuario.getFechaNacimiento()));

        com.cuadra.cuadra.model.SaludDTO saludDTO = new com.cuadra.cuadra.model.SaludDTO();
        saludDTO.setImc(imc);
        saludDTO.setPesoIdeal(pesoIdeal);
        saludDTO.setPresionArterialIdeal(presionArterialIdeal);

        return saludDTO;
    }

    // Método auxiliar para calcular la edad a partir de la fecha de nacimiento
    private int calcularEdad(LocalDate fechaNacimiento) {
        if (fechaNacimiento == null) {
            return 0; // Manejar el caso donde la fecha de nacimiento es nula
        }
        return Period.between(fechaNacimiento, LocalDate.now()).getYears();
    }
}