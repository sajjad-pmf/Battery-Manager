package ir.truelearn.batterymanager.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.RingtoneManager
import android.net.Uri
import android.os.*
import androidx.core.app.NotificationCompat
import ir.truelearn.batterymanager.R


class BatteryAlarmService : Service() {

    var manager: NotificationManager? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        startNotification()

        registerReceiver(batteryInfoReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))


        return START_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel =
                NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_MIN)
            manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }


    private var batteryInfoReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        override fun onReceive(context: Context, intent: Intent) {
            val batteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)

            var plugState = ""
            if (intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) == 0) {
                plugState = context.getString(R.string.using_battery)
            } else {
                plugState = context.getString(R.string.charging_battery)
            }

            if(batteryLevel>98){
                startAlarm()
                plugState = context.getString(R.string.battery_charged)
            }

            updateNotification(batteryLevel ,plugState)
        }
    }


    private fun startNotification() {

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(applicationContext.getString(R.string.loading))
            .setContentText(applicationContext.getString(R.string.wait_for_battery))
            .setSmallIcon(R.drawable.health_good)
            .build()

        startForeground(NOTIFICATION_ID, notification)


    }


    private fun startAlarm(){
        val alarm :Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val ring = RingtoneManager.getRingtone(applicationContext , alarm)
        ring.play()

        val v = getSystemService(VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(1500 , VibrationEffect.DEFAULT_AMPLITUDE))
        }else{
            v.vibrate(1500)
        }

    }

    private fun updateNotification(batteryLevel: Int, plugState: String) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(plugState)
            .setContentText("battery charge : $batteryLevel")
            .setSmallIcon(R.drawable.health_good)
            .build()

        manager?.notify(NOTIFICATION_ID, notification)

    }


    companion object {
        const val CHANNEL_ID = "BatteryManagerChannel"
        const val CHANNEL_NAME = "BatteryManagerService"
        const val NOTIFICATION_ID = 1
    }
}