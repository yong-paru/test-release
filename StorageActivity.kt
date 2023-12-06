package com.example.finalexam3

import android.Manifest
import android.content.ContentUris
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage


class StorageActivity : AppCompatActivity() {
    lateinit var storage: FirebaseStorage
    private val imageView by lazy {findViewById<ImageView>(R.id.imageView)}
    private val imageView2 by lazy {findViewById<ImageView>(R.id.imageView2)}

    companion object {
        const val REQUEST_CODE = 1
        const val UPLOAD_FOLDER = "upload_images/"
    }

    private val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
    } else {
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_storage)

        Firebase.auth.currentUser ?: finish()  // if not authenticated, finish this activity

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2)
            requestSinglePermission(Manifest.permission.READ_EXTERNAL_STORAGE)
        else
            requestSinglePermission(Manifest.permission.READ_MEDIA_IMAGES)

        storage = Firebase.storage
        val storageRef = storage.reference // reference to root
        val imageRef1 = storageRef.child("images/computer_sangsangbugi.jpg")
        val imageRef2 = storage.getReferenceFromUrl(
            "gs://finalexam3-1e2aa.appspot.com"
        )
        // imageRef1 and imageRef2 indicate same object.
        displayImageRef(imageRef1, imageView)
        displayImageRef(imageRef2, imageView2)

        findViewById<Button>(R.id.buttonUpload)?.setOnClickListener {
            uploadDialog()
        }

        findViewById<Button>(R.id.buttonListUploadedPhotos)?.setOnClickListener {
            listPhotosDialog()
        }
    }

    private fun requestSinglePermission(permission: String) {
        if (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED)
            return

        val requestPermLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it == false) { // permission is not granted!
                AlertDialog.Builder(this).apply {
                    setTitle("Warning")
                    setMessage("permission required!")
                }.show()
            }
        }

        if (shouldShowRequestPermissionRationale(permission)) {
            // you should explain the reason why this app needs the permission.
            AlertDialog.Builder(this).apply {
                setTitle("Reason")
                setMessage("permission required!")
                setPositiveButton("Allow") { _, _ -> requestPermLauncher.launch(permission) }
                setNegativeButton("Deny") { _, _ -> }
            }.show()
        } else {
            // should be called in onCreate()
            requestPermLauncher.launch(permission)
        }
    }

    private fun hasPermission(permission: String) =
        checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED

    private fun uploadDialog() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            if (!hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) return
        } else {
            if (!hasPermission(Manifest.permission.READ_MEDIA_IMAGES)) return
        }

        val cursor = contentResolver.query(collection,
            null, null, null, null)

        AlertDialog.Builder(this)
            .setTitle("Choose Photo")
            .setCursor(cursor, { _, i ->
                cursor?.run {
                    moveToPosition(i)
                    val idIdx = getColumnIndex(MediaStore.Images.ImageColumns._ID)
                    val nameIdx = getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME)
                    uploadFile(getLong(idIdx), getString(nameIdx))
                }
            }, MediaStore.Images.ImageColumns.DISPLAY_NAME).create().show()

    }

    private fun listPhotosDialog() {
        storage.reference.child(UPLOAD_FOLDER).listAll()
            .addOnSuccessListener {
                val itemsString = mutableListOf<String>()
                for (i in it.items) {
                    itemsString.add(i.name)
                }
                AlertDialog.Builder(this)
                    .setTitle("Uploaded Photos")
                    .setItems(itemsString.toTypedArray(), {_, i -> }).show()
            }.addOnFailureListener {

            }
    }

    private fun uploadFile(file_id: Long?, fileName: String?) {
        file_id ?: return
        val imageRef = storage.reference.child("${UPLOAD_FOLDER}${fileName}")
        val contentUri = ContentUris.withAppendedId(collection, file_id)
        imageRef.putFile(contentUri).addOnCompleteListener {
            if (it.isSuccessful) {
                // upload success
                Snackbar.make(imageView, "Upload completed.", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayImageRef(imageRef: StorageReference?, view: ImageView) {
        imageRef?.getBytes(Long.MAX_VALUE)?.addOnSuccessListener {
            val bmp = BitmapFactory.decodeByteArray(it, 0, it.size)
            view.setImageBitmap(bmp)
        }?.addOnFailureListener {
            // Failed to download the image
        }
    }
}