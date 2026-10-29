package videojuego;

public class CheckPoint {
    private String nombre;
    private int posicionX;
    private int posicionY;
    private boolean activo;

    public CheckPoint(String nombre, int posicionX, int posicionY) {
        this.nombre = nombre;
        this.posicionX = posicionX;
        this.posicionY = posicionY;
        this.activo = false;
    }

    public String getNombre() {
        return nombre;
    }

    public int getPosicionX() {
        return posicionX;
    }
    public int getPosicionY() {
        return posicionY;
    }
    public boolean isActivado() {
        return activo;
    }
    public void activar(){
        this.activo = true;
    }

}
