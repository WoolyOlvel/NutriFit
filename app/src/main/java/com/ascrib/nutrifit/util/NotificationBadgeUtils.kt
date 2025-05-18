
import android.view.MenuItem
import android.view.View
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.ascrib.nutrifit.R
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils
import com.google.android.material.badge.ExperimentalBadgeUtils

object NotificationBadgeUtils {
    private var badge: BadgeDrawable? = null
    private var badgeCount: Int = 0 // Variable para mantener el conteo persistente

    @OptIn(ExperimentalBadgeUtils::class)
    fun setupBadge(activity: AppCompatActivity, menuItem: MenuItem) {
        try {
            // 1. Crear el badge si no existe
            badge = badge ?: BadgeDrawable.create(activity).apply {
                backgroundColor = ContextCompat.getColor(activity, R.color.red)
                badgeTextColor = ContextCompat.getColor(activity, R.color.white)
                maxCharacterCount = 2 // Para mostrar "9+" si hay más de 9
                number = badgeCount // Usar el valor persistente
                isVisible = badgeCount >= 0 // Siempre visible, incluso con cero
                // GRAVEDAD por defecto: TOP_END
                badgeGravity = BadgeDrawable.TOP_END
                // OFFSETS en px (negativo para acercar al icono)
                val hOffset = activity.resources.getDimensionPixelOffset(R.dimen.badge_horizontal_offset)
                val vOffset = activity.resources.getDimensionPixelOffset(R.dimen.badge_vertical_offset)
                horizontalOffset = -hOffset
                verticalOffset   = -vOffset
            }

            // 2. Configurar el actionView
            val iconView = activity.findViewById<View>(R.id.action_notification)
                ?: menuItem.actionView
                ?: createDefaultActionView(activity, menuItem)

            // 3. Adjuntar el badge
            badge?.let {
                BadgeUtils.attachBadgeDrawable(it, iconView)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createDefaultActionView(activity: AppCompatActivity, menuItem: MenuItem): View {
        return androidx.appcompat.widget.AppCompatImageView(activity).apply {
            menuItem.actionView = this
        }
    }

    fun updateBadgeCount(count: Int) {
        badgeCount = count // Actualizar el valor persistente
        badge?.let {
            it.number = count
            it.isVisible = true // Mantener visible incluso con 0
        }
    }
}