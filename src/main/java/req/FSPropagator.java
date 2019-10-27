package req;


import commonmodels.Request;
import req.rand.RandomGenerator;
import req.rand.UniformGenerator;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FSPropagator {

    private final String src;

    private final String rankFile;

    private final RequestThread.RequestGenerateThreadCallBack callBack;

    public FSPropagator(String src, String rankFile, RequestThread.RequestGenerateThreadCallBack callBack) {
        this.src = src;
        this.rankFile = rankFile;
        this.callBack = callBack;
    }

    public void start() {
        try (Writer out = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(rankFile), "UTF-8"))) {
            StaticTree tree = StaticTree.getStaticTree(src);
            RandomGenerator uniform = new UniformGenerator();
            for (int i = 0; i < tree.getFileSize(); ++i) {
                Request req=tree.fileInfo(i);
                req.setCommand(Request.Command.CREATE_FILE);

                List<Integer> order = new ArrayList<>();
                for (int j = 1; j < 10; ++j) order.add(j);
                StaticTree.plainShuffle(order, uniform);
                int find = uniform.nextInt(6) + 1; //  1~6
                List list = order.subList(0, find);
                String lstring = list.toString();

                callBack.onRequestGenerated(req, 0);
                out.write(lstring.substring(1, lstring.length() - 1) + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
