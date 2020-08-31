package org.cssblab.multislide.filters;

import java.io.File;
import java.util.Collections;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import org.cssblab.multislide.structure.AnalysisContainer;
import org.cssblab.multislide.structure.Serializer;
import org.cssblab.multislide.utils.Utils;

/**
 *
 * @author soumitag
 */
public class MultislideSessionListener implements HttpSessionListener {
    @Override
    public void sessionDestroyed(final HttpSessionEvent event) {
        /*
        HttpSession session = event.getSession();
        for (Object attr_name : Collections.list(session.getAttributeNames())) {
            Object attr = session.getAttribute((String)attr_name);
            if (attr instanceof AnalysisContainer) {
                AnalysisContainer analysis = (AnalysisContainer)attr;
                String analysis_filepath = analysis.base_path + File.separator  + "data";
                Serializer serializer = new Serializer();
                serializer.serializeAnalysis(analysis, analysis_filepath);
            }
        }
        */
    }

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        Utils.log_info("Created new session");
    }
}
