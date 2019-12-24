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

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// entities = {...} Tells room about the entities that you'll be using.
// exportSchema = true will save the schema of the database into a folder.
// change version number for when making changes to the database

@Database(entities = [SleepNight::class], version = 1, exportSchema = false)
abstract class SleepDatabase : RoomDatabase(){

    abstract val sleepDatabaseDao: SleepDatabaseDao

    companion object{

        // Instance will keep a reference to the database
        @Volatile // Volatile annotation makes sure that the value of INSTANCE is always up to date
        private var INSTANCE: SleepDatabase ?= null

        fun getInstance(context: Context): SleepDatabase{
            // synchronized = Means only one thread of execution at a time can enter this block of code
            // Makes sure that the database only get's made once.
            synchronized(this){
                var instance = INSTANCE

                if(instance == null){
                    instance = Room.databaseBuilder(
                            context.applicationContext,
                            SleepDatabase::class.java,
                            "sleep_history_database"
                    )
                            .fallbackToDestructiveMigration()
                            .build()
                    INSTANCE = instance
                }
                return instance
            }
        }

    }

}