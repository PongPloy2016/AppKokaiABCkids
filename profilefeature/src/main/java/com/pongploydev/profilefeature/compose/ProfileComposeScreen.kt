package com.pongploydev.profilefeature.compose

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.res.painterResource
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.MailOutline
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayCircleOutline
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import java.util.Calendar
import java.util.UUID

object KidTheme {
    val Primary = Color(0xFF6C5CE7)
    val Accent = Color(0xFFFFD166)
    val TextDark = Color(0xFF1F2430)
    val TextMuted = Color(0xFF6B7280)
    val Background = Color(0xFFF6F7FB)
    val CardBorder = Color(0xFFE8ECF2)
    val DividerColor = Color(0xFFF0F2F6)
    val GoldGradient = Brush.horizontalGradient(
        listOf(Color(0xFFFFCA28), Color(0xFFFF9800))
    )
}

private data class ProfileDailyTheme(
    val gradientColors: List<Color>,
    val accentColor: Color
) {
    val headerGradient: Brush
        get() = Brush.linearGradient(colors = gradientColors)
}

private fun profileDailyHeaderThemeForDay(
    dayOfWeek: Int = Calendar.getInstance()[Calendar.DAY_OF_WEEK]
): ProfileDailyTheme = when (dayOfWeek) {
    Calendar.SUNDAY -> ProfileDailyTheme(
        gradientColors = listOf(Color(0xFFE53E51), Color(0xFFFF6B78)),
        accentColor = Color(0xFFE53E51)
    )
    Calendar.MONDAY -> ProfileDailyTheme(
        gradientColors = listOf(Color(0xFFD98A00), Color(0xFFF2AA1F)),
        accentColor = Color(0xFFD98A00)
    )
    Calendar.TUESDAY -> ProfileDailyTheme(
        gradientColors = listOf(Color(0xFFE0428B), Color(0xFFFF6BA9)),
        accentColor = Color(0xFFE0428B)
    )
    Calendar.WEDNESDAY -> ProfileDailyTheme(
        gradientColors = listOf(Color(0xFF159D6B), Color(0xFF2CC88B)),
        accentColor = Color(0xFF159D6B)
    )
    Calendar.THURSDAY -> ProfileDailyTheme(
        gradientColors = listOf(Color(0xFFEB6B1E), Color(0xFFFF8C3B)),
        accentColor = Color(0xFFEB6B1E)
    )
    Calendar.FRIDAY -> ProfileDailyTheme(
        gradientColors = listOf(Color(0xFF1B86E6), Color(0xFF42A5FF)),
        accentColor = Color(0xFF1B86E6)
    )
    else -> ProfileDailyTheme(
        gradientColors = listOf(Color(0xFF6A4EFF), Color(0xFF866BFF)),
        accentColor = Color(0xFF6A4EFF)
    )
}

private fun formatProfileDisplayName(rawName: String?): String {
    val trimmed = rawName?.trim().orEmpty()
    if (trimmed.isEmpty() ||
        trimmed.equals("guest", ignoreCase = true) ||
        trimmed.startsWith("guest", ignoreCase = true) ||
        trimmed.startsWith("ผู้ใช้ชั่วคราว") ||
        (trimmed.length > 14 && trimmed.any { it.isDigit() } && !trimmed.contains(" "))
    ) {
        return "น้องคนเก่ง"
    }
    return trimmed
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
    var showAuthScreen by rememberSaveable { mutableStateOf(false) }

    DisposableEffect(firebaseAuth) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            firebaseUser = auth.currentUser
        }
        firebaseAuth.addAuthStateListener(listener)
        onDispose {
            firebaseAuth.removeAuthStateListener(listener)
        }
    }

    LaunchedEffect(firebaseUser, showAuthScreen) {
        if (firebaseUser == null && !showAuthScreen) {
            try {
                val guestName = supabaseAuthManager.getOrCreateGuestDisplayName()
                val dataMgr = SupabaseDataManager.getInstance(context)
                dataMgr.saveUserProgress(
                    stars = dataMgr.getUserTotalStars(),
                    level = dataMgr.getUserCurrentLevel(),
                    displayName = guestName
                )
                if (supabaseUser == null) {
                    supabaseAuthManager.signInAnonymously().onSuccess { spUser ->
                        supabaseUser = spUser
                        dataMgr.upsertUserProfile(
                            UserProfile(
                                id = spUser.id,
                                email = null,
                                displayName = guestName,
                                avatarUrl = dataMgr.getLocalAvatarUrl() ?: "",
                                role = "student",
                                totalStars = dataMgr.getUserTotalStars(),
                                totalCoins = 0,
                                currentLevel = dataMgr.getUserCurrentLevel(),
                                streakDays = dataMgr.getUserStreakDays()
                            )
                        )
                    }
                }
                firebaseAuth.signInAnonymously()
                    .addOnSuccessListener { result ->
                        result.user?.updateProfile(
                            UserProfileChangeRequest.Builder()
                                .setDisplayName(guestName)
                                .build()
                        )
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
            .background(KidTheme.Background)
    ) {
        if (!showAuthScreen && (firebaseUser != null || supabaseUser != null)) {
            ProfileDetailsViewDual(
                firebaseUser = firebaseUser,
                supabaseUser = supabaseUser,
                firebaseAuth = firebaseAuth,
                supabaseAuthManager = supabaseAuthManager,
                onOpenAuth = {
                    showAuthScreen = true
                },
                onSignOut = {
                    firebaseAuth.signOut()
                    supabaseAuthManager.signOut(resetGuestSession = true)
                    SupabaseDataManager.getInstance(context).clearLocalUserData()
                    try {
                        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
                        GoogleSignIn.getClient(context, gso).signOut()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    firebaseUser = null
                    supabaseUser = null
                    showAuthScreen = true
                }
            )
        } else {
            AuthLoginViewDual(
                firebaseAuth = firebaseAuth,
                supabaseAuthManager = supabaseAuthManager,
                onDismiss = if (firebaseUser != null || supabaseUser != null) {
                    { showAuthScreen = false }
                } else null,
                onSupabaseSuccess = { user ->
                    supabaseUser = user
                    showAuthScreen = false
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
    onDismiss: (() -> Unit)? = null,
    onSupabaseSuccess: (SupabaseUser) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isSignUpMode by remember { mutableStateOf(false) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSha1Dialog by remember { mutableStateOf(false) }

    val googleSignInClient = remember(context) {
        val gsoBuilder = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()

        val defaultWebClientIdRes = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        val webClientId = if (defaultWebClientIdRes != 0) {
            try { context.getString(defaultWebClientIdRes) } catch (e: Exception) { null }
        } else {
            null
        }

        if (!webClientId.isNullOrBlank()) {
            gsoBuilder.requestIdToken(webClientId)
        }
        GoogleSignIn.getClient(context, gsoBuilder.build())
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d("GoogleAuth", "googleSignInLauncher returned resultCode=${result.resultCode}, hasData=${result.data != null}")

        if (result.data == null) {
            isLoading = false
            Log.w("GoogleAuth", "Google sign-in result.data is null (user backed out)")
        } else {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    val googleEmail = account.email ?: ""
                    val googleDisplayName = account.displayName ?: account.givenName ?: "เพื่อนตัวน้อย Google"
                    val googleId = account.id ?: UUID.randomUUID().toString()
                    val photoUrl = account.photoUrl?.toString()
                    val idToken = account.idToken

                    val analytics = FirebaseAnalytics.getInstance(context)
                    val bundle = Bundle().apply {
                        putString("button_name", "google_signin_success")
                        putString("user_email", googleEmail)
                    }
                    analytics.logEvent("select_content", bundle)

                    scope.launch {
                        // 1. Supabase Auth Sync
                        val supabaseRes = supabaseAuthManager.signInWithGoogle(
                            email = googleEmail,
                            displayName = googleDisplayName,
                            googleId = googleId,
                            avatarUrl = photoUrl,
                            idToken = idToken
                        )
                        val spUser = supabaseRes.getOrNull()

                        // 2. Upsert profile & device to Supabase
                        if (spUser != null) {
                            val dataMgr = SupabaseDataManager.getInstance(context)
                            val currentLocalStars = dataMgr.getUserTotalStars()
                            val currentLocalLevel = dataMgr.getUserCurrentLevel()
                            dataMgr.upsertUserProfile(
                                UserProfile(
                                    id = spUser.id,
                                    email = googleEmail,
                                    displayName = googleDisplayName,
                                    avatarUrl = photoUrl,
                                    totalStars = currentLocalStars,
                                    currentLevel = currentLocalLevel
                                )
                            )
                            val androidId = try {
                                Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
                            } catch (e: Exception) {
                                null
                            } ?: UUID.randomUUID().toString()

                            val appVersion = try {
                                context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0.0"
                            } catch (e: Exception) {
                                "1.0.0"
                            }
                            dataMgr.registerUserDevice(
                                userId = spUser.id,
                                deviceId = androidId,
                                appVersion = appVersion
                            )
                        }

                        // 3. Firebase Auth Sync
                        val finishAuth = {
                            isLoading = false
                            if (spUser != null) {
                                onSupabaseSuccess(spUser)
                            }
                            Toast.makeText(context, "ยินดีต้อนรับ ${googleDisplayName}! เข้าสู่ระบบด้วย Google เรียบร้อยแล้ว 🚀⭐", Toast.LENGTH_SHORT).show()
                        }

                        if (!idToken.isNullOrBlank()) {
                            try {
                                val credential = GoogleAuthProvider.getCredential(idToken, null)
                                if (firebaseAuth.currentUser?.isAnonymous == true) {
                                    firebaseAuth.signOut()
                                }
                                firebaseAuth.signInWithCredential(credential)
                                    .addOnSuccessListener {
                                        finishAuth()
                                    }
                                    .addOnFailureListener { e ->
                                        Log.w("GoogleAuth", "Firebase signInWithCredential failed: ${e.message}, keeping active session")
                                        if (firebaseAuth.currentUser == null) {
                                            firebaseAuth.signInAnonymously().addOnCompleteListener { finishAuth() }
                                        } else {
                                            finishAuth()
                                        }
                                    }
                            } catch (e: Exception) {
                                finishAuth()
                            }
                        } else {
                            if (firebaseAuth.currentUser == null) {
                                firebaseAuth.signInAnonymously().addOnCompleteListener { finishAuth() }
                            } else {
                                val u = firebaseAuth.currentUser
                                if (u != null) {
                                    val updates = UserProfileChangeRequest.Builder()
                                        .setDisplayName(googleDisplayName)
                                        .build()
                                    u.updateProfile(updates)
                                }
                                finishAuth()
                            }
                        }
                    }
                } else {
                    isLoading = false
                    val msg = "ไม่พบข้อมูลบัญชี Google กรุณาลองใหม่อีกครั้ง"
                    errorMessage = msg
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            } catch (e: ApiException) {
                isLoading = false
                Log.e("GoogleAuth", "Google sign-in ApiException code: ${e.statusCode}, message: ${e.message}", e)
                if (e.statusCode == 10) {
                    showSha1Dialog = true
                }
                val friendlyMsg = when (e.statusCode) {
                    10 -> "Google Sign-In ต้องการ SHA-1 ใน Firebase Console (Code 10: DEVELOPER_ERROR)"
                    12500 -> "การยืนยันตัวตน Google ไม่สำเร็จ (Code 12500: SIGN_IN_FAILED)"
                    12501, 16 -> "ยกเลิกการเลือกบัญชี Google"
                    7 -> "เครือข่ายขัดข้อง กรุณาตรวจสอบอินเทอร์เน็ต (Code 7)"
                    else -> "เข้าสู่ระบบด้วย Google ไม่สำเร็จ (รหัสสถานะ: ${e.statusCode}) ${e.localizedMessage ?: ""}"
                }
                errorMessage = friendlyMsg
                Toast.makeText(context, friendlyMsg, Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                isLoading = false
                Log.e("GoogleAuth", "Google sign-in unexpected exception: ${e.localizedMessage}", e)
                val msg = "เกิดข้อผิดพลาด: ${e.localizedMessage}"
                errorMessage = msg
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    val dailyTheme = remember { profileDailyHeaderThemeForDay() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(KidTheme.Background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 112.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (onDismiss != null) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        TextButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "ย้อนกลับ",
                                tint = KidTheme.TextDark,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ย้อนกลับ",
                                color = KidTheme.TextDark,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            item {
                // Clean app account icon
                Surface(
                    modifier = Modifier.size(64.dp),
                    shape = RoundedCornerShape(18.dp),
                    color = dailyTheme.accentColor.copy(alpha = 0.12f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = null,
                            tint = dailyTheme.accentColor,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = if (isSignUpMode) "สร้างบัญชีใหม่" else "เข้าสู่ระบบ",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = KidTheme.TextDark,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = if (isSignUpMode) {
                        "สมัครสมาชิกเพื่อสำรองดาวและประวัติการเรียนไว้บนคลาวด์"
                    } else {
                        "เข้าสู่ระบบเพื่อซิงค์ดาว คะแนน และเหรียญรางวัลของคุณ"
                    },
                    fontSize = 13.sp,
                    color = KidTheme.TextMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, KidTheme.CardBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Mode Switcher (Login vs Register)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF1F3F7))
                                .padding(3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clickable {
                                        isSignUpMode = false
                                        errorMessage = null
                                    },
                                shape = RoundedCornerShape(9.dp),
                                color = if (!isSignUpMode) Color.White else Color.Transparent,
                                shadowElevation = if (!isSignUpMode) 1.dp else 0.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "เข้าสู่ระบบ",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.5.sp,
                                        color = if (!isSignUpMode) KidTheme.TextDark else KidTheme.TextMuted
                                    )
                                }
                            }

                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clickable {
                                        isSignUpMode = true
                                        errorMessage = null
                                    },
                                shape = RoundedCornerShape(9.dp),
                                color = if (isSignUpMode) Color.White else Color.Transparent,
                                shadowElevation = if (isSignUpMode) 1.dp else 0.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "สมัครสมาชิก",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.5.sp,
                                        color = if (isSignUpMode) KidTheme.TextDark else KidTheme.TextMuted
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (isSignUpMode) {
                            OutlinedTextField(
                                value = displayName,
                                onValueChange = { displayName = it },
                                label = { Text("ชื่อเล่น") },
                                placeholder = { Text("เช่น น้องพอใจ", color = Color(0xFF9CA3AF)) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        tint = KidTheme.TextMuted
                                    )
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = dailyTheme.accentColor,
                                    unfocusedBorderColor = Color(0xFFDDE3EC),
                                    focusedLabelColor = dailyTheme.accentColor
                                )
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it.trim() },
                            label = { Text("อีเมล") },
                            placeholder = { Text("name@example.com", color = Color(0xFF9CA3AF)) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Email,
                                    contentDescription = null,
                                    tint = KidTheme.TextMuted
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = dailyTheme.accentColor,
                                unfocusedBorderColor = Color(0xFFDDE3EC),
                                focusedLabelColor = dailyTheme.accentColor
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("รหัสผ่าน") },
                            placeholder = { Text("อย่างน้อย 6 ตัวอักษร", color = Color(0xFF9CA3AF)) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = KidTheme.TextMuted
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (isPasswordVisible) "ซ่อนรหัสผ่าน" else "แสดงรหัสผ่าน",
                                        tint = Color(0xFF9CA3AF)
                                    )
                                }
                            },
                            singleLine = true,
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = dailyTheme.accentColor,
                                unfocusedBorderColor = Color(0xFFDDE3EC),
                                focusedLabelColor = dailyTheme.accentColor
                            )
                        )

                        if (errorMessage != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFFFF1F2),
                                border = BorderStroke(1.dp, Color(0xFFFFCDD2)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = errorMessage!!,
                                    color = Color(0xFFC62828),
                                    fontSize = 12.5.sp,
                                    lineHeight = 18.sp,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = {
                                if (email.isBlank() || password.isBlank()) {
                                    errorMessage = "กรุณากรอกอีเมลและรหัสผ่านให้ครบถ้วน"
                                    return@Button
                                }
                                if (password.length < 6) {
                                    errorMessage = "รหัสผ่านต้องมีอย่างน้อย 6 ตัวอักษร"
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
                                        if (supabaseResult.isFailure) {
                                            val err = supabaseResult.exceptionOrNull()?.localizedMessage ?: "ลงทะเบียนไม่สำเร็จ"
                                            errorMessage = if (err.contains("already registered", ignoreCase = true) || err.contains("user_already_exists", ignoreCase = true)) {
                                                "อีเมลนี้ถูกลงทะเบียนไว้แล้ว กรุณาเข้าสู่ระบบ"
                                            } else {
                                                err
                                            }
                                            isLoading = false
                                            return@launch
                                        }

                                        val spUser = supabaseResult.getOrThrow()
                                        val dataMgr = SupabaseDataManager.getInstance(context)

                                        val currentLocalStars = dataMgr.getUserTotalStars()
                                        val currentLocalLevel = dataMgr.getUserCurrentLevel()
                                        dataMgr.upsertUserProfile(
                                            UserProfile(
                                                id = spUser.id,
                                                email = spUser.email ?: email,
                                                displayName = finalName,
                                                totalStars = currentLocalStars,
                                                currentLevel = currentLocalLevel
                                            )
                                        )

                                        val androidId = try {
                                            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
                                        } catch (e: Exception) {
                                            null
                                        } ?: UUID.randomUUID().toString()

                                        val appVersion = try {
                                            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0.0"
                                        } catch (e: Exception) {
                                            "1.0.0"
                                        }
                                        dataMgr.registerUserDevice(
                                            userId = spUser.id,
                                            deviceId = androidId,
                                            appVersion = appVersion
                                        )

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
                                                onSupabaseSuccess(spUser)
                                                isLoading = false
                                                Toast.makeText(context, "สมัครสมาชิกเรียบร้อยแล้ว", Toast.LENGTH_SHORT).show()
                                            }
                                            .addOnFailureListener {
                                                firebaseAuth.signInWithEmailAndPassword(email, password)
                                                    .addOnSuccessListener {
                                                        onSupabaseSuccess(spUser)
                                                        isLoading = false
                                                        Toast.makeText(context, "สมัครสมาชิกเรียบร้อยแล้ว", Toast.LENGTH_SHORT).show()
                                                    }
                                                    .addOnFailureListener {
                                                        firebaseAuth.signInAnonymously().addOnCompleteListener {
                                                            onSupabaseSuccess(spUser)
                                                            isLoading = false
                                                            Toast.makeText(context, "สมัครสมาชิกเรียบร้อยแล้ว", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                            }
                                    } else {
                                        val bundle = Bundle().apply { putString("button_name", "dual_login_submit") }
                                        analytics.logEvent("select_content", bundle)

                                        // 1. Supabase Sign In
                                        val supabaseResult = supabaseAuthManager.signIn(email, password)
                                        if (supabaseResult.isFailure) {
                                            val err = supabaseResult.exceptionOrNull()?.localizedMessage ?: "เข้าสู่ระบบไม่สำเร็จ"
                                            errorMessage = if (err.contains("invalid", ignoreCase = true) || err.contains("credentials", ignoreCase = true)) {
                                                "อีเมลหรือรหัสผ่านไม่ถูกต้อง"
                                            } else {
                                                err
                                            }
                                            isLoading = false
                                            return@launch
                                        }

                                        val spUser = supabaseResult.getOrThrow()
                                        val dataMgr = SupabaseDataManager.getInstance(context)

                                        val androidId = try {
                                            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
                                        } catch (e: Exception) {
                                            null
                                        } ?: UUID.randomUUID().toString()

                                        val appVersion = try {
                                            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0.0"
                                        } catch (e: Exception) {
                                            "1.0.0"
                                        }
                                        dataMgr.registerUserDevice(
                                            userId = spUser.id,
                                            deviceId = androidId,
                                            appVersion = appVersion
                                        )

                                        // 2. Firebase Sign In
                                        if (firebaseAuth.currentUser?.isAnonymous == true) {
                                            firebaseAuth.signOut()
                                        }
                                        firebaseAuth.signInWithEmailAndPassword(email, password)
                                            .addOnSuccessListener {
                                                onSupabaseSuccess(spUser)
                                                isLoading = false
                                                Toast.makeText(context, "เข้าสู่ระบบเรียบร้อยแล้ว", Toast.LENGTH_SHORT).show()
                                            }
                                            .addOnFailureListener {
                                                firebaseAuth.signInAnonymously().addOnCompleteListener {
                                                    onSupabaseSuccess(spUser)
                                                    isLoading = false
                                                    Toast.makeText(context, "เข้าสู่ระบบเรียบร้อยแล้ว", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = dailyTheme.accentColor),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Text(
                                    text = if (isSignUpMode) "สมัครสมาชิก" else "เข้าสู่ระบบ",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HorizontalDivider(modifier = Modifier.weight(1f), color = KidTheme.DividerColor)
                            Text(
                                text = "หรือ",
                                fontSize = 12.sp,
                                color = KidTheme.TextMuted,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                            HorizontalDivider(modifier = Modifier.weight(1f), color = KidTheme.DividerColor)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Google Sign-In Button
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clickable(enabled = !isLoading) {
                                    isLoading = true
                                    errorMessage = null
                                    try {
                                        googleSignInLauncher.launch(googleSignInClient.signInIntent)
                                    } catch (e: Exception) {
                                        isLoading = false
                                        Log.e("GoogleAuth", "Failed to launch Google Sign-In intent", e)
                                        errorMessage = "ไม่สามารถเปิดหน้าต่าง Google ได้: ${e.localizedMessage}"
                                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                                    }
                                },
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, Color(0xFFD5DCE6))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Image(
                                    painter = painterResource(id = com.pongploydev.profilefeature.R.drawable.ic_google_logo),
                                    contentDescription = "Google",
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = if (isSignUpMode) "ดำเนินการต่อด้วย Google" else "เข้าสู่ระบบด้วย Google",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = KidTheme.TextDark
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        TextButton(
                            enabled = !isLoading,
                            onClick = {
                                isLoading = true
                                errorMessage = null

                                scope.launch {
                                    val guestName = supabaseAuthManager.getOrCreateGuestDisplayName()
                                    val dataMgr = SupabaseDataManager.getInstance(context)
                                    dataMgr.saveUserProgress(
                                        stars = dataMgr.getUserTotalStars(),
                                        level = dataMgr.getUserCurrentLevel(),
                                        displayName = guestName
                                    )

                                    val supabaseResult = supabaseAuthManager.signInAnonymously()
                                    supabaseResult.onSuccess { spUser ->
                                        val androidId = try {
                                            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
                                        } catch (e: Exception) {
                                            null
                                        } ?: UUID.randomUUID().toString()

                                        val appVersion = try {
                                            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0.0"
                                        } catch (e: Exception) {
                                            "1.0.0"
                                        }
                                        dataMgr.upsertUserProfile(
                                            UserProfile(
                                                id = spUser.id,
                                                email = null,
                                                displayName = guestName,
                                                avatarUrl = dataMgr.getLocalAvatarUrl() ?: "",
                                                role = "student",
                                                totalStars = dataMgr.getUserTotalStars(),
                                                totalCoins = 0,
                                                currentLevel = dataMgr.getUserCurrentLevel(),
                                                streakDays = dataMgr.getUserStreakDays()
                                            )
                                        )
                                        dataMgr.registerUserDevice(
                                            userId = spUser.id,
                                            deviceId = androidId,
                                            appVersion = appVersion
                                        )
                                        onSupabaseSuccess(spUser)
                                    }

                                    firebaseAuth.signInAnonymously()
                                        .addOnSuccessListener { authRes ->
                                            authRes.user?.updateProfile(
                                                UserProfileChangeRequest.Builder()
                                                    .setDisplayName(guestName)
                                                    .build()
                                            )
                                            isLoading = false
                                            Toast.makeText(context, "เข้าใช้งานแบบทดลองเล่นแล้ว", Toast.LENGTH_SHORT).show()
                                        }
                                        .addOnFailureListener { e ->
                                            isLoading = false
                                            if (supabaseResult.isSuccess) {
                                                Toast.makeText(context, "เข้าใช้งานแบบทดลองเล่นแล้ว", Toast.LENGTH_SHORT).show()
                                            } else {
                                                errorMessage = e.localizedMessage ?: "เข้าใช้งานแบบทดลองเล่นไม่สำเร็จ"
                                            }
                                        }
                                }
                            }
                        ) {
                            Text(
                                text = "ใช้งานแบบไม่ลงทะเบียน",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = KidTheme.TextMuted
                            )
                        }
                    }
                }
            }
        }

        if (showSha1Dialog) {
            val clipboard = LocalClipboardManager.current
            val debugSha1 = "36:50:D3:6C:9C:B3:41:EB:FC:19:2B:F5:49:AC:99:BD:CA:88:39:8B"
            AlertDialog(
                onDismissRequest = { showSha1Dialog = false },
                shape = RoundedCornerShape(20.dp),
                title = {
                    Text(
                        text = "ต้องเพิ่ม SHA-1 ใน Firebase",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = KidTheme.TextDark
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Google Play Services แจ้งเตือน Developer Error (Code 10) เนื่องจากยังไม่ได้บันทึก SHA-1 ของแอปใน Firebase Console:",
                            fontSize = 13.sp,
                            color = KidTheme.TextMuted
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF1F5F9),
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                        ) {
                            Text(
                                text = debugSha1,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = KidTheme.Primary,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "วิธีแก้:\n1. ไปที่ Firebase Console > Project Settings > General > Your apps (${context.packageName})\n2. กด 'Add fingerprint' แล้ววาง SHA-1 นี้\n3. เปิดใช้งาน Google ในเมนู Authentication > Sign-in method",
                            fontSize = 12.sp,
                            color = KidTheme.TextMuted,
                            lineHeight = 17.sp
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            clipboard.setText(AnnotatedString(debugSha1))
                            Toast.makeText(context, "คัดลอก SHA-1 แล้ว", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = dailyTheme.accentColor)
                    ) {
                        Text("คัดลอก SHA-1", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSha1Dialog = false }) {
                        Text("ปิด", color = KidTheme.TextMuted)
                    }
                }
            )
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
    onOpenAuth: () -> Unit,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dataManager = remember { SupabaseDataManager.getInstance(context) }

    val userId = supabaseUser?.id ?: firebaseUser?.uid ?: supabaseAuthManager.getGuestUserId()

    var currentDayOfWeek by remember {
        mutableIntStateOf(Calendar.getInstance()[Calendar.DAY_OF_WEEK])
    }
    val dailyTheme = remember(currentDayOfWeek) {
        profileDailyHeaderThemeForDay(currentDayOfWeek)
    }

    var profileData by remember { mutableStateOf<UserProfile?>(null) }
    var quizResults by remember { mutableStateOf<List<UserQuizResult>>(emptyList()) }
    var achievements by remember { mutableStateOf<List<UserAchievement>>(emptyList()) }
    var localNameOverride by remember { mutableStateOf<String?>(null) }

    var isDataLoading by remember { mutableStateOf(false) }
    var isUploadingAvatar by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showEditNameDialog by remember { mutableStateOf(false) }
    var showQuizHistoryDetail by remember { mutableStateOf(false) }
    var showSubscriptionDialog by remember { mutableStateOf(false) }
    var appSettings by remember { mutableStateOf(dataManager.getCachedAppSettings()) }

    val isGuestAccount = (firebaseUser == null || firebaseUser.isAnonymous) &&
        (supabaseUser == null || supabaseUser.isAnonymous == true || supabaseUser.email.isNullOrBlank())
    val isVip = dataManager.isVipSubscriber()
    val vipTier = dataManager.getSubscriptionTier()

    val appVersionName = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0.0"
        } catch (e: Exception) {
            "1.0.0"
        }
    }

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
            dataManager.fetchAppSettings().onSuccess { settings ->
                appSettings = settings
            }
            isDataLoading = false
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                isUploadingAvatar = true
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val originalBitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream?.close()

                    if (originalBitmap != null) {
                        val maxDimension = 512
                        val scale = minOf(
                            maxDimension.toFloat() / originalBitmap.width,
                            maxDimension.toFloat() / originalBitmap.height
                        ).coerceAtMost(1.0f)

                        val scaledWidth = (originalBitmap.width * scale).toInt()
                        val scaledHeight = (originalBitmap.height * scale).toInt()
                        val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, scaledWidth, scaledHeight, true)

                        val outputStream = java.io.ByteArrayOutputStream()
                        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
                        val imageBytes = outputStream.toByteArray()

                        dataManager.uploadAvatarImage(userId, imageBytes)
                            .onSuccess { newUrl ->
                                profileData = (profileData ?: UserProfile(id = userId)).copy(avatarUrl = newUrl)
                                loadSupabaseLiveData()
                                Toast.makeText(context, "อัปเดตรูปโปรไฟล์แล้ว", Toast.LENGTH_SHORT).show()
                            }
                            .onFailure { e ->
                                Log.e("ProfileComposeScreen", "uploadAvatarImage failed: ${e.localizedMessage}", e)
                                Toast.makeText(context, "อัปโหลดรูปไม่สำเร็จ", Toast.LENGTH_SHORT).show()
                            }
                    }
                } catch (e: Exception) {
                    Log.e("ProfileComposeScreen", "Error reading image file: ${e.localizedMessage}", e)
                    Toast.makeText(context, "ไม่สามารถอ่านไฟล์รูปได้", Toast.LENGTH_SHORT).show()
                } finally {
                    isUploadingAvatar = false
                }
            }
        }
    }

    val rawDisplayName = localNameOverride
        ?: profileData?.displayName?.takeIf { it.isNotBlank() && it != "นักอ่านตัวน้อย" && !it.startsWith("ผู้ใช้ชั่วคราว") }
        ?: dataManager.getLocalDisplayName()?.takeIf { it.isNotBlank() && !it.startsWith("guest", ignoreCase = true) }
        ?: supabaseUser?.userMetadata?.displayName?.takeIf { it.isNotBlank() && !it.startsWith("ผู้ใช้ชั่วคราว") }
        ?: firebaseUser?.displayName?.takeIf { it.isNotBlank() }

    val displayName = remember(rawDisplayName) { formatProfileDisplayName(rawDisplayName) }
    var newName by remember(displayName) { mutableStateOf(if (displayName == "น้องคนเก่ง") "" else displayName) }

    val emailText = firebaseUser?.email ?: supabaseUser?.email ?: profileData?.email ?: "ยังไม่ได้เชื่อมต่อบัญชี"

    val localStars = dataManager.getUserTotalStars()
    val localLevel = dataManager.getUserCurrentLevel()
    val localStreak = dataManager.getUserStreakDays()
    val cloudStars = profileData?.totalStars ?: 0
    val cloudLevel = profileData?.currentLevel ?: 1
    val cloudStreak = profileData?.streakDays ?: 0

    val totalStars = maxOf(localStars, cloudStars)
    val currentLevel = maxOf(localLevel, cloudLevel, 1 + (totalStars / 50))
    val streakDays = maxOf(localStreak, cloudStreak)
    val totalQuizzes = quizResults.size
    val totalAchievements = achievements.size

    val starsInLevel = totalStars % 50
    val starsNeeded = 50 - starsInLevel
    val levelProgress = (starsInLevel.toFloat() / 50f).coerceIn(0f, 1f)
    val animatedLevelProgress by animateFloatAsState(
        targetValue = levelProgress,
        animationSpec = tween(durationMillis = 600),
        label = "profileLevelProgress"
    )
    val levelTitle = when (currentLevel) {
        1 -> "นักอ่านตัวน้อย"
        2 -> "นักสะสมดาว"
        3 -> "ยอดนักอ่าน"
        4 -> "ผู้พิชิตคำศัพท์"
        5 -> "ยอดอัจฉริยะตัวน้อย"
        else -> "ปรมาจารย์ตัวน้อย"
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, userId) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME || event == Lifecycle.Event.ON_START) {
                currentDayOfWeek = Calendar.getInstance()[Calendar.DAY_OF_WEEK]
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
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 118.dp)
        ) {
            // ==================== HEADER ====================
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                        .background(dailyTheme.headerGradient)
                        .padding(top = 12.dp, bottom = 26.dp, start = 20.dp, end = 20.dp)
                ) {
                    IconButton(
                        onClick = { loadSupabaseLiveData() },
                        enabled = !isDataLoading,
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        if (isDataLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "รีเฟรชข้อมูล",
                                tint = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.size(21.dp)
                            )
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                    ) {
                        // Avatar
                        Box(contentAlignment = Alignment.BottomEnd) {
                            Surface(
                                modifier = Modifier
                                    .size(88.dp)
                                    .clickable { imagePickerLauncher.launch("image/*") },
                                shape = CircleShape,
                                color = Color.White,
                                border = BorderStroke(3.dp, Color.White.copy(alpha = 0.9f)),
                                shadowElevation = 4.dp
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color(0xFFF1F4F9)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val avatarUrl = profileData?.avatarUrl?.takeIf { it.isNotBlank() }
                                        ?: dataManager.getLocalAvatarUrl()?.takeIf { it.isNotBlank() }
                                    if (isUploadingAvatar) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(28.dp),
                                            strokeWidth = 2.5.dp,
                                            color = dailyTheme.accentColor
                                        )
                                    } else if (!avatarUrl.isNullOrBlank()) {
                                        AsyncImage(
                                            model = avatarUrl,
                                            contentDescription = "รูปโปรไฟล์",
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Rounded.Person,
                                            contentDescription = "รูปโปรไฟล์",
                                            tint = dailyTheme.accentColor.copy(alpha = 0.8f),
                                            modifier = Modifier.size(48.dp)
                                        )
                                    }
                                }
                            }

                            Surface(
                                shape = CircleShape,
                                color = dailyTheme.accentColor,
                                border = BorderStroke(2.dp, Color.White),
                                modifier = Modifier
                                    .size(28.dp)
                                    .clickable { imagePickerLauncher.launch("image/*") }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.PhotoCamera,
                                        contentDescription = "เปลี่ยนรูปโปรไฟล์",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Display name + edit button
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = displayName,
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "แก้ไขชื่อ",
                                tint = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier
                                    .size(17.dp)
                                    .clickable {
                                        newName = if (displayName == "น้องคนเก่ง") "" else displayName
                                        showEditNameDialog = true
                                    }
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Single clean subtitle line
                        val subtitleText = buildString {
                            append("ระดับ $currentLevel · $levelTitle")
                            if (isVip) {
                                append(" · VIP")
                            } else if (streakDays > 0) {
                                append(" · ต่อเนื่อง $streakDays วัน")
                            }
                        }
                        Text(
                            text = subtitleText,
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.88f)
                        )
                    }
                }
            }

            // ==================== MAIN CONTENT ====================
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Unified Learning Summary Card (Stats + Level Progress)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, KidTheme.CardBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(vertical = 16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ProfileStatColumn(
                                    value = "$totalStars",
                                    label = "ดาวสะสม",
                                    valueColor = Color(0xFFE67E00),
                                    modifier = Modifier.weight(1f),
                                    onClick = { switchToAppTab(context, 0) }
                                )
                                VerticalDivider(
                                    modifier = Modifier.height(32.dp),
                                    color = KidTheme.DividerColor
                                )
                                ProfileStatColumn(
                                    value = "Lv.$currentLevel",
                                    label = "เลเวล",
                                    valueColor = Color(0xFF1F8A5B),
                                    modifier = Modifier.weight(1f),
                                    onClick = { switchToAppTab(context, 0) }
                                )
                                VerticalDivider(
                                    modifier = Modifier.height(32.dp),
                                    color = KidTheme.DividerColor
                                )
                                ProfileStatColumn(
                                    value = "$totalQuizzes",
                                    label = "ควิซที่ผ่าน",
                                    valueColor = Color(0xFF1976D2),
                                    modifier = Modifier.weight(1f),
                                    onClick = { showQuizHistoryDetail = true }
                                )
                                VerticalDivider(
                                    modifier = Modifier.height(32.dp),
                                    color = KidTheme.DividerColor
                                )
                                ProfileStatColumn(
                                    value = "$totalAchievements",
                                    label = "เหรียญตรา",
                                    valueColor = Color(0xFF7B1FA2),
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider(
                                color = KidTheme.DividerColor,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "อีก $starsNeeded ดาว ขึ้นระดับ ${currentLevel + 1}",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = KidTheme.TextDark
                                    )
                                    Text(
                                        text = "$starsInLevel / 50",
                                        fontSize = 12.sp,
                                        color = KidTheme.TextMuted
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(7.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFFECEFF4))
                                ) {
                                    if (animatedLevelProgress > 0f) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(animatedLevelProgress.coerceAtLeast(0.04f))
                                                .fillMaxHeight()
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(KidTheme.GoldGradient)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Guest Sign-In Prompt (subtle & clean)
                    if (isGuestAccount) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenAuth() },
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, KidTheme.CardBorder),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 13.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = dailyTheme.accentColor.copy(alpha = 0.10f),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Rounded.CloudUpload,
                                            contentDescription = null,
                                            tint = dailyTheme.accentColor,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "เข้าสู่ระบบเพื่อสำรองข้อมูล",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = KidTheme.TextDark
                                    )
                                    Text(
                                        text = "เก็บดาวและประวัติการเรียนไว้ใช้ได้ทุกเครื่อง",
                                        fontSize = 12.sp,
                                        color = KidTheme.TextMuted
                                    )
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = Color(0xFF9CA3AF)
                                )
                            }
                        }
                    }

                    // VIP Membership Row (only when enabled)
                    if (appSettings?.isSubscriptionEnabled == true) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showSubscriptionDialog = true },
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(
                                1.dp,
                                if (isVip) Color(0xFFFFD54F) else KidTheme.CardBorder
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 13.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isVip) Color(0xFFFFF3CD) else Color(0xFFF3E8FF),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Rounded.WorkspacePremium,
                                            contentDescription = null,
                                            tint = if (isVip) Color(0xFFD97706) else Color(0xFF7C3AED),
                                            modifier = Modifier.size(21.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isVip) "สมาชิก VIP ($vipTier)" else "สมาชิก VIP",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = KidTheme.TextDark
                                    )
                                    Text(
                                        text = if (isVip) "ไร้โฆษณาและรับดาวโบนัส x2" else "ไม่มีโฆษณา พร้อมโบนัสดาว x2",
                                        fontSize = 12.sp,
                                        color = KidTheme.TextMuted
                                    )
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = Color(0xFF9CA3AF)
                                )
                            }
                        }
                    }

                    // ==================== QUICK SHORTCUTS (2x2 COMPACT GRID) ====================
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "กิจกรรมการเรียนรู้",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = KidTheme.TextDark,
                            modifier = Modifier.padding(horizontal = 2.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            QuickShortcutTile(
                                icon = Icons.Rounded.Quiz,
                                iconTint = Color(0xFFE65100),
                                iconBg = Color(0xFFFFF0E6),
                                title = "แบบทดสอบ",
                                subtitle = "ทำควิซสะสมดาว",
                                modifier = Modifier.weight(1f),
                                onClick = { switchToAppTab(context, 3) }
                            )
                            QuickShortcutTile(
                                icon = Icons.AutoMirrored.Rounded.MenuBook,
                                iconTint = Color(0xFF1F8A5B),
                                iconBg = Color(0xFFEAF7F0),
                                title = "ก-ฮ / A-Z",
                                subtitle = "ฝึกอ่านและคัดเขียน",
                                modifier = Modifier.weight(1f),
                                onClick = { switchToAppTab(context, 0) }
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            QuickShortcutTile(
                                icon = Icons.Rounded.SportsEsports,
                                iconTint = Color(0xFF6D28D9),
                                iconBg = Color(0xFFF3EEFF),
                                title = "เกมฝึกสมอง",
                                subtitle = "เล่นเกมและระบายสี",
                                modifier = Modifier.weight(1f),
                                onClick = { switchToAppTab(context, 4) }
                            )
                            QuickShortcutTile(
                                icon = Icons.Rounded.PlayCircleOutline,
                                iconTint = Color(0xFF0284C7),
                                iconBg = Color(0xFFEBF6FE),
                                title = "วิดีโอเรียนรู้",
                                subtitle = "การ์ตูนและบทเรียน",
                                modifier = Modifier.weight(1f),
                                onClick = { switchToAppTab(context, 2) }
                            )
                        }
                    }

                    // ==================== REAL ACHIEVEMENTS (HONEST, NO FAKE CHIPS) ====================
                    if (achievements.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "เหรียญรางวัล (${achievements.size})",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = KidTheme.TextDark,
                                modifier = Modifier.padding(horizontal = 2.dp)
                            )

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, KidTheme.CardBorder),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                FlowRow(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    achievements.forEach { ach ->
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = Color(0xFFFFFBF0),
                                            border = BorderStroke(1.dp, Color(0xFFFFE082))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(text = ach.badgeIcon, fontSize = 16.sp)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = ach.badgeName,
                                                    fontSize = 12.5.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = KidTheme.TextDark
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ==================== ACCOUNT & SETTINGS ====================
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "บัญชีและการตั้งค่า",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = KidTheme.TextDark,
                            modifier = Modifier.padding(horizontal = 2.dp)
                        )

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, KidTheme.CardBorder),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                ProfileOptionItem(
                                    icon = Icons.Rounded.History,
                                    title = "ประวัติการทำควิซ",
                                    subtitle = if (totalQuizzes > 0) "ผ่านแล้ว $totalQuizzes รายการ" else "ยังไม่มีประวัติการทำควิซ",
                                    onClick = { showQuizHistoryDetail = true }
                                )

                                HorizontalDivider(
                                    color = KidTheme.DividerColor,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )

                                ProfileOptionItem(
                                    icon = Icons.Rounded.MailOutline,
                                    title = "บัญชีอีเมล",
                                    subtitle = emailText,
                                    onClick = {
                                        if (isGuestAccount) onOpenAuth()
                                    }
                                )

                                if (achievements.isEmpty()) {
                                    HorizontalDivider(
                                        color = KidTheme.DividerColor,
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                    ProfileOptionItem(
                                        icon = Icons.Rounded.EmojiEvents,
                                        title = "เหรียญรางวัล",
                                        subtitle = "ทำแบบทดสอบเพื่อปลดล็อกเหรียญรางวัล",
                                        onClick = { switchToAppTab(context, 3) }
                                    )
                                }

                                HorizontalDivider(
                                    color = KidTheme.DividerColor,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )

                                ProfileOptionItem(
                                    icon = Icons.Rounded.Share,
                                    title = "แชร์แอปให้เพื่อน",
                                    subtitle = "ก ไก่ ABC หรรษา",
                                    onClick = {
                                        try {
                                            val appPackageName = context.packageName
                                            val playStoreUrl = "https://play.google.com/store/apps/details?id=$appPackageName"
                                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                type = "text/plain"
                                                putExtra(Intent.EXTRA_SUBJECT, "ก ไก่ ABC หรรษา - ฝึกเขียนอ่านระบายสี")
                                                putExtra(
                                                    Intent.EXTRA_TEXT,
                                                    "มาเรียนรู้และฝึกเขียน ก ไก่ / A-Z กับแอป ก ไก่ ABC หรรษา กันเถอะ\n$playStoreUrl"
                                                )
                                            }
                                            context.startActivity(Intent.createChooser(shareIntent, "แชร์แอป"))
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "ไม่สามารถเปิดเมนูแชร์ได้", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )

                                HorizontalDivider(
                                    color = KidTheme.DividerColor,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )

                                ProfileOptionItem(
                                    icon = Icons.Rounded.PrivacyTip,
                                    title = "นโยบายความเป็นส่วนตัว",
                                    subtitle = "Privacy Policy",
                                    onClick = {
                                        val url = appSettings?.privacyPolicyUrl?.takeIf { it.isNotBlank() }
                                            ?: "https://pongploydev.github.io/privacy-policy/"
                                        try {
                                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "ไม่สามารถเปิดลิงก์ได้", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )

                                HorizontalDivider(
                                    color = KidTheme.DividerColor,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )

                                ProfileOptionItem(
                                    icon = Icons.Rounded.Description,
                                    title = "เงื่อนไขการใช้งาน",
                                    subtitle = "Terms of Service",
                                    onClick = {
                                        val url = appSettings?.termsOfServiceUrl?.takeIf { it.isNotBlank() }
                                            ?: "https://pongploydev.github.io/terms/"
                                        try {
                                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "ไม่สามารถเปิดลิงก์ได้", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )
                            }
                        }
                    }

                    // ==================== BOTTOM ACTION ====================
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (isGuestAccount) {
                            Button(
                                onClick = { onOpenAuth() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = dailyTheme.accentColor)
                            ) {
                                Text(
                                    text = "เข้าสู่ระบบ / สมัครสมาชิก",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            TextButton(onClick = { showLogoutDialog = true }) {
                                Text(
                                    text = "รีเซ็ตข้อมูลทดลองเล่น",
                                    fontSize = 13.sp,
                                    color = KidTheme.TextMuted
                                )
                            }
                        } else {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .clickable { showLogoutDialog = true },
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFFFFF1F2),
                                border = BorderStroke(1.dp, Color(0xFFFFCDD2))
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                        contentDescription = null,
                                        tint = Color(0xFFD32F2F),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "ออกจากระบบ",
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFFD32F2F)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "เวอร์ชัน $appVersionName",
                            fontSize = 11.5.sp,
                            color = Color(0xFF9CA3AF)
                        )
                    }
                }
            }
        }
    }

    // ==================== DIALOGS ====================

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = if (isGuestAccount) "รีเซ็ตข้อมูลทดลองเล่น" else "ออกจากระบบ",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (isGuestAccount) {
                        "ข้อมูลที่ยังไม่ได้ผูกบัญชีจะถูกล้างออก ต้องการดำเนินการต่อหรือไม่?"
                    } else {
                        "คุณต้องการออกจากระบบใช่หรือไม่?"
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onSignOut()
                        Toast.makeText(
                            context,
                            if (isGuestAccount) "รีเซ็ตข้อมูลเรียบร้อยแล้ว" else "ออกจากระบบแล้ว",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text(if (isGuestAccount) "รีเซ็ต" else "ออกจากระบบ")
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
            shape = RoundedCornerShape(20.dp),
            title = { Text("แก้ไขชื่อแสดงผล", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("ชื่อเล่น") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = newName.trim()
                        if (trimmed.isNotBlank()) {
                            localNameOverride = trimmed
                            dataManager.saveUserProgress(
                                stars = totalStars,
                                level = currentLevel,
                                streakDays = streakDays,
                                displayName = trimmed
                            )
                            scope.launch {
                                dataManager.updateUserProfile(
                                    userId,
                                    com.pongploydev.profilefeature.data.UserProfileUpdate(displayName = trimmed)
                                )
                                loadSupabaseLiveData()

                                firebaseUser?.let { fbUser ->
                                    val updates = UserProfileChangeRequest.Builder().setDisplayName(trimmed).build()
                                    fbUser.updateProfile(updates)
                                }
                                showEditNameDialog = false
                                Toast.makeText(context, "บันทึกชื่อเรียบร้อยแล้ว", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = dailyTheme.accentColor)
                ) { Text("บันทึก") }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) { Text("ยกเลิก") }
            }
        )
    }

    if (showSubscriptionDialog && appSettings?.isSubscriptionEnabled == true) {
        SubscriptionDialog(
            onDismiss = { showSubscriptionDialog = false },
            onSubscribed = {
                showSubscriptionDialog = false
            }
        )
    }
}

// ==================== CLEAN PROFILE SUB-COMPONENTS ====================

@Composable
private fun ProfileStatColumn(
    value: String,
    label: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            fontSize = 11.5.sp,
            color = KidTheme.TextMuted,
            maxLines = 1
        )
    }
}

@Composable
private fun QuickShortcutTile(
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, KidTheme.CardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = iconBg,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = KidTheme.TextDark,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 11.5.sp,
                color = KidTheme.TextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
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
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF4B5563),
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Medium,
                color = KidTheme.TextDark
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = KidTheme.TextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = Color(0xFFD1D5DB),
            modifier = Modifier.size(20.dp)
        )
    }
}
