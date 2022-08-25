import chat_data_classes.Chat
import chat_data_classes.Message
import chat_data_classes.User
import expressions.NoObjectForId_Exception
import org.junit.Test

import org.junit.Assert.*

class ServiceTest {

    fun getDefaultFilling() {
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
    }

    @Test
    fun showMessages_summaryMessagesText() {
        getDefaultFilling()
        val result = Service.showMessages(1, 2, 5)
                                .joinToString(separator = "/n"){it.text}
        assertEquals("Привет. Да ниче/nА у тебя?", result)
    }


    @Test(expected = NoObjectForId_Exception::class)
    fun readMessage_wrongMessageId() {
        //getDefaultFilling()
        Service.readMessage(55)
    }


}