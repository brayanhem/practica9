package videojuego;

public class Personaje implements Destruible,ElementoDinamico {

    private String nombre;
    private int vida;
    private int posicionX;
    private int posicionY;
    private int vidaMaxima;
    private int columnas;
    private int filas;

    public Personaje(String nombre, int vida, int posicionX, int posicionY){
        this.nombre = nombre;
        this.vida = vida;
        this.posicionX = posicionX;
        this.posicionY = posicionY;
        this.vidaMaxima = vida;
    }

    public String getNombre(){
        return nombre;
    }
    public int getVida(){
        return vida;
    }
    public int getVidaMaxima(){
        return vidaMaxima;
    }
    public int getPosicionX(){
        return posicionX;
    }
    public int getPosicionY(){
        return posicionY;
    }

    public void setTablero(int columnas, int filas){
        this.columnas = columnas;
        this.filas = filas;
    }

    public void setPosicion(int x, int y){
        this.posicionX = x;
        this.posicionY = y;
    }

    public void recibirDaño(int daño){
        vida = vida - daño;
        if(vida <= 0){
            destruye();
        }
    }

    public void curar(int cantidad){
        vida = vida + cantidad;
        if(vida > vidaMaxima){
            vida = vidaMaxima;
        }
    }

    @Override
    public void destruye() {
        vida = 0;
        System.out.println(nombre + " ha muerto");
    }

    @Override
    public void mover(String direccion, int distancia) {
        switch (direccion){
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

        // La nave no puede salirse del tablero
        if (posicionX < 0) posicionX = 0;
        if (posicionX > columnas - 1) posicionX = columnas - 1;
        if (posicionY < 0) posicionY = 0;
        if (posicionY > filas - 1) posicionY = filas - 1;
    }
}