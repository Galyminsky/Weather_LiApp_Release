package com.protonmail.jobforandroid.weather

import android.app.AlertDialog
import android.content.Context
import android.widget.EditText

object DialogManager {
    fun locationSettingsDialog(context: Context, listener: Listener) {
        val builder = AlertDialog.Builder(context)
        val dialog = builder.create()
        dialog.setTitle(context.getString(R.string.dialog_enable_location_title))
        dialog.setMessage(context.getString(R.string.dialog_location_disabled))
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, context.getString(R.string.action_ok)) { _, _ ->
            listener.onClick(null)
            dialog.dismiss()
        }
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, context.getString(R.string.action_cancel)) { _, _ ->
            dialog.dismiss()
        }
        dialog.show()
    }

    fun searchByNameDialog(context: Context, listener: Listener) {
        val builder = AlertDialog.Builder(context)
        val edName = EditText(context)
        edName.hint = context.getString(R.string.search_city_hint)
        builder.setView(edName)
        val dialog = builder.create()
        dialog.setTitle(context.getString(R.string.dialog_city_name))
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, context.getString(R.string.action_ok)) { _, _ ->
            listener.onClick(edName.text.toString())
            dialog.dismiss()
        }
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, context.getString(R.string.action_cancel)) { _, _ ->
            dialog.dismiss()
        }
        dialog.show()
    }

    interface Listener {
        fun onClick(name: String?)
    }
}
