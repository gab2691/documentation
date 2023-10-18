package br.documentation.service;

import br.documentation.dto.DocMD;
import br.documentation.dto.SidenavDTO;
import br.documentation.dto.TreeViewDTO;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DocumentationServiceTest {

    @Mock
    private HttpService httpService;

    @Mock
    private JGitService jGitService;

    @Mock
    private FileService fileService;

    private DocumentationService service;
    private DocumentationService serviceSpy;

    @BeforeEach
    public void setUp() {
        service = new DocumentationService(httpService, jGitService, fileService);
        serviceSpy = spy(service);
    }

    @Test
    public void TestCleanMd() {
        assertEquals("Text for test", service.cleanMd("Text {#test}for test"));
    }

    @Test
    public void TestUpdateDocdFromConfluence(){
        SidenavDTO sidenavDTO = new SidenavDTO();
        sidenavDTO.setSidenav(Collections.emptyList());

        when(fileService.transformTreeViewFileToDTO()).thenReturn(sidenavDTO);

        serviceSpy.updateDocdFromConfluence();

        verify(serviceSpy).constructAndPushDoc(sidenavDTO.getSidenav());
    }

    @Test
    public void TestConstructAndPushDocWithNullInput(){
        assertDoesNotThrow(() -> service.constructAndPushDoc(null));
    }

    @Test
    public void TestConstructAndPushDocWithNullURL(){
        // Prepare
        List<TreeViewDTO> treeViewDTOS = new ArrayList<>();
        TreeViewDTO treeViewDTO = new TreeViewDTO();
        treeViewDTO.setUrlConfluence("");
        treeViewDTOS.add(treeViewDTO);

        // Action
        serviceSpy.constructAndPushDoc(treeViewDTOS);

        // Assert
        verify(serviceSpy, never()).processingDocs(any());
    }

    @Test
    public void TestConstructAndPushDocWithValidObject(){
        // Prepare
        List<TreeViewDTO> treeViewDTOS = new ArrayList<>();
        TreeViewDTO treeViewDTO = new TreeViewDTO();
        treeViewDTO.setUrlConfluence("teste");
        treeViewDTOS.add(treeViewDTO);
        DocMD mockDocMD = new DocMD();
        mockDocMD.setDocument(new Document(""));

        // Mocking calls inside processingDocs
        when(httpService.getDocFromConfluence(anyString())).thenReturn("fakeDocFromConfluence");
        doReturn(mockDocMD).when(serviceSpy).createDocFromHTML(anyString());
        doReturn(mockDocMD.toString()).when(serviceSpy).converterHTMLToMD(anyString());
        doNothing().when(jGitService).updateLocalRepo();
        doNothing().when(fileService).saveFile(any(), any(), any());
        doReturn(null).when(jGitService).pushDoc(any());


        // Action
        serviceSpy.constructAndPushDoc(treeViewDTOS);

        // Assert
        verify(serviceSpy).processingDocs(treeViewDTO);
    }

    @Test
    public void testSavingEspecificDOC_WithNullUrl(){
        assertThrows(IllegalArgumentException.class, () -> service.savingEspecificDOC(null, "validFolder"));
    }

    @Test
    public void testSavingEspecificDOC_WithEmptyUrl(){
        assertThrows(IllegalArgumentException.class, () -> service.savingEspecificDOC("  ", "validFolder"));
    }

    @Test
    public void testeSavingEspecificDOC_withNullFolder(){
        assertThrows(IllegalArgumentException.class, () -> service.savingEspecificDOC("valid url", null));
    }

    @Test
    public void testeSavingEspecificDOC_withEmptyFolder(){
        assertThrows(IllegalArgumentException.class, () -> service.savingEspecificDOC("valid url", "  "));
    }

    @Test
    public void testSavingEspecificDOC_withValidInputs(){
        SidenavDTO mockSidenavDTO = new SidenavDTO();
        mockSidenavDTO.setSidenav(new ArrayList<>());
        when(fileService.transformTreeViewFileToDTO()).thenReturn(mockSidenavDTO);
        doNothing().when(serviceSpy).findFolderByName(anyString(), anyList(), anyString());
        doNothing().when(fileService).updateTreeView(anyList());

        serviceSpy.savingEspecificDOC("valid url", "valid folder");

        verify(fileService).transformTreeViewFileToDTO();
        verify(serviceSpy).findFolderByName(anyString(), anyList(), anyString());
    }

    @Test
    public void testfindFolderByName_withNullList(){
        assertDoesNotThrow(() -> service.findFolderByName("anyString()",null, "anyString()"));
    }

    @Test
    public void testFindFolderByName_withValidInput(){
        // Prepare
        List<TreeViewDTO> treeViewList = new ArrayList<>();
        TreeViewDTO treeViewDTO = new TreeViewDTO("title", "validPath", "urlConfluence", null, 1);
        treeViewList.add(treeViewDTO);
        treeViewDTO.setChildren(treeViewList);
        DocMD mockDocMD = new DocMD();
        mockDocMD.setDocument(new Document(""));

        // Mocking calls inside processingDocs
        when(fileService.comparePaths(treeViewDTO.getUrl(), Paths.get(treeViewDTO.getUrl()))).thenReturn(true);
        when(httpService.getDocFromConfluence(anyString())).thenReturn("fakeDocFromConfluence");
        doReturn(mockDocMD).when(serviceSpy).createDocFromHTML(anyString());
        doReturn(mockDocMD.toString()).when(serviceSpy).converterHTMLToMD(anyString());
        doNothing().when(jGitService).updateLocalRepo();
        doNothing().when(fileService).saveFile(any(), any(), any());
        doReturn(null).when(jGitService).pushDoc(any());


        // Action
        serviceSpy.findFolderByName("validPath", treeViewList, "validUrl");

        // Assert
        verify(serviceSpy).processingDocs(treeViewDTO);
    }

    @Test
    public void TestcreateDocFromHTML_WithNullInput(){
        assertThrows(IllegalArgumentException.class, () -> serviceSpy.createDocFromHTML(null));
    }

    @Test
    public void TestcreateDocFromHTML_WithValidInput(){
        // Prepare
        String mockHtml = "<html><head></head><body><div id='title-text'>Test Title</div><div id='main-content'>Test Content</div></body></html>";
        doNothing().when(serviceSpy).updateAttrDocHtml(any(), anyString());

        // Act
        DocMD docFromHTML = serviceSpy.createDocFromHTML(mockHtml);

        // Assert
        assertNotNull(docFromHTML);
        assertEquals("Test Title", docFromHTML.getTreeViewTitle());
    }

    @Test
    public void TestUpdateAttrDocHtml_withNullInput(){
        assertThrows(IllegalArgumentException.class, () -> serviceSpy.updateAttrDocHtml(null, null));
    }

    @Test
    public void TestUpdateAttrDocHtml_withValidInput(){
        // Mock the Elements and Element
        Elements elementsMock = new Elements();
        Element elementMock = mock(Element.class);
        elementsMock.add(elementMock);
        serviceSpy.setConfluencePath("https://confluence.santanderbr.corp");


        // Mock the behavior for the attr method for the elementMock
        String initialHrefValue = "/relativePath";
        when(elementMock.attr("href")).thenReturn(initialHrefValue);

        // Call the method you want to test
        serviceSpy.updateAttrDocHtml(elementsMock, "href");

        // Verify the behavior
        verify(elementMock).attr("href", "https://confluence.santanderbr.corp" + initialHrefValue);
    }

    @Test
    public void TestConverterHTMLToMD_withNullInput(){
        assertThrows(IllegalArgumentException.class, () -> serviceSpy.converterHTMLToMD(null));
    }

    @Test
    public void TestConverterHTMLToMD_withValidInput(){
        assertNotNull(serviceSpy.converterHTMLToMD("<html><head></head><body><div id='title-text'>Test Title</div><div id='main-content'>Test Content</div></body></html>"));
    }
}
