package com.pushprime.ui.screens.auth

import android.app.Activity
import android.util.Patterns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.pushprime.R
import com.pushprime.auth.AuthMode
import com.pushprime.auth.AuthViewModel
import com.pushprime.ui.components.Spacing

private enum class LoadingAction {
    None,
    Google,
    Email
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AuthScreen(
    authViewModel: AuthViewModel,
    onLoggedIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    // Validation states
    var emailTouched by remember { mutableStateOf(false) }
    var passwordTouched by remember { mutableStateOf(false) }
    var submittedOnce by remember { mutableStateOf(false) }
    var emailHadFocus by remember { mutableStateOf(false) }
    var passwordHadFocus by remember { mutableStateOf(false) }
    
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var authError by remember { mutableStateOf<String?>(null) }
    
    var loadingAction by remember { mutableStateOf(LoadingAction.None) }
    var passwordVisible by remember { mutableStateOf(false) }
    
    val viewModelLoading by authViewModel.isLoading.collectAsState()
    val viewModelAuthError by authViewModel.authError.collectAsState()
    val authMode by authViewModel.authMode.collectAsState()
    val isLoading = viewModelLoading || loadingAction != LoadingAction.None
    val onLoggedInState = rememberUpdatedState(onLoggedIn)
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            onLoggedInState.value()
        }
    }

    val googleClientId = remember { context.getString(R.string.default_web_client_id).trim() }
    val isGoogleConfigured = googleClientId.isNotBlank() &&
        !googleClientId.startsWith("REPLACE_", ignoreCase = true)

    fun mapAuthError(error: Throwable): String {
        return when (error) {
            is FirebaseAuthInvalidCredentialsException -> "Invalid email or password."
            is FirebaseAuthWeakPasswordException -> "Password is too weak (min 6 characters)."
            is FirebaseAuthUserCollisionException -> "Account already exists. Try signing in."
            is FirebaseAuthException -> {
                when (error.errorCode) {
                    "ERROR_OPERATION_NOT_ALLOWED" -> "Email/password sign-in is disabled."
                    "ERROR_USER_DISABLED" -> "This account has been disabled."
                    "ERROR_USER_NOT_FOUND" -> "No account found for this email."
                    else -> error.message ?: "Sign-in failed."
                }
            }
            is ApiException -> {
                when (error.statusCode) {
                    12501 -> "Google sign-in was cancelled."
                    10 -> "Google sign-in misconfigured. Check SHA-1 and web client ID."
                    else -> error.message ?: "Google sign-in failed."
                }
            }
            else -> error.message ?: "Something went wrong."
        }
    }

    fun validateFields(): Boolean {
        val isEmailValid = email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
        emailError = if (!isEmailValid) "Enter a valid email address" else null
        
        val passwordMessage = when {
            password.isBlank() -> "Password is required"
            authMode == AuthMode.SIGN_UP && password.length < 6 -> "Password must be at least 6 characters"
            else -> null
        }
        passwordError = passwordMessage
        
        return emailError == null && passwordError == null
    }

    // Effect to clear errors when fields change
    LaunchedEffect(email) { if (emailTouched || submittedOnce) validateFields() }
    LaunchedEffect(password) { if (passwordTouched || submittedOnce) validateFields() }

    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) {
            authError = "Google sign-in was cancelled."
            loadingAction = LoadingAction.None
            return@rememberLauncherForActivityResult
        }
        val data = result.data
        if (data == null) {
            authError = "Google sign-in failed. Please try again."
            loadingAction = LoadingAction.None
            return@rememberLauncherForActivityResult
        }
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(ApiException::class.java)
            val token = account.idToken
            if (token.isNullOrBlank()) {
                authError = "Google sign-in failed. Please try again."
                loadingAction = LoadingAction.None
            } else {
                authViewModel.signInWithGoogle(token) { signInResult ->
                    loadingAction = LoadingAction.None
                    signInResult.onFailure { error -> authError = mapAuthError(error) }
                }
            }
        } catch (e: Exception) {
            authError = mapAuthError(e)
            loadingAction = LoadingAction.None
        }
    }

    fun startGoogleSignIn() {
        if (activity == null) {
            authError = "Unable to start Google sign-in."
            loadingAction = LoadingAction.None
            return
        }
        if (!isGoogleConfigured) {
            authError = "Google sign-in is not configured. Update default_web_client_id."
            loadingAction = LoadingAction.None
            return
        }
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(googleClientId)
            .requestEmail()
            .build()
        val googleClient = GoogleSignIn.getClient(activity, signInOptions)
        googleLauncher.launch(googleClient.signInIntent)
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Error Banner
            val displayError = authError ?: viewModelAuthError
            AnimatedVisibility(
                visible = displayError != null,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(horizontal = Spacing.md, vertical = Spacing.lg)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = MaterialTheme.shapes.small,
                    tonalElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(Spacing.md),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(Spacing.sm))
                        Text(
                            text = displayError ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = {
                            authError = null
                            authViewModel.clearAuthError()
                        }) {
                            Icon(Icons.Default.VisibilityOff, contentDescription = "Close", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = Spacing.xl),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "RAMBOOST",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(Spacing.xl))

                // Title & Subtitle with Animation
                AnimatedContent(
                    targetState = authMode == AuthMode.SIGN_UP,
                    transitionSpec = { fadeIn() with fadeOut() }
                ) { targetIsCreate ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (targetIsCreate) "Create Account" else "Welcome Back",
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(Spacing.sm))
                        Text(
                            text = if (targetIsCreate) 
                                "Join the RAMBOOST mission today" 
                            else "Sign in to resume your mission",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }


                Spacer(modifier = Modifier.height(32.dp))

                // Form
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            authViewModel.clearAuthError()
                        },
                        label = { Text("Email") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged {
                                if (it.isFocused) {
                                    emailHadFocus = true
                                } else if (emailHadFocus) {
                                    emailTouched = true
                                }
                            },
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        isError = emailError != null && (emailTouched || submittedOnce),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Black,
                            focusedLabelColor = Color.Black,
                            cursorColor = Color.Black
                        )
                    )
                    if (emailError != null && (emailTouched || submittedOnce)) {
                        Text(
                            emailError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(Spacing.md))
                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                authViewModel.clearAuthError()
                            },
                            label = { Text("Password") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged {
                                    if (it.isFocused) {
                                        passwordHadFocus = true
                                    } else if (passwordHadFocus) {
                                        passwordTouched = true
                                    }
                                },
                            shape = RoundedCornerShape(16.dp),
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null
                                    )
                                }
                            },
                            singleLine = true,
                            isError = passwordError != null && (passwordTouched || submittedOnce),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Black,
                                focusedLabelColor = Color.Black,
                                cursorColor = Color.Black
                            )
                        )
                        if (passwordError != null && (passwordTouched || submittedOnce)) {
                            Text(
                                passwordError!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }

                        if (authMode == AuthMode.SIGN_IN) {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                                TextButton(onClick = { /* Forgot password logic */ }) {
                                    Text("Forgot password?")
                                }
                            }
                        }

                    Spacer(modifier = Modifier.height(Spacing.lg))

                    Button(
                        onClick = {
                            authError = null
                            authViewModel.clearAuthError()
                            submittedOnce = true
                            if (validateFields()) {
                                loadingAction = LoadingAction.Email
                                authViewModel.signInWithEmail(email, password) { result ->
                                    loadingAction = LoadingAction.None
                                    result.onFailure { authError = mapAuthError(it) }
                                }
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                    ) {
                        if (loadingAction == LoadingAction.Email) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text(
                                text = if (authMode == AuthMode.SIGN_UP) "Create Account" else "Sign In",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.dp)
                            .background(Color.LightGray)
                    )
                    Text("or", modifier = Modifier.padding(horizontal = 16.dp), color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.dp)
                            .background(Color.LightGray)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Google Button
                OutlinedButton(
                    onClick = {
                        authError = null
                        authViewModel.clearAuthError()
                        loadingAction = LoadingAction.Google
                        startGoogleSignIn()
                    },
                    enabled = !isLoading && isGoogleConfigured,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
                ) {
                    if (loadingAction == LoadingAction.Google) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black, strokeWidth = 2.dp)
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(painter = painterResource(id = R.drawable.ic_google), contentDescription = null, tint = Color.Unspecified, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Continue with Google", color = Color.Black, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Switch Mode
                TextButton(
                    onClick = { 
                        authViewModel.toggleAuthMode()
                        submittedOnce = false
                        emailTouched = false
                        passwordTouched = false
                        emailHadFocus = false
                        passwordHadFocus = false
                        emailError = null
                        passwordError = null
                        authError = null
                        authViewModel.clearAuthError()
                    },
                    enabled = !isLoading
                ) {
                    Text(
                        text = if (authMode == AuthMode.SIGN_UP) "Already have an account? Sign In" else "New to RAMBOOST? Create Account",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
