package com.example.grabclip.ui.format

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.res.stringResource
import com.example.grabclip.R
import java.util.Locale
import kotlin.math.roundToLong

private const val BYTES_PER_GIGABYTE: Long = 1_073_741_824L
private const val BYTES_PER_KILOBYTE: Long = 1_024L
private const val BYTES_PER_MEGABYTE: Long = 1_048_576L

@Immutable
data class SizeUnits(val gigabytes: String, val kilobytes: String, val megabytes: String)

@Composable
fun rememberSizeUnits(): SizeUnits = SizeUnits(gigabytes = stringResource(R.string.size_gigabytes), kilobytes = stringResource(R.string.size_kilobytes), megabytes = stringResource(R.string.size_megabytes))

object SizeFormat {
    fun format(bytes: Long, units: SizeUnits): String = when {
        bytes <= 0L -> ""
        bytes >= BYTES_PER_GIGABYTE -> String.format(Locale.US, units.gigabytes, String.format(Locale.US, "%.1f", bytes.toDouble() / BYTES_PER_GIGABYTE))
        bytes >= BYTES_PER_MEGABYTE -> String.format(Locale.US, units.megabytes, (bytes.toDouble() / BYTES_PER_MEGABYTE).roundToLong().toString())
        else -> String.format(Locale.US, units.kilobytes, (bytes.toDouble() / BYTES_PER_KILOBYTE).roundToLong().coerceAtLeast(1L).toString())
    }
}
