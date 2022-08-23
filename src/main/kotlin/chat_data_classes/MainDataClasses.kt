package chat_data_classes

interface UniqueObjects {
    val id: Long
}


sealed interface ObjectsInLists {
    /*
    data class Chat()

    data class Message()

    data class User()

    */
}


data class Chat(
    override val id: Long = 0,
    val arrMembersID: Array<Long>, /* в общем случае могут быть групповые чаты, но можно использовать [0] [1] */
    val isDeleted: Boolean = false
) : UniqueObjects

data class Message(
    override val id: Long = 0,
    val chatID: Long,
    val authorID: Long,
    val positionInChat: Int = 0,
    val text: String = "",
    val isDeleted: Boolean = false,
    val wasRead: Boolean = false,
) : UniqueObjects {
    override fun toString(): String {
        //return super.toString()
        var result:String = "{\n  \"authorID\": \"$authorID\",\n  \"positionInChat\": \"$positionInChat\" \n  \"text\": \"$text\" \n}"
        return result
    }
}

data class User(
    override val id: Long,
    val login: String,
    val password: String,
    val arrFriendsId: Array<Long>
) : UniqueObjects {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as User

        if (id != other.id) return false
        if (login != other.login) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + login.hashCode()
        return result
    }
}


