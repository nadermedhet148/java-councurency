import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ReadFromFile {
    public static class FileReader extends Thread {

        private final File file;
        private List<String> lines = new ArrayList<String>();

        public FileReader(File file) {
            this.file = file;
        }

        @Override
        public void run() {
            {
                Scanner sc = null;
                try {
                    sc = new Scanner(this.file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                while (sc.hasNext())
                {
                    if(sc.hasNext()){
                        lines.add(sc.next());
                    }
                }
                sc.close();
            }
        }

        public List<String> getLines() {
            return lines;
        }
    }

        public static void main(String[] args) throws Exception {
            List<FileReader> threads = new ArrayList<FileReader>();

            threads.add(new FileReader(new File("/home/nader/Documents/projects/Java/concurrency-java/src/main/java/files/1.csv")));
            threads.add(new FileReader(new File("/home/nader/Documents/projects/Java/concurrency-java/src/main/java/files/2.csv")));
            threads.add(new FileReader(new File("/home/nader/Documents/projects/Java/concurrency-java/src/main/java/files/3.csv")));
            threads.add(new FileReader(new File("/home/nader/Documents/projects/Java/concurrency-java/src/main/java/files/4.csv")));

            for (FileReader t : threads) {
                t.start();
            }

            List<String> allLines = new ArrayList<String>();

            for (FileReader t : threads) {
                t.join();
                allLines.addAll(t.getLines());
            }
            System.out.println(allLines.size());
        }
}
