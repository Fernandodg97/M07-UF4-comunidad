package net.elpuig.comunidad.model;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class Comunidad {
    private String id;
    private String nombre;
    private String poblacion;
    private List<Zona> zonas;
    private List<Propiedad> propiedades;
    private List<Propietario> propietarios;
    private List<Gasto> gastos;
    private Map<Zona, BigDecimal> totalesPorZona;
    private BigDecimal totalGeneral;
}
