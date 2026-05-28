package com.crudproject.dto;

import org.springframework.data.domain.Page;

import java.io.Serializable;
import java.util.List;

// DTO de resposta paginada genérico.
// Encapsula o Page<T> do Spring para evitar acoplamento com a serialização interna
// do framework (que tem muitos campos extras e pode mudar entre versões).
//
// Uso típico:
//   Page<Cliente> pagina = repository.findAll(spec, pageable);
//   PageResponseDTO<ClienteResponseDTO> resposta = PageResponseDTO.from(pagina.map(mapper::toResponse));
public class PageResponseDTO<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<T> content;     // itens da página atual
    private long totalElements;  // total absoluto no banco (após filtros)
    private int totalPages;      // total de páginas
    private int page;            // índice da página atual (0-based)
    private int size;            // tamanho da página

    public PageResponseDTO() {}

    // Construtor "atalho" — converte um Page<T> do Spring para o nosso DTO.
    public static <T> PageResponseDTO<T> from(Page<T> page) {
        PageResponseDTO<T> dto = new PageResponseDTO<>();
        dto.content = page.getContent();
        dto.totalElements = page.getTotalElements();
        dto.totalPages = page.getTotalPages();
        dto.page = page.getNumber();
        dto.size = page.getSize();
        return dto;
    }

    public List<T> getContent() { return content; }
    public void setContent(List<T> content) { this.content = content; }

    public long getTotalElements() { return totalElements; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
}
