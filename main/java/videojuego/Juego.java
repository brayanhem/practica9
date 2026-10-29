package videojuego;

import java.util.ArrayList;
import java.util.List;

public class Juego {
    private int columnas = 15;
    private int filas = 10;
    private int capacidadInventario = 7;
    private int vidaInicial = 100;

    private Personaje nave;
    private Nivel nivel;
    private ArrayList<ItemEnMapa> itemsEnMapa;
    private CheckPoint ultimoCheckPoint;
    private String mensaje;
    private boolean juegoTerminado;
    private boolean victoria;

    // Datos del rayo láser (para que la pantalla lo pueda dibujar)
    private boolean hayRayo;
    private int rayoX;
    private int rayoY;

    public Juego() {
        iniciar();
    }

    private void iniciar() {
        Inventario inventario = new Inventario(capacidadInventario);
        nivel = new Nivel("Cinturón de Asteroides", 1, "Fácil", inventario);
        nave = new Personaje("Halcón", vidaInicial, 0, 4);
        nave.setTablero(columnas, filas);
        itemsEnMapa = new ArrayList<>();
        ultimoCheckPoint = null;
        juegoTerminado = false;
        victoria = false;
        hayRayo = false;
        mensaje = "Usa las flechas para moverte. ¡Los asteroides se mueven solos!";

        // Asteroides
        int[][] asteroides = {
                {3, 2}, {3, 7}, {5, 0}, {5, 5}, {7, 3}, {7, 8},
                {9, 1}, {9, 6}, {11, 4}, {11, 9}, {13, 2}, {13, 7}
        };
        for (int[] pos : asteroides) {
            Obstaculo asteroide = new Obstaculo("Asteroide", 20, pos[0], pos[1]);
            asteroide.setFilas(filas);
            nivel.agregarObstaculo(asteroide);
            nivel.agregarElementoDinamico(asteroide);
        }

        nivel.agregarCheckPoint(new CheckPoint("Estacion", 6, 5));

        Utileria satelite1 = new Utileria("Satelite 1", "Repara 25 de vida", 2, 1);
        Utileria satelite2 = new Utileria("Satelite 2", "Repara 25 de vida", 12, 6);
        satelite1.setTablero(columnas, filas);
        satelite2.setTablero(columnas, filas);
        nivel.agregarElementoDinamico(satelite1);
        nivel.agregarElementoDinamico(satelite2);

        itemsEnMapa.add(new ItemEnMapa(new Arma("Laser", 50, 5), 2, 5));
        int[][] cristales = {
                {4, 2}, {6, 6}, {7, 5}, {8, 9}, {9, 4}, {10, 2}, {11, 7}, {12, 5}
        };
        for (int[] pos : cristales) {
            itemsEnMapa.add(new ItemEnMapa(new Recompensa("Cristal", 10, "Cristal"), pos[0], pos[1]));
        }

        nivel.mostrarEstado();
    }

    public void moverNave(String direccion) {
        if (juegoTerminado) {
            return;
        }
        mensaje = "";
        nave.mover(direccion, 1);
        revisarObstaculos();
        if (nave.getVida() > 0) {
            revisarItems();
            revisarCheckPoints();
            revisarSatelites();
        }
        revisarEstadoNave();
    }

    public void disparar() {
        if (juegoTerminado) {
            return;
        }
        mensaje = "";
        hayRayo = false;
        Arma arma = buscarArma();
        if (arma == null) {
            mensaje = "No tienes arma. Busca el Laser";
            return;
        }

        int alcance = (int) arma.getAlcance();
        boolean impacto = false;
        rayoX = nave.getPosicionX();
        rayoY = nave.getPosicionY();

        for (int distancia = 1; distancia <= alcance && !impacto; distancia++) {
            int x = nave.getPosicionX() + distancia;
            if (x >= columnas) {
                break;
            }
            rayoX = x;
            for (Obstaculo o : nivel.getObstaculos()) {
                if (!o.isDestruido() && o.getPosicionX() == x
                        && o.getPosicionY() == nave.getPosicionY()) {
                    o.destruye();
                    impacto = true;
                    mensaje = "¡" + arma.getNombre() + " destruyo un " + o.getNombre() + "!";
                    break;
                }
            }
        }
        hayRayo = true;
        if (!impacto) {
            mensaje = "El disparo no golpeo nada.";
        }
    }

    public void avanzarTiempo() {
        if (juegoTerminado) {
            return;
        }
        nivel.moverElementosDinamicos("ABAJO", 1); // cada elemento se mueve a su manera
        revisarSatelites();
        revisarObstaculos();
        revisarEstadoNave();
    }


    private void revisarObstaculos() {
        for (Obstaculo o : nivel.getObstaculos()) {
            if (!o.isDestruido() && o.getPosicionX() == nave.getPosicionX()
                    && o.getPosicionY() == nave.getPosicionY()) {
                nave.recibirDaño(o.getDaño());
                o.destruye();
                mensaje = "¡Chocaste con un " + o.getNombre() + "! -" + o.getDaño() + " de vida.";
            }
        }
    }

    private void revisarItems() {
        for (ItemEnMapa e : itemsEnMapa) {
            if (e.getPosicionX() == nave.getPosicionX() && e.getPosicionY() == nave.getPosicionY()) {
                boolean agregado = nivel.getInventario().agregarItem(e.getItem());
                if (agregado) {
                    itemsEnMapa.remove(e);
                    mensaje = "Recogiste: " + e.getItem();
                } else {
                    mensaje = "Inventario lleno, no puedes recoger: " + e.getItem();
                }
                break;
            }
        }
    }

    private void revisarCheckPoints() {
        for (CheckPoint c : nivel.getCheckPoints()) {
            if (!c.isActivado() && c.getPosicionX() == nave.getPosicionX()
                    && c.getPosicionY() == nave.getPosicionY()) {
                c.activar();
                ultimoCheckPoint = c;
                mensaje = "CheckPoint activado: " + c.getNombre();
            }
        }
    }

    private void revisarSatelites() {
        for (ElementoDinamico e : nivel.getElementosDinamicos()) {
            if (e instanceof Utileria) {
                Utileria u = (Utileria) e;
                if (!u.isUsada() && u.getPosicionX() == nave.getPosicionX()
                        && u.getPosicionY() == nave.getPosicionY()) {
                    u.usar();
                    nave.curar(25);
                    mensaje = u.getNombre() + ": " + u.getDescripcion();
                }
            }
        }
    }

    private void revisarEstadoNave() {
        if (nave.getVida() <= 0) {
            if (ultimoCheckPoint != null) {
                nave.setPosicion(ultimoCheckPoint.getPosicionX(), ultimoCheckPoint.getPosicionY());
                nave.curar(50);
                mensaje = mensaje + " Nave destruida: reapareces en " + ultimoCheckPoint.getNombre() + ".";
            } else {
                juegoTerminado = true;
                victoria = false;
                mensaje = "GAME OVER: tu nave fue destruida.";
                nivel.mostrarEstado();
            }
            return;
        }

        if (nave.getPosicionX() == columnas - 1) {
            juegoTerminado = true;
            victoria = true;
            mensaje = "¡LLEGASTE A LA META! Puntos: " + calcularPuntos();
            nivel.mostrarEstado();
        }
    }

    private Arma buscarArma() {
        for (Inventariable item : nivel.getInventario().getItems()) {
            if (item instanceof Arma) {
                return (Arma) item;
            }
        }
        return null;
    }

    private int calcularPuntos() {
        int puntos = 0;
        for (Inventariable item : nivel.getInventario().getItems()) {
            if (item instanceof Recompensa) {
                puntos = puntos + ((Recompensa) item).getValor();
            }
        }
        return puntos;
    }

    public int getColumnas() {
        return columnas;
    }

    public int getFilas() {
        return filas;
    }

    public int getPuntos() {
        return calcularPuntos();
    }

    public Personaje getNave() {
        return nave;
    }

    public Nivel getNivel() {
        return nivel;
    }

    public List<ItemEnMapa> getItemsEnMapa() {
        return itemsEnMapa;
    }

    public String getMensaje() {
        return mensaje;
    }

    public boolean isJuegoTerminado() {
        return juegoTerminado;
    }

    public boolean isVictoria() {
        return victoria;
    }

    public boolean isHayRayo() {
        return hayRayo;
    }

    public int getRayoX() {
        return rayoX;
    }

    public int getRayoY() {
        return rayoY;
    }
}