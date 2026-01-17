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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
import com.pushprime.ui.components.AppPrimaryButton
import com.pushprime.ui.components.AppSecondaryButton
import com.pushprime.ui.components.AppCard
import com.pushprime.ui.components.AppTextButton
import com.pushprime.ui.components.AppTextField
import com.pushprime.ui.components.PremiumFadeSlideIn
import com.pushprime.ui.theme.AppSpacing
import com.pushprime.ui.validation.FormValidation
import com.pushprime.ui.validation.rememberFormValidationState

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
    val focusManager = LocalFocusManager.current
    val passwordFocusRequester = remember { FocusRequester() }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    var authError by remember { mutableStateOf<String?>(null) }
    
    var loadingAction by remember { mutableStateOf(LoadingAction.None) }
    var passwordVisible by remember { mutableStateOf(false) }
    val validation = rememberFormValidationState()
    
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

    val trimmedEmail = FormValidation.trim(email)
    val isEmailInvalid = FormValidation.isBlank(trimmedEmail) ||
        !Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()
    val emailError = if (isEmailInvalid) "Enter a valid email address" else null

    val passwordError = when {
        FormValidation.isBlank(password) -> "Password is required"
        authMode == AuthMode.SIGN_UP && password.length < 6 -> "Password must be at least 6 characters"
        else -> null
    }
    val isFormValid = !isEmailInvalid && passwordError == null

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
                    .padding(horizontal = AppSpacing.md, vertical = AppSpacing.lg)
            ) {
                AppCard(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    contentPadding = PaddingValues(AppSpacing.md),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(AppSpacing.sm))
                        Text(
                            text = displayError ?: "",
                            style = MaterialTheme.typography.bodyMedium,
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

            PremiumFadeSlideIn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(AppSpacing.lg)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Top
                ) {
                    Spacer(modifier = Modifier.height(AppSpacing.xxl))
                    
                    Text(
                        text = "RAMBOOST",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(AppSpacing.xl))

                    // Title & Subtitle with Animation
                    AnimatedContent(
                        targetState = authMode == AuthMode.SIGN_UP,
                        transitionSpec = { fadeIn() with fadeOut() },
                        modifier = Modifier.fillMaxWidth()
                    ) { targetIsCreate ->
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                text = if (targetIsCreate) "Create Account" else "Welcome Back",
                                style = MaterialTheme.typography.headlineMedium,
                                textAlign = TextAlign.Start
                            )
                            Spacer(modifier = Modifier.height(AppSpacing.sm))
                            Text(
                                text = if (targetIsCreate) 
                                    "Join the RAMBOOST mission today" 
                                else "Sign in to resume your mission",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Start
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(AppSpacing.xxl))

                    // Form
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
                    ) {
                        AppTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                authViewModel.clearAuthError()
                            },
                            label = "Email",
                            modifier = Modifier.fillMaxWidth(),
                            required = true,
                            errorText = emailError,
                            showError = validation.shouldShowError("email"),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { passwordFocusRequester.requestFocus() }
                            ),
                            onFocusChanged = { state ->
                                if (!state.isFocused) {
                                    validation.markTouched("email")
                                }
                            }
                        )

                        AppTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                authViewModel.clearAuthError()
                            },
                            label = "Password",
                            modifier = Modifier.fillMaxWidth(),
                            required = true,
                            errorText = passwordError,
                            showError = validation.shouldShowError("password"),
                            visualTransformation = if (passwordVisible) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { focusManager.clearFocus() }
                            ),
                            focusRequester = passwordFocusRequester,
                            onFocusChanged = { state ->
                                if (!state.isFocused) {
                                    validation.markTouched("password")
                                }
                            }
                        )

                        if (authMode == AuthMode.SIGN_IN) {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                                AppTextButton(
                                    text = "Forgot password?",
                                    onClick = { /* Forgot password logic */ }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(AppSpacing.md))

                        AppPrimaryButton(
                            text = if (authMode == AuthMode.SIGN_UP) "Create Account" else "Sign In",
                            onClick = {
                                authError = null
                                authViewModel.clearAuthError()
                                validation.markSubmitAttempt()
                                if (isFormValid) {
                                    loadingAction = LoadingAction.Email
                                    authViewModel.signInWithEmail(trimmedEmail, password) { result ->
                                        loadingAction = LoadingAction.None
                                        result.onFailure { authError = mapAuthError(it) }
                                    }
                                }
                            },
                            enabled = isFormValid && !isLoading,
                            loading = loadingAction == LoadingAction.Email
                        )
                    }

                    Spacer(modifier = Modifier.height(AppSpacing.xl))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = AppSpacing.md)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(1.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant)
                        )
                        Text(
                            "or", 
                            modifier = Modifier.padding(horizontal = AppSpacing.lg), 
                            color = MaterialTheme.colorScheme.onSurfaceVariant, 
                            style = MaterialTheme.typography.bodySmall
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(1.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant)
                        )
                    }

                    Spacer(modifier = Modifier.height(AppSpacing.xl))

                    AppSecondaryButton(
                        text = "Continue with Google",
                        onClick = {
                            authError = null
                            authViewModel.clearAuthError()
                            loadingAction = LoadingAction.Google
                            startGoogleSignIn()
                        },
                        enabled = !isLoading && isGoogleConfigured,
                        loading = loadingAction == LoadingAction.Google,
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_google),
                                contentDescription = null,
                                tint = Color.Unspecified,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    AppTextButton(
                        text = if (authMode == AuthMode.SIGN_UP) "Already have an account? Sign In" else "New to RAMBOOST? Create Account",
                        onClick = {
                            authViewModel.toggleAuthMode()
                            validation.reset()
                            authError = null
                            authViewModel.clearAuthError()
                        },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(AppSpacing.lg))
                }
            }
        }
    }
}
