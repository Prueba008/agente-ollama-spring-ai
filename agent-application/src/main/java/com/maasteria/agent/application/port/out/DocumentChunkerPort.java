package com.maasteria.agent.application.port.out;

import com.maasteria.agent.domain.model.DocumentChunk;
import java.util.List;

public interface DocumentChunkerPort {
    List<DocumentChunk> split(String content, String sourceName);
}
