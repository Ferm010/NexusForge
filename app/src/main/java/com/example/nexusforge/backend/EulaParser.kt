package com.example.nexusforge.backend

enum class EulaType {
    HEADING,    // Заголовок раздела (1. Общие положения)
    PARAGRAPH,  // Основной текст
    CONTACT     // Контактные данные Владельца
}

// EulaSection.kt
data class EulaSection(
    val id: Int,
    val content: String,
    val type: EulaType
)

// EulaParser.kt
fun parseEulaText(rawEulaText: String): List<EulaSection> {
    val sections = mutableListOf<EulaSection>()
    var currentId = 1

    // 1. Подготовка: Разделение по строкам, удаление пробелов, игнорирование пустых строк
    val lines = rawEulaText
        .split("\n", "\r\n")
        .map { it.trim() }
        .filter { it.isNotEmpty() }

    val paragraphBuilder = StringBuilder()

    // Вспомогательная функция для сохранения накопленного параграфа
    fun commitCurrentParagraph() {
        if (paragraphBuilder.isNotEmpty()) {
            sections.add(
                EulaSection(
                    id = currentId++,
                    content = paragraphBuilder.toString().trim(),
                    type = EulaType.PARAGRAPH
                )
            )
            paragraphBuilder.clear()
        }
    }

    for (line in lines) {
        val trimmedLine = line.trim()

        // 2. Идентификация Заголовка: Строка начинается с X. YYY (e.g., "1. Общие...")
        // Исключаем подпункты (1.1., 1.2. и т.д.) из этой проверки
        if (trimmedLine.matches("^\\d+\\..*".toRegex()) && !trimmedLine.matches("^\\d+\\.\\d+\\..*".toRegex())) {

            commitCurrentParagraph() // Сохраняем предыдущий параграф

            sections.add(
                EulaSection(
                    id = currentId++,
                    content = trimmedLine,
                    type = EulaType.HEADING
                )
            )
        }
        // 3. Идентификация Контактов
        else if (trimmedLine.startsWith("Контактные данные Владельца:", ignoreCase = true)) {

            commitCurrentParagraph() // Сохраняем предыдущий параграф

            sections.add(
                EulaSection(
                    id = currentId++,
                    content = trimmedLine,
                    type = EulaType.CONTACT
                )
            )
        }
        // 4. Всё остальное — Параграф
        else {
            if (paragraphBuilder.isNotEmpty()) {
                paragraphBuilder.append(" ") // Добавляем пробел для объединения строк
            }
            paragraphBuilder.append(trimmedLine)
        }
    }

    commitCurrentParagraph() // Сохраняем последний параграф
    return sections
}