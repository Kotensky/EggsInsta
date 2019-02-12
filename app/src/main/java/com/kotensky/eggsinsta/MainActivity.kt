package com.kotensky.eggsinsta

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.more_dialog.*
import android.content.Intent
import android.net.Uri


class MainActivity : AppCompatActivity(), FeedAdapter.OnFeedClickListener {


    private lateinit var adapter: FeedAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var prefs: SharedPreferences

    private val likedIds = ArrayList<Int>()
    private var moreDialog: AlertDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)

        adapter = FeedAdapter(likedIds, this)
        layoutManager = LinearLayoutManager(this)

        likedIds.addAll(stringToIds(prefs.getString(LIKED_IDS_KEY, "")))

        main_rv.adapter = adapter
        main_rv.layoutManager = layoutManager
        main_rv.post {
            main_rv?.scrollToPosition(prefs.getInt(CURRENT_POSITION_KEY, adapter.itemCount / 2))
        }

        setupMoreDialog()
    }

    private fun setupMoreDialog(){
        val appPackageName = packageName

        val dialogView = layoutInflater.inflate(R.layout.more_dialog, null)
        dialogView?.rate_app_txt?.setOnClickListener {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")))
            } catch (anfe: android.content.ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")))
            }
            moreDialog?.dismiss()
        }

        dialogView?.share_txt?.setOnClickListener {
            try {
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.type = "text/plain"
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
                var shareMessage = "\nLet me recommend you this application\n\n"
                shareMessage += "https://play.google.com/store/apps/details?id=$appPackageName\n\n"
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
                startActivity(Intent.createChooser(shareIntent, "Share app via..."))
            } catch (e: Exception) {
                e.printStackTrace()
            }

            moreDialog?.dismiss()
        }

        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
        moreDialog = builder.create()
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

    override fun onResume() {
        super.onResume()
        val savedLikedIds = stringToIds(prefs.getString(LIKED_IDS_KEY, ""))

        if (likedIds.size != savedLikedIds.size || !likedIds.containsAll(savedLikedIds)) {
            likedIds.clear()
            likedIds.addAll(savedLikedIds)
            adapter.notifyDataSetChanged()
        }
    }

    override fun onPause() {
        val editor = prefs.edit()
        editor.putString(LIKED_IDS_KEY, likesToString())
        editor.putInt(CURRENT_POSITION_KEY, layoutManager.findFirstVisibleItemPosition())

        editor.apply()

        super.onPause()
    }

    override fun onImageDoubleClick(position: Int) {
        if (!likedIds.contains(position)) {
            likedIds.add(position)
        }
        adapter.doubleClickItemPosition = position
        adapter.notifyDataSetChanged()

    }

    override fun onMoreClick(position: Int) {
        moreDialog?.show()
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
