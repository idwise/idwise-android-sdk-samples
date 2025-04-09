package com.idwise.dynamic


import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.idwise.dynamic.databinding.ActivityMainBinding
import com.idwise.dynamic.extensions.showInfoLoginDialog
import com.idwise.dynamic.utils.AppPreferences
import com.idwise.dynamic.utils.FileStorageHelper
import com.idwise.dynamic.utils.FileUploadHelper
import com.idwise.sdk.IDWise
import com.idwise.sdk.IDWiseDynamic
import com.idwise.sdk.IDWiseJourneyCallbacks
import com.idwise.sdk.IDWiseStepCallbacks
import com.idwise.sdk.data.models.IDWiseError
import com.idwise.sdk.data.models.IDWiseTheme
import com.idwise.sdk.data.models.JourneyBlockedInfo
import com.idwise.sdk.data.models.JourneyCancelledInfo
import com.idwise.sdk.data.models.JourneyCompletedInfo
import com.idwise.sdk.data.models.JourneyResumedInfo
import com.idwise.sdk.data.models.JourneyStartedInfo
import com.idwise.sdk.data.models.StepCancelledInfo
import com.idwise.sdk.data.models.StepCapturedInfo
import com.idwise.sdk.data.models.StepResultInfo
import com.idwise.sdk.data.models.StepSkippedInfo
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
        IDWise.initialize(clientKey, IDWiseTheme.LIGHT) {
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
        IDWiseDynamic.startJourney(
            this,
            //TODO Change this journeyDefinitionId with one provided by IDWise
            Constants.JOURNEY_DEFINITION_ID,
            preferences.referenceNumber,
            Constants.LOCALE,
            journeyCallbacks = journeyCallback,
            stepCallbacks = stepCallback
        )
    }

    private fun resumeJourney(journeyId: String) {
        IDWiseDynamic.resumeJourney(
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
            IDWiseDynamic.unloadSDK()
            recreate()
            showProgressDialog()
        }

        //Step ID may vary as for default journey 0 is used for document front and 1 for document back
        binding.btnJourneyStepOne.setOnClickListener {
            IDWiseDynamic.startStep(this@MainActivity, "0")
        }

        //Step ID may vary as for default journey 2 is used for selfie
        binding.btnJourneyStepTwo.setOnClickListener {
            IDWiseDynamic.startStep(this@MainActivity, "2")
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

    private val stepCallback = object : IDWiseStepCallbacks {
        override fun onStepCancelled(stepCancelledInfo: StepCancelledInfo) {
            Log.d(TAG, "onStepCancelled ${stepCancelledInfo.stepId}")
        }

        override fun onStepCaptured(stepCapturedInfo: StepCapturedInfo) {
            Log.d(TAG, "StepId ${stepCapturedInfo.stepId}")
            stepCapturedInfo.croppedImage?.let {
                FileStorageHelper.saveBitmap(
                    this@MainActivity,
                    preferences.journeyId + "_" + stepCapturedInfo.stepId,
                    it
                )
            }
        }

        override fun onStepResult(stepResultInfo: StepResultInfo) {
            hideProgressDialog()

            Log.d(TAG, "onStepResult StepId ${stepResultInfo.stepId}")
            Log.d(TAG, "OnStepResult  $stepResultInfo")
        }

        override fun onStepSkipped(stepSkippedInfo: StepSkippedInfo) {
            Log.d(TAG, "onStepSkipped ${stepSkippedInfo.stepId}")

        }


    }

    private val journeyCallback = object : IDWiseJourneyCallbacks {
        override fun onJourneyStarted(journeyInfo: JourneyStartedInfo) {
            toast("Journey Started ${journeyInfo.journeyId}")
            hideProgressDialog()
            preferences.journeyId = journeyInfo.journeyId
        }

        override fun onJourneyResumed(journeyInfo: JourneyResumedInfo) {
            hideProgressDialog()
            toast("Journey Resumed ${journeyInfo.journeyId}")
        }

        override fun onJourneyCompleted(journeyInfo: JourneyCompletedInfo) {
            toast("Journey Completed")
        }

        override fun onJourneyCancelled(journeyInfo: JourneyCancelledInfo) {
            toast("Journey Cancelled")
            preferences.reset()
        }

        override fun onError(error: IDWiseError) {
            hideProgressDialog()
            error.printStackTrace()
            toast("Error: " + error.message)
        }

        override fun onJourneyBlocked(journeyBlockedInfo: JourneyBlockedInfo) {
            toast("Journey Blocked ${journeyBlockedInfo.blockedTransaction?.allBlockReasons?.firstOrNull()}")
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
        IDWiseDynamic.unloadSDK()
    }
}