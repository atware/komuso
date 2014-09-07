import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <p>Komuso2</p>
 * <p>A simple JMX monitoring tool</p>
 * <p>Copyright: Copyright (c) 2007,2008 Yusuke Yamamoto</p>
 * <p>Copyright: Copyright (c) 2014 atWare, Inc.</p>
 * @author Yusuke Yamamoto
 * @author Koki Kawano
 * @version 2.0
 */
public class Komuso2 implements Runnable {

  private final String delimiter;

  private List<MBeanAttribute> mbeanAttributes;
  boolean connected = false;
  MBeanServerConnection connection = null;
  Logger csvLogger;
  Logger statusLogger;
  Properties props = new Properties();

  public static void main(String[] args) {
    args = args.length == 0 ? new String[] { "komuso.properties" } : args;
    for (int i = 0; i < args.length; i++) {
      new Thread(new Komuso2(args[i]), "Komuso Monitoring Thread for[" + args[i] + "]").start();
    }
  }

  public Komuso2(String propFileName) {
    File configFile = new File(propFileName);
    try {
      props.load(new FileInputStream(configFile));
    }
    catch (IOException ex) {
      System.out.println("Configuration file not found :"
          + configFile.getAbsoluteFile().getPath());
      System.exit(-1);
    }
    csvLogger = LoggerFactory.getLogger(props.getProperty("csvLogger", "csv"));
    statusLogger = LoggerFactory.getLogger(props.getProperty("statusLogger", "status"));
    delimiter = props.getProperty("delimiter", ",");
  }

  public void run() {
    int count = Integer.parseInt(props.getProperty("count"));
    int interval = Integer.parseInt(props.getProperty("interval"));
    SimpleDateFormat dateFormat = new SimpleDateFormat(props.getProperty("dateFormat"));

    mbeanAttributes = new ArrayList<MBeanAttribute>();
    for (String label : props.getProperty("mbeans").split(",")) {
      mbeanAttributes.add(
        new MBeanAttribute(label,
                props.getProperty(label + ".objectName"), 
                props.getProperty(label + ".attributeName")));
    }
    StringBuilder sb = new StringBuilder();
    // print labels
    sb.delete(0, sb.length()).append("Timestamp");
    for (int i = 0; i < mbeanAttributes.size(); i++) {
      sb.append(delimiter).append(mbeanAttributes.get(i).label);
    }
    csvLogger.info(sb.toString());

    int currentCount = 0;
    JMXConnector connector = null;
    long next = System.currentTimeMillis() + interval * 1000;
    while (-1 == count || currentCount++ < count) {
      // get MBeanServerConnection until success
      while (!connected) {
        statusLogger.info("Trying to connect to the MBean Server.");
        try {
          try {
            // ensure the connection is closed
            connector.close();
          }
          catch (Exception ignore) {
          }
          connector = JMXConnectorFactory.connect(new JMXServiceURL(props
              .getProperty("JMXServiceURL")), new java.util.HashMap(props));
          connection = connector.getMBeanServerConnection();
          statusLogger.info("Successfully connected to the MBean Server.");
          connected = true;
          statusLogger.info("Start monitoring.");
        }
        catch (IOException ioe) {
          statusLogger
              .warn("IOException throwed while connecting to the MBean server. Will retry 5 seconds later.");
          try {
            Thread.sleep(5000);
          }
          catch (InterruptedException ignore) {
          }
        }
      }
      // print values
      sb.delete(0, sb.length()).append(dateFormat.format(new Date()));
      for (MBeanAttribute mbeanAttribute : mbeanAttributes) {
        sb.append(delimiter).append(mbeanAttribute.getValue());
        if (!connected) {
          statusLogger.info("Connection lost.");
          break;
        }
      }
      csvLogger.info(sb.toString());

      // wait until next sampling
      try {
        Thread.sleep(next - System.currentTimeMillis());
      }
      catch (IllegalArgumentException inCaseTheArgumentIsNegative) {
        next = System.currentTimeMillis();
      }
      catch (InterruptedException neverHappen) {
      }
      next += interval * 1000;
    }
  }

  class MBeanAttribute {

    /*package*/String label;
    private ObjectName theBean;
    private String attributeName;

    MBeanAttribute(String label, String objectName, String attributeName) {
      this.label = label;
      try {
        theBean = new ObjectName(objectName);
      }
      catch (MalformedObjectNameException nsme) {
        throw new IllegalArgumentException("Illegal Object Name [" + objectName + "]");
      }
      this.attributeName = attributeName;
    }

    public String getValue() {
      try {
        try {
          Object value = connection.getAttribute(theBean, attributeName);
          if (value instanceof CompositeDataSupport) {
            CompositeDataSupport comp = ((CompositeDataSupport) value);
            StringBuilder sb = new StringBuilder();
            for (String key : comp.getCompositeType().keySet()) {
              if (0 < sb.length()) {
                sb.append(" ");
              }
              sb.append(key).append("=").append(comp.get(key));
            }
            return sb.toString();
          }
          return String.valueOf(value);

        }
        catch (InstanceNotFoundException e) {
          System.out.println("(1) [" + attributeName + ":" + theBean + "] " + e.getMessage());
          connection.getMBeanInfo(theBean);
        }
      }
      catch (Exception ex) {
        System.out.println("(2) [" + attributeName + ":" + theBean + "] " + ex.getMessage());
        statusLogger.error("Exception throwed while getting attrbute \""
            + attributeName + "\" of \"" + theBean + "\"", ex);
        connected = false;
      }
      return "n/a";
    }
  }
}
