package br.documentation.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
public class TreeViewDTO {

    private String title;
    private String url;
    private String urlConfluence;
    private List<TreeViewDTO> children;
    private int order;

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<TreeViewDTO> getChildren() {
        return children;
    }

    public void setChildren(List<TreeViewDTO> children) {
        this.children = children;
    }

    public String getUrlConfluence() {
        return urlConfluence;
    }

    public void setUrlConfluence(String urlConfluence) {
        this.urlConfluence = urlConfluence;
    }

    @Override
    public String toString() {
        return "TreeViewDTO{" +
                "title='" + title + '\'' +
                ", url='" + url + '\'' +
                ", urlConfluence='" + urlConfluence + '\'' +
                ", children=" + children +
                ", order=" + order +
                '}';
    }
}