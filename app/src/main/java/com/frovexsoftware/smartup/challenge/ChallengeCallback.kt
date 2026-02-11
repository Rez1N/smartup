package com.frovexsoftware.smartup.challenge

/**
 * Interface for challenge fragments to communicate completion back to the host activity.
 * Eliminates tight coupling between fragments and StopAlarmActivity.
 */
interface ChallengeCallback {
    fun onChallengeCompleted()
}
