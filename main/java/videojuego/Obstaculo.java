package videojuego;

public class Obstaculo implements Destruible,ElementoDinamico{

    private String nombre;
    private int daño;
    private int posicionX;
    private int posicionY;
    private boolean destruido;

    private String direccionActual;
    private int filas;

    public Obstaculo(String nombre, int daño, int posicionX, int posicionY) {
        this.nombre = nombre;
        this.daño=daño;
        this.posicionX=posicionX;
        this.posicionY=posicionY;
        this.destruido=false;
        this.direccionActual=(Math.random()>0.5 ? "ARRIBA":"ABAJO");
    }

    public String getNombre() {
        return nombre;
    }

    public int getDaño(){
        return daño;
    }

    public int getPosicionX(){
        return posicionX;
    }

    public int getPosicionY(){
        return posicionY;
    }

    public void setFilas(int filas) {
        this.filas = filas;
    }

    public String getDireccionActual() {
        return direccionActual;
    }

    @Override
    public void mover(String direccionIgnorada, int distancia) {
        if (destruido) return;

        if (direccionActual.equals("ABAJO")) {
            if (posicionY + distancia > filas - 1) {
                direccionActual = "ARRIBA";
            }
        } else {
            if (posicionY - distancia < 0) {
                direccionActual = "ABAJO";
            }
        }

        if (direccionActual.equals("ABAJO")) {
            posicionY = posicionY + distancia;
        } else {
            posicionY = posicionY - distancia;
        }
    }

    public boolean isDestruido(){
        return destruido;
    }

    @Override
    public void destruye() {
        destruido=true;
        System.out.println("Obstaculo destruido");
    }

}