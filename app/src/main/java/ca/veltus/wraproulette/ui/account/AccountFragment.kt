package ca.veltus.wraproulette.ui.account

import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import ca.veltus.wraproulette.R
import ca.veltus.wraproulette.authentication.LoginSignupViewModel
import ca.veltus.wraproulette.base.BaseFragment
import ca.veltus.wraproulette.databinding.FragmentAccountBinding
import ca.veltus.wraproulette.utils.ExifUtil.rotateBitmap
import ca.veltus.wraproulette.utils.FirebaseStorageUtil
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream


@AndroidEntryPoint
class AccountFragment : BaseFragment() {

    override val _viewModel by viewModels<LoginSignupViewModel>()

    private lateinit var selectedImageBytes: ByteArray
    private var _binding: FragmentAccountBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    // Registers a photo picker activity launcher in single-select mode.
    private val photoPicker =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            // Callback is invoked after the user selects a media item or closes the photo picker.
            if (uri != null) {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(
                        ImageDecoder.createSource(
                            requireActivity().contentResolver, uri
                        )
                    )
                } else {
                    // Rotate the image to the correct orientation before compressing.
                    @Suppress("DEPRECATION") rotateBitmap(
                        getPathFromUri(uri),
                        MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, uri)
                    )
                }

                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
                selectedImageBytes = outputStream.toByteArray()
                _viewModel.setTemporaryProfileImage(selectedImageBytes)

            } else {
//                Log.d("PhotoPicker", "No media selected")
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_account, container, false)

        setupOnClickListeners()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = _viewModel
        viewLifecycleOwner.lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    _viewModel.userAccount.collectLatest {
                        if (it != null) {
                            // If the user has uploaded a profile image, retrieve it from Firebase Storage and replace the placeholder.
                            if (it.profilePicturePath != null) {
                                Glide.with(this@AccountFragment).asBitmap()
                                    .load(FirebaseStorageUtil.pathToReference(it.profilePicturePath))
                                    .placeholder(R.drawable.ic_baseline_account_circle_24)
                                    .into(binding.profilePictureImageView)
                            }
                        }
                    }
                }
                launch {
                    // Once a new profile image is selected load it into the card view.
                    _viewModel.tempProfileImage.collectLatest {
                        if (it != null) {
                            selectedImageBytes = it
                            Glide.with(this@AccountFragment).asBitmap().load(selectedImageBytes)
                                .into(binding.profilePictureImageView)
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupOnClickListeners() {
        binding.profilePictureImageView.setOnClickListener {
            // Launch the photo picker and allow the user to choose only images.
            photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    /**
     * Used to convert the image uri generated from the photo picker to a string path.
     */
    @Suppress("SENSELESS_COMPARISON")
    private fun getPathFromUri(uri: Uri?): String {
        var result: String? = null
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor: Cursor =
            requireActivity().contentResolver.query(uri!!, projection, null, null, null)!!
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(projection[0])
                result = cursor.getString(columnIndex)
            }
            cursor.close()
        }
        if (result == null) {
            result = getString(R.string.notFound)
        }
        return result
    }
}