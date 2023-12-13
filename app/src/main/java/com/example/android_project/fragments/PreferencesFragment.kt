import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.DialogFragment
import com.example.android_project.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PreferencesDialogFragment : DialogFragment() {

    private lateinit var bestHandSpinner: Spinner
    private lateinit var courtPositionSpinner: Spinner
    private lateinit var matchTypeSpinner: Spinner

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    public interface PreferencesChangeListener {
        fun onPreferencesChanged()
    }

    private var preferencesChangeListener: PreferencesChangeListener? = null

    fun setPreferencesChangeListener(listener: PreferencesChangeListener) {
        this.preferencesChangeListener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val builder = AlertDialog.Builder(requireActivity())
        val inflater = LayoutInflater.from(requireActivity())

        // Inflate the layout for the dialog
        val view = inflater.inflate(R.layout.fragment_preferences, null)
        builder.setView(view)

        bestHandSpinner = view.findViewById(R.id.spinnerBestHand)
        courtPositionSpinner = view.findViewById(R.id.spinnerCourtPosition)
        matchTypeSpinner = view.findViewById(R.id.spinnerMatchType)

        // Set up ArrayAdapter for spinners
        val bestHandAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.best_hand_options,
            android.R.layout.simple_spinner_item
        )
        bestHandAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        bestHandSpinner.adapter = bestHandAdapter

        val courtPositionAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.court_position_options,
            android.R.layout.simple_spinner_item
        )
        courtPositionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        courtPositionSpinner.adapter = courtPositionAdapter

        val matchTypeAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.match_type_options,
            android.R.layout.simple_spinner_item
        )
        matchTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        matchTypeSpinner.adapter = matchTypeAdapter

        builder.setPositiveButton("Save") { _, _ ->
            savePreferences()

        }

        builder.setNegativeButton("Cancel") { _, _ ->
            dismiss()
        }

        return builder.create()
    }

    private fun savePreferences() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userPreferences = hashMapOf(
                "bestHand" to bestHandSpinner.selectedItem.toString(),
                "courtPosition" to courtPositionSpinner.selectedItem.toString(),
                "matchType" to matchTypeSpinner.selectedItem.toString()
            )

            db.collection("userInformation")
                .document(userId)
                .update("preferences", userPreferences)
                .addOnSuccessListener {
                    Log.d("PreferencesDialog", "Preferences successfully updated")
                    preferencesChangeListener?.onPreferencesChanged()
                    dismiss()
                }
                .addOnFailureListener { e ->
                    Log.w("PreferencesDialog", "Error updating preferences", e)
                    dismiss()
                }
        }
    }

}