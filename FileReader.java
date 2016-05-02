import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;


public class FileReader {

	private Scanner scanner;

    public FileReader(String filePath) {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                scanner = new Scanner(file);
            }
        } catch (FileNotFoundException ioe) {
            System.err.println("Error reading " + filePath);
        }
    }
    
    public FileReader (File file){
    	try{
    		if(file.exists()) 
    			scanner = new Scanner(file);
    	} catch(FileNotFoundException ex) {
    		System.err.println("File Not Found");
    	}
    }

    public boolean isReady() {
        return scanner != null;
    }

    public boolean isEmpty() {
        return !scanner.hasNext();
    }

    public int readInt() {
        return scanner.nextInt();
    }
    
     public double readDouble() {
        return scanner.nextDouble();
    }

    public String readLine() {
        return scanner.nextLine();
    }

    public String readString() {
    	return scanner.next();
    }
    
    public void close() {
        scanner.close();
    }
    
}
