package ir.sepas.scanner

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import ir.sepas.scanner.ui.theme.AiScannerTheme
import java.io.File

data class ImageMetadata(
    val fileName: String,
    val fileSize: String,
    val dimensions: String,
    val dateTime: String,
    val camera: String,
    val location: String,
    val focalLength: String,
    val aperture: String,
    val shutterSpeed: String,
    val iso: String,
    val flash: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AiScannerTheme {
                ImageScannerApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageScannerApp() {
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var imageMetadata by remember { mutableStateOf<ImageMetadata?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Create temp file for camera
    val tempFile = remember {
        File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
    }

    val tempUri = remember {
        FileProvider.getUriForFile(context, "${context.packageName}.provider", tempFile)
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            selectedImageUri = tempUri
            imageMetadata = extractMetadata(context, tempUri)
        }
    }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedImageUri = it
            imageMetadata = extractMetadata(context, it)
        }
    }

    // Bottom sheet state
    val bottomSheetState = rememberModalBottomSheetState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("اسکنر تصاویر", fontWeight = FontWeight.Bold)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showBottomSheet = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "افزودن تصویر")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Image placeholder section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri != null) {
                        // Display that image is selected
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "تصویر انتخاب شده",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "هیچ تصویری انتخاب نشده",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Metadata section
            if (imageMetadata != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "مشخصات تصویر",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        MetadataList(imageMetadata!!)
                    }
                }
            }
        }

        // Bottom sheet for image selection options
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = bottomSheetState
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        "انتخاب تصویر",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Camera option
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        ListItem(
                            headlineContent = { Text("دوربین") },
                            supportingContent = { Text("گرفتن عکس جدید") },
                            leadingContent = {
                                Icon(Icons.Default.Settings, contentDescription = null)
                            }
                        )
                    }

                    Button(
                        onClick = {
                            cameraLauncher.launch(tempUri)
                            showBottomSheet = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text("انتخاب دوربین")
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    // Gallery option
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        ListItem(
                            headlineContent = { Text("گالری") },
                            supportingContent = { Text("انتخاب از حافظه داخلی") },
                            leadingContent = {
                                Icon(Icons.Default.Share, contentDescription = null)
                            }
                        )
                    }

                    Button(
                        onClick = {
                            galleryLauncher.launch("image/*")
                            showBottomSheet = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text("انتخاب از گالری")
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun MetadataList(metadata: ImageMetadata) {
    val metadataItems = listOf(
        "نام فایل" to metadata.fileName,
        "حجم فایل" to metadata.fileSize,
        "ابعاد" to metadata.dimensions,
        "تاریخ و زمان" to metadata.dateTime,
        "دوربین" to metadata.camera,
        "مکان" to metadata.location,
        "فاصله کانونی" to metadata.focalLength,
        "دیافراگم" to metadata.aperture,
        "سرعت شاتر" to metadata.shutterSpeed,
        "ISO" to metadata.iso,
        "فلاش" to metadata.flash
    ).filter { it.second.isNotEmpty() }

    LazyColumn(
        modifier = Modifier.height(200.dp)
    ) {
        items(metadataItems) { (label, value) ->
            MetadataItem(label = label, value = value)
        }
    }
}

@Composable
fun MetadataItem(label: String, value: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

fun extractMetadata(context: Context, uri: Uri): ImageMetadata {
    val contentResolver = context.contentResolver
    var fileName = ""
    var fileSize = ""
    var dimensions = ""
    var dateTime = ""
    var camera = ""
    var location = ""
    var focalLength = ""
    var aperture = ""
    var shutterSpeed = ""
    var iso = ""
    var flash = ""

    try {
        // Get basic file info
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)

                if (nameIndex != -1) {
                    fileName = cursor.getString(nameIndex) ?: ""
                }
                if (sizeIndex != -1) {
                    val size = cursor.getLong(sizeIndex)
                    fileSize = formatFileSize(size)
                }
            }
        }

        // Get image dimensions
        contentResolver.openInputStream(uri)?.use { inputStream ->
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            dimensions = "${options.outWidth} × ${options.outHeight}"
        }

        // For now, we'll set basic metadata - EXIF reading can be added later
        dateTime = "نامشخص"
        camera = "نامشخص"
        location = "نامشخص"
        focalLength = "نامشخص"
        aperture = "نامشخص"
        shutterSpeed = "نامشخص"
        iso = "نامشخص"
        flash = "نامشخص"

    } catch (e: Exception) {
        e.printStackTrace()
    }

    return ImageMetadata(
        fileName = fileName,
        fileSize = fileSize,
        dimensions = dimensions,
        dateTime = dateTime,
        camera = camera,
        location = location,
        focalLength = focalLength,
        aperture = aperture,
        shutterSpeed = shutterSpeed,
        iso = iso,
        flash = flash
    )
}

fun formatFileSize(bytes: Long): String {
    val units = arrayOf("B", "KB", "MB", "GB")
    var size = bytes.toDouble()
    var unitIndex = 0

    while (size >= 1024 && unitIndex < units.size - 1) {
        size /= 1024
        unitIndex++
    }

    return String.format("%.1f %s", size, units[unitIndex])
}

@Preview(showBackground = true)
@Composable
fun ImageScannerAppPreview() {
    AiScannerTheme {
        ImageScannerApp()
    }
}