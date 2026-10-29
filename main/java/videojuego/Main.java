package videojuego;

import java.util.HashMap;
import java.util.Map;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Main extends Application {

    private double celda = 56;         // tamaño de cada casilla en píxeles
    private int msPorTick = 700;       // cada cuánto avanzan asteroides y satélites una casilla
    private int msAnimacion = 600;     // duración del deslizamiento

    private Juego juego;


    private Pane capaDinamica;
    private Map<Object, Node> spritesActivos = new HashMap<>();

    private Label lblVida;
    private Label lblInventario;
    private Label lblCheckPoint;
    private Label lblMensaje;

    private Image imgNave;
    private Image imgEstacion;
    private Image imgAsteroide;
    private Image imgFondo;
    private Image imgCristal;
    private Image imgLaser;
    private Image imgSatelite;

    @Override
    public void start(Stage stage) {
        juego = new Juego();

        imgAsteroide = new Image("file:src/main/resources/asteroide.png");
        imgNave = new Image("file:src/main/resources/nave.png");
        imgEstacion = new Image("file:src/main/resources/estacion.png");
        imgCristal = new Image("file:src/main/resources/cristal.png");
        imgLaser = new Image("file:src/main/resources/laser.png");
        imgSatelite = new Image("file:src/main/resources/satelite.png");
        imgFondo = new Image("file:src/main/resources/espacio.png");

        capaDinamica = new Pane();
        capaDinamica.setPrefSize(juego.getColumnas() * celda, juego.getFilas() * celda);
        capaDinamica.setMinSize(juego.getColumnas() * celda, juego.getFilas() * celda);
        capaDinamica.setMaxSize(juego.getColumnas() * celda, juego.getFilas() * celda);
        crearSprites();

        lblVida = crearEtiqueta("");
        lblInventario = crearEtiqueta("");
        lblCheckPoint = crearEtiqueta("");
        lblMensaje = crearEtiqueta("");
        lblMensaje.setWrapText(true);
        lblMensaje.setMinHeight(36);
        lblMensaje.setMaxWidth(juego.getColumnas() * celda);

        Label lblAyuda = crearEtiqueta("Flechas/WASD: mover   |   ESPACIO: disparar láser (hacia la derecha)");

        Button btnReiniciar = new Button("Reiniciar");
        btnReiniciar.setFocusTraversable(false); // para que ESPACIO no presione el botón
        btnReiniciar.setOnAction(e -> {
            juego = new Juego();
            crearSprites();
            refrescar();
        });

        HBox filaInfo = new HBox(30, lblVida, lblCheckPoint, lblInventario);
        HBox filaAyuda = new HBox(15, btnReiniciar, lblAyuda);

        VBox raiz = new VBox(8, filaInfo, capaDinamica, lblMensaje, filaAyuda);
        raiz.setPadding(new Insets(15));
        raiz.setStyle("-fx-background-color: #0b0b2b;");

        Scene escena = new Scene(raiz);
        escena.setOnKeyPressed(event -> manejarTecla(event.getCode()));

        refrescar();
        stage.setTitle("Nave Espacial");
        stage.setScene(escena);
        stage.show();

        Timeline temporizador = new Timeline(new KeyFrame(Duration.millis(msPorTick), e -> {
            juego.avanzarTiempo();
            deslizarAsteroidesYSatelites();
            refrescar();
        }));
        temporizador.setCycleCount(Timeline.INDEFINITE);
        temporizador.play();
    }

    private Label crearEtiqueta(String texto) {
        Label etiqueta = new Label(texto);
        etiqueta.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
        return etiqueta;
    }

    private void manejarTecla(KeyCode codigo) {
        switch (codigo) {
            case UP:
            case W:
                juego.moverNave("ARRIBA");
                break;
            case DOWN:
            case S:
                juego.moverNave("ABAJO");
                break;
            case LEFT:
            case A:
                juego.moverNave("IZQUIERDA");
                break;
            case RIGHT:
            case D:
                juego.moverNave("DERECHA");
                break;
            case SPACE:
                juego.disparar();
                break;
            default:
                return;
        }
        refrescar();
    }

    private void crearSprites() {
        capaDinamica.getChildren().clear();
        spritesActivos.clear();

        ImageView fondo = new ImageView(imgFondo);
        fondo.setFitWidth(juego.getColumnas() * celda);
        fondo.setFitHeight(juego.getFilas() * celda);
        capaDinamica.getChildren().add(fondo);

        for (CheckPoint c : juego.getNivel().getCheckPoints()) {
            agregarSprite(c, crearImagen(imgEstacion), c.getPosicionX(), c.getPosicionY());
        }

        for (ItemEnMapa e : juego.getItemsEnMapa()) {
            Node sprite;
            if (e.getItem() instanceof Arma) {
                sprite = crearImagen(imgLaser);
            } else {
                sprite = crearImagen(imgCristal);
            }
            agregarSprite(e, sprite, e.getPosicionX(), e.getPosicionY());
        }

        for (Obstaculo o : juego.getNivel().getObstaculos()) {
            agregarSprite(o, crearImagen(imgAsteroide), o.getPosicionX(), o.getPosicionY());
        }

        for (ElementoDinamico e : juego.getNivel().getElementosDinamicos()) {
            if (e instanceof Utileria) {
                Utileria u = (Utileria) e;
                agregarSprite(u, crearImagen(imgSatelite), u.getPosicionX(), u.getPosicionY());
            }
        }

        Personaje nave = juego.getNave();
        agregarSprite(nave, crearImagen(imgNave), nave.getPosicionX(), nave.getPosicionY());
    }

    private void agregarSprite(Object objeto, Node sprite, int x, int y) {
        sprite.setTranslateX(x * celda);
        sprite.setTranslateY(y * celda);
        spritesActivos.put(objeto, sprite);
        capaDinamica.getChildren().add(sprite);
    }

    private ImageView crearImagen(Image imagen) {
        ImageView vista = new ImageView(imagen);
        vista.setFitWidth(celda - 8);
        vista.setFitHeight(celda - 8);
        vista.setPreserveRatio(true);
        vista.setX(4);
        vista.setY(4);
        return vista;
    }

    private void deslizarAsteroidesYSatelites() {
        for (Object objeto : spritesActivos.keySet()) {
            Node sprite = spritesActivos.get(objeto);

            if (objeto instanceof Obstaculo) {
                Obstaculo o = (Obstaculo) objeto;
                if (!o.isDestruido()) {
                    deslizar(sprite, o.getPosicionX(), o.getPosicionY());
                }
            } else if (objeto instanceof Utileria) {
                Utileria u = (Utileria) objeto;
                if (!u.isUsada()) {
                    deslizar(sprite, u.getPosicionX(), u.getPosicionY());
                }
            }
        }
    }

    private void deslizar(Node sprite, int x, int y) {
        double destinoX = x * celda;
        double destinoY = y * celda;

        if (Math.abs(sprite.getTranslateY() - destinoY) > 2 * celda) {
            sprite.setTranslateX(destinoX);
            sprite.setTranslateY(destinoY);
            return;
        }

        TranslateTransition animacion = new TranslateTransition(Duration.millis(msAnimacion), sprite);
        animacion.setToX(destinoX);
        animacion.setToY(destinoY);
        animacion.setInterpolator(Interpolator.LINEAR); // velocidad constante
        animacion.play();
    }

    private void refrescar() {
        actualizarSprites();
        actualizarPanel();
    }

    private void actualizarSprites() {
        for (Object objeto : spritesActivos.keySet()) {
            Node sprite = spritesActivos.get(objeto);

            if (objeto instanceof Obstaculo) {
                sprite.setVisible(!((Obstaculo) objeto).isDestruido());
            } else if (objeto instanceof Utileria) {
                sprite.setVisible(!((Utileria) objeto).isUsada());
            } else if (objeto instanceof ItemEnMapa) {
                sprite.setVisible(juego.getItemsEnMapa().contains(objeto));
            } else if (objeto instanceof CheckPoint) {
                sprite.setOpacity(((CheckPoint) objeto).isActivado() ? 1.0 : 0.4);
            } else if (objeto instanceof Personaje) {
                Personaje nave = (Personaje) objeto;
                sprite.setTranslateX(nave.getPosicionX() * celda);
                sprite.setTranslateY(nave.getPosicionY() * celda);
            }
        }
    }

    private void actualizarPanel() {
        lblVida.setText("Vida: " + juego.getNave().getVida() + " / " + juego.getNave().getVidaMaxima());

        String estadoCheckPoint = "sin activar";
        for (CheckPoint c : juego.getNivel().getCheckPoints()) {
            if (c.isActivado()) {
                estadoCheckPoint = "ACTIVADO";
            }
        }
        lblCheckPoint.setText("CheckPoint: " + estadoCheckPoint);

        StringBuilder texto = new StringBuilder();
        texto.append("Inventario (").append(juego.getNivel().getInventario().getItems().size())
                .append("/").append(juego.getNivel().getInventario().getCapacidadMaxima()).append("): ");
        for (Inventariable item : juego.getNivel().getInventario().getItems()) {
            texto.append(item).append("  ");
        }
        lblInventario.setText(texto.toString());
        lblMensaje.setText(juego.getMensaje());
    }

    public static void main(String[] args) {
        launch(args);
    }
}