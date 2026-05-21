package com.example.andespace.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.andespace.model.dto.UserStatus
import com.example.andespace.ui.main.AssetIcon

val friendStatusLabelsEn: Map<UserStatus, String> = mapOf(
    UserStatus.INCOGNITO to "Do not disturb",
    UserStatus.BUSY to "Busy",
    UserStatus.EXERCISING to "Exercising",
    UserStatus.FREE to "Free",
    UserStatus.HANGING_OUT to "Hanging out",
    UserStatus.AT_HOME to "At home",
    UserStatus.LUNCHING to "Lunching"
)

fun friendStatusLabel(status: UserStatus): String =
    friendStatusLabelsEn[status] ?: status.value.replace('_', ' ').replaceFirstChar { it.uppercase() }

/** Short labels for the status picker carousel (matches design mockup). */
val myStatusPickerLabels: Map<UserStatus, String> = mapOf(
    UserStatus.INCOGNITO to "Incognito",
    UserStatus.BUSY to "Busy",
    UserStatus.EXERCISING to "Exercising",
    UserStatus.FREE to "Free",
    UserStatus.HANGING_OUT to "Hanging out",
    UserStatus.AT_HOME to "At home",
    UserStatus.LUNCHING to "Lunching"
)

fun myStatusPickerLabel(status: UserStatus): String =
    myStatusPickerLabels[status] ?: friendStatusLabel(status)

fun friendStatusIconAssetPath(status: UserStatus): String = when (status) {
    UserStatus.INCOGNITO -> "icons/incognito.svg"
    UserStatus.BUSY -> "icons/busy.svg"
    UserStatus.EXERCISING -> "icons/exercising.svg"
    UserStatus.FREE -> "icons/free.svg"
    UserStatus.HANGING_OUT -> "icons/hangingout.svg"
    UserStatus.AT_HOME -> "icons/athome.svg"
    UserStatus.LUNCHING -> "icons/lunching.svg"
}

@Composable
fun FriendStatusIcon(
    status: UserStatus,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    AssetIcon(
        assetPath = friendStatusIconAssetPath(status),
        contentDescription = contentDescription,
        modifier = modifier
    )
}
