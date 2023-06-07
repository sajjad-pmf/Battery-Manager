package ir.truelearn.batterymanager.activity

import android.content.*
import android.graphics.Color
import android.os.BatteryManager
import android.os.Bundle
import android.view.Gravity
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import ir.truelearn.batterymanager.R
import ir.truelearn.batterymanager.databinding.ActivityMainBinding
import ir.truelearn.batterymanager.helper.SpManager
import ir.truelearn.batterymanager.service.BatteryAlarmService


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


        initDrawer();
        serviceConfig()

        registerReceiver(batteryInfoReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))


    }


    private fun initDrawer() {
        binding.imgMenu.setOnClickListener {
            binding.drawer.openDrawer(Gravity.RIGHT)
        }
        binding.incDrawer.txtAppUsage.setOnClickListener {
            startActivity(Intent(this@MainActivity, UsageBatteryActivity::class.java))
            binding.drawer.closeDrawer(Gravity.RIGHT)
        }



    }


    private fun serviceConfig(){
        if (SpManager.isServiceOn(this@MainActivity) == true) {
            binding.incDrawer.serviceSwitchTxt.text = getString(R.string.service_on)
            binding.incDrawer.serviceSwitch.isChecked = true
            startService()
        } else {
            binding.incDrawer.serviceSwitchTxt.text = getString(R.string.service_off)
            binding.incDrawer.serviceSwitch.isChecked = false
            stopService()
        }


        binding.incDrawer.serviceSwitch.setOnCheckedChangeListener { switch, isCheck ->

            SpManager.setServiceState(this@MainActivity, isCheck)
            if (isCheck) {
                startService()
                binding.incDrawer.serviceSwitchTxt.text = getString(R.string.service_on)
                Toast.makeText(applicationContext, getString(R.string.service_turned_on), Toast.LENGTH_SHORT).show()
            } else {
                stopService()
                binding.incDrawer.serviceSwitchTxt.text = getString(R.string.service_off)
                Toast.makeText(applicationContext, getString(R.string.service_turned_off), Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun startService() {
        val serviceIntent = Intent(this, BatteryAlarmService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
    }


    private fun stopService() {
        val serviceIntent = Intent(this, BatteryAlarmService::class.java)
        stopService(serviceIntent)
    }

    private var batteryInfoReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val batteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)

            if (intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) == 0) {
                binding.txtPlug.text = getString(R.string.plugged_out)
            } else {
                binding.txtPlug.text = getString(R.string.plugged_in)
            }


            binding.txtTemp.text =
                (intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10).toString() + " °C"
            binding.txtVoltage.text =
                (intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) / 1000).toString() + " volt"
            binding.txtTechnology.text = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY)


            binding.circularProgressBar.progressMax = 100f
            binding.circularProgressBar.setProgressWithAnimation(batteryLevel.toFloat())
            binding.txtCharge.text = batteryLevel.toString() + "%"


            val health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, 0)
            when (health) {
                BatteryManager.BATTERY_HEALTH_DEAD -> {
                    binding.txtHealth.text = getString(R.string.battery_dead)
                    binding.txtHealth.setTextColor(Color.BLACK)
                    binding.imgHealth.setImageResource(R.drawable.health_dead)
                }
                BatteryManager.BATTERY_HEALTH_GOOD -> {
                    binding.txtHealth.text = getString(R.string.battery_fine)
                    binding.txtHealth.setTextColor(Color.GREEN)
                    binding.imgHealth.setImageResource(R.drawable.health_good)
                }
                BatteryManager.BATTERY_HEALTH_COLD -> {
                    binding.txtHealth.text = getString(R.string.battery_cold)
                    binding.txtHealth.setTextColor(Color.BLUE)
                    binding.imgHealth.setImageResource(R.drawable.health_cold)
                }
                BatteryManager.BATTERY_HEALTH_OVERHEAT -> {
                    binding.txtHealth.text =getString(R.string.battery_overheat)
                    binding.txtHealth.setTextColor(Color.RED)
                    binding.imgHealth.setImageResource(R.drawable.health_overheat)
                }
                BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> {
                    binding.txtHealth.text =getString(R.string.battery_over_voltage)
                    binding.txtHealth.setTextColor(Color.YELLOW)
                    binding.imgHealth.setImageResource(R.drawable.health_volt)
                }
                else -> {
                    binding.txtHealth.text = getString(R.string.battery_dead)
                    binding.txtHealth.setTextColor(Color.BLACK)
                    binding.imgHealth.setImageResource(R.drawable.health_dead)
                }
            }

        }
    }


    override fun onBackPressed() {

        val dialogBuilder = AlertDialog.Builder(this)
            .setMessage(getString(R.string.close_application_test))
            .setCancelable(true)
            .setPositiveButton(getString(R.string.yes) , DialogInterface.OnClickListener{
                dialog , id -> finish()
            })
            .setNegativeButton(getString(R.string.cancel)  , DialogInterface.OnClickListener{
                    dialog , id -> dialog.cancel()
            })
        val alert = dialogBuilder.create()
        alert.setTitle(getString(R.string.exit_app))
        alert.show()


    }


}