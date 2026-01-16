package com.pushprime.ui.screens

import android.app.Activity
import android.util.Patterns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.pushprime.R
import com.pushprime.auth.AuthViewModel
import com.pushprime.ui.theme.PushPrimeColors

private enum class LoadingAction {
    None,
    Google,
    Email
}

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
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var authError by remember { mutableStateOf<String?>(null) }
    var isCreateAccount by remember { mutableStateOf(false) }
    var attemptedSubmit by remember { mutableStateOf(false) }
    var loadingAction by remember { mutableStateOf(LoadingAction.None) }
    val isLoading = loadingAction != LoadingAction.None

    val onLoggedInState = rememberUpdatedState(onLoggedIn)
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            onLoggedInState.value()
        }
    }

    val googleClientId = remember {
        val resId = context.resources.getIdentifier(
            "default_web_client_id",
            "string",
            context.packageName
        )
        if (resId != 0) {
            context.getString(resId)
        } else {
            ""
        }
    }
    val isGoogleConfigured = googleClientId.isNotBlank()

    fun mapAuthError(error: Throwable): String {
        return when (error) {
            is FirebaseAuthInvalidCredentialsException -> "Invalid email or password."
            is FirebaseAuthWeakPasswordException -> "Password is too weak (min 6 characters)."
            is FirebaseAuthUserCollisionException -> "Account already exists. Try signing in."
            is FirebaseAuthException -> {
                when (error.errorCode) {
                    "ERROR_OPERATION_NOT_ALLOWED" -> "Email/password sign-in is disabled in Firebase."
                    "ERROR_USER_DISABLED" -> "This account has been disabled."
                    "ERROR_USER_NOT_FOUND" -> "No account found for this email."
                    else -> error.message ?: "Sign-in failed."
                }
            }
            else -> error.message ?: "Sign-in failed."
        }
    }

    fun validateFields(): Boolean {
        val isEmailValid = email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
        emailError = if (!isEmailValid) "Enter a valid email." else null
        val passwordMessage = when {
            password.isBlank() -> "Enter your password."
            isCreateAccount && password.length < 6 -> "Password must be at least 6 characters."
            else -> null
        }
        passwordError = passwordMessage
        return emailError == null && passwordError == null
    }

    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val token = account.idToken
            if (token.isNullOrBlank()) {
                authError = "Google sign-in failed. Please try again."
                loadingAction = LoadingAction.None
            } else {
                authViewModel.signInWithGoogle(token) { signInResult ->
                    loadingAction = LoadingAction.None
                    signInResult.onSuccess {
                        onLoggedInState.value()
                    }.onFailure { error ->
                        authError = mapAuthError(error)
                    }
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
            authError = "Google sign-in is not configured."
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(PushPrimeColors.Primary, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = "PushPrime logo",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(44.dp)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Sign in to PushPrime",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Use Google or your email to continue.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isGoogleConfigured) {
                    OutlinedButton(
                        onClick = {
                            authError = null
                            loadingAction = LoadingAction.Google
                            startGoogleSignIn()
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .height(56.dp)
                            .fillMaxWidth()
                    ) {
                        if (loadingAction == LoadingAction.Google) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                modifier = Modifier.width(20.dp)
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_google),
                                    contentDescription = null,
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    "Continue with Google",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        text = "Google sign-in not configured. Re-download google-services.json after enabling Google provider.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    Text(
                        text = "or",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        if (attemptedSubmit) {
                            emailError = null
                        }
                    },
                    label = { Text("Email") },
                    isError = attemptedSubmit && emailError != null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                        focusedBorderColor = PushPrimeColors.Primary,
                        focusedLabelColor = PushPrimeColors.Primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        cursorColor = PushPrimeColors.Primary
                    )
                )
                if (attemptedSubmit && emailError != null) {
                    Text(
                        text = emailError.orEmpty(),
                        color = PushPrimeColors.Error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        if (attemptedSubmit) {
                            passwordError = null
                        }
                    },
                    label = { Text("Password") },
                    singleLine = true,
                    isError = attemptedSubmit && passwordError != null,
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                        focusedBorderColor = PushPrimeColors.Primary,
                        focusedLabelColor = PushPrimeColors.Primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        cursorColor = PushPrimeColors.Primary
                    )
                )
                if (attemptedSubmit && passwordError != null) {
                    Text(
                        text = passwordError.orEmpty(),
                        color = PushPrimeColors.Error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }

                Button(
                    onClick = {
                        authError = null
                        attemptedSubmit = true
                        if (!validateFields()) {
                            return@Button
                        }
                        loadingAction = LoadingAction.Email
                        authViewModel.signInWithEmail(
                            email = email,
                            password = password,
                            isCreateAccount = isCreateAccount
                        ) { result ->
                            loadingAction = LoadingAction.None
                            result.onSuccess {
                                onLoggedInState.value()
                            }.onFailure { error ->
                                authError = mapAuthError(error)
                            }
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier
                        .height(56.dp)
                        .fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PushPrimeColors.Primary,
                        contentColor = PushPrimeColors.Surface
                    )
                ) {
                    if (loadingAction == LoadingAction.Email) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            modifier = Modifier.width(20.dp)
                        )
                    } else {
                        Text(
                            text = if (isCreateAccount) "Create account" else "Sign in",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                if (authError != null) {
                    Text(
                        text = authError.orEmpty(),
                        color = PushPrimeColors.Error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                TextButton(
                    onClick = { isCreateAccount = !isCreateAccount },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isCreateAccount) {
                            "Already have an account? Sign in"
                        } else {
                            "New here? Create account"
                        }
                    )
                }
            }
        }
    }
}
