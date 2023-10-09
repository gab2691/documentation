package br.documentation.service;

import br.documentation.dto.SidenavDTO;
import br.documentation.dto.TreeViewDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class DocumentationService {

    @Autowired
    private HttpService httpService;

    @Autowired
    private JGitService jGitService;

    private static final String DIRECTORY_FILE = "C:\\Users\\T734536\\OneDrive - Santander Office 365\\Documents\\Workspace\\docsRepo\\docs\\docs";
    private static final String TREE_VIEW_FILE = "tree-view.json";
    private static final String CONFLUENCE_PATH = "https://confluence.santanderbr.corp";
    private Map<String, File> mapFile = new HashMap<>();
    private String titleTreeView = "";
    private String titleDoc = "";


    public void updateDocdFromConfluence() {
        SidenavDTO sidenavDTO = getJsonConfluenceFIle();
        contructAndPushDoc(sidenavDTO.getSidenav());
    }

    private void contructAndPushDoc(List<TreeViewDTO> sidenav) {
        if (sidenav == null) {
            return;
        }
        for (TreeViewDTO n : sidenav) {
            if (!n.getUrlConfluence().isEmpty() || !n.getUrlConfluence().isBlank()) {
                String docFromConfluence = httpService.getDocFromConfluence(n.getUrlConfluence());
                Document docFromHTML = createDocFromHTML(docFromConfluence);
                String docMd = converterHTMLToMD(docFromHTML.html());
                jGitService.updateLocalRepo();
                saveFile(docMd, n.getUrl(), this.titleDoc);
                jGitService.pushDoc(this.titleDoc);
            }
            contructAndPushDoc(n.getChildren());
        }
    }

    public TreeViewDTO getTreeViewDTO() {
        File file = mapFile.get(TREE_VIEW_FILE);
        if (!file.exists() || !file.canRead()) {
            throw new RuntimeException("Erro tryng create TreeViewDTO, verify file");
        }

        try {
            TreeViewDTO treeViewDTOList = new ObjectMapper().readValue(file, TreeViewDTO.class);

            return treeViewDTOList;
        } catch (IOException e) {
            throw new RuntimeException("Erro tryng create TreeViewDTO, verify file" + e.getCause());
        }

    }


    public void getOneDoc(String url) {
        String docFromConfluence = httpService.getDocFromConfluence(url);
        Document docFromHTML = createDocFromHTML(docFromConfluence);
        String htmlToMD = converterHTMLToMD(docFromHTML.html());
        htmlToMD = cleanMd(htmlToMD);
        /*saveFile(htmlToMD, this.titleDoc);*/
    }

    public void saveFile(String data, String path, String fileName) {
        fileName = Normalizer.normalize(fileName, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "");

        Path basePath = Paths.get(DIRECTORY_FILE);
        path = path.substring(1);

        String[] segments = path.split("/");
        Path directoryPath;

        if (segments.length == 2){
            directoryPath = basePath.resolve(segments[0]);
        } else {
            directoryPath = basePath.resolve(path).getParent();
        }

        Path finalPath = directoryPath.resolve(fileName + ".md");

        try{
            Files.write(finalPath, data.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e){
            throw new RuntimeException("Error to save file: " + e.getMessage());
        }
    }

    public void updateFile(Object object, String file) {
        log.info("Inity saving File: " + file);
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        try {
            JsonNode jsonNode = mapper.valueToTree(object);
            mapper.writeValue(new File(DIRECTORY_FILE + "\\" + file), jsonNode);

            log.info("File saved in folder: " + DIRECTORY_FILE + "\\" + file);
        } catch (Exception e) {
            throw new RuntimeException("Error saving file: " + e.getCause());
        }
    }

    public String cleanMd(String data) {
        Matcher matcher = Pattern.compile("\\{#[^{}]*\\}").matcher(data);
        StringBuffer stringBuffer = new StringBuffer();

        while (matcher.find()) {
            matcher.appendReplacement(stringBuffer, "");
        }
        return matcher.appendTail(stringBuffer).toString();
    }


    private void updateTreeViewFile() {
        File file = this.mapFile.get(TREE_VIEW_FILE);
        if (file != null && file.exists() && file.canRead()) {
            FileReader fileReader = null;
            try {
                fileReader = new FileReader(file);
                TreeViewDTO treeViewDTOList = new ObjectMapper().readValue(file, TreeViewDTO.class);

                /*TreeViewDTO navItemDTO = new TreeViewDTO(titleTreeView, "/" + titleDoc);

                if (!treeViewDTOList.getSidenav().contains(navItemDTO)) {
                    treeViewDTOList.getSidenav().add(navItemDTO);
                    updateFile(treeViewDTOList, file.getName());
                }*/

                log.info("Tree-View file was uploaded with successes");
            } catch (Exception e) {
                throw new RuntimeException("Error to update file", e);
            } finally {
                if (fileReader != null) {
                    try {
                        fileReader.close();
                    } catch (IOException e) {
                        throw new RuntimeException("Error tring closing de file: " + e.getCause());
                    }
                }
            }
        }
    }

    private Document createDocFromHTML(String html) {
        if (html != null && !html.isEmpty() && !html.isBlank()) {
            log.info("Creating DOC");

            Document document = Jsoup.parse(html);
            Element title = document.getElementById("title-text");

            log.info("Extracting title-page " + title.text());

            this.titleTreeView = title.text().toLowerCase();
            this.titleDoc = title.text().replaceAll("[^a-zA-Z0-9\\- ]", "").replaceAll(" ", "-").toLowerCase();
            Element mainDoc = document.getElementById("main-content");
            updateAttrDocHtml(mainDoc.getElementsByTag("a"), "href");
            updateAttrDocHtml(mainDoc.getElementsByTag("img"), "src");

            Document docCreated = new Document("");
            docCreated.append(title.html()).append(mainDoc.html());

            log.info("Document created");

            return docCreated;
        } else {
            throw new RuntimeException("HTML is empty");
        }
    }

    private void updateAttrDocHtml(Elements elements, String tag) {
        for (Element h : elements) {
            String textTag = h.attr(tag);
            if (!textTag.startsWith("https://")) {
                h.attr(tag, CONFLUENCE_PATH + textTag);
            }
        }
    }

    private String converterHTMLToMD(String html) {
        return FlexmarkHtmlConverter
                .builder()
                .build()
                .convert(html);
    }

    public SidenavDTO getJsonConfluenceFIle() {
        File folder = new File(DIRECTORY_FILE);
        File[] files = folder.listFiles();
        ArrayList<File> filteredFiles = new ArrayList<>();

        if (files != null) {
            for (File f : files) {
                mapFile.put(f.getName(), f);
                if (f.isFile() && f.getName().endsWith(TREE_VIEW_FILE)) {
                    filteredFiles.add(f);
                }
            }
        }
        try {
            return new ObjectMapper().readValue(filteredFiles.get(0), SidenavDTO.class);
        } catch (IOException e) {
            throw new RuntimeException("Error to transform data: " + e.getMessage());
        }
    }

}
