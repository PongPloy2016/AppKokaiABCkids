package com.pongploydev.profilefeature.compose

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.pongploydev.profilefeature.auth.SupabaseAuthManager
import com.pongploydev.profilefeature.auth.SupabaseUser
import com.pongploydev.profilefeature.data.SupabaseDataManager
import com.pongploydev.profilefeature.data.UserAchievement
import com.pongploydev.profilefeature.data.UserProfile
import com.pongploydev.profilefeature.data.UserQuizResult
import kotlinx.coroutines.launch

object KidTheme {
    val Primary = Color(0xFF6C5CE7)
    val SupabasePrimary = Color(0xFF3ECF8E)
    val Secondary = Color(0xFFFF7675)
    val Accent = Color(0xFFFFD166)
    val Success = Color(0xFF00B894)
    val TextDark = Color(0xFF2D3436)
    val TextMuted = Color(0xFF636E72)
    val DualHeaderGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF6C5CE7), Color(0xFF3ECF8E))
    )
}

// Helper to navigate to tabs in MainActivity
private fun switchToAppTab(context: Context, tabIndex: Int) {
    try {
        val activity = context as? androidx.fragment.app.FragmentActivity
        if (activity != null) {
            val viewPager = activity.findViewById<androidx.viewpager.widget.ViewPager>(
                context.resources.getIdentifier("viewpager", "id", context.packageName)
            )
            val viewPagerRtl = activity.findViewById<View>(
                context.resources.getIdentifier("viewpager_rtl", "id", context.packageName)
            )
            if (viewPager != null && viewPager.visibility == View.VISIBLE) {
                viewPager.currentItem = tabIndex
                return
            }
            if (viewPagerRtl is androidx.viewpager.widget.ViewPager && viewPagerRtl.visibility == View.VISIBLE) {
                viewPagerRtl.currentItem = tabIndex
                return
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@Composable
fun ProfileComposeScreen() {
    val context = LocalContext.current
    val firebaseAuth = remember { FirebaseAuth.getInstance() }
    val supabaseAuthManager = remember { SupabaseAuthManager.getInstance(context) }

    var firebaseUser by remember { mutableStateOf(firebaseAuth.currentUser) }
    var supabaseUser by remember { mutableStateOf(supabaseAuthManager.getStoredUser()) }

    DisposableEffect(firebaseAuth) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            firebaseUser = auth.currentUser
        }
        firebaseAuth.addAuthStateListener(listener)
        onDispose {
            firebaseAuth.removeAuthStateListener(listener)
        }
    }

    LaunchedEffect(firebaseUser) {
        if (firebaseUser == null) {
            try {
                firebaseAuth.signInAnonymously()
                    .addOnSuccessListener { result ->
                        firebaseUser = result.user
                    }
                    .addOnFailureListener { e ->
                        e.printStackTrace()
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9FC))
    ) {
        if (firebaseUser != null || supabaseUser != null) {
            ProfileDetailsViewDual(
                firebaseUser = firebaseUser,
                supabaseUser = supabaseUser,
                firebaseAuth = firebaseAuth,
                supabaseAuthManager = supabaseAuthManager,
                onSignOut = {
                    firebaseAuth.signOut()
                    supabaseAuthManager.signOut()
                    supabaseUser = null
                }
            )
        } else {
            AuthLoginViewDual(
                firebaseAuth = firebaseAuth,
                supabaseAuthManager = supabaseAuthManager,
                onSupabaseSuccess = { user ->
                    supabaseUser = user
                }
            )
        }
    }
}

// ==================== DUAL LOGIN / SIGN UP VIEW ====================

@Composable
fun AuthLoginViewDual(
    firebaseAuth: FirebaseAuth,
    supabaseAuthManager: SupabaseAuthManager,
    onSupabaseSuccess: (SupabaseUser) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isSignUpMode by remember { mutableStateOf(false) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        item {
            // Header Badge showing Dual Sync
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = KidTheme.Primary.copy(alpha = 0.12f),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🔥 Firebase + ⚡ Supabase Auth Dual Sync",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = KidTheme.Primary
                    )
                }
            }

            // Logo Header
            Surface(
                shape = CircleShape,
                color = KidTheme.Primary.copy(alpha = 0.15f),
                modifier = Modifier.size(80.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Profile Icon",
                        tint = KidTheme.Primary,
                        modifier = Modifier.size(60.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (isSignUpMode) "สร้างบัญชีผู้ใช้ใหม่ ✨" else "ยินดีต้อนรับเข้าสู่ระบบ! 🎈",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = KidTheme.TextDark,
                textAlign = TextAlign.Center
            )

            Text(
                text = if (isSignUpMode) "สมัครสมาชิกครั้งเดียว ลงทะเบียนทั้ง Firebase & Supabase ให้อัตโนมัติ"
                       else "กรอกอีเมลและรหัสผ่านเพื่อเข้าสู่ระบบ ทั้ง Firebase & Supabase",
                fontSize = 13.sp,
                color = KidTheme.TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Mode Switcher (Login vs Register)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(Color(0xFFF0F3F8)),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .padding(2.dp)
                                .clickable {
                                    isSignUpMode = false
                                    errorMessage = null
                                },
                            shape = RoundedCornerShape(20.dp),
                            color = if (!isSignUpMode) KidTheme.Primary else Color.Transparent
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "เข้าสู่ระบบ",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (!isSignUpMode) Color.White else KidTheme.TextMuted
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .padding(2.dp)
                                .clickable {
                                    isSignUpMode = true
                                    errorMessage = null
                                },
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSignUpMode) KidTheme.Primary else Color.Transparent
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "ลงทะเบียน",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (isSignUpMode) Color.White else KidTheme.TextMuted
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (isSignUpMode) {
                        OutlinedTextField(
                            value = displayName,
                            onValueChange = { displayName = it },
                            label = { Text("ชื่อแสดงผล (Display Name)") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = KidTheme.Primary) },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = KidTheme.Primary,
                                unfocusedBorderColor = Color(0xFFE0E0E0)
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it.trim() },
                        label = { Text("อีเมล (Email)") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = KidTheme.Primary) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = KidTheme.Primary,
                            unfocusedBorderColor = Color(0xFFE0E0E0)
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("รหัสผ่าน (Password)") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = KidTheme.Primary) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = KidTheme.Primary,
                            unfocusedBorderColor = Color(0xFFE0E0E0)
                        )
                    )

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFFEBEE),
                            border = BorderStroke(1.dp, Color(0xFFFFCDD2)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = errorMessage!!,
                                color = Color(0xFFD32F2F),
                                fontSize = 13.sp,
                                modifier = Modifier.padding(12.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (email.isBlank() || password.isBlank()) {
                                errorMessage = "กรุณากรอกอีเมลและรหัสผ่านให้ครบถ้วน"
                                return@Button
                            }
                            if (password.length < 6) {
                                errorMessage = "รหัสผ่านต้องมีความยาวอย่างน้อย 6 ตัวอักษร"
                                return@Button
                            }

                            isLoading = true
                            errorMessage = null

                            val analytics = FirebaseAnalytics.getInstance(context)

                            scope.launch {
                                if (isSignUpMode) {
                                    val bundle = Bundle().apply { putString("button_name", "dual_register_submit") }
                                    analytics.logEvent("select_content", bundle)

                                    val finalName = displayName.ifBlank { "นักอ่านตัวน้อย" }

                                    // 1. Supabase Sign Up
                                    val supabaseResult = supabaseAuthManager.signUp(email, password, finalName)
                                    if (supabaseResult.isSuccess) {
                                        val spUser = supabaseResult.getOrThrow()
                                        val dataMgr = SupabaseDataManager.getInstance(context)
                                        dataMgr.upsertUserProfile(
                                            UserProfile(
                                                id = spUser.id,
                                                email = spUser.email ?: email,
                                                displayName = finalName
                                            )
                                        )
                                    }

                                    // 2. Firebase Sign Up (with fallback)
                                    if (firebaseAuth.currentUser?.isAnonymous == true) {
                                        firebaseAuth.signOut()
                                    }
                                    firebaseAuth.createUserWithEmailAndPassword(email, password)
                                        .addOnSuccessListener { result ->
                                            val user = result.user
                                            if (user != null && finalName.isNotBlank()) {
                                                val profileUpdates = UserProfileChangeRequest.Builder()
                                                    .setDisplayName(finalName)
                                                    .build()
                                                user.updateProfile(profileUpdates)
                                            }
                                            if (supabaseResult.isSuccess) {
                                                onSupabaseSuccess(supabaseResult.getOrThrow())
                                            }
                                            isLoading = false
                                            Toast.makeText(context, "สมัครสมาชิกและเชื่อมต่อทั้ง Firebase และ Supabase เรียบร้อย! 🚀", Toast.LENGTH_SHORT).show()
                                        }
                                        .addOnFailureListener { e ->
                                            // Fallback 1: try Firebase sign-in if account already exists
                                            firebaseAuth.signInWithEmailAndPassword(email, password)
                                                .addOnSuccessListener {
                                                    if (supabaseResult.isSuccess) {
                                                        onSupabaseSuccess(supabaseResult.getOrThrow())
                                                    }
                                                    isLoading = false
                                                    Toast.makeText(context, "เข้าสู่ระบบและเชื่อมต่อทั้ง Firebase และ Supabase เรียบร้อย! 🚀", Toast.LENGTH_SHORT).show()
                                                }
                                                .addOnFailureListener {
                                                    // Fallback 2: Keep Firebase Active via anonymous sign in
                                                    firebaseAuth.signInAnonymously().addOnCompleteListener {
                                                        if (supabaseResult.isSuccess) {
                                                            onSupabaseSuccess(supabaseResult.getOrThrow())
                                                            isLoading = false
                                                            Toast.makeText(context, "สมัครสมาชิกสำเร็จ! (Supabase & Firebase Active) 🚀", Toast.LENGTH_SHORT).show()
                                                        } else {
                                                            isLoading = false
                                                            errorMessage = e.localizedMessage ?: "สมัครสมาชิกล้มเหลว"
                                                        }
                                                    }
                                                }
                                        }
                                } else {
                                    val bundle = Bundle().apply { putString("button_name", "dual_login_submit") }
                                    analytics.logEvent("select_content", bundle)

                                    // 1. Supabase Sign In
                                    val supabaseResult = supabaseAuthManager.signIn(email, password)

                                    // 2. Firebase Sign In
                                    if (firebaseAuth.currentUser?.isAnonymous == true) {
                                        firebaseAuth.signOut()
                                    }
                                    firebaseAuth.signInWithEmailAndPassword(email, password)
                                        .addOnSuccessListener {
                                            if (supabaseResult.isSuccess) {
                                                onSupabaseSuccess(supabaseResult.getOrThrow())
                                            }
                                            isLoading = false
                                            Toast.makeText(context, "เข้าสู่ระบบสำเร็จทั้ง Firebase และ Supabase! 🚀", Toast.LENGTH_SHORT).show()
                                        }
                                        .addOnFailureListener { e ->
                                            firebaseAuth.signInAnonymously().addOnCompleteListener {
                                                if (supabaseResult.isSuccess) {
                                                    onSupabaseSuccess(supabaseResult.getOrThrow())
                                                    isLoading = false
                                                    Toast.makeText(context, "เข้าสู่ระบบ Supabase สำเร็จ! (Firebase Active) 🚀", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    isLoading = false
                                                    errorMessage = e.localizedMessage ?: "เข้าสู่ระบบไม่สำเร็จ กรุณาตรวจสอบอีเมลหรือรหัสผ่าน"
                                                }
                                            }
                                        }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(26.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = KidTheme.Primary),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text(
                                text = if (isSignUpMode) "สมัครสมาชิก 🚀" else "เข้าสู่ระบบ 🔑",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clickable {
                                isLoading = true
                                errorMessage = null

                                scope.launch {
                                    // Supabase Guest Sign In
                                    val supabaseResult = supabaseAuthManager.signInAnonymously()
                                    supabaseResult.onSuccess { spUser ->
                                        onSupabaseSuccess(spUser)
                                    }

                                    // Firebase Guest Sign In
                                    firebaseAuth.signInAnonymously()
                                        .addOnSuccessListener {
                                            isLoading = false
                                            Toast.makeText(context, "เข้าสู่ระบบ Guest ทั้ง Firebase และ Supabase เรียบร้อย", Toast.LENGTH_SHORT).show()
                                        }
                                        .addOnFailureListener { e ->
                                            isLoading = false
                                            if (supabaseResult.isSuccess) {
                                                Toast.makeText(context, "เข้าสู่ระบบ Supabase Guest เรียบร้อย", Toast.LENGTH_SHORT).show()
                                            } else {
                                                errorMessage = e.localizedMessage ?: "เข้าใช้งานแบบ Guest ไม่สำเร็จ"
                                            }
                                        }
                                }
                            },
                        shape = RoundedCornerShape(24.dp),
                        color = Color(0xFFE8F5E9),
                        border = BorderStroke(1.5.dp, Color(0xFF81C784))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "👤 เข้าใช้งานแบบ Guest (ชั่วคราว)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==================== DUAL PROFILE VIEW WITH SUPABASE LIVE DATA & QUICK LINKS ====================

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileDetailsViewDual(
    firebaseUser: FirebaseUser?,
    supabaseUser: SupabaseUser?,
    firebaseAuth: FirebaseAuth,
    supabaseAuthManager: SupabaseAuthManager,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dataManager = remember { SupabaseDataManager.getInstance(context) }

    val userId = supabaseUser?.id ?: firebaseUser?.uid ?: "guest_user"

    var profileData by remember { mutableStateOf<UserProfile?>(null) }
    var quizResults by remember { mutableStateOf<List<UserQuizResult>>(emptyList()) }
    var achievements by remember { mutableStateOf<List<UserAchievement>>(emptyList()) }

    var isDataLoading by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showEditNameDialog by remember { mutableStateOf(false) }
    var showQuizHistoryDetail by remember { mutableStateOf(false) }

    val initialName = profileData?.displayName
        ?: firebaseUser?.displayName
        ?: supabaseUser?.userMetadata?.displayName
        ?: "นักอ่านตัวน้อย"

    var newName by remember { mutableStateOf(initialName) }

    val displayName = if (firebaseUser?.isAnonymous == true || supabaseUser?.isAnonymous == true) {
        "ผู้ใช้ชั่วคราว (Guest)"
    } else {
        initialName
    }

    val emailText = firebaseUser?.email ?: supabaseUser?.email ?: profileData?.email ?: "ยังไม่ได้เชื่อมต่ออีเมล"

    // Load Live Data from Supabase
    fun loadSupabaseLiveData() {
        if (userId.isBlank()) return
        isDataLoading = true
        scope.launch {
            dataManager.fetchUserProfile(userId).onSuccess { prof ->
                profileData = prof
            }
            dataManager.fetchQuizResults(userId).onSuccess { quizzes ->
                quizResults = quizzes
            }
            dataManager.fetchAchievements(userId).onSuccess { ach ->
                achievements = ach
            }
            isDataLoading = false
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, userId) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME || event == Lifecycle.Event.ON_START) {
                loadSupabaseLiveData()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(userId) {
        loadSupabaseLiveData()
    }

    if (showQuizHistoryDetail) {
        QuizHistoryListViewScreen(
            quizResults = quizResults,
            onBack = { showQuizHistoryDetail = false },
            onTakeMoreQuiz = {
                showQuizHistoryDetail = false
                switchToAppTab(context, 3)
            }
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 20.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(bottomStart = 38.dp, bottomEnd = 38.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF536DFE),
                                    Color(0xFF7C4DFF),
                                    Color(0xFF00B0FF)
                                )
                            )
                        )
                        .padding(top = 32.dp, bottom = 36.dp, start = 20.dp, end = 20.dp)
                ) {
                    // Floating decorative circles for glassmorphic depth
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .align(Alignment.TopEnd)
                            .graphicsLayer { translationX = 50f; translationY = -50f }
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.12f))
                    )
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .align(Alignment.BottomStart)
                            .graphicsLayer { translationX = -30f; translationY = 30f }
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.1f))
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Avatar Container with Crown Badge Overlay
                        Box(contentAlignment = Alignment.BottomEnd) {
                            Surface(
                                modifier = Modifier.size(96.dp),
                                shape = CircleShape,
                                color = Color.White,
                                border = BorderStroke(4.dp, Color(0xFFFFD166)),
                                shadowElevation = 12.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Profile Avatar",
                                        tint = Color(0xFF536DFE),
                                        modifier = Modifier.size(60.dp)
                                    )
                                }
                            }

                            // Crown / Level Badge
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFFFC107),
                                border = BorderStroke(2.dp, Color.White),
                                shadowElevation = 4.dp,
                                modifier = Modifier.size(30.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(text = "👑", fontSize = 15.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // User Display Name & Edit Button
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = displayName,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            if (firebaseUser?.isAnonymous != true) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = CircleShape,
                                    color = Color.White.copy(alpha = 0.25f),
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clickable { showEditNameDialog = true }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "แก้ไขชื่อ",
                                            tint = Color.White,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Status Pills (Firebase & Supabase)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val isFirebaseActive = firebaseUser != null || firebaseAuth.currentUser != null
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isFirebaseActive) Color.White.copy(alpha = 0.22f) else Color(0xFFFF5252).copy(alpha = 0.4f),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f))
                            ) {
                                Text(
                                    text = if (isFirebaseActive) "🔥 Firebase Active" else "🔥 Firebase Off",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (supabaseUser != null) Color(0xFF00E676).copy(alpha = 0.35f) else Color(0xFFFF5252).copy(alpha = 0.4f),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f))
                            ) {
                                Text(
                                    text = if (supabaseUser != null) "⚡ Supabase Active" else "⚡ Supabase Off",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                    // Header with refresh button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ความก้าวหน้าการเรียนรู้ 🏆",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = KidTheme.TextDark
                        )
                        if (isDataLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = KidTheme.Primary)
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "รีเฟรชข้อมูล",
                                tint = KidTheme.Primary,
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable { loadSupabaseLiveData() }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val totalStars = profileData?.totalStars ?: 0
                    val currentLevel = profileData?.currentLevel ?: 1
                    val totalQuizzes = quizResults.size
                    val totalAchievements = achievements.size

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ProfileStatCard("ดาวสะสม", "$totalStars ⭐", Color(0xFFFFF9E6), Color(0xFFFFD166), Color(0xFFFF9800), Modifier.weight(1f)) {
                            switchToAppTab(context, 0)
                        }
                        ProfileStatCard("เลเวล", "Lv.$currentLevel 🏆", Color(0xFFE8F5E9), Color(0xFF81C784), Color(0xFF2E7D32), Modifier.weight(1f)) {
                            switchToAppTab(context, 0)
                        }
                        ProfileStatCard("ควิซที่ผ่าน", "$totalQuizzes 📚", Color(0xFFE3F2FD), Color(0xFF64B5F6), Color(0xFF1976D2), Modifier.weight(1f)) {
                            showQuizHistoryDetail = true
                        }
                        ProfileStatCard("เหรียญตรา", "$totalAchievements 🎖️", Color(0xFFF3E5F5), Color(0xFFBA68C8), Color(0xFF7B1FA2), Modifier.weight(1f)) {
                            Toast.makeText(context, "เหรียญตราทั้งหมดที่ปลดล็อกแล้ว 🏆", Toast.LENGTH_SHORT).show()
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // BUTTON TO VIEW QUIZ HISTORY CARD LISTVIEW
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showQuizHistoryDetail = true },
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFE3F2FD),
                        border = BorderStroke(1.dp, Color(0xFF90CAF9))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "📚", fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(text = "ดูรายละเอียดควิซที่ผ่านทั้งหมด ($totalQuizzes รายการ)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
                                    Text(text = "แสดงคะแนน เวลาที่ใช้ และสถานะผ่านเกณฑ์แบบ Card List", fontSize = 11.sp, color = KidTheme.TextMuted)
                                }
                            }
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color(0xFF1565C0), modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // SECTION: QUICK LEARNING LINKS (ทางลัดสะสมคะแนน)
                    Text(
                        text = "กิจกรรมเรียนรู้สะสมคะแนน & ดาว 🎯",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = KidTheme.TextDark,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // LINK 1: QUIZ (แบบทดสอบ)
                        QuickEarnScoreCard(
                            icon = "🎯",
                            title = "ทำแบบทดสอบวัดความรู้ (Quiz)",
                            subtitle = "ทำควิซ 10 ข้อ รับดาวสูงสุด +10 ⭐ พร้อมเหรียญเกียรติบัตร 🏆",
                            tagText = "รับดาวเยอะสุด 🔥",
                            tagBgColor = Color(0xFFFFE0B2),
                            tagTextColor = Color(0xFFE65100),
                            onClick = {
                                Toast.makeText(context, "บันทึกคะแนนสำเร็จแล้ว! 🎯 กำลังพาไปหน้าแบบทดสอบ (Quiz)", Toast.LENGTH_SHORT).show()
                                switchToAppTab(context, 3) // Tab 3: Quiz
                            }
                        )

                        // LINK 2: LEARNING CATEGORIES (ฝึกเขียน/ท่อง ก-ฮ)
                        QuickEarnScoreCard(
                            icon = "✍️",
                            title = "หมวดเรียนรู้ ก-ฮ / A-Z / 1-10",
                            subtitle = "ฝึกเขียนคัดลายมือ และฟังเสียงคำอ่าน รับดาวสะสม +1 ⭐ ทุกตัวอักษร",
                            tagText = "สะสมดาวฟรี 🌟",
                            tagBgColor = Color(0xFFE8F5E9),
                            tagTextColor = Color(0xFF2E7D32),
                            onClick = {
                                Toast.makeText(context, "บันทึกคะแนนสำเร็จแล้ว! ✍️ กำลังพาไปหน้าหมวดเรียนรู้", Toast.LENGTH_SHORT).show()
                                switchToAppTab(context, 0) // Tab 0: Home Learning
                            }
                        )

                        // LINK 3: VIDEO LEARNING (วิดีโอสร้างทักษะ)
                        QuickEarnScoreCard(
                            icon = "🎬",
                            title = "วิดีโอเสริมสร้างทักษะเด็ก",
                            subtitle = "ชมคลิปการ์ตูนและบทเรียนวิดีโอเพื่อความเพลิดเพลิน",
                            tagText = "วิดีโอความรู้ 📺",
                            tagBgColor = Color(0xFFE3F2FD),
                            tagTextColor = Color(0xFF1565C0),
                            onClick = {
                                Toast.makeText(context, "บันทึกคะแนนสำเร็จแล้ว! 🎬 กำลังพาไปหน้าวิดีโอเสริมสร้างทักษะ", Toast.LENGTH_SHORT).show()
                                switchToAppTab(context, 2) // Tab 2: Video List
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // INTERACTIVE SUPABASE LIVE ACTIONS CARD
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F4FF)),
                        border = BorderStroke(1.dp, Color(0xFFC7D2FE))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "⚡ ทดสอบบันทึกข้อมูลสดเข้า Supabase Table",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = KidTheme.Primary
                            )
                            Text(
                                text = "ทดลองกดบันทึกเพื่ออัปเดตคะแนนลงตาราง PostgreSQL ทันที",
                                fontSize = 12.sp,
                                color = KidTheme.TextMuted,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        scope.launch {
                                            dataManager.addStarsAndLevel(userId, totalStars, currentLevel, 10).onSuccess { updated ->
                                                profileData = updated
                                                Toast.makeText(context, "เพิ่ม ⭐ +10 เข้า Supabase สำเร็จ!", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB74D))
                                ) {
                                    Text("+10 ⭐ ดาว", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }

                                Button(
                                    onClick = {
                                        scope.launch {
                                            dataManager.saveQuizResult(
                                                userId = userId,
                                                topicId = 1,
                                                topicName = "แบบทดสอบ ก-ฮ (Live Sync)",
                                                score = 10,
                                                totalQuestions = 10
                                            ).onSuccess {
                                                loadSupabaseLiveData()
                                                Toast.makeText(context, "บันทึกผล Quiz ลง Supabase สำเร็จ!", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4FC3F7))
                                ) {
                                    Text("+1 📚 Quiz", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }

                                Button(
                                    onClick = {
                                        scope.launch {
                                            val badgeCount = achievements.size + 1
                                            dataManager.unlockAchievement(
                                                userId = userId,
                                                badgeKey = "badge_star_$badgeCount",
                                                badgeName = "เกียรติบัตรนักอ่าน #$badgeCount",
                                                badgeIcon = if (badgeCount % 2 == 0) "🌟" else "🏆"
                                            ).onSuccess {
                                                loadSupabaseLiveData()
                                                Toast.makeText(context, "ปลดล็อกเหรียญตราใหม่ใน Supabase!", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBA68C8))
                                ) {
                                    Text("+1 🏆 Badge", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // UNLOCKED ACHIEVEMENTS BADGES DISPLAY
                    Text(
                        text = "เหรียญรางวัลความสำเร็จ 🎖️",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = KidTheme.TextDark,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    if (achievements.isEmpty()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                        ) {
                            Text(
                                text = "ยังไม่มีเหรียญรางวัล ลองกดทำควิซเพื่อปลดล็อกเหรียญแรกได้เลย!",
                                fontSize = 13.sp,
                                color = KidTheme.TextMuted,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    } else {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            achievements.forEach { ach ->
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color.White,
                                    border = BorderStroke(1.dp, KidTheme.Accent),
                                    shadowElevation = 2.dp
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = ach.badgeIcon, fontSize = 20.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = ach.badgeName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = KidTheme.TextDark)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "ข้อมูลบัญชีและไอดีระบบ ⚙️",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = KidTheme.TextDark,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            ProfileOptionItem(Icons.Default.Email, "อีเมล", emailText) {}
                            if (firebaseUser != null) {
                                ProfileOptionItem(Icons.Default.Security, "🔥 Firebase UID", firebaseUser.uid.take(16) + "...") {}
                            }
                            if (supabaseUser != null) {
                                ProfileOptionItem(Icons.Default.Security, "⚡ Supabase User ID", supabaseUser.id.take(16) + "...") {}
                            }
                            ProfileOptionItem(Icons.Default.Share, "แชร์แอปฝึกเขียน ก ไก่", "บอกต่อเพื่อนๆ ให้มาเรียนรู้ด้วยกัน") {
                                Toast.makeText(context, "ขอบคุณที่ร่วมแชร์แอปจ้า! ❤️", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { showLogoutDialog = true },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(26.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252))
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("ออกจากระบบทั้งหมด", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("ยืนยันการออกจากระบบ", fontWeight = FontWeight.Bold) },
            text = { Text("คุณต้องการออกจากระบบทั้ง Firebase Auth และ Supabase Auth ใช่หรือไม่?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onSignOut()
                        Toast.makeText(context, "ออกจากระบบทั้งหมดแล้ว", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252))
                ) {
                    Text("ออกจากระบบ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("ยกเลิก") }
            }
        )
    }

    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            title = { Text("แก้ไขชื่อแสดงผล", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("ชื่อใหม่") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isNotBlank()) {
                            scope.launch {
                                // 1. Update Supabase Profile
                                dataManager.updateUserProfile(userId, com.pongploydev.profilefeature.data.UserProfileUpdate(displayName = newName))
                                loadSupabaseLiveData()

                                // 2. Update Firebase User Profile
                                firebaseUser?.let { fbUser ->
                                    val updates = UserProfileChangeRequest.Builder().setDisplayName(newName).build()
                                    fbUser.updateProfile(updates)
                                }
                                showEditNameDialog = false
                                Toast.makeText(context, "อัปเดตชื่อสำเร็จทั้งในระบบ!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = KidTheme.Primary)
                ) { Text("บันทึก") }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) { Text("ยกเลิก") }
            }
        )
    }
}

// ==================== QUIZ HISTORY CARD LISTVIEW SCREEN ====================

// Quick Learning Card Component
@Composable
fun QuickEarnScoreCard(
    icon: String,
    accentColor: Color = KidTheme.Primary,
    title: String,
    subtitle: String,
    tagText: String,
    tagBgColor: Color,
    tagTextColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, Color(0xFFF1F5F9))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = accentColor.copy(alpha = 0.12f),
                modifier = Modifier.size(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = icon, fontSize = 26.sp)
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = KidTheme.TextDark,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = tagBgColor
                    ) {
                        Text(
                            text = tagText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = tagTextColor,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = KidTheme.TextMuted
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                shape = CircleShape,
                color = KidTheme.Primary.copy(alpha = 0.08f),
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "ไปยังหน้ากิจกรรม",
                        tint = KidTheme.Primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// Helpers
@Composable
fun ProfileStatCard(
    title: String,
    value: String,
    bgColor: Color,
    borderColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = bgColor,
        border = BorderStroke(1.5.dp, borderColor),
        shadowElevation = 3.dp
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = textColor,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = KidTheme.TextDark,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@Composable
fun ProfileOptionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = KidTheme.Primary.copy(alpha = 0.1f),
            modifier = Modifier.size(44.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = KidTheme.Primary, modifier = Modifier.size(22.dp))
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = KidTheme.TextDark)
            Text(text = subtitle, fontSize = 12.sp, color = KidTheme.TextMuted)
        }

        Text(text = "›", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.LightGray)
    }
}
