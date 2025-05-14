# Proyecto Comunidad

## Descripción
Este es un proyecto web desarrollado con Spring Boot que forma parte del módulo M07 - UF4. El proyecto ha sido desarrollado por Fernando Diaz y Mouad Sedjari.

## Tecnologías Utilizadas
- Java 21
- Spring Boot 3.4.5
- Maven
- Thymeleaf
- Spring Web
- Spring Validation
- Spring DevTools
- Lombok

## Estructura del Proyecto
src/
├── main/
│ ├── java/
│ │ └── net/
│ │ └── elpuig/
│ │ └── comunidad/
│ │ ├── controller/ # Controladores de la aplicación
│ │ ├── model/ # Modelos de datos
│ │ ├── service/ # Servicios de negocio
│ │ ├── util/ # Utilidades
│ │ ├── ComunidadApplication.java
│ │ └── ServletInitializer.java
│ └── resources/ # Recursos estáticos y plantillas
└── test/ # Pruebas unitarias

## Requisitos Previos
- Java 21
- Maven

## Configuración del Proyecto
1. Clonar el repositorio
2. Ejecutar `mvn clean install` para instalar las dependencias
3. Ejecutar `mvn spring-boot:run` para iniciar la aplicación

## Características
- Arquitectura MVC (Model-View-Controller)
- Validación de datos
- Desarrollo con Spring Boot
- Soporte para desarrollo en tiempo real con Spring DevTools

## Dependencias Principales
- spring-boot-starter-web: Para desarrollo web
- spring-boot-starter-thymeleaf: Motor de plantillas
- spring-boot-starter-validation: Validación de datos
- spring-boot-devtools: Herramientas de desarrollo
- lombok: Reducción de código boilerplate

## Componentes Principales

### CalculadoraCuotas
Componente encargado de calcular las cuotas de la comunidad de propietarios. Implementa la lógica para:
- Calcular cuotas por zona según el tipo de reparto (proporcional o igualitario)
- Calcular cuotas por propietario
- Generar resúmenes de gastos por zona y totales

### FileParser
Componente encargado de analizar y procesar los archivos de entrada de la comunidad. Maneja dos tipos de archivos:
1. Archivo de comunidad: Contiene información sobre la comunidad, zonas, propietarios y propiedades
2. Archivo de gastos: Contiene información sobre los gastos de la comunidad

El parser espera un formato específico en los archivos:
- Archivo comunidad: Secciones marcadas con # (#Comunidad, #Zona, #Propietario, #Propiedad)
- Archivo gastos: Comienza con #Presupuesto y contiene líneas con formato id;descripcion;importe;zona

## Licencia
Este proyecto está bajo licencia. Consultar los detalles en el archivo de licencia.

## Autores
- [@Fernandodg97](https://github.com/Fernandodg97)
- [@Msedjari](https://github.com/Msedjari)