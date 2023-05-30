package ashraf.googledriveuploadsample

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.viewinterop.AndroidViewBinding
import ashraf.googledriveuploadsample.databinding.MainLayoutBinding
import ashraf.googledriveuploadsample.ui.theme.GoogleDriveUploadSampleTheme
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnFailureListener

class MainActivity : ComponentActivity() {

    var oneTapClient: SignInClient? = null
    var signUpRequest: BeginSignInRequest? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            oneTapClient = Identity.getSignInClient(this)
            signUpRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(
                    BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true) // Your server's client ID, not your Android client ID.
                        .setServerClientId(getString(R.string.web_client_id)) // Show all accounts on the device.
                        .setFilterByAuthorizedAccounts(false)
                        .build()
                )
                .build()
//            val activityResultLauncher: ActivityResultLauncher<IntentSenderRequest> =
//                registerForActivityResult(
//                    ActivityResultContracts.StartIntentSenderForResult()
//                ) { result ->
//                    if (result.resultCode == RESULT_OK) {
//                        try {
//                            val credential =
//                                oneTapClient!!.getSignInCredentialFromIntent(result.data)
//                            val idToken = credential.googleIdToken
//                            if (idToken != null) {
//                                val email = credential.id
//                                Toast.makeText(
//                                    applicationContext,
//                                    "Email: $email",
//                                    Toast.LENGTH_SHORT
//                                ).show()
//                            }
//                        } catch (e: ApiException) {
//                            e.printStackTrace()
//                        }
//                    }
//                }
            GoogleDriveUploadSampleTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    // XmlView("Hello")
                    AndroidViewBindingExample(
                        oneTapClient,
                        signUpRequest,
                        this@MainActivity
                    )
                }
            }
        }

    }
}

@Composable
fun XmlView(text: String) {
    AndroidView(
        factory = { context ->
            val view = LayoutInflater.from(context).inflate(R.layout.main_layout, null, false)

            // do whatever you want...
            view // return the view
        },
        update = { view ->

            val textView = view.findViewById<TextView>(R.id.mTextView)
            textView.setText(text)
            textView.setOnClickListener {

            }
        }
    )
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}


@Composable
fun AndroidViewBindingExample(
    oneTapClient: SignInClient?,
    signUpRequest: BeginSignInRequest?,
    mainActivity: MainActivity
) {
    AndroidViewBinding(MainLayoutBinding::inflate) {
        mTextView.text = "How are you??"
        mTextView.setOnClickListener {
            if (signUpRequest != null) {
                oneTapClient?.beginSignIn(signUpRequest)
                    ?.addOnSuccessListener(
                        mainActivity
                    ) { result ->
                        Log.d("TAG", "Success")
//                        val intentSenderRequest: IntentSenderRequest =
//                            IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
//                        activityResultLauncher.launch(intentSenderRequest)
                    }
                    ?.addOnFailureListener(
                        mainActivity,
                        OnFailureListener { e -> // No Google Accounts found. Just continue presenting the signed-out UI.
                            e.localizedMessage?.let { it1 -> Log.d("TAG", it1) }
                        })
            }
        }
    }
}
