package expressions

class NoObjectForId_Exception(id:Long, className:String) : RuntimeException("Объект $className с ID=$id не найден")