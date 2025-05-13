package net.elpuig.comunidad.model;

import lombok.Data;

@Data
public class Zona {
    private String id;
    private String nombre;
    private char tipoReparto; // 'P' (Proporcional) o 'I' (Igualitario)
}
