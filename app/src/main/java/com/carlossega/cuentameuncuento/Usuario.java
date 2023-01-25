package com.carlossega.cuentameuncuento;

/**
 * Clase Usuario con sus getters y setters
 */
public class Usuario {

    private String mail;
    private String nombre;
    private String idioma;
    private String favorito;
    private String modo_fav;

    public Usuario() { }

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

    public String getFavorito() { return favorito; }

    public void setFavorito(String favorito) { this.favorito = favorito; }

    public String getModo_fav() { return modo_fav; }

    public void setModo_fav(String modo_fav) { this.modo_fav = modo_fav; }
}
