package com.example.aiissuetriage.issue.infrastructure.vector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.aiissuetriage.issue.application.result.KnowledgeSearchResult;
import com.example.aiissuetriage.issue.infrastructure.persistence.KnowledgeDocumentEntity;
import com.example.aiissuetriage.issue.infrastructure.persistence.KnowledgeDocumentJpaRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.domain.Pageable;

class DatabaseKnowledgeSearchAdapterTest {

    private final KnowledgeDocumentJpaRepository repository = Mockito.mock(KnowledgeDocumentJpaRepository.class);
    private final DatabaseKnowledgeSearchAdapter adapter = new DatabaseKnowledgeSearchAdapter(repository);

    @Test
    void query가_비어있으면_빈_결과를_반환한다() {
        assertThat(adapter.search(" ", 5)).isEmpty();
    }

    @Test
    void limit이_0보다_작거나_같으면_빈_결과를_반환한다() {
        assertThat(adapter.search("결제", 0)).isEmpty();
    }

    @Test
    void KnowledgeDocument를_검색하고_Result로_변환한다() {
        KnowledgeDocumentEntity document = Mockito.mock(KnowledgeDocumentEntity.class);
        when(document.getId()).thenReturn(100L);
        when(document.getTitle()).thenReturn("결제 승인 후 주문 생성 실패 대응 가이드");
        when(repository.searchByTitleOrContent(eq("결제"), Mockito.any(Pageable.class)))
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
