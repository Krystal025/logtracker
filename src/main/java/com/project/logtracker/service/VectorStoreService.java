package com.project.logtracker.service;

import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import io.pinecone.clients.Index;
import io.pinecone.unsigned_indices_model.QueryResponseWithUnsignedIndices;
import io.pinecone.unsigned_indices_model.ScoredVectorWithUnsignedIndices;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VectorStoreService {

    private final Index index;

    public void upsert(Long issueId, Long projectId, List<Float> vector) {
        log.info("[Pinecone] upsert 시작 - issueId={}, projectId={}, vectorSize={}",
                issueId, projectId, vector != null ? vector.size() : 0);

        Struct metadata = Struct.newBuilder()
                .putFields("issueId", Value.newBuilder().setStringValue(issueId.toString()).build())
                .putFields("projectId", Value.newBuilder().setStringValue(projectId.toString()).build())
                .build();

        log.info("[Pinecone] metadata={}", metadata);

        try {
            index.upsert(issueId.toString(), vector, null, null, metadata, null);
            log.info("[Pinecone] upsert 완료 - issueId={}", issueId);
        } catch (Exception e) {
            log.error("[Pinecone] upsert 실패 - issueId={}, error={}", issueId, e.getMessage(), e);
            throw e;
        }
    }

    public List<Long> search(List<Float> vector, int topK, Long projectId) {
        Struct filter = Struct.newBuilder()
                .putFields("projectId", Value.newBuilder()
                        .setStructValue(Struct.newBuilder()
                                .putFields("$eq", Value.newBuilder()
                                        .setStringValue(projectId.toString())
                                        .build())
                                .build())
                        .build())
                .build();

        QueryResponseWithUnsignedIndices response = index.query(
                topK, vector, null, null, null, null, filter, false, true
        );

        List<Long> issueIds = new ArrayList<>();
        for (ScoredVectorWithUnsignedIndices match : response.getMatchesList()) {
            try {
                issueIds.add(Long.parseLong(match.getId()));
            } catch (NumberFormatException ignored) {
            }
        }
        return issueIds;
    }

    public void delete(Long issueId) {
        index.deleteByIds(List.of(issueId.toString()), null);
    }
}
