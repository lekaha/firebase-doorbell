/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hyperaware.doorbell.thing.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.google.firebase.storage.FirebaseStorage
import com.hyperaware.doorbell.thing.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ResponseActivity : Activity() {

    companion object {
        private const val TAG = "ResponseActivity"
        const val EXTRA_DISPOSITION = "disposition"
        const val EXTRA_PICTURE_TASK = "task"
    }

    private lateinit var tvDisposition: TextView
    private var disposition: Boolean? = null
    private var taskId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = intent

        val extras = intent.extras
        if (extras == null) {
            Log.e(TAG, "$EXTRA_DISPOSITION nor $EXTRA_PICTURE_TASK was not provided")
            finish()
            return
        }

        if (extras.containsKey(EXTRA_DISPOSITION)) {
            disposition = extras.getBoolean(EXTRA_DISPOSITION)
        }

        if (extras.containsKey(EXTRA_PICTURE_TASK)) {
            taskId = extras.getString(EXTRA_PICTURE_TASK)
        }

        initViews()
    }

    private fun initViews() {
        setContentView(R.layout.activity_response)

        disposition?.let {
            tvDisposition = findViewById(R.id.disposition)
            tvDisposition.text = if (it) {
                getString(R.string.disposition_come_in)
            }
            else {
                getString(R.string.disposition_go_away)
            }
        }

        taskId?.let {
            startActivityForResult(Intent(this, Camera2Activity::class.java),
                                   REQUEST_TAKE_PICTURE)
        }
    }

    private fun uploadFile(filePath: String) {
        val sdf = SimpleDateFormat("yyyyMMddHHmmss", Locale.US)
        val storagePath = "pictures/task_${sdf.format(Date())}_$taskId.jpg"
        val file = File(filePath)

        Log.i(ResponseActivity.TAG, "Uploading to $file to $storagePath")
        val ref = FirebaseStorage.getInstance().getReference(storagePath)
        ref.putFile(Uri.fromFile(file))
            .addOnSuccessListener(this) {
                Log.i(ResponseActivity.TAG, "Picture uploaded")
            }
            .addOnFailureListener(this) { e ->
                Log.i(ResponseActivity.TAG, "Upload failed", e)
                finish()
            }
            .addOnCompleteListener(this) {
                file.delete()
                finish()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (resultCode) {
            Activity.RESULT_CANCELED -> {}
            Camera2Activity.RESULT_PICTURE -> {
                val file = data!!.getStringExtra(Camera2Activity.EXTRA_PICTURE_FILE)
                uploadFile(file)
            }
        }
    }
}
