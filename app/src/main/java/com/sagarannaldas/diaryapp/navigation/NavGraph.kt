package com.sagarannaldas.diaryapp.navigation

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.sagarannaldas.diaryapp.data.repository.MongoDB
import com.sagarannaldas.diaryapp.model.GalleryImage
import com.sagarannaldas.diaryapp.model.Mood
import com.sagarannaldas.diaryapp.presentation.components.DisplayAlertDialog
import com.sagarannaldas.diaryapp.presentation.screens.auth.AuthenticationScreen
import com.sagarannaldas.diaryapp.presentation.screens.auth.AuthenticationViewModel
import com.sagarannaldas.diaryapp.presentation.screens.home.HomeScreen
import com.sagarannaldas.diaryapp.presentation.screens.home.HomeViewModel
import com.sagarannaldas.diaryapp.presentation.screens.write.WriteScreen
import com.sagarannaldas.diaryapp.presentation.screens.write.WriteViewModel
import com.sagarannaldas.diaryapp.util.Constants.APP_ID
import com.sagarannaldas.diaryapp.util.Constants.WRITE_SCREEN_ARGUMENT_KEY
import com.sagarannaldas.diaryapp.model.RequestState
import com.sagarannaldas.diaryapp.model.rememberGalleryState
import com.stevdzasan.messagebar.rememberMessageBarState
import com.stevdzasan.onetap.rememberOneTapSignInState
import io.realm.kotlin.mongodb.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SetupNavGraph(
    startDestination: String,
    navController: NavHostController,
    onDataLoaded: () -> Unit
) {

    NavHost(
        startDestination = startDestination,
        navController = navController
    ) {
        authenticationRoute(
            navigateToHome = {
                navController.popBackStack()
                navController.navigate(Screen.Home.route)
            },
            onDataLoaded = onDataLoaded
        )
        homeRoute(
            navigateToWrite = {
                navController.navigate(Screen.Write.route)
            },
            navigateToWriteWithArgs = {
                navController.navigate(Screen.Write.passDiaryID(diaryId = it))
            },
            navigateToAuth = {
                navController.popBackStack()
                navController.navigate(Screen.Authentication.route)
            },
            onDataLoaded = onDataLoaded
        )
        writeRoute(
            onBackPressed = {
                navController.popBackStack()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
fun NavGraphBuilder.authenticationRoute(
    navigateToHome: () -> Unit,
    onDataLoaded: () -> Unit
) {
    composable(route = Screen.Authentication.route) {
        val viewModel: AuthenticationViewModel = viewModel()
        val authenticated by viewModel.authenticated
        val loadingState by viewModel.loadingState
        val oneTapState = rememberOneTapSignInState()
        val messageBarState = rememberMessageBarState()

        LaunchedEffect(key1 = Unit) {
            onDataLoaded()
        }

        AuthenticationScreen(
            loadingState = loadingState,
            authenticated = authenticated,
            oneTapSignInState = oneTapState,
            messageBarState = messageBarState,
            onTokenIdReceived = { tokenId ->
                viewModel.signInWithMongoAtlas(
                    tokenId = tokenId,
                    onSuccess = {
                        messageBarState.addSuccess("Successfully Authenticated!")
                        viewModel.setLoadingState(false)
                    },
                    onFailure = { exception ->
                        messageBarState.addError(exception)
                        viewModel.setLoadingState(false)
                    }
                )
            },
            onDialogDismissed = { message ->
                messageBarState.addError(Exception(message))
                viewModel.setLoadingState(false)

            },
            onButtonClicked = {
                oneTapState.open()
                viewModel.setLoadingState(true)
            },
            navigateToHome = navigateToHome
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
fun NavGraphBuilder.homeRoute(
    navigateToWrite: () -> Unit,
    navigateToWriteWithArgs: (String) -> Unit,
    navigateToAuth: () -> Unit,
    onDataLoaded: () -> Unit
) {
    composable(route = Screen.Home.route) {
        val homeViewModel: HomeViewModel = viewModel()
        val diaries by homeViewModel.diaries
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        var signOutDialogOpened by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()

        LaunchedEffect(key1 = diaries) {
            if (diaries !is RequestState.Loading) {
                onDataLoaded()
            }
        }

        HomeScreen(
            diaries = diaries,
            drawerState = drawerState,
            onMenuClicked = {
                scope.launch {
                    drawerState.open()
                }
            },
            onSignOutClicked = {
                signOutDialogOpened = true
            },
            navigateToWrite = navigateToWrite,
            navigateToWriteWithArgs = navigateToWriteWithArgs
        )

        LaunchedEffect(key1 = Unit) {
            MongoDB.configureTheRealm()
        }

        DisplayAlertDialog(
            title = "Sign Out",
            message = "Are you sure you want to Sign Out from your Google Account?",
            dialogOpened = signOutDialogOpened,
            onCloseDialog = {
                signOutDialogOpened = false
            },
            onYesClicked = {
                scope.launch(Dispatchers.IO) {
                    val user = App.Companion.create(APP_ID).currentUser
                    if (user != null) {
                        user.logOut()
                        // calling method on main thread
                        withContext(Dispatchers.Main) {
                            navigateToAuth()
                        }
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
fun NavGraphBuilder.writeRoute(onBackPressed: () -> Unit) {
    composable(
        route = Screen.Write.route,
        arguments = listOf(navArgument(name = WRITE_SCREEN_ARGUMENT_KEY) {
            type = NavType.StringType
            nullable = true
            defaultValue = null
        })
    ) {
        val viewModel: WriteViewModel = viewModel()
        val context = LocalContext.current
        val uiState = viewModel.uiState
        val pagerState = rememberPagerState(pageCount = { Mood.values().size })
        val galleryState = rememberGalleryState()
        val pageNumber by remember { derivedStateOf { pagerState.currentPage } }

//        LaunchedEffect(key1 = uiState) {
//            Log.d("SelectedDiary", "${uiState.selectedDiaryId}")
//        }

        WriteScreen(
            uiState = uiState,
            moodName = { Mood.values()[pageNumber].name },
            pagerState = pagerState,
            galleryState = galleryState,
            onTitleChanged = { viewModel.setTitle(title = it) },
            onDescriptionChanged = { viewModel.setDescription(description = it) },
            onDateTimeUpdated = { viewModel.updateDateTime(zonedDateTime = it) },
            onDeleteConfirmed = {
                viewModel.deleteDiary(
                    onSuccess = {
                        Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show()
                        onBackPressed()
                    },
                    onError = { message ->
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }
                )
            },
            onBackPressed = onBackPressed,
            onSaveClicked = {
                viewModel.upsertDairy(
                    diary = it.apply { mood = Mood.values()[pageNumber].name },
                    onSuccess = { onBackPressed() },
                    onError = { message ->
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }
                )
            },
            onImageSelect ={
                galleryState.addImage(
                    GalleryImage(
                        image = it,
                        remoteImagePath = ""
                    )
                )
            }
        )
    }
}