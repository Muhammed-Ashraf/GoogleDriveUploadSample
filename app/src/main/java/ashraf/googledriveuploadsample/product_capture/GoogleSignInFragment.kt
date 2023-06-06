package ashraf.googledriveuploadsample.product_capture

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.Fragment
import ashraf.googledriveuploadsample.databinding.FragmentGoogleSignInBinding
import ashraf.googledriveuploadsample.util.Helpers


/**
 * A simple [Fragment] subclass.
 * Use the [GoogleSignInFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GoogleSignInFragment : Fragment() {

    private lateinit var binding: FragmentGoogleSignInBinding
    private val viewModel: ProductCaptureViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentGoogleSignInBinding.inflate(requireActivity().layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val someActivityResultLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result?.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                Toast.makeText(requireActivity(), "Success", Toast.LENGTH_SHORT).show()
                viewModel.navigateToCapturePage()
            } else {
                Toast.makeText(
                    requireActivity(),
                    "Error, Cant Sign In to Selected Google Account",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }


        binding.mButton.setOnClickListener {
            someActivityResultLauncher.launch(Helpers().getGoogleSignInClient(requireActivity()).signInIntent)
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment GoogleSignInFragment.
         */
        @JvmStatic
        fun newInstance() =
            GoogleSignInFragment()

    }
}