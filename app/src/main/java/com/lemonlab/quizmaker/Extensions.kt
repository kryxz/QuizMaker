package com.lemonlab.quizmaker

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.media.MediaScannerConnection
import android.os.Handler
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import java.util.*

fun Activity.hideKeypad() =
    with(getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager) {
        hideSoftInputFromWindow(window.decorView.rootView.windowToken, 0)
    }


fun Context.copyText(text: String) {

    val clip = android.content.ClipData.newPlainText("code", text)
    (getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
        .setPrimaryClip(clip)
    Toast.makeText(this, getString(R.string.copied), Toast.LENGTH_SHORT).show()

}

fun Long.timeAsAString(): String {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = this
    return "${calendar.get(Calendar.DATE)}/${calendar.get(Calendar.MONTH) + 1}/${calendar.get(
        Calendar.YEAR
    ).toString().substring(
        2,
        4
    )}"
}


fun String.removedWhitespace(): String {
    var isFirstSpace = false
    val result = StringBuilder()
    for (char in this) {
        if (char != ' ' && char != '\n') {
            isFirstSpace = true
            result.append(char)
        } else if (isFirstSpace) {
            result.append(" ")
            isFirstSpace = false
        }
    }
    return result.toString()
}

fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Context.showYesNoDialog(
    functionToPerform: () -> Unit,
    functionIfCancel: () -> Unit,
    dialogTitle: String,
    dialogMessage: String
) {
    val dialogBuilder = AlertDialog.Builder(this).create()
    val dialogView = with(LayoutInflater.from(this)) {
        inflate(
            R.layout.yes_no_dialog,
            null
        )
    }
    dialogView.findViewById<AppCompatTextView>(R.id.dialogTitle).text = dialogTitle
    dialogView.findViewById<AppCompatTextView>(R.id.dialogMessageText).text = dialogMessage

    dialogView.findViewById<AppCompatButton>(R.id.dialogCancelButton).setOnClickListener {
        functionIfCancel()
        dialogBuilder.dismiss()
    }

    dialogView.findViewById<AppCompatButton>(R.id.dialogConfirmButton).setOnClickListener {
        functionToPerform()
        dialogBuilder.dismiss()
    }

    with(dialogBuilder) {
        setView(dialogView)
        show()
    }

    if (dialogTitle == getString(R.string.changeTheme))
        dialogBuilder.setOnDismissListener { functionIfCancel() }

}


fun Context.askThenSave(bitmap: Bitmap) {

    val uuid = UUID.randomUUID().toString().substring(0, 8)

    Dexter.withActivity(MainActivity.instance!!)

        .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)

        .withListener(object : PermissionListener {
            override fun onPermissionGranted(response: PermissionGrantedResponse) {
                val path = MediaStore.Images.Media.insertImage(
                    contentResolver,
                    bitmap,
                    uuid,
                    uuid + getString(R.string.app_name)
                )
                if (path != null)
                    scanFile(path)

                showToast(getString(R.string.answersSaved))
            }

            override fun onPermissionDenied(response: PermissionDeniedResponse) {}

            override fun onPermissionRationaleShouldBeShown(
                permission: PermissionRequest?,
                token: PermissionToken?
            ) {
            }

        }).check()

}


fun Context.scanFile(filePath: String) {
    val path = arrayOf(filePath)
    MediaScannerConnection.scanFile(this, path, null)
    { _, _ -> }

}

fun View.downloadAsBitMap() {
    (this as RecyclerView).scrollToPosition(0)

    Handler().postDelayed({

        measure(
            View.MeasureSpec.makeMeasureSpec(
                width,
                View.MeasureSpec.EXACTLY
            ),
            View.MeasureSpec.makeMeasureSpec(
                0,
                View.MeasureSpec.UNSPECIFIED
            )
        )

        val bm: Bitmap = Bitmap.createBitmap(
            width,
            measuredHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bm)

        if (background is ColorDrawable)
            canvas.drawColor((background as ColorDrawable).color)

        draw(canvas)

        context.askThenSave(bm)
    }, 300)


}

fun AdView.loadAd() {
    loadAd(AdRequest.Builder().build())
}