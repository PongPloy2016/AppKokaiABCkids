package com.pongploydev.priyankvex.paintapp.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Feedback
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.pongploydev.priyankvex.paintapp.utils.FeedbackHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsFeedbackItem(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showSheet by remember { mutableStateOf(false) }

    // รายการในหน้า Settings
    ListItem(
        headlineContent = { Text("ส่งข้อเสนอแนะ / แจ้งปัญหา") },
        supportingContent = { Text("popodev@outlook.com") },
        leadingContent = { Icon(Icons.Outlined.Feedback, contentDescription = null) },
        modifier = modifier.clickable { showSheet = true }
    )

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Text(
                    text = "เลือกประเภทข้อเสนอแนะ",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                ListItem(
                    headlineContent = { Text("แจ้งปัญหาการใช้งาน (Bug Report)") },
                    leadingContent = { Icon(Icons.Outlined.BugReport, contentDescription = null) },
                    modifier = Modifier.clickable {
                        showSheet = false
                        FeedbackHelper.sendFeedbackEmail(context, category = "Bug Report")
                    }
                )

                ListItem(
                    headlineContent = { Text("เสนอแนะฟีเจอร์ใหม่ (Feature Request)") },
                    leadingContent = { Icon(Icons.Outlined.Lightbulb, contentDescription = null) },
                    modifier = Modifier.clickable {
                        showSheet = false
                        FeedbackHelper.sendFeedbackEmail(context, category = "Feature Request")
                    }
                )

                ListItem(
                    headlineContent = { Text("ข้อเสนอแนะทั่วไป (General)") },
                    leadingContent = { Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = null) },
                    modifier = Modifier.clickable {
                        showSheet = false
                        FeedbackHelper.sendFeedbackEmail(context, category = "General")
                    }
                )
            }
        }
    }
}
