package com.example.insta

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.insta.models.Post
import com.example.insta.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_create_post.*

private const val TAG = "CreatePostActivity"
private const val PICK_PHOTO_CODE = 1234

class CreatePostActivity : AppCompatActivity() {

    private var photoUri: Uri? = null
    private var signedInUser: User? = null
    private lateinit var fireStoreDb : FirebaseFirestore
    private lateinit var storageReference: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)

        storageReference = FirebaseStorage.getInstance().reference

        fireStoreDb = FirebaseFirestore.getInstance()
        fireStoreDb.collection("users")
            .document(
                FirebaseAuth
                .getInstance()
                .currentUser?.uid as String)
            .get()
            .addOnSuccessListener { userSnapShot ->
                signedInUser = userSnapShot.toObject(User::class.java)
                Log.i(TAG, "signed in user: $signedInUser")

            }
            .addOnFailureListener { exception ->
                Log.i(TAG, "Failure fetching signed in user", exception)
            }


        btnPickImage.setOnClickListener {
            Log.i(TAG, "Open Up Image Picker On Device")
            val imagePickerIntent = Intent(Intent.ACTION_GET_CONTENT)
            imagePickerIntent.type = "image/*"

            if (imagePickerIntent.resolveActivity(packageManager) != null){
                startActivityForResult(imagePickerIntent, PICK_PHOTO_CODE)
            }
        }

        btnSubmit.setOnClickListener {
            handleSubmitButton()
        }
    }

    private fun handleSubmitButton(){
        if (photoUri == null){
            Toast.makeText(this, "No Photo Selected",
                    Toast.LENGTH_SHORT).show()
            return
        }

        if (etDescription.text.isBlank()){
            Toast.makeText(this, "Description Can't Be Empty",
                Toast.LENGTH_SHORT).show()
            return
        }

        if (signedInUser == null){
            Toast.makeText(this, "No signed in User, Please wait",
                Toast.LENGTH_SHORT).show()
            return
        }

        btnSubmit.isEnabled = false

        val photoUploadUri = photoUri as Uri

        // Define the path to Upload image and it's name
        val photoReference = storageReference
            .child("images/${System.currentTimeMillis()}-photo.jpg")

        // Upload Image to FireStore
        photoReference.putFile(photoUploadUri)
            .continueWithTask { photoUploadTask ->
                Log.i(TAG, "uploaded bytes: ${photoUploadTask.result?.bytesTransferred}")


                // Retrieve Image URL of The Uploaded Image
                photoReference.downloadUrl
            }
            .continueWithTask { downloadUrlTask ->
                // Create a post object with image URL
                // and add that to the post collection
                val post = Post(
                    etDescription.text.toString(),
                    downloadUrlTask.result.toString(),
                    System.currentTimeMillis(),
                    signedInUser
                )

                fireStoreDb.collection("posts").add(post)
            }
            .addOnCompleteListener { postCreationTask ->
                btnSubmit.isEnabled = true

                if (!postCreationTask.isSuccessful){
                    Log.e(TAG, "Exception during Firebase Operations", postCreationTask.exception)
                    Toast.makeText(this, "Failed to Save Post", Toast.LENGTH_SHORT).show()
                }

                etDescription.text.clear()
                imageView.setImageResource(0)
                Toast.makeText(this, "Success!", Toast.LENGTH_SHORT).show()

                val profileIntent = Intent(this, ProfileActivity::class.java)
                profileIntent.putExtra(EXTRA_USERNAME, signedInUser?.username)
                startActivity(profileIntent)
                finish()
            }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_PHOTO_CODE){
            if (resultCode == Activity.RESULT_OK){
                photoUri = data?.data
                Log.i(TAG, "photoUri: $photoUri")
                imageView.setImageURI(photoUri)
            }
            else{
                Toast.makeText(this,
                    "Image Picker Action Canceled", Toast.LENGTH_SHORT).show()
            }
        }
    }
}