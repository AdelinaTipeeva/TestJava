package ru.example.filestorage.service;

import ru.example.filestorage.dto.NewFileIdResponse;
import ru.example.filestorage.dto.FileDto;

import java.util.Optional;

public interface FileService {

    Optional<FileDto> getFile(long id);

    Optional<NewFileIdResponse> saveFile(FileDto fileDto);
}
