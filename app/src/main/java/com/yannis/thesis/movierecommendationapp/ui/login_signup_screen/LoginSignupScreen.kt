package com.yannis.thesis.movierecommendationapp.ui.login_signup_screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yannis.thesis.movierecommendationapp.R
import com.yannis.thesis.movierecommendationapp.ui.theme.BrandColors

@Composable
fun LoginSignupScreen(
    state: AuthUiState,
    onLogin: (String, String) -> Unit,
    onSignup: (String, String) -> Unit,
    onErrorMessageShown: () -> Unit,
    onSuccessMessageShown: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var loginEmail by rememberSaveable { mutableStateOf("") }
    var loginPassword by rememberSaveable { mutableStateOf("") }
    var signupEmail by rememberSaveable { mutableStateOf("") }
    var signupPassword by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(state.errorMessage) {
        if (state.errorMessage != null) onErrorMessageShown()
    }
    LaunchedEffect(state.successMessage) {
        if (state.successMessage != null) {
            signupEmail = ""
            signupPassword = ""
            onSuccessMessageShown()
        }
    }
    LaunchedEffect(state.loginSuccess) {
        if (state.loginSuccess) onLoginSuccess()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandColors.PrimaryBlue)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(36.dp))
        Text(
            text = "Movie Recommendation App",
            color = Color.White,
            style = MaterialTheme.typography.h6
        )
        Image(
            painter = painterResource(R.drawable.movie_icon),
            contentDescription = "Movie recommendation app",
            modifier = Modifier.padding(16.dp)
        )
        TabRow(
            selectedTabIndex = selectedTab,
            backgroundColor = Color.Transparent,
            contentColor = Color.White
        ) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                Text("Login", Modifier.padding(16.dp))
            }
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                Text("Sign Up", Modifier.padding(16.dp))
            }
        }
        if (selectedTab == 0) {
            AuthForm(
                email = loginEmail,
                password = loginPassword,
                buttonText = "Login",
                isLoading = state.isLoading,
                onEmailChange = { loginEmail = it },
                onPasswordChange = { loginPassword = it },
                onSubmit = { onLogin(loginEmail, loginPassword) }
            )
        } else {
            AuthForm(
                email = signupEmail,
                password = signupPassword,
                buttonText = "Register",
                isLoading = state.isLoading,
                onEmailChange = { signupEmail = it },
                onPasswordChange = { signupPassword = it },
                onSubmit = { onSignup(signupEmail, signupPassword) }
            )
        }
    }
}

@Composable
private fun AuthForm(
    email: String,
    password: String,
    buttonText: String,
    isLoading: Boolean,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        TextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            colors = authFieldColors()
        )
        TextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("Password") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            colors = authFieldColors()
        )
        Button(
            onClick = onSubmit,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth(),
            colors = androidx.compose.material.ButtonDefaults.buttonColors(
                backgroundColor = BrandColors.Coral,
                contentColor = Color.White
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.height(20.dp))
            } else {
                Text(buttonText)
            }
        }
    }
}

@Composable
private fun authFieldColors() = TextFieldDefaults.textFieldColors(
    backgroundColor = BrandColors.LoginField,
    textColor = Color.White,
    cursorColor = BrandColors.Yellow,
    focusedIndicatorColor = BrandColors.Yellow,
    unfocusedIndicatorColor = Color.White.copy(alpha = 0.75f),
    focusedLabelColor = BrandColors.Yellow,
    unfocusedLabelColor = Color.White.copy(alpha = 0.9f),
    placeholderColor = Color.White.copy(alpha = 0.8f)
)

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun LoginSignupScreenPreview() {
    LoginSignupScreen(
        state = AuthUiState(),
        onLogin = { _, _ -> },
        onSignup = { _, _ -> },
        onErrorMessageShown = {},
        onSuccessMessageShown = {},
        onLoginSuccess = {}
    )
}
