package com.sagarannaldas.diaryapp.data.repository

import com.sagarannaldas.diaryapp.model.Diary
import com.sagarannaldas.diaryapp.util.RequestState
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

typealias Diaries = RequestState<Map<LocalDate, List<Diary>>>

interface MongoRepository {
    fun configureTheRealm()
    fun getAllDiaries(): Flow<Diaries>
    fun getSelectedDiary(diaryId: io.realm.kotlin.types.ObjectId): RequestState<Diary>
    suspend fun addNewDiary(diary: Diary): RequestState<Diary>
}