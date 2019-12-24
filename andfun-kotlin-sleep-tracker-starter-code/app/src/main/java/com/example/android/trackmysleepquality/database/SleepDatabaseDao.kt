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

package com.example.android.trackmysleepquality.database

import androidx.lifecycle.LiveData
import androidx.room.*

//Dao tells Room that is interface will have the data access object
@Dao
interface SleepDatabaseDao{

    // Insert into the database
    // Room will generate all the necessary code to insert the passed in
    // SleepNight into the database. Note that you can call the function anything you want
    @Insert
    fun insert(night: SleepNight)

    // Update the database
    @Update
    fun update(night: SleepNight)

    // Given a key, return the sleep night
    @Query("SELECT * FROM daily_sleep_quality_table WHERE nightId = :key")
    fun get(key:Long): SleepNight

    // No where constraint means that everything is going to be deleted from daily_sleep_quality_table
    @Query("DELETE FROM daily_sleep_quality_table")
    fun clear()

    // Get back all of the entities ordered by nightId in descending order
    @Query("SELECT * FROM daily_sleep_quality_table ORDER BY nightId DESC")
    fun getAllNights(): LiveData<List<SleepNight>>

    // Return back only the most recent sleep night
    @Query("SELECT * FROM daily_sleep_quality_table ORDER BY nightId DESC LIMIT 1")
    fun getTonight(): SleepNight?
}
