package ashraf.googledriveuploadsample.product_capture

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import ashraf.googledriveuploadsample.databinding.FragmentProductCaptureBinding


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
        Toast.makeText(requireActivity(), "Photo Capture", Toast.LENGTH_SHORT).show()
    }

    companion object {

        @JvmStatic
        fun newInstance() =
            ProductCaptureFragment()
    }
}