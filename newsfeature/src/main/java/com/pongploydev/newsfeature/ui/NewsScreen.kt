package com.pongploydev.newsfeature.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.pongploydev.newsfeature.data.FeedState
import com.pongploydev.newsfeature.data.NewsArticle
import com.pongploydev.newsfeature.data.NewsFeedItem
import kotlinx.coroutines.delay

private const val HERO_COUNT = 5
private const val HERO_AUTO_SCROLL_MS = 5000L

@Composable
fun NewsScreen(
    feeds: List<NewsFeedItem>,
    selectedFeed: NewsFeedItem,
    state: FeedState,
    onSelectFeed: (NewsFeedItem) -> Unit,
    onRefresh: () -> Unit,
    onArticleClick: (NewsArticle) -> Unit
) {
    var selectedCategory by rememberSaveable(selectedFeed.id) { mutableStateOf<String?>(null) }

    val categories = remember(state.items) {
        state.items
            .map { it.category }
            .filter { it.isNotBlank() }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .map { it.key }
    }
    val activeCategory = selectedCategory?.takeIf { it in categories }
    val filtered = remember(state.items, activeCategory) {
        if (activeCategory == null) state.items else state.items.filter { it.category == activeCategory }
    }
    val heroItems = filtered.take(HERO_COUNT)
    val restItems = filtered.drop(HERO_COUNT)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NewsColors.Background)
            .statusBarsPadding()
    ) {
        NewsHeader(loading = state.loading, onRefresh = onRefresh)

        if (state.loading && state.items.isNotEmpty()) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp),
                color = NewsColors.Accent,
                trackColor = Color.Transparent
            )
        } else {
            Spacer(Modifier.height(2.dp))
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            item(key = "feed_tabs") {
                FeedTabs(
                    feeds = feeds,
                    selected = selectedFeed,
                    onSelect = onSelectFeed
                )
            }

            if (categories.size > 1) {
                item(key = "category_chips") {
                    CategoryChips(
                        categories = categories,
                        selected = activeCategory,
                        onSelect = { selectedCategory = it }
                    )
                }
            }

            when {
                state.items.isEmpty() && state.loading -> item(key = "loading") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp)
                            .padding(top = 80.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        CircularProgressIndicator(color = NewsColors.Accent)
                    }
                }

                state.items.isEmpty() -> item(key = "error") {
                    ErrorPanel(
                        message = state.error ?: "กำลังเตรียมนิทานและสาระสนุกๆ ให้เด็กๆ 🎈",
                        onRetry = onRefresh
                    )
                }

                else -> {
                    if (state.error != null) {
                        item(key = "error_banner") { ErrorBanner(state.error, onRefresh) }
                    }

                    if (heroItems.isNotEmpty()) {
                        item(key = "hero_${selectedFeed.id}_$activeCategory") {
                            HeroCarousel(items = heroItems, onClick = onArticleClick)
                        }
                    }

                    if (restItems.isNotEmpty()) {
                        item(key = "section_title") {
                            val sectionLabel = when (selectedFeed.category) {
                                "podcast" -> "ตอนทั้งหมด 🎧"
                                "video" -> "วิดีโอทั้งหมด 🎬"
                                else -> "เรื่องน่ารู้ทั้งหมด"
                            }
                            SectionTitle(sectionLabel)
                        }
                        items(restItems, key = { it.id }) { article ->
                            NewsListItem(article = article, onClick = { onArticleClick(article) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NewsHeader(loading: Boolean, onRefresh: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(NewsColors.Accent.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Newspaper,
                contentDescription = null,
                tint = NewsColors.Accent
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "นิทานและการเรียนรู้เด็ก 🌟",
                color = NewsColors.TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "นิทานเสียง · ศิลปะ · สัตว์น่ารัก · วิทย์แสนสนุก",
                color = NewsColors.TextMuted,
                fontSize = 12.sp
            )
        }
        IconButton(onClick = onRefresh, enabled = !loading) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "รีเฟรช",
                tint = if (loading) NewsColors.TextMuted else NewsColors.TextPrimary
            )
        }
    }
}

@Composable
private fun FeedTabs(
    feeds: List<NewsFeedItem>,
    selected: NewsFeedItem,
    onSelect: (NewsFeedItem) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(feeds, key = { it.id }) { feed ->
            val isSelected = feed.id == selected.id
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isSelected) NewsColors.Accent else NewsColors.Surface)
                    .clickable { onSelect(feed) }
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = feed.displayLabel,
                    color = if (isSelected) Color.White else NewsColors.TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun CategoryChips(
    categories: List<String>,
    selected: String?,
    onSelect: (String?) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item(key = "all") {
            SmallChip(text = "ทั้งหมด (All)", selected = selected == null) { onSelect(null) }
        }
        items(categories, key = { it }) { category ->
            SmallChip(text = category, selected = selected == category) { onSelect(category) }
        }
    }
}

@Composable
private fun SmallChip(text: String, selected: Boolean, onClick: () -> Unit) {
    val shape = CircleShape
    Box(
        modifier = Modifier
            .clip(shape)
            .background(if (selected) NewsColors.Accent else Color.Transparent)
            .border(
                BorderStroke(1.dp, if (selected) NewsColors.Accent else NewsColors.Outline),
                shape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) Color.White else NewsColors.TextPrimary,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        color = NewsColors.TextPrimary,
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 8.dp)
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HeroCarousel(items: List<NewsArticle>, onClick: (NewsArticle) -> Unit) {
    key(items.map { it.id }) {
        val pagerState = rememberPagerState { items.size }

        LaunchedEffect(pagerState, items.size) {
            if (items.size < 2) return@LaunchedEffect
            while (true) {
                delay(HERO_AUTO_SCROLL_MS)
                if (!pagerState.isScrollInProgress) {
                    pagerState.animateScrollToPage((pagerState.currentPage + 1) % items.size)
                }
            }
        }

        Column(modifier = Modifier.padding(top = 10.dp)) {
            HorizontalPager(
                state = pagerState,
                contentPadding = PaddingValues(horizontal = 16.dp),
                pageSpacing = 12.dp
            ) { page ->
                HeroCard(
                    article = items[page],
                    positionLabel = "${page + 1}/${items.size}",
                    onClick = { onClick(items[page]) }
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(items.size) { index ->
                    val active = index == pagerState.currentPage
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .height(6.dp)
                            .width(if (active) 20.dp else 6.dp)
                            .clip(CircleShape)
                            .background(if (active) NewsColors.Accent else NewsColors.Outline)
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroCard(article: NewsArticle, positionLabel: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.35f)
            .clip(RoundedCornerShape(22.dp))
            .background(NewsColors.SurfaceHigh)
            .clickable(onClick = onClick)
    ) {
        NewsImage(
            url = article.imageUrl,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.35f to Color.Transparent,
                        1f to Color(0xF0050A16)
                    )
                )
        )
        if (article.category.isNotBlank() || article.audioUrl != null || article.videoUrl != null) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (article.category.isNotBlank()) {
                    CategoryBadge(category = article.category)
                }
                if (article.audioUrl != null) {
                    AudioBadge()
                }
                if (article.videoUrl != null) {
                    VideoBadge()
                }
            }
        }
        Text(
            text = positionLabel,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(14.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0x99000000))
                .padding(horizontal = 10.dp, vertical = 5.dp)
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Text(
                text = article.title,
                color = Color.White,
                fontSize = 18.sp,
                lineHeight = 26.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            val time = formatRelativeTime(article.publishedAtMillis)
            if (time.isNotEmpty()) {
                val label = if (article.category.isNotBlank()) "${article.category} · " else ""
                Text(
                    text = "$label$time",
                    color = Color(0xFFCBD5E1),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun NewsListItem(article: NewsArticle, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(NewsColors.Surface)
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        NewsImage(
            url = article.imageUrl,
            modifier = Modifier
                .size(width = 112.dp, height = 92.dp)
                .clip(RoundedCornerShape(12.dp))
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            if (article.category.isNotBlank() || article.audioUrl != null || article.videoUrl != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (article.category.isNotBlank()) {
                        CategoryBadge(category = article.category, small = true)
                    }
                    if (article.audioUrl != null) {
                        AudioBadge(small = true)
                    }
                    if (article.videoUrl != null) {
                        VideoBadge(small = true)
                    }
                }
                Spacer(Modifier.height(6.dp))
            }
            Text(
                text = article.title,
                color = NewsColors.TextPrimary,
                fontSize = 14.sp,
                lineHeight = 21.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            val time = formatRelativeTime(article.publishedAtMillis)
            if (time.isNotEmpty()) {
                Text(
                    text = time,
                    color = NewsColors.TextMuted,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun CategoryBadge(category: String, modifier: Modifier = Modifier, small: Boolean = false) {
    Text(
        text = category,
        color = Color.White,
        fontSize = if (small) 10.sp else 12.sp,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
            .clip(RoundedCornerShape(if (small) 8.dp else 10.dp))
            .background(NewsColors.badgeColor(category))
            .padding(horizontal = if (small) 8.dp else 12.dp, vertical = if (small) 3.dp else 5.dp)
    )
}

@Composable
fun AudioBadge(modifier: Modifier = Modifier, small: Boolean = false) {
    Text(
        text = "🎧 เสียงนิทาน",
        color = Color.White,
        fontSize = if (small) 10.sp else 12.sp,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        modifier = modifier
            .clip(RoundedCornerShape(if (small) 8.dp else 10.dp))
            .background(Color(0xFF8B5CF6))
            .padding(horizontal = if (small) 8.dp else 12.dp, vertical = if (small) 3.dp else 5.dp)
    )
}

@Composable
fun VideoBadge(modifier: Modifier = Modifier, small: Boolean = false) {
    Text(
        text = "🎬 วิดีโอ",
        color = Color.White,
        fontSize = if (small) 10.sp else 12.sp,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        modifier = modifier
            .clip(RoundedCornerShape(if (small) 8.dp else 10.dp))
            .background(Color(0xFFEF4444))
            .padding(horizontal = if (small) 8.dp else 12.dp, vertical = if (small) 3.dp else 5.dp)
    )
}

@Composable
fun NewsImage(url: String?, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(NewsColors.SurfaceHigh),
        contentAlignment = Alignment.Center
    ) {
        if (url.isNullOrBlank()) {
            Icon(
                imageVector = Icons.Default.Newspaper,
                contentDescription = null,
                tint = NewsColors.Outline,
                modifier = Modifier.size(36.dp)
            )
        } else {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(url)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun ErrorPanel(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.WifiOff,
            contentDescription = null,
            tint = NewsColors.TextMuted,
            modifier = Modifier.size(56.dp)
        )
        Text(
            text = message,
            color = NewsColors.TextMuted,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp)
        )
        Button(
            onClick = onRetry,
            modifier = Modifier.padding(top = 20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NewsColors.Accent)
        ) {
            Text("ลองอีกครั้ง", color = Color.White)
        }
    }
}

@Composable
private fun ErrorBanner(message: String, onRetry: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(NewsColors.Error.copy(alpha = 0.15f))
            .clickable(onClick = onRetry)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = message,
            color = NewsColors.Error,
            fontSize = 12.sp,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "ลองใหม่",
            color = NewsColors.Error,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}
