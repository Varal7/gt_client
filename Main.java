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


public class Main {
    public static TreeIoUtils.TreeSerializer myToJson(TreeContext ctx) {
        return new TreeIoUtils.TreeSerializer(ctx) {
            @Override
            protected TreeIoUtils.TreeFormatter newFormatter(TreeContext ctx, MetadataSerializers serializers, Writer writer)
                    throws Exception {
                return new MyJsonFormatter(writer, ctx);
            }
        };
  }


  public static void main(String[] args) throws Exception {
    Run.initGenerators();
    String file1 = "/Users/varal7/Projects/samples/python/whois_v0.py";
    String file2 = "/Users/varal7/Projects/samples/python/whois_v1.py";
    TreeContext srcCtx = Generators.getInstance().getTree(file1);
    ITree src = srcCtx.getRoot();
    ITree dst = Generators.getInstance().getTree(file2).getRoot();
    Matcher m = Matchers.getInstance().getMatcher(src, dst); // retrieve the default matcher
    m.match();
    ActionGenerator g = new ActionGenerator(src, dst, m.getMappings());
    g.generate();
    List<Action> actions = g.getActions(); // return the actions
    ActionsIoUtils.ActionSerializer serializer = ActionsIoUtils.toJson(srcCtx, actions, m.getMappings());
    //System.out.println("Parse");
    //System.out.println(TreeIoUtils.toJson(srcCtx).toString());
    TreeIoUtils.TreeSerializer ts = myToJson(srcCtx);
    ts.writeTo(System.out);
    //serializer.writeTo(System.out);
    //System.out.println(TreeIoUtils.toAnnotatedXml(srcCtx, true, m.getMappings()).toString());
  }

  static class MyJsonFormatter implements TreeIoUtils.TreeFormatter {
        private final JsonWriter writer;
        protected final TreeContext context;


        public MyJsonFormatter(Writer w, TreeContext ctx) {
            context = ctx;
            writer = new JsonWriter(w);
            writer.setIndent("  ");
        }

        @Override
        public void startTree(ITree t) throws IOException {
            writer.beginObject();
            writer.name("type").value(Integer.toString(t.getType()));
            if (t.hasLabel()) writer.name("label").value(t.getLabel());
            if (context.hasLabelFor(t.getType())) writer.name("typeLabel").value(context.getTypeLabel(t.getType()));
            if (ITree.NO_VALUE != t.getPos()) {
                writer.name("pos").value(Integer.toString(t.getPos()));
                writer.name("length").value(Integer.toString(t.getLength()));
                writer.name("tree").value(Integer.toString(t.getId()));
            }
        }

        @Override
        public void endTreeProlog(ITree tree) throws IOException {
            writer.name("children");
            writer.beginArray();
        }

        @Override
        public void endTree(ITree tree) throws IOException {
            writer.endArray();
            writer.endObject();
        }

        @Override
        public void startSerialization() throws IOException {
            writer.beginObject();
            writer.setIndent("  ");
        }

        @Override
        public void endProlog() throws IOException {
            writer.name("root");
        }

        @Override
        public void serializeAttribute(String key, String value) throws IOException {
            writer.name(key).value(value);
        }

        @Override
        public void stopSerialization() throws IOException {
            writer.endObject();
        }

        @Override
        public void close() throws IOException {
            writer.close();
        }
    }
}
