package com.example.nexusforge.frontend

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nexusforge.frontend.components.NameAppBar
import com.example.nexusforge.viewmodels.RegViewModel

@Composable
fun TechnicalPage(
    vm: RegViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxSize()) {
        NameAppBar(
            onBackClick = onBackClick,
            onProfileClick = onProfileClick,
            namePage = "Техническая поддержка",
            userPhotoUrl = vm.userPhotoUrl
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Контакты поддержки",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "Email",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = "support@nexusforge.com",
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "Telegram",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = "@nexusforge_support",
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "Часы работы",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = "Пн-Пт: 9:00 - 18:00 (МСК)",
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Text(
                text = "Часто задаваемые вопросы",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp, top = 8.dp)
            )

            Text(
                text = "Как создать модпак?",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = "Нажмите на кнопку 'Создать' в нижней панели навигации и выберите 'Создать модпак'.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Text(
                text = "Как добавить проект в избранное?",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = "Откройте страницу проекта и нажмите на иконку закладки в правом верхнем углу.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Text(
                text = "Как изменить тему приложения?",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = "Перейдите в раздел 'Настройки' и нажмите на иконку темы для переключения между светлой и темной темой.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TechnicalPagePreview() {
    MaterialTheme {
        TechnicalPage()
    }
}
