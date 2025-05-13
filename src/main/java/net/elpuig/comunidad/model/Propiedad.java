package net.elpuig.comunidad.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.util.Map;

@Data
@EqualsAndHashCode(exclude = {"propietario"})
public class Propiedad {
    private String codigo;
    private int metrosCuadrados;
    private Propietario propietario;
    private Map<Zona, Integer> porcentajesZona; // Zona → Porcentaje (1-100)
    private String tipo; // 'P' (Piso), 'L' (Local), 'G' (Garaje)
    private String infoAdicional; // HH/HNH, actividad, etc.
    private Map<Zona, BigDecimal> cuotas; // Zona → Cuota calculada
}
