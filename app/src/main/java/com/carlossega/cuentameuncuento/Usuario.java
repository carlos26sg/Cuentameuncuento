package com.carlossega.cuentameuncuento;

public class Usuario {

    private String mail;
    private String nombre;
    private String idioma;
    private String favorito;

    public Usuario(String mail) {
        this.mail = mail;
    }

    public Usuario(String mail, String nombre, String idioma, String favorito) {
        this.mail = mail;
        this.nombre = nombre;
        this.idioma = idioma;
        this.favorito = favorito;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getIdioma() {
        return idioma;
    }

    public void setIdioma(String idioma) {
        this.idioma = idioma;
    }

    public String getFavorito() {
        return favorito;
    }

    public void setFavorito(String favorito) {
        this.favorito = favorito;
    }
}
