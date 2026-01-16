package com.pushprime.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.pushprime.auth.AuthViewModel
import com.pushprime.ui.theme.PushPrimeColors

private enum class EmailAuthStep {
    Email, Password
}

@Composable
fun AuthScreen(
    authViewModel: AuthViewModel,
    onLoggedIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity

    var step by remember { mutableStateOf(EmailAuthStep.Email) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var authError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isCreateAccount by remember { mutableStateOf(false) }

    val onLoggedInState = rememberUpdatedState(onLoggedIn)

    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            onLoggedInState.value()
        }
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
                isLoading = false
            } else {
                authViewModel.signInWithGoogle(token) { signInResult ->
                    isLoading = false
                    signInResult.onFailure { error ->
                        authError = error.message ?: "Sign-in failed."
                    }
                }
            }
        } catch (e: Exception) {
            authError = e.message ?: "Google sign-in failed."
            isLoading = false
        }
    }

    fun startGoogleSignIn() {
        if (activity == null) {
            authError = "Unable to start Google sign-in."
            isLoading = false
            return
        }
        val clientId = try {
            context.getString(
                context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
            )
        } catch (_: Exception) {
            ""
        }
        if (clientId.isBlank()) {
            authError = "Google sign-in is not configured."
            isLoading = false
            return
        }
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(clientId)
            .requestEmail()
            .build()
        val googleClient = GoogleSignIn.getClient(activity, signInOptions)
        googleLauncher.launch(googleClient.signInIntent)
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = PushPrimeColors.Background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 32.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(80.dp)
                        .background(PushPrimeColors.Primary, shape = MaterialTheme.shapes.large)
                )
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "Welcome back",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = PushPrimeColors.OnBackground
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Log in to keep your streak going.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = PushPrimeColors.OnSurfaceVariant
                )
            }

            if (step == EmailAuthStep.Email) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(vertical = 24.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            authError = null
                            isLoading = true
                            startGoogleSignIn()
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .height(56.dp)
                            .fillMaxWidth()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                modifier = Modifier.width(20.dp)
                            )
                        } else {
                            Text(
                                "Continue with Google",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }

                    Button(
                        onClick = {
                            authError = null
                            emailError = null
                            if (email.isBlank() || !email.contains("@")) {
                                emailError = "Enter a valid email."
                            } else {
                                step = EmailAuthStep.Password
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
                        Text(
                            "Continue with Email",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    slideInHorizontally(
                        animationSpec = tween(300),
                        initialOffsetX = { it }
                    ) + fadeIn(animationSpec = tween(300)) togetherWith
                        slideOutHorizontally(
                            animationSpec = tween(300),
                            targetOffsetX = { -it }
                        ) + fadeOut(animationSpec = tween(300))
                },
                label = "email_auth_step"
            ) { target ->
                when (target) {
                    EmailAuthStep.Email -> {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = email,
                                onValueChange = {
                                    email = it
                                    emailError = null
                                },
                                label = { Text("Email") },
                                isError = emailError != null,
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    focusedBorderColor = PushPrimeColors.Primary
                                )
                            )
                            if (emailError != null) {
                                Text(
                                    text = emailError.orEmpty(),
                                    color = PushPrimeColors.Error,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(start = 16.dp)
                                )
                            }
                        }
                    }
                    EmailAuthStep.Password -> {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = password,
                                onValueChange = {
                                    password = it
                                    passwordError = null
                                },
                                label = { Text("Password") },
                                singleLine = true,
                                isError = passwordError != null,
                                modifier = Modifier.fillMaxWidth(),
                                visualTransformation = PasswordVisualTransformation(),
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    focusedBorderColor = PushPrimeColors.Primary
                                )
                            )
                            if (passwordError != null) {
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
                                    if (password.length < 6) {
                                        passwordError = "Password must be at least 6 characters."
                                        return@Button
                                    }
                                    isLoading = true
                                    authViewModel.signInWithEmail(
                                        email = email,
                                        password = password,
                                        isCreateAccount = isCreateAccount
                                    ) { result ->
                                        isLoading = false
                                        result.onFailure { error ->
                                            authError = error.message ?: "Sign-in failed."
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
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.width(20.dp)
                                    )
                                } else {
                                    Text(
                                        "Continue",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            TextButton(
                                onClick = { step = EmailAuthStep.Email },
                                enabled = !isLoading,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Back")
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

            if (authError != null) {
                Text(
                    text = authError.orEmpty(),
                    color = PushPrimeColors.Error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
