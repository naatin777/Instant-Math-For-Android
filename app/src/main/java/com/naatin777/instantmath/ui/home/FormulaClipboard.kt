package com.naatin777.instantmath.ui.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.view.View
import androidx.core.content.FileProvider
import com.naatin777.instantmath.R
import java.io.File
import java.io.FileOutputStream
import androidx.core.graphics.createBitmap
import androidx.core.graphics.withTranslation

object FormulaClipboard {

    fun copyText(context: Context, latex: String): Boolean {
        if (latex.isBlank()) return false
        val clipboard = context.getSystemService(ClipboardManager::class.java) ?: return false
        clipboard.setPrimaryClip(ClipData.newPlainText("formula", latex))
        return true
    }

    fun copyImage(context: Context, mathView: View): Boolean {
        if (mathView.width <= 0 || mathView.height <= 0) return false

        val padding = context.resources.getDimensionPixelSize(R.dimen.formula_clipboard_padding)
        val bitmap = createBitmap(mathView.width + padding * 2, mathView.height + padding * 2)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        canvas.withTranslation(padding.toFloat(), padding.toFloat()) {
            mathView.draw(this)
        }

        val cacheDir = File(context.cacheDir, "clipboard").apply { mkdirs() }
        val file = File(cacheDir, "formula.png")
        FileOutputStream(file).use { output ->
            if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)) {
                bitmap.recycle()
                return false
            }
        }
        bitmap.recycle()

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
        val clipboard = context.getSystemService(ClipboardManager::class.java) ?: return false
        clipboard.setPrimaryClip(
            ClipData.newUri(context.contentResolver, "formula", uri),
        )
        return true
    }
}
