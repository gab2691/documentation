package br.documentation.service;

import br.documentation.dto.SidenavDTO;
import br.documentation.dto.TreeViewDTO;
import br.documentation.exception.FileNotAccessibleException;
import br.documentation.exception.SaveFileException;
import br.documentation.exception.UpdateTreeViewFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.List;

@Slf4j
@Service
public class FileService {

    @Value("${directory.file}")
    private String directoryFile;

    @Value("${directory.treeView-file}")
    private String treeViewFile;

    public void saveFile(String data, String path, String fileName) {
        log.info("Starting saving file {} in Path {}", fileName, path);
        fileName = Normalizer.normalize(fileName, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "");

        Path basePath = Paths.get(directoryFile);
        path = path.substring(1);

        String[] segments = path.split("/");
        Path directoryPath;

        if (segments.length == 2) {
            directoryPath = basePath.resolve(segments[0]);
        } else {
            directoryPath = basePath.resolve(path).getParent();
        }

        Path finalPath = directoryPath.resolve(fileName + ".md");

        try {
            Files.write(finalPath, data.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            log.warn("Error tryng saving file {} in Path {}", fileName, path);
            throw new SaveFileException("Error to save file: " + e.getMessage());
        }
    }

    public SidenavDTO transformTreeViewFileToDTO() {
        File file = new File(treeViewFile);
        log.info("Getting file {} from directory {} ", file.getName() , directoryFile);

        if (!file.exists() || !file.canRead()) {
            throw new FileNotAccessibleException("File not available or no exists");
        }

        try {
            log.info("Tranform file into DTO");
            return new ObjectMapper().readValue(file, SidenavDTO.class);
        } catch (IOException e) {
            log.warn("Error tryng transform file {} to DTO ", file.getName(), SidenavDTO.class.getName());
            throw new IllegalArgumentException("Erro tryng create TreeViewDTO, verify file" + e.getCause());
        }
    }

    public boolean comparePaths(String directory, Path folder){
        return Paths.get(directory).getFileName().endsWith(folder);
    }

    public void updateTreeView(List<TreeViewDTO> treeViewDTO) {
        log.info("Saving new version of TreeView in folder {}", treeViewFile);

        try{
            ObjectMapper objectMapper = new ObjectMapper();
            File file = new File(treeViewFile);

            objectMapper.writeValue(file, treeViewDTO);
        } catch (IOException e){
            log.error("Error tryng update file TreeView");
            throw new UpdateTreeViewFile("Error tryng update file TreeView");
        }
    }
}
