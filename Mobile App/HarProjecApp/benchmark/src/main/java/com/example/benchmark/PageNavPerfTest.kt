package com.yourapp.benchmark       // ← klasör yoluna göre güncelle

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
    fun navHomeSensorsBack() = benchmarkRule.measureRepeated(
        packageName = "com.example.harprojecapp",
        metrics = listOf(FrameTimingMetric()),
        iterations = 15,
        startupMode = StartupMode.WARM,
        compilationMode = CompilationMode.None()
    ) {
        val navSensorsBtn = device.wait(
            Until.findObject(By.res("com.example.harprojecapp:id/nav_sensors")),
            3_000
        )
        navSensorsBtn?.click()

        device.wait(Until.hasObject(By.res("com.example.harprojecapp:id/titleText")), 3_000)

        val navHomeBtn = device.wait(
            Until.findObject(By.res("com.example.harprojecapp:id/nav_home")),
            3_000
        )
        navHomeBtn?.click()

        device.wait(Until.hasObject(By.res("com.example.harprojecapp:id/startStopButton")), 3_000)
    }
}
