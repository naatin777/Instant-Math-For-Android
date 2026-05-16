package com.naatin777.instantmath.ui.home

enum class HomeListTab {
    FAVORITE,
    HISTORY,
    ;

    fun dummyEntries(): List<FormulaEntry> = when (this) {
        FAVORITE -> listOf(
            FormulaEntry("x = (-b ± √(b² - 4ac)) / 2a", isFavorite = true, timestamp = "Pinned"),
            FormulaEntry("e^(iπ) + 1 = 0", isFavorite = true, timestamp = "Pinned"),
            FormulaEntry("a² + b² = c²", isFavorite = true, timestamp = "Pinned"),
            FormulaEntry("A = πr²", isFavorite = true, timestamp = "Pinned"),
        )
        HISTORY -> listOf(
            FormulaEntry("d/dx sin(x) = cos(x)", isFavorite = true, timestamp = "2 min ago"),
            FormulaEntry("∫ (1/x) dx = ln|x| + C", isFavorite = false, timestamp = "15 min ago"),
            FormulaEntry("(a + b)^n = Σ C(n,k) a^(n-k) b^k", isFavorite = false, timestamp = "1 hour ago"),
            FormulaEntry("e^x = Σ x^n / n!", isFavorite = true, timestamp = "Yesterday"),
            FormulaEntry("c² = a² + b² - 2ab cos(C)", isFavorite = false, timestamp = "3 days ago"),
        )
    }
}
