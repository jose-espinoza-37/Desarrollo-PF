package com.poseidon.nauticaapi.config;

import com.poseidon.nauticaapi.entity.Modulo;
import com.poseidon.nauticaapi.entity.Rol;
import com.poseidon.nauticaapi.entity.SolicitudSocio;
import com.poseidon.nauticaapi.entity.Usuario;
import com.poseidon.nauticaapi.repository.ModuloRepository;
import com.poseidon.nauticaapi.repository.RolRepository;
import com.poseidon.nauticaapi.repository.SolicitudSocioRepository;
import com.poseidon.nauticaapi.repository.UsuarioRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final RolRepository rolRepository;
    private final ModuloRepository moduloRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;
    private final SolicitudSocioRepository solicitudRepository;
    private final String passwordDemo;
    private final boolean cargarSolicitudesDemo;

    public DataInitializer(
            RolRepository rolRepository,
            ModuloRepository moduloRepository,
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JdbcTemplate jdbcTemplate,
            SolicitudSocioRepository solicitudRepository,
            @Value("${app.demo.password:Poseidon2026}") String passwordDemo,
            @Value("${app.demo.solicitudes:true}") boolean cargarSolicitudesDemo) {

        this.rolRepository = rolRepository;
        this.moduloRepository = moduloRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
        this.solicitudRepository = solicitudRepository;
        this.passwordDemo = passwordDemo;
        this.cargarSolicitudesDemo = cargarSolicitudesDemo;
    }

    @Override
    @Transactional
    public void run(String... args) {


        Modulo dashboard = modulo("DASHBOARD", "Dashboard", "/panel/dashboard", "bi-graph-up", 1);
        Modulo verificar = modulo("SOLICITUDES_VERIFICAR", "Verificar solicitudes",
                "/panel/solicitudes/verificar", "bi-clipboard-check", 2);
        Modulo aprobar = modulo("SOLICITUDES_APROBAR", "Aprobar solicitudes",
                "/panel/solicitudes/aprobar", "bi-check2-circle", 3);
        Modulo perfil = modulo("PERFIL", "Mi perfil", "/panel/perfil", "bi-person-circle", 9);


        Map<String, Object[]> roles = new LinkedHashMap<>();
        roles.put(Rol.GERENTE, new Object[]{"Gerente General", List.of(dashboard, verificar, aprobar, perfil)});
        roles.put(Rol.ATENCION_CLIENTE, new Object[]{"Atención al Cliente", List.of(verificar, aprobar, perfil)});
        roles.put(Rol.SERVICIOS_NAVIEROS, new Object[]{"Servicios Navieros", List.of(perfil)});
        roles.put(Rol.ADMINISTRACION, new Object[]{"Administración", List.of(dashboard, perfil)});
        roles.put(Rol.SOCIO, new Object[]{"Socio", List.of(perfil)});

        roles.forEach((codigo, datos) -> {
            @SuppressWarnings("unchecked")
            List<Modulo> modulos = (List<Modulo>) datos[1];
            rol(codigo, (String) datos[0], modulos);
        });


        migrarRolAntiguo();

        usuarioDemo("gerente@poseidon.pe", "Gerente", "General", Rol.GERENTE);
        usuarioDemo("atencion@poseidon.pe", "Atención", "Cliente", Rol.ATENCION_CLIENTE);
        usuarioDemo("navieros@poseidon.pe", "Servicios", "Navieros", Rol.SERVICIOS_NAVIEROS);
        usuarioDemo("administracion@poseidon.pe", "Personal", "Administración", Rol.ADMINISTRACION);


        jdbcTemplate.execute("ALTER TABLE IF EXISTS solicitudes_socio DROP COLUMN IF EXISTS codigo_seguimiento");
        jdbcTemplate.execute("ALTER TABLE IF EXISTS solicitudes_socio ADD COLUMN IF NOT EXISTS codigo UUID");
        jdbcTemplate.execute("UPDATE solicitudes_socio SET codigo = gen_random_uuid() WHERE codigo IS NULL");

        if (cargarSolicitudesDemo && solicitudRepository.count() == 0) {
            cargarSolicitudesDemo();
        }
    }


    private void cargarSolicitudesDemo() {
        Object[][] datos = {
                {"Carlos", "Mendoza Ríos", "45128736", 13, SolicitudSocio.APROBADA, SolicitudSocio.PAGADOR, 2},
                {"Lucía", "Paredes Soto", "47219845", 13, SolicitudSocio.APROBADA, SolicitudSocio.PAGADOR, 0},
                {"Jorge", "Salazar Vega", "41987320", 12, SolicitudSocio.NO_APTA, SolicitudSocio.RENUENTE, 1},
                {"María", "Quispe Huamán", "46031298", 12, SolicitudSocio.APROBADA, SolicitudSocio.PAGADOR, 0},
                {"Andrés", "Torres Lazo", "43876521", 11, SolicitudSocio.DESAPROBADA, SolicitudSocio.PAGADOR, 1},
                {"Valeria", "Gutiérrez Pinto", "48120934", 10, SolicitudSocio.APROBADA, SolicitudSocio.PAGADOR, 1},
                {"Ricardo", "Flores Chávez", "40219873", 10, SolicitudSocio.NO_APTA, SolicitudSocio.PAGADOR_ESPORADICO, 0},
                {"Daniela", "Rojas Medina", "45982317", 9, SolicitudSocio.APROBADA, SolicitudSocio.PAGADOR, 3},
                {"Fernando", "Castillo Ramos", "42319087", 9, SolicitudSocio.NO_APTA, SolicitudSocio.PAGADOR_ESPORADICO, 0},
                {"Camila", "Vargas León", "47653210", 8, SolicitudSocio.APROBADA, SolicitudSocio.PAGADOR, 0},
                {"Miguel", "Herrera Díaz", "44120983", 8, SolicitudSocio.DESAPROBADA, SolicitudSocio.PAGADOR, 0},
                {"Sofía", "Morales Cruz", "46871234", 7, SolicitudSocio.APROBADA, SolicitudSocio.PAGADOR, 1},
                {"Luis", "Ramírez Ortiz", "43098712", 7, SolicitudSocio.NO_APTA, SolicitudSocio.RENUENTE, 2},
                {"Gabriela", "Núñez Campos", "48765432", 6, SolicitudSocio.APROBADA, SolicitudSocio.PAGADOR, 0},
                {"Pedro", "Aguilar Reyes", "41230987", 6, SolicitudSocio.NO_APTA, SolicitudSocio.PAGADOR_ESPORADICO, 0},
                {"Alejandra", "Silva Romero", "45673821", 5, SolicitudSocio.DESAPROBADA, SolicitudSocio.PAGADOR, 1},
                {"Héctor", "Cárdenas Loayza", "42987654", 5, SolicitudSocio.VERIFICADA, SolicitudSocio.PAGADOR, 1},
                {"Paola", "Espinoza Arias", "47321098", 4, SolicitudSocio.VERIFICADA, SolicitudSocio.PAGADOR, 0},
                {"Raúl", "Navarro Salas", "44567890", 3, SolicitudSocio.VERIFICADA, SolicitudSocio.PAGADOR, 2},
                {"Elena", "Ponce Villanueva", "46098123", 3, SolicitudSocio.VERIFICADA, SolicitudSocio.PAGADOR, 0},
                {"Martín", "Chávez Benites", "43210765", 2, SolicitudSocio.PENDIENTE, null, 1},
                {"Natalia", "Ibáñez Cornejo", "48012376", 2, SolicitudSocio.PENDIENTE, null, 0},
                {"Óscar", "Valdivia Mejía", "41876543", 1, SolicitudSocio.PENDIENTE, null, 0},
                // Mismo DNI que una solicitud desaprobada → alerta de doble inscripción
                {"Miguel Ángel", "Herrera Díaz", "44120983", 1, SolicitudSocio.PENDIENTE, null, 0},
                {"Rosa", "Delgado Fuentes", "47980321", 0, SolicitudSocio.PENDIENTE, null, 1},
                {"Iván", "Muñoz Tapia", "42765109", 0, SolicitudSocio.PENDIENTE, null, 0},
        };

        Rol rolSocio = rolRepository.findByCodigo(Rol.SOCIO).orElseThrow();
        String hash = passwordEncoder.encode(passwordDemo);
        int secuencia = 9000;

        for (Object[] d : datos) {
            String nombres = (String) d[0];
            String apellidos = (String) d[1];
            String dni = (String) d[2];
            int diasAtras = (Integer) d[3];
            String estado = (String) d[4];
            String clasificacion = (String) d[5];
            int embarcaciones = (Integer) d[6];

            LocalDateTime fecha = LocalDate.now().minusDays(diasAtras).atTime(9 + (secuencia % 8), 15);
            String correo = (nombres.split(" ")[0] + "." + apellidos.split(" ")[0])
                    .toLowerCase().replace("á", "a").replace("é", "e").replace("í", "i")
                    .replace("ó", "o").replace("ú", "u").replace("ñ", "n")
                    + secuencia + "@correo.pe";

            SolicitudSocio s = new SolicitudSocio();
            secuencia++;
            s.setNombres(nombres);
            s.setApellidos(apellidos);
            s.setDni(dni);
            s.setFechaNacimiento(LocalDate.of(1975 + (secuencia % 25), 1 + (secuencia % 12), 1 + (secuencia % 27)));
            s.setCorreo(correo);
            s.setCelular("9" + dni);
            s.setDireccion("Av. La Marina " + (100 + secuencia % 900) + ", San Miguel, Lima");
            s.setPoseeEmbarcacion(embarcaciones > 0);
            s.setCantidadEmbarcaciones(embarcaciones);
            s.setPasswordHash(hash);
            s.setEstado(estado);
            s.setFechaSolicitud(fecha);

            if (clasificacion != null) {
                s.setClasificacion(clasificacion);
                s.setResultadoVerificacion(switch (clasificacion) {
                    case SolicitudSocio.PAGADOR -> "Club Regatas Lima y Club Náutico Ancón confirman pagos puntuales.";
                    case SolicitudSocio.PAGADOR_ESPORADICO -> "Club Náutico Ancón reporta pagos con atrasos frecuentes.";
                    default -> "Club Regatas Lima reporta deuda impaga de más de 6 meses.";
                });
                s.setFechaVerificacion(fecha.plusHours(20));
                s.setVerificadoPor("atencion@poseidon.pe");
            }

            if (SolicitudSocio.APROBADA.equals(estado) || SolicitudSocio.DESAPROBADA.equals(estado)) {
                s.setFechaResolucion(fecha.plusDays(1).plusHours(4));
                s.setResueltoPor("gerente@poseidon.pe");
            }
            if (SolicitudSocio.DESAPROBADA.equals(estado)) {
                s.setMotivoDesaprobacion("Documento de identidad ilegible. Debe adjuntar copia legible del DNI.");
            }
            if (SolicitudSocio.APROBADA.equals(estado) && !usuarioRepository.existsByCorreo(correo)) {
                Usuario socio = new Usuario();
                socio.setNombres(nombres);
                socio.setApellidos(apellidos);
                socio.setCorreo(correo);
                socio.setPassword(hash);
                socio.setRol(rolSocio);
                usuarioRepository.save(socio);
                s.setUsuarioId(socio.getId());
            }

            solicitudRepository.save(s);
        }
        log.info("Solicitudes de ejemplo cargadas: {}", datos.length);
    }

    private Modulo modulo(String codigo, String nombre, String ruta, String icono, int orden) {
        return moduloRepository.findByCodigo(codigo)
                .orElseGet(() -> moduloRepository.save(new Modulo(codigo, nombre, ruta, icono, orden)));
    }

    private void rol(String codigo, String nombre, List<Modulo> modulos) {
        if (rolRepository.findByCodigo(codigo).isPresent()) {
            return; // Si ya existe, se respetan los cambios hechos en la BD
        }
        Rol rol = new Rol(codigo, nombre);
        rol.getModulos().addAll(modulos);
        rolRepository.save(rol);
        log.info("Rol creado: {} con {} módulos", codigo, modulos.size());
    }


    private void migrarRolAntiguo() {
        Integer existe = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM information_schema.columns
                WHERE table_name = 'usuarios' AND column_name = 'rol'
                """, Integer.class);

        if (existe == null || existe == 0) {
            return;
        }

        int migrados = jdbcTemplate.update("""
                UPDATE usuarios u
                SET rol_id = r.id
                FROM roles r
                WHERE u.rol_id IS NULL
                  AND r.codigo = CASE WHEN u.rol = 'ADMINISTRADOR' THEN 'GERENTE' ELSE 'SOCIO' END
                """);

        jdbcTemplate.execute("ALTER TABLE usuarios DROP COLUMN rol");
        log.info("Migración de roles: {} usuarios pasaron a la tabla roles", migrados);
    }

    private void usuarioDemo(String correo, String nombres, String apellidos, String codigoRol) {
        if (usuarioRepository.existsByCorreo(correo)) {
            return;
        }
        Usuario usuario = new Usuario();
        usuario.setNombres(nombres);
        usuario.setApellidos(apellidos);
        usuario.setCorreo(correo);
        usuario.setPassword(passwordEncoder.encode(passwordDemo));
        usuario.setRol(rolRepository.findByCodigo(codigoRol).orElseThrow());
        usuarioRepository.save(usuario);
        log.info("Usuario de prueba creado: {} ({})", correo, codigoRol);
    }
}
