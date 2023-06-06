package ashraf.googledriveuploadsample.product_capture

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProductCaptureViewModel @Inject constructor() : ViewModel() {

    private val navigateToCapture = MutableLiveData<Boolean>()
    fun navigateToCaptureListener() = navigateToCapture

    fun navigateToCapturePage() {
        navigateToCapture.value = true
        navigateToCapture.value = false
    }
}