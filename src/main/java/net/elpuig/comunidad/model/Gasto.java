package net.elpuig.comunidad.model;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class Gasto {
    private String id;
    private String descripcion;
    private BigDecimal importe;
    private Zona zona;
}
