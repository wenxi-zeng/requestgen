package commonmodels;

import java.io.Serializable;

public class Request implements Serializable
{
    private Command command;

    private String filename;

    private long size;

    public Request(Command command) {
        this.command = command;
    }

    public Request(Command command, String filename) {
        this.command = command;
        this.filename = filename;
    }

    public Request(Command command, String filename, long size) {
        this.command = command;
        this.filename = filename;
        this.size = size;
    }

    public Request(String filename, long size) {
        this.filename = filename;
        this.size = size;
    }

    public Command getCommand() {
        return command;
    }

    public void setCommand(Command command) {
        this.command = command;
    }

    public Request withCommand(Command command) {
        this.command = command;
        return this;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Request withFilename(String filename) {
        this.filename = filename;
        return this;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public Request withSize(long size) {
        this.size = size;
        return this;
    }

    @Override
    public String toString() {
        return command + " " + filename + " " + size;
    }

    public static Request translate(String s) {
        String[] args = s.split(" ");
        Request request = null;
        if (args.length > 0)
            request = new Request(Command.valueOf(args[0]));
        if (args.length > 1)
            request.setFilename(args[1]);
        if (args.length > 2)
            request.setSize(Long.parseLong(args[2]));
        return request;
    }

    public enum Command{
        READ, WRITE, DELETE, CREATE_FILE, RMDIR, LS, CREATE_DIR
    }
}