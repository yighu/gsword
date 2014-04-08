/*
 * Copyright 2004-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Gant script that generates i18n messages for domain classes
 * 
 * @author Marcel Overdijk
 */

import org.codehaus.groovy.grails.commons.GrailsClassUtils as GCU
import org.codehaus.groovy.grails.commons.spring.GrailsRuntimeConfigurator;
import org.codehaus.groovy.grails.scaffolding.DomainClassPropertyComparator;
import org.codehaus.groovy.grails.validation.ConstrainedProperty

Ant.property(environment:"env")                             
grailsHome = Ant.antProject.properties."env.GRAILS_HOME"    

includeTargets << new File ( "${grailsHome}/scripts/Bootstrap.groovy" )

target('default': "Generates i18n messages for domain classes") {
    depends(checkVersion, packageApp)
    typeName = "Domain Class"
    promptForName()    
    generateI18nMessages()
    
}

target(generateI18nMessages: "The implementation target") {

    rootLoader.addURL(classesDir.toURI().toURL())
    loadApp()

    def name = args.trim()
    if (!name || name == "*") {
        uberGenerate()
    }
    else {
        generateForOne(name: name)
    }    
}
    
target(generateForOne: "Generates i18n messages for only one domain class") { Map args ->   
    def name = args["name"]
    name = name.indexOf('.') > -1 ? name : GCU.getClassNameRepresentation(name)
    def domainClass = grailsApp.getDomainClass(name)
    
    if(!domainClass) {
        println "Domain class not found in grails-app/domain, trying hibernate mapped classes..."
        doRuntimeConfig(grailsApp, appCtx)
        domainClass = grailsApp.getDomainClass(name)
    }
    
    if(domainClass) {
        generateForDomainClass(domainClass)
        event("StatusFinal", ["Finished generation for domain class ${domainClass.fullName}. Copy messages to appropriate resource bundle(s)"])        
    }
    else {
        event("StatusFinal", ["No domain class found for name ${name}. Please try again and enter a valid domain class name"])
    }
}

target(uberGenerate: "Generates i18n messages for all domain classes") {
    def domainClasses = grailsApp.domainClasses

    if (!domainClasses) {
        println "No domain classes found in grails-app/domain, trying hibernate mapped classes..."
        doRuntimeConfig(grailsApp, appCtx)
        domainClasses = grailsApp.domainClasses
    }

   if (domainClasses) {
        domainClasses.each { domainClass ->
            generateForDomainClass(domainClass)
        }
        event("StatusFinal", ["Finished generation for domain classes. Copy messages to appropriate resource bundle(s)"])
    }
    else {
        event("StatusFinal", ["No domain classes found"])
    }
}

def doRuntimeConfig(app, ctx) {
    try {
        def config = new GrailsRuntimeConfigurator(app, ctx)
        appCtx = config.configure(ctx.servletContext)
    }
    catch (Exception e) {
        println e.message
        e.printStackTrace()
    }
}

def generateForDomainClass(domainClass) {
    // print generic messages for this domain class
    println "# ${domainClass.shortName} messages"
    println "${domainClass.propertyName}.create=Create ${domainClass.shortName}"
    println "${domainClass.propertyName}.edit=Edit ${domainClass.shortName}"
    println "${domainClass.propertyName}.list=${domainClass.shortName} List"
    println "${domainClass.propertyName}.new=New ${domainClass.shortName}"
    println "${domainClass.propertyName}.show=Show ${domainClass.shortName}"
    println "${domainClass.propertyName}.created=${domainClass.shortName} {0} created"
    println "${domainClass.propertyName}.updated=${domainClass.shortName} {0} updated"
    println "${domainClass.propertyName}.deleted=${domainClass.shortName} {0} deleted"
    println "${domainClass.propertyName}.not.found=${domainClass.shortName} not found with id {0}"

    // print messages for all properties contained by domain class
    props = domainClass.properties.findAll { it.name != 'version' }
    Collections.sort(props, new DomainClassPropertyComparator(domainClass))
    props.each { p ->
        println "${domainClass.propertyName}.${p.name}=${p.naturalName}"

        // print messages for inList constaint values
        cp = domainClass.constrainedProperties[p.name]
        if(cp?.inList) {
            cp.inList.each { v ->
                println "${domainClass.propertyName}.${p.name}.${v}=${v}"
            }
        }
        
        // print error messages for constraints
        cp?.appliedConstraints?.each { c ->
            switch (c.name) {
                case ConstrainedProperty.BLANK_CONSTRAINT:
                    println "${domainClass.propertyName}.${p.name}.${c.name}.error=Property [${p.naturalName}] of class [${domainClass.shortName}] cannot be blank"
                    break
                case ConstrainedProperty.CREDIT_CARD_CONSTRAINT:
                    println "${domainClass.propertyName}.${p.name}.${c.name}.error=Property [${p.naturalName}] of class [${domainClass.shortName}] with value [{2}] is not a valid credit card number"
                    break
                case ConstrainedProperty.EMAIL_CONSTRAINT:
                    println "${domainClass.propertyName}.${p.name}.${c.name}.error=Property [${p.naturalName}] of class [${domainClass.shortName}] with value [{2}] is not a valid e-mail address"
                    break
                case ConstrainedProperty.IN_LIST_CONSTRAINT:
                    println "${domainClass.propertyName}.${p.name}.${c.name}.error=Property [${p.naturalName}] of class [${domainClass.shortName}] with value [{2}] is not contained within the list [{3}]"
                    break
                case ConstrainedProperty.MATCHES_CONSTRAINT:
                    println "${domainClass.propertyName}.${p.name}.${c.name}.error=Property [${p.naturalName}] of class [${domainClass.shortName}] with value [{2}] does not match the required pattern [{3}]"
                    break
                case ConstrainedProperty.MAX_CONSTRAINT:
                    println "${domainClass.propertyName}.${p.name}.${c.name}.error=Property [${p.naturalName}] of class [${domainClass.shortName}] with value [{2}] exceeds maximum value [{3}]"
                    break
                case ConstrainedProperty.MAX_SIZE_CONSTRAINT:
                    println "${domainClass.propertyName}.${p.name}.${c.name}.error=Property [${p.naturalName}] of class [${domainClass.shortName}] with value [{2}] exceeds the maximum size of [{3}]"
                    break
                case ConstrainedProperty.MIN_CONSTRAINT:
                    println "${domainClass.propertyName}.${p.name}.${c.name}.error=Property [${p.naturalName}] of class [${domainClass.shortName}] with value [{2}] is less than minimum value [{3}]"
                    break
                case ConstrainedProperty.MIN_SIZE_CONSTRAINT:
                    println "${domainClass.propertyName}.${p.name}.${c.name}.error=Property [${p.naturalName}] of class [${domainClass.shortName}] with value [{2}] is less than the minimum size of [{3}]"
                    break
                case ConstrainedProperty.NOT_EQUAL_CONSTRAINT:
                    println "${domainClass.propertyName}.${p.name}.${c.name}.error=Property [${p.naturalName}] of class [${domainClass.shortName}] with value [{2}] cannot equal [{3}]"
                    break
                case ConstrainedProperty.NULLABLE_CONSTRAINT:
                    println "${domainClass.propertyName}.${p.name}.${c.name}.error=Property [${p.naturalName}] of class [${domainClass.shortName}] cannot be null"
                    break
                case ConstrainedProperty.RANGE_CONSTRAINT:
                    println "${domainClass.propertyName}.${p.name}.${c.name}.error=Property [${p.naturalName}] of class [${domainClass.shortName}] with value [{2}] does not fall within the valid range from [{3}] to [{4}]"
                    break
                case ConstrainedProperty.SIZE_CONSTRAINT:
                    println "${domainClass.propertyName}.${p.name}.${c.name}.error=Property [${p.naturalName}] of class [${domainClass.shortName}] with value [{2}] does not fall within the valid size range from [{3}] to [{4}]"
                    break
                //case ConstrainedProperty.UNIQUE_CONSTRAINT: // unique constraint reference not available
                //    println "${domainClass.propertyName}.${p.name}.${c.name}.error=Property [${p.naturalName}] of class [${domainClass.shortName}] with value [{2}] must be unique"
                //    break
                case ConstrainedProperty.URL_CONSTRAINT:
                    println "${domainClass.propertyName}.${p.name}.${c.name}.error=Property [${p.naturalName}] of class [${domainClass.shortName}] with value [{2}] is not a valid URL"
                    break
                case ConstrainedProperty.VALIDATOR_CONSTRAINT:
                    println "${domainClass.propertyName}.${p.name}.${c.name}.error=Property [${p.naturalName}] of class [${domainClass.shortName}] with value [{2}] does not pass custom validation"
                    break
            }        
        }
    }
    println ""  
}