package com.idwise.dynamic

import android.app.ProgressDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ShareCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.idwise.dynamic.databinding.ActivityMainBinding
import com.idwise.dynamic.databinding.LayoutStepDetailBinding
import com.idwise.dynamic.extensions.showInfoLoginDialog
import com.idwise.dynamic.utils.AppPreferences
import com.idwise.dynamic.utils.FileStorageHelper
import com.idwise.dynamic.utils.FileUploadHelper
import com.idwise.sdk.IDWiseDynamic
import com.idwise.sdk.IDWiseJourneyCallbacks
import com.idwise.sdk.IDWiseStepCallbacks
import com.idwise.sdk.data.models.*
import java.util.UUID


class MainActivity : AppCompatActivity() {
    private val TAG = "DynamicJourneyDemo"

    lateinit var binding: ActivityMainBinding

    private lateinit var progress: ProgressDialog
    private lateinit var preferences: AppPreferences
    private lateinit var fileUploadHelper: FileUploadHelper

    private val STEP_ID_DOCUMENT = "0";
    private val STEP_SELFIE = "2";

    lateinit var journeyId: String
    private var stepResultsMap = HashMap<String, StepResult?>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        preferences = AppPreferences(this)
        progress = ProgressDialog(this)

        showProgressDialog()

        fileUploadHelper = FileUploadHelper().init(this)

        listeners()

        if (IDWiseDynamic.isDeviceBlocked(this@MainActivity)) {
            Log.d(TAG, "This device is blocked")
        } else {
            initializeSDK()
        }
    }

    private fun initializeSDK() {
        //TODO Change this clientKey with one provided by IDWise
        val clientKey = Constants.CLIENT_KEY
        IDWiseDynamic.initialize(clientKey, IDWiseTheme.SYSTEM_DEFAULT) {
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
            context = this,
            flowId = Constants.JOURNEY_DEFINITION_ID,
            referenceNo = preferences.referenceNumber,
            locale = Constants.LOCALE,
            journeyCallbacks = journeyCallback,
            stepCallbacks = stepCallback
        )
    }

    private fun resumeJourney(journeyId: String) {
        IDWiseDynamic.resumeJourney(
            context = this,
            flowId = Constants.JOURNEY_DEFINITION_ID,
            journeyId = journeyId,
            locale = Constants.LOCALE,
            journeyCallbacks = journeyCallback,
            stepCallbacks = stepCallback
        )
    }

    private fun observeJourneySummary() {
        IDWiseDynamic.getJourneySummary { summary, error ->
            Log.v(TAG, Gson().toJson(summary))

            summary?.stepSummaries?.find { it.definition.stepId.toString() == STEP_ID_DOCUMENT }
                ?.let {
                    binding.btnJourneyStepOne.isEnabled = it.result?.isConcluded != true
                    binding.ivInfoStepOne.isVisible = it.result?.isConcluded == true
                }

            summary?.stepSummaries?.find { it.definition.stepId.toString() == STEP_SELFIE }
                ?.let {
                    binding.btnJourneyStepTwo.isEnabled = it.result?.isConcluded != true
                    binding.ivInfoStepTwo.isVisible = it.result?.isConcluded == true
                }

            if (summary?.isCompleted == true) {
                binding.consLayoutCompleteStatus.isVisible = true
                IDWiseDynamic.finishJourney()
                preferences.clear()
            }
        }
    }


    private fun listeners() {

        binding.btnNewJourney?.setOnClickListener {
            preferences.clear()
            IDWiseDynamic.unloadSDK()
            recreate()
            showProgressDialog()

            journeyId = ""
            stepResultsMap.clear()
            binding.ivInfoStepOne.isVisible = false
            binding.ivInfoStepTwo.isVisible = false
            binding.consLayoutCompleteStatus.isVisible = false
        }

        //Step ID may vary as for default journey 0 is used for document front and 1 for document back
        binding.btnJourneyStepOne.setOnClickListener {
            IDWiseDynamic.startStep(this@MainActivity, STEP_ID_DOCUMENT)
        }

        //Step ID may vary as for default journey 2 is used for selfie
        binding.btnJourneyStepTwo.setOnClickListener {
            IDWiseDynamic.startStep(this@MainActivity, STEP_SELFIE)
        }

        /*binding.ivInfoStepOne.setOnClickListener {
            showBottomSheetDialog(STEP_ID_DOCUMENT)
        }

        binding.ivInfoStepTwo.setOnClickListener {
            showBottomSheetDialog(STEP_SELFIE)
        }*/

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

    private fun showBottomSheetDialog(stepId: String) {
        val bottomSheet = ExpandedBottomSheetDialog(this@MainActivity)
        val binding = LayoutStepDetailBinding.inflate(layoutInflater)
        bottomSheet.setContentView(binding.root)
        bottomSheet.setCancelable(false)

        binding.tvStepId.text = "Step Id: ${stepId}"
        val gson = GsonBuilder().setPrettyPrinting().create()
        val je = JsonParser.parseString(Gson().toJson(stepResultsMap[stepId]))

        val stepResultDetails = gson.toJson(je)
        binding.tvStepDetails.text = stepResultDetails

        binding.btnShare.setOnClickListener {
            ShareCompat.IntentBuilder(this@MainActivity)
                .setSubject("Step Result : ${stepId}")
                .setText(stepResultDetails)
                .setType("text/plain")
                .startChooser()
        }

        binding.btnClose.setOnClickListener {
            bottomSheet.dismiss()
        }

        bottomSheet.show()
    }

    private val stepCallback = object : IDWiseStepCallbacks {
        override fun onStepCancelled(stepCancelledInfo: StepCancelledInfo) {
            Log.d(TAG, "onStepCancelled ${stepCancelledInfo.stepId}")
        }

        override fun onStepCaptured(stepInfo: StepCapturedInfo) {
            Log.d(TAG, "StepId ${stepInfo.stepId}")
            stepInfo.croppedImage?.let {
                FileStorageHelper.saveBitmap(
                    this@MainActivity,
                    preferences.journeyId + "_" + stepInfo.stepId,
                    it
                )
            }
        }

        override fun onStepResult(stepInfo: StepResultInfo) {
            hideProgressDialog()

            stepResultsMap[stepInfo.stepId] = stepInfo.stepResult
            observeJourneySummary()

            Log.d(TAG, "onStepResult StepId ${stepInfo.stepId}")
            Log.d(TAG, "OnStepResult  ${stepInfo.stepId} \n ${stepInfo.stepResult?.toString()}")
        }

        override fun onStepSkipped(stepSkippedInfo: StepSkippedInfo) {
            Log.d(TAG, "onStepSkipped ${stepSkippedInfo.stepId}")
        }


    }

    private val journeyCallback = object : IDWiseJourneyCallbacks {
        override fun onJourneyStarted(journeyInfo: JourneyStartedInfo) {
            toast("Journey Started ${journeyInfo.journeyId}")
            hideProgressDialog()
            journeyId = journeyInfo.journeyId
            preferences.journeyId = journeyInfo.journeyId
        }

        override fun onJourneyResumed(journeyInfo: JourneyResumedInfo) {
            hideProgressDialog()
            journeyId = journeyInfo.journeyId
            observeJourneySummary()
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
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
        }
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
