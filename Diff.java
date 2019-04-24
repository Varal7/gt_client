import com.github.gumtreediff.client.Run;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeContext;
import com.github.gumtreediff.gen.Generators;
import com.github.gumtreediff.io.TreeIoUtils;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.matchers.Matchers;
import com.github.gumtreediff.actions.ActionGenerator;
import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.io.ActionsIoUtils;
import com.github.gumtreediff.tree.TreeContext.MetadataSerializers;

import com.google.gson.stream.JsonWriter;

import java.io.*;
import java.util.List;


public class Diff {
    public static void diff(String file1, String file2, Writer writer) throws Exception {
        Run.initGenerators();
        TreeContext srcCtx = Generators.getInstance().getTree(file1);
        ITree src = srcCtx.getRoot();
        ITree dst = Generators.getInstance().getTree(file2).getRoot();
        Matcher m = Matchers.getInstance().getMatcher(src, dst); // retrieve the default matcher
        m.match();
        ActionGenerator g = new ActionGenerator(src, dst, m.getMappings());
        g.generate();
        List<Action> actions = g.getActions(); // return the actions
        ActionsIoUtils.toJson(srcCtx, actions, m.getMappings()).writeTo(writer);
    }
    public static void main(String[] args) throws Exception {
        String before = args[0];
        String after = args[1];
        String output_dir = args[2];
        File dir = new File(before);
        File[] directoryListing = dir.listFiles();
        int count = 0;
        if (directoryListing != null) {
            for (File child : directoryListing) {
                try {
                    String name = (child.getName().toString());
                    String jsonName = (name.substring(0, name.lastIndexOf("."))) + ".json";
                    try(Writer writer = new BufferedWriter(new OutputStreamWriter( new FileOutputStream(output_dir + "/" + jsonName), "utf-8"))) {
                        diff(before + "/" + name, after + "/" + name, writer);
                    }
                }
                catch (Exception e) {
                    System.out.println(child.getPath());
                    System.out.println(e);
                }
                if (count % 10 == 0) {
                    System.out.println(count);
                }
                count += 1;
            }
        } else {
            System.out.println("Not a directory");
        }

    }
}
