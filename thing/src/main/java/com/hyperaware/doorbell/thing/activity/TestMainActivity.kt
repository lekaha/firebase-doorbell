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
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.hyperaware.doorbell.thing.R
import android.graphics.BitmapFactory
import android.util.Log
import android.view.View
import com.google.firebase.auth.GoogleAuthProvider

class TestMainActivity : Activity() {

    companion object {
        const val TAG = "TestMainActivity"
    }

    private val auth = FirebaseAuth.getInstance()
    private var user: FirebaseUser? = null

    private lateinit var vTrySignIn: Button
    private lateinit var vMain: Button
    private lateinit var vAuthConns: Button
    private lateinit var vAuthMessaging: Button
    private lateinit var vSignOut: Button
    private lateinit var vTakePicture: Button
    private lateinit var vCameraPreviewImage: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_main)

        vTrySignIn = findViewById<Button>(R.id.btn_try_sign_in)
        vTrySignIn.setOnClickListener {
            trySignIn()
        }

        vMain = findViewById<Button>(R.id.btn_main)
        vMain.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        vAuthConns = findViewById<Button>(R.id.btn_auth_conns)
        vAuthConns.setOnClickListener {
            startActivity(Intent(this, NearbyConnectionsActivity::class.java))
        }

        vAuthMessaging = findViewById<Button>(R.id.btn_auth_messaging)
        vAuthMessaging.setOnClickListener {
            startActivity(Intent(this, NearbyMessagingActivity::class.java))
        }

        vSignOut = findViewById<Button>(R.id.btn_sign_out)
        vSignOut.setOnClickListener {
            auth.signOut()
        }

        vTakePicture = findViewById(R.id.btn_take_picture)
        vTakePicture.setOnClickListener {
            startActivityForResult(Intent(this, Camera2Activity::class.java), REQUEST_TAKE_PICTURE)
        }

        vCameraPreviewImage = findViewById(R.id.iv_camera_preview)

        updateUi()
    }

    override fun onStart() {
        super.onStart()
        auth.addAuthStateListener(authStateListener)
    }

    override fun onStop() {
        auth.removeAuthStateListener(authStateListener)
        super.onStop()
    }

    private fun trySignIn() {
        getString(R.string.firebase_auth_token).takeIf(String::isNotEmpty)?.also { token ->

            Log.d(TAG, "Signing in with token $token")
            val credential = GoogleAuthProvider.getCredential(token, null)
            FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnSuccessListener(this) { result ->
                    user = result.user.apply {
                        Log.d(TAG, "signInWithCredential $displayName $email")
                    }

                }
                .addOnFailureListener(this) { e ->
                    Log.e(TAG, "signInWithCredential onFailure", e)
                }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (resultCode) {
            Activity.RESULT_CANCELED -> {}
            Camera2Activity.RESULT_PICTURE -> {
                data!!.getStringExtra(Camera2Activity.EXTRA_PICTURE_FILE).apply(::showImage)

            }
        }
    }

    private fun showImage(filePath: String) {
        BitmapFactory.decodeFile(filePath).apply(vCameraPreviewImage::setImageBitmap)
        vCameraPreviewImage.visibility = View.VISIBLE
    }

    private val authStateListener = FirebaseAuth.AuthStateListener {
        user = auth.currentUser
        updateUi()
    }

    private fun updateUi() {
        vTrySignIn.isEnabled = user == null
        vMain.isEnabled = user != null
        vAuthConns.isEnabled = user == null
        vAuthMessaging.isEnabled = user == null
        vSignOut.isEnabled = user != null
    }

}
