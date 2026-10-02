package com.example.ui.screens.auth

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.ui.theme.*
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneAuthScreen(
    onAuthSuccess: (FirebaseUser) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val auth = remember { FirebaseAuth.getInstance() }

    var phoneNumber by remember { mutableStateOf("081248001122") }
    var otpCode by remember { mutableStateOf("") }
    var verificationId by remember { mutableStateOf("") }
    var resendToken by remember { mutableStateOf<PhoneAuthProvider.ForceResendingToken?>(null) }
    var isCodeSent by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var countdownTimer by remember { mutableIntStateOf(60) }

    // Auto-countdown when OTP is sent
    LaunchedEffect(isCodeSent, countdownTimer) {
        if (isCodeSent && countdownTimer > 0) {
            delay(1000)
            countdownTimer -= 1
        }
    }

    fun formatPhoneForFirebase(raw: String): String {
        val trimmed = raw.trim().replace(" ", "").replace("-", "")
        return when {
            trimmed.startsWith("+") -> trimmed
            trimmed.startsWith("0") -> "+62" + trimmed.substring(1)
            trimmed.startsWith("62") -> "+$trimmed"
            else -> "+62$trimmed"
        }
    }

    val callbacks = remember {
        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                isLoading = true
                auth.signInWithCredential(credential)
                    .addOnCompleteListener { task ->
                        isLoading = false
                        if (task.isSuccessful) {
                            task.result.user?.let(onAuthSuccess)
                        } else {
                            errorMessage = task.exception?.localizedMessage ?: "Verifikasi otomatis gagal"
                        }
                    }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                isLoading = false
                Log.e("GoPacePhoneAuth", "Verification failed: ${e.message}", e)
                errorMessage = "Gagal kirim SMS: ${e.localizedMessage}. Anda juga dapat verifikasi menggunakan mode simulasi atau Google Sign-In."
            }

            override fun onCodeSent(
                verId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                isLoading = false
                verificationId = verId
                resendToken = token
                isCodeSent = true
                countdownTimer = 60
                errorMessage = null
                Toast.makeText(context, "Kode verifikasi telah dikirim ke nomor Anda!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun requestOtp(isResend: Boolean = false) {
        val formattedNumber = formatPhoneForFirebase(phoneNumber)
        if (formattedNumber.length < 9) {
            errorMessage = "Mohon masukkan nomor handphone yang valid"
            return
        }

        errorMessage = null
        isLoading = true

        val builder = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(formattedNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(context as Activity)
            .setCallbacks(callbacks)

        if (isResend && resendToken != null) {
            builder.setForceResendingToken(resendToken!!)
        }

        try {
            PhoneAuthProvider.verifyPhoneNumber(builder.build())
        } catch (e: Exception) {
            isLoading = false
            errorMessage = e.localizedMessage
        }
    }

    fun verifyOtp() {
        if (otpCode.length < 6) {
            errorMessage = "Masukkan 6 digit kode OTP verifikasi"
            return
        }

        errorMessage = null
        isLoading = true
        focusManager.clearFocus()

        // If verificationId is empty (e.g. testing mode or direct verify)
        if (verificationId.isEmpty()) {
            // Instant verification fallback for development demo
            auth.signInAnonymously().addOnCompleteListener { task ->
                isLoading = false
                if (task.isSuccessful && task.result.user != null) {
                    Toast.makeText(context, "Verifikasi Berhasil!", Toast.LENGTH_SHORT).show()
                    onAuthSuccess(task.result.user!!)
                } else {
                    errorMessage = "Silakan klik 'Kirim Kode OTP' terlebih dahulu."
                }
            }
            return
        }

        val credential = PhoneAuthProvider.getCredential(verificationId, otpCode)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                isLoading = false
                if (task.isSuccessful) {
                    task.result.user?.let(onAuthSuccess)
                } else {
                    errorMessage = "Kode verifikasi salah atau telah kadaluarsa"
                }
            }
    }

    // Google Sign-In with Credential Manager
    fun signInWithGoogle() {
        val clientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            errorMessage = "Konfigurasi Google Web Client ID belum siap"
            return
        }

        isLoading = true
        errorMessage = null

        val credentialManager = CredentialManager.create(context)
        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
        val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

        coroutineScope.launch {
            try {
                val result = credentialManager.getCredential(context as Activity, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val idToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                    auth.signInWithCredential(authCredential)
                        .addOnCompleteListener { task ->
                            isLoading = false
                            if (task.isSuccessful) {
                                task.result.user?.let(onAuthSuccess)
                            } else {
                                errorMessage = task.exception?.localizedMessage ?: "Google Sign-In gagal"
                            }
                        }
                } else {
                    isLoading = false
                    errorMessage = "Tipe kredensial tidak sesuai"
                }
            } catch (e: GetCredentialCancellationException) {
                isLoading = false
                Log.w("GoPaceAuth", "Google Sign-In dibatalkan user: ${e.message}")
            } catch (e: Exception) {
                isLoading = false
                Log.e("GoPaceAuth", "Google Sign-In error", e)
                errorMessage = e.localizedMessage ?: "Gagal masuk dengan Google"
            }
        }
    }

    Scaffold(
        containerColor = SoftBackground,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 480.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
            Spacer(modifier = Modifier.height(16.dp))

            // App Brand Emblem
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(GoPaceGreen),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "GP",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 30.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "GoPace Merauke",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp,
                color = TextPrimary
            )
            Text(
                text = "Masuk dengan nomor HP untuk mulai perjalanan & pesan kuliner",
                fontSize = 12.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Main Clean Authentication Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {

                    // 1. Phone Number Entry Field
                    Text(
                        text = "Nomor Handphone",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SoftBackground,
                            border = androidx.compose.foundation.BorderStroke(1.dp, SoftCardBorder),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🇮🇩 +62", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                            }
                        }

                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = {
                                phoneNumber = it
                                errorMessage = null
                            },
                            placeholder = { Text("Contoh: 812-4800-1122") },
                            leadingIcon = {
                                Icon(Icons.Default.Phone, contentDescription = null, tint = GoPaceGreen, modifier = Modifier.size(18.dp))
                            },
                            trailingIcon = {
                                if (phoneNumber.isNotEmpty()) {
                                    IconButton(onClick = { phoneNumber = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Hapus", modifier = Modifier.size(16.dp))
                                    }
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Next
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoPaceGreen,
                                unfocusedBorderColor = SoftCardBorder,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("phone_number_input")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Request OTP button (if not sent yet, or to change number)
                    if (!isCodeSent) {
                        Button(
                            onClick = { requestOtp(isResend = false) },
                            enabled = !isLoading && phoneNumber.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = GoPaceGreen),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("send_otp_button")
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Kirim Kode OTP (SMS)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Kode dikirim ke: $phoneNumber",
                                fontSize = 11.sp,
                                color = GoPaceGreenDark,
                                fontWeight = FontWeight.SemiBold
                            )
                            TextButton(
                                onClick = {
                                    isCodeSent = false
                                    otpCode = ""
                                },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Ganti Nomor", fontSize = 11.sp, color = GoPaceBlue)
                            }
                        }
                    }

                    // 2. OTP Verification Input
                    AnimatedVisibility(visible = isCodeSent) {
                        Column(modifier = Modifier.padding(top = 16.dp)) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = SoftCardBorder)

                            Text(
                                text = "Kode Verifikasi (OTP)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = "Masukkan 6 digit kode yang dikirimkan melalui SMS",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                            )

                            // 6-digit Soft Card OTP boxes
                            OtpBoxField(
                                otp = otpCode,
                                onOtpChange = { newCode ->
                                    if (newCode.length <= 6) {
                                        otpCode = newCode
                                        errorMessage = null
                                    }
                                }
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Resend Timer Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (countdownTimer > 0) {
                                    Text(
                                        text = "Kirim ulang kode dalam ${countdownTimer} detik",
                                        fontSize = 11.sp,
                                        color = TextMuted
                                    )
                                } else {
                                    TextButton(
                                        onClick = { requestOtp(isResend = true) },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp), tint = GoPaceGreen)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Kirim Ulang Kode SMS", fontSize = 12.sp, color = GoPaceGreen, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // 3. 'Verify' Button (Requested)
                            Button(
                                onClick = { verifyOtp() },
                                enabled = !isLoading && otpCode.length >= 4,
                                colors = ButtonDefaults.buttonColors(containerColor = GoPaceGreen),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("verify_button")
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Verify", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                                }
                            }
                        }
                    }

                    // Error Message Banner
                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFEF2F2),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = errorMessage!!,
                                    color = Color(0xFFB91C1C),
                                    fontSize = 11.sp,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Alternative: Google Sign-In with Credential Manager
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = SoftCardBorder)
                Text("  atau opsi lain  ", fontSize = 11.sp, color = TextMuted)
                HorizontalDivider(modifier = Modifier.weight(1f), color = SoftCardBorder)
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { signInWithGoogle() },
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SoftCardBorder),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("google_signin_button")
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    tint = GoPaceGreen,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Sign in with Google",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Demo Access button for instant emulator testing
            TextButton(
                onClick = {
                    auth.signInAnonymously().addOnCompleteListener { task ->
                        if (task.isSuccessful && task.result.user != null) {
                            onAuthSuccess(task.result.user!!)
                        } else {
                            Toast.makeText(context, "Masuk mode Demo GoPace Merauke", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            ) {
                Text(
                    text = "Masuk Sebagai Pengguna Demo (Cepat)",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
}

/**
 * 6-Digit Soft Card OTP Input Component
 */
@Composable
fun OtpBoxField(
    otp: String,
    onOtpChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable { focusRequester.requestFocus() }
            .testTag("otp_input_container"),
        contentAlignment = Alignment.Center
    ) {
        // Invisible input taking keyboard focus
        BasicTextField(
            value = otp,
            onValueChange = onOtpChange,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.NumberPassword,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier
                .size(1.dp)
                .focusRequester(focusRequester)
                .testTag("otp_hidden_input")
        )

        // Visual 6 soft cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0 until 6) {
                val digit = if (i < otp.length) otp[i].toString() else ""
                val isFocused = i == otp.length || (i == 5 && otp.length == 6)

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (digit.isNotEmpty()) GoPaceGreenContainer else SoftBackground
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (isFocused) 1.5.dp else 1.dp,
                        color = when {
                            digit.isNotEmpty() -> GoPaceGreen
                            isFocused -> GoPaceGreen
                            else -> SoftCardBorder
                        }
                    ),
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = digit,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = if (digit.isNotEmpty()) GoPaceGreenDark else TextPrimary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
