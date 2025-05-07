package net.elpuig.comunidad.util;

import net.elpuig.comunidad.model.*;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Componente encargado de calcular las cuotas de la comunidad de propietarios.
 * Esta clase implementa la lógica para:
 * - Calcular cuotas por zona según el tipo de reparto (proporcional o igualitario)
 * - Calcular cuotas por propietario
 * - Generar resúmenes de gastos por zona y totales
 */
@Component
public class CalculadoraCuotas {
    
    /**
     * Calcula las cuotas para todas las zonas de la comunidad.
     * El cálculo se realiza agrupando los gastos por zona y aplicando el tipo de reparto
     * correspondiente (proporcional o igualitario).
     *
     * @param comunidad La comunidad para la cual calcular las cuotas
     * @param gastos Lista de gastos a repartir
     */
    public void calcularCuotas(Comunidad comunidad, List<Gasto> gastos) {
        // 1. Agrupar gastos por zona
        Map<Zona, List<Gasto>> gastosPorZona = gastos.stream()
            .collect(Collectors.groupingBy(Gasto::getZona));
        
        // 2. Para cada zona, calcular reparto según su tipo
        gastosPorZona.forEach((zona, gastosZona) -> {
            // System.out.println("Procesando zona: " + zona.getNombre() + 
            //           " - Tipo reparto: " + zona.getTipoReparto());
            // Calcular el total de gastos para esta zona
            BigDecimal totalZona = gastosZona.stream()
                .map(Gasto::getImporte)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
                
            // Aplicar el tipo de reparto correspondiente
            if (zona.getTipoReparto() == 'P') {
                calcularRepartoProporcional(zona, totalZona, comunidad);
            } else {
                calcularRepartoIgualitario(zona, totalZona, comunidad);
            }
        });
    }
    
    /**
     * Calcula el reparto proporcional de los gastos de una zona entre las propiedades.
     * El reparto se realiza según los porcentajes asignados a cada propiedad en la zona.
     *
     * @param zona Zona para la cual calcular el reparto
     * @param total Importe total a repartir
     * @param comunidad Comunidad a la que pertenece la zona
     */
    private void calcularRepartoProporcional(Zona zona, BigDecimal total, Comunidad comunidad) {
        // Verificar que haya propiedades
        if (comunidad.getPropiedades() == null || comunidad.getPropiedades().isEmpty()) {
            return;
        }
        
        // Obtener todas las propiedades que tienen porcentaje en esta zona
        List<Propiedad> propiedadesConPorcentaje = comunidad.getPropiedades().stream()
            .filter(p -> p.getPorcentajesZona() != null && p.getPorcentajesZona().containsKey(zona))
            .collect(Collectors.toList());
            
        // Si no hay propiedades con porcentaje, no hay nada que hacer
        if (propiedadesConPorcentaje.isEmpty()) {
            return;
        }
        
        // Calcular el total de porcentajes para esta zona
        int totalPorcentajes = propiedadesConPorcentaje.stream()
            .mapToInt(p -> p.getPorcentajesZona().get(zona))
            .sum();
            
        // Verificar que el total no sea cero
        if (totalPorcentajes == 0) {
            return;
        }
        
        // Calcular cuota por cada propiedad según su porcentaje
        for (Propiedad propiedad : propiedadesConPorcentaje) {
            int porcentaje = propiedad.getPorcentajesZona().get(zona);
            BigDecimal cuota = total.multiply(new BigDecimal(porcentaje))
                                  .divide(new BigDecimal(totalPorcentajes), 2, RoundingMode.UP);
            
            // Actualizar la cuota en la propiedad
            if (propiedad.getCuotas() == null) {
                propiedad.setCuotas(new HashMap<>());
            }
            propiedad.getCuotas().put(zona, cuota);
        }
    }
    
    /**
     * Calcula el reparto igualitario de los gastos de una zona entre las propiedades.
     * El importe total se divide equitativamente entre todas las propiedades de la zona.
     *
     * @param zona Zona para la cual calcular el reparto
     * @param total Importe total a repartir
     * @param comunidad Comunidad a la que pertenece la zona
     */
    private void calcularRepartoIgualitario(Zona zona, BigDecimal total, Comunidad comunidad) {
        // Verificar que haya propiedades
        if (comunidad.getPropiedades() == null || comunidad.getPropiedades().isEmpty()) {
            return;
        }
        
        // Obtener todas las propiedades que tienen porcentaje en esta zona
        List<Propiedad> propiedadesConPorcentaje = comunidad.getPropiedades().stream()
            .filter(p -> p.getPorcentajesZona() != null && p.getPorcentajesZona().containsKey(zona))
            .collect(Collectors.toList());
            
        // Si no hay propiedades con porcentaje, no hay nada que hacer
        if (propiedadesConPorcentaje.isEmpty()) {
            return;
        }
        
        // Calcular cuota igual para cada propiedad
        BigDecimal cuotaPorPropiedad = total.divide(
            new BigDecimal(propiedadesConPorcentaje.size()), 
            2, 
            RoundingMode.UP
        );
        
        // Asignar cuota a cada propiedad
        for (Propiedad propiedad : propiedadesConPorcentaje) {
            if (propiedad.getCuotas() == null) {
                propiedad.setCuotas(new HashMap<>());
            }
            propiedad.getCuotas().put(zona, cuotaPorPropiedad);
        }
    }
    
    /**
     * Calcula las cuotas totales por propietario, sumando las cuotas de todas sus propiedades.
     * El resultado es un mapa que asocia cada propietario con sus cuotas por zona.
     *
     * @param comunidad Comunidad para la cual calcular las cuotas
     * @return Mapa con las cuotas por propietario y zona
     */
    public Map<Propietario, Map<Zona, BigDecimal>> calcularCuotasPorPropietario(Comunidad comunidad) {
        Map<Propietario, Map<Zona, BigDecimal>> cuotasPorPropietario = new HashMap<>();
        
        // Para cada propietario
        for (Propietario propietario : comunidad.getPropietarios()) {
            Map<Zona, BigDecimal> cuotasPropietario = new HashMap<>();
            
            // Verificar que el propietario tenga propiedades
            if (propietario.getPropiedades() != null && !propietario.getPropiedades().isEmpty()) {
                // Sumar las cuotas de todas sus propiedades
                for (Propiedad propiedad : propietario.getPropiedades()) {
                    if (propiedad.getCuotas() != null) {
                        propiedad.getCuotas().forEach((zona, cuota) -> {
                            cuotasPropietario.merge(zona, cuota, BigDecimal::add);
                        });
                    }
                }
            }
            
            cuotasPorPropietario.put(propietario, cuotasPropietario);
        }
        
        return cuotasPorPropietario;
    }
    
    /**
     * Genera un resumen de los gastos de la comunidad, calculando:
     * - Totales por zona
     * - Total general de gastos
     *
     * @param comunidad Comunidad para la cual generar el resumen
     * @param gastos Lista de gastos a procesar
     */
    public void generarResumen(Comunidad comunidad, List<Gasto> gastos) {
        // Verificar que haya gastos
        if (gastos == null || gastos.isEmpty()) {
            comunidad.setTotalesPorZona(new HashMap<>());
            comunidad.setTotalGeneral(BigDecimal.ZERO);
            return;
        }
        
        // Calcular totales por zona
        Map<Zona, BigDecimal> totalesPorZona = gastos.stream()
            .filter(gasto -> gasto.getZona() != null) // Filtrar gastos sin zona
            .collect(Collectors.groupingBy(
                Gasto::getZona,
                Collectors.mapping(
                    Gasto::getImporte,
                    Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                )
            ));
            
        // Calcular total general
        BigDecimal totalGeneral = totalesPorZona.values().stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        // Actualizar resumen en la comunidad
        comunidad.setTotalesPorZona(totalesPorZona);
        comunidad.setTotalGeneral(totalGeneral);
    }
} 