package videojuego;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//Se encarga SOLO de leer y escribir líneas de texto en un archivo
//No sabe nada del juego: eso lo decide la clase Juego
public class GestorArchivo {

    //Revisa si el archivo ya existe
    public boolean existeArchivo(String nombreArchivo) {
        File archivo = new File(nombreArchivo);
        return archivo.exists();
    }

    //Escribe cada elemento de la lista como una línea del archivo
    //Si el archivo ya existe, lo sobrescribe. Devuelve true si todo salió bien
    public boolean escribirConfiguracion(String nombreArchivo, List<String> configuracion) {
        boolean exito = false;
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(nombreArchivo));
            for (String linea : configuracion) {
                writer.write(linea);
                writer.newLine();
            }
            exito = true;
            System.out.println("Configuracion escrita en el archivo: " + nombreArchivo);
        } catch (IOException e) {
            System.err.println("Error al escribir en el archivo: " + e.getMessage());
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    System.err.println("Error al cerrar el archivo: " + e.getMessage());
                }
            }
        }
        return exito;
    }

    //Lee el archivo línea por línea y regresa todas las líneas en una lista
    public List<String> leerConfiguracion(String nombreArchivo) {
        List<String> configuracion = new ArrayList<>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(nombreArchivo));
            String linea;
            while ((linea = reader.readLine()) != null) {
                configuracion.add(linea);
            }
            System.out.println("Configuracion leida del archivo: " + nombreArchivo);
        } catch (IOException e) {
            System.err.println("Error al leer el archivo: " + e.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    System.err.println("Error al cerrar el archivo: " + e.getMessage());
                }
            }
        }
        return configuracion;
    }
}