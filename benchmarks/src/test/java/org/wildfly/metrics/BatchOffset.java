package org.wildfly.metrics;

import com.sun.japex.Constants;
import com.sun.japex.JapexDriverBase;
import com.sun.japex.TestCase;
import org.joda.time.DateTime;
import org.wildfly.metrics.storage.FS;
import org.wildfly.metrics.storage.MetricStorage;

import java.io.File;
import java.util.UUID;

/**
 * @author Heiko Braun
 * @since 20/02/15
 */
public class BatchOffset extends JapexDriverBase {


    private DateTime offset;
    private String dataDir;
    private MetricStorage storage;

    @Override
    public void initializeDriver() {
        dataDir = genStorageName();
        storage = new MetricStorage(dataDir);
        System.out.println("DataDir: " + dataDir);
    }

    @Override
    public void prepare(TestCase testCase) {

        String sizeParam= testCase.getParam("batch.size");
        offset = new DateTime();
        Integer n = Integer.valueOf(sizeParam.substring(1, sizeParam.length()));
        switch (sizeParam.charAt(0))
        {
            case 's':
                offset = offset.minusSeconds(n);
                break;
            case 'm':
                offset = offset.minusMinutes(n);
                break;
            case 'h':
                offset = offset.minusHours(n);
                break;
        }

    }

    @Override
    public void run(TestCase testCase) {
        super.run(testCase);

        long start = System.currentTimeMillis();


        //setLongParam(Constants.RESULT_VALUE, data.length);
        setLongParam(Constants.RESULT_TIME, System.currentTimeMillis()-start);
    }

    private static String genStorageName() {
        String tmpdir = System.getProperty("java.io.tmpdir");
        return tmpdir + File.pathSeparator + "metrics-data-"+ UUID.randomUUID().toString();
    }

    @Override
    public void terminateDriver() {
        try {
            FS.removeDir(dataDir);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
