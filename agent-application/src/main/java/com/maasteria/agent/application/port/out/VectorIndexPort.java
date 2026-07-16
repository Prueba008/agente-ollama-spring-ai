package com.maasteria.agent.application.port.out;

import com.maasteria.agent.domain.model.DocumentChunk;
import java.util.List;

public interface VectorIndexPort {
    void index(List<DocumentChunk> chunks);
    List<DocumentChunk> search(String query, int topK, double similarityThreshold);
}
