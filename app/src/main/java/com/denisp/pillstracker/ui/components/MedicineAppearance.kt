package com.denisp.pillstracker.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.denisp.pillstracker.R
import com.denisp.pillstracker.model.Medicine
import com.denisp.pillstracker.model.DEFAULT_MEDICINE_BACKGROUND_ARGB
import com.denisp.pillstracker.model.MedicineForm
import com.denisp.pillstracker.model.PillShape
import kotlin.math.min

@Composable
fun MedicineAppearance(
    medicine: Medicine,
    modifier: Modifier = Modifier,
    size: Dp = 28.dp,
) {
    MedicineFormSticker(
        form = medicine.form,
        shape = medicine.pillShape,
        primaryColorArgb = medicine.colorArgb,
        secondaryColorArgb = medicine.secondaryColorArgb,
        modifier = modifier,
        size = size,
        backgroundColorArgb = medicine.backgroundColorArgb,
    )
}

@Composable
fun MedicineAppearance(
    shape: PillShape,
    primaryColorArgb: Long,
    secondaryColorArgb: Long?,
    modifier: Modifier = Modifier,
    size: Dp = 28.dp,
    backgroundColorArgb: Long = DEFAULT_MEDICINE_BACKGROUND_ARGB,
) {
    MedicineFormSticker(
        form = if (shape == PillShape.CAPSULE) MedicineForm.CAPSULE else MedicineForm.TABLET,
        shape = shape,
        primaryColorArgb = primaryColorArgb,
        secondaryColorArgb = secondaryColorArgb,
        modifier = modifier,
        size = size,
        backgroundColorArgb = backgroundColorArgb,
    )
}

@Composable
fun MedicineFormSticker(
    form: MedicineForm,
    shape: PillShape = PillShape.ROUND,
    primaryColorArgb: Long,
    secondaryColorArgb: Long? = null,
    modifier: Modifier = Modifier,
    size: Dp = 52.dp,
    backgroundColorArgb: Long = DEFAULT_MEDICINE_BACKGROUND_ARGB,
) {
    val primary = Color(primaryColorArgb.toInt())
    val secondary = secondaryColorArgb?.let { Color(it.toInt()) }
    val background = Color(backgroundColorArgb.toInt())
    val supportsTwoColors = form == MedicineForm.TABLET || form == MedicineForm.CAPSULE
    val usesTwoColors = supportsTwoColors && secondary != null
    val backgroundBrush = Brush.verticalGradient(
        listOf(
            lerp(background, Color.White, 0.30f),
            background,
            lerp(background, Color.Black, 0.08f),
        ),
    )

    Box(
        modifier = modifier
            .size(size)
            .shadow(
                elevation = size * 0.08f,
                shape = CircleShape,
                ambientColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f),
                spotColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.24f),
            )
            .clip(CircleShape)
            .background(backgroundBrush)
            .border(
                width = (size * 0.025f).coerceAtLeast(0.5.dp),
                color = Color.White.copy(alpha = 0.24f),
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        MedicineFormImage(
            drawableRes = medicineFormDrawable(form, shape),
            contentDescription = form.title,
            primary = primary,
            secondary = secondary.takeIf { usesTwoColors },
            size = size * medicineIconScale(form),
        )
    }
}

@Composable
private fun MedicineFormImage(
    @DrawableRes drawableRes: Int,
    contentDescription: String,
    primary: Color,
    secondary: Color?,
    size: Dp,
) {
    val painter = painterResource(drawableRes)
    val primaryFilter = ColorFilter.tint(primary, BlendMode.Modulate)
    val secondaryFilter = secondary?.let { ColorFilter.tint(it, BlendMode.Modulate) }
    val intrinsicSize = painter.intrinsicSize

    Canvas(
        modifier = Modifier
            .size(size)
            .semantics { this.contentDescription = contentDescription },
    ) {
        val sourceWidth = intrinsicSize.width.takeIf { it.isFinite() && it > 0f } ?: this.size.width
        val sourceHeight = intrinsicSize.height.takeIf { it.isFinite() && it > 0f } ?: this.size.height
        val scale = min(this.size.width / sourceWidth, this.size.height / sourceHeight)
        val renderedSize = Size(sourceWidth * scale, sourceHeight * scale)
        val offset = Offset(
            x = (this.size.width - renderedSize.width) / 2f,
            y = (this.size.height - renderedSize.height) / 2f,
        )

        translate(left = offset.x, top = offset.y) {
            with(painter) {
                if (secondaryFilter == null) {
                    draw(size = renderedSize, colorFilter = primaryFilter)
                } else {
                    clipRect(
                        left = 0f,
                        top = 0f,
                        right = renderedSize.width / 2f,
                        bottom = renderedSize.height,
                    ) {
                        draw(size = renderedSize, colorFilter = primaryFilter)
                    }
                    clipRect(
                        left = renderedSize.width / 2f,
                        top = 0f,
                        right = renderedSize.width,
                        bottom = renderedSize.height,
                    ) {
                        draw(size = renderedSize, colorFilter = secondaryFilter)
                    }
                }
            }
        }
    }
}

@DrawableRes
private fun medicineFormDrawable(
    form: MedicineForm,
    shape: PillShape,
): Int = when (form) {
    MedicineForm.TABLET -> when (shape) {
        PillShape.ROUND -> R.drawable.medicine_form_tablet_scored
        PillShape.OVAL -> R.drawable.medicine_form_tablet_oval_scored
        PillShape.CAPSULE -> R.drawable.medicine_form_capsule
        PillShape.OBLONG -> R.drawable.medicine_form_tablet_oblong_scored
    }

    MedicineForm.CAPSULE -> R.drawable.medicine_form_capsule
    MedicineForm.POWDER -> R.drawable.medicine_form_powder
    MedicineForm.INJECTION -> R.drawable.medicine_form_injection
    MedicineForm.DROPS -> R.drawable.medicine_form_drops
    MedicineForm.SYRUP -> R.drawable.medicine_form_syrup
    MedicineForm.SPRAY -> R.drawable.medicine_form_spray
    MedicineForm.OINTMENT -> R.drawable.medicine_form_ointment
    MedicineForm.SUPPOSITORY -> R.drawable.medicine_form_suppository
    MedicineForm.OTHER -> R.drawable.medicine_form_other
}

private fun medicineIconScale(form: MedicineForm): Float = when (form) {
    MedicineForm.INJECTION,
    MedicineForm.OINTMENT,
    -> 0.70f

    MedicineForm.TABLET,
    MedicineForm.CAPSULE,
    -> 0.62f

    else -> 0.66f
}
