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

/**
 * Servicio principal que gestiona la lógica de negocio relacionada con la comunidad de propietarios.
 * Este servicio se encarga de procesar los archivos de entrada, calcular las cuotas y mantener
 * el estado actual de la comunidad.
 */
@Service
public class ComunidadService {
    
    @Autowired
    private FileParser fileParser;
    
    @Autowired
    private CalculadoraCuotas calculadoraCuotas;
    
    /** Instancia actual de la comunidad siendo procesada */
    private Comunidad comunidadActual;
    
    /**
     * Procesa los archivos de comunidad y gastos para generar la información completa de la comunidad.
     * Este método realiza las siguientes operaciones:
     * 1. Parsea el archivo de comunidad
     * 2. Parsea el archivo de gastos
     * 3. Calcula las cuotas
     * 4. Genera el resumen
     * 5. Almacena la comunidad actual
     *
     * @param comunidadFile Archivo con la información de la comunidad
     * @param gastosFile Archivo con la información de los gastos
     * @return La comunidad procesada con toda su información
     * @throws IOException Si ocurre algún error al leer los archivos
     */
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
    
    /**
     * Obtiene la instancia actual de la comunidad.
     * 
     * @return La comunidad actual o null si no hay ninguna comunidad cargada
     */
    public Comunidad getComunidadActual() {
        return comunidadActual;
    }
    
    /**
     * Devuelve una lista ordenada de propietarios por su código.
     * Si no hay comunidad cargada o no hay propietarios, devuelve una lista vacía.
     * 
     * @return Lista ordenada de propietarios
     */
    public List<Propietario> getPropietariosOrdenados() {
        if (comunidadActual == null || comunidadActual.getPropietarios() == null) {
            return new ArrayList<>();
        }
        
        List<Propietario> propietariosOrdenados = new ArrayList<>(comunidadActual.getPropietarios());
        propietariosOrdenados.sort(Comparator.comparing(Propietario::getCodigo));
        return propietariosOrdenados;
    }
    
    /**
     * Calcula las cuotas por propietario y zona para una comunidad dada.
     * 
     * @param comunidad La comunidad para la cual calcular las cuotas
     * @return Mapa con las cuotas por propietario y zona
     */
    public Map<Propietario, Map<Zona, BigDecimal>> calcularCuotasPorPropietario(Comunidad comunidad) {
        return calculadoraCuotas.calcularCuotasPorPropietario(comunidad);
    }
}
