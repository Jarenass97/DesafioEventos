package assistant

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.format.DateFormat
import android.widget.EditText
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import model.Evento
import java.util.*

class TimePickerFragment(val editText: EditText,val saved:Boolean=false,val evento: Evento?=null) : DialogFragment(), TimePickerDialog.OnTimeSetListener {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current time as the default values for the picker
        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)

        // Create a new instance of TimePickerDialog and return it
        return TimePickerDialog(activity, this, hour, minute, DateFormat.is24HourFormat(activity))
    }

    @SuppressLint("SetTextI18n")
    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
        editText.setText("$hourOfDay:${String.format("%02d",minute)}")
        if(saved)BDFirebase.changeHourEvent(evento!!, editText.text.toString())
    }
}