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

    // Objeto que sabe leer y escribir archivos de texto
    private GestorArchivo gestorArchivo = new GestorArchivo();

    // Partida nueva con la configuración inicial por defecto (no lee ningún archivo)
    public Juego() {
        cargarConfiguracion(crearConfiguracionPorDefecto());
    }

    //Partida que se restaura leyendo el archivo
    //Si el archivo todavía no existe, se crea con la configuración inicial

    public Juego(String nombreArchivo) {
        if (gestorArchivo.existeArchivo(nombreArchivo)) {
            cargarConfiguracion(gestorArchivo.leerConfiguracion(nombreArchivo));
            mensaje = "Partida cargada desde " + nombreArchivo;
        } else {
            List<String> porDefecto = crearConfiguracionPorDefecto();
            gestorArchivo.escribirConfiguracion(nombreArchivo, porDefecto);
            cargarConfiguracion(porDefecto);
            mensaje = "Se creó " + nombreArchivo + " con la configuración inicial.";
        }
    }

    //Persistencia: LEER el archivo
    //Convierte las líneas de texto en objetos y los agrega al nivel
    private void cargarConfiguracion(List<String> lineas) {
        // Valores por si el archivo no trae esas líneas
        columnas = 15;
        filas = 10;
        capacidadInventario = 7;
        String nombreNivel = "Cinturon de Asteroides";
        int numeroNivel = 1;
        String dificultad = "Facil";

        // Paso 1: leer los datos generales (tablero, inventario y nivel)
        for (String linea : lineas) {
            String[] d = separarLinea(linea);
            if (d != null) {
                try {
                    if (d[0].equals("TABLERO")) {
                        columnas = Integer.parseInt(d[1]);
                        filas = Integer.parseInt(d[2]);
                    } else if (d[0].equals("INVENTARIO")) {
                        capacidadInventario = Integer.parseInt(d[1]);
                    } else if (d[0].equals("NIVEL")) {
                        nombreNivel = d[1];
                        numeroNivel = Integer.parseInt(d[2]);
                        dificultad = d[3];
                    }
                } catch (Exception e) {
                    System.out.println("Linea ignorada (formato incorrecto): " + linea);
                }
            }
        }

        // Paso 2: crear el nivel vacío y reiniciar el estado del juego
        nivel = new Nivel(nombreNivel, numeroNivel, dificultad, new Inventario(capacidadInventario));
        itemsEnMapa = new ArrayList<>();
        nave = null;
        ultimoCheckPoint = null;
        juegoTerminado = false;
        victoria = false;
        hayRayo = false;
        mensaje = "Usa las flechas para moverte. ¡Los asteroides se mueven solos!";

        // Paso 3: agregar al nivel cada elemento que aparece en el archivo
        for (String linea : lineas) {
            String[] d = separarLinea(linea);
            if (d != null) {
                try {
                    agregarElemento(d);
                } catch (Exception e) {
                    System.out.println("Linea ignorada (formato incorrecto): " + linea);
                }
            }
        }

        if (nave == null) {
            System.out.println("El archivo no tiene una linea NAVE: se usa la configuracion por defecto.");
            cargarConfiguracion(crearConfiguracionPorDefecto());
            return;
        }
        nivel.mostrarEstado();
    }

    //Separa una línea en sus campos. Regresa null si está vacía o es un comentario (#)
    private String[] separarLinea(String linea) {
        linea = linea.trim();
        if (linea.isEmpty() || linea.startsWith("#")) {
            return null;
        }
        String[] campos = linea.split(",");
        for (int i = 0; i < campos.length; i++) {
            campos[i] = campos[i].trim();
        }
        return campos;
    }

    //Crea el objeto que describe una línea (d[0] es el tipo de elemento)
    private void agregarElemento(String[] d) {
        String tipo = d[0];

        if (tipo.equals("NAVE")) {
            // NAVE,nombre,vida,vidaMaxima,x,y
            nave = new Personaje(d[1], Integer.parseInt(d[3]), Integer.parseInt(d[4]), Integer.parseInt(d[5]));
            nave.setTablero(columnas, filas);
            nave.setVida(Integer.parseInt(d[2]));

        } else if (tipo.equals("CHECKPOINT")) {
            // CHECKPOINT,nombre,x,y,activado
            CheckPoint checkPoint = new CheckPoint(d[1], Integer.parseInt(d[2]), Integer.parseInt(d[3]));
            if (d[4].equals("true")) {
                checkPoint.activar();
                ultimoCheckPoint = checkPoint;
            }
            nivel.agregarCheckPoint(checkPoint);

        } else if (tipo.equals("ASTEROIDE")) {
            // ASTEROIDE,nombre,daño,x,y,direccion
            Obstaculo asteroide = new Obstaculo(d[1], Integer.parseInt(d[2]), Integer.parseInt(d[3]), Integer.parseInt(d[4]));
            asteroide.setFilas(filas);
            asteroide.setDireccionActual(d[5]);
            nivel.agregarObstaculo(asteroide);
            nivel.agregarElementoDinamico(asteroide);

        } else if (tipo.equals("SATELITE")) {
            // SATELITE,nombre,descripcion,x,y
            Utileria satelite = new Utileria(d[1], d[2], Integer.parseInt(d[3]), Integer.parseInt(d[4]));
            satelite.setTablero(columnas, filas);
            nivel.agregarElementoDinamico(satelite);

        } else if (tipo.equals("ARMA_MAPA")) {
            // ARMA_MAPA,nombre,daño,alcance,x,y
            Arma arma = new Arma(d[1], Integer.parseInt(d[2]), Double.parseDouble(d[3]));
            itemsEnMapa.add(new ItemEnMapa(arma, Integer.parseInt(d[4]), Integer.parseInt(d[5])));

        } else if (tipo.equals("RECOMPENSA_MAPA")) {
            // RECOMPENSA_MAPA,nombre,valor,tipo,x,y
            Recompensa recompensa = new Recompensa(d[1], Integer.parseInt(d[2]), d[3]);
            itemsEnMapa.add(new ItemEnMapa(recompensa, Integer.parseInt(d[4]), Integer.parseInt(d[5])));

        } else if (tipo.equals("ARMA_INVENTARIO")) {
            // ARMA_INVENTARIO,nombre,daño,alcance
            nivel.getInventario().agregarItem(new Arma(d[1], Integer.parseInt(d[2]), Double.parseDouble(d[3])));

        } else if (tipo.equals("RECOMPENSA_INVENTARIO")) {
            // RECOMPENSA_INVENTARIO,nombre,valor,tipo
            nivel.getInventario().agregarItem(new Recompensa(d[1], Integer.parseInt(d[2]), d[3]));
        }
    }

    //Persistencia ESCRIBIR el archivo

    //Guarda el estado actual del juego en el archivo (sobrescribe la configuración anterior)
    public void guardarPartida(String nombreArchivo) {
        if (juegoTerminado) {
            mensaje = "No se puede guardar: la partida ya terminó.";
            return;
        }

        List<String> lineas = crearEncabezado();
        lineas.add("TABLERO," + columnas + "," + filas);
        lineas.add("INVENTARIO," + nivel.getInventario().getCapacidadMaxima());
        lineas.add("NIVEL," + nivel.getNombre() + "," + nivel.getNumero() + "," + nivel.getDificultad());
        lineas.add("NAVE," + nave.getNombre() + "," + nave.getVida() + "," + nave.getVidaMaxima()
                + "," + nave.getPosicionX() + "," + nave.getPosicionY());

        for (CheckPoint c : nivel.getCheckPoints()) {
            lineas.add("CHECKPOINT," + c.getNombre() + "," + c.getPosicionX() + "," + c.getPosicionY()
                    + "," + c.isActivado());
        }

        // Los asteroides destruidos y los satélites ya usados no se guardan: ya no existen
        for (Obstaculo o : nivel.getObstaculos()) {
            if (!o.isDestruido()) {
                lineas.add("ASTEROIDE," + o.getNombre() + "," + o.getDaño() + "," + o.getPosicionX()
                        + "," + o.getPosicionY() + "," + o.getDireccionActual());
            }
        }
        for (ElementoDinamico e : nivel.getElementosDinamicos()) {
            if (e instanceof Utileria) {
                Utileria u = (Utileria) e;
                if (!u.isUsada()) {
                    lineas.add("SATELITE," + u.getNombre() + "," + u.getDescripcion() + ","
                            + u.getPosicionX() + "," + u.getPosicionY());
                }
            }
        }

        // Objetos que todavía están en el mapa
        for (ItemEnMapa i : itemsEnMapa) {
            if (i.getItem() instanceof Arma) {
                Arma a = (Arma) i.getItem();
                lineas.add("ARMA_MAPA," + a.getNombre() + "," + a.getDaño() + "," + a.getAlcance()
                        + "," + i.getPosicionX() + "," + i.getPosicionY());
            } else if (i.getItem() instanceof Recompensa) {
                Recompensa r = (Recompensa) i.getItem();
                lineas.add("RECOMPENSA_MAPA," + r.getNombre() + "," + r.getValor() + "," + r.getTipo()
                        + "," + i.getPosicionX() + "," + i.getPosicionY());
            }
        }

        // Objetos que ya recogiste
        for (Inventariable item : nivel.getInventario().getItems()) {
            if (item instanceof Arma) {
                Arma a = (Arma) item;
                lineas.add("ARMA_INVENTARIO," + a.getNombre() + "," + a.getDaño() + "," + a.getAlcance());
            } else if (item instanceof Recompensa) {
                Recompensa r = (Recompensa) item;
                lineas.add("RECOMPENSA_INVENTARIO," + r.getNombre() + "," + r.getValor() + "," + r.getTipo());
            }
        }

        boolean exito = gestorArchivo.escribirConfiguracion(nombreArchivo, lineas);
        if (exito) {
            mensaje = "Partida guardada en " + nombreArchivo;
        } else {
            mensaje = "Error: no se pudo guardar la partida.";
        }
    }

    //Comentarios que explican el formato; se escriben al inicio del archivo
    private List<String> crearEncabezado() {
        List<String> lineas = new ArrayList<>();
        lineas.add("# Configuracion del juego (las lineas que empiezan con # se ignoran)");
        lineas.add("# TABLERO,columnas,filas");
        lineas.add("# INVENTARIO,capacidad");
        lineas.add("# NIVEL,nombre,numero,dificultad");
        lineas.add("# NAVE,nombre,vida,vidaMaxima,x,y");
        lineas.add("# CHECKPOINT,nombre,x,y,activado");
        lineas.add("# ASTEROIDE,nombre,dano,x,y,direccion");
        lineas.add("# SATELITE,nombre,descripcion,x,y");
        lineas.add("# ARMA_MAPA,nombre,dano,alcance,x,y");
        lineas.add("# RECOMPENSA_MAPA,nombre,valor,tipo,x,y");
        lineas.add("# ARMA_INVENTARIO,nombre,dano,alcance");
        lineas.add("# RECOMPENSA_INVENTARIO,nombre,valor,tipo");
        return lineas;
    }

    //La configuración con la que empieza una partida nueva (se usa si no hay archivo)
    private List<String> crearConfiguracionPorDefecto() {
        List<String> lineas = crearEncabezado();
        lineas.add("TABLERO," + columnas + "," + filas);
        lineas.add("INVENTARIO," + capacidadInventario);
        lineas.add("NIVEL,Cinturon de Asteroides,1,Facil");
        lineas.add("NAVE,Halcon," + vidaInicial + "," + vidaInicial + ",0,4");
        lineas.add("CHECKPOINT,Estacion,6,5,false");

        // Asteroides
        int[][] asteroides = {
                {3, 2}, {3, 7}, {5, 0}, {5, 5}, {7, 3}, {7, 8},
                {9, 1}, {9, 6}, {11, 4}, {11, 9}, {13, 2}, {13, 7}
        };
        for (int i = 0; i < asteroides.length; i++) {
            String direccion = (i % 2 == 0) ? "ABAJO" : "ARRIBA";
            lineas.add("ASTEROIDE,Asteroide,20," + asteroides[i][0] + "," + asteroides[i][1] + "," + direccion);
        }

        // Satélites
        lineas.add("SATELITE,Satelite 1,Repara 25 de vida,2,1");
        lineas.add("SATELITE,Satelite 2,Repara 25 de vida,12,6");

        // Objetos para recoger
        lineas.add("ARMA_MAPA,Laser,50,5.0,2,5");
        int[][] cristales = {
                {4, 2}, {6, 6}, {7, 5}, {8, 9}, {9, 4}, {10, 2}, {11, 7}, {12, 5}
        };
        for (int[] pos : cristales) {
            lineas.add("RECOMPENSA_MAPA,Cristal,10,Cristal," + pos[0] + "," + pos[1]);
        }
        return lineas;
    }

    //Acciones del jugador

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