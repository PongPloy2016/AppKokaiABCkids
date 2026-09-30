package com.pongploydev.profilefeature.compose

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.android.billingclient.api.BillingClient
import com.pongploydev.profilefeature.billing.PlayBillingManager
import com.pongploydev.profilefeature.data.SupabaseDataManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Subscription Plan Data Model matching Google Play Billing Subscriptions
 */
data class SubscriptionPlan(
    val id: String,
    val productId: String,
    val title: String,
    val price: String,
    val durationText: String,
    val badge: String? = null,
    val bonusStars: Int = 0,
    val isRecommended: Boolean = false,
    val isLifetime: Boolean = false,
    val description: String
)

/**
 * Extension helper to find host Activity from Context
 */
fun Context.findActivity(): Activity? {
    var current = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}

@Composable
fun SubscriptionDialog(
    onDismiss: () -> Unit,
    onSubscribed: ((SubscriptionPlan) -> Unit)? = null
) {
    val context = LocalContext.current
    val dataManager = remember { SupabaseDataManager.getInstance(context) }
    val billingManager = remember { PlayBillingManager.getInstance(context) }

    // Google Play Billing States
    val isBillingConnected by billingManager.isConnected.collectAsState()
    val productDetailsMap by billingManager.productDetailsMap.collectAsState()

    val plans = remember {
        listOf(
            SubscriptionPlan(
                id = "monthly",
                productId = PlayBillingManager.PRODUCT_SUB_MONTHLY,
                title = "ผู้สนับสนุนตัวน้อย",
                price = "฿29",
                durationText = "/ เดือน",
                badge = "เริ่มต้น",
                bonusStars = 20,
                description = "ทดลองสนับสนุนแอปการเรียนรู้ ปิดโฆษณารายเดือน"
            ),
            SubscriptionPlan(
                id = "yearly",
                productId = PlayBillingManager.PRODUCT_SUB_YEARLY,
                title = "สมาชิก VIP นักอ่าน",
                price = "฿199",
                durationText = "/ ปี",
                badge = "ยอดนิยม คุ้มค่า 🔥",
                bonusStars = 100,
                isRecommended = true,
                description = "เฉลี่ยเพียง ฿16/เดือน • ปิดโฆษณาตลอดปี + โบนัส 100 ดาว ⭐"
            ),
            SubscriptionPlan(
                id = "lifetime",
                productId = PlayBillingManager.PRODUCT_LIFETIME,
                title = "ครอบครัวซูเปอร์คิดส์",
                price = "฿399",
                durationText = "ครั้งเดียวจบ",
                badge = "VIP ตลอดชีพ 👑",
                bonusStars = 300,
                isLifetime = true,
                description = "จ่ายครั้งเดียวจบ VIP ถาวรตลอดชีพ + โบนัส 300 ดาว ⭐⭐⭐"
            )
        )
    }

    var selectedPlanId by remember { mutableStateOf("yearly") }
    var isSubscribing by remember { mutableStateOf(false) }
    var isRestoring by remember { mutableStateOf(false) }
    var isCurrentlyVip by remember { mutableStateOf(dataManager.isVipSubscriber()) }
    var currentTier by remember { mutableStateOf(dataManager.getSubscriptionTier()) }
    var showTestFallbackDialog by remember { mutableStateOf(false) }
    var fallbackDebugReason by remember { mutableStateOf("") }

    val selectedPlan = plans.firstOrNull { it.id == selectedPlanId } ?: plans[1]

    Dialog(
        onDismissRequest = { if (!isSubscribing) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f)
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFFAFAFE))
            ) {
                // Header Bar with Google Play Badge
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF5B3CC4), Color(0xFF7E30E1), Color(0xFFF39C12))
                            )
                        )
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 36.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "👑", fontSize = 26.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "สนับสนุน สมาชิก VIP",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "Security",
                                tint = Color(0xFF7BED9F),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Google Play Subscriptions • ปลอดภัย 100%",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }

                    // Close Button
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(32.dp)
                            .background(Color.White.copy(alpha = 0.25f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "ปิด",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Google Play Status Indicator
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isBillingConnected) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                        border = BorderStroke(
                            1.dp,
                            if (isBillingConnected) Color(0xFFA5D6A7) else Color(0xFFFFCC80)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isBillingConnected) Icons.Default.CheckCircle else Icons.Default.Refresh,
                                    contentDescription = null,
                                    tint = if (isBillingConnected) Color(0xFF2E7D32) else Color(0xFFE65100),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                val statusText = when {
                                    productDetailsMap.isNotEmpty() -> "Google Play Billing พร้อมใช้งาน (${productDetailsMap.size} รายการ)"
                                    isBillingConnected -> "เชื่อมต่อ Google Play Billing สำเร็จ"
                                    else -> "กำลังเชื่อมต่อ Google Play..."
                                }
                                Text(
                                    text = statusText,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isBillingConnected) Color(0xFF1B5E20) else Color(0xFFE65100)
                                )
                            }

                            // Restore Purchases Button
                            TextButton(
                                onClick = {
                                    if (isRestoring) return@TextButton
                                    isRestoring = true
                                    billingManager.queryPurchases { anyFound ->
                                        isRestoring = false
                                        if (anyFound) {
                                            isCurrentlyVip = dataManager.isVipSubscriber()
                                            currentTier = dataManager.getSubscriptionTier()
                                            Toast.makeText(context, "🎉 กู้คืนการสมัครสำเร็จ! ข้อมูล VIP ได้รับการอัปเดตแล้ว", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "ไม่พบประวัติการสมัครในบัญชี Google Play นี้", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (isRestoring) "กำลังตรวจ..." else "กู้คืนการซื้อ ↺",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4A148C)
                                )
                            }
                        }
                    }

                    // Current Membership Status Pill
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isCurrentlyVip) Color(0xFFE8F5E9) else Color(0xFFF3E5F5),
                        border = BorderStroke(1.dp, if (isCurrentlyVip) Color(0xFF81C784) else Color(0xFFCE93D8))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = if (isCurrentlyVip) "👑" else "✨", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "สถานะสมาชิกของคุณ",
                                        fontSize = 11.sp,
                                        color = Color(0xFF666666)
                                    )
                                    Text(
                                        text = if (isCurrentlyVip) "VIP Member ($currentTier)" else "สมาชิกทั่วไป (Free User)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCurrentlyVip) Color(0xFF2E7D32) else Color(0xFF6A1B9A)
                                    )
                                }
                            }
                            if (isCurrentlyVip) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFF2E7D32)
                                    ) {
                                        Text(
                                            text = "เปิดใช้งานอยู่ ✅",
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    TextButton(
                                        onClick = {
                                            val activity = context.findActivity()
                                            if (activity != null) {
                                                billingManager.openManageSubscriptions(activity)
                                            }
                                        },
                                        contentPadding = PaddingValues(horizontal = 4.dp)
                                    ) {
                                        Text(
                                            text = "จัดการ ⚙️",
                                            fontSize = 10.5.sp,
                                            color = Color(0xFF1B5E20),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // VIP Perks Card
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "สิทธิพิเศษสำหรับสมาชิกผู้สนับสนุน 🌟",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2D3436)
                            )

                            VipPerkItem(icon = "🚫", title = "ปิดโฆษณาทั้งหมด 100%", desc = "เรียนรู้ต่อเนื่อง เพลิดเพลิน ปลอดภัยสำหรับเด็ก")
                            VipPerkItem(icon = "⭐", title = "รับดาวสะสมโบนัส x2", desc = "เล่นเกมและทำควิซได้รับดาวไวขึ้น 2 เท่า")
                            VipPerkItem(icon = "🎮", title = "ปลดล็อกครบทุกบทเรียนและ 10 เกม", desc = "เข้าถึงสื่อการเรียนรู้ใหม่ได้ไม่จำกัด")
                            VipPerkItem(icon = "👑", title = "ป้ายสัญลักษณ์มงกุฎ VIP", desc = "แสดงสถานะเกียรติยศบนหน้าโปรไฟล์และผู้นำ")
                        }
                    }

                    Text(
                        text = "เลือกแพ็กเกจสมัครสมาชิก (Google Play Subscriptions) 💎",
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2D3436)
                    )

                    // Subscription Plan Cards
                    plans.forEach { plan ->
                        val isSelected = plan.id == selectedPlanId
                        // Live Google Play price or fallback
                        val displayPrice = billingManager.getFormattedPrice(plan.productId, plan.price)

                        PlanSelectionCard(
                            plan = plan,
                            displayPrice = displayPrice,
                            isSelected = isSelected,
                            onClick = { selectedPlanId = plan.id }
                        )
                    }

                    // Google Play Subscription Terms & Info
                    Text(
                        text = "ℹ️ การสมัครรับข้อมูลจะต่ออายุโดยอัตโนมัติผ่าน Google Play จนกว่าจะยกเลิก สามารถยกเลิกหรือเปลี่ยนแปลงแผนได้ตลอดเวลาในการตั้งค่าการสมัครรับข้อมูลของ Google Play Store",
                        fontSize = 10.5.sp,
                        color = Color(0xFF757575),
                        textAlign = TextAlign.Center,
                        lineHeight = 15.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Bottom Action Button Bar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp)
                    ) {
                        val displayPrice = billingManager.getFormattedPrice(selectedPlan.productId, selectedPlan.price)

                        Button(
                            onClick = {
                                if (isSubscribing) return@Button
                                val activity = context.findActivity()
                                if (activity == null) {
                                    Toast.makeText(context, "ไม่พบบริบทหน้าจอหลัก", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }

                                isSubscribing = true
                                // Launch official Google Play Billing Flow
                                billingManager.launchBillingFlow(activity, selectedPlan.productId) { result ->
                                    isSubscribing = false
                                    when (result.responseCode) {
                                        BillingClient.BillingResponseCode.OK -> {
                                            // Flow launched successfully in Google Play Bottom Sheet
                                        }
                                        BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                                            Toast.makeText(context, "คุณได้สมัครแพ็กเกจนี้ไว้แล้ว", Toast.LENGTH_LONG).show()
                                            isCurrentlyVip = true
                                            currentTier = selectedPlan.title
                                        }
                                        BillingClient.BillingResponseCode.USER_CANCELED -> {
                                            // User canceled, nothing to do
                                        }
                                        else -> {
                                            // E.g. ITEM_UNAVAILABLE in debug/sideload build or not yet published in Play Console
                                            fallbackDebugReason = result.debugMessage.ifBlank { "รหัสข้อผิดพลาด: ${result.responseCode}" }
                                            showTestFallbackDialog = true
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(26.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedPlan.isLifetime) Color(0xFFFF9F43) else Color(0xFF6C5CE7)
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            if (isSubscribing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    color = Color.White,
                                    strokeWidth = 2.5.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("กำลังเปิดระบบ Google Play...", color = Color.White, fontWeight = FontWeight.Bold)
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("💎", fontSize = 18.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "สมัครสมาชิก $displayPrice (${selectedPlan.title})",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Fallback/Sandbox Test Dialog for Development & Testing
    if (showTestFallbackDialog) {
        AlertDialog(
            onDismissRequest = { showTestFallbackDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🛠️", fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Google Play Sandbox / Test", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "ระบบส่งคำขอ Google Play สำเร็จ แต่ Google Play Console ส่งสถานะ:",
                        fontSize = 13.sp,
                        color = Color(0xFF333333)
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF1F2F6),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Product: ${selectedPlan.productId}\n$fallbackDebugReason",
                            fontSize = 11.5.sp,
                            color = Color(0xFF57606F),
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                    Text(
                        text = "💡 สำหรับการทดสอบในแอปเวอร์ชันนี้ คุณต้องการเปิดใช้งานสิทธิ VIP ทันทีในโหมดทดสอบ (Test Mode) หรือไม่?",
                        fontSize = 12.sp,
                        color = Color(0xFF2C3E50)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showTestFallbackDialog = false
                        dataManager.setSubscription(
                            tierName = selectedPlan.title,
                            isLifetime = selectedPlan.isLifetime,
                            bonusStars = selectedPlan.bonusStars
                        )
                        isCurrentlyVip = true
                        currentTier = selectedPlan.title
                        Toast.makeText(
                            context,
                            "🎉 เปิดใช้งานสมาชิก ${selectedPlan.title} (โหมดทดสอบ) สำเร็จ! +${selectedPlan.bonusStars} ดาว ⭐",
                            Toast.LENGTH_LONG
                        ).show()
                        onSubscribed?.invoke(selectedPlan)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2ED573))
                ) {
                    Text("เปิดใช้งานทดสอบ (Test Mode)", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTestFallbackDialog = false }) {
                    Text("ปิด", color = Color(0xFF747D8C))
                }
            }
        )
    }
}

@Composable
private fun VipPerkItem(icon: String, title: String, desc: String) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = icon, fontSize = 16.sp)
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2D3436)
            )
            Text(
                text = desc,
                fontSize = 11.sp,
                color = Color(0xFF757575)
            )
        }
    }
}

@Composable
private fun PlanSelectionCard(
    plan: SubscriptionPlan,
    displayPrice: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = when {
        isSelected && plan.isLifetime -> Color(0xFFFF9F43)
        isSelected -> Color(0xFF6C5CE7)
        else -> Color(0xFFE2E8F0)
    }

    val bgColor = when {
        isSelected && plan.isLifetime -> Color(0xFFFFF8E1)
        isSelected -> Color(0xFFF3E5F5).copy(alpha = 0.6f)
        else -> Color.White
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 3.dp else 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .border(
                                width = if (isSelected) 6.dp else 2.dp,
                                color = if (isSelected) (if (plan.isLifetime) Color(0xFFFF9F43) else Color(0xFF6C5CE7)) else Color(0xFFBDBDBD),
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = plan.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.5.sp,
                        color = Color(0xFF2D3436)
                    )
                }

                if (!plan.badge.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (plan.isRecommended || plan.isLifetime) Color(0xFFFF9F43) else Color(0xFF6C5CE7)
                    ) {
                        Text(
                            text = plan.badge,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = plan.description,
                        fontSize = 11.5.sp,
                        color = Color(0xFF616161)
                    )
                    if (plan.bonusStars > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "🎁 โบนัสพิเศษ: +${plan.bonusStars} ดาวทันที!",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE65100)
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = displayPrice,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (plan.isLifetime) Color(0xFFE65100) else Color(0xFF6C5CE7)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = plan.durationText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF757575),
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }
        }
    }
}

object SubscriptionDialogHelper {
    @Composable
    fun Show(
        onDismiss: () -> Unit,
        onSubscribed: ((SubscriptionPlan) -> Unit)? = null
    ) {
        SubscriptionDialog(onDismiss = onDismiss, onSubscribed = onSubscribed)
    }
}
