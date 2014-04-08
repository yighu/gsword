import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.grails.validation.ConstrainedProperty;
import org.codehaus.groovy.grails.web.mapping.RegexUrlMapping;
import org.codehaus.groovy.grails.web.mapping.UrlMapping;
import org.codehaus.groovy.grails.web.mapping.exceptions.UrlMappingException;
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest;
import org.codehaus.groovy.grails.web.servlet.mvc.exceptions.ControllerExecutionException;
import org.springframework.web.context.request.RequestContextHolder;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

public class GenURL{

    private static final String CAPTURED_WILDCARD = "(*)";
    private static final String SLASH = "/";
    private static final String DEFAULT_ENCODING = "UTF-8";

    private static final String CAPTURED_DOUBLE_WILDCARD = "(**)";
    private static final Log LOG = LogFactory.getLog(GenURL.class);
    private RegexUrlMapping mapping;
    public void setMapping(RegexUrlMapping mp){
	 mapping=mp;
	}
    public RegexUrlMapping getMapping(){
	return mapping ;
	}
    /**
     * @see org.codehaus.groovy.grails.web.mapping.UrlMapping
     */
    public String gcreateURL(Map parameterValues, String encoding) {
        return gcreateURLInternal(parameterValues, encoding, true);
    }

    private String gcreateURLInternal(Map parameterValues, String encoding, boolean includeContextPath) {

        if(encoding == null) encoding = "utf-8";

        String contextPath = "";
        if(includeContextPath) {
            GrailsWebRequest webRequest = (GrailsWebRequest) RequestContextHolder.getRequestAttributes();
            if(webRequest != null) {
                contextPath = webRequest.getAttributes().getApplicationUri(webRequest.getCurrentRequest());
            }
        }
        if(parameterValues==null)parameterValues= Collections.EMPTY_MAP;
        StringBuffer uri = new StringBuffer(contextPath);
        Set usedParams = new HashSet();

        String[] tokens = mapping.getUrlData().getTokens();

         int paramIndex = 0;
  for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            if(CAPTURED_WILDCARD.equals(token) || CAPTURED_DOUBLE_WILDCARD.equals(token)) {
              
                ConstrainedProperty prop = mapping.getConstraints()[paramIndex++];
                String propName = prop.getPropertyName();
                Object value = parameterValues.get(propName);
                usedParams.add(propName);
                if(value == null && !prop.isNullable())
                    throw new UrlMappingException("Unable to create URL for mapping ["+this+"] and parameters ["+parameterValues+"]. Parameter ["+prop.getPropertyName()+"] is required, but was not specified!");
                else if(value == null)
                    break;
                try {
                    uri.append(SLASH).append(URLEncoder.encode(value.toString(), encoding));
                } catch (UnsupportedEncodingException e) {
                    throw new ControllerExecutionException("Error creating URL for parameters ["+parameterValues +"], problem encoding URL part ["+value +"]: " + e.getMessage(),e);
                }
            }
            else
            {
                uri.append(SLASH).append(token);
            }

        }
        gpopulateParameterList(parameterValues, encoding, uri, usedParams);

        if(LOG.isDebugEnabled()) {
            LOG.debug("Created reverse URL mapping ["+uri.toString()+"] for parameters ["+parameterValues+"]");
        }
        return  uri.toString();
    }

    public String gcreateURL(Map parameterValues, String encoding, String fragment) {
        String url = gcreateURL(parameterValues, encoding);
        return gcreateUrlWithFragment(url, fragment, encoding);
    }

    public String gcreateURL(String controller, String action, Map parameterValues, String encoding) {
        return gcreateURLInternal(controller, action, parameterValues, encoding, true);
    }

    private String gcreateURLInternal(String controller, String action, Map parameterValues, String encoding, boolean includeContextPath) {
        if(parameterValues == null) parameterValues = new HashMap();

        boolean hasController = !StringUtils.isBlank(controller);
        boolean hasAction = !StringUtils.isBlank(action);

        try {

            if(hasController)
                parameterValues.put(UrlMapping.CONTROLLER, controller);
            if(hasAction)
                parameterValues.put(UrlMapping.ACTION, action);

            return gcreateURLInternal(parameterValues, encoding, includeContextPath);
        } finally {
            if(hasController)
                parameterValues.remove(UrlMapping.CONTROLLER);
            if(hasAction)
                parameterValues.remove(UrlMapping.ACTION);

        }
    }

    public String gcreateRelativeURL(String controller, String action, Map parameterValues, String encoding) {
        return gcreateURLInternal(controller, action, parameterValues, encoding, false);
    }

    public String gcreateURL(String controller, String action, Map parameterValues, String encoding, String fragment) {
        String url = gcreateURL(controller, action, parameterValues, encoding);
        return gcreateUrlWithFragment(url, fragment, encoding);
    }

    private String gcreateUrlWithFragment(String url, String fragment, String encoding) {
        if(fragment != null) {
            // A 'null' encoding will cause an exception, so default to 'UTF-8'.
            if (encoding == null) {
                encoding = DEFAULT_ENCODING;
            }

            try {
                return url + '#' + URLEncoder.encode(fragment, encoding);
            } catch (UnsupportedEncodingException ex) {
                throw new ControllerExecutionException("Error creating URL  ["+url +"], problem encoding URL fragment ["+fragment +"]: " + ex.getMessage(),ex);
            }
        }
        else {
            return url;
        }
    }


    private void gpopulateParameterList(Map parameterValues, String encoding, StringBuffer uri, Set usedParams) {
        boolean addedParams = false;
        usedParams.add( "controller" );
        usedParams.add( "action" );
        usedParams.add( "max" );

        // A 'null' encoding will cause an exception, so default to 'UTF-8'.
        if (encoding == null) {
            encoding = DEFAULT_ENCODING;
        }
	Object ofset=null;
	String OFFSET="offset";
	if(parameterValues.containsKey(OFFSET)) {
	ofset=parameterValues.get(OFFSET);
	parameterValues.remove(OFFSET);
	}
        for (Iterator i = parameterValues.keySet().iterator(); i.hasNext();) {
            String name = i.next().toString();
            if(!usedParams.contains(name)) {
                if(!addedParams) {
                    uri.append(SLASH); //YIGUANG CHANGED IT from QUESTION_MARK
                    addedParams = true;
                }
                else {
                    //uri.append(AMPERSAND);
                            uri.append(SLASH); //YIGUANG CHANGED IT from AMPERSAND
                }
                Object value = parameterValues.get(name);
                if(value != null && value instanceof Collection) {
                    Collection multiValues = (Collection)value;
                    for (Iterator j = multiValues.iterator(); j.hasNext();) {
                        Object o = j.next();
                        gappendValueToURI(encoding, uri, name, o);
                        if(j.hasNext()) {
                            uri.append(SLASH); //YIGUANG CHANGED IT from AMPERSAND

                        }
                    }
                }
                else if(value!= null && value.getClass().isArray()) {
                    Object[] multiValues = (Object[])value;
                    for (int j = 0; j < multiValues.length; j++) {
                        Object o = multiValues[j];
                        gappendValueToURI(encoding, uri, name, o);
                        if(j+1 < multiValues.length) {
                            //uri.append(AMPERSAND);
                            uri.append(SLASH); //YIGUANG CHANGED IT from AMPERSAND
                        }
                    }
                }
                else {
                    gappendValueToURI(encoding, uri, name, value);
                }
            }

        }
	if(ofset!=null){
                    uri.append(SLASH).append(ofset);
	}
    }

    private void gappendValueToURI(String encoding, StringBuffer uri, String name, Object value) {
        try {
            //uri.append(URLEncoder.encode(name,encoding)).append('=').append(URLEncoder.encode(value != null ? value.toString() : "",encoding));//YIGUANG ORIGINA
            uri.append(URLEncoder.encode(value != null ? value.toString() : "",encoding));
        } catch (UnsupportedEncodingException e) {
            throw new ControllerExecutionException("Error redirecting request for url ["+name+":"+value +"]: " + e.getMessage(),e);
        }
    }

}
