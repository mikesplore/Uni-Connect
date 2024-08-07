package com.mike.uniadmin.chat

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import androidx.camera.core.Preview as myPreview


@Composable
fun QRCodeImage(data: String, size: Dp) {
    val qrCodeBitmap = remember(data) {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(
            data, BarcodeFormat.QR_CODE, size.value.toInt(), size.value.toInt()
        )
        val width = bitMatrix.width
        val height = bitMatrix.height
        Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565).apply {
            for (x in 0 until width) {
                for (y in 0 until height) {
                    setPixel(
                        x,
                        y,
                        if (bitMatrix[x, y]) Color.Black.toArgb() else Color.White.toArgb()
                    )
                }
            }
        }
    }

    Image(
        painter = BitmapPainter(qrCodeBitmap.asImageBitmap()),
        contentDescription = "QR Code",
        modifier = Modifier.size(size)
    )
}

@Composable
fun CameraPreview(context: Context, onBarcodeScanned: (String) -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current

    val previewView = remember { PreviewView(context) }
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    DisposableEffect(Unit) {
        val cameraProvider = cameraProviderFuture.get()

        val preview = myPreview.Builder().build().apply {
            setSurfaceProvider(previewView.surfaceProvider)
        }

        val barcodeScanner = BarcodeScanning.getClient()

        val imageAnalyzer =
            ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build().also { analysis ->
                    analysis.setAnalyzer(
                        ContextCompat.getMainExecutor(context)
                    ) { imageProxy ->
                        processImageProxy(barcodeScanner, imageProxy, onBarcodeScanned)
                    }
                }

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner, cameraSelector, preview, imageAnalyzer
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

        onDispose {
            cameraProvider.unbindAll()
        }
    }

    AndroidView(
        factory = { previewView }, modifier = Modifier.fillMaxSize()
    )
}

@OptIn(ExperimentalGetImage::class)
private fun processImageProxy(
    barcodeScanner: BarcodeScanner, imageProxy: ImageProxy, onBarcodeScanned: (String) -> Unit
) {
    val mediaImage = imageProxy.image ?: return
    val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

    barcodeScanner.process(inputImage).addOnSuccessListener { barcodes ->
            for (barcode in barcodes) {
                barcode.rawValue?.let { value ->
                    onBarcodeScanned(value)
                }
            }
        }.addOnFailureListener { e ->
            e.printStackTrace()
        }.addOnCompleteListener {
            imageProxy.close()
        }
}

@Composable
fun QRCodeScanner(context: Context, onScanned: (String) -> Unit) {
    var scannedCode by remember { mutableStateOf("") }
    var legit by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp), // Adjusted height for visibility
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Box(modifier = Modifier.size(200.dp)) { // Set a fixed size for the preview
            CameraPreview(context) { scannedValue ->
                scannedCode = scannedValue
            }

            if (legit) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Check",
                    tint = Color.Green,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(40.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        if (scannedCode.isNotEmpty()) {
            legit = true
            Toast.makeText(context, "Scanned: $scannedCode", Toast.LENGTH_SHORT).show()
            onScanned(scannedCode)
        }
    }
}


object EncryptionUtils {

    // Generate a new AES SecretKey
    fun generateSecretKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(256) // Key size: 256 bits
        return keyGenerator.generateKey()
    }

    // Encrypt a message with AES
    fun encryptMessage(secretKey: SecretKey, message: String): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val iv = ByteArray(16) // Initialization vector
        val ivSpec = IvParameterSpec(iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec)

        val encryptedBytes = cipher.doFinal(message.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(
            encryptedBytes,
            Base64.DEFAULT
        ) // Encode the encrypted message in Base64
    }

    // Decrypt a message with AES
    fun decryptMessage(secretKey: SecretKey, encryptedMessage: String): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val iv = ByteArray(16) // Initialization vector
        val ivSpec = IvParameterSpec(iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)

        val decodedBytes = Base64.decode(encryptedMessage, Base64.DEFAULT)
        val decryptedBytes = cipher.doFinal(decodedBytes)
        return String(decryptedBytes, Charsets.UTF_8)
    }
}


@Preview
@Composable
fun QRCodeImagePreview() {
    QRCodeImage(data = "https://example.com", size = 150.dp)
}



