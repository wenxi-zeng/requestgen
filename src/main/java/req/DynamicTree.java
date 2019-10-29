package req;

import commonmodels.Request;
import req.rand.RandomGenerator;

import java.io.IOException;
import java.util.*;

public class DynamicTree extends StaticTree{
    protected DynamicTree(){
        super();
    }

    protected DynamicTree(RandomGenerator uniform){
        super(uniform);
    }

    public DynamicTree(RandomGenerator uniform,String sep){
        super(uniform,sep);
    }

    public static DynamicTree getDynamicTree(String filename) throws IOException {
        DynamicTree tree=new DynamicTree();
        new TreeParser<DynamicTree,DynamicRandTreeNode>().parse(tree,filename,true);
        return tree;
    }

    @Override
    protected RandTreeNode emptyNode(){
        return new DynamicRandTreeNode();
    }

    protected class DynamicRandTreeNode extends RandTreeNode{
        Map<String,DynamicRandTreeNode> children=null;
        boolean isLast = false;

        @Override
        public String toTreeString(boolean isLast) {
            this.isLast = isLast;
            StringBuilder prefix = new StringBuilder();
            DynamicRandTreeNode iterator = (DynamicRandTreeNode)parent;

            while (iterator != null && iterator.parent != null) {
                if (iterator.isLast)
                    prefix.insert(0, "    ");
                else
                    prefix.insert(0, "│   ");
                iterator = (DynamicRandTreeNode)iterator.parent;
            }
            if (isLast) prefix.append("└── ");
            else prefix.append("├── ");

            StringBuilder result = new StringBuilder();

            if (this.parent == null && this.name.equals(root.name)) {
                result.append("directory ").append(this.name);
            }
            else {
                result.append(prefix).append("[").append(String.format("%14s", size)).append(" ").append(String.format("%10s", System.currentTimeMillis() / 1000L)).append("]  ").append(name);
            }

            if (children != null) {
                List<DynamicRandTreeNode> nodes = new ArrayList<>(children.values());
                int i = 0;
                for (; i < nodes.size() - 1; i++) {
                    result.append('\n').append(nodes.get(i).toTreeString(false));
                }

                result.append('\n').append(nodes.get(i).toTreeString(true));
            }

            return result.toString();
        }

        DynamicRandTreeNode removeUp(){
            if(parent!=null){
                DynamicRandTreeNode p=(DynamicRandTreeNode)parent;
                p.children.remove(this.name);
                if(p.children.size()==0){
                    nonEmptyDirs.remove(parent);
                    emptyDirs.add(parent);
                }
                return p;
            }
            return null;
        }

        @Override
        protected void setParent(RandTreeNode p){
            super.setParent(p);
            DynamicRandTreeNode parent=(DynamicRandTreeNode)p;
            if(parent.children==null) parent.children=new HashMap<>();
            parent.children.put(name,this);
        }

        DynamicRandTreeNode createFile(int index){
            if(children==null){
                children=new HashMap<>();
            }
            String newName=randName();
            while(children.containsKey(newName)) newName=randName();
            DynamicRandTreeNode child=new DynamicRandTreeNode();
            child.name=newName;
            child.parent=this;
            children.put(newName,child);
            files.add(0,child);
            if(children.size()==1){
                emptyDirs.remove(index);
                nonEmptyDirs.add(this);
            }
            return child;
        }

        DynamicRandTreeNode createDir(int index){
            if(children==null){
                children=new HashMap<>();
            }
            DynamicRandTreeNode child=new DynamicRandTreeNode();
            String newName=randName()+sep;
            while(children.containsKey(newName)) newName=randName()+sep;
            child.name=newName;
            child.parent=this;
            children.put(newName,child);
            emptyDirs.add(0,child);
            if(children.size()==1){
                emptyDirs.remove(index);
                nonEmptyDirs.add(this);
            }
            return child;
        }
    }

    public int getEmptyDirSize(){
        return emptyDirs.size();
    }


    public int getAllDirSize(){
        return emptyDirs.size()+nonEmptyDirs.size();
    }

    public Request rmdir(int index){    //  index in emptyDirs
        if(index<emptyDirs.size()){
            DynamicRandTreeNode result=(DynamicRandTreeNode)emptyDirs.get(index);
            result.removeUp();
            emptyDirs.remove(index);
            Request r=new Request(Request.Command.RMDIR,result.toString());
            return r;
        }else return null;
    }

    public Request delete(int index){   //  index in files
        if(index<files.size()){
            DynamicRandTreeNode result=(DynamicRandTreeNode)files.get(index);
            result.removeUp();
            files.remove(index);
            Request r=new Request(Request.Command.DELETE,result.toString(),result.size);
            return r;
        }else return null;
    }

    private Request create(int index,boolean isDir){
        DynamicRandTreeNode parent;
        if(index>=emptyDirs.size()){
            index-=emptyDirs.size();
            if(index>=nonEmptyDirs.size()) return null;
            else parent=(DynamicRandTreeNode)nonEmptyDirs.get(index);
        }else parent=(DynamicRandTreeNode)emptyDirs.get(index);
        DynamicRandTreeNode child=isDir ? parent.createDir(index) : parent.createFile(index);
        Request r=new Request(isDir ? Request.Command.CREATE_DIR : Request.Command.CREATE_FILE,child.toString());
        return r;
    }

    public Request createFile(int index){   //  index in nonEmptyDirs, then emptyDirs
        return create(index,false);
    }

    public Request createDir(int index){
        return create(index,true);
    }
}
