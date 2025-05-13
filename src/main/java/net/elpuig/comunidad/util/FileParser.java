package net.elpuig.comunidad.util;

import net.elpuig.comunidad.model.*;
import org.springframework.stereotype.Component;
import java.io.*;
import java.math.BigDecimal;
import java.util.*;

/**
 * Componente encargado de analizar y procesar los archivos de entrada de la comunidad.
 * Este parser maneja dos tipos de archivos:
 * 1. Archivo de comunidad: Contiene información sobre la comunidad, zonas, propietarios y propiedades
 * 2. Archivo de gastos: Contiene información sobre los gastos de la comunidad
 */
@Component
public class FileParser {
    
    /**
     * Analiza el archivo de comunidad y crea la estructura de datos correspondiente.
     * El archivo debe seguir un formato específico con secciones marcadas con #:
     * - #Comunidad: Información general de la comunidad
     * - #Zona: Definición de zonas
     * - #Propietario: Información de propietarios
     * - #Propiedad: Información de propiedades
     *
     * @param input Stream de entrada con el contenido del archivo
     * @return Objeto Comunidad con toda la información procesada
     * @throws IOException Si hay un error al leer el archivo
     * @throws IllegalArgumentException Si el formato del archivo no es válido
     */
    public Comunidad parseComunidad(InputStream input) throws IOException {
        Comunidad comunidad = new Comunidad();
        Map<String, Zona> zonasMap = new HashMap<>();
        Map<String, Propietario> propietariosMap = new HashMap<>();
        List<String> propiedadesLines = new ArrayList<>();
        boolean formatoValido = false;
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
            String line;
            String currentSection = null;
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                // Detectar secciones del archivo
                if (line.startsWith("#")) {
                    currentSection = line;
                    if ("#Comunitat".equals(currentSection) || "#Comunidad".equals(currentSection)) {
                        formatoValido = true;
                    }
                    continue;
                }
                
                // Procesar línea según la sección actual
                switch(currentSection) {
                    case "#Comunitat":
                    case "#Comunidad":
                        parseComunidadLine(line, comunidad);
                        break;
                    case "#Zona":
                        parseZonaLine(line, zonasMap);
                        break;
                    case "#Propietat":
                    case "#Propiedad":
                        propiedadesLines.add(line);
                        break;
                    case "#Propietari":
                    case "#Propietario":
                        parsePropietarioLine(line, propietariosMap);
                        break;
                }
            }
        }
        
        if (!formatoValido) {
            throw new IllegalArgumentException("El archivo de comunidad no tiene un formato válido.");
        }
        
        // Configurar la comunidad con los datos procesados
        comunidad.setZonas(new ArrayList<>(zonasMap.values()));
        
        // Ordenar propietarios por código
        List<Propietario> propietariosOrdenados = new ArrayList<>(propietariosMap.values());
        propietariosOrdenados.sort(Comparator.comparing(Propietario::getCodigo));
        comunidad.setPropietarios(propietariosOrdenados);
        
        comunidad.setPropiedades(new ArrayList<>());
        
        // Procesar las propiedades
        for (String propiedadLine : propiedadesLines) {
            parsePropiedadLine(propiedadLine, comunidad, zonasMap, propietariosMap);
        }
        
        return comunidad;
    }

    /**
     * Procesa una línea de información de la comunidad.
     * Formato esperado: id;nombre;poblacion
     */
    private void parseComunidadLine(String line, Comunidad comunidad) {
        String[] parts = line.split(";");
        if (parts.length >= 3) {
            comunidad.setId(parts[0]);
            comunidad.setNombre(parts[1]);
            comunidad.setPoblacion(parts[2]);
        }
    }

    /**
     * Procesa una línea de información de zona.
     * Formato esperado: id;nombre;tipoReparto
     */
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

    /**
     * Procesa una línea de información de propiedad.
     * Formato esperado: tipo;codigo;metros;propietario;porcentajes;infoAdicional;infoExtra
     * 
     * @param line Línea a procesar
     * @param comunidad Comunidad a la que pertenece la propiedad
     * @param zonasMap Mapa de zonas para relacionar porcentajes
     * @param propietariosMap Mapa de propietarios para asignar la propiedad
     */
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
            
            // Procesar información adicional
            String infoAdicional = parts[5];
            if (parts.length > 6) {
                infoAdicional += ";" + parts[6];
            }
            
            // Traducir códigos de información adicional
            if ("HH".equals(parts[5])) {
                infoAdicional = "Habitaje habitual;" + (parts.length > 6 ? parts[6] : "");
            } else if ("HNH".equals(parts[5])) {
                infoAdicional = "Habitaje no habitual;" + (parts.length > 6 ? parts[6] : "");
            } else if ("G".equals(propiedad.getTipo())) {
                if ("A".equals(parts[5])) {
                    infoAdicional = "Abierta;" + (parts.length > 6 ? parts[6] : "");
                } else if ("C".equals(parts[5])) {
                    infoAdicional = "Cerrada;" + (parts.length > 6 ? parts[6] : "");
                }
            }
            
            propiedad.setInfoAdicional(infoAdicional);
            
            // Añadir propiedad a la comunidad
            if (comunidad.getPropiedades() == null) {
                comunidad.setPropiedades(new ArrayList<>());
            }
            comunidad.getPropiedades().add(propiedad);
        }
    }

    /**
     * Procesa una línea de información de propietario.
     * Formato esperado: codigo;nombre;direccion;email
     */
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

    /**
     * Analiza el archivo de gastos y crea la lista de gastos correspondiente.
     * El archivo debe comenzar con #Presupuesto o #Pressupost y contener líneas con el formato:
     * id;descripcion;importe;zona
     *
     * @param input Stream de entrada con el contenido del archivo
     * @param comunidad Comunidad a la que pertenecen los gastos
     * @return Lista de gastos procesados
     * @throws IOException Si hay un error al leer el archivo
     * @throws IllegalArgumentException Si el formato del archivo no es válido o hay datos incorrectos
     */
    public List<Gasto> parseGastos(InputStream input, Comunidad comunidad) throws IOException {
        List<Gasto> gastos = new ArrayList<>();
        Map<String, Zona> zonasMap = new HashMap<>();
        comunidad.getZonas().forEach(zona -> zonasMap.put(zona.getId(), zona));
        boolean formatoValido = false;
        int lineasProcesadas = 0;
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                // Verificar formato del archivo
                if (line.startsWith("#")) {
                    if (line.startsWith("#Pressupost") || line.startsWith("#Presupuesto")) {
                        formatoValido = true;
                    }
                    continue;
                }
                
                // Procesar línea de gasto
                String[] parts = line.split(";");
                if (parts.length >= 4) {
                    lineasProcesadas++;
                    Gasto gasto = new Gasto();
                    gasto.setId(parts[0]);
                    gasto.setDescripcion(parts[1]);
                    try {
                        gasto.setImporte(new BigDecimal(parts[2]));
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Error en formato de importe para el gasto " + parts[0]);
                    }
                    
                    // Verificar que la zona existe
                    Zona zona = zonasMap.get(parts[3]);
                    if (zona == null) {
                        throw new IllegalArgumentException("La zona " + parts[3] + " no existe para el gasto " + parts[0]);
                    }
                    gasto.setZona(zona);
                    gastos.add(gasto);
                }
            }
        }
        
        if (!formatoValido && lineasProcesadas == 0) {
            throw new IllegalArgumentException("El archivo de gastos no tiene un formato válido.");
        }
        
        return gastos;
    }
}
