package com.example.harprojecapp.benchmark

import androidx.benchmark.macro.*
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PageNavPerfTest {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun navHomeToSensorsAndBack() = benchmarkRule.measureRepeated(
        packageName = "com.example.harprojecapp",
        metrics = listOf(FrameTimingMetric()),
        iterations = 15,
        startupMode = StartupMode.WARM,
        compilationMode = CompilationMode.Partial()
    ) {
        // Home → Sensors
        device.findObject(By.res("com.example.harprojecapp:id/nav_sensors")).click()
        device.wait(Until.hasObject(By.res("com.example.harprojecapp:id/sensorList")), 3_000)

        // Sensors → Home
        device.findObject(By.res("com.example.harprojecapp:id/nav_home")).click()
        device.wait(Until.hasObject(By.res("com.example.harprojecapp:id/homeTextView")), 3_000)
    }
}
