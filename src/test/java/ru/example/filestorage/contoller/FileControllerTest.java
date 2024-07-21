package ru.example.filestorage.contoller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.validation.MapBindingResult;
import org.springframework.web.util.UriComponentsBuilder;
import ru.example.filestorage.dto.FileDto;
import ru.example.filestorage.dto.NewFileIdResponse;
import ru.example.filestorage.service.FileService;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileControllerTest {

    private static final String BASE_URI_STRING = "http://localhost/api/v1/files";
    private static final Locale LOCALE = Locale.of("ru");

    @Mock
    private FileService fileService;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private FileController fileController;

    @Test
    void getFile_FileIdExists_ReturnsFile() {
        final var fileDto = new FileDto("title", LocalDateTime.now(), "description", "Y29udGVudA==");

        when(fileService.getFile(anyLong()))
                .thenReturn(Optional.of(fileDto));

        var responseEntity = fileController.getFile(1);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(fileDto, responseEntity.getBody());

        verify(fileService).getFile(anyLong());
        verifyNoMoreInteractions(fileService);
    }

    @Test
    void getFile_FileIdDoesNotExist_ReturnsNotFound() {
        var responseEntity = fileController.getFile(1);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertNull(responseEntity.getBody());

        verify(fileService).getFile(anyLong());
        verifyNoMoreInteractions(fileService);
    }

    @Test
    void saveFile_ValidData_ReturnsId() {
        final var savedFileId = new NewFileIdResponse(1L);
        final var fileDto = new FileDto("title", LocalDateTime.now(), "description", "Y29udGVudA==");
        final var bindingResult = new MapBindingResult(Map.of(), "fileDto");
        final var uriComponentsBuilder = UriComponentsBuilder.fromUriString(BASE_URI_STRING);

        when(fileService.saveFile(fileDto))
                .thenReturn(Optional.of(savedFileId));

        var result = this.fileController.saveFile(fileDto, bindingResult, uriComponentsBuilder, LOCALE);

        assertNotNull(result);
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(URI.create(BASE_URI_STRING + "/" + savedFileId.id()), result.getHeaders().getLocation());
        assertEquals(savedFileId, result.getBody());

        verify(fileService).saveFile(fileDto);
        verifyNoMoreInteractions(fileService);
    }

    @Test
    void saveFile_DuplicateTitle_ReturnsProblemDetail() {
        final var savedFileId = new NewFileIdResponse(1L);
        final var fileDto = new FileDto("title", LocalDateTime.now(), "description", "Y29udGVudA==");
        final var bindingResult = new MapBindingResult(Map.of(), "fileDto");
        final var uriComponentsBuilder = UriComponentsBuilder.fromUriString(BASE_URI_STRING);

        when(fileService.saveFile(fileDto))
                .thenReturn(Optional.of(savedFileId), Optional.empty());
        when(messageSource.getMessage(anyString(), eq(new Object[]{fileDto.title()}), anyString(), eq(LOCALE)))
                .thenReturn(anyString());

        this.fileController.saveFile(fileDto, bindingResult, uriComponentsBuilder, LOCALE);
        var result = this.fileController.saveFile(fileDto, bindingResult, uriComponentsBuilder, LOCALE);

        assertNotNull(result);
        assertEquals(HttpStatus.CONFLICT, result.getStatusCode());
        var problemDetail = (ProblemDetail) result.getBody();
        assertNotNull(problemDetail);
        var properties = problemDetail.getProperties();
        assertNotNull(properties);
        assertEquals(1, properties.size());
        var errors = (List<String>) properties.get("errors");
        assertNotNull(errors);
        assertEquals(1, errors.size());

        verify(fileService, times(2)).saveFile(fileDto);
        verifyNoMoreInteractions(fileService);
    }

    @Test
    void saveFile_InvalidDto_ReturnsProblemDetail() {
        final var fileDto = new FileDto("  ", null, "description", "Y29udGVudA==");
        final var bindingResult = new MapBindingResult(Map.of(), "fileDto");
        bindingResult.addError(new FieldError("fileDto", "title", "title error"));
        bindingResult.addError(new FieldError("fileDto", "creation_date", "date error"));
        final var uriComponentsBuilder = UriComponentsBuilder.fromUriString(BASE_URI_STRING);

        var result = this.fileController.saveFile(fileDto, bindingResult, uriComponentsBuilder, LOCALE);

        assertNotNull(result);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        var problemDetail = (ProblemDetail) result.getBody();
        assertNotNull(problemDetail);
        var properties = problemDetail.getProperties();
        assertNotNull(properties);
        assertEquals(1, properties.size());
        var errors = (List<String>) properties.get("errors");
        assertNotNull(errors);
        assertEquals(2, errors.size());

        verifyNoInteractions(fileService);
    }
}