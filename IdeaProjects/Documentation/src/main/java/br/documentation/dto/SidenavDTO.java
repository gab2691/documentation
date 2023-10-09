package br.documentation.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SidenavDTO {

    private List<TreeViewDTO> sidenav;

    public List<TreeViewDTO> getSidenav() {
        return sidenav;
    }

    public void setSidenav(List<TreeViewDTO> sidenav) {
        this.sidenav = sidenav;
    }
}
