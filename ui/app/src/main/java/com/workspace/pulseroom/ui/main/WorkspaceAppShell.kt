package com.workspace.pulseroom.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout

@Composable
fun WorkspaceAppShell(windowSizeClass: WindowSizeClass){
    // 1. Calculate if we are on a tablet screen
    val isTablet = windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact

    if(isTablet){
        // TABLET LAYOUT: Navigation Rail on the left, Content on the right[cite: 21]
        Row(modifier = Modifier.fillMaxSize()){
            TabletSidebar()

            // The main screen content
            Box(modifier = Modifier.weight(1f)){
                RoomsScreenContent()
            }
        }
    }else{
        // PHONE LAYOUT: Scaffold with a Bottom Navigation Bar[cite: 21]
        Scaffold(
            bottomBar = {PhoneBottomBar()}
        ) {innerpadding->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerpadding)
            ){
                RoomsScreenContent()
            }

        }
    }
}

// Dummy composables for now, we will fill these in next
@Composable
fun TabletSidebar() { /* UI coming soon */ }

@Composable
fun PhoneBottomBar() { /* UI coming soon */ }

@Composable
fun RoomsScreenContent() { /* UI coming soon */ }