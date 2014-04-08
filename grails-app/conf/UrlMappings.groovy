class UrlMappings {
    static mappings = {

  "/bible/readgen/$book?/$chapter?/$verse?"
                {
                    controller = "bible"
                    action = "readgen"
                    constraints {
                        // apply constraints here
                    }
                }
      "/bible/read/$version?/$book?/$chapter?/$verse?"
                {
                    controller = "bible"
                    action = "read"
                    constraints {
                        // apply constraints here
                    }
                }
      "/bible/cmnt/$version?/$book?/$chapter?/$verse?"
                   {
                       controller = "bible"
                       action = "cmnt"
                       constraints {
                           // apply constraints here
                       }
                   }

       "/bible/search/$vk?/$offset?"
                {
                    controller = "bible"
                    action = "search"
                    constraints {
                        // apply constraints here
                    }
                }

       "/gbook/searchdics/$dic?/$offset?"
                      {
                          controller = "gbook"
                          action = "searchdics"
                          constraints {
                              // apply constraints here
                          }
                      }


       "/bible/search"
                {
                    controller = "bible"
                    action = "search"
                    constraints {
                        // apply constraints here
                    }
                }
     "/$controller/$action?/$id?"{
	      constraints {
			 // apply constraints here
		  }
	  }
	  "500"(view:'/error')
	}
}
