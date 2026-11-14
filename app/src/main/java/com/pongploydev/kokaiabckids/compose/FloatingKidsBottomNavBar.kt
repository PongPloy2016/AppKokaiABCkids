package com.pongploydev.kokaiabckids.compose

import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Newspaper
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Newspaper
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pongploydev.kokaiabckids.R

data class FloatingNavTabItem(
    val menuId: Int,
    @StringRes val titleRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val accentColor: Color,
    val pillBgColor: Color
)

val kidsFloatingNavItems = listOf(
    FloatingNavTabItem(
        menuId = R.id.navigation_home,
        titleRes = R.string.title_nav_home,
        selectedIcon = Icons.Rounded.Home,
        unselectedIcon = Icons.Rounded.Home,
        accentColor = Color(0xFFFF6B4A),
        pillBgColor = Color(0xFFFFECE6)
    ),
    FloatingNavTabItem(
        menuId = R.id.navigation_news,
        titleRes = R.string.title_nav_news,
        selectedIcon = Icons.Rounded.Newspaper,
        unselectedIcon = Icons.Outlined.Newspaper,
        accentColor = Color(0xFF0EA5E9),
        pillBgColor = Color(0xFFE0F2FE)
    ),
    FloatingNavTabItem(
        menuId = R.id.navigation_video,
        titleRes = R.string.title_nav_video,
        selectedIcon = Icons.Rounded.PlayCircle,
        unselectedIcon = Icons.Rounded.PlayCircle,
        accentColor = Color(0xFFEC4899),
        pillBgColor = Color(0xFFFCE7F3)
    ),
    FloatingNavTabItem(
        menuId = R.id.navigation_quiz,
        titleRes = R.string.title_nav_quiz,
        selectedIcon = Icons.Rounded.Quiz,
        unselectedIcon = Icons.Outlined.Quiz,
        accentColor = Color(0xFF10B981),
        pillBgColor = Color(0xFFD1FAE5)
    ),
    FloatingNavTabItem(
        menuId = R.id.navigation_game,
        titleRes = R.string.title_nav_game,
        selectedIcon = Icons.Rounded.SportsEsports,
        unselectedIcon = Icons.Rounded.SportsEsports,
        accentColor = Color(0xFF7C3AED),
        pillBgColor = Color(0xFFEDE9FE)
    ),
    FloatingNavTabItem(
        menuId = R.id.navigation_profile,
        titleRes = R.string.title_nav_profile,
        selectedIcon = Icons.Rounded.Person,
        unselectedIcon = Icons.Outlined.Person,
        accentColor = Color(0xFFF59E0B),
        pillBgColor = Color(0xFFFEF3C7)
    )
)

@Composable
fun FloatingKidsBottomNavBar(
    selectedIndex: Int,
    onTabSelected: (index: Int, menuId: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val capsuleShape = RoundedCornerShape(32.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = capsuleShape,
                    ambientColor = Color(0x285B42EB),
                    spotColor = Color(0x225B42EB)
                )
                .clip(capsuleShape)
                .background(Color(0xFCFFFFFF))
                .border(
                    width = 1.5.dp,
                    color = Color(0xFFEAE5FF),
                    shape = capsuleShape
                )
                .padding(horizontal = 6.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            kidsFloatingNavItems.forEachIndexed { index, item ->
                val isSelected = index == selectedIndex
                val pillColor by animateColorAsState(
                    targetValue = if (isSelected) item.pillBgColor else Color.Transparent,
                    animationSpec = tween(durationMillis = 200),
                    label = "pillColor"
                )
                val iconBgColor by animateColorAsState(
                    targetValue = if (isSelected) item.accentColor else item.pillBgColor.copy(alpha = 0.65f),
                    animationSpec = tween(durationMillis = 200),
                    label = "iconBgColor"
                )
                val iconTintColor by animateColorAsState(
                    targetValue = if (isSelected) Color.White else item.accentColor,
                    animationSpec = tween(durationMillis = 180),
                    label = "iconTintColor"
                )
                val labelColor by animateColorAsState(
                    targetValue = if (isSelected) item.accentColor else Color(0xFF5E6A7D),
                    animationSpec = tween(durationMillis = 180),
                    label = "labelColor"
                )
                val iconScale by animateFloatAsState(
                    targetValue = if (isSelected) 1.08f else 1.0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "iconScale"
                )
                val itemShape = RoundedCornerShape(24.dp)

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(itemShape)
                        .background(pillColor)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onTabSelected(index, item.menuId)
                        }
                        .padding(horizontal = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .scale(iconScale)
                            .clip(CircleShape)
                            .background(iconBgColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = stringResource(item.titleRes),
                            tint = iconTintColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(item.titleRes),
                        color = labelColor,
                        fontSize = 10.sp,
                        lineHeight = 12.sp,
                        letterSpacing = (-0.2).sp,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

