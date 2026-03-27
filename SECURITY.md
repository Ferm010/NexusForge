# NexusForge - Защита от декомпиляции

## Реализованные меры безопасности

### 1. ✅ ProGuard/R8 Обфускация
**Файл:** `app/build.gradle.kts` + `app/proguard-rules.pro`

**Что делает:**
- Переименовывает классы, методы и переменные в нечитаемые имена (a, b, c...)
- Удаляет неиспользуемый код
- Оптимизирует байт-код
- Удаляет логи в release сборке
- Сжимает ресурсы

**Уровень защиты:** Высокий

---

### 2. ✅ Защита API ключей
**Файлы:** `FirebaseConfig.kt` + `local.properties` + `build.gradle.kts`

**Что делает:**
- Ключи хранятся в `local.properties` (не попадает в git)
- Читаются через BuildConfig во время компиляции
- Обфусцируются ProGuard

**Важно:** 
- `local.properties` уже в `.gitignore`
- Для CI/CD добавьте ключи в секреты GitHub Actions

---

### 3. ✅ Защита от Tampering
**Файл:** `SecurityCheck.kt` + `MainActivity.kt`

**Что делает:**
- Проверяет подпись APK при запуске
- Обнаруживает root устройства
- Обнаруживает эмуляторы
- Закрывает приложение если обнаружена модификация

**Настройка:**
1. Соберите release APK
2. Запустите приложение в debug режиме
3. В logcat найдите: `Current signature hash: XXXXXX`
4. Скопируйте хеш в `SecurityCheck.kt` → `EXPECTED_SIGNATURE`

---

### 4. ✅ Certificate Pinning
**Файлы:** `network_security_config.xml` + `AndroidManifest.xml`

**Что делает:**
- Блокирует HTTP трафик (только HTTPS)
- Проверяет SSL сертификаты Firebase/Google
- Защищает от MITM атак
- Блокирует прокси-перехватчики (Charles, Fiddler)

---

## Инструкция по сборке защищенного APK

### Шаг 1: Настройка ключей
```bash
# Убедитесь что local.properties содержит:
WEB_CLIENT_ID=ваш_client_id
```

### Шаг 2: Создание keystore для подписи
```bash
keytool -genkey -v -keystore nexusforge.keystore -alias nexusforge -keyalg RSA -keysize 2048 -validity 10000
```

### Шаг 3: Настройка подписи в build.gradle.kts
Добавьте в `app/build.gradle.kts`:
```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("../nexusforge.keystore")
            storePassword = "ваш_пароль"
            keyAlias = "nexusforge"
            keyPassword = "ваш_пароль"
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            // ...
        }
    }
}
```

### Шаг 4: Сборка release APK
```bash
./gradlew assembleRelease
```

### Шаг 5: Получение хеша подписи
1. Установите debug версию
2. Запустите приложение
3. В logcat найдите: `Current signature hash: XXXXXX`
4. Скопируйте хеш в `SecurityCheck.kt`:
```kotlin
private const val EXPECTED_SIGNATURE = "ваш_хеш_здесь"
```
5. Пересоберите release APK

---

## Что НЕ попадает в git (проверьте .gitignore)
- ✅ `google-services.json` - Firebase конфигурация
- ✅ `local.properties` - API ключи
- ✅ `.idea/` - настройки IDE
- ✅ `.gradle/` - кеш сборки
- ✅ `*.keystore` - ключи подписи (добавьте если создали)

---

## Дополнительные рекомендации

### Уровень 1 (Реализовано):
- ✅ ProGuard обфускация
- ✅ Защита ключей через BuildConfig
- ✅ Certificate pinning
- ✅ Проверка подписи APK

### Уровень 2 (Опционально):
- 🔲 NDK (C++) для хранения критичных ключей
- 🔲 DexGuard (платная версия ProGuard)
- 🔲 Root detection библиотеки (RootBeer)
- 🔲 SafetyNet Attestation API

### Уровень 3 (Для критичных приложений):
- 🔲 Backend сервер для всех API запросов
- 🔲 Шифрование SharedPreferences
- 🔲 Runtime Application Self-Protection (RASP)
- 🔲 Обфускация строк (StringFog)

---

## Важно понимать

⚠️ **Полная защита невозможна!**

Любое приложение можно декомпилировать, но мы максимально усложнили задачу:
- Код превращается в нечитаемую кашу
- Ключи не хранятся в открытом виде
- Модифицированное APK не запустится
- MITM атаки заблокированы

Для 99% хакеров этого достаточно. Для оставшегося 1% нужен backend сервер.

---

## Тестирование защиты

### Проверка обфускации:
```bash
./gradlew assembleRelease
# Декомпилируйте APK через jadx-gui
# Код должен быть нечитаемым
```

### Проверка tampering:
```bash
# Измените что-то в APK и пересоберите
# Приложение должно закрыться при запуске
```

### Проверка certificate pinning:
```bash
# Попробуйте перехватить трафик через Charles Proxy
# Запросы должны падать с SSL ошибкой
```

---

## Контакты
Если нашли уязвимость - создайте issue в GitHub (не публикуйте детали публично).
