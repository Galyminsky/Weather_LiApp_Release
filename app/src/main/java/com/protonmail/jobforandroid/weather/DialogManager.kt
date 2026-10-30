package com.protonmail.jobforandroid.weather

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import com.protonmail.jobforandroid.weather.databinding.DialogSearchCityBinding

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
        val binding = DialogSearchCityBinding.inflate(LayoutInflater.from(context))
        val dialog = AlertDialog.Builder(context)
            .setView(binding.root)
            .create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        binding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        binding.btnOk.setOnClickListener {
            val query = binding.edCityName.text.toString().trim()
            if (query.isNotEmpty()) {
                listener.onClick(query)
            }
            dialog.dismiss()
        }
        binding.edCityName.requestFocus()
        dialog.show()
    }

    interface Listener {
        fun onClick(name: String?)
    }
}
