package com.lemonlab.quizmaker


import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.app.TaskStackBuilder
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import kotlinx.android.synthetic.main.fragment_settings.*


class SettingsFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        with(settingsRecyclerView) {
            layoutManager = LinearLayoutManager(context!!)
            val listOfSettings = listOf(

                OptionsItem(
                    R.drawable.ic_storage,
                    getString(R.string.clearCache), Option.CACHE
                ),

                OptionsItem(
                    R.drawable.ic_color_lens,
                    getString(R.string.changeTheme), Option.THEME
                ),

                OptionsItem(
                    R.drawable.ic_question_answer,
                    getString(R.string.FAQTitle), Option.FAQ
                ),

                OptionsItem(
                    R.drawable.ic_exit,
                    getString(R.string.logout), Option.LOGOUT
                ),

                OptionsItem(
                    R.drawable.ic_feedback,
                    getString(R.string.suggestIdea), Option.FEEDBACK
                ),

                OptionsItem(
                    R.drawable.ic_info,
                    getString(R.string.aboutLemonLabTitle), Option.ABOUT
                ),

                OptionsItem(
                    R.drawable.ic_apps,
                    getString(R.string.moreApps), Option.APPS
                ),

                OptionsItem(
                    R.drawable.ic_arrow_right,
                    getString(R.string.privacyPolicyAr), Option.PRIVACY
                )

            )
            adapter = TextViewAdapter(
                activity!!, listOfSettings, (activity as MainActivity).vm
            )
        }

    }
}


class TextViewAdapter(
    private val activity: Activity,
    private val listOfSettings: List<OptionsItem>,
    private val viewModel: QuizzesVM
) :
    RecyclerView.Adapter<SettingsRV>() {

    override fun onBindViewHolder(holder: SettingsRV, position: Int) {

            setUp(holder.settingsItemText, position)

    }

    private fun showInfoDialog(dialogTitle: String, dialogMessage: String) {
        val dialogBuilder = AlertDialog.Builder(activity).create()
        val dialogView = with(LayoutInflater.from(activity)) {
            inflate(
                R.layout.info_dialog,
                null
            )
        }
        dialogView.findViewById<AppCompatTextView>(R.id.infoDialogTitle).text = dialogTitle
        dialogView.findViewById<AppCompatTextView>(R.id.infoDialogMessageText).text = dialogMessage

        dialogView.findViewById<AppCompatButton>(R.id.infoDialogConfirmButton).setOnClickListener {
            dialogBuilder.dismiss()
        }

        with(dialogBuilder) {
            setView(dialogView)
            show()
        }

    }

    private fun setUp(textView: AppCompatTextView, position: Int) {
        when (listOfSettings[position].type) {
            Option.CACHE -> deleteCache(textView, position)
            Option.THEME -> changeTheme(textView, position)
            Option.APPS -> moreApps(textView, position)
            Option.LOGOUT -> logout(textView, position)
            Option.ABOUT -> aboutUs(textView, position)
            Option.PRIVACY -> privacyPolicy(textView, position)
            Option.FAQ -> fAQ(textView, position)
            Option.FEEDBACK -> feedbackDialog(textView, position)
        }
    }

    private fun feedbackDialog(textView: AppCompatTextView, position: Int) {
        val dialogBuilder = AlertDialog.Builder(activity).create()
        val dialogView = with(LayoutInflater.from(activity)) {
            inflate(
                R.layout.feedback_dialog,
                null
            )
        }
        val feedbackText = dialogView.findViewById<TextInputEditText>(R.id.feedbackText)
        dialogView.findViewById<AppCompatButton>(R.id.sendFeedbackButton).setOnClickListener {
            if (feedbackText.text!!.isEmpty())
                return@setOnClickListener
            viewModel.sendFeedback(feedbackText.text.toString().removedWhitespace())
            activity.applicationContext.showToast(activity.getString(R.string.thanks))
            dialogBuilder.dismiss()
        }

        dialogView.findViewById<AppCompatButton>(R.id.cancelFeedbackButton).setOnClickListener {
            dialogBuilder.dismiss()
        }
        dialogBuilder.setView(dialogView)
        with(textView) {
            setCompoundDrawablesWithIntrinsicBounds(listOfSettings[position].icon, 0, 0, 0)
            text = listOfSettings[position].text
            setOnClickListener {
                dialogBuilder.show()
            }
        }
    }

    private fun changeTheme(textView: AppCompatTextView, position: Int) {
        val sharedPrefs = activity.getSharedPreferences("userPrefs", 0)
        val isLightModeOn = sharedPrefs.getBoolean("lightMode", false)

        fun saveToSharedPrefs(key: String, value: Boolean) {
            with(sharedPrefs.edit()) {
                remove(key)
                putBoolean(key, value)
                apply()
            }
        }

        fun applyChanges() {
            if (isLightModeOn)
                saveToSharedPrefs("lightMode", false)
            else
                saveToSharedPrefs("lightMode", true)
            TaskStackBuilder.create(activity)
                .addNextIntent(Intent(activity, MainActivity::class.java))
                .addNextIntent(activity.intent)
                .startActivities()
        }

        fun invertMode(context: Context, isTheLightModeOn: Boolean) {
            if (isTheLightModeOn)
                context.setTheme(R.style.AppTheme)
            else
                context.setTheme(R.style.LightTheme)
        }
        with(textView) {
            setCompoundDrawablesWithIntrinsicBounds(listOfSettings[position].icon, 0, 0, 0)
            text = listOfSettings[position].text
            setOnClickListener {
                invertMode(it.context, isLightModeOn)
                context.showYesNoDialog(
                    {
                        applyChanges()
                    },
                    {
                        invertMode(it.context, !isLightModeOn)
                    },
                    context.getString(R.string.changeTheme),
                    context.getString(R.string.confirmChangeTheme)
                )
            }
        }

    }

    private fun fAQ(textView: AppCompatTextView, position: Int) {
        with(textView) {
            setCompoundDrawablesWithIntrinsicBounds(listOfSettings[position].icon, 0, 0, 0)
            text = listOfSettings[position].text
            setOnClickListener {
                it.findNavController().navigate(R.id.faqFragment)
            }
        }
    }

    private fun logout(textView: AppCompatTextView, position: Int) {
        with(textView) {
            setCompoundDrawablesWithIntrinsicBounds(listOfSettings[position].icon, 0, 0, 0)
            text = listOfSettings[position].text
            setOnClickListener {
                context.showYesNoDialog(
                    {
                        viewModel.unsubscribeNotifications()
                        viewModel.signOut()
                        Navigation.findNavController(it).navigate(R.id.loginFragment)
                    },
                    {},
                    context.getString(R.string.logout),
                    context.getString(R.string.confirmLogout)
                )


            }
        }
    }

    private fun deleteCache(textView: AppCompatTextView, position: Int) {
        with(textView) {
            setCompoundDrawablesWithIntrinsicBounds(listOfSettings[position].icon, 0, 0, 0)
            text = listOfSettings[position].text
            setOnClickListener {
                animate().scaleX(0f).scaleY(0f).setDuration(100)
                    .withEndAction { animate().scaleX(1f).scaleY(1f).duration = 100 }
                context.cacheDir.delete()
                context.showToast(context.getString(R.string.cacheDeleted))
            }
        }

    }

    private fun moreApps(textView: AppCompatTextView, position: Int) {
        with(textView) {
            setCompoundDrawablesWithIntrinsicBounds(listOfSettings[position].icon, 0, 0, 0)
            text = listOfSettings[position].text
            setOnClickListener {
                context.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/developer?id=Lemon+Lab")
                    )
                )
            }
        }

    }

    private fun aboutUs(textView: AppCompatTextView, position: Int) {

        with(textView) {
            setCompoundDrawablesWithIntrinsicBounds(listOfSettings[position].icon, 0, 0, 0)
            text = listOfSettings[position].text
            setOnClickListener {
                showInfoDialog(
                    context.getString(R.string.aboutLemonLabTitle),
                    context.getString(R.string.aboutLemonLabAr)
                )

            }
        }

    }

    private fun privacyPolicy(textView: AppCompatTextView, position: Int) {
        val privacyPolicyText = activity.getString(R.string.privacyPolicyText)
        val privacyPolicyTextAr = activity.getString(R.string.privacyPolicyTextAr)
        fun changeColorToWhite(string: String): SpannableString {
            val newString = SpannableString(string)
            newString.setSpan(ForegroundColorSpan(Color.WHITE), 0, newString.length, 0)
            return newString
        }

        val dialog = AlertDialog.Builder(activity, 4)

        with(dialog) {
            setPositiveButton(changeColorToWhite(context.getString(R.string.okEn))) { firstDialog, _ ->
                firstDialog.dismiss()
            }

            dialog.setNegativeButton(changeColorToWhite(context.getString(R.string.changeLanguage))) { firstDialog, _ ->
                val anotherDialog =
                    AlertDialog.Builder(activity, 4)
                with(anotherDialog) {
                    setPositiveButton(changeColorToWhite(context.getString(R.string.ok))) { secondDialog, _ ->
                        secondDialog.dismiss()

                    }
                    setTitle(changeColorToWhite(context.getString(R.string.privacyPolicyAr)))
                    setMessage(changeColorToWhite(privacyPolicyTextAr))
                    show()
                    firstDialog.dismiss()
                }

            }
            setTitle(changeColorToWhite(context.getString(R.string.privacyPolicyEn)))
            setMessage(changeColorToWhite(privacyPolicyText))
        }
        with(textView) {
            setCompoundDrawablesWithIntrinsicBounds(listOfSettings[position].icon, 0, 0, 0)
            text = listOfSettings[position].text
            setOnClickListener {
                dialog.show()

            }
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingsRV {
        return SettingsRV(
            LayoutInflater.from(activity).inflate(
                R.layout.text_item,
                parent,
                false
            )
        )
    }

    override fun getItemCount() = listOfSettings.size

}


class SettingsRV(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val settingsItemText = itemView.findViewById(R.id.ItemTextView) as AppCompatTextView

}