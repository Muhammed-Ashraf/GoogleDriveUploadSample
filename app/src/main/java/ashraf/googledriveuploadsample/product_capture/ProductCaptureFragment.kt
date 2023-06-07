package ashraf.googledriveuploadsample.product_capture

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import ashraf.googledriveuploadsample.R
import ashraf.googledriveuploadsample.databinding.FragmentProductCaptureBinding
import ashraf.googledriveuploadsample.ui.CapturePhotoActivity
import ashraf.googledriveuploadsample.ui.REQUEST_IMAGE_CAPTURE
import ashraf.googledriveuploadsample.util.RotateImage
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


/**
 * A simple [Fragment] subclass.
 * Use the [ProductCaptureFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProductCaptureFragment : Fragment() {

    val adapter = PhotoAdapter()
    private lateinit var binding: FragmentProductCaptureBinding

    private var photoFile: File? = null
    private lateinit var photoFileName: String
    private var takenPhotoUriList = ArrayList<Uri>(emptyList())
    private var takenImages = ArrayList<File>(emptyList())
    val images = ArrayList<Bitmap>(emptyList())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentProductCaptureBinding.inflate(requireActivity().layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.addPhotoHeaderLayout.visibility = View.INVISIBLE
        val layoutManager = GridLayoutManager(requireContext(), 2)
        binding.mRecyclerView.layoutManager = layoutManager
        binding.mRecyclerView.adapter = adapter

        val barcodeLauncher = registerForActivityResult(
            ScanContract()
        ) { result: ScanIntentResult ->
            if (result.contents == null) {
                Toast.makeText(requireActivity(), "Cancelled", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(
                    requireActivity(), result.contents, Toast.LENGTH_LONG
                ).show()
                takenPhotoUriList.clear()
                takenImages.clear()
                images.clear()
                adapter.setData(images)
                binding.uploadButton.visibility = View.GONE
                binding.imeiTextView.text = result.contents
                binding.addPhotoHeaderLayout.visibility = View.VISIBLE
            }
        }
        binding.imeiDataLayout.setOnClickListener {
            barcodeLauncher.launch(ScanOptions())
        }

        binding.addPhotoImageView.setOnClickListener {
            openCamera()
        }

        binding.uploadButton.setOnClickListener {
            if (binding.imeiTextView.text.isEmpty()) {
                Toast.makeText(requireContext(), "Scan Barcode", Toast.LENGTH_SHORT).show()
            } else {
                binding.uploadButton.visibility = View.INVISIBLE
                binding.mProgressbar.visibility = View.VISIBLE
                binding.addPhotoHeaderLayout.visibility = View.INVISIBLE
                createFolder(requireContext())
            }
        }

    }

    fun openCamera() {
        if (ContextCompat.checkSelfPermission(
                requireActivity(), Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(
                requireActivity(), "Grand Camera Permission", Toast.LENGTH_SHORT
            ).show()
        } else {
            dispatchTakePictureIntent()
        }
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
//            updateIntentForCameraFacing(takePictureIntent, false)
            requireActivity().let { it ->
                takePictureIntent.resolveActivity(it.packageManager)?.also {
                    // Create the File where the photo should go
                    photoFile = try {
                        photoFileName = System.currentTimeMillis().toString() + ".jpg"
                        getPhotoFileUri(photoFileName)
                    } catch (ex: IOException) {
                        null
                    }
                    // Continue only if the File was successfully created
                    photoFile?.also {
                        val photoURI: Uri = FileProvider.getUriForFile(
                            requireActivity(),
                            requireActivity().applicationContext.packageName.toString() + ".provider",
                            it
                        )
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                        startActivityForResult(
                            takePictureIntent, REQUEST_IMAGE_CAPTURE
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
        val mediaStorageDir = File(
            requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "captures"
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


            //    val takenImage = BitmapFactory.decodeFile(resizedFile.absolutePath)
            val image = RotateImage.handleSamplingAndRotationBitmap(
                requireActivity(), Uri.fromFile(photoFile)
            )

            if (image != null) {
                addToTakenPhotoUris(takenPhotoUri)
                addToTakenPhotosList(resizedFile)
                images.add(image)
                adapter.setData(images)
                binding.uploadButton.visibility = View.VISIBLE
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }


    private fun addToTakenPhotoUris(takenPhotoUri: Uri?) {
        if (takenPhotoUri != null) {
            takenPhotoUriList.add(takenPhotoUri)
        }
    }

    private fun addToTakenPhotosList(file: File?) {
        if (file != null) {
            takenImages.add(file)
        }
    }

    fun createFolder(context: Context) {
        GoogleSignIn.getLastSignedInAccount(context)?.let { googleAccount ->

            // get credentials
            val credential = GoogleAccountCredential.usingOAuth2(
                context, listOf(DriveScopes.DRIVE, DriveScopes.DRIVE_FILE)
            )
            credential.selectedAccount = googleAccount.account!!

            // get Drive Instance
            val drive = Drive
                .Builder(
                    AndroidHttp.newCompatibleTransport(),
                    JacksonFactory.getDefaultInstance(),
                    credential
                )
                .setApplicationName(context.getString(R.string.app_name))
                .build()
            CoroutineScope(Dispatchers.IO).launch {
                val gFolder = com.google.api.services.drive.model.File()
                // Set file name and MIME
                gFolder.name = binding.imeiTextView.text.toString()
                gFolder.mimeType = "application/vnd.google-apps.folder"

                // You can also specify where to create the new Google folder
                // passing a parent Folder Id
                val parents: MutableList<String> = ArrayList(1)
//            parents.add("your_parent_folder_id_here")
//            gFolder.parents = parents
                val file = drive.Files().create(gFolder).setFields("id").execute()
                uploadFiles(drive, file.id)
            }
        }
    }

    fun uploadFiles(drive: Drive, id: String) {
        drive.Files().list()
        for (file in takenImages) {
            val gfile = com.google.api.services.drive.model.File()

            val fileContent = FileContent("image/jpeg", file)
            gfile.name = file.name

            val parents: MutableList<String> = ArrayList(1)
            parents.add(id) // Here you need to get the parent folder id

            gfile.parents = parents

            drive.Files().create(gfile, fileContent).setFields("id").execute()
        }
        resetFields()
    }

    private fun resetFields() {
        activity?.runOnUiThread {
            binding.imeiTextView.text = ""
            binding.addPhotoHeaderLayout.visibility = View.GONE
            takenPhotoUriList.clear()
            takenImages.clear()
            images.clear()
            adapter.setData(images)
            binding.uploadButton.visibility = View.GONE
            binding.mProgressbar.visibility = View.GONE
        }

    }

    companion object {

        @JvmStatic
        fun newInstance() = ProductCaptureFragment()
    }
}