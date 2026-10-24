package com.a2z.scan.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Merge
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.a2z.scan.model.PdfTool
import com.a2z.scan.model.PdfToolType
import com.a2z.scan.util.PdfUtils
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfToolsScreen(onBack: () -> Unit) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var isLoading by remember { mutableStateOf(false) }
    var loadingMessage by remember { mutableStateOf("") }

    var textExtractResult by remember { mutableStateOf<String?>(null) }
    var actionSuccessFile by remember { mutableStateOf<File?>(null) }
    var pendingToolAction by remember { mutableStateOf<PdfToolType?>(null) }

    val tools = listOf(
        PdfTool("Merge PDF", "Combine multiple files into one", Icons.Default.Merge, PdfToolType.MERGE),
        PdfTool("Split PDF", "Extract pages into separate files", Icons.Default.CallSplit, PdfToolType.SPLIT),
        PdfTool("Compress PDF", "Reduce file size without losing quality", Icons.Default.Compress, PdfToolType.COMPRESS),
        PdfTool("Image to PDF", "Convert JPG/PNG to PDF format", Icons.Default.Image, PdfToolType.IMAGE_TO_PDF),
        PdfTool("PDF to Text", "Extract text from PDF documents", Icons.Default.Description, PdfToolType.PDF_TO_TEXT)
    )

    // Activity Result Launchers with zero dangerous permissions
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 20)
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            isLoading = true
            loadingMessage = "Converting images to PDF..."
            scope.launch {
                val result = PdfUtils.createPdfFromUris(
                    context = context,
                    imageUris = uris,
                    documentName = "ImageScan_" + System.currentTimeMillis()
                )
                isLoading = false
                result.onSuccess { file ->
                    actionSuccessFile = file
                    snackbarHostState.showSnackbar("PDF created: ${file.name}")
                }.onFailure { error ->
                    snackbarHostState.showSnackbar("Failed: ${error.localizedMessage ?: "Unknown error"}")
                }
            }
        }
    }

    val mergePdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        if (uris.size >= 2) {
            isLoading = true
            loadingMessage = "Merging ${uris.size} PDF files..."
            scope.launch {
                val result = PdfUtils.mergePdfs(
                    context = context,
                    pdfUris = uris,
                    outputName = "Merged_" + System.currentTimeMillis()
                )
                isLoading = false
                result.onSuccess { file ->
                    actionSuccessFile = file
                    snackbarHostState.showSnackbar("Merged into ${file.name}")
                }.onFailure { err ->
                    snackbarHostState.showSnackbar("Merge failed: ${err.localizedMessage ?: "Unknown error"}")
                }
            }
        } else if (uris.size == 1) {
            Toast.makeText(context, "Please select 2 or more PDFs to merge", Toast.LENGTH_SHORT).show()
        }
    }

    val splitPdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            isLoading = true
            loadingMessage = "Splitting PDF pages..."
            scope.launch {
                val result = PdfUtils.splitPdf(
                    context = context,
                    pdfUri = uri,
                    baseName = "SplitDoc"
                )
                isLoading = false
                result.onSuccess { files ->
                    if (files.isNotEmpty()) {
                        actionSuccessFile = files.first()
                        snackbarHostState.showSnackbar("Split into ${files.size} separate documents")
                    }
                }.onFailure { err ->
                    snackbarHostState.showSnackbar("Split failed: ${err.localizedMessage ?: "Unknown error"}")
                }
            }
        }
    }

    val compressPdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            isLoading = true
            loadingMessage = "Compressing PDF document..."
            scope.launch {
                val result = PdfUtils.compressPdf(
                    context = context,
                    pdfUri = uri,
                    outputName = "Optimized_" + System.currentTimeMillis()
                )
                isLoading = false
                result.onSuccess { file ->
                    actionSuccessFile = file
                    snackbarHostState.showSnackbar("Compressed PDF saved: ${PdfUtils.formatFileSize(file.length())}")
                }.onFailure { err ->
                    snackbarHostState.showSnackbar("Compression failed: ${err.localizedMessage ?: "Unknown error"}")
                }
            }
        }
    }

    val extractTextLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            isLoading = true
            loadingMessage = "Analyzing document structure..."
            scope.launch {
                val result = PdfUtils.extractTextOrSummary(context, uri)
                isLoading = false
                result.onSuccess { text ->
                    textExtractResult = text
                }.onFailure { err ->
                    snackbarHostState.showSnackbar("Extraction failed: ${err.localizedMessage ?: "Invalid file"}")
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("PDF Tools", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack, modifier = Modifier.testTag("pdf_tools_back_btn")) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                        TextButton(onClick = onBack) { Text("Back") }
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(tools) { tool ->
                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tool_card_${tool.toolType.name}"),
                        onClick = {
                            when (tool.toolType) {
                                PdfToolType.IMAGE_TO_PDF -> {
                                    imagePickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                                PdfToolType.MERGE -> {
                                    mergePdfLauncher.launch(arrayOf("application/pdf"))
                                }
                                PdfToolType.SPLIT -> {
                                    splitPdfLauncher.launch(arrayOf("application/pdf"))
                                }
                                PdfToolType.COMPRESS -> {
                                    compressPdfLauncher.launch(arrayOf("application/pdf"))
                                }
                                PdfToolType.PDF_TO_TEXT -> {
                                    extractTextLauncher.launch(arrayOf("application/pdf"))
                                }
                            }
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = tool.icon,
                                contentDescription = tool.name,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(text = tool.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(
                                    text = tool.description,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }

            // Loading overlay
            if (isLoading) {
                Card(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = loadingMessage, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }

    // Success dialog for created/processed file
    actionSuccessFile?.let { file ->
        AlertDialog(
            onDismissRequest = { actionSuccessFile = null },
            title = { Text("File Processed Successfully", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Saved to app documents:")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(file.name, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Size: ${PdfUtils.formatFileSize(file.length())}", fontSize = 13.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        PdfUtils.shareDocument(context, file)
                        actionSuccessFile = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Share / Export")
                }
            },
            dismissButton = {
                TextButton(onClick = { actionSuccessFile = null }) {
                    Text("Done")
                }
            }
        )
    }

    // Text Extraction Dialog
    textExtractResult?.let { text ->
        AlertDialog(
            onDismissRequest = { textExtractResult = null },
            title = { Text("Extracted Document Text", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .height(280.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(text = text, fontSize = 14.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                        val clip = ClipData.newPlainText("Extracted Document Text", text)
                        clipboard?.setPrimaryClip(clip)
                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                        textExtractResult = null
                    }
                ) {
                    Text("Copy to Clipboard")
                }
            },
            dismissButton = {
                TextButton(onClick = { textExtractResult = null }) {
                    Text("Close")
                }
            }
        )
    }
}
