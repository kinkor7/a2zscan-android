package com.a2z.scan.model

import androidx.compose.ui.graphics.vector.ImageVector
import java.io.File

/**
 * Data model for dashboard navigation tiles.
 */
data class ToolItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val route: String
)

/**
 * Tool types available in the PDF Tools screen.
 */
enum class PdfToolType {
    MERGE,
    SPLIT,
    COMPRESS,
    IMAGE_TO_PDF,
    PDF_TO_TEXT
}

/**
 * Data model for PDF tool tiles.
 */
data class PdfTool(
    val name: String,
    val description: String,
    val icon: ImageVector,
    val toolType: PdfToolType
)

/**
 * Represents a saved scanned PDF document.
 */
data class ScannedDocument(
    val file: File,
    val name: String,
    val sizeBytes: Long,
    val lastModified: Long,
    val pageCount: Int = 1
)

/**
 * Post-processing filters for document scans.
 */
enum class DocumentFilter(val displayName: String) {
    ORIGINAL("Original"),
    ENHANCED("Enhanced"),
    GRAYSCALE("Grayscale"),
    BLACK_WHITE("B&W Clean")
}
