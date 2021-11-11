package io;

import java.io.*;

/**
 * FileLogger class monitors / logs any directories / files for crud operations.
 * HYavitz - 10/21
 */
public class FileLogger {

    public static void LogFileContents(File fileWatched) throws IOException {

        File file = new File("C:" + File.separator + "YESEFT" + File.separator + "file_watcher_log.txt");

        FileInputStream fileInputStream = new FileInputStream(fileWatched);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));

        String data;
        StringBuilder stringBuilder = new StringBuilder();

        while ((data = bufferedReader.readLine()) != null) {
            stringBuilder.append(data).append("\n");
        }

        fileInputStream.close();
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
        bufferedWriter.write(stringBuilder.toString());
        bufferedWriter.close();
        stringBuilder.setLength(0);
    }
}