package client;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class HistoryHandler {

    private File file;
    private String fileName;
    private FileWriter fileWriter;

    public HistoryHandler(String login) {
        fileName = String.format("history/history_%s.txt", login);
        file = new File(fileName);
        try {
            file.createNewFile();
            fileWriter = new FileWriter(file, true);
        } catch (IOException e) {
            System.out.println("Файл не создан");
        }
    }

    public String readHistory(int size)  {
        String read;
        String result = "";
        List <String> history = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName)))  {
           while ((read = reader.readLine()) != null) {
               if (history.size() == size) {
                   history.remove(0);
               }
               history.add(read);
           }
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (String s : history) {
            result = result + s + "\n";
        }
        return result;
    }

    public void writeToHistoryFile(String msg) throws IOException {
            fileWriter.write(msg);
    }

    public void closeFileWriter() throws IOException {
            fileWriter.close();
    }
}
