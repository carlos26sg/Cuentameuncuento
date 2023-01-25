package com.carlossega.cuentameuncuento;

public class SpinnerId {
    public String id;
    public String nombre;

    public SpinnerId(String id, String nombre) {
        this.nombre = nombre;
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }
}
