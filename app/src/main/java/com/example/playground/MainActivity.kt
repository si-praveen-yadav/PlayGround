package com.example.playground

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.playground.databinding.ActivityMainBinding
import com.example.playground.databinding.ItemUserBinding


class MainActivity : AppCompatActivity() {
    companion object{
        const val TAG = "MainActivity"
        const val TAG_TEST = "MainActivityTest"
    }
    private lateinit var binding: ActivityMainBinding

    private var messages = mutableListOf<Message>()
    private var users = mutableListOf<User>()

    private lateinit var rvUsersAdapter: SimpleListAdapter<ItemUserBinding, User>

    private val viewRegex = "@\\{([A-Z][a-z])?\\w+(\\s([A-Z][a-z])?(\\w+)?)?\\}"

    private var displayString = ""
    private var cursorPosition = 0

//    private val mentionedUsers = mutableListOf<MentionUser>()

    private var mentionedUsers = mutableListOf<String>()
    private var messageString = ""

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
                Log.i(TAG,"beforeTextChanged : ${p0.toString()}")
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                /*Log.i(TAG,"onTextChanged : CharSequence > $s")
                Log.i(TAG,"onTextChanged : start > $start")
                Log.i(TAG,"onTextChanged : before > $before")
                Log.i(TAG,"onTextChanged : count > $count")
                Log.i(TAG,"onTextChanged : cursor position > ${binding.etMessage.selectionStart}")
                */
                Log.i(TAG,"onTextChanged : ${s.toString()}")
//                Log.i(TAG,"onTextChanged : Mentioned Users > ${findMentionedUsers(s.toString())}")

                cursorPosition = binding.etMessage.selectionStart
                updateMessageString(currentText = s.toString())
//                displayString = s.toString()
                if(!s.isNullOrEmpty()){
                    val foundMentions =findMentionedUsers(s.toString().substring(0,cursorPosition))
                    val updatedMentions = mutableListOf<String>()
                    mentionedUsers.forEachIndexed { index, s ->
                        if(foundMentions.contains(s)) updatedMentions.add(s)
                    }
                    mentionedUsers.clear()
                    mentionedUsers.addAll(updatedMentions)
                    filterMatchedUsers(foundMentions)
                }

            }

            override fun afterTextChanged(p0: Editable?) {
                Log.i(TAG,"afterTextChanged : ${p0.toString()}")
//                updateMentionedUsersIndexes()
            }
        })
    }

    private fun updateMessageString(currentText:String){

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

    private fun fetchViewMentionedUsers(inputText: String): List<String> {
        val regex = Regex(viewRegex)
        val mentions = regex.findAll(inputText)
        return mentions.map { it.value }.toList()
    }

    /*private fun updateMentionedUsersIndexes(){
        if(mentionedUsers.isEmpty()) return
        val items = mutableListOf<MentionUser>()
        mentionedUsers.forEachIndexed { index, mentionUser ->
            if(cursorPosition<mentionUser.startAt || cursorPosition>mentionUser.endAt){
                items.add(mentionUser)
            }
        }
        mentionedUsers.clear()
        mentionedUsers.addAll(items)

        Log.i(TAG,"")
    }*/
    private fun setEditTextMentions(user: User){
        val selectedUser = user.name
        val currentText = binding.etMessage.text.toString()
        val mentionText = "@$selectedUser "

        val mentionStartIndex = currentText.substring(0,cursorPosition).lastIndexOf("@")

        /*val mentionUserItem = MentionUser(mentionStartIndex,mentionStartIndex+mentionText.length,mentionText)
        mentionedUsers.add(mentionUserItem)*/

        val newText =
            currentText.replaceRange(
                mentionStartIndex,
                cursorPosition,
                mentionText
            )

        val spans = fetchViewMentionedUsers(newText)
        val coloredSpans = mutableListOf<Pair<Int,Int>>()
        mentionedUsers.forEach {
            val start = newText.indexOf(it)
            val end = start + it.length

            coloredSpans.add(Pair(start,end))
        }

        val mySpan = SpannableString(newText)
        val textColor: Int = ResourcesCompat.getColor(resources, R.color.blue, null)
        coloredSpans.forEach {
            mySpan.setSpan(
                ForegroundColorSpan(textColor),
                it.first,
                it.second,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
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


    data class MentionUser(
        var startAt:Int,
        var endAt:Int,
        var name:String,
    )
}