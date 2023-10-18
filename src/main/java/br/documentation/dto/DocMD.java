package br.documentation.dto;

import org.jsoup.nodes.Document;

public class DocMD {

    private Document document;
    private String docName;
    private String treeViewTitle;

    public DocMD(){};

    public DocMD(Document document, String docName, String treeViewTitle){
        this.docName = docName;
        this.treeViewTitle = treeViewTitle;
        this.document = document;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public String getDocName() {
        return docName;
    }

    public void setDocName(String docName) {
        this.docName = docName;
    }

    public String getTreeViewTitle() {
        return treeViewTitle;
    }

    public void setTreeViewTitle(String treeViewTitle) {
        this.treeViewTitle = treeViewTitle;
    }
}
