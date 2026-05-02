package com.example.demeter_pgc

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.util.concurrent.Executors

// Color principal DEMETER
private val DemeterGreen = Color(0xFF0C7211)

// Emojis por clase
private val animalEmojis = mapOf(
    "caballo" to "🐴",
    "gallina" to "🐔",
    "gato"    to "🐱",
    "oveja"   to "🐑",
    "perro"   to "🐶",
    "vaca"    to "🐄"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimalScreen(onClickBack: () -> Unit = {}) {

    val context       = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Estados
    var resultText     by remember { mutableStateOf("Apunta la cámara a un animal") }
    var confidenceText by remember { mutableStateOf("") }
    var isRealTime     by remember { mutableStateOf(false) }
    var isLoading      by remember { mutableStateOf(false) }
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED
        )
    }

    val classifier = remember { AnimalClassifier(context) }
    DisposableEffect(Unit) { onDispose { classifier.close() } }

    // ── Launchers ──────────────────────────────────────────────────────────────
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            isLoading = true
            val stream = context.contentResolver.openInputStream(it)
            val bitmap = BitmapFactory.decodeStream(stream)
            val results = classifier.classify(bitmap)
            updateResults(results) { r, c -> resultText = r; confidenceText = c }
            isLoading = false
        }
    }

    val cameraCaptureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let {
            isLoading = true
            val results = classifier.classify(it)
            updateResults(results) { r, c -> resultText = r; confidenceText = c }
            isLoading = false
        }
    }

    // ── UI ─────────────────────────────────────────────────────────────────────
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🐾 Clasificador de Animales", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onClickBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor    = DemeterGreen,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF5F5F5)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ── Visor: cámara en tiempo real o placeholder ──────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                if (isRealTime && hasCameraPermission) {
                    CameraPreview(
                        classifier     = classifier,
                        onResult       = { r, c -> resultText = r; confidenceText = c }
                    )
                } else {
                    Text(
                        text  = "📷",
                        fontSize = 80.sp
                    )
                }
                if (isLoading) {
                    CircularProgressIndicator(color = DemeterGreen)
                }
            }

            // ── Tarjeta de resultado ────────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape     = RoundedCornerShape(16.dp),
                colors    = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text       = resultText,
                        fontSize   = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Color(0xFF1A1A1A)
                    )
                    if (confidenceText.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text     = confidenceText,
                            fontSize = 14.sp,
                            color    = Color.Gray
                        )
                    }
                }
            }

            // ── Botones ─────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        isRealTime = false
                        if (hasCameraPermission) cameraCaptureLauncher.launch(null)
                        else permissionLauncher.launch(Manifest.permission.CAMERA)
                    },
                    modifier = Modifier.weight(1f).height(52.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = DemeterGreen)
                ) { Text("📷 Cámara") }

                Button(
                    onClick  = { isRealTime = false; galleryLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f).height(52.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
                ) { Text("🖼 Galería") }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
                    else isRealTime = !isRealTime
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRealTime) Color(0xFFD32F2F) else Color(0xFFE65100)
                )
            ) {
                Text(if (isRealTime) "⏹ Detener Tiempo Real" else "▶ Tiempo Real")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ── CameraX en tiempo real ─────────────────────────────────────────────────────
@Composable
private fun CameraPreview(
    classifier: AnimalClassifier,
    onResult: (String, String) -> Unit
) {
    val context        = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                val imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build().also { analysis ->
                        analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                            val bitmap = imageProxy.toBitmap()
                            val results = classifier.classify(bitmap)
                            updateResults(results) { r, c ->
                                // Callback al hilo principal
                                android.os.Handler(android.os.Looper.getMainLooper()).post {
                                    onResult(r, c)
                                }
                            }
                            imageProxy.close()
                        }
                    }
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageAnalyzer
                )
            }, ContextCompat.getMainExecutor(ctx))
            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}

// ── Helper para formatear resultados ──────────────────────────────────────────
private fun updateResults(
    results: List<AnimalClassifier.Recognition>,
    onUpdate: (String, String) -> Unit
) {
    if (results.isEmpty()) {
        onUpdate("❓ No reconocido", "Confianza insuficiente")
        return
    }
    val top   = results.first()
    val emoji = animalEmojis[top.label] ?: "🐾"
    val conf  = if (results.size > 1)
        results.joinToString("  |  ") { "${animalEmojis[it.label] ?: ""} ${it.label}: ${it.confidencePercent}" }
    else top.confidencePercent

    onUpdate("$emoji ${top.label.uppercase()}", conf)
}
