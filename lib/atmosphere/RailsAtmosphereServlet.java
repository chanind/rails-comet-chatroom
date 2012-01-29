package atmosphere;

import javax.servlet.ServletConfig;
import org.atmosphere.cpr.AtmosphereHandler;
import org.atmosphere.cpr.AtmosphereServlet;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.BroadcasterFactory;
import org.atmosphere.di.InjectorProvider;
import javax.servlet.ServletException;
import java.net.URL;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLClassLoader;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class RailsAtmosphereServlet extends AtmosphereServlet{
	
	private static final Logger logger = LoggerFactory.getLogger(RailsAtmosphereServlet.class);
	
	protected void loadConfiguration(ServletConfig sc) throws ServletException {
        try {
            URL url = sc.getServletContext().getResource("/lib/rails_atmosphere.jar");
            URLClassLoader urlC = new URLClassLoader(new URL[]{url},
                    Thread.currentThread().getContextClassLoader());
            loadAtmosphereDotXml(sc.getServletContext().
                    getResourceAsStream("/META-INF/atmosphere.xml"), urlC);
        } catch (Throwable t) {
            throw new ServletException(t);
        }
    }
	
	protected void loadAtmosphereDotXml(InputStream stream, URLClassLoader c) throws IOException, ServletException {
		String handlerClassName = "atmosphere.ChatHandler";
		AtmosphereHandler handler;
		try {
			
			//handler = new ChatHandler();
			handler = (AtmosphereHandler) c.loadClass(handlerClassName).newInstance();
			String handlerPath = "/ChatHandler";
			
			InjectorProvider.getInjector().inject(handler);
			logger.info("Installed AtmosphereHandler {} mapped to context-path: {}", handler, handlerPath);
			
			Broadcaster b = BroadcasterFactory.getDefault().get(handlerPath);
	
	        AtmosphereHandlerWrapper wrapper = new AtmosphereHandlerWrapper(handler, b);
	        addMapping(handlerPath, wrapper);
		
		} catch (Throwable t) {
            logger.warn("unable to load AtmosphereHandler class: " + handlerClassName, t);
            throw new ServletException(t);
        }
    }
	
	private void addMapping(String path, AtmosphereHandlerWrapper w) {
        // We are using JAXRS mapping algorithm.
        if (path.contains("*")) {
            path = path.replace("*", "[/a-zA-Z0-9-&=;\\?]+");
        }

        atmosphereHandlers.put(path, w);
    }
	
}