package br.documentation.service;

import br.documentation.dto.DocMD;
import br.documentation.dto.TreeViewDTO;
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class DocumentationService {

    public static final String BASE_URL = "https://";
    private final HttpService httpService;
    private final JGitService jGitService;
    private final FileService fileService;

    @Autowired
    public DocumentationService(HttpService httpService, JGitService jGitService, FileService fileService) {
        this.httpService = httpService;
        this.jGitService = jGitService;
        this.fileService = fileService;
    }

    @Value("${confluence.path}")
    private String confluencePath;

    /**
     * Este método serve de Interface para inicio do proessamento
     */
    public void updateDocdFromConfluence() {
        constructAndPushDoc(fileService.transformTreeViewFileToDTO().getSidenav());
    }

    /**
     * Método responsavel por Orquetrar o processamentos dos documentos vindo do File
     *
     * <p>Este método cuida de todo o fluxo de processamento dos documentos que estão no arquivo do TreeView,
     * de forma recursiva ele processa todos os itens, alcaçando o final de cada item pai do arquivo</p>
     * @param sidenav
     */
    protected void constructAndPushDoc(List<TreeViewDTO> sidenav) {
        if (sidenav == null) {
            return;
        }
        for (TreeViewDTO n : sidenav) {
            if (!n.getUrlConfluence().isEmpty() && !n.getUrlConfluence().isBlank()) {
                processingDocs(n);
            }
            constructAndPushDoc(n.getChildren());
        }
    }

    /**
     * Método responsavel por orquestrar o fluxo de processamento do documento.
     *
     * <p>Este método invoca o metodo responsavél por transforma o arquivo em Objeto, apos isso é localizado a pasta certa onde o documento vai ser salvo e então o
     * aquivo TreeView é salvo novamente atulizado</p>
     *
     * @param url da nova documentação
     * @param folder pasta especifica onde a documentação vai ser salva
     */
    public void savingEspecificDOC(String url, String folder){
        if (url == null || url.trim().isEmpty() || folder == null || folder.trim().isEmpty()){
            log.warn("Illegal arguments url {} or folder {] are invalid", url, folder);
            throw new IllegalArgumentException("Invalid Arguments");
        }
        List<TreeViewDTO> sidenav = fileService.transformTreeViewFileToDTO().getSidenav();
        findFolderByName(folder, sidenav, url);
        fileService.updateTreeView(sidenav);
    }


    /**
     * Método responsavel por achar a pasta correta para salvar o documento.
     *
     * <p>Este método percorre todo o objeto TreeView de forma recurisa, quando a pasta certa é encontrada é feito o processamento da documentação, feita a criação
     * de um novo Objeto TreeViewDTO e inserido dentro da lista</p>
     *
     * @param url da nova documentação
     * @param folder pasta especifica onde a documentação vai ser salva
     * @param sidenav Lista de Objeto para ser percorrido
     */
    protected void findFolderByName(String folder, List<TreeViewDTO> sidenav, String url){
        if (sidenav == null){
            return;
        }

        for (TreeViewDTO n: sidenav){
            if (fileService.comparePaths(n.getUrl(), Paths.get(folder))){
                DocMD docMD = processingDocs(n);

                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(n.getUrl()).append("/").append(docMD.getDocName());

                TreeViewDTO treeViewDTO = new TreeViewDTO();
                treeViewDTO.setTitle(docMD.getTreeViewTitle());
                treeViewDTO.setUrl(stringBuilder.toString());
                treeViewDTO.setUrlConfluence(url);
                treeViewDTO.setOrder(n.getChildren() != null ? n.getChildren().size() + 1 : 1);

                n.getChildren().add(treeViewDTO);

                return;
            }
            findFolderByName(folder, n.getChildren(), url);
        }
    }

    /**
     * Método responsavel por processar os documentos da lista de Objetos.
     *
     * <p>Este método cuida de todo o fluxo de processamento dos documentos que estão no arquivo do TreeView,
     * de forma recursiva ele processa todos os itens, alcaçando o final de cada item pai do arquivo</p>
     *
     * @param sidenav
     */
    protected DocMD processingDocs(TreeViewDTO sidenav) {
        String docFromConfluence = httpService.getDocFromConfluence(sidenav.getUrlConfluence());
        DocMD docFromHTML = createDocFromHTML(docFromConfluence);
        String docMd = converterHTMLToMD(docFromHTML.getDocument().html());
        jGitService.updateLocalRepo();
        fileService.saveFile(cleanMd(docMd), sidenav.getUrl(), docFromHTML.getDocName());
        jGitService.pushDoc(docFromHTML.getTreeViewTitle());

        return docFromHTML;
    }

    /**
     * Limpa textos indesejados dentro do MD
     *
     * <p>Esse metodo limpa os dados que foram transformados de String para MD, retirando um padrão especifico atraves de um regex</p>
     * @param data String contendo o conteúdo em formato MD.
     * @return String MD após a remoção de padrões indesejados.
     */
    protected String cleanMd(String data) {
        Matcher matcher = Pattern.compile("\\{#[^{}]*\\}").matcher(data);
        StringBuffer stringBuffer = new StringBuffer();

        while (matcher.find()) {
            matcher.appendReplacement(stringBuffer, "");
        }
        return matcher.appendTail(stringBuffer).toString();
    }

    /**
     * Cria um {@link Document} a partir de uma string HTML.
     *
     * <p>Este método primeiro verifica se o HTML fornecido não é nulo ou vazio.
     * Em seguida, ele faz o parsing do HTML para extrair o título e o conteúdo principal.
     * O método também atualiza os atributos de certos elementos HTML para garantir que os links e imagens
     * apontem para os URLs corretos.</p>
     *
     * @param html A string HTML a ser convertida em um {@link Document}.
     * @return Um {@link Document} contendo o título e o conteúdo principal extraídos do HTML.
     * @throws IllegalArgumentException Se o HTML fornecido for nulo, vazio ou apenas espaços em branco.
     */
    protected DocMD createDocFromHTML(String html) {
        if (html != null && !html.isBlank()) {
            log.info("Creating DOC");

            Document document = Jsoup.parse(html);
            Element title = document.getElementById("title-text");

            log.info("Extracting title-page " + title.text());

            Element mainDoc = document.getElementById("main-content");
            updateAttrDocHtml(mainDoc.getElementsByTag("a"), "href");
            updateAttrDocHtml(mainDoc.getElementsByTag("img"), "src");

            Document docCreated = new Document("");
            docCreated.append(title.html()).append(mainDoc.html());

            log.info("Document created");

            return new DocMD(docCreated, treatingDocName(title.text()), title.text());
        } else {
            throw new IllegalArgumentException("HTML is empty");
        }
    }

    /**
     * Trata o título da página removendo caracteres indesejados.
     *
     * <p>Este método retira caracteres especiais e espaços do título da página,
     * convertendo-o para um formato mais adequado para ser usado como nome de arquivo ou URL.</p>
     *
     * @param data Título original da página que está sendo processada.
     * @return Título após o tratamento.
     */
    protected String treatingDocName(String data){
        return data.replaceAll("[^\\p{ASCII}]", "").replaceAll(" ", "-").toLowerCase();
    }

    /**
     * Trata as tags do documento para garantir URLs corretas.
     *
     * <p>Este método primeiro verifica se os elementos recebido são validos, depois verifica e atualiza os atributos das tags fornecidas. Se a URL do atributo não começar com "https://",
     * o método adiciona o caminho base do Confluence à URL.</p>
     *
     * @param elements Elementos do documento a serem tratados.
     * @param tag Tipo de atributo (por exemplo, href ou src) que será verificado e possivelmente atualizado.
     * @throws IllegalArgumentException Se o Elements fornecido for nulo, vazio ou apenas espaços em branco.
     */
    protected void updateAttrDocHtml(Elements elements, String tag) {
        if (elements == null || elements.isEmpty() || tag == null || tag.isEmpty()){
            log.warn("Elements {} or tag {} are invalids for iteration", elements, tag);
            throw new IllegalArgumentException("Elements or tag is invalid for iteration");
        }
        for (Element h : elements) {
            String textTag = h.attr(tag);
            if (!textTag.startsWith(BASE_URL)) {
                h.attr(tag, confluencePath + textTag);
                log.info("Update URL for tag {}: {}", tag, h.attr(tag));
            }
        }
    }

    /**
     * Este método converte um String HTML em um MD
     *
     * <p>Este metodo verifica primeiro se a String HTML é valida, sendo válida ele converte a String para o formato MD utilizando a biblioteca FlexmarkHtmlConverter</p>
     *
     * @param html de entrada para ser convertido em MD
     * @return o documento em formato MD
     */
    protected String converterHTMLToMD(String html) {
        if (html == null || html.isEmpty()){
            log.error("HTML invalid for conversion");
            throw new IllegalArgumentException("HTML is invalid");
        }

        log.info("Converting HTML into MD file");
        return FlexmarkHtmlConverter
                .builder()
                .build()
                .convert(html);
    }

    public void setConfluencePath(String confluencePath) {
        this.confluencePath = confluencePath;
    }
}
