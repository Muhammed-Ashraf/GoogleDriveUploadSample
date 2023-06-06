package ashraf.googledriveuploadsample.product_capture

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import ashraf.googledriveuploadsample.databinding.FragmentProductCaptureBinding
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions


/**
 * A simple [Fragment] subclass.
 * Use the [ProductCaptureFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProductCaptureFragment : Fragment() {


    private lateinit var binding: FragmentProductCaptureBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentProductCaptureBinding.inflate(requireActivity().layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val barcodeLauncher = registerForActivityResult(
            ScanContract()
        ) { result: ScanIntentResult ->
            if (result.contents == null) {
                Toast.makeText(requireActivity(), "Cancelled", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(
                    requireActivity(),
                    result.contents,
                    Toast.LENGTH_LONG
                ).show()
                binding.imeiTextView.text = result.contents
            }
        }
        binding.imeiDataLayout.setOnClickListener {
            barcodeLauncher.launch(ScanOptions())
        }

    }

    companion object {

        @JvmStatic
        fun newInstance() =
            ProductCaptureFragment()
    }
}