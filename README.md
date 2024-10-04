# NanoFiles

## Descripción

NanoFiles es un sistema de transferencia de archivos desarrollado en Java. Este sistema consta de un servidor de directorio y peers que operan tanto en un modelo cliente-servidor como en P2P. Los peers pueden registrar usuarios, compartir y descargar archivos de manera eficiente y segura. Entre sus características destacan la autenticación, el intercambio de claves y la capacidad de permitir que un peer actúe como cliente y servidor simultáneamente.

## Estructura del Proyecto

El proyecto incluye dos ficheros ejecutables:

- **Directory.jar**: Servidor de directorio. Este archivo no requiere parámetros al ser ejecutado.
  
- **NanoFiles.jar**: Cliente. Para ejecutar este archivo, es necesario proporcionar el nombre de un directorio. Este directorio será utilizado por el peer para compartir archivos. Si el directorio no existe, el sistema lo creará automáticamente.

## Requisitos

- Java Runtime Environment (JRE) 8 o superior.

## Ejecución

1. **Ejecutar el Servidor de Directorio**:

   ```bash
   java -jar Directory.jar

2. **Ejecutar el Cliente**:

   ```bash
   java -jar NanoFiles.jar <nombre_del_directorio>


