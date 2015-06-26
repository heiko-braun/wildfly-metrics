package org.wildfly.metrics;

import com.sun.japex.Constants;
import com.sun.japex.TestCase;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.List;
import java.util.Random;

/**
 * @author Heiko Braun
 * @since 20/02/15
 */
public class BatchWindow extends BenchmarkBase {

    private DateTime from;
    private DateTime to;

    static DateTimeFormatter FMT = DateTimeFormat.forPattern("MM/dd/yyyy HH:mm:ss");

    @Override
    public void prepare(TestCase testCase) {

        // window position
        to = new DateTime();
        to = to.minusHours(random(1, (TestData.NUMBER_OF_DAYS*24)-1));

        from = new DateTime(to.getMillis());

        // window size
        String sizeParam= testCase.getParam("time.window");
        Integer n = Integer.valueOf(sizeParam.substring(1, sizeParam.length()));
        switch (sizeParam.charAt(0))
        {
            case 's':
                from = from.minusSeconds(n);
                break;
            case 'm':
                from = from.minusMinutes(n);
                break;
            case 'h':
                from = from.minusHours(n);
                break;
        }

        // lower bounds
        if(from.getMillis()<testData.getFrom())
            from = new DateTime(testData.getFrom());

        System.out.println(FMT.print(from) + " > " +FMT.print(to));

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
        List<Long[]> measurements = storage.getMeasurements(TestData.METRIC_NAME, from.getMillis(), to.getMillis());
        String param = testCase.getParam("time.window");
        setLongParam("NumSamples_"+ param, measurements.size());
        setParam("Window_"+param, (FMT.print(from) + " > " +FMT.print(to)));
        setLongParam(Constants.RESULT_TIME,  System.currentTimeMillis() - start);
    }

    public static int random(int min, int max)
    {
        Random rand = new Random();
        int randomNum = rand.nextInt((max - min) + 1) + min;
        return randomNum;
    }
}
