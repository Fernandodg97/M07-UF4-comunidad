package net.elpuig.comunidad.util;

import net.elpuig.comunidad.model.*;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class CalculadoraCuotas {
    
    public void calcularCuotas(Comunidad comunidad, List<Gasto> gastos) {
        // 1. Agrupar gastos por zona
        Map<Zona, List<Gasto>> gastosPorZona = gastos.stream()
            .collect(Collectors.groupingBy(Gasto::getZona));
        
        // 2. Para cada zona, calcular reparto
        gastosPorZona.forEach((zona, gastosZona) -> {
            BigDecimal totalZona = gastosZona.stream()
                .map(Gasto::getImporte)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
                
            if (zona.getTipoReparto() == 'P') {
                calcularRepartoProporcional(zona, totalZona, comunidad);
            } else {
                calcularRepartoIgualitario(zona, totalZona, comunidad);
            }
        });
    }
    
    private void calcularRepartoProporcional(Zona zona, BigDecimal total, Comunidad comunidad) {
        // Obtener todas las propiedades que tienen porcentaje en esta zona
        List<Propiedad> propiedadesConPorcentaje = comunidad.getPropiedades().stream()
            .filter(p -> p.getPorcentajesZona().containsKey(zona))
            .collect(Collectors.toList());
            
        // Calcular el total de porcentajes para esta zona
        int totalPorcentajes = propiedadesConPorcentaje.stream()
            .mapToInt(p -> p.getPorcentajesZona().get(zona))
            .sum();
            
        // Calcular cuota por cada propiedad
        for (Propiedad propiedad : propiedadesConPorcentaje) {
            int porcentaje = propiedad.getPorcentajesZona().get(zona);
            BigDecimal cuota = total.multiply(new BigDecimal(porcentaje))
                                  .divide(new BigDecimal(totalPorcentajes), 2, RoundingMode.HALF_UP);
            
            // Actualizar la cuota en la propiedad
            if (propiedad.getCuotas() == null) {
                propiedad.setCuotas(new HashMap<>());
            }
            propiedad.getCuotas().put(zona, cuota);
        }
    }
    
    private void calcularRepartoIgualitario(Zona zona, BigDecimal total, Comunidad comunidad) {
        // Obtener todas las propiedades que tienen porcentaje en esta zona
        List<Propiedad> propiedadesConPorcentaje = comunidad.getPropiedades().stream()
            .filter(p -> p.getPorcentajesZona().containsKey(zona))
            .collect(Collectors.toList());
            
        // Calcular cuota igual para cada propiedad
        BigDecimal cuotaPorPropiedad = total.divide(
            new BigDecimal(propiedadesConPorcentaje.size()), 
            2, 
            RoundingMode.HALF_UP
        );
        
        // Asignar cuota a cada propiedad
        for (Propiedad propiedad : propiedadesConPorcentaje) {
            if (propiedad.getCuotas() == null) {
                propiedad.setCuotas(new HashMap<>());
            }
            propiedad.getCuotas().put(zona, cuotaPorPropiedad);
        }
    }
    
    public Map<Propietario, Map<Zona, BigDecimal>> calcularCuotasPorPropietario(Comunidad comunidad) {
        Map<Propietario, Map<Zona, BigDecimal>> cuotasPorPropietario = new HashMap<>();
        
        // Para cada propietario
        for (Propietario propietario : comunidad.getPropietarios()) {
            Map<Zona, BigDecimal> cuotasPropietario = new HashMap<>();
            
            // Sumar las cuotas de todas sus propiedades
            for (Propiedad propiedad : propietario.getPropiedades()) {
                if (propiedad.getCuotas() != null) {
                    propiedad.getCuotas().forEach((zona, cuota) -> {
                        cuotasPropietario.merge(zona, cuota, BigDecimal::add);
                    });
                }
            }
            
            cuotasPorPropietario.put(propietario, cuotasPropietario);
        }
        
        return cuotasPorPropietario;
    }
    
    public void generarResumen(Comunidad comunidad, List<Gasto> gastos) {
        // Calcular totales por zona
        Map<Zona, BigDecimal> totalesPorZona = gastos.stream()
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