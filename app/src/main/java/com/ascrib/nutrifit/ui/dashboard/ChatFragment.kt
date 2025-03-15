package com.ascrib.nutrifit.ui.dashboard

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.updateLayoutParams
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ascrib.nutrifit.R
import com.ascrib.nutrifit.databinding.FragmentChatBinding
import com.ascrib.nutrifit.databinding.FragmentHomeBinding
import com.ascrib.nutrifit.handler.ChatHandler
import com.ascrib.nutrifit.model.Chat
import com.ascrib.nutrifit.ui.dashboard.adapter.ChatAdapter
import com.ascrib.nutrifit.ui.dashboard.adapter.ChatHeadAdapter
import com.ascrib.nutrifit.ui.dashboard.viewmodel.DashboardViewModel
import com.ascrib.nutrifit.ui.dashboard.viewmodel.DashboardViewModelFactory
import com.ascrib.nutrifit.util.getStatusBarHeight

class ChatFragment : Fragment(), ChatHandler {
    lateinit var binding: FragmentChatBinding

    lateinit var model: DashboardViewModel

    lateinit var chatAdapter: ChatAdapter
    lateinit var chatHeadAdapter: ChatHeadAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_chat, container, false)

        binding.handler = this

        model = ViewModelProvider(this, DashboardViewModelFactory())[DashboardViewModel::class.java]
        toolbarConfig()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        makeChat()
        makeChatHead()
    }

    private fun toolbarConfig() {
        binding.toolbar.toolbar.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            setMargins(0, activity?.getStatusBarHeight()!!.plus(10), 0, 0)
        }

        binding.toolbar.toolbar.title = "Mensajes"
        binding.toolbar.toolbar.setTitleTextColor(ContextCompat.getColor(requireContext(),R.color.black))
        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.toolbar.toolbar)

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Add menu items here
                menuInflater.inflate(R.menu.header_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    android.R.id.home -> {
                        findNavController().navigateUp()
                        true
                    }
                    R.id.action_notification ->{
                        findNavController().navigate(R.id.global_notificationFragment)
                        return true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    fun makeChat() {
        chatAdapter = ChatAdapter(model.getChat(), this)
        binding.recyclerviewChat.apply {
            adapter = chatAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }
    }

    fun makeChatHead() {
        chatHeadAdapter = ChatHeadAdapter(model.getChat(), this)
        binding.recyclerviewChatHead.apply {
            adapter = chatHeadAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }
    }

    override fun onChatClicked(chat: Chat) {
        val dashboardViewModel: DashboardViewModel by viewModels()

        // Obtener el chat correspondiente por ID
        val selectedChat = dashboardViewModel.getChat().find { it.id == chat.id }

        val chatName = selectedChat?.name ?: "Nombre no disponible"
        val chatImage = selectedChat?.img ?: R.drawable.big_square  // Imagen por defecto si no se encuentra

        val bundle = Bundle()
        bundle.putString("chat_name", chatName)
        bundle.putInt("chat_image", chatImage)

        findNavController().navigate(R.id.action_chatFragment_to_chatPersonFragment, bundle)
    }


}