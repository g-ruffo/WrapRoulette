package ca.veltus.wraproulette.ui.account

import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    companion object {
        private const val TAG = "AccountFragment"
    }

    override val _viewModel by viewModels<LoginSignupViewModel>()

    private val RC_SELECT_IMAGE = 2
    private lateinit var selectedImageBytes: ByteArray
    private var pictureChange = false

    private var _binding: FragmentAccountBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

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

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                _viewModel.userAccount.collectLatest {
                    if (it != null) {
                        if (this@AccountFragment.isVisible) {
                            binding.nameInputEditText.setText(it.displayName)
                            binding.departmentInputEditText.setText(it.department)

                            if (!pictureChange && it.profilePicturePath != null) {
                                Glide.with(this@AccountFragment).asBitmap()
                                    .load(FirebaseStorageUtil.pathToReference(it.profilePicturePath))
                                    .placeholder(R.drawable.ic_baseline_account_circle_24)
                                    .into(binding.profilePictureImageView)
                            }
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

    override fun onStart() {
        super.onStart()
    }

    // TODO: Replace with ActivityResultContract
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // TODO: Fix Photo Orientation
        if (requestCode == RC_SELECT_IMAGE && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            val selectedImagePath = data.data
            val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)

            val cursor: Cursor = activity?.contentResolver!!.query(
                selectedImagePath!!, filePathColumn, null, null, null
            )!!
            cursor.moveToFirst()
            val columnIndex: Int = cursor.getColumnIndex(filePathColumn[0])
            val picturePath: String = cursor.getString(columnIndex)
            cursor.close()
            val bitmap = BitmapFactory.decodeFile(picturePath)

            val rotatedBitmap = rotateBitmap(picturePath, bitmap)

            val outputStream = ByteArrayOutputStream()
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            selectedImageBytes = outputStream.toByteArray()

            Glide.with(this).asBitmap().load(selectedImageBytes)
                .into(binding.profilePictureImageView)

            pictureChange = true
        }
    }

    private fun setupOnClickListeners() {
        binding.apply {
            profilePictureImageView.setOnClickListener {
                val intent = Intent().apply {
                    type = "image/*"
                    action = Intent.ACTION_PICK
                    putExtra(
                        Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png", "image/gif")
                    )
                }
                // TODO: Replace with ActivityResultContract
                startActivityForResult(
                    Intent.createChooser(intent, "Select Image"), RC_SELECT_IMAGE
                )

            }
            saveButton.setOnClickListener {
                it.isEnabled = false
                _viewModel.showLoading.value = true
                if (::selectedImageBytes.isInitialized) {
                    FirebaseStorageUtil.uploadProfilePhoto(selectedImageBytes) { imagePath ->
                        _viewModel.updateCurrentUser(
                            binding.nameInputEditText.text.toString(),
                            binding.departmentInputEditText.text.toString(),
                            imagePath
                        )
                    }
                } else {
                    _viewModel.updateCurrentUser(
                        binding.nameInputEditText.text.toString(),
                        binding.departmentInputEditText.text.toString(),
                        null
                    )

                }
            }
        }
    }

}