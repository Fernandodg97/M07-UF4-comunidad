package net.elpuig.comunidad.model;

import lombok.Data;
import java.util.List;

@Data
public class Propietario {
    private String codigo;
    private String nombre;
    private String direccion;
    private String email;
    private List<Propiedad> propiedades;
}
