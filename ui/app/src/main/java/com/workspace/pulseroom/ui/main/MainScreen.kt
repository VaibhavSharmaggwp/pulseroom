package com.workspace.pulseroom.ui.main

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation3.runtime.NavKey
import com.workspace.pulseroom.theme.PulseRoomTheme
import com.workspace.pulseroom.ui.main.screens.RoomsScreen

@Composable
fun MainScreen(
  onItemClick: (NavKey) -> Unit = {},
  modifier: Modifier = Modifier,
) {
  RoomsScreen(modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
  PulseRoomTheme {
    MainScreen()
  }
}
