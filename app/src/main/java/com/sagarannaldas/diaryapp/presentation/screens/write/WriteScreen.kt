package com.sagarannaldas.diaryapp.presentation.screens.write

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.sagarannaldas.diaryapp.model.Diary
import com.sagarannaldas.diaryapp.model.Mood
import java.time.ZonedDateTime

@OptIn(ExperimentalFoundationApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun WriteScreen(
    uiState: UiState,
    moodName: () -> String,
    pagerState: PagerState,
    onTitleChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onDateTimeUpdated: (ZonedDateTime) -> Unit,
    onDeleteConfirmed: () -> Unit,
    onBackPressed: () -> Unit,
    onSaveClicked: (Diary) -> Unit
) {
    //update the mood when selecting an existing diary
    LaunchedEffect(key1 = uiState.mood) {
        pagerState.scrollToPage(Mood.valueOf(uiState.mood.name).ordinal)
    }
    Scaffold(
        topBar = {
            WriteTopBar(
                selectedDiary = uiState.selectedDiary,
                moodName = moodName,
                onDateTimeUpdated = onDateTimeUpdated,
                onDeleteConfirmed = onDeleteConfirmed,
                onBackPressed = onBackPressed
            )
        },
        content = {
            WriteContent(
                uiState = uiState,
                pagerState = pagerState,
                title = uiState.title,
                onTitleChanged = onTitleChanged,
                description = uiState.description,
                onDescriptionChanged = onDescriptionChanged,
                paddingValues = it,
                onSaveClicked = onSaveClicked
            )
        }
    )
}