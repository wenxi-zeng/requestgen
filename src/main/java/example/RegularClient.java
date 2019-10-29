package example;

import commonmodels.Request;
import req.FSPropagator;
import req.RequestService;
import req.RequestThread;
import req.gen.ClientRequestGenerator;
import req.gen.RequestGenerator;
import req.gen.SequentialRequestGenerator;
import req.gen.SmartRequestGenerator;
import util.Config;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class RegularClient implements RequestThread.RequestGenerateThreadCallBack {

    public static void main(String[] args) {
        RegularClient regularClient = new RegularClient();

        try {
            if (args.length == 0) {
                System.out.println("Usage: RegularClient -r <filename> [number of requests]\n" +
                        "Usage: RegularClient -f <file in> <file out> [number of requests]\n" +
                        "Usage: RegularClient -s <filename>\n");
            } else if (args[0].equals("-r")) {
                regularClient.launchRequestGenerator(args);
            } else if (args[0].equals("-m")) {
                regularClient.launchSmartRequestGenerator(args);
            } else if (args[0].equals("-f")) {
                regularClient.launchFileRequestGenerator(args);
            } else if (args[0].equals("-s")) {
                regularClient.launchSequentialRequestGenerator(args);
            } else if (args[0].equals("-p")) {
                regularClient.launchFilePropagator(args);
            } else {
                System.out.println("Usage: RegularClient -r <filename> [number of requests]\n" +
                        "Usage: RegularClient -f <file in> <file out> [number of requests]\n" +
                        "Usage: RegularClient -s <filename>\n");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void launchRequestGenerator(String[] args) throws IOException {
        if (args.length >= 2) {
            RequestGenerator generator = new ClientRequestGenerator(args[1], args.length >= 3 ? args[2] : null);
            int numOfRequests = Config.getInstance().getNumberOfRequests();
            if (args.length >= 3) numOfRequests = Integer.parseInt(args[2]);
            generateRequest(generator, numOfRequests);
        }
        else {
            System.out.println ("Usage: RegularClient -r <filename> [rank filename]");
        }
    }

    private void launchSmartRequestGenerator(String[] args) throws IOException {
        if (args.length >= 2) {
            RequestGenerator generator = new SmartRequestGenerator(args[1],
                    args.length >= 3 ? args[2] : null,
                    args.length >= 4 ? args[3] : null);
            int numOfRequests = Config.getInstance().getNumberOfRequests();
            generateRequest(generator, numOfRequests);
        }
        else {
            System.out.println ("Usage: RegularClient -m <static tree file> [dynamic tree file]");
        }
    }

    private void launchFileRequestGenerator(String[] args) {
        if (args.length >= 3) {
            int numOfRequests = Config.getInstance().getNumberOfRequests();
            if (args.length >= 4) numOfRequests = Integer.parseInt(args[3]);
            generateRequestFile(args[1], args[2], numOfRequests);
        }
        else {
            System.out.println ("Usage: RegularClient -f <file in> <file out> [number of requests]");
        }
    }

    private void launchSequentialRequestGenerator(String[] args) throws IOException {
        if (args.length >= 2) {
            RequestGenerator generator = new SequentialRequestGenerator(
                    Config.getInstance().getNumberOfThreads(),
                    Config.getInstance().getNumberOfRequests(),
                    args[1]
            );
            int numOfRequests = Config.getInstance().getNumberOfRequests();
            generateRequest(generator, numOfRequests);
        }
        else {
            System.out.println ("Usage: RegularClient -s <filename>");
        }
    }


    private void launchFilePropagator(String[] args) {
        if (args.length >= 3) {
            propagateFiles(args[1], args[2]);
        }
        else {
            System.out.println ("Usage: RegularClient -p <src file> <output rank file>");
        }
    }

    private void generateRequest(RequestGenerator generator, int numOfRequests) {
        int numThreads = Config.getInstance().getNumberOfThreads();
        RequestService service = new RequestService(numThreads,
                Config.getInstance().getReadWriteInterArrivalRate(),
                numOfRequests,
                generator,
                this);

        service.start();
        ((SmartRequestGenerator)generator).saveDynamicTreeToFile("dynamicTree.txt");
        System.exit(0);
    }

    private void generateRequestFile(String filename, String fileOut, int numOfRequests) {
        try {
            FileWriter w = new FileWriter(fileOut);
            BufferedWriter bw = new BufferedWriter(w);
            PrintWriter wr = new PrintWriter(bw, true);

            RequestGenerator generator = new ClientRequestGenerator(filename);
            int numThreads = Config.getInstance().getNumberOfThreads();
            RequestService service = new RequestService(1,
                    1,
                    numOfRequests * numThreads,
                    generator,
                    (request, threadId) -> wr.println(request.toString()));

            service.start();
            wr.close();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.exit(0);
    }

    private void propagateFiles(String src, String rankFile) {
        FSPropagator propagator = new FSPropagator(src, rankFile, this);
        propagator.start();
        System.exit(0);
    }

    @Override
    public void onRequestGenerated(Request request, int threadId) {
        System.out.println("thread[" + threadId + "]: " +  request);
    }
}
