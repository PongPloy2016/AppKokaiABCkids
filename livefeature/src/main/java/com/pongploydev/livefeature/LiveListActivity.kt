package com.pongploydev.livefeature

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit

/**
 * Activity ที่ host LiveListFragment (Compose)
 * เปิดมาแล้วแสดงรายการช่อง DLTV Live ทันที
 */
class LiveListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ใช้ Fragment container แบบ simple — ไม่ต้องการ layout XML เพิ่ม
        val container = android.widget.FrameLayout(this).apply {
            id = android.R.id.content
        }
        // ตั้งค่า content view ให้เต็มจอ
        setContentView(container)

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace(android.R.id.content, LiveListFragment())
            }
        }
    }
}
