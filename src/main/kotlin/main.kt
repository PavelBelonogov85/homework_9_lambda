import chat_data_classes.*
import expressions.NoObjectForId_Exception
import java.util.function.BiPredicate
import kotlin.reflect.KClass

object Service {
    private var tableChats: MutableList<Chat> = mutableListOf()
    private var tableMessages: MutableList<Message> = mutableListOf()
    private var tableUsers: MutableList<User> = mutableListOf()

    fun addChat(chat: Chat): Chat {
        val currentId: Long =
            (tableChats.size + 1).toLong() /* при условии, что не удаляем из коллекции - будут уникальными */
        val newChat = chat.copy(id = currentId)
        tableChats.add(newChat)
        return newChat
    }

    fun addUser(user: User): User {
        val currentId: Long = (tableUsers.size + 1).toLong()
        val newUser = user.copy(id = currentId)
        tableUsers.add(newUser)
        return newUser
    }

    fun addMessage(message: Message): Message {
        val currentId: Long = (tableMessages.size + 1).toLong()
        val newMessage = message.copy(
            id = currentId,
            positionInChat = (totalMessagesInChat(message.chatID) + 1)
        ) /* небольшое отличие в `positionInChat` */
        tableMessages.add(newMessage)
        return newMessage
    }

    fun readMessage(messageId: Long) {
        val currMessage = findAnyObjectById(messageId, tableMessages)
        tableMessages.remove(currMessage)
        tableMessages.add(currMessage.copy(wasRead = true))
    }


    fun <T : UniqueObjects> findAnyObjectById(id: Long, objectsList: MutableList<T>): T {
        for (currentObj in objectsList) {
            if (currentObj.id == id) {
                return currentObj
            }
        }
        val className: String? = if (objectsList.size > 0) {
            objectsList[objectsList.size - 1]::class.simpleName
        } else {
            "null"
        } // ?? А можно красивее?? Например T::class ??
        throw NoObjectForId_Exception(id, className ?: "null ")
    }


    fun getChatsList(
        withUserId: Long? = null,
        isDeleted: Boolean? = null,
        onlyUnreadMessages: Boolean = false
    ): List<Chat> {

        // 1 - только чаты с непрочитанными сообщениями или все подряд:
        var basicChatsList: MutableList<Chat> =
            mutableListOf()

        if (onlyUnreadMessages) {
            val unreadMessagesList = tableMessages.filter { message: Message -> !message.wasRead } // раз лямбда
            var chatsIDs: MutableList<Long> = mutableListOf()
            for (mess in unreadMessagesList) {
                if (!chatsIDs.contains(mess.chatID)) {
                    chatsIDs.add(mess.chatID)
                }
            }
            for (chatID in chatsIDs) {
                basicChatsList.add(findAnyObjectById(chatID, tableChats))
            }
        } else {
            basicChatsList = tableChats
        }

        // 2 - фильтр по удаленным:
        var resultChatsList: List<Chat> = if (isDeleted != null) {
            basicChatsList.filter { chat: Chat -> chat.isDeleted == isDeleted } // два лямбда
        } else {
            basicChatsList
        }

        // 3 - фильтр по пользователю:
        if (withUserId != null) {
            resultChatsList =
                resultChatsList.filter { chat: Chat -> chat.arrMembersID.contains(withUserId) } // три лямбда
        }

        return resultChatsList
    }


    fun deleteRecoverMessage(messageId: Long, isDeleted: Boolean) {
        val currMess = findAnyObjectById(messageId, tableMessages)
        editMessage(currMess.copy(isDeleted = isDeleted))
    }

    fun editMessage(message: Message): Message {
        val currMess = findAnyObjectById(message.id, tableMessages)
        val newMess = message.copy(wasRead = false)
        tableMessages.remove(currMess)
        tableMessages.add(newMess)

        // В общем случае хотелось бы сравнивать отличия по полям и исправлять только в случае необходимости:
        /*
        val java_fields = message::class.java.declaredFields
        for (field in java_fields) {
            println("${field.name} = ...") // field.get(message) - ругается: 'java.lang.IllegalAccessException' exception.
        }
        */
        /*
        val kotlin_class_members = message::class.members // ругается: "Method threw 'kotlin.jvm.KotlinReflectionNotSupportedError' exception." ?!
        for (funcAndFields in kotlin_class_members) {
            println("$funcAndFields = ...")
        }
        */

        return newMess
    }


    fun getUnreadChatsCount(UserId: Long? = null): String {
        val chatsQty = getChatsList(withUserId = UserId, onlyUnreadMessages = true).size
        return if (chatsQty == 0) "Нет сообщений" else chatsQty.toString()
    }


    fun deleteRecoverChat(chatId: Long, isDeleted: Boolean) {
        // грохнем сам чат:
        val currChat = findAnyObjectById(chatId, tableChats)
        tableChats.remove(currChat)
        tableChats.add(currChat.copy(isDeleted = isDeleted))
        // и все его сообщения:
        val MessagesOfThisChat =
            tableMessages.filter { message: Message -> message.chatID == chatId } // четвертая лямбда
        for (mess in MessagesOfThisChat) {
            deleteRecoverMessage(mess.id, isDeleted)
        }
    }

    fun showMessages(chatId: Long, firstMessagePosition: Long, totalMessages: Int): List<Message> {
        val lastMessageIndex = firstMessagePosition + totalMessages

        val resultMessagesList =
            tableMessages.filter { message: Message -> message.chatID == chatId && message.positionInChat >= firstMessagePosition && message.positionInChat <= lastMessageIndex } // еще

        for (currMess in resultMessagesList) {
            Service.readMessage(currMess.id)
        }

        return resultMessagesList
    }

    fun totalMessagesInChat(chatID:Long): Int {
        val MessagesList = tableMessages.filter { message: Message -> message.chatID == chatID}
        return MessagesList.size
    }
}


fun main() {
    val masha = User(1, "masha", "password", arrayOf(2, 3))
    val petya = User(2, "petya", "$%FYdnfb4f", emptyArray())
    val vasya = User(3, "vasya", "sdfJFG3f^$", emptyArray())

    Service.addUser(masha)
    Service.addUser(petya)
    Service.addUser(vasya)

    val MashaPetya_Chat = Service.addChat(Chat(arrMembersID = arrayOf(masha.id, petya.id)))
    val MashaVasya_Chat = Service.addChat(Chat(arrMembersID = arrayOf(masha.id, vasya.id)))

    val msg11 =
        Service.addMessage(Message(chatID = MashaPetya_Chat.id, authorID = masha.id, text = "Привет. Как дела?"))
    Service.readMessage(msg11.id)
    val msg12 = Service.addMessage(Message(chatID = MashaPetya_Chat.id, authorID = petya.id, text = "Привет. Да ниче"))
    val msg13 = Service.addMessage(Message(chatID = MashaPetya_Chat.id, authorID = petya.id, text = "А у тебя?"))

    var msg21 =
        Service.addMessage(Message(chatID = MashaVasya_Chat.id, authorID = vasya.id, text = "Что делаешь вечером?"))
    msg21 = Service.editMessage(msg21.copy(text = msg21.text + " ;-)"))
    Service.readMessage(msg21.id)
    val msg22 =
        Service.addMessage(Message(chatID = MashaVasya_Chat.id, authorID = masha.id, text = "Решаю задачки по Kotlin"))
    Service.readMessage(msg22.id)


    println("Все чаты Маши с непрочитанными сообщениями:")
    println(Service.getChatsList(masha.id, null, true))

    println("Вообще все неудаленные чаты:")
    println(Service.getChatsList(isDeleted = false))

    println("Кол-во непрочитанных чатов для пользователя:")
    println(Service.getUnreadChatsCount(masha.id))

    println("Вывести 5 (если есть) сообщений для чата Маша-Петя начиная со 2-го включительно")
    println(Service.showMessages(MashaPetya_Chat.id, 2, 5))
}