package ru.example.filestorage.contoller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import ru.example.filestorage.dto.FileDto;
import ru.example.filestorage.service.FileService;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;
    private final MessageSource messageSource;

    @GetMapping(value = "/{fileId:\\d+}")
    public ResponseEntity<FileDto> getFile(@PathVariable("fileId") long fileId) {
        return ResponseEntity.of(fileService.getFile(fileId));
    }

    @PostMapping
    public ResponseEntity<?> saveFile(@RequestBody @Valid FileDto fileDto,
                                      BindingResult bindingResult,
                                      UriComponentsBuilder uriComponentsBuilder,
                                      Locale locale) {
        if (bindingResult.hasErrors()) {
            var problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
            problemDetail.setProperty("errors", bindingResult.getAllErrors().stream()
                    .map(ObjectError::getDefaultMessage)
                    .toList());
            return ResponseEntity
                    .badRequest()
                    .body(problemDetail);
        }

        var newFileIdResponse = fileService.saveFile(fileDto);
        if (newFileIdResponse.isPresent()) {
            return ResponseEntity
                    .created(uriComponentsBuilder
                            .replacePath("api/v1/files/{fileId}")
                            .build(newFileIdResponse.get().id()))
                    .body(newFileIdResponse.get());
        }

        var message = messageSource.getMessage("filestorage.file.error.already_exists", new Object[]{fileDto.title()},
                "filestorage.file.error.already_exists", locale);
        var problemDetail = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problemDetail.setProperty("errors", List.of(Objects.requireNonNull(message)));
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(problemDetail);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleInvalidDateFormat(Locale locale) {
        var message = messageSource.getMessage("filestorage.file.error.date_format", new Object[0],
                "filestorage.file.error.date_format", locale);
        var problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setProperty("errors", List.of(Objects.requireNonNull(message)));
        return ResponseEntity
                .badRequest()
                .body(problemDetail);
    }
}
