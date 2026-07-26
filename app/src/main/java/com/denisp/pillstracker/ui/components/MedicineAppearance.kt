package com.denisp.pillstracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.denisp.pillstracker.model.Medicine
import com.denisp.pillstracker.model.PillShape
import com.denisp.pillstracker.ui.toComposeColor

@Composable
fun MedicineAppearance(
    medicine: Medicine,
    modifier: Modifier = Modifier,
    size: Dp = 28.dp,
) {
    MedicineAppearance(
        shape = medicine.pillShape,
        primaryColorArgb = medicine.colorArgb,
        secondaryColorArgb = medicine.secondaryColorArgb,
        modifier = modifier,
        size = size,
    )
}

@Composable
fun MedicineAppearance(
    shape: PillShape,
    primaryColorArgb: Long,
    secondaryColorArgb: Long?,
    modifier: Modifier = Modifier,
    size: Dp = 28.dp,
) {
    val dimensions = when (shape) {
        PillShape.ROUND -> size to size
        PillShape.OVAL -> size * 1.35f to size * 0.82f
        PillShape.CAPSULE -> size * 1.55f to size * 0.72f
        PillShape.OBLONG -> size * 1.45f to size * 0.68f
    }
    val outline: Shape = when (shape) {
        PillShape.ROUND -> CircleShape
        PillShape.OVAL, PillShape.CAPSULE -> RoundedCornerShape(50)
        PillShape.OBLONG -> RoundedCornerShape(7.dp)
    }
    val secondary = secondaryColorArgb ?: primaryColorArgb

    Row(
        modifier = modifier
            .width(dimensions.first)
            .height(dimensions.second)
            .clip(outline)
            .border(1.dp, 0x33000000L.toComposeColor(), outline),
    ) {
        Box(
            Modifier
                .weight(1f)
                .fillMaxSize()
                .background(primaryColorArgb.toComposeColor()),
        )
        Box(
            Modifier
                .weight(1f)
                .fillMaxSize()
                .background(secondary.toComposeColor()),
        )
    }
}
