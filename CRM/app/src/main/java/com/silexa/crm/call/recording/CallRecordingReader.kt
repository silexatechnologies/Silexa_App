package com.silexa.crm.call.recording

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log

data class CallRecording(
    val uri: Uri,
    val displayName: String,
    val dateAdded: Long,
    val duration: Long?,
    val size: Long
)

class CallRecordingReader(
    private val context: Context
) {

    companion object {
        private const val TAG = "CRM_CALL_RECORDING"

        /*
         * For diagnosis we look at audio files added
         * within the last 10 minutes.
         */
        private const val DIAGNOSTIC_WINDOW_MS = 10 * 60 * 1000L
    }

    fun findRecording(
        callDateMillis: Long,
        callDurationSeconds: Long
    ): CallRecording? {

        val resolver = context.contentResolver

        Log.d(TAG, "================================")
        Log.d(TAG, "SEARCHING FOR CALL RECORDING")
        Log.d(TAG, "Call date: $callDateMillis")
        Log.d(TAG, "Call duration: $callDurationSeconds seconds")
        Log.d(TAG, "================================")

        /*
         * ----------------------------------------------------
         * STEP 1
         * ----------------------------------------------------
         *
         * First list what MediaStore can actually see.
         *
         * We deliberately do NOT filter by the call timestamp
         * here. This is only for diagnosis.
         */
        val recordings = queryRecentAudioFiles(resolver)

        if (recordings.isEmpty()) {

            Log.d(
                TAG,
                "================================"
            )

            Log.d(
                TAG,
                "NO AUDIO FILES FOUND THROUGH MEDIASTORE"
            )

            Log.d(
                TAG,
                "================================"
            )

            return null
        }

        Log.d(
            TAG,
            "================================"
        )

        Log.d(
            TAG,
            "MEDIASTORE FOUND ${recordings.size} AUDIO FILE(S)"
        )

        Log.d(
            TAG,
            "================================"
        )

        /*
         * ----------------------------------------------------
         * STEP 2
         * ----------------------------------------------------
         *
         * Now try matching the recording with the call.
         */
        val durationMatch =
            recordings
                .filter { it.duration != null }
                .minByOrNull { recording ->

                    kotlin.math.abs(
                        (recording.duration!! / 1000L) -
                                callDurationSeconds
                    )
                }

        if (durationMatch != null) {

            val recordingDurationSeconds =
                durationMatch.duration!! / 1000L

            val difference =
                kotlin.math.abs(
                    recordingDurationSeconds -
                            callDurationSeconds
                )

            Log.d(
                TAG,
                "Closest recording:"
            )

            Log.d(
                TAG,
                "Name: ${durationMatch.displayName}"
            )

            Log.d(
                TAG,
                "URI: ${durationMatch.uri}"
            )

            Log.d(
                TAG,
                "Recording duration: ${recordingDurationSeconds}s"
            )

            Log.d(
                TAG,
                "Call duration: ${callDurationSeconds}s"
            )

            Log.d(
                TAG,
                "Duration difference: ${difference}s"
            )

            /*
             * Allow up to 10 seconds difference.
             */
            if (difference <= 10) {

                Log.d(
                    TAG,
                    "================================"
                )

                Log.d(
                    TAG,
                    "RECORDING MATCH FOUND"
                )

                Log.d(
                    TAG,
                    "Name: ${durationMatch.displayName}"
                )

                Log.d(
                    TAG,
                    "URI: ${durationMatch.uri}"
                )

                Log.d(
                    TAG,
                    "Duration: ${recordingDurationSeconds}s"
                )

                Log.d(
                    TAG,
                    "Size: ${durationMatch.size} bytes"
                )

                Log.d(
                    TAG,
                    "================================"
                )

                return durationMatch
            }
        }

        Log.d(
            TAG,
            "================================"
        )

        Log.d(
            TAG,
            "NO MATCHING CALL RECORDING FOUND"
        )

        Log.d(
            TAG,
            "================================"
        )

        return null
    }

    private fun queryRecentAudioFiles(
        resolver: ContentResolver
    ): List<CallRecording> {

        val recordings =
            mutableListOf<CallRecording>()

        /*
         * Current time.
         */
        val currentTime =
            System.currentTimeMillis()

        /*
         * Look back 10 minutes.
         */
        val minimumTime =
            currentTime - DIAGNOSTIC_WINDOW_MS

        /*
         * MediaStore DATE_ADDED is in seconds.
         */
        val minimumSeconds =
            minimumTime / 1000L

        val projection =
            arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DATE_ADDED,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.MIME_TYPE,
                MediaStore.Audio.Media.RELATIVE_PATH
            )

        /*
         * Only use the minimum time.
         *
         * There is no upper bound because we only want
         * everything recently added up to now.
         */
        val selection =
            "${MediaStore.Audio.Media.DATE_ADDED} >= ?"

        val selectionArgs =
            arrayOf(
                minimumSeconds.toString()
            )

        val sortOrder =
            "${MediaStore.Audio.Media.DATE_ADDED} DESC"

        Log.d(
            TAG,
            "Querying MediaStore..."
        )

        Log.d(
            TAG,
            "Looking back: 10 minutes"
        )

        try {

            resolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->

                Log.d(
                    TAG,
                    "MediaStore query successful"
                )

                Log.d(
                    TAG,
                    "Column count: ${cursor.columnCount}"
                )

                val idColumn =
                    cursor.getColumnIndexOrThrow(
                        MediaStore.Audio.Media._ID
                    )

                val nameColumn =
                    cursor.getColumnIndexOrThrow(
                        MediaStore.Audio.Media.DISPLAY_NAME
                    )

                val dateColumn =
                    cursor.getColumnIndexOrThrow(
                        MediaStore.Audio.Media.DATE_ADDED
                    )

                val durationColumn =
                    cursor.getColumnIndex(
                        MediaStore.Audio.Media.DURATION
                    )

                val sizeColumn =
                    cursor.getColumnIndex(
                        MediaStore.Audio.Media.SIZE
                    )

                val mimeColumn =
                    cursor.getColumnIndex(
                        MediaStore.Audio.Media.MIME_TYPE
                    )

                val pathColumn =
                    cursor.getColumnIndex(
                        MediaStore.Audio.Media.RELATIVE_PATH
                    )

                var count = 0

                while (cursor.moveToNext()) {

                    count++

                    val id =
                        cursor.getLong(idColumn)

                    val name =
                        cursor.getString(nameColumn)

                    val dateAdded =
                        cursor.getLong(dateColumn) * 1000L

                    val duration =
                        if (durationColumn >= 0) {
                            cursor.getLong(durationColumn)
                        } else {
                            null
                        }

                    val size =
                        if (sizeColumn >= 0) {
                            cursor.getLong(sizeColumn)
                        } else {
                            0L
                        }

                    val mimeType =
                        if (mimeColumn >= 0) {
                            cursor.getString(mimeColumn)
                        } else {
                            null
                        }

                    val relativePath =
                        if (pathColumn >= 0) {
                            cursor.getString(pathColumn)
                        } else {
                            null
                        }

                    val uri =
                        Uri.withAppendedPath(
                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            id.toString()
                        )

                    val recording =
                        CallRecording(
                            uri = uri,
                            displayName = name,
                            dateAdded = dateAdded,
                            duration = duration,
                            size = size
                        )

                    recordings.add(recording)

                    Log.d(
                        TAG,
                        "--------------------------------"
                    )

                    Log.d(
                        TAG,
                        "AUDIO FILE #$count"
                    )

                    Log.d(
                        TAG,
                        "Name: $name"
                    )

                    Log.d(
                        TAG,
                        "URI: $uri"
                    )

                    Log.d(
                        TAG,
                        "Date Added: $dateAdded"
                    )

                    Log.d(
                        TAG,
                        "Duration: ${duration ?: "unknown"} ms"
                    )

                    Log.d(
                        TAG,
                        "Size: $size bytes"
                    )

                    Log.d(
                        TAG,
                        "MIME: ${mimeType ?: "unknown"}"
                    )

                    Log.d(
                        TAG,
                        "Relative Path: ${relativePath ?: "unknown"}"
                    )
                }

                Log.d(
                    TAG,
                    "--------------------------------"
                )

                Log.d(
                    TAG,
                    "TOTAL AUDIO FILES: $count"
                )
            }

        } catch (e: Exception) {

            Log.e(
                TAG,
                "ERROR QUERYING MEDIASTORE",
                e
            )
        }

        return recordings
    }
}