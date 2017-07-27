package cross;

import java.util.Optional;

public interface CompilerAPI {
    void compile(String code, String options[], Optional<String> filePath);
}
