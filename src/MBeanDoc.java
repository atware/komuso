import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.RuntimeMBeanException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogSystem;

/**
 * <p>MBeanDoc</p>
 * <p>A simple JMX monitoring tool</p>
 * <p>Copyright: Copyright (c) 2007,2008 Yusuke Yamamoto</p>
 * @author Yusuke Yamamoto
 * @version 2.0
 */
public class MBeanDoc {
  MBeanServerConnection connection;
  Properties props = new Properties();
  public static void main(String[] args) throws Exception {
    new MBeanDoc().generateMBeanDoc(args);
  }

  public void generateMBeanDoc(String[] args) throws Exception {
    ResourceBundle message = ResourceBundle.getBundle("templates.Message");
    String BASE_DIR = "mbeandocroot/";
    String MBDOC_DIR = BASE_DIR + "mbeandoc/";
    new File(MBDOC_DIR).mkdirs();
    Velocity.setProperty("input.encoding", "UTF-8");
    Velocity.setProperty("resource.loader", "class");
    Velocity.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
    Velocity.setProperty("runtime.log.logsystem.class", "MBeanDoc$NullVelocityLogger");

    Velocity.init();

    // load configuration from file
    File configFile = new File(args.length > 0 ? args[0] : "komuso.properties");
    try {
      props.load(new FileInputStream(configFile));
      for(Object key : props.keySet().toArray()){
        if(((String)key).startsWith("\u00EF\u00BB\u00BF")){
          //remove strange key(workaround for java.util.Properties' possible bug)
          props.remove(key);
//          break;
        }
      }
    } catch (IOException ex) {
      System.out.println("Configuration file not found :" + configFile.getAbsoluteFile().getPath());
      System.exit( -1);
    }
    JMXConnector connector = JMXConnectorFactory.connect(new JMXServiceURL(props.getProperty("JMXServiceURL")), new java.util.HashMap(props));

    System.out.println(message.getString("connecting"));
    connection = connector.getMBeanServerConnection();
    System.out.println(message.getString("retrievingObjectNames"));
    Set set = connection.queryNames(null, null);
    Map<String, List<ObjectName>> mbInfoObjectNameMap = new HashMap<String, List<ObjectName>> ();
    Map<String, MBeanInfo> classMBinfoMap = new HashMap<String, MBeanInfo> ();
    Iterator ite = set.iterator();
    System.out.println(message.getString("constructingClassMaps"));
    while (ite.hasNext()) {
      ObjectName on = (ObjectName) ite.next();
      System.out.println("getting MBeanInfo:"+on);
      try{
        MBeanInfo mbInfo = connection.getMBeanInfo(on);
        List<ObjectName> oNameList = mbInfoObjectNameMap.get(mbInfo.getClassName());
        if (null == oNameList) {
          oNameList = new ArrayList<ObjectName> ();
        }
        oNameList.add(on);
        Collections.sort(oNameList, new Comparator<ObjectName> () {
          public int compare(ObjectName o1, ObjectName o2) {
            return o1.toString().compareTo(o2.toString());
          }
        });
        mbInfoObjectNameMap.put(mbInfo.getClassName(), oNameList);
        classMBinfoMap.put(mbInfo.getClassName(), mbInfo);
      }catch(NullPointerException ignore){
        //workarounding JBoss bug - KMS-6
      }catch(java.rmi.UnmarshalException ignore){
        //workarounding JBoss bug - KMS-6
      }
    }
    List<String> mbInfoList = new ArrayList<String> (mbInfoObjectNameMap.keySet());
    Collections.sort(mbInfoList);

    Set<String> packageSet = new HashSet<String> (mbInfoList.size());
    List<String> classList = new ArrayList(mbInfoList.size());
    Map<String, List<String>> packageClassMap = new HashMap<String, List<String>> ();
    for (String info : mbInfoList) {
      String fqcn = info;
      String packageName = getPackageName(fqcn);
      packageSet.add(packageName);
      classList.add(fqcn);
      List<String> classes = packageClassMap.get(packageName);
      if (null == classes) {
        classes = new ArrayList<String> ();
      }
      classes.add(fqcn);
      packageClassMap.put(packageName, classes);
    }
    List<String> packageList = new ArrayList<String> (packageSet);
    Collections.sort(packageList, new Comparator<String> () {
      public int compare(String o1, String o2) {
        return o1.compareTo(o2);
      }
    });

    Collections.sort(classList, new Comparator<String> () {
      public int compare(String o1, String o2) {
        return getClassName(o1).compareTo(getClassName(o2));
      }
    });

    VelocityContext context = new VelocityContext();
    context.put("util", this);
    context.put("props", props);
    context.put("message", message);

    System.out.println(message.getString("write_static_files"));
    applyAndSaveTemplate("templates/mbdoc/blank.html.vm", context, MBDOC_DIR + "blank.html");
    applyAndSaveTemplate("templates/mbdoc/refine.js.vm", context, MBDOC_DIR + "refine.js");
    applyAndSaveTemplate("templates/mbdoc/class-frame.html.vm", context, MBDOC_DIR + "class-frame.html");
    applyAndSaveTemplate("templates/mbdoc/stylesheet.css.vm", context, MBDOC_DIR + "stylesheet.css");
    applyAndSaveTemplate("templates/mbdoc/index.html.vm", context, MBDOC_DIR + "index.html");

    applyAndSaveTemplate("templates/komuso.js.vm", context, BASE_DIR + "komuso.js");
    applyAndSaveTemplate("templates/index.html.vm", context, BASE_DIR + "index.html");
    applyAndSaveTemplate("templates/stylesheet.css.vm", context, BASE_DIR + "stylesheet.css");

    System.out.println(message.getString("dump_all_classes"));
    //dump all classes
    context.put("classList", classList);
    context.put("mbInfoObjectNameMap", mbInfoObjectNameMap);
    context.put("classMBinfoMap", classMBinfoMap);
    applyAndSaveTemplate("templates/mbdoc/allclasses-frame.html.vm", context, MBDOC_DIR + "allclasses-frame.html");

    System.out.println(message.getString("dump_all_packages"));
    //dump all packages
    context.put("packageList", packageList);
    applyAndSaveTemplate("templates/mbdoc/overview-frame.html.vm", context, MBDOC_DIR + "overview-frame.html");

    System.out.println(message.getString("dump_each_packages"));
    //dump each package
    for (String thePackage : packageList) {
      System.out.print(".");
      context.put("thePackage", thePackage);
      context.put("classList", packageClassMap.get(thePackage));
      applyAndSaveTemplate("templates/mbdoc/PACKAGE.html.vm", context, MBDOC_DIR + thePackage + ".html");
    }
    System.out.println();

    System.out.println(message.getString("dump_each_classes"));
    //dump each class
    for (String fqcn : classList) {
      System.out.print(".");
      MBeanInfo mbInfo = classMBinfoMap.get(fqcn);
      context.put("mbInfo", mbInfo);
      context.put("objectNames", mbInfoObjectNameMap.get(mbInfo.getClassName()));
      context.put("package", getPackageName(fqcn));
      context.put("className", getClassName(fqcn));
      applyAndSaveTemplate("templates/mbdoc/CLASS-NAME.html.vm", context, MBDOC_DIR + mbInfo.getClassName() + ".html");
      applyAndSaveTemplate("templates/mbdoc/CLASS-NAME_objectNames.html.vm", context, MBDOC_DIR + mbInfo.getClassName() + "_objectNames.html");
    }
    System.out.println();
    System.out.println(message.getString("done"));

    connector.close();
  }

  static Map<String, Template> templateMap = new HashMap<String, Template> ();

  public String getAttribute(ObjectName objectName, MBeanAttributeInfo attribute) {
    try {
      if (attribute.isReadable()) {
        Object value = connection.getAttribute(objectName, attribute.getName());
        if (null == value) {
          return "null";
        } else {
          return escape(value.toString());
        }
      } else {
        return "not readable";
      }

    } catch (RuntimeMBeanException ex) {
      return "unavailable";
    } catch (IOException ex) {
      return null;
    } catch (ReflectionException ex) {
      return null;
    } catch (InstanceNotFoundException ex) {
      return null;
    } catch (AttributeNotFoundException ex) {
      return null;
    } catch (MBeanException ex) {
      return null;
//    } catch (IllegalArgumentException ex) {
//      System.out.println("objectName:"+objectName);
//      System.out.println("attributeName:"+attributeName);
//      return null;
    }
  }

  private static void applyAndSaveTemplate(String templateName, VelocityContext context, String filename) throws IOException {
    Template template = templateMap.get(templateName);
    if (null == template) {
      try {
        template = Velocity.getTemplate(templateName);
      } catch (Exception ex) {
        ex.printStackTrace();
        throw new IOException(ex.getMessage());
      }
      templateMap.put(templateName, template);
    }
    FileOutputStream stream = new FileOutputStream(filename);
    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stream, "UTF-8"));
    template.merge(context, writer);
    writer.close();
  }

  private final String DEPRECATION = "<h3 class=\"TypeSafeDeprecation\">Deprecation of MBeanHome and Type-Safe Interfaces</h3>";
  public String omitDeprecationMessage(String msg) {
    int index = msg.indexOf(DEPRECATION);
    if ( -1 == index) {
      return msg;
    } else {
      return msg.substring(0, index);
    }
  }

  public static String getPackageName(String fqcn) {
    return fqcn.substring(0, fqcn.lastIndexOf("."));
  }

  public static String getClassName(String fqcn) {
    return fqcn.substring(fqcn.lastIndexOf(".") + 1);
  }

  public static void save(File file, String content) throws IOException {
    save(file.getAbsolutePath(), content);
  }

  public static void save(String path, String content) throws IOException {
    FileOutputStream stream = new FileOutputStream(path);
    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stream, "UTF-8"));
    writer.write(content);
    writer.close();
  }

  public static final String escape(String str) {
    StringBuffer escaped = new StringBuffer(str.length());
    char[] array = str.toCharArray();
    for (int i = 0; i < str.length(); i++)
      switch (array[i]) {
        case '<':
          escaped.append("&lt;");
          break;
        case '>':
          escaped.append("&gt;");
          break;
        case '&':
          escaped.append("&amp;");
          break;

        case '"':
          escaped.append("&quot;");
          break;
        case '\r':
          if (array.length < (i + 1) && array[i + i] == '\n') {
            i++;
          }
          //          escaped.append("<br>");
          escaped.append("\\n");
          break;
        case '\n':
          //          escaped.append("<br>");
          escaped.append("\\n");
          break;
        default:
          escaped.append(array[i]);
          break;
      }
    return escaped.toString();
  }

  public String getAttributeName(String label) {
    return props.getProperty(label + ".attributeName");
  }

  public String getObjectName(String label) {
    return props.getProperty(label + ".objectName");
  }

  public static class NullVelocityLogger implements LogSystem {
    public NullVelocityLogger() {
    }

    public void init(RuntimeServices runtimeServices) throws Exception {
    }

    public void logVelocityMessage(int _int, String string) {
//    System.out.println(string);
    }
  }
}
