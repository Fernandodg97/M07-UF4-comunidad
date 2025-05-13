package net.elpuig.comunidad.controller;

import net.elpuig.comunidad.model.*;
import net.elpuig.comunidad.service.ComunidadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.TreeMap;

/**
 * Controlador principal que maneja las peticiones relacionadas con la comunidad de propietarios.
 * Este controlador gestiona la carga de archivos, visualización de propiedades, propietarios,
 * cuotas y resumen de la comunidad.
 */
@Controller
public class ComunidadController {
    
    @Autowired
    private ComunidadService comunidadService;

    /**
     * Muestra el formulario inicial para cargar los archivos de la comunidad.
     * 
     * @return Nombre de la vista "upload" que contiene el formulario de carga
     */
    @GetMapping("/")
    public String mostrarFormulario() {
        return "upload";
    }

    /**
     * Procesa los archivos subidos de comunidad y gastos.
     * Realiza validaciones sobre los archivos y los procesa para generar la información de la comunidad.
     * 
     * @param comunidadFile Archivo con la información de la comunidad
     * @param gastosFile Archivo con la información de los gastos
     * @param model Modelo para añadir atributos a la vista
     * @return Nombre de la vista a mostrar ("upload" en caso de error, "resumen" si todo es correcto)
     */
    @PostMapping("/procesar")
    public String procesarArchivos(
            @RequestParam("comunidadFile") MultipartFile comunidadFile,
            @RequestParam("gastosFile") MultipartFile gastosFile,
            Model model) {
        
        try {
            // Verificar que los archivos no estén vacíos
            if (comunidadFile.isEmpty()) {
                model.addAttribute("error", "El archivo de comunidad está vacío");
                return "upload";
            }
            
            if (gastosFile.isEmpty()) {
                model.addAttribute("error", "El archivo de gastos está vacío");
                return "upload";
            }
            
            // Verificar los nombres de archivo para asegurar que son los correctos
            if (!comunidadFile.getOriginalFilename().toLowerCase().contains("comun") 
                && !comunidadFile.getOriginalFilename().toLowerCase().contains("comuni")) {
                model.addAttribute("error", "El archivo seleccionado no parece ser un archivo de comunidad válido");
                return "upload";
            }
            
            if (!gastosFile.getOriginalFilename().toLowerCase().contains("gast") 
                && !gastosFile.getOriginalFilename().toLowerCase().contains("despes")) {
                model.addAttribute("error", "El archivo seleccionado no parece ser un archivo de gastos válido");
                return "upload";
            }
            
            // Procesar los archivos y generar la información de la comunidad
            Comunidad comunidad = comunidadService.procesarArchivos(comunidadFile, gastosFile);
            model.addAttribute("comunidad", comunidad);
            return "resumen";
            
        } catch (IllegalArgumentException e) {
            // Error de formato de archivo
            model.addAttribute("error", e.getMessage());
            return "upload";
        } catch (Exception e) {
            // Otro tipo de error no esperado
            model.addAttribute("error", "Error al procesar archivos: " + e.getMessage());
            return "upload";
        }
    }

    /**
     * Muestra la lista de propiedades de la comunidad.
     * Si no hay comunidad cargada, redirige al inicio.
     * 
     * @param model Modelo para añadir atributos a la vista
     * @return Nombre de la vista a mostrar ("propiedades" o redirección a "/")
     */
    @GetMapping("/propiedades")
    public String mostrarPropiedades(Model model) {
        Comunidad comunidad = comunidadService.getComunidadActual();
        if (comunidad != null) {
            model.addAttribute("comunidad", comunidad);
            model.addAttribute("propiedades", comunidad.getPropiedades());
            return "propiedades";
        }
        return "redirect:/";
    }

    /**
     * Muestra la lista de propietarios de la comunidad.
     * Si no hay comunidad cargada, redirige al inicio.
     * 
     * @param model Modelo para añadir atributos a la vista
     * @return Nombre de la vista a mostrar ("propietarios" o redirección a "/")
     */
    @GetMapping("/propietarios")
    public String mostrarPropietarios(Model model) {
        Comunidad comunidad = comunidadService.getComunidadActual();
        if (comunidad != null) {
            model.addAttribute("comunidad", comunidad);
            model.addAttribute("propietarios", comunidadService.getPropietariosOrdenados());
            return "propietarios";
        }
        return "redirect:/";
    }

    /**
     * Muestra las cuotas calculadas para cada propietario y propiedad.
     * Si no hay comunidad cargada, redirige al inicio.
     * 
     * @param model Modelo para añadir atributos a la vista
     * @return Nombre de la vista a mostrar ("cuotas" o redirección a "/")
     */
    @GetMapping("/cuotas")
    public String mostrarCuotas(Model model) {
        Comunidad comunidad = comunidadService.getComunidadActual();
        if (comunidad != null) {
            Map<Propietario, Map<Zona, BigDecimal>> cuotasPorPropietario = 
                comunidadService.calcularCuotasPorPropietario(comunidad);
            
            // Ordenar el mapa por código de propietario
            Map<Propietario, Map<Zona, BigDecimal>> cuotasOrdenadas = new TreeMap<>(
                Comparator.comparing(Propietario::getCodigo)
            );
            cuotasOrdenadas.putAll(cuotasPorPropietario);
            
            model.addAttribute("comunidad", comunidad);
            model.addAttribute("cuotasPorPropietario", cuotasOrdenadas);
            return "cuotas";
        }
        return "redirect:/";
    }

    /**
     * Muestra el resumen general de la comunidad.
     * Si no hay comunidad cargada, redirige al inicio.
     * 
     * @param model Modelo para añadir atributos a la vista
     * @return Nombre de la vista a mostrar ("resumen" o redirección a "/")
     */
    @GetMapping("/resumen")
    public String mostrarResumen(Model model) {
        Comunidad comunidad = comunidadService.getComunidadActual();
        if (comunidad != null) {
            model.addAttribute("comunidad", comunidad);
            return "resumen";
        }
        return "redirect:/";
    }
}
