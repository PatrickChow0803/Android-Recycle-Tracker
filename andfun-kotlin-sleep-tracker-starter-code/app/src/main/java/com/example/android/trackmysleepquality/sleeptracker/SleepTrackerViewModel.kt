/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.trackmysleepquality.sleeptracker

import android.app.Application
import android.provider.SyncStateContract.Helpers.insert
import android.provider.SyncStateContract.Helpers.update
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import com.example.android.trackmysleepquality.database.SleepNight
import com.example.android.trackmysleepquality.formatNights
import kotlinx.coroutines.*

/**
 * ViewModel for SleepTrackerFragment.
 */
class SleepTrackerViewModel(
        val database: SleepDatabaseDao,
        application: Application) : AndroidViewModel(application) {

    // Allows us to cancel all coroutines started by this view model when the view model is no longer used and destroyed.
    private var viewModelJob = Job()

    // When a view model is destroyed, onCleared is called
    override fun onCleared() {
        super.onCleared()

        // call cancel to cancel all other coroutines made by this job.
        viewModelJob.cancel()
    }

    // Determines what threads the coroutines will run on. Also needs to knoe about the job
    // Dispatchers.Main = Coroutines launched in the UI scope will run on the main thread
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    // Live Data because we want to observe it
    // MutableLiveData so that we can change it
    private var tonight = MutableLiveData<SleepNight?>()

    // Gets all the nights in the database when we create the view model
    private val nights = database.getAllNights()

    val nightsString = Transformations.map(nights){nights ->
        formatNights(nights, application.resources)
    }

    val startButtonVisible = Transformations.map(tonight){
        null == it
    }

    val stopButtonVisible = Transformations.map(tonight){
        null != it
    }

    val clearButtonVisible = Transformations.map(nights){
        it?.isNotEmpty()
    }

    private var _showSnackBarEvent = MutableLiveData<Boolean>()
    val showSnackBarEvent : LiveData<Boolean>
        get() = _showSnackBarEvent

    fun doneShowingSnackbar(){
        _showSnackBarEvent.value = false
    }

    private val _navigationToSleepQuality = MutableLiveData<SleepNight>()
    val navigationToSleepQuality : LiveData<SleepNight>
        get() = _navigationToSleepQuality

    fun doneNavigating(){
        _navigationToSleepQuality.value = null
    }

    init {
        initializeTonight()
    }

    private fun initializeTonight(){
        // launch means start a coroutine
        uiScope.launch {
            tonight.value = getTonightFromDatabase()
        }
    }

    // Suspend = We want to call it from coroutine and not block
    private suspend fun getTonightFromDatabase(): SleepNight?{
        return withContext(Dispatchers.IO){
            var night = database.getTonight()
            if(night?.endTimeMilli != night?.startTimeMilli){
                night = null
            }
            night
        }
    }

    fun onStartTracking(){
        uiScope.launch {
            val newNight = SleepNight()
            insert(newNight)
            tonight.value = getTonightFromDatabase()
        }
    }

    private suspend fun insert(night: SleepNight){
        withContext(Dispatchers.IO){
            database.insert(night)
        }
    }
//    General Pattern
//    fun someWorkNeedsToBeDone{
//        uiScope.launch {
//
//            suspendFunction()
//
//        }
//    }
//
//    suspend fun suspendFunction(){
//        withContext(Dispatchers.IO){
//            longRunningWork()
//        }
//    }

    /**
     * Executes when the STOP button is clicked.
     */
    fun onStopTracking() {
        uiScope.launch {
            // In Kotlin, the return@label syntax is used for specifying which function among
            // several nested ones this statement returns from.
            // In this case, we are specifying to return from launch(),
            // not the lambda.
            val oldNight = tonight.value ?: return@launch

            // Update the night in the database to add the end time.
            oldNight.endTimeMilli = System.currentTimeMillis()

            update(oldNight)
            _navigationToSleepQuality.value = oldNight
        }
    }

    private suspend fun update(night: SleepNight){
        withContext(Dispatchers.IO){
            database.update(night)
        }
    }

    fun onClear(){
        uiScope.launch {
            clear()
            tonight.value = null
            _showSnackBarEvent.value = true
        }
    }

    private suspend fun clear(){
        withContext(Dispatchers.IO){
            database.clear()
        }
    }

}

