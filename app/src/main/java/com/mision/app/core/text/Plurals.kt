package com.mision.app.core.text

/** "1 día" / "3 días": count plus the noun in the right number. */
fun plural(count: Int, singular: String, plural: String): String =
    "$count ${if (count == 1) singular else plural}"
