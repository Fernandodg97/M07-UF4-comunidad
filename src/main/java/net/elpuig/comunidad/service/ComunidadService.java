package net.elpuig.comunidad.service;

import net.elpuig.comunidad.model.*;
import net.elpuig.comunidad.util.FileParser;
import net.elpuig.comunidad.util.CalculadoraCuotas;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class ComunidadService {
    
    @Autowired
    private FileParser fileParser;
    
    @Autowired
    private CalculadoraCuotas calculadoraCuotas;
    
    private Comunidad comunidadActual;
    
    public Comunidad procesarArchivos(MultipartFile comunidadFile, MultipartFile gastosFile) throws IOException {
        // Procesar archivo de comunidad
        Comunidad comunidad = fileParser.parseComunidad(comunidadFile.getInputStream());
        
        // Procesar archivo de gastos
        List<Gasto> gastos = fileParser.parseGastos(gastosFile.getInputStream(), comunidad);
        
        // Calcular cuotas
        calculadoraCuotas.calcularCuotas(comunidad, gastos);
        
        // Generar resumen
        calculadoraCuotas.generarResumen(comunidad, gastos);
        
        // Guardar comunidad actual
        this.comunidadActual = comunidad;
        
        return comunidad;
    }
    
    public Comunidad getComunidadActual() {
        return comunidadActual;
    }
    
    public Map<Propietario, Map<Zona, BigDecimal>> calcularCuotasPorPropietario(Comunidad comunidad) {
        return calculadoraCuotas.calcularCuotasPorPropietario(comunidad);
    }
}
