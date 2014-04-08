/**
 * An hack of grails pagination so the links would be google search friendly
 *
 * @author Yiguang Hu
 * @since 18-Feb-2009
 */
import org.springframework.web.servlet.support.RequestContextUtils as RCU
import org.codehaus.groovy.grails.commons.GrailsControllerClass
import org.codehaus.groovy.grails.commons.ControllerArtefactHandler
import org.codehaus.groovy.grails.commons.ConfigurationHolder;



class JspaginateTagLib{
 static namespace = 'j'
 
	def out // to facilitate testing



	/**
	 * Creates next/previous links to support gpagination for the current controller
	 *
	 * <g:gpaginate total="${Account.count()}" />
	 */
	def gpaginate = { attrs ->
		def writer = out
        if(attrs.total == null)
            throwTagError("Tag [gpaginate] is missing required attribute [total]")

		def messageSource = grailsAttributes.getApplicationContext().getBean("messageSource")
		def locale = RCU.getLocale(request)

		def total = attrs.total.toInteger()
		def action = (attrs.action ? attrs.action : (params.action ? params.action : "list"))
		def offset = params.offset?.toInteger()
		def max = params.max?.toInteger()
		def maxsteps = (attrs.maxsteps ? attrs.maxsteps.toInteger() : 10)

		if(!offset) offset = (attrs.offset ? attrs.offset.toInteger() : 0)
		if(!max) max = (attrs.max ? attrs.max.toInteger() : 10)

		def linkParams = [offset:offset - max, max:max]
		if(params.sort) linkParams.sort = params.sort
		if(params.order) linkParams.order = params.order
		if(attrs.params) linkParams.putAll(attrs.params)

		def linkTagAttrs = [action:action]
		if(attrs.controller) {
			linkTagAttrs.controller = attrs.controller
		}
		if(attrs.id!=null) {
			linkTagAttrs.id = attrs.id
		}
		linkTagAttrs.params = linkParams

		// determine paging variables
		def steps = maxsteps > 0
		int currentstep = (offset / max) + 1
		int firststep = 1
		int laststep = Math.round(Math.ceil(total / max))

		// display previous link when not on firststep
		if(currentstep > firststep) {
			linkTagAttrs.class = 'prevLink'
			writer << glink(linkTagAttrs.clone()) {
//				(attrs.prev ? attrs.prev : messageSource.getMessage('paginate.prev', null, messageSource.getMessage('default.paginate.prev', null, 'Previous', locale), locale))
              (attrs.prev ? attrs.prev : '&laquo;' )

			 }
		}

		// display steps when steps are enabled and laststep is not firststep
		if(steps && laststep > firststep) {
			linkTagAttrs.class = 'step'

			// determine begin and endstep paging variables
			int beginstep = currentstep - Math.round(maxsteps / 2) + (maxsteps % 2)
			int endstep = currentstep + Math.round(maxsteps / 2) - 1

			if(beginstep < firststep) {
				beginstep = firststep
				endstep = maxsteps
			}
			if(endstep > laststep) {
				beginstep = laststep - maxsteps + 1
				if(beginstep < firststep) {
					beginstep = firststep
				}
				endstep = laststep
			}

			// display firststep link when beginstep is not firststep
			if(beginstep > firststep) {
				linkParams.offset = 0
				writer << glink(linkTagAttrs.clone()) {firststep.toString()}
				writer << '<span class="step">..</span>'
			}

			// display paginate steps
			(beginstep..endstep).each { i ->
				if(currentstep == i) {
					writer << "<span class=\"currentStep\">${i}</span>"
				}
				else {
					linkParams.offset = (i - 1) * max
					writer << glink(linkTagAttrs.clone()) {i.toString()}
				}
			}

			// display laststep link when endstep is not laststep
			if(endstep < laststep) {
				//YIGUANG ADD A HALF WAY LINK
				int i= currentstep+(int)(laststep-currentstep)/2
				if (i>endstep){
				writer << '<span class="step">..</span>' 
				linkParams.offset = i*max
				writer << glink(linkTagAttrs.clone()) { i.toString() }
				}

				writer << '<span class="step">..</span>'
				linkParams.offset = (laststep -1) * max
				writer << glink(linkTagAttrs.clone()) { laststep.toString() }
			}
		}

		// display next link when not on laststep
		if(currentstep < laststep) {
			linkTagAttrs.class = 'nextLink'
			linkParams.offset = offset + max
			writer << glink(linkTagAttrs.clone()) {
//				(attrs.next ? attrs.next : messageSource.getMessage('paginate.next', null, messageSource.getMessage('default.paginate.next', null, 'Next', locale), locale))
              (attrs.next ? attrs.next : '&raquo;' )

			}
		}

	}

  def grailsUrlMappingsHolder


 // static final SCOPES = [page:'pageScope',                        application:'servletContext',                         request:'request',                         session:'session',                         flash:'flash']



  /**
   *  General glinking to controllers, actions etc. Examples:
   *
   *  <g:glink action="myaction">link 1</gr:link>
   *  <g:glink controller="myctrl" action="myaction">link 2</gr:link>
   */
  def glink = { attrs, body ->
      def writer = out
      writer << '<a href="'
      // create the link
      if(request['flowExecutionKey']) {
          if(!attrs.params) attrs.params = [:]
          attrs.params."_flowExecutionKey" = request['flowExecutionKey']
      }

      writer << gcreateLink(attrs).encodeAsHTML()
      writer << '"'
      // process remaining attributes
      attrs.each { k,v ->
          writer << "/$k/\"$v\""
      }
      writer << '>'
      // output the body
      writer << body()
      // close tag
      writer << '</a>'
  }


  /**
   * Creates a grails application link from a set of attributes. This
   * link can then be included in links, ajax calls etc. Generally used as a method call
   * rather than a tag eg.
   *
   *  <a href="${gcreateLink(action:'list')}">List</a>
   */
  def gcreateLink = { attrs ->
      // prefer a URL attribute
      def urlAttrs = attrs
      if(attrs['url'] instanceof Map) {
          urlAttrs = attrs.remove('url').clone()
      }
      else if(attrs['url']) {
          urlAttrs = attrs.remove('url').toString()
      }

      if(urlAttrs instanceof String) {
          out << response.encodeURL(urlAttrs)
      }
      else {
          def controller = urlAttrs.containsKey("controller") ? urlAttrs.remove("controller") : grailsAttributes.getController(request)?.controllerName
          def action = urlAttrs.remove("action")
          if(controller && !action) {
              GrailsControllerClass controllerClass = grailsApplication.getArtefactByLogicalPropertyName(ControllerArtefactHandler.TYPE, controller)
              String defaultAction = controllerClass?.getDefaultAction()
              if(controllerClass?.hasProperty(defaultAction))
                  action = defaultAction
          }
          def id = urlAttrs.remove("id")
          def frag = urlAttrs.remove('fragment')
          def params = urlAttrs.params && urlAttrs.params instanceof Map ? urlAttrs.remove('params') : [:]

          if(urlAttrs.event) {
              params."_eventId" = urlAttrs.event
          }
          def url
          if(id != null) params.id = id
          //def urlMappings = applicationContext.getBean("grailsUrlMappingsHolder")
         def urlMappings= grailsAttributes.getApplicationContext().getBean("grailsUrlMappingsHolder")
           // println "mapping class:"+urlMappings.getClass().getName()
          def mapping = urlMappings.getReverseMapping(controller,action,params)
           // println "mapping name:"+mapping.getClass().getName()
             GenJSURL gu=new GenJSURL()
              gu.setMapping(mapping)
          url = gu.gcreateURL(controller, action, params, request.characterEncoding, frag)
          if (attrs.base) {
              out << attrs.remove('base')
          } else {
              handleAbsolute(attrs)
          }
          out << response.encodeURL(url)
      }

  }
  /**
     * Check for "absolute" attribute and render server URL if available from Config or deducible in non-production
     */
    private handleAbsolute(attrs) {
        def abs = attrs.remove("absolute")
        if (Boolean.valueOf(abs)) {
            def u = makeServerURL()
            if (u) {
                out << u
            } else {
                throwTagError("Attribute absolute='true' specified but no grails.serverURL set in Config")
            }
        }
    }
  /**
    * Get the declared URL of the server from config, or guess at localhost for non-production
    */
   String makeServerURL() {
       def u = ConfigurationHolder.config.grails.serverURL
       if (!u) {
           // Leave it null if we're in production so we can throw
           if (GrailsUtil.environment != GrailsApplication.ENV_PRODUCTION) {
               u = "http://localhost:" +(System.getProperty('server.port') ? System.getProperty('server.port') : "8080")
           }
       }
       return u
   }
 

}
