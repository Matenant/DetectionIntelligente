package model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Password {

    public static void newPassWord(String newPassWord) throws NoSuchAlgorithmException {
        
        try {

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(newPassWord.getBytes());
            writeInTheFile(encodedhash);
        }
        catch(IOException e) {
            System.out.println("Error writing to file 'passWord'");
       		e.printStackTrace();
        }
    }

    public static byte[] getPassWord(File f) throws IOException {
        return  Files.readAllBytes(f.toPath());
    }
    
    public static void writeInTheFile(byte[] data) throws IOException {

        Files.write(Paths.get("passWord"), data);
    }

}