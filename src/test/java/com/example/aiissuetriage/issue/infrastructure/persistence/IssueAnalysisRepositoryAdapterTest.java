package com.example.aiissuetriage.issue.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.aiissuetriage.issue.application.result.KnowledgeSearchResult;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IssueAnalysisRepositoryAdapterTest {

    @Mock
    private IssueAnalysisJpaRepository issueAnalysisJpaRepository;

    @Mock
    private AnalysisReferenceJpaRepository analysisReferenceJpaRepository;

    @InjectMocks
    private IssueAnalysisRepositoryAdapter adapter;

    @Test
    @DisplayName("saveReferences 는 분석 reference 를 저장한다")
    void saveReferences_whenReferencesExist_thenSaveAnalysisReferences() {
        List<KnowledgeSearchResult> references = List.of(
                new KnowledgeSearchResult(100L, "결제 역할 가이드", 0.8),
                new KnowledgeSearchResult(101L, "주문 생성 가이드", 0.7)
        );

        adapter.saveReferences(10L, references);

        ArgumentCaptor<List<AnalysisReferenceEntity>> referenceCaptor = ArgumentCaptor.captor();
        verify(analysisReferenceJpaRepository).saveAll(referenceCaptor.capture());
        assertThat(referenceCaptor.getValue()).hasSize(2);
        assertThat(referenceCaptor.getValue().get(0).getIssueAnalysisId()).isEqualTo(10L);
        assertThat(referenceCaptor.getValue().get(0).getKnowledgeDocumentId()).isEqualTo(100L);
        assertThat(referenceCaptor.getValue().get(0).getScore()).isEqualTo(0.8);
        assertThat(referenceCaptor.getValue().get(0).getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("findReferencesByAnalysisId 는 저장된 reference 를 조회한다")
    void findReferencesByAnalysisId_whenReferencesExist_thenReturnKnowledgeSearchResults() {
        List<KnowledgeSearchResult> references = List.of(
                new KnowledgeSearchResult(100L, "결제 역할 가이드", 0.8)
        );
        when(analysisReferenceJpaRepository.findKnowledgeResultsByAnalysisId(10L)).thenReturn(references);

        List<KnowledgeSearchResult> result = adapter.findReferencesByAnalysisId(10L);

        assertThat(result).containsExactlyElementsOf(references);
    }
}
