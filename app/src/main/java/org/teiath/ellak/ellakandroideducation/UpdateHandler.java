package org.teiath.ellak.ellakandroideducation;

import android.content.Context;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;import java.lang.Float;import java.lang.String;
import java.util.Iterator;
import java.util.LinkedHashSet;

/**
 * Χειρίζεται το update της βάσης δεδομένων. Περιέχει μεθόδους μέσω των οποίων γίνεται σύνδεση σε ftp server,
 * συγκρίνεται η έκδοση της βάσης δεδομένων του server με την τοπική βάση δεδομένων και αν χρειάζεται κατεβαίνει
 * η νεότερη βάση και τα συνοδευτικά της αρχεία (εικόνες).
 */
public class UpdateHandler
{
    private static UpdateHandler ourInstance = new UpdateHandler ();

    /**
     * Επιστρέφει αναφορά στο μοναδικό αντικείμενο που δημιουργείται από την κλάση/
     * @return
     */
    public static UpdateHandler getInstance ()
    {
        return ourInstance;
    }

    /**
     * Ο κατασκευαστής τους αντικειμένου. Εδώ πραγματοποιούνται κάποιες αρχικοποιήσεις.
     */
    private UpdateHandler ()
    {

    }

    /**
     * Επιστρέφει την έκδοση της βάσης δεδομένων η οποία βρίσκεται στον server. Αν δεν υπάρχει επικοινωνία με τον server
     * επιστρέφει 0
     * @return Η έκδοση της βάσης δεδομένων στον server
     */
    public static float GetVersion(Context cont) throws IOException {
        //Variables Declaration.
        FTPClient ftp = new FTPClient();//Create FTPClient object.
        boolean eof2 = false;
        float newStr = 0;
        float ret = 0;
        int test = 0;

        try {
            ftp.connect("enterprise.cs.teiath.gr", 803);//Make the connection with the FTP Server.
            int reply = ftp.getReplyCode();//Stores the value of the FTP reply.
            if (FTPReply.isPositiveCompletion(reply)) {
                test = 0;
            }//Checking if the connection has been established.
            else {
                test = 1;
            }
            boolean login = ftp.login("ellaku", "ellaku#!");//Logging in to the FTP Server.
            if(login) {
                test = 0;
            }//Checking if the connection has been established.
            else {
                test = 1;
            }
            ftp.enterLocalPassiveMode();//Enter passive mode.

            File newVersion = new File("data/data/"+cont.getPackageName()+"/new_version.txt");//Create path to download the new_version.txt file.
            FileOutputStream streamNewVersion = new FileOutputStream(newVersion);//Create FileOutputStream variable to download file from FTP Server.
            ftp.setFileType(FTP.BINARY_FILE_TYPE);//Define the type of file to download.
            ftp.retrieveFile("version.txt", streamNewVersion);//Downloads the file witch contains the new version.

            FileReader frNewVersion = new FileReader("data/data/"+cont.getPackageName()+"/new_version.txt");//Create FileReader variable to read the content of the new_version.txt file.
            BufferedReader brNewVersion = new BufferedReader(frNewVersion);//Create BufferedReader variable.
            //Store the content of the new_version.txt file in a float variable.
            while(!eof2) {
                String line = brNewVersion.readLine();//Read file line by line until file ends.
                if (line == null) {
                    eof2 = true;
                } else {
                    newStr = Float.parseFloat(line);//Convert String to Float and store it.
                }
            }

            if(streamNewVersion != null) {
                streamNewVersion.close();//Closing streamNewVersion FileOutputStream if it has be opened.
            }
            if(brNewVersion != null) {
                brNewVersion.close();//Closing brNewVersion BufferedReader if it has be opened.
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if(ftp.isConnected()) {
                ftp.disconnect();//Disconnecting from the FTP Server if a connection has been made.
            }
        }

        //Chooses a value to return.
        if(test == 1) {
            ret = 0;
        }
        else {
            ret = newStr;
        }

        return ret;
    }

    /**
     * Κατεβάζει τη νέα έκδοση της βάσης δεδομένων και αντικαθιστά με αυτή την τρέχουσα βάση δεδομένων.
     * @return true αν η διαδικασία ολοκληρώθηκε κανονικά, false αν παρουσιάστηκε κάποιο σφάλμα.
     */
    public static boolean GetNewDB(Context cont) throws IOException {
        //Variables Declaration.
        FTPClient ftp = new FTPClient();//Create FTPClient object.
        File oldVer = new File("data/data/"+cont.getPackageName()+"/old_version.txt");//Create path to make the old_version.txt file if not exists.
        oldVer.createNewFile();//Create an old_version.txt file if not exists.
        boolean eof1 = false;
        float oldStr=0;
        float newStr=0;
        boolean ret = false;

        try {
            ftp.connect("enterprise.cs.teiath.gr", 803);//Make the connection with the FTP Server.
            int reply = ftp.getReplyCode();//Stores the value of the FTP reply.
            if (FTPReply.isPositiveCompletion(reply)) {
                ret = true;
            }//If FTP's reply positive set the proper message.
            else {
                ret = false;
            }
            boolean login = ftp.login("ellaku", "ellaku#!");//Logging in to the FTP Server.
            if(login) {
                ret = true;
            }//If login is successful set the proper message.
            else {
                ret = false;
            }
            ftp.enterLocalPassiveMode();//Enter passive mode.

            FileReader oldVersion = new FileReader("data/data/"+cont.getPackageName()+"/old_version.txt");//Create FileReader variable to read the content of the old_version.txt file.
            BufferedReader buffOldVersion = new BufferedReader(oldVersion);//Create BufferedReader variable.
            //Store the content of the old_version.txt file in a float variable.
            while(!eof1) {
                String line = buffOldVersion.readLine();//Read file line by line until file ends.
                if(line == null) {
                    eof1 = true;
                }
                else {
                    oldStr = Float.parseFloat(line);//Convert String to Float and store it.
                }
            }

            newStr = GetVersion(cont);
            //If the new version is greater than the old, then the new database will be downloaded.
            if(newStr > oldStr) {
                File dir = new File("data/data/"+cont.getPackageName()+"/databases");
                if(!dir.exists()) {
                    dir.mkdirs();//Create path to store the new database file if not exists.
                }
                File db = new File("data/data/"+cont.getPackageName()+"/databases/ellakDB.sqlite");//Create path to download the ellakDB.sqlite file.
                FileOutputStream streamDb = new FileOutputStream(db);//Create FileOutputStream variable to download file from FTP Server.
                ftp.setFileType(FTP.BINARY_FILE_TYPE);//Define the type of file to download.
                ftp.retrieveFile("EllakDB.sqlite", streamDb);//Downloads the the new database from the FTP Server.

                File oldV = new File("data/data/"+cont.getPackageName()+"/old_version.txt");
                oldV.delete();//Delete the old_version.txt file.

                File newV = new File("data/data/"+cont.getPackageName()+"/new_version.txt");
                newV.renameTo(new File("data/data/"+cont.getPackageName()+"/old_version.txt"));//Rename the new_version.txt file to old_version.txt.

                if(streamDb != null) {
                    streamDb.close();//Closing streamDb FileOutputStream if it has be opened.
                }
            }
            //If the new version is not greater than the old one, then just set the proper message.
            else {
                File newV = new File("data/data/"+cont.getPackageName()+"/new_version.txt");
                newV.delete();//Delete the new_version.txt file that just downloaded for the comparison.
            }

            if(buffOldVersion != null) {
                buffOldVersion.close();//Closing buffOldVersion BufferedReader if it has be opened.
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if(ftp.isConnected()) {
                ftp.disconnect();//Disconnecting from the FTP Server if a connection has been made.
            }
        }

        return ret;
    }

    /**
     * Κατεβάζει ένα αρχείο από τον server και το τοποθετεί στην κατάλληλη θέση. Το αρχείο, λογικά, είναι εικόνα η
     * οποία υπάρχει στην νέα βάση και δεν υπήρχε στην προηγούμενη.
     * @param Fn
     * @return
     */
    public static boolean GetFile (Context cont, String Fn) throws IOException {
        FTPClient ftp = new FTPClient();//Create FTPClient object.
        boolean ret = false;

        File img = new File("data/data/"+cont.getPackageName()+"/images/"+Fn);//Create path to download the ellakDB.sqlite file.
        FileOutputStream streamImg = new FileOutputStream(img);//Create FileOutputStream variable to download file from FTP Server.

        try {
            ftp.connect("enterprise.cs.teiath.gr", 21);//Make the connection with the FTP Server.
            int reply = ftp.getReplyCode();//Stores the value of the FTP reply.
            if (FTPReply.isPositiveCompletion(reply)) {
                ret = true;
            }//If FTP's reply positive set the proper message.
            else {
                ret = false;
            }
            boolean login = ftp.login("ellaku", "ellaku#!");//Logging in to the FTP Server.
            if (login) {
                ret = true;
            }//If login is successful set the proper message.
            else {
                ret = false;
            }
            ftp.enterLocalPassiveMode();//Enter passive mode.
            ftp.setFileType(FTP.IMAGE_FILE_TYPE);//Define the type of file to download.
            ftp.retrieveFile(Fn, streamImg);//Downloads the the new database from the FTP Server.


            if (streamImg != null) {
                streamImg.close();//Closing streamDb FileOutputStream if it has be opened.
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (ftp.isConnected()) {
                ftp.disconnect();//Disconnecting from the FTP Server if a connection has been made.
            }
        }

        return ret;
    }

    /**
     * Ελέγχει αν όλες οι εικόνες οι οποίες αναφέρονται στη βάση δεδομένων υπάρχουν στον κατάλογο της εφαρμογής. Αν
     * κάποιες λείπουν τις κατεβάζει. Επίσης ελέγχει αν υπάρχουν εικόνες οι οποίες δεν χρειάζονται πλέον και τις σβήνει.
     */
    public static void CheckImages(Context cont) throws IOException {
        File dir = new File("data/data/"+cont.getPackageName()+"/images");
        if(!dir.exists()) {
            dir.mkdirs();//Create path to store the new database file if not exists.
        }
        String[] listOfFiles = dir.list();
        LinkedHashSet<String> lhs = DBHandler.GetPhotos();
        Iterator<String> it = lhs.iterator();

        while(it.hasNext()){
            GetFile(cont, it.next());
        }
        for (int i = 0; i < listOfFiles.length; i++) {
            if(!lhs.contains(listOfFiles[i])) {
                File toDelete = new File("data/data/"+cont.getPackageName()+"/images/"+listOfFiles[i]);
                toDelete.delete();
            }
        }
    }
}
