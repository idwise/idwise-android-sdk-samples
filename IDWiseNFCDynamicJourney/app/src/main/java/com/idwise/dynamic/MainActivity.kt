package com.idwise.dynamic


import android.app.ProgressDialog
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.idwise.dynamic.databinding.ActivityMainBinding
import com.idwise.dynamic.extensions.showInfoLoginDialog
import com.idwise.dynamic.utils.AppPreferences
import com.idwise.dynamic.utils.FileStorageHelper
import com.idwise.dynamic.utils.FileUploadHelper
import com.idwise.sdk.IDWise
import com.idwise.sdk.IDWiseSDKCallback
import com.idwise.sdk.IDWiseSDKStepCallback
import com.idwise.sdk.data.models.IDWiseSDKError
import com.idwise.sdk.data.models.IDWiseSDKTheme
import com.idwise.sdk.data.models.JourneyInfo
import com.idwise.sdk.data.models.StepResult
import java.util.*

class MainActivity : AppCompatActivity() {
    lateinit var progress: ProgressDialog
    lateinit var preferences: AppPreferences
    lateinit var fileUploadHelper: FileUploadHelper

    val TAG = "DynamicJourneyDemo"

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        preferences = AppPreferences(this)
        progress = ProgressDialog(this)

        showProgressDialog()

        fileUploadHelper = FileUploadHelper().init(this)

        listeners()

        initializeSDK()
    }

    private fun initializeSDK() {
        //TODO Change this clientKey with one provided by IDWise
        val clientKey = Constants.CLIENT_KEY
        IDWise.initialize(clientKey, IDWiseSDKTheme.LIGHT) {
            showInfoLoginDialog(getString(R.string.error), it?.message ?: "N/A")
            hideProgressDialog()
        }

        if (preferences.journeyId.isEmpty()) {
            preferences.referenceNumber = "idwise_test_" + UUID.randomUUID().toString()
            startDynamicJourney()
        } else {
            resumeJourney(preferences.journeyId)
        }

    }


    private fun startDynamicJourney() {
        IDWise.startDynamicJourney(
            this,
            //TODO Change this journeyDefinitionId with one provided by IDWise
            Constants.JOURNEY_DEFINITION_ID,
            preferences.referenceNumber,
            Constants.LOCALE,
            journeyCallback,
            stepCallback
        )
    }

    private fun resumeJourney(journeyId: String) {
        IDWise.resumeDynamicJourney(
            this,
            Constants.JOURNEY_DEFINITION_ID,
            journeyId,
            Constants.LOCALE,
            journeyCallback,
            stepCallback
        )
    }


    private fun listeners() {

        binding.btnNewJourney.setOnClickListener {
            preferences.clear()
            IDWise.unloadSDK()
            recreate()
            showProgressDialog()
        }

        //Step ID may vary as for default journey 0 is used for document front and 1 for document back
        binding.btnJourneyStepOne.setOnClickListener {
            IDWise.startStep(this@MainActivity,"0")
        }

        //Step ID may vary as for default journey 2 is used for selfie
        binding.btnJourneyStepTwo.setOnClickListener {
            IDWise.startStep(this@MainActivity,"2")
        }

        /**
         * Here is a sample of how a user can select a file from storage
         */

        /*btnUploadFromGallery?.setOnClickListener {
            fileUploadHelper.withCallback { bitmap, uri, isSuccess ->
                if (isSuccess) {

                    val bos = ByteArrayOutputStream()
                    bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, bos)
                    //convert to ByteArray
                    val byteArray = bos.toByteArray()

                    //Start upload step
                    IDWise.startStepFromFileUpload(this, Steps.LABOUR_CARD, byteArray)

                }
            }.pickImage()
        }*/
    }

    private val stepCallback = object : IDWiseSDKStepCallback {
        override fun onStepCaptured(stepId: String, bitmap: Bitmap?, croppedBitmap: Bitmap?) {
            Log.d(TAG, "StepId $stepId")
            croppedBitmap?.let {
                FileStorageHelper.saveBitmap(
                    this@MainActivity,
                    preferences.journeyId + "_" + stepId,
                    croppedBitmap
                )
            }
        }

        override fun onStepResult(stepId: String, stepResult: StepResult?) {
            hideProgressDialog()

            Log.d(TAG, "onStepResult StepId $stepId")
            Log.d(TAG, "OnStepResult  $stepId \n ${stepResult?.toString()}")
        }

        override fun onStepConfirmed(stepId: String) {
            Log.d(TAG, "onStepConfirmed Step $stepId confirmed!!")
        }

    }

    private val journeyCallback = object : IDWiseSDKCallback {
        override fun onJourneyStarted(journeyInfo: JourneyInfo) {
            toast("Journey Started ${journeyInfo.journeyId}")
            hideProgressDialog()
            preferences.journeyId = journeyInfo.journeyId
        }

        override fun onJourneyResumed(journeyInfo: JourneyInfo) {
            hideProgressDialog()
            toast("Journey Resumed ${journeyInfo.journeyId}")
        }

        override fun onJourneyCompleted(journeyInfo: JourneyInfo, isSucceeded: Boolean) {
            toast("Journey Completed")
        }

        override fun onJourneyCancelled(journeyInfo: JourneyInfo?) {
            toast("Journey Cancelled")
            preferences.reset()
        }

        override fun onError(error: IDWiseSDKError) {
            hideProgressDialog()
            error.printStackTrace()
            toast("Error: " + error.message)
        }
    }

    private fun toast(message: String) {
        Log.d(TAG, message)
        Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
    }

    fun showProgressDialog() {
        if (progress.isShowing) return
        progress.setMessage("Please Wait")
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progress.isIndeterminate = true;
        progress.show();
    }

    fun hideProgressDialog() {
        if (progress.isShowing)
            progress.dismiss()
    }

    override fun onDestroy() {
        super.onDestroy()
        IDWise.unloadSDK()
    }
}