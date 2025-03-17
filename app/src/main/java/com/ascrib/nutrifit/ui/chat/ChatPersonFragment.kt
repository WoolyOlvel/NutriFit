package com.ascrib.nutrifit.ui.chat

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.updateLayoutParams
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ascrib.nutrifit.ui.chat.adapter.ChatHeadAdapter
import com.ascrib.nutrifit.R
import com.ascrib.nutrifit.databinding.FragmentChatPersonBinding
import com.ascrib.nutrifit.model.Chat
import com.ascrib.nutrifit.ui.dashboard.viewmodel.DashboardViewModel
import com.ascrib.nutrifit.ui.dashboard.viewmodel.DashboardViewModelFactory
import com.ascrib.nutrifit.util.getStatusBarHeight
import java.util.*

class ChatPersonFragment : Fragment() {
    lateinit var binding: FragmentChatPersonBinding
    lateinit var model: DashboardViewModel

    lateinit var chatHeadAdapter: ChatHeadAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_chat_person, container, false)
        binding.handler = this

        model = ViewModelProvider(this, DashboardViewModelFactory())[DashboardViewModel::class.java]

        toolbarConfig()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        makeAppointment()
    }

    private fun toolbarConfig() {
        binding.toolbar.toolbar.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            setMargins(0, activity?.getStatusBarHeight()!!.plus(10), 0, 0)
        }

        val chatName = arguments?.getString("chat_name") ?: "Chat"
        val chatImage = arguments?.getInt("chat_image", R.drawable.big_square) ?: R.drawable.big_square
        binding.toolbar.toolbar.title = chatName
        binding.toolbar.toolbar.setTitleTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.toolbar.toolbar)

        (requireActivity() as AppCompatActivity).apply {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowHomeEnabled(true)
        }

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.header_chat, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    android.R.id.home -> {
                        findNavController().navigateUp()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    fun makeAppointment() {
        val chatList = model.getChat() // Obtener la lista de chats desde el ViewModel

        chatHeadAdapter = ChatHeadAdapter(chatList) // Pasar la lista al adaptador

        binding.recyclerViewChat.apply {
            adapter = chatHeadAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }

        binding.recyclerViewChat.smoothScrollToPosition(chatList.size)
    }


    fun replyClicked() {
        if (!binding.etMessage.text.toString().trim().contentEquals("")) {
            model.dataChat.add(
                Chat(
                    UUID.randomUUID().mostSignificantBits.toInt(),
                    "",
                    binding.etMessage.text.toString(),
                    R.drawable.perfil_prueba2,
                    "Ahora",
                    true,
                    true,
                    true

                )
            )

            chatHeadAdapter.notifyDataSetChanged()
            binding.recyclerViewChat.smoothScrollToPosition(model.dataChat.size)
            binding.etMessage.setText("")
        }
    }

    fun selectImage() {
        val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        takePhotoForResult.launch(gallery)
    }

    private val takePhotoForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data?.data
            }
        }
}