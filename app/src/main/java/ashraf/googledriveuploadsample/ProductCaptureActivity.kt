package ashraf.googledriveuploadsample

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ashraf.googledriveuploadsample.databinding.ActivityProductCaptureBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn

class ProductCaptureActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProductCaptureBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductCaptureBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null) {
            Toast.makeText(this@ProductCaptureActivity, "Already Signed In", Toast.LENGTH_SHORT)
                .show()
        } else {
            supportFragmentManager.beginTransaction()
                .replace(binding.mFragmentContainerView.id, GoogleSignInFragment.newInstance())
                .commit()
        }
    }
}