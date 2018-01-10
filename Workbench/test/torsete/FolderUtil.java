package torsete;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * En klasse der kan håndtere filer og underfoldere.
 */
public class FolderUtil {
    private static Logger log_ = LoggerFactory.getLogger(FolderUtil.class);

    /**
     * Folder til generel anvendelse af alle klasser
     */
    private File rootFolderFile;
    /**
     * Folder til eksklusiv anvendelse af den her klasse
     */
    private File folderFile;

    /**
     * Simplet navn på {@link #folderFile}
     */
    private String folderName;
    private Class callerClass;
    private String uniqueFolderNameExtension;

    private static Lock getInstanceLock_ = new ReentrantLock();

    public FolderUtil() {
        useUniqueFolderNameSupplier(() -> UUID.randomUUID().toString());
    }

    /**
     * @param uniqueFolderNameSupplier Skal levere "noget meget entydigt"
     */
    public FolderUtil useUniqueFolderNameSupplier(Supplier<Object> uniqueFolderNameSupplier) {
        uniqueFolderNameExtension = uniqueFolderNameSupplier.get().toString();
        return this;
    }

    public FolderUtil setCallerClass(Class callerClass) {
        this.callerClass = callerClass;
        return this;
    }

    public File getRootFolderFile() {
        return rootFolderFile;
    }

    public File getFolderFile() {
        return folderFile;
    }

    /**
     * @param localSubFolderName Må ikke indeholde navn på andre foldere
     * @return Fuldt kvalificeret navn på folder. Aldrig null
     */
    public String getSubFolder(String localSubFolderName) {
        File folderFile = new File(getFolderFile().getAbsolutePath() + "\\" + localSubFolderName);
        if (!folderFile.exists()) {
            if (!folderFile.mkdir()) {
                throw new RuntimeException("Kunne ikke oprette folderen " + folderFile.getAbsolutePath());
            }
        }
        return folderFile.getAbsolutePath();
    }

    public FolderUtil open() {
        folderName = callerClass.getSimpleName() + "-" + uniqueFolderNameExtension;
        rootFolderFile = createRootFolderFile();
        folderFile = createFolderFile(folderName);
        log_.info("open                 this=" + this);
        log_.info("             calllerClass=" + callerClass.getSimpleName());
        log_.info("uniqueFolderNameExtension=" + uniqueFolderNameExtension);
        log_.info("               folderName=" + folderName);
        log_.info("           rootFolderFile=" + rootFolderFile.getAbsolutePath());
        log_.info("               folderFile=" + folderFile.getAbsolutePath());
        return this;
    }

    public void close() {
        deleteInFolder(folderFile.getPath(), null);
        if (!folderFile.delete()) {
            log_.warn("Kunne ikke slette " + folderFile.getAbsolutePath() + " - eksekvering fortsætter");
        }
        deleteOtherExpiredFiles(1);
        log_.info("close                this=" + this);
        log_.info("             calllerClass=" + callerClass.getSimpleName());
        log_.info("               folderName=" + folderName);
        log_.info("           rootFolderFile=" + rootFolderFile.getAbsolutePath());
        log_.info("               folderFile=" + folderFile.getAbsolutePath());
        folderFile = null;
    }

    public boolean isOpen() {
        return folderFile != null;
    }

    private File createRootFolderFile() {
        String rootFolderName = System.getenv("TEMP") + "\\" + getClass().getSimpleName();  //
        File rootFolderFile = new File(rootFolderName);
        boolean mkdir = rootFolderFile.mkdir();
        log_.info((mkdir ? "Har oprettet " : "Har ikke oprettet ") + rootFolderFile.getAbsolutePath());

        if (!rootFolderFile.exists()) {
            throw new RuntimeException("Kan ikke få adgang til den temporære folder " + rootFolderFile.getAbsolutePath());
        }
        return rootFolderFile;
    }

    /**
     * @param localFolderName Må ikke indeholde navn på andre foldere
     * @return
     */
    private File createFolderFile(String localFolderName) {
        File folderFile = new File(getRootFolderFile() + "\\" + localFolderName);
        boolean mkdir = folderFile.mkdir();
        log_.info((mkdir ? "Har oprettet " : "Har ikke oprettet ") + folderFile.getAbsolutePath());
        if (!folderFile.exists()) {
            throw new RuntimeException("Mangler adgang til den temporære folder " + localFolderName);
        }
        return folderFile;
    }

    /**
     * Ryd også op efter andre:
     */
    private void deleteOtherExpiredFiles(int days) {
        String path = rootFolderFile.getPath();
        Date dueDate = new Date(new Date().getTime() - days * 25 * 60 * 60 * 1000);
        long dueTime = dueDate.getTime();
        log_.info("Forsøger at slette filer i " + path + " der er fra før " +
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(dueDate));
        deleteInFolder(path, f -> f.lastModified() < dueTime);
    }

    public List<File> deleteInFolder(String path, FileFilter filter) {
        List<File> res = new ArrayList<File>();
        File p = new File(path);
        Arrays.stream(p.listFiles(filter)).forEach(file -> {
            if (file.isDirectory()) {
                res.addAll(deleteInFolder(file.getPath(), filter));
            }
            if (!file.delete()) {
                res.add(file);
            }
        });
        return res;
    }

    @Override
    public String toString() {
        return super.toString() +
                "\n             calllerClass=" + callerClass.getSimpleName() +
                "\n               folderName=" + folderName +
                "\n           rootFolderFile=" + rootFolderFile.getAbsolutePath() +
                "\n               folderFile=" + folderFile.getAbsolutePath();

    }


}
