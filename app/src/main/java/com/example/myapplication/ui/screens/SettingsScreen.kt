package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.data.model.UserSettingsDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: UserViewModel = hiltViewModel()
) {
    val state by viewModel.settingsState

    LaunchedEffect(Unit) {
        viewModel.loadSettings()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Paramètres") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            when (val settingsState = state) {
                SettingsState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }
                is SettingsState.Error -> {
                    Text(settingsState.message, color = MaterialTheme.colorScheme.error)
                }
                is SettingsState.Saving -> {
                    SettingsContent(settingsState.settings, saving = true, onChange = viewModel::updateSettings)
                }
                is SettingsState.Success -> {
                    SettingsContent(settingsState.settings, saving = false, onChange = viewModel::updateSettings)
                }
            }
        }
    }
}

@Composable
private fun SettingsContent(
    settings: UserSettingsDto,
    saving: Boolean,
    onChange: (UserSettingsDto) -> Unit
) {
    Text(text = "Préférences du compte", style = MaterialTheme.typography.titleMedium)

    ListItem(
        headlineContent = { Text("Notifications") },
        supportingContent = { Text("Recevoir les messages, commandes et nouveaux vendeurs") },
        trailingContent = {
            Switch(
                checked = settings.notificationsEnabled,
                enabled = !saving,
                onCheckedChange = { enabled ->
                    onChange(settings.copy(notificationsEnabled = enabled))
                }
            )
        }
    )
    HorizontalDivider()

    ListItem(
        headlineContent = { Text("Mode sombre") },
        supportingContent = { Text("Préférence enregistrée côté serveur") },
        trailingContent = {
            Switch(
                checked = settings.darkModeEnabled,
                enabled = !saving,
                onCheckedChange = { enabled ->
                    onChange(settings.copy(darkModeEnabled = enabled))
                }
            )
        }
    )
    HorizontalDivider()

    Text(
        text = "Langue",
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier.padding(top = 18.dp, bottom = 8.dp)
    )

    LanguageRow(
        label = "Français",
        selected = settings.language == "fr",
        enabled = !saving,
        onClick = { onChange(settings.copy(language = "fr")) }
    )
    LanguageRow(
        label = "English",
        selected = settings.language == "en",
        enabled = !saving,
        onClick = { onChange(settings.copy(language = "en")) }
    )
}

@Composable
private fun LanguageRow(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(label) },
        trailingContent = {
            RadioButton(
                selected = selected,
                enabled = enabled,
                onClick = onClick
            )
        }
    )
}
