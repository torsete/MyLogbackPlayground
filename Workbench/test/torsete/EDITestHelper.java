package torsete;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Hjælpemetoder til brug ifm. test af EDI.
 *
 * @author TJT
 */
public class EDITestHelper {
    private static Logger log_ = LoggerFactory.getLogger(EDITestHelper.class);

    private static final boolean DEBUG = true;

    public boolean argProcess_ = true;
    private FolderUtil folderUtil;

    private static HashMap<String, EDITestHelper> instances = new HashMap<>();

    private static Lock getInstanceLock_ = new ReentrantLock();

    public EDITestHelper(Class callerClass) {
        folderUtil = new FolderUtil()
                .setCallerClass(callerClass);
        log_.info("instantieret");
    }


    public static EDITestHelper getInstance(Class callerClass) {
        getInstanceLock_.lock();
        Exception catchedException = null;
        EDITestHelper instance = null;
        try {
            String key = callerClass.getName();
            if (instances.get(key) == null) {
                instance = new EDITestHelper(callerClass);
                instances.put(key, instance);
                log_.info("getInstanceUnderLock 1 callerClass=" + callerClass.getName() + " instance=" + instance);
                log_.info("instances.size()=" + instances.size());
            }
            instance = instances.get(key);
            log_.info("getInstanceUnderLock 2 callerClass=" + callerClass.getName() + " instance=" + instance);

        } catch (Exception e) {
            catchedException = e;
        } finally {
            log_.info("getInstance instance=" + instance);
            getInstanceLock_.unlock();
        }
        if (catchedException != null) {
            throw new RuntimeException(catchedException);
        }
        return instance;
    }

    public static HashMap<String, EDITestHelper> getInstances() {
        getInstanceLock_.lock();
        HashMap<String, EDITestHelper> instances = null;
        Exception catchedException = null;
        EDITestHelper instance = null;
        try {
            instances = EDITestHelper.instances;
        } catch (Exception e) {
            catchedException = e;
        } finally {
            log_.info("getInstances instances=" + instances.size());
            getInstanceLock_.unlock();
        }
        if (catchedException != null) {
            throw new RuntimeException(catchedException);
        }
        ;
        return instances;
    }

    public String getDIR() {
        String value = folderUtil.getSubFolder("dir");
        log_.info("DIR= " + value);
        return value;
    }

    public String getINDIR() {
        String value = folderUtil.getSubFolder("in");
        log_.info("INDIR= " + value);
        return value;
    }

    public String getBACKUPDIR() {
        String value = folderUtil.getSubFolder("backup");
        log_.info("BACKUPDIR= " + value);
        return value;
    }

    public String getOUTDIR() {
        String value = folderUtil.getSubFolder("out");
        log_.info("OUTDIR= " + value);
        return value;
    }

    public String getLOGS() {
        String value = folderUtil.getSubFolder("logs");
        log_.info("LOGS= " + value);
        return value;
    }

    public String establishEdiTestDirectory() {
        log_.info("establishEdiTestDirectory");
        if (folderUtil.isOpen()) {
            folderUtil.close();
        }
        folderUtil.open();
        getINDIR();
        getOUTDIR();
        getBACKUPDIR();
        getDIR();
        getLOGS();
        return folderUtil.getFolderFile().getAbsolutePath();
    }

    public FolderUtil getFolderUtil() {
        return folderUtil;
    }

    /**
     * Sletter alle EDI-testmeddelelser der i og under mappen med EDI-testmeddelelser.
     */
    public void flushEdiTestDirectory() {
        log_.info("flushEdiTestDirectory");
        folderUtil.close();
    }


}
