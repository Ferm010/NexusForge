package com.ferm.nexusforge.frontend.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SkeletonLoading(
    modifier: Modifier = Modifier,
    width: Float = 1f,
    height: Float = 16f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton")
    val alpha = infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = InfiniteRepeatableSpec(
            animation = androidx.compose.animation.core.tween(
                durationMillis = 1000,
                easing = FastOutSlowInEasing
            )
        ),
        label = "alpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth(width)
            .height(height.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha.value)
            )
    )
}

@Composable
fun SkeletonProjectCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon skeleton
            SkeletonLoading(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Title skeleton
                SkeletonLoading(width = 0.8f, height = 16f)

                Spacer(modifier = Modifier.height(4.dp))

                // Description skeleton (2 lines)
                SkeletonLoading(width = 1f, height = 12f)
                SkeletonLoading(width = 0.9f, height = 12f)

                Spacer(modifier = Modifier.height(8.dp))

                // Author, downloads, version skeleton
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SkeletonLoading(width = 0.25f, height = 10f)
                    SkeletonLoading(width = 0.02f, height = 10f)
                    SkeletonLoading(width = 0.2f, height = 10f)
                    SkeletonLoading(width = 0.02f, height = 10f)
                    SkeletonLoading(width = 0.2f, height = 10f)
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Date skeleton
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SkeletonLoading(width = 0.35f, height = 10f)
                    SkeletonLoading(width = 0.02f, height = 10f)
                    SkeletonLoading(width = 0.35f, height = 10f)
                }
            }
        }
    }
}

@Composable
fun SkeletonProjectDetails(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Header image skeleton
        SkeletonLoading(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(16.dp))
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Title skeleton
        SkeletonLoading(width = 0.8f, height = 24f)

        Spacer(modifier = Modifier.height(16.dp))

        // Description skeleton
        repeat(3) {
            SkeletonLoading(width = 1f, height = 14f)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Info section skeleton
        repeat(2) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                SkeletonLoading(width = 0.3f, height = 14f)
                Spacer(modifier = Modifier.width(16.dp))
                SkeletonLoading(width = 0.5f, height = 14f)
            }
        }
    }
}

@Composable
fun SkeletonListLoading(
    count: Int = 5,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        repeat(count) {
            SkeletonProjectCard()
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

