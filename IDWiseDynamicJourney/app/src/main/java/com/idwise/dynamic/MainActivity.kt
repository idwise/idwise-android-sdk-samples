package com.idwise.dynamic

import android.app.ProgressDialog
import android.graphics.Bitmap
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
import com.idwise.sdk.IDWise
import com.idwise.sdk.IDWiseSDKCallback
import com.idwise.sdk.IDWiseSDKStepCallback
import com.idwise.sdk.data.models.IDWiseSDKError
import com.idwise.sdk.data.models.JourneyInfo
import com.idwise.sdk.data.models.StepResult
import com.idwise.sdk.data.models.IDWiseSDKTheme
import java.util.*


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

        initializeSDK()
    }

    private fun initializeSDK() {
        //TODO Change this clientKey with one provided by IDWise
        val clientKey = Constants.CLIENT_KEY
        IDWise.initialize(clientKey, IDWiseSDKTheme.SYSTEM_DEFAULT) {
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

    private fun observeJourneySummary() {
        IDWise.getJourneySummary(journeyId) { summary, error ->
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
                IDWise.finishDynamicJourney(journeyId)
                preferences.clear()
            }
        }
    }


    private fun listeners() {

        binding.btnNewJourney?.setOnClickListener {
            preferences.clear()
            IDWise.unloadSDK()
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
            IDWise.startStep(this@MainActivity, STEP_ID_DOCUMENT)
        }

        //Step ID may vary as for default journey 2 is used for selfie
        binding.btnJourneyStepTwo.setOnClickListener {
            IDWise.startStep(this@MainActivity, STEP_SELFIE)
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

            stepResultsMap[stepId] = stepResult
            observeJourneySummary()

            Log.d(TAG, "onStepResult StepId $stepId")
            Log.d(TAG, "OnStepResult  $stepId \n ${stepResult?.toString()}")
        }

        override fun onStepConfirmed(stepId: String) {
            Log.d(TAG, "onStepConfirmed Step $stepId confirmed!!")
        }

        override fun onStepCancelled(stepId: String) {
            Log.d(TAG, "onStepCancelled Step $stepId cancelled!!")
        }

    }

    private val journeyCallback = object : IDWiseSDKCallback {
        override fun onJourneyStarted(journeyInfo: JourneyInfo) {
            toast("Journey Started ${journeyInfo.journeyId}")
            hideProgressDialog()
            journeyId = journeyInfo.journeyId
            preferences.journeyId = journeyInfo.journeyId
        }

        override fun onJourneyResumed(journeyInfo: JourneyInfo) {
            hideProgressDialog()
            journeyId = journeyInfo.journeyId
            observeJourneySummary()
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
        IDWise.unloadSDK()
    }
}
