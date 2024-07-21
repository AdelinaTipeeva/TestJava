package ru.example.filestorage.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.example.filestorage.dto.FileDto;
import ru.example.filestorage.dto.NewFileIdResponse;
import ru.example.filestorage.entity.File;
import ru.example.filestorage.repository.FileRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final FileRepository fileRepository;

    public Optional<FileDto> getFile(long id) {
        return fileRepository.findById(id)
                .map(file -> new FileDto(file.getTitle(), file.getCreationDate(), file.getDescription(), file.getContent()));
    }

    public Optional<NewFileIdResponse> saveFile(FileDto fileDto) {
        var file = fileRepository.findByTitle(fileDto.title());
        if (file.isPresent()) {
            return Optional.empty();
        }

        File newFile = File.builder()
                .title(fileDto.title())
                .creationDate(fileDto.creationDate())
                .description(fileDto.description())
                .content(fileDto.content())
                .build();
        newFile = fileRepository.save(newFile);
        return Optional.of(new NewFileIdResponse(newFile.getId()));
    }
}
