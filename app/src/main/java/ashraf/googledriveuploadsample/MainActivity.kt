package ashraf.googledriveuploadsample

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.viewinterop.AndroidViewBinding
import ashraf.googledriveuploadsample.databinding.MainLayoutBinding
import ashraf.googledriveuploadsample.ui.theme.GoogleDriveUploadSampleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GoogleDriveUploadSampleTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    // XmlView("Hello")
                    AndroidViewBindingExample()
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
        }
    )
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun AndroidViewBindingExample() {
    AndroidViewBinding(MainLayoutBinding::inflate) {
        mTextView.text = "How are you??"
    }
}
