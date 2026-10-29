package videojuego;

import java.util.ArrayList;

public class Inventario {

    private int capacidadMaxima;
    private ArrayList<Inventariable> items;

    public Inventario(int capacidadMaxima) {
        this.capacidadMaxima = capacidadMaxima;
        this.items = new ArrayList<>();
    }

    public int getCapacidadMaxima() {
        return capacidadMaxima;
    }

    public ArrayList<Inventariable> getItems() {
        return items;
    }

    public boolean agregarItem(Inventariable item){
        if(items.size()<capacidadMaxima){
            items.add(item);
            item.registrar();
            return true;
        }
        return false;
    }

    public boolean eliminarItem(Inventariable item){
        boolean eliminado = items.remove(item);
        if(eliminado){
            item.borrar();
        }
        return eliminado;
    }

    public void listarItems(){
        System.out.println("Items en inventario: "+items.size());
        for(Inventariable item: items){
            System.out.println(" - " + item);
        }
    }

}
