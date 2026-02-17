package com.example.huntopia

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

class ScanFragment : Fragment(R.layout.fragment_scan) {

    private val repository = AchievementRepository()

    private val scannerOptions = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
        .build()

    private val scanner by lazy { BarcodeScanning.getClient(scannerOptions) }

    private var imageAnalysis: ImageAnalysis? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private val cameraExecutor = Executors.newSingleThreadExecutor()

    @Volatile
    private var isProcessing = false

    @Volatile
    private var isFinishing = false

    private val requestCameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startCamera()
        } else {
            Toast.makeText(
                requireContext(),
                "Camera permission is required to scan QR code",
                Toast.LENGTH_SHORT
            ).show()
            closeScanner()
        }
    }

    override fun onViewCreated(view: android.view.View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<ImageButton>(R.id.btn_close).setOnClickListener {
            closeScanner()
        }

        requestCameraPermission.launch(Manifest.permission.CAMERA)
    }

    private fun startCamera() {
        val previewView = view?.findViewById<PreviewView>(R.id.preview_view) ?: return
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val provider = cameraProviderFuture.get()
            cameraProvider = provider

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.getSurfaceProvider())
            }

            imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { analysis ->
                    analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                        analyzeImage(imageProxy)
                    }
                }

            try {
                provider.unbindAll()
                provider.bindToLifecycle(
                    viewLifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageAnalysis
                )
            } catch (_: Exception) {
                Toast.makeText(requireContext(), "Unable to open camera", Toast.LENGTH_SHORT).show()
                closeScanner()
            }
        }, androidx.core.content.ContextCompat.getMainExecutor(requireContext()))
    }

    private fun analyzeImage(imageProxy: ImageProxy) {
        if (isProcessing || isFinishing) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                val rawValue = barcodes.firstNotNullOfOrNull { it.rawValue?.trim() }
                if (!rawValue.isNullOrBlank() && !isProcessing && !isFinishing) {
                    isProcessing = true
                    requireActivity().runOnUiThread {
                        handleScannedValue(rawValue)
                    }
                }
            }
            .addOnFailureListener {
                // Ignore per-frame ML errors and continue scanning.
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    private fun handleScannedValue(rawValue: String) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(requireContext(), "Please log in again", Toast.LENGTH_SHORT).show()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            when (val result = repository.collectIfNew(user.uid, rawValue)) {
                is CollectResult.SuccessNew -> {
                    Toast.makeText(
                        requireContext(),
                        "Collected: ${result.item.foundTitle}",
                        Toast.LENGTH_SHORT
                    ).show()
                    closeScanner()
                }

                is CollectResult.AlreadyCollected -> {
                    Toast.makeText(requireContext(), "Already collected", Toast.LENGTH_SHORT).show()
                    closeScanner()
                }

                is CollectResult.InvalidCode,
                is CollectResult.CatalogMissing -> {
                    Toast.makeText(requireContext(), "Invalid QR code", Toast.LENGTH_SHORT).show()
                    closeScanner()
                }

                is CollectResult.NotLoggedIn -> {
                    Toast.makeText(requireContext(), "Please log in again", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(requireContext(), LoginActivity::class.java))
                    requireActivity().finish()
                }

                is CollectResult.Error -> {
                    Toast.makeText(
                        requireContext(),
                        "Couldn't save scan. Try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                    closeScanner()
                }
            }
        }
    }

    private fun closeScanner() {
        if (isFinishing) {
            return
        }
        isFinishing = true
        stopCamera()
        if (isAdded) {
            parentFragmentManager.popBackStack()
        }
    }

    private fun stopCamera() {
        imageAnalysis?.clearAnalyzer()
        cameraProvider?.unbindAll()
        imageAnalysis = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopCamera()
        scanner.close()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
