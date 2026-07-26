package persistence;

import java.io.IOException;

public interface GraphRepository {
    
    void save(GraphData data, String path) throws IOException;
    GraphData load(String path) throws IOException;
}
