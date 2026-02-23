package mg.razherana.framework.web.utils.jsp.preprocessor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;

import mg.razherana.framework.web.utils.jsp.JspFunctionBridge;

public abstract class AbstractJSPPreprocessor {

  public boolean modify(String filePath) throws IOException {
    return modify(filePath, JspFunctionBridge.getRegisteredViewNames());
  }

  public abstract boolean modify(String filePath, Set<String> functionNames) throws IOException;

  public static Path backup(String filePath) throws IOException {
    Path originalPath = Paths.get(filePath);
    Path backupPath = Paths.get(filePath + ".backup");

    if (!Files.exists(backupPath)) {
      Files.copy(originalPath, backupPath, StandardCopyOption.REPLACE_EXISTING);
    }

    return backupPath;
  }

  public static boolean restoreBackup(String backupFilePath) throws IOException {
    Path backupPath = Paths.get(backupFilePath);
    if (!Files.exists(backupPath) || !backupFilePath.endsWith(".backup")) {
      return false;
    }

    Path originalPath = Paths.get(backupFilePath.substring(0, backupFilePath.length() - ".backup".length()));
    Files.copy(backupPath, originalPath, StandardCopyOption.REPLACE_EXISTING);
    Files.delete(backupPath);
    return true;
  }
}