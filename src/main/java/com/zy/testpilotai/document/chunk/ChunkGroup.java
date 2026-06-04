package com.zy.testpilotai.document.chunk;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class ChunkGroup {
    /**
     * 父级切块
     */
    private ParentTextChunk parent;

    /**
     * 子级切块
     */
    private List<ChildTextChunk> children;
}