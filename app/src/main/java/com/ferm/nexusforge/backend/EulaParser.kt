package com.ferm.nexusforge.backend

enum class EulaType {
    HEADING,
    PARAGRAPH,
    CONTACT
}


data class EulaSection(
    val id: Int,
    val content: String,
    val type: EulaType
)


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

        // РОВЕРКА НА ГЛАВНЫЙ ЗАГОЛОВОК
        val isMainHeading = trimmedLine.matches("^\\d+\\.\\s+.*".toRegex())

        // ПРОВЕРКА НА ПОДПУНКТ
        val isSubParagraph = trimmedLine.matches("^\\d+\\.\\d+.*".toRegex())

        // ПРОВЕРКА НА КОНТАКТЫ
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
                commitCurrentParagraph()
                sections.add(EulaSection(currentId++, trimmedLine, EulaType.PARAGRAPH))
            }

            else -> {
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