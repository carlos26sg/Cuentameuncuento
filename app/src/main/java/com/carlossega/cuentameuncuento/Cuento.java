package com.carlossega.cuentameuncuento;

public class Cuento {

    private String titulo, descripcion, imagen, id;

    public Cuento(String titulo, String descripcion, String imagen, String id) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.imagen = imagen;
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
