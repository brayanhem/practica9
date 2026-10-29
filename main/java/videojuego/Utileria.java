package videojuego;

public class Utileria implements ElementoDinamico{

    private String nombre;
    private String descripcion;
    private int posicionX;
    private int posicionY;
    private boolean usada;
    private int columnas;
    private int filas;

    public Utileria(String nombre, String descripcion, int posicionX, int posicionY) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.posicionX = posicionX;
        this.posicionY = posicionY;
        this.usada = false;
    }

    public String getNombre() {
        return nombre;
    }
    public String getDescripcion() {
        return descripcion;
    }
    public int getPosicionX() {
        return posicionX;
    }
    public int getPosicionY() {
        return posicionY;
    }

    public void setTablero(int columnas, int filas) {
        this.columnas = columnas;
        this.filas = filas;
    }

    public boolean isUsada(){
        return usada;
    }

    public void usar(){
        usada = true;
        System.out.println("Usando " + nombre);
    }

    @Override
    public void mover(String direccion, int distancia) {
        switch (direccion) {
            case "ARRIBA":
                posicionY = posicionY - distancia;
                break;
            case "ABAJO":
                posicionY = posicionY + distancia;
                break;
            case "IZQUIERDA":
                posicionX = posicionX - distancia;
                break;
            case "DERECHA":
                posicionX = posicionX + distancia;
                break;
        }

        // Si se sale por un borde, aparece por el lado contrario
        if (posicionX >= columnas) posicionX = 0;
        if (posicionX < 0) posicionX = columnas - 1;
        if (posicionY >= filas) posicionY = 0;
        if (posicionY < 0) posicionY = filas - 1;
    }
}