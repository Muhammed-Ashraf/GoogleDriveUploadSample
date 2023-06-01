package ashraf.googledriveuploadsample.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import ashraf.googledriveuploadsample.R
import ashraf.googledriveuploadsample.databinding.ActivityCapturePhotoBinding
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnFailureListener
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

const val REQUEST_IMAGE_CAPTURE = 1034
private const val CAMERA_PERMISSION_REQUEST_CODE = 1036
class CapturePhotoActivity : AppCompatActivity() {

    private var photoFile: File? = null
    private lateinit var photoFileName: String
    private var takenPhotoUriList = ArrayList<Uri>(emptyList())
    private var takenImages = ArrayList<File>(emptyList())
    val images = ArrayList<Bitmap>(emptyList())

    var oneTapClient: SignInClient? = null
    var signUpRequest: BeginSignInRequest? = null

    private val readImagePermission =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.READ_MEDIA_IMAGES else Manifest.permission.READ_EXTERNAL_STORAGE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var binding = ActivityCapturePhotoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        askCameraPermission()
        binding.mButton.setOnClickListener {
            if (signUpRequest != null) {
                oneTapClient?.beginSignIn(signUpRequest!!)
                    ?.addOnSuccessListener(
                        this@CapturePhotoActivity
                    ) { result ->
                        Log.d("TAG", "Success")

                        val intentSenderRequest: IntentSenderRequest =
                            IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
                        activityResultLauncher.launch(intentSenderRequest)
                    }
                    ?.addOnFailureListener(
                        this@CapturePhotoActivity,
                        OnFailureListener { e -> // No Google Accounts found. Just continue presenting the signed-out UI.
                            e.localizedMessage?.let { it1 -> Log.d("TAG", it1) }
                        })
            }
        }
        oneTapClient = Identity.getSignInClient(this)
        signUpRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true) // Your server's client ID, not your Android client ID.
                    .setServerClientId(resources.getString(R.string.web_client_id)) // Show all accounts on the device.
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .build()

    }

    private val activityResultLauncher: ActivityResultLauncher<IntentSenderRequest> =
        registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                try {
                    val credential =
                        oneTapClient!!.getSignInCredentialFromIntent(result.data)
                    val idToken = credential.googleIdToken
                    if (idToken != null) {
                        val email = credential.id
                        Toast.makeText(
                            applicationContext,
                            "Email: $email",
                            Toast.LENGTH_SHORT
                        ).show()

                        GoogleSignIn.getLastSignedInAccount(this@CapturePhotoActivity)?.let { googleAccount ->

                            // get credentials
//                            val credential = GoogleAccountCredential.usingOAuth2(
//                                this@CapturePhotoActivity, listOf(DriveScopes.DRIVE, DriveScopes.DRIVE_FILE)
//                            )
//                            credential.selectedAccount = googleAccount.account!!
//
//                            // get Drive Instance
//                            val drive = Drive
//                                .Builder(
//                                    AndroidHttp.newCompatibleTransport(),
//                                    JacksonFactory.getDefaultInstance(),
//                                    credential
//                                )
//                                .setApplicationName(this@CapturePhotoActivity.getString(R.string.app_name))
//                                .build()
//
//                            val gFolder = com.google.api.services.drive.model.File()
//                            // Set file name and MIME
//                            gFolder.name = "My Cool Folder Name"
//                            gFolder.mimeType = "application/vnd.google-apps.folder"
//
//                            // You can also specify where to create the new Google folder
//                            // passing a parent Folder Id
//                            val parents: MutableList<String> = ArrayList(1)
//                            parents.add("your_parent_folder_id_here")
//                            gFolder.parents = parents
//                            drive.Files().create(gFolder).setFields("id").execute()
                        }


                        openCamera()
                    }
                } catch (e: ApiException) {
                    e.printStackTrace()
                }
            }
        }

    fun getDriveService(): Drive? {
        GoogleSignIn.getLastSignedInAccount(this)?.let { googleAccount ->
            val credential = GoogleAccountCredential.usingOAuth2(
                this, listOf(DriveScopes.DRIVE_FILE)
            )
            credential.selectedAccount = googleAccount.account!!
            return Drive.Builder(
                AndroidHttp.newCompatibleTransport(),
                JacksonFactory.getDefaultInstance(),
                credential
            )
                .setApplicationName(getString(R.string.app_name))
                .build()
        }
        return null
    }

    fun uploadFileToGDrive(resizedFile: File, takenPhotoUri: Uri) {
        getDriveService()?.let { googleDriveService ->
            lifecycleScope.launch {
                try {

                    val gfile = com.google.api.services.drive.model.File()
                    gfile.name = resizedFile.name
                    val fileContent = FileContent("image/jpeg", resizedFile)
                    googleDriveService.Files().create(gfile, fileContent).execute()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } ?: Toast.makeText(this@CapturePhotoActivity, "Sign In Error", Toast.LENGTH_SHORT).show()

    }


    fun openCamera() {
        if (ContextCompat.checkSelfPermission(
                this@CapturePhotoActivity,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(
                this@CapturePhotoActivity,
                "Grand Camera Permission",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            dispatchTakePictureIntent()
        }
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
//            updateIntentForCameraFacing(takePictureIntent, false)
            this@CapturePhotoActivity.let { it ->
                takePictureIntent.resolveActivity(it.packageManager)?.also {
                    // Create the File where the photo should go
                    photoFile = try {
                        photoFileName =
                            System.currentTimeMillis().toString() + ".jpg"
                        getPhotoFileUri(photoFileName)
                    } catch (ex: IOException) {
                        null
                    }
                    // Continue only if the File was successfully created
                    photoFile?.also {
                        val photoURI: Uri = FileProvider.getUriForFile(
                            this@CapturePhotoActivity,
                            this@CapturePhotoActivity.applicationContext
                                .packageName.toString() + ".provider",
                            it
                        )
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                        startActivityForResult(
                            takePictureIntent,
                            REQUEST_IMAGE_CAPTURE
                        )
                    }
                }
            }
        }
    }

    private fun getPhotoFileUri(fileName: String): File {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        val mediaStorageDir =
            File(
                this@CapturePhotoActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "captures"
            )

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            Log.d(CapturePhotoActivity::class.java.simpleName, "failed to create directory")
        }

        // Return the file target for the photo based on filename
        return File(mediaStorageDir.path + File.separator + fileName)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {

            val takenPhotoUri = Uri.fromFile(getPhotoFileUri(photoFileName))
            val rawTakenImage = BitmapFactory.decodeFile(takenPhotoUri.path)

//            val resizedBitmap: Bitmap = BitmapScaler.scaleToFitWidth(rawTakenImage, SOME_WIDTH)

            val bytes = ByteArrayOutputStream()
            rawTakenImage.compress(Bitmap.CompressFormat.JPEG, 55, bytes)
            val resizedFile = getPhotoFileUri(System.currentTimeMillis().toString() + ".jpg")
            resizedFile.createNewFile()
            val fos = FileOutputStream(resizedFile)
            fos.write(bytes.toByteArray())
            fos.close()

            uploadFileToGDrive(resizedFile, takenPhotoUri)

            val takenImage = BitmapFactory.decodeFile(resizedFile.absolutePath)
//            val image =
//                RotateImage.handleSamplingAndRotationBitmap(
//                    requireActivity(),
//                    Uri.fromFile(photoFile)
//                )
//
//            addToTakenPhotoUris(takenPhotoUri)
//            addToTakenPhotosList(resizedFile)

//            if (image != null) {
//                images.add(image)
//            }
//            if (takenPhotoCount == 1) {
//                binding.firstPhotoLayout.photoImageView
//                    .setImageBitmap(image)
//            }
//            if (takenPhotoCount == 2) {
//                binding.secondPhotoLayout.photoImageView
//                    .setImageBitmap(image)
//            }
//            if (takenPhotoCount == 3) {
//                binding.thirdPhotoLayout.photoImageView
//                    .setImageBitmap(image)
//            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onStart() {
        super.onStart()
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null) {
            Toast.makeText(this@CapturePhotoActivity, "Already Signed In", Toast.LENGTH_SHORT)
                .show()
        } else {
            Toast.makeText(this@CapturePhotoActivity, "Please sign iN", Toast.LENGTH_SHORT).show()
        }
    }

    private fun askCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                this@CapturePhotoActivity,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(
                    this@CapturePhotoActivity,
                    Manifest.permission.CAMERA
                )
            ) {
                ActivityCompat.requestPermissions(
                    this@CapturePhotoActivity,
                    arrayOf(
                        Manifest.permission.CAMERA, readImagePermission
                    ),
                    CAMERA_PERMISSION_REQUEST_CODE
                )
            } else {
                ActivityCompat.requestPermissions(
                    this@CapturePhotoActivity,
                    arrayOf(
                        Manifest.permission.CAMERA
                    ),
                    CAMERA_PERMISSION_REQUEST_CODE
                )
            }
        }
    }
}