package org.weld.loguploader.filemanagement;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Verify path and open log file
 *
 * @author <a href="mailto:manovotn@redhat.com">Matej Novotny</a>
 */
public class FileManager {

    private File f;
    private String pathToLog;
    private BufferedReader reader;

    public FileManager(String path) {
        pathToLog = path;
    }

    public void closeResource() {
        try {
            reader.close();
        } catch (IOException ex) {
            Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private boolean fileExists() {
        f = new File(pathToLog);
        return (f.exists() && f.canRead());
    }

    public BufferedReader getReader() throws IOException {
        if (fileExists()) {
            FileReader fr = null;
            fr = new FileReader(f);
            reader = new BufferedReader(fr);
            return reader;
        } else {
            throw new IllegalArgumentException("Path to log file is probably incorrect: value=" + pathToLog);
        }
    }
}
