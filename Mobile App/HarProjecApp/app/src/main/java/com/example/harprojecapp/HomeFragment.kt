package com.example.harprojecapp

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {

    private var isRecording = false
    private var startDate: String? = null
    private var startTime: String? = null
    private var endDate: String? = null
    private var endTime: String? = null
    private val client = OkHttpClient()

    private lateinit var sharedPref: SharedPreferences
    private lateinit var ipAddressInput: EditText
    private lateinit var saveIpButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        sharedPref = requireActivity().getSharedPreferences("AppSettings", Context.MODE_PRIVATE)

        ipAddressInput = view.findViewById(R.id.ipAddressInput)
        saveIpButton = view.findViewById(R.id.saveIpButton)
        ipAddressInput.setText(sharedPref.getString("SERVER_IP", ""))

        saveIpButton.setOnClickListener {
            val enteredIp = ipAddressInput.text.toString()
                .removePrefix("http://")
                .removePrefix("https://")
                .removeSuffix("/")
            if (enteredIp.isNotBlank()) {
                sharedPref.edit().putString("SERVER_IP", enteredIp).apply()
                Toast.makeText(requireContext(), "IP saved: $enteredIp", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Please enter a valid IP", Toast.LENGTH_SHORT).show()
            }
        }


        val activitySpinner: Spinner = view.findViewById(R.id.activitySpinner)
        val progressLabel: TextView = view.findViewById(R.id.progressLabel)
        val startStopButton: Button = view.findViewById(R.id.startStopButton)
        val heightInput: EditText = view.findViewById(R.id.heightInput)


        val activities = listOf("Running", "Walking", "Jumping", "Standing")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, activities)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        activitySpinner.adapter = adapter

        val isRecordingSaved = sharedPref.getBoolean("IS_RECORDING", false)
        if (isRecordingSaved) {
            isRecording = true
            val savedActivity = sharedPref.getString("REC_ACTIVITY", "") ?: ""
            startDate = sharedPref.getString("REC_DATE", "")
            startTime = sharedPref.getString("REC_TIME", "")

            progressLabel.text = "Labeling In Progress: $savedActivity\nStarted at: $startDate $startTime"
            progressLabel.visibility = View.VISIBLE
            startStopButton.text = "Stop"

            val index = activities.indexOf(savedActivity)
            if (index != -1) activitySpinner.setSelection(index)
        }

        startStopButton.setOnClickListener {
            val selectedActivity = activitySpinner.selectedItem.toString()
            val height = heightInput.text.toString()

            isRecording = !isRecording

            if (isRecording) {
                val timestamp = getCurrentTimestamp()
                startDate = timestamp.first
                startTime = timestamp.second
                progressLabel.text = "Labeling In Progress: $selectedActivity\nStarted at: $startDate $startTime"
                progressLabel.visibility = View.VISIBLE
                startStopButton.text = "Stop"

                sharedPref.edit().apply {
                    putBoolean("IS_RECORDING", true)
                    putString("REC_ACTIVITY", selectedActivity)
                    putString("REC_DATE", startDate)
                    putString("REC_TIME", startTime)
                    apply()
                }

            } else {
                val timestamp = getCurrentTimestamp()
                endDate = timestamp.first
                endTime = timestamp.second
                progressLabel.visibility = View.GONE
                startStopButton.text = "Start"

                sharedPref.edit().putBoolean("IS_RECORDING", false).apply()

                sendData(selectedActivity, startDate, startTime, endDate, endTime, height)
            }
        }


        return view
    }



    private fun getCurrentTimestamp(): Pair<String, String> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val currentDate = Date()
        return Pair(dateFormat.format(currentDate), timeFormat.format(currentDate))
    }

    private fun sendData(
        activity: String?,
        startDate: String?,
        startTime: String?,
        endDate: String?,
        endTime: String?,
        height: String?
    ) {

        if (activity.isNullOrBlank() || startDate.isNullOrBlank() || startTime.isNullOrBlank()
            || endDate.isNullOrBlank() || endTime.isNullOrBlank()) {

            Toast.makeText(requireContext(), "There is partial data. Please start/stop again!", Toast.LENGTH_LONG).show()
            return
        }

        val serverIp = sharedPref.getString("SERVER_IP", "") ?: ""
        if (serverIp.isBlank()) {
            Toast.makeText(requireContext(), "IP address not set!", Toast.LENGTH_LONG).show()
            return
        }

        val serverUrl = "http://$serverIp:5000/activity"

        val json = """
            {
                "activity_label": "$activity",
                "activity_start_date": "$startDate",
                "activity_start_time": "$startTime",
                "activity_end_date": "$endDate",
                "activity_end_time": "$endTime",
                "height": "$height"
            }
        """.trimIndent()

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = json.toRequestBody(mediaType)

        val request = Request.Builder()
            .url(serverUrl)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Failed to send data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                requireActivity().runOnUiThread {
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "Data sent successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        val errorBody = response.body?.string()
                        Log.e("POST_ERROR", "Server error: ${response.code} - $errorBody")
                        Toast.makeText(requireContext(), "Server error: ${response.code}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}
