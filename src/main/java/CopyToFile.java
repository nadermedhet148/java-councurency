import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CopyToFile {
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

    public  static  class  FIleWriter  {
        File file = new File("/home/nader/Documents/projects/Java/concurrency-java/src/main/java/files/all.csv");
        FileWriter  fr = null;
        public FIleWriter() throws IOException {
              fr = new FileWriter(file);
        }

        public void write(List<String>  fileData , String fileName) {
            synchronized(fileData){
                try {

                    for ( String i : fileData ){
                        fr.write(i  + "," + fileName + '\n');
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }finally{

                }
            }
        }

    }

    public static void main(String[] args) throws Exception {
        List<ReadFromFile.FileReader> threads = new ArrayList<ReadFromFile.FileReader>();
        FIleWriter writer = new FIleWriter();
        threads.add(new ReadFromFile.FileReader(new File("/home/nader/Documents/projects/Java/concurrency-java/src/main/java/files/1.csv")));
        threads.add(new ReadFromFile.FileReader(new File("/home/nader/Documents/projects/Java/concurrency-java/src/main/java/files/2.csv")));
        threads.add(new ReadFromFile.FileReader(new File("/home/nader/Documents/projects/Java/concurrency-java/src/main/java/files/3.csv")));
        threads.add(new ReadFromFile.FileReader(new File("/home/nader/Documents/projects/Java/concurrency-java/src/main/java/files/4.csv")));

        for (ReadFromFile.FileReader t : threads) {
            t.start();
        }

        List<String> allLines = new ArrayList<String>();

        for (ReadFromFile.FileReader t : threads) {
            t.join();
            writer.write(t.getLines() , t.file.getName());
        }
        try {
            writer.fr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
