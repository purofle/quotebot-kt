package com.github.purofle.quotebot.tdlibhelper

data class QuoteUser(
    val id: Long?,
    val fullName: String,
    val avatar: ByteArray?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as QuoteUser

        if (id != other.id) return false
        if (fullName != other.fullName) return false
        if (!avatar.contentEquals(other.avatar)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + fullName.hashCode()
        result = 31 * result + (avatar?.contentHashCode() ?: 0)
        return result
    }
}
