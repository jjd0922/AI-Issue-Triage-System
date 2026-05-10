package com.example.aiissuetriage.issue.infrastructure.vector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.aiissuetriage.issue.application.result.KnowledgeSearchResult;
import com.example.aiissuetriage.issue.infrastructure.persistence.KnowledgeDocumentEntity;
import com.example.aiissuetriage.issue.infrastructure.persistence.KnowledgeDocumentJpaRepository;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class DatabaseKnowledgeSearchAdapterTest {

    @Mock
    private KnowledgeDocumentJpaRepository repository;

    @Mock
    private KnowledgeDocumentEntity document;

    @InjectMocks
    private DatabaseKnowledgeSearchAdapter adapter;

    @Test
    @DisplayName("search 는 query 가 비어 있으면 빈 결과를 반환한다")
    void search_whenQueryIsBlank_thenReturnEmptyList() {
        assertThat(adapter.search(" ", 5)).isEmpty();
    }

    @Test
    @DisplayName("search 는 limit 이 0보다 작거나 같으면 빈 결과를 반환한다")
    void search_whenLimitIsNotPositive_thenReturnEmptyList() {
        assertThat(adapter.search("결제", 0)).isEmpty();
    }

    @Test
    @DisplayName("search 는 KnowledgeDocument 를 검색하고 result 로 변환한다")
    void search_whenDocumentsFound_thenMapToKnowledgeSearchResults() {
        when(document.getId()).thenReturn(100L);
        when(document.getTitle()).thenReturn("결제 승인 후 주문 생성 실패 대응 가이드");
        when(repository.searchByTitleOrContent(eq("결제"), any(Pageable.class)))
                .thenReturn(List.of(document));

        List<KnowledgeSearchResult> results = adapter.search(" 결제 ", 5);

        assertThat(results).containsExactly(new KnowledgeSearchResult(
                100L,
                "결제 승인 후 주문 생성 실패 대응 가이드",
                1.0
        ));
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).searchByTitleOrContent(eq("결제"), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(5);
    }
}
