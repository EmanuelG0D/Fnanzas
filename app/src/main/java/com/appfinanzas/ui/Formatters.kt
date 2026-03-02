package com.appfinanzas.ui

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class ThousandsSeparatorVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val inputText = text.text
        val intStr = inputText.filter { it.isDigit() }
        
        if (intStr.isEmpty()) return TransformedText(text, OffsetMapping.Identity)

        val symbols = DecimalFormatSymbols(Locale("es", "CO"))
        symbols.groupingSeparator = '.'
        val formatter = DecimalFormat("#,###", symbols)
        
        val formattedText = try {
            formatter.format(intStr.toLong())
        } catch (e: Exception) {
            intStr
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                var transformedOffset = 0
                var originalCharsCount = 0
                for (i in formattedText.indices) {
                    if (originalCharsCount == offset) break
                    if (formattedText[i].isDigit()) {
                        originalCharsCount++
                    }
                    transformedOffset++
                }
                return transformedOffset
            }

            override fun transformedToOriginal(offset: Int): Int {
                var originalOffset = 0
                var transformedCharsCount = 0
                for (i in formattedText.indices) {
                    if (transformedCharsCount == offset) break
                    if (formattedText[i].isDigit()) {
                        originalOffset++
                    }
                    transformedCharsCount++
                }
                return originalOffset
            }
        }
        return TransformedText(AnnotatedString(formattedText), offsetMapping)
    }
}
