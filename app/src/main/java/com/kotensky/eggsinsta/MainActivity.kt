package com.kotensky.eggsinsta

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), FeedAdapter.OnFeedClickListener {


    private lateinit var adapter: FeedAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var prefs: SharedPreferences

    private val likedIds = ArrayList<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)

        adapter = FeedAdapter(likedIds, this)
        layoutManager = LinearLayoutManager(this)


        likedIds.addAll(stringToIds(prefs.getString(LIKED_IDS_KEY, "")))

        main_rv.adapter = adapter
        main_rv.layoutManager = layoutManager
        main_rv.scrollToPosition(prefs.getInt(CURRENT_POSITION_KEY, Int.MAX_VALUE / 2))
    }

    override fun onResume() {
        super.onResume()
        val savedLikedIds = stringToIds(prefs.getString(LIKED_IDS_KEY, ""))

        if (likedIds.size != savedLikedIds.size || !likedIds.containsAll(savedLikedIds)) {
            likedIds.clear()
            likedIds.addAll(savedLikedIds)
            adapter.notifyDataSetChanged()
        }
    }

    private fun stringToIds(string: String?): ArrayList<Int> {
        val newList = ArrayList<Int>()
        if (string.isNullOrEmpty())
            return newList
        for (idStr in string.split(VALUE_DIVIDER)) {
            val id = idStr.toIntOrNull() ?: continue
            newList.add(id)
        }
        return newList
    }

    override fun onPause() {
        val editor = prefs.edit()
        editor.putString(LIKED_IDS_KEY, likesToString())
        editor.putInt(CURRENT_POSITION_KEY, layoutManager.findFirstVisibleItemPosition())
        editor.apply()

        super.onPause()
    }

    private fun likesToString(): String {
        val sb = StringBuilder()
        likedIds.forEach {
            if (sb.isNotEmpty()) {
                sb.append(VALUE_DIVIDER)
            }
            sb.append(it)
        }
        return sb.toString()
    }

    override fun onImageDoubleClick(position: Int) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onLikeClick(position: Int) {
        if (likedIds.contains(position)) {
            likedIds.remove(position)
        } else {
            likedIds.add(position)
        }
        adapter.notifyDataSetChanged()
    }

    companion object {
        const val LIKED_IDS_KEY = "liked_ids_key"
        const val CURRENT_POSITION_KEY = "current_position_key"

        const val VALUE_DIVIDER = ","
    }
}
