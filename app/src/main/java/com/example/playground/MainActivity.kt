package com.example.playground

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.playground.databinding.ActivityMainBinding
import com.example.playground.databinding.ItemUserBinding


class MainActivity : AppCompatActivity() {
    companion object {
        const val TAG = "MainActivity"
        const val TAG_TEST = "MainActivityTest"
        const val beforeTextChanged = "beforeTextChanged"
        const val onTextChanged = "onTextChanged"
        const val afterTextChanged = "afterTextChanged"
    }

    private lateinit var binding: ActivityMainBinding

    private var messages = mutableListOf<Message>()
    private var users = mutableListOf<User>()

    private lateinit var rvUsersAdapter: SimpleListAdapter<ItemUserBinding, User>

    private val viewRegex = "@\\{([A-Z][a-z])?\\w+(\\s([A-Z][a-z])?(\\w+)?)?\\}"

    private var displayString = ""
    private var prevDisplayString = ""
    private var messageString = ""
    private var cursorPosition = 0
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

        binding.etMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//                Log.i(TAG,"beforeTextChanged : ${p0.toString()}")
                Log.i(beforeTextChanged, "beforeTextChanged : CharSequence > $s")
                Log.i(beforeTextChanged, "beforeTextChanged : start > $start")
                Log.i(beforeTextChanged, "beforeTextChanged : count > $count")
                Log.i(beforeTextChanged, "beforeTextChanged : after > $after")
                prevDisplayString = s.toString()


            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Log.i(onTextChanged, "onTextChanged : CharSequence > $s")
                Log.i(onTextChanged, "onTextChanged : start > $start")
                Log.i(onTextChanged, "onTextChanged : before > $before")
                Log.i(onTextChanged, "onTextChanged : count > $count")
                updateMessageString(prevDisplayString,s.toString(),start,count,before)

                /*Log.i(TAG,"onTextChanged : ${s.toString()}")
                Log.i(TAG,"onTextChanged : Mentioned Users > ${findMentionedUsers(s.toString())}")

                cursorPosition = binding.etMessage.selectionStart
                updateMessageString(s.toString(),cursorPosition)
                messageString = s.toString()
                if(!s.isNullOrEmpty()){
                    filterMatchedUsers(findMentionedUsers(messageString.substring(0,cursorPosition)))
                }*/

            }

            override fun afterTextChanged(p0: Editable?) {
                Log.i(afterTextChanged, "afterTextChanged : ${p0.toString()}")
            }
        })
    }

    private fun updateMessageString(prevText:String,currentText: String, i: Int,addedChars:Int,modifiedChars:Int) {
        Log.i(TAG_TEST, "text -> $prevText")
        Log.i(TAG_TEST, "cursorPosition -> $i")
        val current = currentText.toMutableList()
        val prev = messageString.toMutableList()

        val spans = fetchViewMentionedUsers(messageString)
        val coloredSpans = mutableListOf<Pair<Int, Int>>()
        spans.forEach {
            val start = messageString.indexOf(it)
            val end = start + it.length

            coloredSpans.add(Pair(start, end))
        }

        // check if editing done in mentions
        var editedSpans = mutableListOf<Pair<Int, Int>>()
        coloredSpans.forEach {
            if (i in it.first..it.second) {
                editedSpans.add(Pair(it.first, it.second))
            }

        }

        editedSpans.forEach {
            prev.removeAt(it.first + 1)
            prev.removeAt(it.second)
        }





        /*if(prev.size<i){
            prev.add(current[i])
        }else{
            prev.add(i,current[i])
        }*/

        Log.i(TAG_TEST, "prev message string -> $prev")
        Log.i(TAG_TEST, "last messageString -> $messageString")

        messageString = ""

        Log.i(TAG_TEST, "updated messageString -> $messageString")
    }

    private fun filterMatchedUsers(matches: List<String>) {
        val filteredData = mutableListOf<User>()
        val fMatches = matches.map {
            it.substring(1)
        }

        users.forEach { user ->
            if (fMatches.isNotEmpty() && user.name.startsWith(
                    fMatches.last(),
                    ignoreCase = true
                )
            ) filteredData.add(user)
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

    private fun setEditTextMentions(user: User) {
        val selectedUser = user.name
        val currentText = binding.etMessage.text.toString()
        val mentionText = "@{$selectedUser} "

        val mentionStartIndex = currentText.substring(0, cursorPosition).lastIndexOf("@")
        val newText =
            currentText.replaceRange(
                mentionStartIndex,
                binding.etMessage.selectionStart,
                mentionText
            )

        messageString = newText

        val spans = fetchViewMentionedUsers(newText)
        val coloredSpans = mutableListOf<Pair<Int, Int>>()
        spans.forEach {
            val start = newText.indexOf(it)
            val end = start + it.length

            coloredSpans.add(Pair(start, end))
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
        /*val left : Char = '{'
        val right : Char = '}'
        val fStr = mySpan.toString().map {
            if(it != left && it !=right){
                return@map it
            }else{
                return@map ""
            }
        }.joinToString("")*/
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