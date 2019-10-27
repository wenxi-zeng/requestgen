package util;

import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class Config {
    private final static String CONFIG_PATH = "config";

    private final static String PROPERTY_RATIO_READ_WRITE = "read_write_ratio";
    private final static String PROPERTY_RATIO_REQUEST = "request_ratio";
    private final static String PROPERTY_INTER_ARRIVAL_RATE_READ_WRITE = "read_write_inter_arrival_rate";
    private final static String PROPERTY_REQUEST_DISTRIBUTION = "request_distribution";
    private final static String PROPERTY_REQUEST_ZIPF_ALPHA = "alpha";
    private final static String PROPERTY_REQUEST_NUMBER_OF_THREADS = "number_threads";
    public static final String PROPERTY_NUMBER_OF_REQUESTS = "num_of_requests";

    public final static int RATIO_KEY_READ = 0;
    public final static int RATIO_KEY_WRITE = 1;
    public final static int RATIO_KEY_DELETE = 2;
    public final static int RATIO_KEY_CREATE_FILE = 3;
    public final static int RATIO_KEY_RMDIR = 4;
    public final static int RATIO_KEY_LS = 5;
    public final static int RATIO_KEY_CREATE_DIR = 6;
    public final static String REQUEST_DISTRIBUTION_ZIPF = "zipf";
    public final static String REQUEST_DISTRIBUTION_EXP = "exp";

    private static volatile Config instance = null;

    private ResourceBundle rb;

    public Config() {
        rb = ResourcesLoader.getBundle(CONFIG_PATH);
    }

    public static Config getInstance() {
        if (instance == null) {
            synchronized(Config.class) {
                if (instance == null) {
                    instance = new Config();
                }
            }
        }

        return instance;
    }

    public double[] getReadWriteRatio() {
        String[] ratio = rb.getString(PROPERTY_RATIO_READ_WRITE).split(",");
        return Arrays.stream(ratio)
                .mapToDouble(Double::parseDouble)
                .toArray();
    }

    public double[] getRequestRatio() {
        String[] ratio = rb.getString(PROPERTY_RATIO_REQUEST).split(",");
        return Arrays.stream(ratio)
                .mapToDouble(Double::parseDouble)
                .toArray();
    }

    public double getReadWriteInterArrivalRate() {
        return Double.valueOf(rb.getString(PROPERTY_INTER_ARRIVAL_RATE_READ_WRITE));
    }

    public String getRequestDistribution() {
        return rb.getString(PROPERTY_REQUEST_DISTRIBUTION);
    }

    public double getZipfAlpha() {
        return Double.valueOf(rb.getString(PROPERTY_REQUEST_ZIPF_ALPHA));
    }

    public int getNumberOfThreads() {
        return Integer.valueOf(rb.getString(PROPERTY_REQUEST_NUMBER_OF_THREADS));
    }

    public int getNumberOfRequests() {
        return Integer.valueOf(rb.getString(PROPERTY_NUMBER_OF_REQUESTS));
    }
}
