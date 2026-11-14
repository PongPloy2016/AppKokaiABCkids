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
    val unselectedIcon: ImageVector
)

val kidsFloatingNavItems = listOf(
    FloatingNavTabItem(
        menuId = R.id.navigation_home,
        titleRes = R.string.title_nav_home,
        selectedIcon = Icons.Rounded.Home,
        unselectedIcon = Icons.Outlined.Home
    ),
    FloatingNavTabItem(
        menuId = R.id.navigation_news,
        titleRes = R.string.title_nav_news,
        selectedIcon = Icons.Rounded.Newspaper,
        unselectedIcon = Icons.Outlined.Newspaper
    ),
    FloatingNavTabItem(
        menuId = R.id.navigation_video,
        titleRes = R.string.title_nav_video,
        selectedIcon = Icons.Rounded.PlayCircle,
        unselectedIcon = Icons.Outlined.PlayCircle
    ),
    FloatingNavTabItem(
        menuId = R.id.navigation_quiz,
        titleRes = R.string.title_nav_quiz,
        selectedIcon = Icons.Rounded.Quiz,
        unselectedIcon = Icons.Outlined.Quiz
    ),
    FloatingNavTabItem(
        menuId = R.id.navigation_game,
        titleRes = R.string.title_nav_game,
        selectedIcon = Icons.Rounded.SportsEsports,
        unselectedIcon = Icons.Outlined.SportsEsports
    ),
    FloatingNavTabItem(
        menuId = R.id.navigation_profile,
        titleRes = R.string.title_nav_profile,
        selectedIcon = Icons.Rounded.Person,
        unselectedIcon = Icons.Outlined.Person
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
                .height(64.dp)
                .shadow(
                    elevation = 14.dp,
                    shape = capsuleShape,
                    ambientColor = Color(0x331E293B),
                    spotColor = Color(0x281E293B)
                )
                .clip(capsuleShape)
                .background(Color(0xF7FFFFFF))
                .border(
                    width = 1.dp,
                    color = Color(0xFFE6ECF5),
                    shape = capsuleShape
                )
                .padding(horizontal = 6.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            kidsFloatingNavItems.forEachIndexed { index, item ->
                val isSelected = index == selectedIndex
                val pillColor by animateColorAsState(
                    targetValue = if (isSelected) Color(0xFFEDE9FE) else Color.Transparent,
                    animationSpec = tween(durationMillis = 200),
                    label = "pillColor"
                )
                val contentColor by animateColorAsState(
                    targetValue = if (isSelected) Color(0xFF5B42EB) else Color(0xFF788596),
                    animationSpec = tween(durationMillis = 180),
                    label = "contentColor"
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
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = stringResource(item.titleRes),
                        tint = contentColor,
                        modifier = Modifier
                            .size(21.dp)
                            .scale(iconScale)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(item.titleRes),
                        color = contentColor,
                        fontSize = 10.sp,
                        lineHeight = 12.sp,
                        letterSpacing = (-0.2).sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

