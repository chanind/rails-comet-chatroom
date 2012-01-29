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
	
	protected void loadAtmosphereDotXml(InputStream stream, URLClassLoader c) throws IOException, ServletException {
		String handlerClassName = "ChatHandler";
		try {
			AtmosphereHandler handler = new ChatHandler();
			String handlerPath = "/ChatHandler";
			
			InjectorProvider.getInjector().inject(handler);
			logger.info("Installed AtmosphereHandler {} mapped to context-path: {}", handler, handlerPath);
			
			config.supportSession = true;
			
			Broadcaster b = BroadcasterFactory.getDefault().get(handlerPath);
	
	        AtmosphereHandlerWrapper wrapper = new AtmosphereHandlerWrapper(handler, b);
	        addMapping(handlerPath, wrapper);
		
		} catch (Throwable t) {
            logger.warn("unable to load AtmosphereHandler class: " + handlerClassName, t);
            throw new ServletException(t);
        }
    }
	
}