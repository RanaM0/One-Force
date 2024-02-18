
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.itdocx.oneforce.DashboardActivity
import com.itdocx.oneforce.R

class ProfitUpdateService : Service() {

    override fun onCreate() {
        super.onCreate()
        // Start the service in foreground
        startForeground(NOTIFICATION_ID, createNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Your service logic here
        // This method will be called whenever the service is started or restarted

        // Return START_STICKY to restart the service if it's killed by the system
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        // Stop foreground and remove notification
        stopForeground(false)
    }

    private fun createNotification(): Notification {
        // Create a notification with a pending intent to launch the app when tapped
        val intent = Intent(this, DashboardActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Profit Update Service")
            .setContentText("Updating profits...")
            .setSmallIcon(R.drawable.notification) // Replace with your notification icon
            .setContentIntent(pendingIntent)
            .build()
    }

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "ProfitUpdateServiceChannel"
    }



}