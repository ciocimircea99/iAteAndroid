import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateTimeUtil {

    private val iso8601Format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC") // Ensure UTC for ISO 8601 compliance
    }

    /**
     * Converts the current system time in milliseconds to ISO 8601 formatted string.
     * Example Output: "2023-11-15"
     */
    fun currentTimeMillisToIso8601(): String {
        return iso8601Format.format(Date(System.currentTimeMillis()))
    }

    /**
     * Converts a timestamp in milliseconds to ISO 8601 formatted string.
     * Example Output: "2023-11-15"
     *
     * @param millis Timestamp in milliseconds
     * @return ISO 8601 formatted string
     */
    fun millisToIso8601(millis: Long): String {
        return iso8601Format.format(Date(millis))
    }

    /**
     * Converts an ISO 8601 formatted string to a timestamp in milliseconds.
     * Example Input: "2023-11-15"
     *
     * @param iso8601Date ISO 8601 formatted string
     * @return Timestamp in milliseconds
     * @throws IllegalArgumentException if the date format is invalid
     */
    fun iso8601ToMillis(iso8601Date: String): Long {
        return iso8601Format.parse(iso8601Date)?.time
            ?: throw IllegalArgumentException("Invalid ISO 8601 date format: $iso8601Date")
    }
}