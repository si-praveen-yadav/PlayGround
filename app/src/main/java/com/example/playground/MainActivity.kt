package com.example.playground

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.bold
import androidx.core.text.color
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.playground.databinding.ActivityMainBinding
import com.example.playground.databinding.ItemUserBinding


class MainActivity : AppCompatActivity() {
    companion object{
        const val TAG = "MainActivity"
    }
    private lateinit var binding: ActivityMainBinding

    private var messages = mutableListOf<Message>()
    private var users = mutableListOf<User>()

    private lateinit var rvUsersAdapter: SimpleListAdapter<ItemUserBinding, User>

    private val viewRegex = "@\\{([A-Z][a-z])?\\w+(\\s([A-Z][a-z])?(\\w+)?)?\\}"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initUsers()
        setupRvMessages()
        setupRvUsers()
        setupEditMessage()
    }

    private fun setupEditMessage() {
        binding.etMessage.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                /*Log.i(TAG,"onTextChanged : CharSequence > $s")
                Log.i(TAG,"onTextChanged : start > $start")
                Log.i(TAG,"onTextChanged : before > $before")
                Log.i(TAG,"onTextChanged : count > $count")
                Log.i(TAG,"onTextChanged : cursor position > ${binding.etMessage.selectionStart}")
                */

                Log.i(TAG,"onTextChanged : Mentioned Users > ${findMentionedUsers(s.toString())}")

                val cPosition = binding.etMessage.selectionStart
                if(!s.isNullOrEmpty()){
                    filterMatchedUsers(findMentionedUsers(s.toString().substring(0,cPosition)))
                }

            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })
    }

    private fun filterMatchedUsers(matches:List<String>){
        val filteredData = mutableListOf<User>()
        val fMatches = matches.map {
            it.substring(1)
        }

        users.forEach {user->
            if(fMatches.isNotEmpty() && user.name.startsWith(fMatches.last(), ignoreCase = true)) filteredData.add(user)
        }

        rvUsersAdapter.submitList(filteredData)
    }

    fun findMentionedUsers(inputText: String): List<String> {
        val regex = Regex("@([A-Z][a-z])?\\w+(\\s([A-Z][a-z])?(\\w+)?)?")
        val mentions = regex.findAll(inputText)
        return mentions.map { it.value }.toList()
    }

    fun fetchViewMentionedUsers(inputText: String): List<String> {
        val regex = Regex(viewRegex)
        val mentions = regex.findAll(inputText)
        return mentions.map { it.value }.toList()
    }

    private fun setEditTextMentions(user: User){
        val selectedUser = user.name
        val currentText = binding.etMessage.text.toString()
        val mentionText = "@{$selectedUser} "

        val mentionStartIndex = currentText.substring(0,binding.etMessage.selectionStart).lastIndexOf("@")
        val newText =
            currentText.replaceRange(
                mentionStartIndex,
                binding.etMessage.selectionStart,
                mentionText
            )


        /*val spans = fetchViewMentionedUsers(newText)


        spans.forEach {

        }*/

        val spans = fetchViewMentionedUsers(newText)
        Log.i(TAG,"setEditTextMentions : Matched Spans > $spans")
        var iCount = 0
        val mySpan = SpannableStringBuilder()
        val items = newText.split(Regex(viewRegex))
        Log.i(TAG,"setEditTextMentions : Matched Items > $items")
        items.forEach {
            if(it == " "){
                if(iCount<spans.size){
                    mySpan.bold {
                        append(spans[iCount])
                        iCount++
                    }
                }
            }else{
                mySpan.append(it)
            }
        }
        binding.etMessage.setText(mySpan)
        binding.etMessage.setSelection(mySpan.length)
    }

    fun getColoredString(context: Context?, text: CharSequence?, color: Int): Spannable {
        val spannable: Spannable = SpannableString(text)
        spannable.setSpan(
            ForegroundColorSpan(color),
            0,
            spannable.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return spannable
    }
    private fun setupRvUsers() {
        binding.rvUsers.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rvUsersAdapter = SimpleListAdapter(
            inflate = ItemUserBinding::inflate,
            onBind = { position, binding, data ->
                binding.apply {
                    tvName.text = data.name

                    root.setOnClickListener {
                        setEditTextMentions(data)
                    }
                }
            },
            itemComparator = object : DiffUtil.ItemCallback<User>() {
                override fun areItemsTheSame(
                    oldItem: User,
                    newItem: User
                ): Boolean {
                    return oldItem.userId == newItem.userId
                }

                override fun areContentsTheSame(
                    oldItem: User,
                    newItem: User
                ): Boolean {
                    return oldItem == newItem
                }
            })

        binding.rvUsers.adapter = rvUsersAdapter

        rvUsersAdapter.submitList(users)
    }

    private fun setupRvMessages() {

    }


    private fun initUsers() {
        users.clear()
        users.add(User("Praveen Yadav", 1))
        users.add(User("Meet Kachhadiya", 2))
        users.add(User("Valentine Miranda", 3))
        users.add(User("Vishal Thakur", 4))
        users.add(User("Bhakti Mamaniya", 5))
    }


}