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
            Comunidad comunidad = comunidadService.procesarArchivos(comunidadFile, gastosFile);
            model.addAttribute("comunidad", comunidad);
            return "resumen";
            
        } catch (Exception e) {
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
            model.addAttribute("propietarios", comunidad.getPropietarios());
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
