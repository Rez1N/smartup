package com.frovexsoftware.smartup

enum class ChallengeType {
    NONE,
    TEXT,
    SNAKE,
    DOTS;

    companion object {
        fun from(value: String?): ChallengeType {
            if (value == null) return NONE
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: NONE
        }
    }
}
