package com.example.aiissuetriage.issue.infrastructure.persistence;

import com.example.aiissuetriage.issue.application.command.IssueSearchCommand;
import com.example.aiissuetriage.issue.application.port.IssueRepositoryPort;
import com.example.aiissuetriage.issue.domain.Issue;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@ConditionalOnBean(IssueJpaRepository.class)
public class IssueRepositoryAdapter implements IssueRepositoryPort {

    private final IssueJpaRepository issueJpaRepository;

    @Override
    public Issue save(Issue issue) {
        IssueEntity entity = issue.getId() == null
                ? IssuePersistenceMapper.toNewEntity(issue)
                : issueJpaRepository.findById(issue.getId())
                        .map(existingEntity -> {
                            IssuePersistenceMapper.updateEntity(existingEntity, issue);
                            return existingEntity;
                        })
                        .orElseGet(() -> IssuePersistenceMapper.toNewEntity(issue));

        IssueEntity savedEntity = issueJpaRepository.save(entity);
        return IssuePersistenceMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Issue> findById(Long issueId) {
        return issueJpaRepository.findById(issueId)
                .map(IssuePersistenceMapper::toDomain);
    }

    @Override
    public Page<Issue> findAll(IssueSearchCommand command, Pageable pageable) {
        return issueJpaRepository.findAll(IssueSpecification.from(command), pageable)
                .map(IssuePersistenceMapper::toDomain);
    }
}
