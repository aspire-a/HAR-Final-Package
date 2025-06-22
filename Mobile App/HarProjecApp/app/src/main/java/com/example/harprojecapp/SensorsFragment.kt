package com.example.harprojecapp

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class SensorsFragment : Fragment() {

    private val client = OkHttpClient()
    private lateinit var espTextViews: List<TextViewGroup>
    private lateinit var lastUpdatedTextView: TextView
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var sharedPref: SharedPreferences
    private var secondsSinceLastUpdate = 0
    private val updateInterval = 3000L // 3 seconds

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sensors, container, false)

        sharedPref = requireActivity().getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val serverIp = sharedPref.getString("SERVER_IP", "") ?: ""
        if (serverIp.isBlank()) {
            Toast.makeText(requireContext(), "IP address not set!", Toast.LENGTH_LONG).show()
            return view
        }
        val raspberryPiBaseUrl = "http://$serverIp:5000/data"

        lastUpdatedTextView = view.findViewById(R.id.lastUpdatedTextView)

        espTextViews = listOf(
            TextViewGroup(
                mpu1TextView = view.findViewById(R.id.mpu1TextView),
                mpu2TextView = view.findViewById(R.id.mpu2TextView),
                hmcTextView = view.findViewById(R.id.hmc1TextView),
                headingTextView = view.findViewById(R.id.heading1TextView)
            ),
            TextViewGroup(
                mpu1TextView = view.findViewById(R.id.mpu1TextView2),
                mpu2TextView = view.findViewById(R.id.mpu2TextView2),
                hmcTextView = view.findViewById(R.id.hmc2TextView),
                headingTextView = view.findViewById(R.id.heading2TextView)
            ),
            TextViewGroup(
                mpu1TextView = view.findViewById(R.id.mpu1TextView3),
                mpu2TextView = view.findViewById(R.id.mpu2TextView3),
                hmcTextView = view.findViewById(R.id.hmc3TextView),
                headingTextView = view.findViewById(R.id.heading3TextView)
            ),
            TextViewGroup(
                mpu1TextView = view.findViewById(R.id.mpu1TextView4),
                mpu2TextView = view.findViewById(R.id.mpu2TextView4),
                hmcTextView = view.findViewById(R.id.hmc4TextView),
                headingTextView = view.findViewById(R.id.heading4TextView)
            ),
            TextViewGroup(
                mpu1TextView = view.findViewById(R.id.mpu1TextView5),
                mpu2TextView = view.findViewById(R.id.mpu2TextView5),
                hmcTextView = view.findViewById(R.id.hmc5TextView),
                headingTextView = view.findViewById(R.id.heading5TextView)
            )
        )

        startPeriodicUpdate()
        return view
    }

    private fun startPeriodicUpdate() {
        handler.post(object : Runnable {
            override fun run() {
                performGetRequest()
                handler.postDelayed(this, updateInterval)
            }
        })

        handler.post(object : Runnable {
            override fun run() {
                secondsSinceLastUpdate++
                lastUpdatedTextView.text = "Last update: $secondsSinceLastUpdate seconds ago"
                handler.postDelayed(this, 1000)
            }
        })
    }

    private fun performGetRequest() {
        val serverIp = sharedPref.getString("SERVER_IP", "") ?: ""
        if (serverIp.isBlank()) {
            requireActivity().runOnUiThread {
                Toast.makeText(requireContext(), "IP address not set!", Toast.LENGTH_LONG).show()
            }
            return
        }
        val raspberryPiBaseUrl = "http://$serverIp:5000/data"

        val request = Request.Builder()
            .url(raspberryPiBaseUrl)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("SensorsFragment", "GET request failed: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    response.body?.string()?.let { data ->
                        activity?.runOnUiThread {
                            updateData(data)
                            secondsSinceLastUpdate = 0
                        }
                    }
                } else {
                    Log.e("SensorsFragment", "GET request unsuccessful: ${response.message}")
                }
            }
        })
    }


    private fun updateData(data: String?) {
        data?.let {
            try {
                val jsonObject = JSONObject(it)
                for (i in espTextViews.indices) {
                    val espKey = "ESP${i + 1}"
                    if (jsonObject.has(espKey)) {
                        val espData = jsonObject.getJSONObject(espKey)
                        updateESPData(espData, espTextViews[i])
                    }
                }
            } catch (e: Exception) {
                Log.e("SensorsFragment", "Error parsing data: ${e.message}")
            }
        }
    }

    private fun updateESPData(espData: JSONObject, textViewGroup: TextViewGroup) {
        try {
            val mpu1 = espData.getJSONObject("MPU1")
            textViewGroup.mpu1TextView.text = """
                MPU1:
                ax: ${mpu1.getDouble("ax")}     ay: ${mpu1.getDouble("ay")}     az: ${mpu1.getDouble("az")}
                gx: ${mpu1.getDouble("gx")}       gy: ${mpu1.getDouble("gy")}     gz: ${mpu1.getDouble("gz")}
            """.trimIndent()

            val mpu2 = espData.getJSONObject("MPU2")
            textViewGroup.mpu2TextView.text = """
                MPU2:
                ax: ${mpu2.getDouble("ax")}     ay: ${mpu2.getDouble("ay")}     az: ${mpu2.getDouble("az")}
                gx: ${mpu2.getDouble("gx")}       gy: ${mpu2.getDouble("gy")}     gz: ${mpu2.getDouble("gz")}
            """.trimIndent()

            val hmc = espData.getJSONObject("HMC")
            textViewGroup.hmcTextView.text = """
                HMC:
                degrees: ${hmc.getDouble("degrees")}
                x: ${hmc.getDouble("x")}      y: ${hmc.getDouble("y")}    z: ${hmc.getDouble("z")}
            """.trimIndent()

            val heading = espData.getJSONObject("Timestamps")
            textViewGroup.headingTextView.text = """
                Date: ${heading.getString("date")}
                Time: ${heading.getString("time")}
            """.trimIndent()

        } catch (e: Exception) {
            Log.e("SensorsFragment", "Error updating ESP data: ${e.message}")
        }
    }

    data class TextViewGroup(
        val mpu1TextView: TextView,
        val mpu2TextView: TextView,
        val hmcTextView: TextView,
        val headingTextView: TextView
    )
}
