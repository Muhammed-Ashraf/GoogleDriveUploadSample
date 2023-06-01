package ashraf.googledriveuploadsample

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ashraf.googledriveuploadsample.ui.theme.GoogleDriveUploadSampleTheme
import ashraf.googledriveuploadsample.util.Helpers
import ashraf.googledriveuploadsample.util.findActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UploadActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GoogleDriveUploadSampleTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    ImagePicker()
                }
            }
        }
    }

//    val startForResult = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
//        if (result.resultCode == Activity.RESULT_OK) {
//            val intent = result.data
//            if (result.data != null) {
//                val task: Task<GoogleSignInAccount> =
//                    GoogleSignIn.getSignedInAccountFromIntent(intent)
//
//                /**
//                 * handle [task] result
//                 */
//            } else {
//                Toast.makeText(ctx, "Google Login Error!", Toast.LENGTH_LONG).show()
//            }
//        }
//    }

}

@Preview(showBackground = true)
@Composable
fun ImagePicker(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val activity = context.findActivity()
    val startForResult =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data
                if (result.data != null) {
                    val task: Task<GoogleSignInAccount> =
                        GoogleSignIn.getSignedInAccountFromIntent(intent)
                    createFolder(context)
                } else {
                    Toast.makeText(context, "Google Login Error!", Toast.LENGTH_LONG).show()
                }
            }
        }
    Box(
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
//            Button(
//                onClick = { /* TODO */ },
//            ) {
//                Text(
//                    text = "Select Image"
//                )
//            }
//            Button(
//                modifier = Modifier.padding(top = 16.dp),
//                onClick = { /* TODO */ },
//            ) {
//                Text(
//                    text = "Take photo"
//                )
//            }
            Button(
                onClick = {
                    startForResult.launch(Helpers().getGoogleSignInClient(activity).signInIntent)
                },
            ) {
                Text(text = "Sign in with Google")
            }
        }
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
            gFolder.name = "My Cool Folder Name"
            gFolder.mimeType = "application/vnd.google-apps.folder"

            // You can also specify where to create the new Google folder
            // passing a parent Folder Id
            val parents: MutableList<String> = ArrayList(1)
//            parents.add("your_parent_folder_id_here")
//            gFolder.parents = parents
            drive.Files().create(gFolder).setFields("id").execute()
        }
    }
}

