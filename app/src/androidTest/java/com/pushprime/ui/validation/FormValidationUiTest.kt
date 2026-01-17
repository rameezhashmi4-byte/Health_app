package com.pushprime.ui.validation

import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.pushprime.data.AuthRepository
import com.pushprime.data.LocalStore
import com.pushprime.data.ProfileRepository
import com.pushprime.data.StepsRepository
import com.pushprime.model.ExperienceLevel
import com.pushprime.model.FitnessGoal
import com.pushprime.model.UserProfile
import com.pushprime.ui.screens.EditProfileSheet
import com.pushprime.ui.screens.profile_setup.ProfileSetupScreen
import com.pushprime.ui.screens.profile_setup.ProfileSetupViewModel
import com.pushprime.ui.screens.settings.OpenAiKeySection
import com.pushprime.ui.theme.RamboostTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FormValidationUiTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun profileSetup_basicDetails_gates_continue_and_shows_errors_after_touch() {
        val context = composeTestRule.activity
        val viewModel = ProfileSetupViewModel(
            profileRepository = ProfileRepository(LocalStore(context)),
            stepsRepository = StepsRepository(context),
            authRepository = AuthRepository()
        )

        composeTestRule.setContent {
            RamboostTheme {
                ProfileSetupScreen(
                    viewModel = viewModel,
                    onFinished = {}
                )
            }
        }

        composeTestRule.runOnIdle { viewModel.nextStep() }

        composeTestRule.onNodeWithTag("profile_setup_continue").assertIsNotEnabled()
        composeTestRule.onNodeWithText("Required").assertDoesNotExist()

        composeTestRule.onNodeWithTag("profile_setup_full_name").performClick()
        composeTestRule.onNodeWithTag("profile_setup_full_name").performTextInput(" ")
        composeTestRule.onRoot().performClick()

        composeTestRule.onNodeWithText("Required").assertExists()

        composeTestRule.onNodeWithTag("profile_setup_full_name").performTextClearance()
        composeTestRule.onNodeWithTag("profile_setup_full_name").performTextInput("Alex")
        composeTestRule.onNodeWithText("Get stronger").performClick()
        composeTestRule.onNodeWithText("Beginner").performClick()

        composeTestRule.onNodeWithTag("profile_setup_continue").assertIsEnabled()
    }

    @Test
    fun profileEditSheet_disables_save_until_valid_and_shows_errors_after_touch() {
        val profile = UserProfile(
            uid = "test",
            fullName = "",
            goal = FitnessGoal.GET_STRONGER,
            experience = ExperienceLevel.BEGINNER,
            weightKg = 0.0,
            heightCm = 180.0,
            age = null,
            sex = null,
            stepTrackingEnabled = false
        )

        composeTestRule.setContent {
            RamboostTheme {
                EditProfileSheet(
                    profile = profile,
                    isSaving = false,
                    onDismiss = {},
                    onSave = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("profile_edit_save").assertIsNotEnabled()
        composeTestRule.onNodeWithText("Name is required").assertDoesNotExist()

        composeTestRule.onNodeWithTag("profile_edit_name").performClick()
        composeTestRule.onRoot().performClick()

        composeTestRule.onNodeWithText("Name is required").assertExists()

        composeTestRule.onNodeWithTag("profile_edit_name").performTextInput("Jamie")
        composeTestRule.onNodeWithTag("profile_edit_weight").performTextClearance()
        composeTestRule.onNodeWithTag("profile_edit_weight").performTextInput("72")

        composeTestRule.onNodeWithTag("profile_edit_save").assertIsEnabled()
    }

    @Test
    fun voiceCoach_openAiKey_section_gates_save_and_shows_errors_after_touch() {
        composeTestRule.setContent {
            RamboostTheme {
                val apiKeyState = remember { mutableStateOf("") }
                val savedState = remember { mutableStateOf(false) }
                OpenAiKeySection(
                    apiKeyInput = apiKeyState.value,
                    hasSavedKey = savedState.value,
                    onApiKeyChange = { apiKeyState.value = it },
                    onSave = {},
                    onClear = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("voice_openai_save").assertIsNotEnabled()
        composeTestRule.onNodeWithText("Enter a valid OpenAI API key.").assertDoesNotExist()

        composeTestRule.onNodeWithTag("voice_openai_key").performTextInput("short")
        composeTestRule.onRoot().performClick()

        composeTestRule.onNodeWithText("Enter a valid OpenAI API key.").assertExists()

        composeTestRule.onNodeWithTag("voice_openai_key").performTextClearance()
        composeTestRule.onNodeWithTag("voice_openai_key").performTextInput("sk-1234567890")
        composeTestRule.onRoot().performClick()

        composeTestRule.onNodeWithTag("voice_openai_save").assertIsEnabled()
    }
}
