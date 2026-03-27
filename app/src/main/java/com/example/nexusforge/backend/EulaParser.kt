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

    val lines = rawEulaText
        .split("\n", "\r\n")
        .map { it.trim() }
        .filter { it.isNotEmpty() }

    val paragraphBuilder = StringBuilder()

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

        // 1. ПРОВЕРКА НА ГЛАВНЫЙ ЗАГОЛОВОК (Начинается с "Цифра. Пробел")
        // Пример: "1. Общие положения"
        val isMainHeading = trimmedLine.matches("^\\d+\\.\\s+.*".toRegex())

        // 2. ПРОВЕРКА НА ПОДПУНКТ (Начинается с "Цифра.Цифра..." или "Цифра.Буква")
        // Пример: "1.1. Область применения" или "2.1.3."
        val isSubParagraph = trimmedLine.matches("^\\d+\\.\\d+.*".toRegex())

        // 3. ПРОВЕРКА НА КОНТАКТЫ
        val isContact = trimmedLine.startsWith("Контактные данные Владельца:", ignoreCase = true)

        when {
            isMainHeading -> {
                commitCurrentParagraph()
                sections.add(EulaSection(currentId++, trimmedLine, EulaType.HEADING))
            }

            isContact -> {
                commitCurrentParagraph()
                sections.add(EulaSection(currentId++, trimmedLine, EulaType.CONTACT))
            }

            isSubParagraph -> {
                // Если встретили подпункт — сохраняем то, что накопили ранее,
                // и сразу создаем новую секцию для этого подпункта
                commitCurrentParagraph()
                sections.add(EulaSection(currentId++, trimmedLine, EulaType.PARAGRAPH))
            }

            else -> {
                // Если это обычный текст без цифр в начале — склеиваем
                if (paragraphBuilder.isNotEmpty()) {
                    paragraphBuilder.append(" ")
                }
                paragraphBuilder.append(trimmedLine)
            }
        }
    }

    commitCurrentParagraph()
    return sections
}