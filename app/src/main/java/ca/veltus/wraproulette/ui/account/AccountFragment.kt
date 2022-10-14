package ca.veltus.wraproulette.ui.account

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import ca.veltus.wraproulette.R
import ca.veltus.wraproulette.base.BaseFragment
import ca.veltus.wraproulette.databinding.FragmentAccountBinding
import ca.veltus.wraproulette.databinding.FragmentPoolsBinding
import ca.veltus.wraproulette.ui.pools.PoolsViewModel
import ca.veltus.wraproulette.utils.FirebaseStorageUtil
import ca.veltus.wraproulette.utils.FirestoreUtil
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import java.io.ByteArrayOutputStream

@AndroidEntryPoint
class AccountFragment : BaseFragment() {
    companion object {
        private const val TAG = "AccountFragment"
    }

    override val _viewModel by viewModels<PoolsViewModel>()

    private val RC_SELECT_IMAGE = 2
    private lateinit var selectedImageBytes: ByteArray
    private var pictureChange = false

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseReference: FirebaseFirestore

    private var _binding: FragmentAccountBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_account, container, false)

        databaseReference = FirebaseFirestore.getInstance()


        setupOnClickListeners()

        return binding.root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStart() {
        super.onStart()
        FirestoreUtil.getCurrentUser { user ->
            if (this@AccountFragment.isVisible) {
                binding.nameInputEditText.setText(user.displayName)
                binding.departmentInputEditText.setText(user.department)

                if (!pictureChange && user.profilePicturePath != null) {
                    Glide.with(this).load(FirebaseStorageUtil.pathToReference(user.profilePicturePath))
                        .placeholder(R.drawable.ic_baseline_account_circle_24)
                        .into(binding.profilePictureImageView)
                }
            }
        }
    }

    // TODO: Replace with ActivityResultContract
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SELECT_IMAGE && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            val selectedImagePath = data.data
            val selectedImageBmp =
                MediaStore.Images.Media.getBitmap(activity?.contentResolver, selectedImagePath)

            val outputStream = ByteArrayOutputStream()
            selectedImageBmp.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            selectedImageBytes = outputStream.toByteArray()

            Glide.with(this).load(selectedImageBytes)
                .into(binding.profilePictureImageView)

            pictureChange = true
        }
    }

    private fun setupOnClickListeners() {
        binding.apply {
            profilePictureImageView.setOnClickListener {
                val intent = Intent().apply {
                    type = "image/*"
                    action = Intent.ACTION_GET_CONTENT
                    putExtra(
                        Intent.EXTRA_MIME_TYPES,
                        arrayOf("image/jpeg", "image/png", "image/gif")
                    )
                }
                // TODO: Replace with ActivityResultContract
                startActivityForResult(
                    Intent.createChooser(intent, "Select Image"),
                    RC_SELECT_IMAGE
                )

            }
            saveButton.setOnClickListener {
                if (::selectedImageBytes.isInitialized) {
                    FirebaseStorageUtil.uploadProfilePhoto(selectedImageBytes) { imagePath ->
                        FirestoreUtil.updateCurrentUser(
                            FirebaseAuth.getInstance().currentUser?.uid!!,
                            binding.nameInputEditText.text.toString(),
                            FirebaseAuth.getInstance().currentUser?.email!!,
                            binding.departmentInputEditText.text.toString(),
                            imagePath,
                            null
                        )
                    }
                }
                else {
                    FirestoreUtil.updateCurrentUser(
                        FirebaseAuth.getInstance().currentUser?.uid!!,
                        binding.nameInputEditText.text.toString(),
                        FirebaseAuth.getInstance().currentUser?.email!!,
                        binding.departmentInputEditText.text.toString(),
                        null,
                        null
                    )
                }
            }
        }
    }




}