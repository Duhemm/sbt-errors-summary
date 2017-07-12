package cross;

public interface CompilerAPI {
    void compile(String code, String options[], xsbti.Maybe<String> filePath);
}
