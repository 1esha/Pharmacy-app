package com.example.domain

/**
 * Класс [DisconnectionException] - исключение возникающее при отсутствии подключения к интернету.
 */
class DisconnectionException: Exception()

/**
 * Класс [IdentificationException] - исключение возникающее при ошибках с идентификатором пользователя.
 */
class IdentificationException: Exception()

/**
 * Класс [InputDataException] - исключение возникающее при вводе данных, когда значение пустое.
 */
class InputDataException: Exception()

/**
 * Класс [ServerException] - исключение возникающее при получении ответа с сервера, когда статус код не успешный.
 *
 * Параметры:
 * [serverMessage] - сообщение от сервера, описывающее ошибку.
 */
class ServerException(val serverMessage: String?): Exception()
