package videojuego;

public class ItemEnMapa {

    private Inventariable item;
    private int posicionX;
    private int posicionY;

    public ItemEnMapa(Inventariable item, int posicionX, int posicionY) {
        this.item = item;
        this.posicionX = posicionX;
        this.posicionY = posicionY;
    }

    public Inventariable getItem() {
        return item;
    }

    public int getPosicionX() {
        return posicionX;
    }

    public int getPosicionY() {
        return posicionY;
    }
}
