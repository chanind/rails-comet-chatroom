package atmosphere;

import javax.servlet.ServletConfig;
import org.atmosphere.cpr.AtmosphereHandler;
import org.atmosphere.cpr.AtmosphereServlet;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class RailsAtmosphereServlet extends AtmosphereServlet{
	
	private static final Logger logger = LoggerFactory.getLogger(RailsAtmosphereServlet.class);
	
	protected void configureWebDotXmlAtmosphereHandler(ServletConfig sc) {
        String s = sc.getInitParameter("atmosphere.ChatHandler");
        if (s != null) {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            try {

                String mapping = sc.getInitParameter("/ChatHandler");
                if (mapping == null) {
                    mapping = "/*";
                }
                addAtmosphereHandler(mapping, (AtmosphereHandler<?, ?>) cl.loadClass(s).newInstance());
            } catch (Exception ex) {
                logger.warn("Unable to load WebSocketHandle instance", ex);
            }
        }
    }
	
}