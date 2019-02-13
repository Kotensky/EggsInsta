package com.kotensky.eggsinsta

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.more_dialog.view.*
import java.math.BigInteger


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
            val scrollPosition = prefs.getInt(CURRENT_POSITION_KEY, adapter.itemCount / 2)
            main_rv?.scrollToPosition(scrollPosition)
        }
    }

    private fun createMoreDialog() {
        val appPackageName = packageName

        val dialogView = layoutInflater.inflate(R.layout.more_dialog, null)

        dialogView?.statistic_value_txt?.text =
                getString(
                    R.string.statistic_value_tmp,
                    100 - 100 * likedIds.size / adapter.itemCount.toFloat()
                )


        dialogView?.scroll_to_container?.setOnClickListener {
            moreDialog?.dismiss()

            showScrollToPositionDialog() // todo show after adv
        }

        dialogView?.rate_app_txt?.setOnClickListener {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")))
            } catch (anfe: android.content.ActivityNotFoundException) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
                    )
                )
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

        dialogView?.reset_app_txt?.setOnClickListener {
            moreDialog?.dismiss()
            showResetAppDataDialog()
        }


        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
        moreDialog = builder.create()
    }

    private fun showScrollToPositionDialog() {
        val dialogView = layoutInflater.inflate(R.layout.scroll_to_dialog, null)

        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
        builder.setPositiveButton(getString(R.string.yes)) { dialog, which ->
            dialog.dismiss()
            val positionBigInt =
                (dialog as AlertDialog).findViewById<EditText>(R.id.scroll_to_edt)?.text?.toString()
                    ?.toBigIntegerOrNull()
            if (positionBigInt != null) {
                val position = when {
                    positionBigInt < BigInteger.valueOf(1) -> 1
                    positionBigInt >= BigInteger.valueOf((adapter.itemCount - 2).toLong()) -> adapter.itemCount - 3
                    else -> positionBigInt.toInt()
                }
                main_rv.scrollToPosition(position)
            }
        }
        builder.setNegativeButton(getString(R.string.cancel)) { dialog, which ->
            dialog.dismiss()
        }
        builder.create().show()
    }


    private fun showResetAppDataDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(getString(R.string.reset_app_data_dialog_message))
        builder.setPositiveButton(getString(R.string.yes)) { dialog, which ->
            val editor = prefs.edit()
            editor.clear()
            editor.apply()
            dialog.dismiss()
        }
        builder.setNegativeButton(getString(R.string.cancel)) { dialog, which ->
            dialog.dismiss()
        }
        builder.create().show()
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
        createMoreDialog()
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
