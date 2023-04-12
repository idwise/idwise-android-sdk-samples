package com.idwise.simple

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import com.idwise.sdk.IDWise
import com.idwise.sdk.IDWiseSDKCallback
import com.idwise.sdk.data.models.IDWiseSDKError
import com.idwise.sdk.data.models.JourneyInfo
import com.idwise.simple.databinding.ActivityMainBinding
import com.idwise.simple.extensions.preventMultipleTap

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.btnStartJourney.setOnClickListener {
            it.preventMultipleTap()
            initializeSDK()
            startJourney()
        }
    }

    private fun initializeSDK() {
        IDWise.initialize("<CLIENT_KEY>") { error: IDWiseSDKError? ->

            Log.v("RAW_EVENT", "onError: ${error?.message}")
        }
    }

    private fun startJourney() {
        IDWise.startJourney(
            this,
            "<JOURNEY_DEFINITION_ID>",
            "",
            "en",
            object : IDWiseSDKCallback {
                override fun onJourneyStarted(journeyInfo: JourneyInfo) {

                }

                override fun onJourneyResumed(journeyInfo: JourneyInfo) {

                }

                override fun onJourneyCompleted(
                    journeyInfo: JourneyInfo,
                    isSucceeded: Boolean
                ) {

                }

                override fun onJourneyCancelled(journeyInfo: JourneyInfo?) {

                }

                override fun onJourneyInterrupted(journeyInfo: JourneyInfo?) {

                }

                override fun onError(error: IDWiseSDKError) {

                    Log.v("RAW_EVENT", "onError: ${error.message.toString()}")
                }
            }
        )
    }
}