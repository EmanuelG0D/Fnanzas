package com.appfinanzas.ui

import android.content.Intent
import android.service.quicksettings.TileService
import com.appfinanzas.MainActivity

class QuickAddTileService : TileService() {
    override fun onClick() {
        super.onClick()
        
        // Cierra el panel de notificaciones / status bar para mostrar la app
        val it = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
        sendBroadcast(it)

        // Abre la app en la pantalla de "Añadir Gasto" (puedes pasar un extra en el intent indicándolo)
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("OPEN_ADD_EXPENSE", true)
        }
        startActivityAndCollapse(intent)
    }
}
