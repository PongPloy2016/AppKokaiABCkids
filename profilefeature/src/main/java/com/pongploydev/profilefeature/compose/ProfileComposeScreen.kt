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
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
    val GoldGradient = Brush.horizontalGradient(
        listOf(Color(0xFFFFD166), Color(0xFFFF9F43))
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
                    supabaseAuthManager.signOut()
                    try {
                        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
                        GoogleSignIn.getClient(context, gso).signOut()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
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
            return@rememberLauncherForActivityResult
        }

        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            if (account != null) {
                val googleEmail = account.email ?: ""
                val googleDisplayName = account.displayName ?: account.givenName ?: "เพื่อนตัวน้อย Google"
                val googleId = account.id ?: java.util.UUID.randomUUID().toString()
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
                        } ?: java.util.UUID.randomUUID().toString()

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF7F4FF),
                        Color(0xFFFFFBF2),
                        Color(0xFFF2FBF6)
                    )
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (onDismiss != null) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        TextButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "ย้อนกลับ",
                                tint = KidTheme.Primary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "กลับไปหน้าหลัก",
                                color = KidTheme.Primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            item {
                // Cute Kid Character Header Badge
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFFF9F43).copy(alpha = 0.25f),
                                    KidTheme.Primary.copy(alpha = 0.25f),
                                    Color(0xFF00B894).copy(alpha = 0.25f)
                                )
                            )
                        )
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = if (isSignUpMode) {
                                        listOf(Color(0xFFFF9F43), Color(0xFFFF7675))
                                    } else {
                                        listOf(KidTheme.Primary, Color(0xFF818CF8))
                                    }
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isSignUpMode) "🎒" else "🎨",
                            fontSize = 44.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Friendly Kid-Oriented Headlines
                Text(
                    text = if (isSignUpMode) "สมัครเป็นนักเรียนตัวน้อย 🎒✨" else "ยินดีต้อนรับเพื่อนตัวน้อย! 🎈",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = KidTheme.TextDark,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = if (isSignUpMode) "สมัครสมาชิกเพื่อสะสมดาวความดี ⭐ และเก็บรูปวาดสวยๆ ไว้อวดทุกคน"
                           else "เข้าสู่ระบบเพื่อสะสมดาว ⭐ เก็บรูปวาดระบายสี 🎨 และเล่นเกม ก-ฮ แสนสนุก!",
                    fontSize = 13.sp,
                    color = KidTheme.TextMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                // Kid-Friendly Mini Feature Pills
                Row(
                    modifier = Modifier.padding(bottom = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFFF8E1),
                        border = BorderStroke(1.dp, Color(0xFFFFE082))
                    ) {
                        Text(
                            text = "⭐ สะสมดาว",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB78103),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFE8F5E9),
                        border = BorderStroke(1.dp, Color(0xFFA5D6A7))
                    ) {
                        Text(
                            text = "🎨 บันทึกรูปวาด",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFEDE7F6),
                        border = BorderStroke(1.dp, Color(0xFFD1C4E9))
                    ) {
                        Text(
                            text = "🏆 ชิงเหรียญรางวัล",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF5E35B1),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFECEFF5)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Mode Switcher (Login vs Register)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .clip(RoundedCornerShape(23.dp))
                                .background(Color(0xFFF1F4F9))
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
                                shape = RoundedCornerShape(20.dp),
                                color = if (!isSignUpMode) KidTheme.Primary else Color.Transparent,
                                shadowElevation = if (!isSignUpMode) 3.dp else 0.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "เข้าสู่ระบบ 🎒",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (!isSignUpMode) Color.White else KidTheme.TextMuted
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
                                shape = RoundedCornerShape(20.dp),
                                color = if (isSignUpMode) Color(0xFFFF7675) else Color.Transparent,
                                shadowElevation = if (isSignUpMode) 3.dp else 0.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "สมัครสมาชิก 🌟",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (isSignUpMode) Color.White else KidTheme.TextMuted
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        if (isSignUpMode) {
                            OutlinedTextField(
                                value = displayName,
                                onValueChange = { displayName = it },
                                label = { Text("ชื่อเล่นของน้อง 👧👦") },
                                placeholder = { Text("เช่น น้องพอใจ, น้องต้นกล้า", color = Color(0xFFAAAAAA)) },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFFFF7675)) },
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFFF7675),
                                    unfocusedBorderColor = Color(0xFFE2E8F0),
                                    focusedLabelColor = Color(0xFFFF7675)
                                )
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                        }

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it.trim() },
                            label = { Text("อีเมล (ของผู้ปกครองหรือน้อง) 📧") },
                            placeholder = { Text("เช่น parent@gmail.com", color = Color(0xFFAAAAAA)) },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = KidTheme.Primary) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = KidTheme.Primary,
                                unfocusedBorderColor = Color(0xFFE2E8F0),
                                focusedLabelColor = KidTheme.Primary
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("รหัสผ่านลับ 🔒") },
                            placeholder = { Text("อย่างน้อย 6 ตัวอักษร", color = Color(0xFFAAAAAA)) },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = KidTheme.Primary) },
                            trailingIcon = {
                                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (isPasswordVisible) "ซ่อนรหัสผ่าน" else "แสดงรหัสผ่าน",
                                        tint = if (isPasswordVisible) KidTheme.Primary else Color(0xFF9E9E9E)
                                    )
                                }
                            },
                            singleLine = true,
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = KidTheme.Primary,
                                unfocusedBorderColor = Color(0xFFE2E8F0),
                                focusedLabelColor = KidTheme.Primary
                            )
                        )

                        if (errorMessage != null) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFFFFF0F1),
                                border = BorderStroke(1.dp, Color(0xFFFFCCD1)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "💡",
                                        fontSize = 16.sp,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    Text(
                                        text = errorMessage!!,
                                        color = Color(0xFFD32F2F),
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                if (email.isBlank() || password.isBlank()) {
                                    errorMessage = "อย่าลืมกรอกอีเมลและรหัสผ่านให้ครบถ้วนนะจ๊ะ 😊"
                                    return@Button
                                }
                                if (password.length < 6) {
                                    errorMessage = "รหัสผ่านต้องมีความยาวอย่างน้อย 6 ตัวอักษรจ้ะ 🔑"
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
                                                "อีเมลนี้เคยลงทะเบียนไว้แล้วจ้ะ ลองกดเข้าสู่ระบบดูนะ 💡"
                                            } else {
                                                err
                                            }
                                            isLoading = false
                                            return@launch
                                        }

                                        val spUser = supabaseResult.getOrThrow()
                                        val dataMgr = SupabaseDataManager.getInstance(context)

                                        // Save profile to Supabase
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

                                        // Register user device in Supabase user_devices
                                        val androidId = try {
                                            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
                                        } catch (e: Exception) {
                                            null
                                        } ?: java.util.UUID.randomUUID().toString()

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
                                                Toast.makeText(context, "ยินดีต้อนรับเพื่อนตัวน้อย สมัครสมาชิกสำเร็จแล้วจ้า! 🎒🎉", Toast.LENGTH_SHORT).show()
                                            }
                                            .addOnFailureListener { e ->
                                                // Fallback 1: try Firebase sign-in if account already exists
                                                firebaseAuth.signInWithEmailAndPassword(email, password)
                                                    .addOnSuccessListener {
                                                        onSupabaseSuccess(spUser)
                                                        isLoading = false
                                                        Toast.makeText(context, "ยินดีต้อนรับเพื่อนตัวน้อย สมัครสมาชิกสำเร็จแล้วจ้า! 🎒🎉", Toast.LENGTH_SHORT).show()
                                                    }
                                                    .addOnFailureListener {
                                                        // Fallback 2: Keep Firebase Active via anonymous sign in
                                                        firebaseAuth.signInAnonymously().addOnCompleteListener {
                                                            onSupabaseSuccess(spUser)
                                                            isLoading = false
                                                            Toast.makeText(context, "ยินดีต้อนรับเพื่อนตัวน้อย สมัครสมาชิกสำเร็จแล้วจ้า! 🎒🎉", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                            }
                                    } else {
                                        val bundle = Bundle().apply { putString("button_name", "dual_login_submit") }
                                        analytics.logEvent("select_content", bundle)

                                        // 1. Supabase Sign In
                                        val supabaseResult = supabaseAuthManager.signIn(email, password)
                                        if (supabaseResult.isFailure) {
                                            val err = supabaseResult.exceptionOrNull()?.localizedMessage ?: "เข้าสู่ระบบไม่สำเร็จ กรุณาตรวจสอบอีเมลหรือรหัสผ่าน"
                                            errorMessage = if (err.contains("invalid", ignoreCase = true) || err.contains("credentials", ignoreCase = true)) {
                                                "อีเมลหรือรหัสผ่านไม่ถูกต้องจ้ะ ลองตรวจสอบดูอีกครั้งนะ 🎈"
                                            } else {
                                                err
                                            }
                                            isLoading = false
                                            return@launch
                                        }

                                        val spUser = supabaseResult.getOrThrow()
                                        val dataMgr = SupabaseDataManager.getInstance(context)

                                        // Register device upon login
                                        val androidId = try {
                                            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
                                        } catch (e: Exception) {
                                            null
                                        } ?: java.util.UUID.randomUUID().toString()

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
                                                Toast.makeText(context, "ยินดีต้อนรับกลับมาจ้า เข้าสู่ระบบสำเร็จแล้ว! 🚀⭐", Toast.LENGTH_SHORT).show()
                                            }
                                            .addOnFailureListener { e ->
                                                firebaseAuth.signInAnonymously().addOnCompleteListener {
                                                    onSupabaseSuccess(spUser)
                                                    isLoading = false
                                                    Toast.makeText(context, "ยินดีต้อนรับกลับมาจ้า เข้าสู่ระบบสำเร็จแล้ว! 🚀⭐", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(26.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSignUpMode) Color(0xFFFF7675) else KidTheme.Primary
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 1.dp),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Text(
                                    text = if (isSignUpMode) "เริ่มการผจญภัยกันเลย! 🎒🎉" else "เข้าสู่ห้องเรียนแสนสนุก 🚀⭐",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Divider: หรือ 🌟
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(1.dp)
                                    .background(Color(0xFFE2E8F0))
                            )
                            Text(
                                text = "หรือ 🌟",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = KidTheme.TextMuted,
                                modifier = Modifier.padding(horizontal = 10.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(1.dp)
                                    .background(Color(0xFFE2E8F0))
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Google (Gmail) Sign-In Button
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
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
                            shape = RoundedCornerShape(25.dp),
                            color = Color.White,
                            border = BorderStroke(1.5.dp, Color(0xFFD8E2EC)),
                            shadowElevation = 2.dp
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Image(
                                    painter = painterResource(id = com.pongploydev.profilefeature.R.drawable.ic_google_logo),
                                    contentDescription = "Google Logo",
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = if (isSignUpMode) "สมัครด้วยบัญชี Google (Gmail) 🌐" else "เข้าสู่ระบบด้วย Google (Gmail) 🌐",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF374151)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

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
                                            val androidId = try {
                                                Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
                                            } catch (e: Exception) {
                                                null
                                            } ?: java.util.UUID.randomUUID().toString()

                                            val appVersion = try {
                                                context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0.0"
                                            } catch (e: Exception) {
                                                "1.0.0"
                                            }
                                            val dataMgr = SupabaseDataManager.getInstance(context)
                                            dataMgr.registerUserDevice(
                                                userId = spUser.id,
                                                deviceId = androidId,
                                                appVersion = appVersion
                                            )
                                            onSupabaseSuccess(spUser)
                                        }

                                        // Firebase Guest Sign In
                                        firebaseAuth.signInAnonymously()
                                            .addOnSuccessListener {
                                                isLoading = false
                                                Toast.makeText(context, "เข้าเล่นเป็นแขกตัวน้อยเรียบร้อย สนุกกับการเรียนรู้นะจ๊ะ 🎈", Toast.LENGTH_SHORT).show()
                                            }
                                            .addOnFailureListener { e ->
                                                isLoading = false
                                                if (supabaseResult.isSuccess) {
                                                    Toast.makeText(context, "เข้าเล่นเป็นแขกตัวน้อยเรียบร้อย สนุกกับการเรียนรู้นะจ๊ะ 🎈", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    errorMessage = e.localizedMessage ?: "เข้าใช้งานแบบ Guest ไม่สำเร็จ"
                                                }
                                            }
                                    }
                                },
                            shape = RoundedCornerShape(24.dp),
                            color = Color(0xFFF1FDF5),
                            border = BorderStroke(1.5.dp, Color(0xFFA5D6A7))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "🎈 เล่นแบบไม่ลงทะเบียน (ทดลองเล่นฟรี)",
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

        if (showSha1Dialog) {
            val clipboard = LocalClipboardManager.current
            val debugSha1 = "36:50:D3:6C:9C:B3:41:EB:FC:19:2B:F5:49:AC:99:BD:CA:88:39:8B"
            AlertDialog(
                onDismissRequest = { showSha1Dialog = false },
                title = {
                    Text(
                        text = "⚙️ ต้องการเพิ่ม SHA-1 ใน Firebase",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = KidTheme.TextDark
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Google Play Services แจ้งเตือน Developer Error (Code 10) เนื่องจากยังไม่ได้บันทึกค่า Fingerprint SHA-1 ของแอปใน Firebase Console:",
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
                            text = "วิธีแก้:\n1. ไปที่ Firebase Console > Project Settings > General > Your apps (com.pongploydev.priyankvex.paintapp)\n2. กดปุ่ม 'Add fingerprint' แล้ววาง SHA-1 นี้\n3. เปิดใช้งาน Google ในเมนู Authentication > Sign-in method",
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
                            Toast.makeText(context, "คัดลอก SHA-1 เรียบร้อยแล้ว! 📋", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = KidTheme.Primary)
                    ) {
                        Text("คัดลอก SHA-1 📋", color = Color.White)
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

    val userId = supabaseUser?.id ?: firebaseUser?.uid ?: "guest_user"

    var profileData by remember { mutableStateOf<UserProfile?>(null) }
    var quizResults by remember { mutableStateOf<List<UserQuizResult>>(emptyList()) }
    var achievements by remember { mutableStateOf<List<UserAchievement>>(emptyList()) }

    var isDataLoading by remember { mutableStateOf(false) }
    var isUploadingAvatar by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showEditNameDialog by remember { mutableStateOf(false) }
    var showQuizHistoryDetail by remember { mutableStateOf(false) }
    var showSubscriptionDialog by remember { mutableStateOf(false) }

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

    // Image Picker Launcher for Supabase Storage Upload
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        Log.d("ProfileComposeScreen", "Selected photo Uri: $uri")
        if (uri != null) {
            scope.launch {
                isUploadingAvatar = true
                Toast.makeText(context, "กำลังอัปโหลดรูปโปรไฟล์เข้า Supabase... ☁️", Toast.LENGTH_SHORT).show()
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val originalBitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream?.close()

                    if (originalBitmap != null) {
                        Log.d("ProfileComposeScreen", "Loaded bitmap size: ${originalBitmap.width}x${originalBitmap.height}")
                        val maxDimension = 512
                        val scale = Math.min(
                            maxDimension.toFloat() / originalBitmap.width,
                            maxDimension.toFloat() / originalBitmap.height
                        ).coerceAtMost(1.0f)

                        val scaledWidth = (originalBitmap.width * scale).toInt()
                        val scaledHeight = (originalBitmap.height * scale).toInt()
                        val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, scaledWidth, scaledHeight, true)

                        val outputStream = java.io.ByteArrayOutputStream()
                        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
                        val imageBytes = outputStream.toByteArray()
                        Log.d("ProfileComposeScreen", "Compressed image bytes: ${imageBytes.size} bytes. Calling uploadAvatarImage...")

                        val uploadResult = dataManager.uploadAvatarImage(userId, imageBytes)
                        uploadResult.onSuccess { newUrl ->
                            Log.d("ProfileComposeScreen", "uploadAvatarImage succeeded! New URL: $newUrl")
                            profileData = (profileData ?: UserProfile(id = userId)).copy(avatarUrl = newUrl)
                            loadSupabaseLiveData()
                            Toast.makeText(context, "อัปโหลดรูปโปรไฟล์เข้า Supabase สำเร็จแล้ว! 📸", Toast.LENGTH_SHORT).show()
                        }.onFailure { e ->
                            Log.e("ProfileComposeScreen", "uploadAvatarImage failed: ${e.localizedMessage}", e)
                            Toast.makeText(context, "อัปโหลดรูปไม่สำเร็จ: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Log.e("ProfileComposeScreen", "Failed to decode bitmap from Uri: $uri")
                    }
                } catch (e: Exception) {
                    Log.e("ProfileComposeScreen", "Error reading image file: ${e.localizedMessage}", e)
                    Toast.makeText(context, "เกิดข้อผิดพลาดในการอ่านไฟล์รูป", Toast.LENGTH_SHORT).show()
                } finally {
                    isUploadingAvatar = false
                }
            }
        } else {
            Log.d("ProfileComposeScreen", "Photo selection cancelled by user.")
        }
    }

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
                        // Avatar Container with Photo Picker & Camera Overlay
                        Box(contentAlignment = Alignment.BottomEnd) {
                            Surface(
                                modifier = Modifier
                                    .size(96.dp)
                                    .clickable { imagePickerLauncher.launch("image/*") },
                                shape = CircleShape,
                                color = Color.White,
                                border = BorderStroke(4.dp, Color(0xFFFFD166)),
                                shadowElevation = 12.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    val avatarUrl = profileData?.avatarUrl
                                    if (isUploadingAvatar) {
                                        CircularProgressIndicator(modifier = Modifier.size(36.dp), strokeWidth = 3.dp, color = Color(0xFF536DFE))
                                    } else if (!avatarUrl.isNullOrBlank()) {
                                        AsyncImage(
                                            model = avatarUrl,
                                            contentDescription = "Profile Avatar",
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = "Profile Avatar",
                                            tint = Color(0xFF536DFE),
                                            modifier = Modifier.size(60.dp)
                                        )
                                    }
                                }
                            }

                            // Camera Upload Icon Overlay
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF536DFE),
                                border = BorderStroke(2.dp, Color.White),
                                shadowElevation = 6.dp,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clickable { imagePickerLauncher.launch("image/*") }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.PhotoCamera,
                                        contentDescription = "เปลี่ยนรูปโปรไฟล์",
                                        tint = Color.White,
                                        modifier = Modifier.size(17.dp)
                                    )
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
                    // Guest / Supabase Off Call-to-Action Banner
                    if (firebaseUser?.isAnonymous == true || supabaseUser == null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                                .clickable { onOpenAuth() },
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                            border = BorderStroke(1.5.dp, Color(0xFFFFCA28)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🚀", fontSize = 28.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "เข้าสู่ระบบ / ลงทะเบียน Supabase",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp,
                                        color = Color(0xFF5D4037)
                                    )
                                    Text(
                                        text = "บันทึกผลการเรียนรู้, ดาว และเหรียญตราไว้บนคลาวด์ถาวร",
                                        fontSize = 11.sp,
                                        color = Color(0xFF795548)
                                    )
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "ไปหน้าลงทะเบียน",
                                    tint = Color(0xFF5D4037)
                                )
                            }
                        }
                    }

                    // VIP MEMBER / SUBSCRIPTION BANNER CARD
                    val isVip = dataManager.isVipSubscriber()
                    val vipTier = dataManager.getSubscriptionTier()
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .clickable { showSubscriptionDialog = true },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isVip) Color(0xFFFFF8E1) else Color(0xFFF3E5F5)
                        ),
                        border = BorderStroke(
                            1.5.dp,
                            if (isVip) Color(0xFFFFB300) else Color(0xFFAB47BC)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .background(
                                            if (isVip) Color(0xFFFFB300) else Color(0xFF8E44AD),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = if (isVip) "👑" else "💎", fontSize = 22.sp)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = if (isVip) "สมาชิก VIP: $vipTier" else "สนับสนุน สมาชิก (Subscriptions)",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 13.5.sp,
                                            color = if (isVip) Color(0xFFE65100) else Color(0xFF4A148C)
                                        )
                                        if (isVip) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(text = "👑", fontSize = 12.sp)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (isVip) "สิทธิพิเศษ VIP ทำงานอยู่ • ไร้โฆษณา • ดาว x2" else "ปลดล็อก VIP ไร้โฆษณา • ดาวโบนัส x2 • ครบทุกเกม",
                                        fontSize = 11.sp,
                                        color = if (isVip) Color(0xFF795548) else Color(0xFF6A1B9A)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isVip) Color(0xFFFFB300) else Color(0xFF8E44AD)
                            ) {
                                Text(
                                    text = if (isVip) "ดูสิทธิ VIP" else "สนับสนุน 💎",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

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

                    val localStars = dataManager.getUserTotalStars()
                    val localLevel = dataManager.getUserCurrentLevel()
                    val cloudStars = profileData?.totalStars ?: 0
                    val cloudLevel = profileData?.currentLevel ?: 1

                    val totalStars = maxOf(localStars, cloudStars)
                    val currentLevel = maxOf(localLevel, cloudLevel, 1 + (totalStars / 50))
                    val totalQuizzes = quizResults.size
                    val totalAchievements = achievements.size

                    val starsInLevel = totalStars % 50
                    val starsNeeded = 50 - starsInLevel
                    val levelProgress = (starsInLevel.toFloat() / 50f).coerceIn(0f, 1f)
                    val levelTitle = when (currentLevel) {
                        1 -> "นักอ่านตัวน้อย"
                        2 -> "นักสะสมดาว"
                        3 -> "ยอดนักอ่าน"
                        4 -> "ผู้พิชิตคำศัพท์"
                        5 -> "ยอดอัจฉริยะตัวน้อย"
                        else -> "ปรมาจารย์ตัวน้อย"
                    }

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

                    Spacer(modifier = Modifier.height(14.dp))

                    // LEVEL PROGRESS BAR (Direct reference matching Home screen)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "🎖️", fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "ระดับ $currentLevel • $levelTitle",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = KidTheme.TextDark
                                    )
                                }
                                Text(
                                    text = "อีก $starsNeeded ดาว จะขึ้นระดับ ${currentLevel + 1} 🚀",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = KidTheme.Primary
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFF1F5F9))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(levelProgress)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(KidTheme.GoldGradient)
                                    )
                            }
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
                                try {
                                    val appPackageName = context.packageName
                                    val playStoreUrl = "https://play.google.com/store/apps/details?id=$appPackageName"
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_SUBJECT, "แชร์แอปฝึกเขียน ก ไก่")
                                        putExtra(
                                            Intent.EXTRA_TEXT,
                                            "มาเรียนรู้และฝึกเขียน ก ไก่ / A-Z กับแอป Paint App for Kids กันเถอะ! 🎨\nดาวน์โหลดได้ที่: $playStoreUrl"
                                        )
                                    }
                                    val chooserIntent = Intent.createChooser(shareIntent, "แชร์แอปไปที่")
                                    context.startActivity(chooserIntent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "ไม่สามารถเปิดเมนูแชร์ได้", Toast.LENGTH_SHORT).show()
                                }
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

    if (showSubscriptionDialog) {
        SubscriptionDialog(
            onDismiss = { showSubscriptionDialog = false },
            onSubscribed = {
                showSubscriptionDialog = false
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
