package cc.holstr.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.util.List;
import java.util.stream.Collectors;

public class ClassCompilationException extends Exception {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public ClassCompilationException(List<Diagnostic <? extends JavaFileObject>> diagnostics) {
        super(diagnostics.stream().map(d -> d.getMessage(null)).collect(Collectors.joining("\n")));
    }
}
