package ca.veltus.wraproulette.ui.account

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
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
    private val STORAGE_REQUEST_CODE = 8
    private lateinit var selectedImageBytes: ByteArray
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
        binding.viewModel = _viewModel
        viewLifecycleOwner.lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    _viewModel.userAccount.collectLatest {
                        if (it != null) {
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SELECT_IMAGE && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            val selectedImagePath = data.data
            val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)

            val cursor: Cursor = requireActivity().contentResolver!!.query(
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
            _viewModel.setTemporaryProfileImage(selectedImageBytes)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_DENIED) {
            // Permission is denied
            _viewModel.showSnackBar.value =
                "You need to grant permissions to select a new profile image."
        }
    }

    private fun setupOnClickListeners() {
        binding.profilePictureImageView.setOnClickListener {
            if (arePermissionsGranted()) {
                val intent = Intent().apply {
                    type = "image/*"
                    action = Intent.ACTION_PICK
                    putExtra(
                        Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png", "image/gif")
                    )
                }
                startActivityForResult(
                    Intent.createChooser(intent, "Select Image"), RC_SELECT_IMAGE
                )
            } else {
                requestPermission()
            }
        }
    }

    private fun arePermissionsGranted() = ActivityCompat.checkSelfPermission(
        requireActivity(), READ_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED

    private fun requestPermission() {
        if (!arePermissionsGranted()) {
            val permissions = arrayOf(
                READ_EXTERNAL_STORAGE
            )
            requestPermissions(permissions, STORAGE_REQUEST_CODE)
        }
    }
}