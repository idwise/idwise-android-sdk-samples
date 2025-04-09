package com.idwise.simple

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import com.idwise.sdk.IDWise
import com.idwise.sdk.IDWiseJourneyCallbacks
import com.idwise.sdk.data.models.IDWiseError
import com.idwise.sdk.data.models.*
import com.idwise.simple.databinding.ActivityMainBinding
import com.idwise.sdk.data.models.IDWiseTheme
import com.idwise.simple.extensions.preventMultipleTap

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.btnStartJourney.setOnClickListener {
            it.preventMultipleTap()
            if (IDWise.isDeviceBlocked(this@MainActivity)) {
                Log.v(
                    "RAW_EVENT",
                    "Device is blocked, you can not perform verification on this device"
                )
            } else {
                initializeSDK()
                startJourney()
            }
        }
    }

    private fun initializeSDK() {
        IDWise.initialize(
            "<CLIENT_KEY>",
            IDWiseTheme.SYSTEM_DEFAULT
        ) { error: IDWiseError? ->

            Log.v("RAW_EVENT", "onError: ${error?.message}")
        }
    }

    private fun startJourney() {
        IDWise.startJourney(
            context = this,
            flowId = "<Flow ID>",
            referenceNo = "<User Reference No.>",
            locale = "en",
            journeyCallbacks = object : IDWiseJourneyCallbacks {
                override fun onJourneyStarted(journeyInfo: JourneyStartedInfo) {
                    Log.v("RAW_EVENT", "onJourneyStarted: ${journeyInfo?.journeyId}")

                }

                override fun onJourneyResumed(journeyInfo: JourneyResumedInfo) {

                }

                override fun onJourneyCompleted(
                    journeyInfo: JourneyCompletedInfo
                ) {

                }

                override fun onJourneyCancelled(journeyInfo: JourneyCancelledInfo) {

                }

                override fun onError(error: IDWiseError) {

                    Log.v("RAW_EVENT", "onError: ${error.message.toString()}")
                }

                override fun onJourneyBlocked(journeyBlockedInfo: JourneyBlockedInfo) {
                    Log.v("RAW_EVENT", "journeyBlockedInfo: $journeyBlockedInfo")
                }
            }
        )
    }
}
