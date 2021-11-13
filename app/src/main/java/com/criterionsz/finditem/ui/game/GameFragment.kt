package com.criterionsz.finditem.ui.game

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.criterionsz.finditem.Constants
import com.criterionsz.finditem.databinding.FragmentSecondBinding
import com.criterionsz.finditem.repository.AmazonRepository
import com.criterionsz.finditem.ui.game.adapters.LiveAdapter
import java.nio.ByteBuffer


/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class GameFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null
    private var imageCapture: ImageCapture? = null
    private lateinit var vm: GameViewModel
    private lateinit var vmFactory: GameViewModelFactory
    private lateinit var adapter: LiveAdapter
    private val wrongAnswers = mutableListOf<Int>()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val permReqLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions.entries.all {
                it.value == true
            }
            if (granted) {
                showCamera()
            }
        }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vmFactory = GameViewModelFactory(AmazonRepository())
        vm = ViewModelProvider(this, vmFactory)
            .get(GameViewModel::class.java)
        binding.vm = vm
        binding.lifecycleOwner = this

        adapter = LiveAdapter(mutableListOf())
        binding.liveList.adapter = adapter

        if (allPermissionsGranted()) {
            showCamera()
        } else {
            permReqLauncher.launch(
                Constants.REQUIRED_PERMISSIONS
            )
        }

        binding.takePhoto.setOnClickListener {
            binding.takePhoto.visibility = View.GONE
            binding.progress.visibility = View.VISIBLE
            try {
                takePhoto()
            } catch (e: java.lang.Exception) {
                Log.v("GameFragment", e.message ?: "")
            }
        }

        binding.exit.setOnClickListener {
            findNavController().popBackStack()
        }

        vm.currentTime.observe(viewLifecycleOwner) {
            if (it <= 0) {
                vm.onSkip()
                vm.resetTimer()
            }
        }

        binding.restart.setOnClickListener {
            wrongAnswers.clear()
            adapter.submit(wrongAnswers)
            vm.startGame()
            binding.restart.visibility = View.GONE
        }

        binding.rightAnswer.visibility = View.GONE
        binding.wrongAnswer.visibility = View.GONE
        binding.progress.visibility = View.GONE
        binding.takePhoto.visibility = View.VISIBLE

        vm.defaultView.observe(viewLifecycleOwner) {
            it?.let {
                if (it) {
                    binding.rightAnswer.visibility = View.GONE
                    binding.wrongAnswer.visibility = View.GONE
                    binding.progress.visibility = View.GONE
                    binding.takePhoto.visibility = View.VISIBLE
                }
            }
        }

        vm.isCorrectAnswer.observe(viewLifecycleOwner) {
            it?.let {
                when {
                    it -> {
                        binding.rightAnswer.visibility = View.VISIBLE
                        binding.wrongAnswer.visibility = View.GONE
                        binding.progress.visibility = View.VISIBLE
                        binding.takePhoto.visibility = View.GONE
                    }
                    else -> {
                        binding.rightAnswer.visibility = View.GONE
                        binding.wrongAnswer.visibility = View.VISIBLE
                        binding.progress.visibility = View.VISIBLE
                        binding.takePhoto.visibility = View.GONE
                        wrongAnswers.add(Constants.WRONG_HEART_IMAGE)
                        adapter.submit(wrongAnswers)
                    }
                }
            }
        }
        vm.beforeStartTime.observe(viewLifecycleOwner) {
            binding.mainTitle.visibility = View.VISIBLE
            val seconds = DateUtils.formatElapsedTime(it).split(":")[1][1].toString()
            binding.mainTitle.text = seconds
            if (seconds == "0") {
                binding.mainTitle.visibility = View.GONE
                binding.progress.visibility = View.GONE
                binding.score.visibility = View.VISIBLE
                binding.takePhoto.visibility = View.VISIBLE
                binding.countDown.visibility = View.VISIBLE
                vm.resetTimer()
            }
        }

        vm.eventGameFinish.observe(viewLifecycleOwner) {
            binding.restart.visibility = View.VISIBLE
            binding.wrongAnswer.visibility = View.GONE
            binding.takePhoto.visibility = View.GONE
            binding.countDown.visibility = View.GONE
            binding.mainTitle.visibility = View.VISIBLE
            when (it) {
                GameViewModel.GameState.WON -> {
                    binding.mainTitle.text =
                        "Congratulations! \n You won! \n Press button to play again"
                }
                GameViewModel.GameState.LOST -> {
                    binding.mainTitle.text = "Game over \n Press button to restart"
                }
            }
        }
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun takePhoto() {
        val imageCapture = imageCapture ?: return


        imageCapture.takePicture(ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    super.onCaptureSuccess(image)
                    val temp = image.image!!.planes[0].buffer.toByteArray().copyOf()


                    val tempBuffer: ByteBuffer = ByteBuffer.wrap(temp)

                    image.close()

                    vm.getResult(tempBuffer)

                    Log.v("SecondFragment", "GOT IT")

                }

                override fun onError(exception: ImageCaptureException) {
                    super.onError(exception)
                    Log.v("SecondFragment", exception.message ?: "")
                }
            }

        )
    }

    private fun ByteBuffer.toByteArray(): ByteArray {
        rewind()    // Rewind the buffer to zero
        val data = ByteArray(remaining())
        get(data)   // Copy the buffer into a byte array
        return data // Return the byte array
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        //cameraExecutor.shutdown()
    }

    private fun showCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also { mPreview ->
                mPreview.setSurfaceProvider(
                    binding.previewView.surfaceProvider
                )
            }
            imageCapture = ImageCapture.Builder().build()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    viewLifecycleOwner, cameraSelector, preview, imageCapture
                )

            } catch (e: Exception) {
                Log.e("SecondFragment", e.message ?: "")

            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun allPermissionsGranted() =
        Constants.REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(
                requireContext(),
                it
            ) == PackageManager.PERMISSION_GRANTED
        }

}