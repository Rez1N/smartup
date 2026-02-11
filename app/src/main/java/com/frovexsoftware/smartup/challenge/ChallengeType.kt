package com.frovexsoftware.smartup.challenge

import com.frovexsoftware.smartup.R

/**
 * All available alarm challenge types with their associated icon and label resources.
 */
enum class ChallengeType(val iconRes: Int, val labelRes: Int) {
    NONE(R.drawable.ic_leaf, R.string.edit_morning_me),
    TEXT(R.drawable.ic_leaf, R.string.edit_morning_me),
    SNAKE(R.drawable.ic_challenge_snake, R.string.chip_snake),
    DOTS(R.drawable.ic_challenge_dots, R.string.chip_dots),
    MATH(R.drawable.ic_challenge_math, R.string.chip_math),
    DATE(R.drawable.ic_leaf, R.string.edit_morning_me),
    COLOR(R.drawable.ic_challenge_color, R.string.chip_color),
    SHAKE(R.drawable.ic_challenge_shake, R.string.challenge_shake),
    HOLD(R.drawable.ic_challenge_hold, R.string.challenge_hold);

    companion object {
        fun from(value: String?): ChallengeType {
            if (value == null) return NONE
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: NONE
        }
    }
}
