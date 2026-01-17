package com.pushprime.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pushprime.ui.theme.AppSpacing

/**
 * A container that adds a subtle fade-in and slide-up animation.
 * Used for premium screen transitions.
 */
@Composable
fun PremiumFadeSlideIn(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val visibleState = remember {
        MutableTransitionState(false).apply {
            targetState = true
        }
    }

    AnimatedVisibility(
        visibleState = visibleState,
        enter = fadeIn(animationSpec = androidx.compose.animation.core.tween(600)) +
                slideInVertically(
                    initialOffsetY = { 40 },
                    animationSpec = androidx.compose.animation.core.tween(600)
                ),
        modifier = modifier
    ) {
        content()
    }
}

@Composable
fun AppPrimaryButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    fullWidth: Boolean = true,
    onClick: () -> Unit
) {
    val widthModifier = if (fullWidth) modifier.fillMaxWidth() else modifier
    
    val containerColor by animateColorAsState(
        targetValue = if (enabled && !loading) MaterialTheme.colorScheme.primary 
                     else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
        label = "buttonContainerColor"
    )
    
    val contentColor by animateColorAsState(
        targetValue = if (enabled && !loading) MaterialTheme.colorScheme.onPrimary 
                     else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
        label = "buttonContentColor"
    )

    Button(
        onClick = onClick,
        modifier = widthModifier.height(56.dp),
        enabled = enabled && !loading,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor,
            disabledContentColor = contentColor
        )
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = contentColor
            )
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
fun AppSecondaryButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    fullWidth: Boolean = true,
    icon: @Composable (() -> Unit)? = null,
    onClick: () -> Unit
) {
    val widthModifier = if (fullWidth) modifier.fillMaxWidth() else modifier
    val contentColor by animateColorAsState(
        targetValue = if (enabled && !loading) MaterialTheme.colorScheme.primary 
                     else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
        label = "secondaryButtonContentColor"
    )
    
    OutlinedButton(
        onClick = onClick,
        modifier = widthModifier.height(56.dp),
        enabled = enabled && !loading,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, contentColor.copy(alpha = 0.3f)),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = contentColor,
            disabledContentColor = contentColor
        )
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = contentColor
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (icon != null) {
                    icon()
                    Spacer(modifier = Modifier.width(AppSpacing.sm))
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
fun AppTextButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(AppSpacing.lg),
    content: @Composable () -> Unit
) {
    val elevation by animateDpAsState(
        targetValue = if (onClick != null) 2.dp else 0.dp,
        label = "cardElevation"
    )

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            colors = CardDefaults.cardColors(
                containerColor = containerColor,
                contentColor = contentColor
            ),
            shape = RoundedCornerShape(18.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = elevation)
        ) {
            Box(modifier = Modifier.padding(contentPadding)) {
                content()
            }
        }
    } else {
        Card(
            modifier = modifier,
            colors = CardDefaults.cardColors(
                containerColor = containerColor,
                contentColor = contentColor
            ),
            shape = RoundedCornerShape(18.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(modifier = Modifier.padding(contentPadding)) {
                content()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppChoiceChip(
    label: String,
    selected: Boolean,
    onSelectedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                     else Color.Transparent,
        label = "chipContainerColor"
    )
    val labelColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary 
                     else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "chipLabelColor"
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary 
                     else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
        label = "chipBorderColor"
    )
    val borderWidth by animateDpAsState(
        targetValue = if (selected) 2.dp else 1.dp,
        label = "chipBorderWidth"
    )

    FilterChip(
        selected = selected,
        onClick = { onSelectedChange(!selected) },
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge
            )
        },
        shape = RoundedCornerShape(12.dp),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = Color.Transparent,
            labelColor = labelColor,
            selectedContainerColor = containerColor,
            selectedLabelColor = labelColor
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = borderColor,
            selectedBorderColor = borderColor,
            borderWidth = borderWidth,
            selectedBorderWidth = borderWidth
        ),
        modifier = modifier
    )
}

@Composable
fun AppSelectionCard(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null
) {
    val containerColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                     else MaterialTheme.colorScheme.surface,
        label = "selectionCardContainer"
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary 
                     else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
        label = "selectionCardBorder"
    )
    val borderWidth by animateDpAsState(
        targetValue = if (selected) 2.dp else 1.dp,
        label = "selectionCardBorderWidth"
    )

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(borderWidth, borderColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (selected) 2.dp else 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .padding(AppSpacing.lg)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleLarge,
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                if (description != null) {
                    Spacer(modifier = Modifier.height(AppSpacing.xs))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Box(modifier = Modifier.size(24.dp))
            }
        }
    }
}
