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
import java.util.ArrayList;
import java.util.Comparator;

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
        
        // Guardar gastos en la comunidad
        comunidad.setGastos(gastos);
        
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
    
    /**
     * Devuelve los propietarios ordenados por código
     */
    public List<Propietario> getPropietariosOrdenados() {
        if (comunidadActual == null || comunidadActual.getPropietarios() == null) {
            return new ArrayList<>();
        }
        
        List<Propietario> propietariosOrdenados = new ArrayList<>(comunidadActual.getPropietarios());
        propietariosOrdenados.sort(Comparator.comparing(Propietario::getCodigo));
        return propietariosOrdenados;
    }
    
    public Map<Propietario, Map<Zona, BigDecimal>> calcularCuotasPorPropietario(Comunidad comunidad) {
        return calculadoraCuotas.calcularCuotasPorPropietario(comunidad);
    }
}
