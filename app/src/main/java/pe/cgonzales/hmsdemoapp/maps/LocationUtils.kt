package pe.cgonzales.hmsdemoapp.maps

import android.content.Context
import android.location.Geocoder
import android.util.Log
import java.util.*

/**
 * @author by cgonzales.
 */
 class LocationUtils {

    fun getCompleteAddress(context: Context, latitude: Double, longitude: Double): String {
        val strAdd = ""
        val geocoder = Geocoder(context, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses != null) {
                val returnedAddress = addresses[0]
                return returnedAddress.getAddressLine(0)
            } else {
                Log.w("tag", "My Current location address No Address returned!")
            }
        } catch (e: Exception) {
            Log.e("tag", e.localizedMessage)
        }

        return strAdd
    }
}