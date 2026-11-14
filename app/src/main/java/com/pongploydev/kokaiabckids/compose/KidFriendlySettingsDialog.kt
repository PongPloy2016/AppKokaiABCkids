package com.pongploydev.kokaiabckids.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Celebration
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Mail
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.pongploydev.kokaiabckids.utils.FeedbackHelper
import com.pongploydev.profilefeature.data.SupabaseDataManager


// โทนสีสำหรับธีมเด็กสดใส
private val PrimaryPurple = Color(0xFF6C5CE7)
private val LightPurpleBg = Color(0xFFF3F0FF)
private val SoftCardBg = Color(0xFFF8F9FA)
private val TextDark = Color(0xFF2D3436)
private val TextMuted = Color(0xFF636E72)
private val MusicGreen = Color(0xFF00B894)

@Composable
fun KidFriendlySettingsDialog(
    isMusicEnabled: Boolean,
    onMusicToggle: (Boolean) -> Unit,
    onOtherActivitiesClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sharedPref = remember { context.getSharedPreferences("setting", Context.MODE_PRIVATE) }
    var isSupabaseApi by remember { mutableStateOf(sharedPref.getBoolean("use_supabase_api", true)) }


    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Header พร้อมไอคอนน่ารัก
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 18.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(LightPurpleBg)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = null,
                            tint = PrimaryPurple,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "ข้อมูลและการตั้งค่า",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                }

                // 1. การตั้งค่าระบบ (Settings Card)
                Text(
                    text = "การตั้งค่าระบบ",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = SoftCardBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.MusicNote,
                                contentDescription = null,
                                tint = MusicGreen,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "เปิดเสียงดนตรีพื้นหลัง",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextDark
                            )
                        }
                        Switch(
                            checked = isMusicEnabled,
                            onCheckedChange = onMusicToggle,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = MusicGreen
                            )
                        )
                    }
                }

                // 1.2 การตั้งค่า Video API (Supabase / Legacy)
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = SoftCardBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Bolt,
                                contentDescription = null,
                                tint = if (isSupabaseApi) Color(0xFF3ECF8E) else Color(0xFF1565C0),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isSupabaseApi) "⚡ Supabase API" else "🌐 Legacy API",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextDark
                                )
                                Text(
                                    text = if (isSupabaseApi) "ใช้ฐานข้อมูล Supabase" else "ใช้เซิร์ฟเวอร์เดิม",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                        }
                        Switch(
                            checked = isSupabaseApi,
                            onCheckedChange = { checked ->
                                sharedPref.edit().putBoolean("use_supabase_api", checked).apply()
                                isSupabaseApi = checked
                                val msg = if (checked) "⚡ สลับไปใช้ Supabase API" else "🌐 สลับไปใช้ Legacy API"
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF3ECF8E)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))


                // 2. ผู้จัดทำ & ส่ง Feedback (Dev Info Card)
                Text(
                    text = "ผู้จัดทำ & ข้อเสนอแนะ",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = LightPurpleBg.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Code,
                                contentDescription = null,
                                tint = PrimaryPurple,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ponployproDev",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextDark
                            )
                        }

                        Text(
                            text = "popodev@outlook.com",
                            fontSize = 12.sp,
                            color = TextMuted,
                            modifier = Modifier
                                .padding(start = 24.dp, bottom = 12.dp)
                                .clickable {
                                    FeedbackHelper.showFeedbackCategoryDialog(context)
                                }
                        )

                        // ปุ่ม Feedback นุ่มนวล โทนแอป
                        Button(
                            onClick = {
                                FeedbackHelper.showFeedbackCategoryDialog(context)
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                            contentPadding = PaddingValues(vertical = 10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Mail,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ส่งคำติชม / ข้อเสนอแนะ",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        TextButton(
                            onClick = {
                                val privacyUrl = SupabaseDataManager.getInstance(context)
                                    .getCachedAppSettings()?.privacyPolicyUrl
                                    ?.takeIf { it.isNotBlank() }
                                    ?: "https://pongploydev.github.io/privacy-policy/"
                                try {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(privacyUrl)))
                                } catch (e: Exception) {
                                    Toast.makeText(context, "ไม่สามารถเปิดลิงก์นโยบายความเป็นส่วนตัวได้", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PrivacyTip,
                                contentDescription = null,
                                tint = PrimaryPurple,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "นโยบายความเป็นส่วนตัว (Privacy Policy)",
                                fontSize = 13.sp,
                                color = PrimaryPurple,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 3. ปุ่มด้านล่าง (Action Row)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onOtherActivitiesClick,
                        colors = ButtonDefaults.textButtonColors(contentColor = PrimaryPurple)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Celebration,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "กิจกรรมอื่นๆ",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    FilledTonalButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = SoftCardBg,
                            contentColor = TextDark
                        ),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Text(
                            text = "ตกลง",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
