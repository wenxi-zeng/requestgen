package req.gen;

import commonmodels.Request;
import req.StaticTree;
import util.Config;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ClientRequestGenerator extends RequestGenerator {

    private final StaticTree tree;

    public ClientRequestGenerator(String filename) throws IOException {
        super();
        this.tree = StaticTree.getStaticTree(filename);
        this.generator.setUpper(tree.getFileSize() - 1);
    }

    public ClientRequestGenerator(String filename, String rankFile) throws IOException {
        this(filename);
        if (rankFile != null)
            this.tree.shuffleFilesUneven(rankFile);
    }

    @Override
    public Request next(int threadId) {
        StaticTree.RandTreeNode file = tree.getFiles().get(generator.nextInt());
        Request request = headerGenerator.next();
        return request
                .withFilename(file.toString())
                .withSize(file.getSize());
    }

    @Override
    public Map<Request, Double> loadRequestRatio() {
        double[] ratio = Config.getInstance().getReadWriteRatio();
        Map<Request, Double> map = new HashMap<>();
        map.put(new Request(Request.Command.READ), ratio[Config.RATIO_KEY_READ]);
        map.put(new Request(Request.Command.WRITE), ratio[Config.RATIO_KEY_WRITE]);
        return map;
    }
}
