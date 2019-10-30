package req.gen;

import commonmodels.Request;
import req.DynamicTree;
import req.StaticTree;
import util.AutoLock;
import util.Config;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class SmartRequestGenerator extends RequestGenerator{

    private final StaticTree sTree;

    private final DynamicTree dTree;

    private ReadWriteLock lock;

    private float dynamicInsertRatio = 1;

    public SmartRequestGenerator(String staticFile) throws IOException {
        super();
        this.sTree = StaticTree.getStaticTree(staticFile);
        this.generator.setUpper(sTree.getFileSize() - 1);
        this.dTree = null;
    }

    public SmartRequestGenerator(String staticFile, String dynamicFile) throws IOException {
        super();
        this.sTree = StaticTree.getStaticTree(staticFile);
        this.generator.setUpper(sTree.getFileSize() - 1);
        this.dTree = dynamicFile == null ? null : DynamicTree.getDynamicTree(dynamicFile);
        this.lock = dynamicFile == null ? null : new ReentrantReadWriteLock();
    }

    public SmartRequestGenerator(String staticFile, String dynamicFile, String rankFile) throws IOException {
        this(staticFile, dynamicFile);
        if (rankFile != null)
            this.sTree.shuffleFilesUneven(rankFile);
    }

    private Request next(Request.Command type) {
        int sTreeFiles = sTree.getFileSize();
        int sTreeDirs = sTree.getNonEmptyDirSize();
        int sTreeHeadFiles = (int) (sTreeFiles * dynamicInsertRatio);
        int sTreeHeadDirs = (int) (sTreeDirs * dynamicInsertRatio);
        Request request = null;

        if (dTree == null) {
            if (type == Request.Command.LS)
                request = sTree.ls(generator.nextInt(sTreeDirs - 1));
            else if (type == Request.Command.READ || type == Request.Command.WRITE) {
                request = sTree.fileInfo(generator.nextInt(sTreeFiles - 1));
                request.setCommand(type);
            }
        } else {
            try (AutoLock auto = AutoLock.lock(lock.readLock())) {
                if (type == Request.Command.CREATE_DIR || type == Request.Command.CREATE_FILE
                        || type == Request.Command.DELETE || type == Request.Command.RMDIR) {
                    lock.readLock().unlock();
                    lock.writeLock().lock();
                    if (type == Request.Command.CREATE_DIR) {
                        request = dTree.createDir(generator.nextInt(dTree.getAllDirSize() - 1));
                    } else if (type == Request.Command.CREATE_FILE) {
                        request = dTree.createFile(generator.nextInt(dTree.getAllDirSize() - 1));
                    } else if (type == Request.Command.DELETE) {
                        if (dTree.getFileSize() == 0) {
                            lock.writeLock().unlock();
                            return request;
                        }
                        request = dTree.delete(generator.nextInt(dTree.getFileSize() - 1));
                    } else if (type == Request.Command.RMDIR) {
                        if (dTree.getEmptyDirSize() == 0) {
                            lock.writeLock().unlock();
                            return request;
                        }
                        request = dTree.rmdir(generator.nextInt(dTree.getEmptyDirSize() - 1));
                    }
                    lock.readLock().lock();
                    lock.writeLock().unlock();
                } else {
                    if (type == Request.Command.LS) {
                        int n = generator.nextInt(sTreeDirs + dTree.getNonEmptyDirSize() - 1);
                        if (n < sTreeHeadDirs)
                            request = sTree.ls(n);
                        else {
                            n -= sTreeHeadDirs;
                            if (n < dTree.getNonEmptyDirSize())
                                request = dTree.ls(n);
                            else {
                                n = n - dTree.getNonEmptyDirSize() + sTreeHeadDirs;
                                request = sTree.ls(n);
                            }
                        }
                    } else {
                        int n = generator.nextInt(sTreeFiles + dTree.getFileSize() - 1);
                        StaticTree t = sTree;
                        if (n >= sTreeHeadFiles) {
                            n -= sTreeHeadFiles;
                            if (n < dTree.getFileSize())
                                t = dTree;
                            else
                                n = n - dTree.getFileSize() + sTreeHeadFiles;
                        }
                        if (type == Request.Command.READ || type == Request.Command.WRITE) {
                            request = t.fileInfo(n);
                            request.setCommand(type);
                        }
                    }
                }
            }
        }

        return request;
    }

    @Override
    public Request next(int threadId) {
        Request request = null;
        while (request == null) {
            Request header = headerGenerator.next();
            request = next(header.getCommand());
        }
        return request;
    }

    @Override
    public Map<Request, Double> loadRequestRatio() {
        double[] ratio = Config.getInstance().getRequestRatio();
        Map<Request, Double> map = new HashMap<>();
        map.put(new Request(Request.Command.READ), ratio[Config.RATIO_KEY_READ]);
        map.put(new Request(Request.Command.WRITE), ratio[Config.RATIO_KEY_WRITE]);
        map.put(new Request(Request.Command.DELETE), ratio[Config.RATIO_KEY_DELETE]);
        map.put(new Request(Request.Command.CREATE_FILE), ratio[Config.RATIO_KEY_CREATE_FILE]);
        map.put(new Request(Request.Command.RMDIR), ratio[Config.RATIO_KEY_RMDIR]);
        map.put(new Request(Request.Command.LS), ratio[Config.RATIO_KEY_LS]);
        map.put(new Request(Request.Command.CREATE_DIR), ratio[Config.RATIO_KEY_CREATE_DIR]);
        return map;
    }

    public void saveDynamicTreeToFile(String filename) {
        if (dTree ==  null) {
            System.out.println("Failed to save. No dynamic tree file provided");
            return;
        }

        try {
            FileWriter w = new FileWriter(filename);
            BufferedWriter bw = new BufferedWriter(w);
            PrintWriter wr = new PrintWriter(bw, true);
            wr.print(dTree.getRoot() == null ? "null" : dTree.getRoot().toTreeString(false));
            wr.close();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
