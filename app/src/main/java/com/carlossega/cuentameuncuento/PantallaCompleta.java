package com.carlossega.cuentameuncuento;

import android.view.View;

public class PantallaCompleta {
    public void pantallaCompleta(View decorView){
        // Ocultamos la barra de navegación y la barra de status
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }
}
