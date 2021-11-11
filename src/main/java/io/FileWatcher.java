package io;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

import static java.nio.file.StandardWatchEventKinds.*;

public class FileWatcher {

    private static WatchService watchService;
    private static Path path;

    boolean poll;

    public FileWatcher() throws IOException {
        watchService = FileSystems.getDefault().newWatchService();
        path = Paths.get("C:"+ File.separator + "Users" + File.separator + "Hunter");
        poll = true;
    }

    public void setWatchService() throws IOException, InterruptedException {
        path.register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);

        while (poll) {
            WatchKey watchKey = watchService.take();
            for (WatchEvent<?> event : watchKey.pollEvents()) {
                File fileToLog = new File(path.toString() + File.separator + event.context().toString());
                poll = fileToLog.createNewFile();
                FileLogger.LogFileContents(fileToLog);
            }
            poll = watchKey.reset();
        }
    }
}