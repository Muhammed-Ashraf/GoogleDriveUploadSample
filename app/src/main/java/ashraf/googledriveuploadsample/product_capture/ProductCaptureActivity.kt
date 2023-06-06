package ashraf.googledriveuploadsample.product_capture

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import ashraf.googledriveuploadsample.databinding.ActivityProductCaptureBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ProductCaptureActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProductCaptureBinding

    private val viewModel: ProductCaptureViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductCaptureBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setObservers()
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null) {
            supportFragmentManager.beginTransaction()
                .replace(binding.mFragmentContainerView.id, ProductCaptureFragment.newInstance())
                .commit()
        } else {
            supportFragmentManager.beginTransaction()
                .replace(binding.mFragmentContainerView.id, GoogleSignInFragment.newInstance())
                .commit()
        }
    }

    private fun setObservers() {
        viewModel.navigateToCaptureListener().observe(this) {
            if (it) {

            }
        }
    }
}