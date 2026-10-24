package app.valenced.manager.utils

import java.io.Serializable

data class DiscordVersion(
    val major: Int,
    val minor: Int,
    val type: Type
) : Serializable, Comparable<DiscordVersion> {

    enum class Type(val label: String) {
        STABLE("Stable"),
        BETA("Beta"),
        ALPHA("Alpha")
    }

    override fun compareTo(other: DiscordVersion): Int =
        toVersionCode().toInt() - other.toVersionCode().toInt()

    override fun toString() = "$major.$minor - ${type.label}"

    fun toVersionCode() = "$major${type.ordinal}${if (minor < 10) 0 else ""}${minor}"

    companion object {

        /** Parses a version name in the form [toString] writes, such as `350.8 - Stable`. */
        fun fromVersionName(name: String): DiscordVersion? {
            val (number, label) = name.split(" - ", limit = 2).takeIf { it.size == 2 } ?: return null
            val (major, minor) = number.split(".").mapNotNull { it.toIntOrNull() }.takeIf { it.size == 2 } ?: return null
            val type = Type.entries.firstOrNull { it.label == label } ?: return null
            return DiscordVersion(major, minor, type)
        }

        fun fromVersionCode(string: String): DiscordVersion? = with(string) {
            if (length < 4) return@with null
            if (toIntOrNull() == null) return@with null
            if (toInt() <= 126021) return@with null
            val codeReversed = toCharArray().reversed().joinToString("")
            val typeInt = codeReversed[2].toString().toInt()
            val type = Type.values().getOrNull(typeInt) ?: return@with null
            DiscordVersion(
                codeReversed.slice(3..codeReversed.lastIndex).reversed().toInt(),
                codeReversed.substring(0, 2).reversed().toInt(),
                type
            )
        }

    }

}