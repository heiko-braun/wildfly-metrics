package org.wildfly.metrics;

import com.sun.japex.Constants;
import com.sun.japex.TestCase;
import org.joda.time.DateTime;

import java.util.List;

/**
 * @author Heiko Braun
 * @since 20/02/15
 */
public class BatchOffset extends BenchmarkBase {


    private DateTime offset;
    private long now;

    @Override
    public void prepare(TestCase testCase) {

        String sizeParam= testCase.getParam("time.window");
        offset = new DateTime();
        now = System.currentTimeMillis();
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

        // the database needs some time to normalise ...
        try {
            System.out.println("Linger ...");
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Begin test executions ...");

    }

    @Override
    public void run(TestCase testCase) {
        super.run(testCase);

        long start = System.currentTimeMillis();
        List<Long[]> measurements = storage.getMeasurements(TestData.METRIC_NAME, offset.getMillis(), now);
        setLongParam("NumSamples_"+testCase.getParam("time.window"), measurements.size());
        setLongParam(Constants.RESULT_TIME,  System.currentTimeMillis() - start);
    }

}
