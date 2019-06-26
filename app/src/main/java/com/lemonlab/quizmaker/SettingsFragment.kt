package com.lemonlab.quizmaker


import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
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
        with(settingsRecyclerView) {
            layoutManager = LinearLayoutManager(context!!)
            val listOfSettings = listOf(

                OptionsItem(
                    R.drawable.ic_storage,
                    getString(R.string.clearCache), Option.CACHE
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
            adapter = TextViewAdapter(context!!, listOfSettings, null)
        }
        super.onViewCreated(view, savedInstanceState)
    }

}


class TextViewAdapter(
    private val context: Context,
    private val listOfSettings: List<OptionsItem>?,
    private val listOfFaqs: List<String>?
) :
    RecyclerView.Adapter<SettingsRV>() {

    override fun onBindViewHolder(holder: SettingsRV, position: Int) {
        if (listOfFaqs == null)
            setUp(holder.settingsItemText, position)
        else
            setUpFaq(holder.settingsItemText, position)
    }

    private fun setUpFaq(settingsItemText: AppCompatTextView, position: Int) {
        with(settingsItemText) {
            text = listOfFaqs!![position]
            val listOfAnswers = resources.getStringArray(R.array.faqsAnswers)
            setOnClickListener {
                animate().scaleX(.3f).scaleY(.3f).setDuration(50)
                    .withEndAction {
                        animate().scaleX(1f).scaleY(1f).duration = 50
                        showInfoDialog(listOfFaqs[position], listOfAnswers[position])
                    }
            }
        }
    }

    private fun showInfoDialog(dialogTitle: String, dialogMessage: String) {
        val dialogBuilder = AlertDialog.Builder(context).create()
        val dialogView = with(LayoutInflater.from(context)) {
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
        when (listOfSettings!![position].type) {
            Option.CACHE -> deleteCache(textView, position)
            Option.APPS -> moreApps(textView, position)
            Option.LOGOUT -> logout(textView, position)
            Option.ABOUT -> aboutUs(textView, position)
            Option.PRIVACY -> privacyPolicy(textView, position)
            Option.FAQ -> fAQ(textView, position)
        }
    }

    private fun fAQ(textView: AppCompatTextView, position: Int) {
        with(textView) {
            setCompoundDrawablesWithIntrinsicBounds(listOfSettings!![position].icon, 0, 0, 0)
            text = listOfSettings[position].text
            setOnClickListener {

                val dialogBuilder = AlertDialog.Builder(context).create()
                val dialogView = with(LayoutInflater.from(context)) {
                    inflate(
                        R.layout.faq_dialog,
                        null
                    )
                }
                with(dialogView.findViewById<RecyclerView>(R.id.faqRecyclerView)) {
                    layoutManager = LinearLayoutManager(context)
                    adapter = TextViewAdapter(context, null, resources.getStringArray(R.array.faqs).toList())
                }

                dialogView.findViewById<AppCompatButton>(R.id.faqOKButton).setOnClickListener {
                    dialogBuilder.dismiss()
                }

                dialogBuilder.setView(dialogView)

                animate().scaleX(0f).scaleY(0f).setDuration(50)
                    .withEndAction {
                        animate().scaleX(1f).scaleY(1f).duration = 50
                        dialogBuilder.show()
                    }
            }
        }
    }

    private fun logout(textView: AppCompatTextView, position: Int) {
        with(textView) {
            setCompoundDrawablesWithIntrinsicBounds(listOfSettings!![position].icon, 0, 0, 0)
            text = listOfSettings[position].text
            setOnClickListener {
                animate().scaleX(0f).scaleY(0f).setDuration(100)
                    .withEndAction {
                        animate().scaleX(1f).scaleY(1f).duration = 100
                        FirebaseAuth.getInstance().signOut()
                        Navigation.findNavController(it).navigate(R.id.loginFragment)
                    }
            }
        }
    }

    private fun deleteCache(textView: AppCompatTextView, position: Int) {
        with(textView) {
            setCompoundDrawablesWithIntrinsicBounds(listOfSettings!![position].icon, 0, 0, 0)
            text = listOfSettings[position].text
            setOnClickListener {
                animate().scaleX(0f).scaleY(0f).setDuration(100)
                    .withEndAction { animate().scaleX(1f).scaleY(1f).duration = 100 }
                context.cacheDir.deleteRecursively()
                showToast(context, context.getString(R.string.cacheDeleted))
            }
        }

    }

    private fun moreApps(textView: AppCompatTextView, position: Int) {
        with(textView) {
            setCompoundDrawablesWithIntrinsicBounds(listOfSettings!![position].icon, 0, 0, 0)
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
        val dialog = AlertDialog.Builder(context, 4)
        with(dialog) {
            setPositiveButton(context.getString(R.string.ok)) { firstDialog, _ ->
                firstDialog.dismiss()
            }
            setTitle(context.getString(R.string.aboutLemonLabTitle))
            setMessage(context.getString(R.string.aboutLemonLabAr))
        }
        with(textView) {
            setCompoundDrawablesWithIntrinsicBounds(listOfSettings!![position].icon, 0, 0, 0)
            text = listOfSettings[position].text
            setOnClickListener {
                animate().scaleX(0f).scaleY(0f).setDuration(50)
                    .withEndAction {
                        animate().scaleX(1f).scaleY(1f).duration = 50
                        dialog.show()
                    }
            }
        }

    }

    private fun privacyPolicy(textView: AppCompatTextView, position: Int) {
        val privacyPolicyText = context.getString(R.string.privacyPolicyText)
        val privacyPolicyTextAr = context.getString(R.string.privacyPolicyTextAr)

        val dialog = AlertDialog.Builder(context, 4)

        with(dialog) {
            setPositiveButton(context.getString(R.string.okEn)) { firstDialog, _ ->
                firstDialog.dismiss()
            }

            dialog.setNegativeButton(context.getString(R.string.changeLanguage)) { firstDialog, _ ->
                val anotherDialog =
                    AlertDialog.Builder(context, 4)
                with(anotherDialog) {
                    setPositiveButton(context.getString(R.string.ok)) { secondDialog, _ ->
                        secondDialog.dismiss()
                    }
                    setTitle(context.getString(R.string.privacyPolicyAr))
                    setMessage(privacyPolicyTextAr)
                    show()
                    firstDialog.dismiss()
                }

            }
            setTitle(context.getString(R.string.privacyPolicyAr))
            setMessage(privacyPolicyText)
        }
        with(textView) {
            setCompoundDrawablesWithIntrinsicBounds(listOfSettings!![position].icon, 0, 0, 0)
            text = listOfSettings[position].text
            setOnClickListener {
                animate().scaleX(0f).scaleY(0f).setDuration(50)
                    .withEndAction {
                        animate().scaleX(1f).scaleY(1f).duration = 50
                        dialog.show()
                    }
            }
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingsRV {
        return SettingsRV(
            LayoutInflater.from(context).inflate(
                R.layout.text_item,
                parent,
                false
            )
        )
    }

    override fun getItemCount() = listOfFaqs?.size ?: listOfSettings!!.size

}


class SettingsRV(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val settingsItemText = itemView.findViewById(R.id.ItemTextView) as AppCompatTextView

}