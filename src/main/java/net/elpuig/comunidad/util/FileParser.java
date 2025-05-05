package net.elpuig.comunidad.util;

import net.elpuig.comunidad.model.*;
import org.springframework.stereotype.Component;
import java.io.*;
import java.math.BigDecimal;
import java.util.*;

@Component
public class FileParser {
    
    public Comunidad parseComunidad(InputStream input) throws IOException {
        Comunidad comunidad = new Comunidad();
        Map<String, Zona> zonasMap = new HashMap<>();
        Map<String, Propietario> propietariosMap = new HashMap<>();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
            String line;
            String currentSection = null;
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                if (line.startsWith("#")) {
                    currentSection = line;
                    continue;
                }
                
                switch(currentSection) {
                    case "#Comunitat":
                        parseComunidadLine(line, comunidad);
                        break;
                    case "#Zona":
                        parseZonaLine(line, zonasMap);
                        break;
                    case "#Propietat":
                        parsePropiedadLine(line, comunidad, zonasMap, propietariosMap);
                        break;
                    case "#Propietari":
                        parsePropietarioLine(line, propietariosMap);
                        break;
                }
            }
        }
        
        // Actualizar las listas en la comunidad
        comunidad.setZonas(new ArrayList<>(zonasMap.values()));
        comunidad.setPropietarios(new ArrayList<>(propietariosMap.values()));
        
        return comunidad;
    }

    private void parseComunidadLine(String line, Comunidad comunidad) {
        String[] parts = line.split(";");
        if (parts.length >= 3) {
            comunidad.setId(parts[0]);
            comunidad.setNombre(parts[1]);
            comunidad.setPoblacion(parts[2]);
        }
    }

    private void parseZonaLine(String line, Map<String, Zona> zonasMap) {
        String[] parts = line.split(";");
        if (parts.length >= 3) {
            Zona zona = new Zona();
            zona.setId(parts[0]);
            zona.setNombre(parts[1]);
            zona.setTipoReparto(parts[2].charAt(0));
            zonasMap.put(zona.getId(), zona);
        }
    }

    private void parsePropiedadLine(String line, Comunidad comunidad, 
                                  Map<String, Zona> zonasMap, 
                                  Map<String, Propietario> propietariosMap) {
        String[] parts = line.split(";");
        if (parts.length >= 7) {
            Propiedad propiedad = new Propiedad();
            propiedad.setTipo(parts[0]);
            propiedad.setCodigo(parts[1]);
            propiedad.setMetrosCuadrados(Integer.parseInt(parts[2]));
            
            // Asignar propietario
            String codigoPropietario = parts[3];
            Propietario propietario = propietariosMap.get(codigoPropietario);
            if (propietario != null) {
                propiedad.setPropietario(propietario);
                if (propietario.getPropiedades() == null) {
                    propietario.setPropiedades(new ArrayList<>());
                }
                propietario.getPropiedades().add(propiedad);
            }
            
            // Procesar porcentajes por zona
            Map<Zona, Integer> porcentajes = new HashMap<>();
            String[] porcentajesStr = parts[4].split(",");
            for (String porcentaje : porcentajesStr) {
                String[] p = porcentaje.split("-");
                if (p.length == 2) {
                    Zona zona = zonasMap.get(p[0]);
                    if (zona != null) {
                        porcentajes.put(zona, Integer.parseInt(p[1]));
                    }
                }
            }
            propiedad.setPorcentajesZona(porcentajes);
            
            // Información adicional
            propiedad.setInfoAdicional(parts[5] + (parts.length > 6 ? ";" + parts[6] : ""));
            
            if (comunidad.getPropiedades() == null) {
                comunidad.setPropiedades(new ArrayList<>());
            }
            comunidad.getPropiedades().add(propiedad);
        }
    }

    private void parsePropietarioLine(String line, Map<String, Propietario> propietariosMap) {
        String[] parts = line.split(";");
        if (parts.length >= 4) {
            Propietario propietario = new Propietario();
            propietario.setCodigo(parts[0]);
            propietario.setNombre(parts[1]);
            propietario.setDireccion(parts[2]);
            propietario.setEmail(parts[3]);
            propietario.setPropiedades(new ArrayList<>());
            propietariosMap.put(propietario.getCodigo(), propietario);
        }
    }

    public List<Gasto> parseGastos(InputStream input, Comunidad comunidad) throws IOException {
        List<Gasto> gastos = new ArrayList<>();
        Map<String, Zona> zonasMap = new HashMap<>();
        comunidad.getZonas().forEach(zona -> zonasMap.put(zona.getId(), zona));
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                
                String[] parts = line.split(";");
                if (parts.length >= 4) {
                    Gasto gasto = new Gasto();
                    gasto.setId(parts[0]);
                    gasto.setDescripcion(parts[1]);
                    gasto.setImporte(new BigDecimal(parts[2]));
                    gasto.setZona(zonasMap.get(parts[3]));
                    gastos.add(gasto);
                }
            }
        }
        return gastos;
    }
}
