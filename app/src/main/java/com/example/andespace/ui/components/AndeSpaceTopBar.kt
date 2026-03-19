package com.example.andespace.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties

@Composable
fun AndeSpaceTopBar(
    isLoggedIn: Boolean,
    isMenuExpanded: Boolean,
    onAccountClick: () -> Unit,
    onDismissMenu: () -> Unit,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onLogOut: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.windowInsetsPadding(
            WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onHistoryClick) {
                Icon(
                    Icons.Default.History,
                    contentDescription = "History",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.scale(1.5f)
                )
            }

            Text(
                text = "AndeSpace",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Box {
                IconButton(onClick = onAccountClick) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Account",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.scale(1.5f)
                    )
                }

                if (isMenuExpanded) {
                    Popup(
                        alignment = Alignment.TopEnd,
                        offset = IntOffset(x = -20, y = 140),
                        onDismissRequest = onDismissMenu,
                        properties = PopupProperties(focusable = true)
                    ) {
                        Column(horizontalAlignment = Alignment.End) {

                            Box(
                                modifier = Modifier
                                    .padding(end = 24.dp)
                                    .size(16.dp)
                                    .offset(y = 8.dp)
                                    .rotate(45f)
                                    .background(MaterialTheme.colorScheme.background)
                            )

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.background,
                                shadowElevation = 8.dp,
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    if (!isLoggedIn) {
                                        CustomYellowButton("Log in", onLoginClick)
                                        CustomYellowButton("Sign Up", onRegisterClick)
                                    } else {
                                        CustomYellowButton("Log Out", onLogOut)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}