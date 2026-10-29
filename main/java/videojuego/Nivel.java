package videojuego;

import java.util.ArrayList;

public class Nivel {
    private String nombre;
    private int numero;
    private String dificultad;

    private ArrayList<Obstaculo> obstaculos;
    private ArrayList<ElementoDinamico> elementosDinamicos;
    private ArrayList<CheckPoint>checkPoints;
    private Inventario inventario;

    public Nivel(String nombre, int numero, String dificultad, Inventario inventario) {
        this.nombre = nombre;
        this.numero = numero;
        this.dificultad = dificultad;
        this.inventario = inventario;
        this.obstaculos = new ArrayList<>();
        this.checkPoints = new ArrayList<>();
        this.elementosDinamicos = new ArrayList<>();
    }

    public String getNombre() {
        return nombre;
    }

    public int getNumero() {
        return numero;
    }

    public String getDificultad() {
        return dificultad;
    }

    ArrayList<Obstaculo> getObstaculos() {
        return obstaculos;
    }

    ArrayList<ElementoDinamico> getElementosDinamicos() {
        return elementosDinamicos;
    }

    ArrayList<CheckPoint> getCheckPoints() {
        return checkPoints;
    }

    public Inventario getInventario() {
        return inventario;
    }

    public void agregarObstaculo(Obstaculo obstaculo) {
        obstaculos.add(obstaculo);
    }

    public void agregarCheckPoint(CheckPoint checkPoint) {
        checkPoints.add(checkPoint);
    }

    public void agregarElementoDinamico(ElementoDinamico elemento) {
        elementosDinamicos.add(elemento);
    }

    //Movimiento de Elementos
    public void moverElementosDinamicos(String direccion, int distancia) {
        for (ElementoDinamico elemento : elementosDinamicos) {
            elemento.mover(direccion, distancia);
        }
    }

    public void mostrarEstado() {
        int obstaculosActivos = 0;
        for (Obstaculo o : obstaculos) {
            if (!o.isDestruido()) {
                obstaculosActivos++;
            }
        }
        int checkPointsActivados = 0;
        for (CheckPoint c : checkPoints) {
            if (c.isActivado()) {
                checkPointsActivados++;
            }
        }
        System.out.println("Nivel " + numero + ": " + nombre + " (" + dificultad + ") ");
        System.out.println("Obstaculos activos: " + obstaculosActivos + " de " + obstaculos.size());
        System.out.println("CheckPoints activados: " + checkPointsActivados + " de " + checkPoints.size());
        System.out.println("Elementos dinamicos: " + elementosDinamicos.size());
        inventario.listarItems();
    }
}
