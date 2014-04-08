<%--
  Created by IntelliJ IDEA.
  User: Yiguang
  Date: Jul 8, 2009
  Time: 9:29:22 PM
  To change this template use File | Settings | File Templates.
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
  <head><title>Simple GSP page</title></head>
  <body>Place your content here</body>
<div class="paginateButtons">
     <span class="pager-list"><strong class="pager-current">
                       <c:gpaginate total="${books.size()}" controller="bible" action="bbls" params="['':params.pth]" />
        </strong>
     </span></div>
</html>