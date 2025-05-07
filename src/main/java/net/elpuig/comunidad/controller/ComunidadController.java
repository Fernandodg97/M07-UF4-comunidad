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

@Controller
public class ComunidadController {
    
    @Autowired
    private ComunidadService comunidadService;

    @GetMapping("/")
    public String mostrarFormulario() {
        return "upload";
    }

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
            
            // Verificar los nombres de archivo (opcional)
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
            
            // Procesar archivos
            Comunidad comunidad = comunidadService.procesarArchivos(comunidadFile, gastosFile);
            model.addAttribute("comunidad", comunidad);
            return "resumen";
            
        } catch (IllegalArgumentException e) {
            // Error de formato de archivo
            model.addAttribute("error", e.getMessage());
            return "upload";
        } catch (Exception e) {
            // Otro tipo de error
            model.addAttribute("error", "Error al procesar archivos: " + e.getMessage());
            return "upload";
        }
    }

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

    @GetMapping("/cuotas")
    public String mostrarCuotas(Model model) {
        Comunidad comunidad = comunidadService.getComunidadActual();
        if (comunidad != null) {
            Map<Propietario, Map<Zona, BigDecimal>> cuotasPorPropietario = 
                comunidadService.calcularCuotasPorPropietario(comunidad);
            
            model.addAttribute("comunidad", comunidad);
            model.addAttribute("cuotasPorPropietario", cuotasPorPropietario);
            return "cuotas";
        }
        return "redirect:/";
    }

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
