package ru.example.filestorage.contoller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
public class FileControllerIntegrationTest {

    private static final String BASE_URI_STRING = "http://localhost/api/v1/files";
    private static final Locale LOCALE = Locale.of("ru");

    @Autowired
    private MockMvc mockMvc;

    @Test
    @Sql("/files.sql")
    void getFile_FileIdExists_ReturnsFile() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.get(BASE_URI_STRING + "/" + 1);

        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isOk(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON),
                        content().json("""
                                {
                                  "title": "title",
                                  "creation_date": "2024.01.01 00:00:00",
                                  "description": "description",
                                  "content": "Y29udGVudA=="
                                }""")
                );
    }

    @Test
    @Sql("/files.sql")
    void getFile_FileIdDoesNotExist_ReturnsNotFound() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.get(BASE_URI_STRING + "/" + 2);

        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isNotFound()
                );
    }

    @Test
    void saveFile_ValidData_ReturnsId() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.post(BASE_URI_STRING)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "title": "title",
                          "creation_date": "2024.01.01 00:00:00",
                          "description": "description",
                          "content": "Y29udGVudA=="
                        }""");

        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isCreated(),
                        header().string(HttpHeaders.LOCATION, BASE_URI_STRING + "/" + 1),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json("{'id': 1}")
                );
    }

    @Test
    @Sql("/files.sql")
    void saveFile_DuplicateTitle_ReturnsProblemDetail() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.post(BASE_URI_STRING)
                .locale(LOCALE)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "title": "title",
                          "creation_date": "2024.01.01 00:00:00",
                          "description": "description",
                          "content": "Y29udGVudA=="
                        }""");

        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isConflict(),
                        content().contentType(MediaType.APPLICATION_PROBLEM_JSON),
                        content().json("{'errors': ['Файл с названием title уже существует']}")
                );
    }

    @Test
    void saveFile_InvalidDateFormat_ReturnsProblemDetail() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.post(BASE_URI_STRING)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "title": "title",
                          "creation_date": "not a date",
                          "description": "description",
                          "content": "Y29udGVudA=="
                        }""");

        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentType(MediaType.APPLICATION_PROBLEM_JSON),
                        content().json("{'errors': ['Неверный формат даты']}")
                );
    }

    @Test
    void saveFile_InvalidDto_ReturnsProblemDetail() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.post(BASE_URI_STRING)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "title": " ",
                          "description": "description",
                          "content": "Y29udGVudA=="
                        }""");

        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentType(MediaType.APPLICATION_PROBLEM_JSON),
                        content().json("""
                                {
                                  "errors": [
                                    "Название файла не может быть пустым",
                                    "Дата и время отправки файла не могут быть пустыми"
                                  ]
                                }""")
                );
    }
}
