package net.elpuig.comunidad.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

@Data
@EqualsAndHashCode(exclude = {"propiedades"})
public class Propietario {
    private String codigo;
    private String nombre;
    private String direccion;
    private String email;
    private List<Propiedad> propiedades;
}
